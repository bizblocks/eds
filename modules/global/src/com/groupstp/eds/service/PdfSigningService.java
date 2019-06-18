package com.groupstp.eds.service;


import com.haulmont.cuba.core.global.FileStorageException;

import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

public interface PdfSigningService {
    String NAME = "eds_PdfSigningService";

    /**
     * Подписывает электронный документ pdf электронной подписью по ГОСТ Р 34.10-2012
     * @param fileToSign файл, который необходимо подписать ЭП.
     * @param location одноименное поле ЭП, которое отображается в информации о подписи.
     * @param contact одноименное поле ЭП, которое отображается в информации о подписи.
     * @param reason одноименное поле ЭП, которое отображается в информации о подписи.
     * @param signAppearanceVisible если истина, на первой странице, подписанного документа, будет
     *                              отображен "штамп" с информацией о подписи. Имейте в виду, что информация
     *                              из полей location, contact и reason будет отображена кореектно только
     *                              в случае, если они заданы латиницей.
     * @return - подписанный ЭП файл в виде массива байт.
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws SignatureException
     */
    byte[] sign(byte[] fileToSign, String location, String contact, String reason, boolean signAppearanceVisible)
            throws KeyStoreException, NoSuchAlgorithmException, SignatureException, FileStorageException;
}