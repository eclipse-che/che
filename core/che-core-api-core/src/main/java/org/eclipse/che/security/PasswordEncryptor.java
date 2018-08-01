/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.security;

/**
 * Encrypts password in implementation specific way.
 *
 * @author Yevhenii Voevodin
 */
public interface PasswordEncryptor {

  /**
   * Encrypts the given {@code password}.
   *
   * @param password the plain password to be encrypted
   * @return the encrypted password
   * @throws NullPointerException when the password is null
   * @throws RuntimeException when any error occurs during password encryption
   */
  String encrypt(String password);

  /**
   * Tests whether given {@code password} is {@code encryptedPassword}.
   *
   * @param encryptedPassword encrypted password
   * @param password the password to check
   * @return true if given {@code password} is {@code encryptedPassword}
   * @throws NullPointerException when either of arguments is null
   * @throws RuntimeException when any error occurs during test
   */
  boolean test(String password, String encryptedPassword);
}
