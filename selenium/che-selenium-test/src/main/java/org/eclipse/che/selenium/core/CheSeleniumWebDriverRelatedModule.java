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
package org.eclipse.che.selenium.core;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import org.eclipse.che.selenium.core.entrance.CookieEntrance;
import org.eclipse.che.selenium.core.entrance.Entrance;
import org.eclipse.che.selenium.core.entrance.LoginPageEntrance;
import org.eclipse.che.selenium.pageobject.site.CheLoginPage;
import org.eclipse.che.selenium.pageobject.site.LoginPage;

/**
 * Module which is dedicated to deal with dependencies which are injecting SeleniumWebDriver.class
 * itself.
 *
 * @author Dmytro Nochevnov
 */
public class CheSeleniumWebDriverRelatedModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(LoginPage.class).to(CheLoginPage.class);
  }

  @Provides
  public Entrance getEntrance(
      @Named("che.multiuser") boolean isMultiuser,
      LoginPage loginPage,
      SeleniumWebDriver seleniumWebDriver) {
    if (isMultiuser) {
      return new LoginPageEntrance(loginPage);
    } else {
      return new CookieEntrance(seleniumWebDriver);
    }
  }
}
