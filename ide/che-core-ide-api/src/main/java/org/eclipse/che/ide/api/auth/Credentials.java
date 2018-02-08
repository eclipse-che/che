/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.auth;

/**
 * Credentials object for subversion operations.
 *
 * @author Igor Vinokur
 */
public class Credentials {
  private String username;
  private String password;

  public Credentials(String username, String password) {
    this.username = username;
    this.password = password;
  }

  /** Returns user name for authentication. */
  public String getUsername() {
    return username;
  }

  /** Set user name for authentication. */
  public void setUsername(String username) {
    this.username = username;
  }

  /** Returns password for authentication. */
  public String getPassword() {
    return password;
  }

  /** Set password for authentication. */
  public void setPassword(String password) {
    this.password = password;
  }
}
