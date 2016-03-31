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
 * JCE-based encrypt service which uses AES 128 with PBKDF2WithHmacSHA256.
 */
public class AES128WithSHA1EncryptTextService extends AESWithSHA1EncryptTextService {

    public static final int SCHEME_VERSION = 1;

    private static final byte[] SALT = {
                                 (byte)0x32, (byte)0x11, (byte)0x9a, (byte)0x70,
                                 (byte)0x06, (byte)0xba, (byte)0x88, (byte)0xd5,
                                };

    public AES128WithSHA1EncryptTextService() {
        super(SALT, 128, SCHEME_VERSION);
    }

}
