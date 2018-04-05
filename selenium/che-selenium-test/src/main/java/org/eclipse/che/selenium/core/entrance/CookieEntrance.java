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
package org.eclipse.che.selenium.core.entrance;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
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
  public void login(DefaultTestUser user) {
    Cookie accessKey = new Cookie("session-access-key", user.obtainAuthToken());
    seleniumWebDriver.manage().addCookie(accessKey);
  }
}
