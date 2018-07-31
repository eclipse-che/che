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
package org.eclipse.che.selenium.core.entrance;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.user.TestUser;
import org.openqa.selenium.Cookie;

/**
 * Enter the product by adding "session-access-key=[auth_token]" to the cookie of web driver.
 *
 * @author Dmytro Nochevnov
 */
@Singleton
public class CookieEntrance implements Entrance {

  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public CookieEntrance(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
  }

  /**
   * Login to product by using cookies.
   *
   * @param user
   */
  @Override
  public void login(TestUser user) {
    Cookie accessKey = new Cookie("session-access-key", user.obtainAuthToken());
    seleniumWebDriver.manage().addCookie(accessKey);
  }
}
