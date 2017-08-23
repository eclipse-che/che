/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.core.user;

import com.google.inject.Singleton;

/** @author Anatolii Bazko */
@Singleton
public class CheAdminTestUser implements AdminTestUser {

  @Override
  public String getEmail() {
    return "admin@email";
  }

  @Override
  public String getPassword() {
    return "password";
  }

  @Override
  public String getAuthToken() {
    return "auth";
  }

  @Override
  public String getName() {
    return "admin";
  }

  @Override
  public String getId() {
    return "id";
  }

  @Override
  public void delete() {}
}
