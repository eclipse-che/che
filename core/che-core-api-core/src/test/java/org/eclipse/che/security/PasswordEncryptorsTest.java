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

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Yevhenii Voevodin
 */
public class PasswordEncryptorsTest {

    @Test(dataProvider = "encryptorsProvider")
    public void testEncryption(PasswordEncryptor encryptor) throws Exception {
        final String password = "password";

        final String hash = encryptor.encrypt(password);
        assertNotNull(hash, "encrypted password's hash");

        assertTrue(encryptor.test(password, hash), "password test");
    }

    @DataProvider(name = "encryptorsProvider")
    public Object[][] encryptorsProvider() {
        return new Object[][] {
                {new SHA512PasswordEncryptor()},
                {new PBKDF2PasswordEncryptor()}
        };
    }
}
