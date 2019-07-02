package com.groupstp.eds.service;

import com.google.common.base.Strings;
import com.groupstp.eds.config.EdsServiceConfig;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.app.FileStorageAPI;
import com.haulmont.cuba.core.app.FileStorageService;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.FileLoader;
import com.haulmont.cuba.core.global.FileStorageException;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.security.DigestAlgorithms;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.PdfPKCS7;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import ru.CryptoPro.JCP.JCP;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Calendar;
import java.util.HashMap;


@Service(PdfSigningService.NAME)
public class PdfSigningServiceBean implements PdfSigningService {

    @Inject
    private EdsServiceConfig edsServiceConfig;
    @Inject
    private Persistence persistence;
    @Inject
    private Logger log;
    @Inject
    private FileStorageAPI fileStorageAPI;

    @Override
    public byte[] sign(byte[] fileToSign, String location, String contact, String reason, boolean signAppearanceVisible)
            throws KeyStoreException, NoSuchAlgorithmException, SignatureException, FileStorageException {

        final String ksPassword = edsServiceConfig.getKeyStorePassword();
        final String containerPassword = edsServiceConfig.getContainerPassword();
        final KeyStore keyStore = getKeyStore(ksPassword);
        String alias = edsServiceConfig.getContainerAlias();
        if (Strings.isNullOrEmpty(alias))
            alias = keyStore.aliases().nextElement();
        final PrivateKey key = getKey(keyStore, alias, containerPassword);
        final Certificate[] chain = getChain(keyStore, alias);
        final String keyAlgorithm = key.getAlgorithm();

        final String hashAlgorithm = getHashAlgorithm(keyAlgorithm);

        return sign(key, hashAlgorithm, chain, fileToSign, location, reason, contact, true, signAppearanceVisible);
    }

    private byte[] sign(PrivateKey privateKey, String hashAlgorithm,
                        Certificate[] chain, byte[] fileToSign,
                        String location, String reason, String contact, boolean append,
                        boolean signAppearanceVisible)
            throws SignatureException, FileStorageException {

        final Slf4JStopWatch stopWatch = new Slf4JStopWatch(log);
        stopWatch.start("sign");

        PdfStamper stp;
        PdfReader reader;
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            reader = new PdfReader(fileToSign);
            stp = append
                    ? PdfStamper.createSignature(reader, outputStream, '\0', null, true)
                    : PdfStamper.createSignature(reader, outputStream, '\0');
        } catch (IOException | DocumentException e) {
            throw new SignatureException("Error while creating PdfReader");
        }

        PdfSignatureAppearance sap = stp.getSignatureAppearance();
        sap.setCertificate(chain[0]);
        sap.setReason(reason);
        sap.setLocation(location);
        sap.setContact(contact);
        if (edsServiceConfig.isUseImage()) {
            sap.setRenderingMode(PdfSignatureAppearance.RenderingMode.GRAPHIC);
            setSapImage(sap);
        }
        else
            sap.setRenderingMode(PdfSignatureAppearance.RenderingMode.DESCRIPTION);
        if (signAppearanceVisible)
            sap.setVisibleSignature(
                    new Rectangle(
                            edsServiceConfig.getAppearanceRectangleCoordinates().get(0),
                            edsServiceConfig.getAppearanceRectangleCoordinates().get(1),
                            edsServiceConfig.getAppearanceRectangleCoordinates().get(2),
                            edsServiceConfig.getAppearanceRectangleCoordinates().get(3)),
                    edsServiceConfig.isPlacedInLastPage() ? reader.getNumberOfPages() : 1,
                    null);

        PdfSignature dic = new PdfSignature(PdfName.ADOBE_CryptoProPDF, PdfName.ADBE_PKCS7_DETACHED);
        dic.setReason(sap.getReason());
        dic.setLocation(sap.getLocation());
        dic.setSignatureCreator(sap.getSignatureCreator());
        dic.setContact(sap.getContact());
        dic.setDate(new PdfDate(sap.getSignDate())); // time-stamp will over-rule this

        sap.setCryptoDictionary(dic);
        int estimatedSize = 8192;

        HashMap<PdfName, Integer> exc = new HashMap<>();
        exc.put(PdfName.CONTENTS, estimatedSize * 2 + 2);

