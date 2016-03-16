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

/**
 * JCE-based encrypt service which uses AES 256 with PBKDF2WithHmacSHA256.
 * AES 256 may not be available without JCE unlimited strength encryption policy.
 */
public class AES256WithSHA1EncryptTextService extends AESWithSHA1EncryptTextService {

    public static final int SCHEME_VERSION = 2;

    private static final byte[] SALT = {
                                 (byte)0xa1, (byte)0x30, (byte)0x95, (byte)0x0d,
                                 (byte)0x4e, (byte)0xbb, (byte)0x33, (byte)0x10,
                                };

    public AES256WithSHA1EncryptTextService() {
        super(SALT, 256, SCHEME_VERSION);
    }
}
