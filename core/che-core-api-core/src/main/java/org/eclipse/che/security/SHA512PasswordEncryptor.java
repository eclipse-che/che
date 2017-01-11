/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.security;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.primitives.Bytes;

import java.nio.charset.Charset;
import java.security.SecureRandom;

import static java.util.Objects.requireNonNull;

/**
 * SHA-512 based encryptor {@code hash = sha512(password + salt) + salt}.
 *
 * @author Yevhenii Voevodin
 */
public class SHA512PasswordEncryptor implements PasswordEncryptor {

    /** 64 bit salt length is based on the <a href="https://www.ietf.org/rfc/rfc2898.txt">source</a>. */
    private static final int          SALT_BYTES_LENGTH               = 64 / 8;
    /** SHA-512 produces 512 bits. */
    private static final int          ENCRYPTED_PASSWORD_BYTES_LENGTH = 512 / 8;
    private static final SecureRandom SECURE_RANDOM                   = new SecureRandom();
    private static final Charset      PWD_CHARSET                     = Charset.forName("UTF-8");

    @Override
    public String encrypt(String password) {
        requireNonNull(password, "Required non-null password");
        // generate salt
        final byte[] salt = new byte[SALT_BYTES_LENGTH];
        SECURE_RANDOM.nextBytes(salt);
        // sha512(password + salt)
        final HashCode hash = Hashing.sha512().hashBytes(Bytes.concat(password.getBytes(PWD_CHARSET), salt));
        final HashCode saltHash = HashCode.fromBytes(salt);
        // add salt to the hash, result length (512 / 8) * 2 + (64 / 8) * 2 = 144
        return hash.toString() + saltHash.toString();
    }

    @Override
    public boolean test(String password, String passwordHash) {
        requireNonNull(password, "Required non-null password");
        requireNonNull(passwordHash, "Required non-null password's hash");
        // retrieve salt from the hash
        final int passwordHashLength = ENCRYPTED_PASSWORD_BYTES_LENGTH * 2;
        if (passwordHash.length() < passwordHashLength + SALT_BYTES_LENGTH * 2) {
            return false;
        }
        final HashCode saltHash = HashCode.fromString(passwordHash.substring(passwordHashLength));
        // sha1(password + salt)
        final HashCode hash = Hashing.sha512().hashBytes(Bytes.concat(password.getBytes(PWD_CHARSET), saltHash.asBytes()));
        // test sha1(password + salt) + salt == passwordHash
        return (hash.toString() + saltHash.toString()).equals(passwordHash);
    }
}
