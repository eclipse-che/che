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
 * JCE-based encrypt service which uses AES with PBKDF2WithHmacSHA256.
 */
public abstract class AESWithSHA1EncryptTextService extends JCEEncryptTextService {

    private static final String SECRETKEY_FACTORY_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String CIPHER = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";

    public AESWithSHA1EncryptTextService(final byte[] salt, final int keySize, final int schemeVersion) {
        super(SECRETKEY_FACTORY_ALGORITHM, CIPHER, TRANSFORMATION, salt, keySize, schemeVersion);
    }

}
