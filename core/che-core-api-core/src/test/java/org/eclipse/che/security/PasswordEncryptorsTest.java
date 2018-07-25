/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.security;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** @author Yevhenii Voevodin */
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
    return new Object[][] {{new SHA512PasswordEncryptor()}, {new PBKDF2PasswordEncryptor()}};
  }
}
