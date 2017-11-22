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
package org.eclipse.che.selenium.core.entrance;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.pageobject.site.LoginPage;

/**
 * Enter the product through the Login Page.
 *
 * @author Dmytro Nochevnov
 */
@Singleton
public class LoginPageEntrance implements Entrance {

  private final LoginPage loginPage;

  @Inject
  public LoginPageEntrance(LoginPage loginPage) {
    this.loginPage = loginPage;
  }

  /**
   * Login to product.
   *
   * @param user
   */
  @Override
  public void login(TestUser user) {
    if (loginPage.isOpened()) {
      loginPage.login(user.getName(), user.getPassword());
    }
  }
}
