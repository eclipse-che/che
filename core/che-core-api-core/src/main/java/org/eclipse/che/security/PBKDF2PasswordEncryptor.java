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
package org.eclipse.che.security;

import com.google.common.hash.HashCode;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.primitives.Ints.tryParse;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Encrypts password using <a href="https://en.wikipedia.org/wiki/PBKDF2">Password-based-Key-Derivative-Function</a>
 * with <b>SHA512</b> as pseudorandom function.
 * See <a href="https://www.ietf.org/rfc/rfc2898.txt">rfc2898</a>.
 *
 * @author Yevhenii Voevodin
 */
public class PBKDF2PasswordEncryptor implements PasswordEncryptor {

    private static final String  PWD_FMT   = "%s:%s:%d";
    private static final Pattern PWD_REGEX = Pattern.compile("(?<pwdHash>\\w+):(?<saltHash>\\w+):(?<iterations>[0-9]+)");

    private static final String       SECRET_KEY_FACTORY_NAME = "PBKDF2WithHmacSHA512";
    private static final SecureRandom SECURE_RANDOM           = new SecureRandom();
    /**
     * Minimum number of iterations required is 1_000(rfc2898),
     * pick greater as potentially safer in the case of brute-force attacks .
     */
    private static final int          ITERATIONS_COUNT        = 10_000;
    /** 64bit salt length based on the rfc2898 spec . */
    private static final int          SALT_LENGTH             = 64 / 8;

    @Override
    public String encrypt(String password) {
        requireNonNull(password, "Required non-null password");
        final byte[] salt = new byte[SALT_LENGTH];
        SECURE_RANDOM.nextBytes(salt);
        final HashCode hash = computeHash(password.toCharArray(), salt, ITERATIONS_COUNT);
        final HashCode saltHash = HashCode.fromBytes(salt);
        return format(PWD_FMT, hash, saltHash, ITERATIONS_COUNT);
    }

    @Override
    public boolean test(String password, String encryptedPassword) {
        requireNonNull(password, "Required non-null password");
        requireNonNull(password, "Required non-null encrypted password");
        final Matcher matcher = PWD_REGEX.matcher(encryptedPassword);
        if (!matcher.matches()) {
            return false;
        }
        // retrieve salt, password hash and iterations count from hash
        final Integer iterations = tryParse(matcher.group("iterations"));
        final String salt = matcher.group("saltHash");
        final String pwdHash = matcher.group("pwdHash");
        // compute password's hash and test whether it matches to given hash
        final HashCode hash = computeHash(password.toCharArray(), HashCode.fromString(salt).asBytes(), iterations);
        return hash.toString().equals(pwdHash);
    }

    private HashCode computeHash(char[] password, byte[] salt, int iterations) {
        try {
            final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(SECRET_KEY_FACTORY_NAME);
            final KeySpec keySpec = new PBEKeySpec(password, salt, iterations, 512);
            return HashCode.fromBytes(keyFactory.generateSecret(keySpec).getEncoded());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException x) {
            throw new RuntimeException(x.getMessage(), x);
        }
    }
}
