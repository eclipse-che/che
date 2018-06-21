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
package org.eclipse.che.selenium.pageobject.ocp;

import static java.util.Arrays.asList;
import static org.eclipse.che.selenium.pageobject.ocp.OpenShiftLoginPage.Locators.LOGIN_BUTTON_XPATH;
import static org.eclipse.che.selenium.pageobject.ocp.OpenShiftLoginPage.Locators.PASSWORD_FIELD_NAME;
import static org.eclipse.che.selenium.pageobject.ocp.OpenShiftLoginPage.Locators.USERNAME_FIELD_NAME;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.pageobject.site.LoginPage;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class OpenShiftLoginPage implements LoginPage {
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;

  protected interface Locators {
    String USERNAME_FIELD_NAME = "username";
    String PASSWORD_FIELD_NAME = "password";
    String LOGIN_BUTTON_XPATH = "//button[contains(text(),'Log In')]";
  }

  @FindBy(name = USERNAME_FIELD_NAME)
  private WebElement usernameInput;

  @FindBy(name = PASSWORD_FIELD_NAME)
  private WebElement passwordInput;

  @FindBy(xpath = LOGIN_BUTTON_XPATH)
  private WebElement loginButton;

  @Inject
  public OpenShiftLoginPage(
      SeleniumWebDriver seleniumWebDriver, SeleniumWebDriverHelper seleniumWebDriverHelper) {
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;

    PageFactory.initElements(seleniumWebDriver, this);
  }

  @Override
  public void login(String username, String password) {
    waitOnOpen();

    seleniumWebDriverHelper.setValue(usernameInput, username);
    seleniumWebDriverHelper.setValue(passwordInput, password);
    seleniumWebDriverHelper.waitAndClick(loginButton);

    waitOnClose();
  }

  @Override
  public boolean isOpened() {
    try {
      waitOnOpen();
    } catch (TimeoutException e) {
      return false;
    }

    return true;
  }

  private void waitOnOpen() {
    seleniumWebDriverHelper.waitAllVisibility(asList(usernameInput, passwordInput, loginButton));
  }

  private void waitOnClose() {
    seleniumWebDriverHelper.waitAllInvisibility(asList(usernameInput, passwordInput, loginButton));
  }
}
