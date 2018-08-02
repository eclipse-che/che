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
package org.eclipse.che.security.oauth;

/** @author Vladislav Zhukovskii */
public enum OAuthStatus {
  /** If OAuth window manually closed by user. */
  NOT_PERFORMED(1),

  /** If some problem according while user try to login. */
  FAILED(2),

  /** If user has successfully logged in. */
  LOGGED_IN(3),

  /** If user has successfully logged out. */
  LOGGED_OUT(4);

  private final int value;

  private OAuthStatus(int value) {
    this.value = value;
  }

  public static OAuthStatus fromValue(int value) {
    for (OAuthStatus v : OAuthStatus.values()) {
      if (v.value == value) {
        return v;
      }
    }
    throw new IllegalArgumentException("Invalid value '" + value + "' ");
  }
}
