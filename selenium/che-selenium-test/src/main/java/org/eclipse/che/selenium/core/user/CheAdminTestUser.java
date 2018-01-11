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
package org.eclipse.che.selenium.core.user;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/** @author Anatolii Bazko */
@Singleton
public class CheAdminTestUser implements AdminTestUser {

  private final TestUser delegate;

  @Inject
  public CheAdminTestUser(
      TestUserFactory userFactory,
      @Named("che.admin.email") String email,
      @Named("che.admin.password") String password)
      throws Exception {
    this.delegate = userFactory.create(email, password);
  }

  @Override
  public String getEmail() {
    return delegate.getEmail();
  }

  @Override
  public String getPassword() {
    return delegate.getPassword();
  }

  @Override
  public String getAuthToken() {
    return delegate.getAuthToken();
  }

  @Override
  public String getName() {
    return delegate.getName();
  }

  @Override
  public String getId() {
    return delegate.getId();
  }

  @Override
  public void delete() {}
}