        InputStream data;
        try {
            sap.preClose(exc);
            data = sap.getRangeStream();
        } catch (IOException | DocumentException e) {
            throw new SignatureException("Error while sap.preClose");
        }

        PdfPKCS7 sgn;
        byte[] hash;
        try {
            sgn = new PdfPKCS7(privateKey, chain, hashAlgorithm, JCP.PROVIDER_NAME, null, false);
            MessageDigest md = MessageDigest.getInstance(hashAlgorithm);
            hash = DigestAlgorithms.digest(data, md);
        } catch (GeneralSecurityException | IOException e) {
            throw new SignatureException("Error while getting signature");
        }
        Calendar cal = Calendar.getInstance();
        byte[] sh = sgn.getAuthenticatedAttributeBytes(hash, cal,
                null, null, MakeSignature.CryptoStandard.CMS);
        sgn.update(sh, 0, sh.length);
        byte[] encodedSig = sgn.getEncodedPKCS7(hash, cal);
        if (estimatedSize < encodedSig.length) throw new SignatureException("Not enough space");
        byte[] paddedSig = new byte[estimatedSize];
        System.arraycopy(encodedSig, 0, paddedSig, 0, encodedSig.length);
        PdfDictionary dic2 = new PdfDictionary();
        dic2.put(PdfName.CONTENTS, new PdfString(paddedSig).setHexWriting(true));

        try {
            sap.close(dic2);
            stp.close();
            outputStream.close();
            reader.close();
        } catch (IOException | DocumentException e) {
            throw new SignatureException("Error while closing streams");
        }

        stopWatch.stop();

        return outputStream.toByteArray();
    }

    private void setSapImage(PdfSignatureAppearance sap) throws FileStorageException {
        final FileDescriptor fileDescriptor = persistence.callInTransaction(em ->
                em.find(FileDescriptor.class, edsServiceConfig.getImageId()));

        if (fileDescriptor != null && fileStorageAPI.fileExists(fileDescriptor)){
            log.info("Image exists in file storage");
            try {
                byte[] bytes = fileStorageAPI.loadFile(fileDescriptor);
                Image image = Image.getInstance(bytes);
                sap.setSignatureGraphic(image);
            } catch (IOException | BadElementException e) {
                log.error("Error while getting/setting Signature Graphic", e);
            }
        }
    }

    private String getHashAlgorithm(String keyAlgorithm) throws NoSuchAlgorithmException {
        final HashMap<String, String> signDigestMap = new HashMap<>();
        signDigestMap.put(JCP.GOST_EL_2012_256_NAME, JCP.GOST_DIGEST_2012_256_NAME);
        signDigestMap.put(JCP.GOST_DH_2012_256_NAME, JCP.GOST_DIGEST_2012_256_NAME);
        signDigestMap.put(JCP.GOST_EL_2012_512_NAME, JCP.GOST_DIGEST_2012_512_NAME);
        signDigestMap.put(JCP.GOST_DH_2012_512_NAME, JCP.GOST_DIGEST_2012_512_NAME);

        final String hashAlgorithm = signDigestMap.get(keyAlgorithm);
        if (hashAlgorithm == null)
            throw new NoSuchAlgorithmException("Can`t find correct algorithm");
        return hashAlgorithm;
    }


    private static Certificate[] getChain(KeyStore keyStore, String alias) throws KeyStoreException {
        try {
            return keyStore.getCertificateChain(alias);
        } catch (KeyStoreException e) {
            throw new KeyStoreException("Error while loading certificate chain");
        }
    }

    private static KeyStore getKeyStore(String password) throws KeyStoreException {
        try {
            KeyStore keyStore = KeyStore.getInstance(JCP.HD_STORE_NAME);
            keyStore.load(null, Strings.isNullOrEmpty(password) ? null : password.toCharArray());
            return keyStore;

        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            throw new KeyStoreException("Error while loading keystore");
        }
    }

    private static PrivateKey getKey(KeyStore keyStore, String alias, String password) throws KeyStoreException {
        try {
            final PrivateKey key = (PrivateKey) keyStore.getKey(alias, password.toCharArray());
            if (key == null)
                throw new KeyStoreException("Can`t find key");
            return key;
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            throw new KeyStoreException("Error while loading key");
        }
    }

}