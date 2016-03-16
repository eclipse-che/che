/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.crypt.server;

import java.io.UnsupportedEncodingException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.text.MessageFormat;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.che.api.crypt.shared.EncryptResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JCA/Java Crypto Architecture based encryption service.
 */
public class JCEEncryptTextService implements EncryptTextService {

    private static final Logger LOG = LoggerFactory.getLogger(JCEEncryptTextService.class);

    private final String secretKeyFactoryAlgorithm;
    private final String cipher;
    private final String transformation;

    private final int schemeVersion;

    /**
     * The salt for the encyption.<br>
     * Not a secret, must be the same for encypt and decrypt phases.
     */
    private final byte[] salt;

    /**
     * The size of the cipher key.
     */
    private int keySize;

    public JCEEncryptTextService(final String secretKeyFactoryAlgorithm,
                              final String secretKeyAlgorithm,
                              final String transformation,
                              final byte[] salt,
                                     final int keySize,
                              final int schemeVersion) {
        if (salt == null || salt.length == 0) {
            throw new IllegalArgumentException("salt can't be empty");
        }
        this.secretKeyFactoryAlgorithm = secretKeyFactoryAlgorithm;
        this.cipher = secretKeyAlgorithm;
        this.transformation = transformation;
        this.salt = salt;
        this.keySize = keySize;
        this.schemeVersion = schemeVersion;
    }

    private char[] getMasterPassword() {
        String masterPwd = System.getProperty("com.codenvy.security.masterpwd");
        if (masterPwd == null) {
            // complain
            LOG.warn("Master password 'com.codenvy.security.masterpwd' was not defined, using weak and universally know default one!");
            masterPwd = "changeMe";
        }
        return masterPwd.toCharArray();
    }

    @Override
    public EncryptResult encryptText(final String textToEncrypt) throws EncryptException {
        byte[] ivBlob;
        byte[] cipherBlob;
        try {
            final SecretKey secret = generateSecret();

            final Cipher cipher = Cipher.getInstance(this.transformation);
            cipher.init(Cipher.ENCRYPT_MODE, secret);

            final AlgorithmParameters params = cipher.getParameters();
            ivBlob = params.getParameterSpec(IvParameterSpec.class).getIV();
            cipherBlob = cipher.doFinal(textToEncrypt.getBytes("UTF-8"));
        } catch (final InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException
            | InvalidParameterSpecException | IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e) {
            throw new EncryptException(e);
        }

        final String cipherText = Base64.encodeBase64String(cipherBlob);
        final String ivText = Base64.encodeBase64String(ivBlob);

        return new EncryptResult(cipherText, ivText);
    }

    @Override
    public String decryptText(final EncryptResult toDecrypt) throws EncryptException {
        if (toDecrypt.getInitVector() == null || toDecrypt.getCipherText() == null) {
            throw new EncryptException("Incorrect encrypt result for this scheme");
        }
        final byte[] ivBlob = Base64.decodeBase64(toDecrypt.getInitVector());
        final byte[] cipherBlob = Base64.decodeBase64(toDecrypt.getCipherText());
        String plainText;
        try {
            final SecretKey secret = generateSecret();
            final Cipher cipher = Cipher.getInstance(this.transformation);
            cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(ivBlob));
            plainText = new String(cipher.doFinal(cipherBlob), "UTF-8");
        } catch (final NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException
            | InvalidKeySpecException | UnsupportedEncodingException | IllegalBlockSizeException | BadPaddingException e) {
            throw new EncryptException(e);
        }

        return plainText;
    }

    private SecretKey generateSecret() throws NoSuchAlgorithmException, InvalidKeySpecException {
        final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(this.secretKeyFactoryAlgorithm);
        final KeySpec keySpec = new PBEKeySpec(getMasterPassword(), this.salt, 65536, this.keySize);
        final SecretKey tempKey = keyFactory.generateSecret(keySpec);
        final SecretKey secret = new SecretKeySpec(tempKey.getEncoded(), this.cipher);
        return secret;
    }

    @Override
    public int getSchemeVersion() {
        return this.schemeVersion;
    }

    @Override
    public boolean isActive() {
        try {
            final int keyMaxLength = Cipher.getMaxAllowedKeyLength(this.cipher);
            boolean active = (keyMaxLength >= this.keySize);
            if (active) {
                LOG.debug("Encryption scheme {} enabled - max. allowed keysize={}", this.toString(), keyMaxLength);
            } else {
                LOG.debug("Encryption scheme {} disabled - max. allowed keysize={}", this.toString(), keyMaxLength);
            }
            return active;
        } catch (final NoSuchAlgorithmException e) {
            // cipher is not available
            LOG.debug("Encryption scheme {} disabled - algorithm is not available", this.toString());
            return false;
        }
    }

    @Override
    public String toString() {
        return MessageFormat.format("{0}:{1}({2})+{3}+{4})", this.schemeVersion, this.cipher,
                                    this.keySize, this.secretKeyFactoryAlgorithm, this.transformation);
    }
}

