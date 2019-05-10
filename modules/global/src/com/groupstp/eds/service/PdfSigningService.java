package com.groupstp.eds.service;


import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

public interface PdfSigningService {
    String NAME = "eds_PdfSigningService";

    byte[] sign(byte[] fileToSign, String location, String contact, String reason, boolean signAppearanceVisible)
            throws KeyStoreException, NoSuchAlgorithmException, SignatureException;
}