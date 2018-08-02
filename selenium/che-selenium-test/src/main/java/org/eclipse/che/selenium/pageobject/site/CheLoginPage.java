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
package org.eclipse.che.selenium.pageobject.site;

import static java.util.Arrays.asList;
import static org.eclipse.che.selenium.pageobject.site.CheLoginPage.Locators.LOGIN_BUTTON_NAME;
import static org.eclipse.che.selenium.pageobject.site.CheLoginPage.Locators.OPEN_SHIFT_OAUTH_LINK_ID;
import static org.eclipse.che.selenium.pageobject.site.CheLoginPage.Locators.PASSWORD_INPUT_NAME;
import static org.eclipse.che.selenium.pageobject.site.CheLoginPage.Locators.USERNAME_INPUT_NAME;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/** @author Dmytro Nochevnov */
@Singleton
public class CheLoginPage implements LoginPage {
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;

  private static final String FIRST_NAME = "first name";
  private static final String LAST_NAME = "last name";

  protected interface Locators {
    String USERNAME_INPUT_NAME = "username";
    String PASSWORD_INPUT_NAME = "password";
    String LOGIN_BUTTON_NAME = "login";
    String OPEN_SHIFT_OAUTH_LINK_ID = "zocial-openshift-v3";

    String ERROR_ALERT_XPATH = "//div[@class='alert alert-error']/span[@class='kc-feedback-text']";
    String INFO_ALERT_XPATH = "//div[@class='alert alert-info']/span[@class='kc-feedback-text']";
  }

  @FindBy(name = USERNAME_INPUT_NAME)
  private WebElement usernameInput;

  @FindBy(name = PASSWORD_INPUT_NAME)
  private WebElement passwordInput;

  @FindBy(name = LOGIN_BUTTON_NAME)
  private WebElement loginButton;

  @FindBy(id = OPEN_SHIFT_OAUTH_LINK_ID)
  private WebElement openShiftOAuthLink;

  @FindBy(xpath = Locators.INFO_ALERT_XPATH)
  private WebElement infoAlert;

  @FindBy(xpath = Locators.ERROR_ALERT_XPATH)
  private WebElement errorAlert;

  @Inject
  public CheLoginPage(
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

  public void loginWithPredefinedUsername(String password) {
    waitOnOpen();

    seleniumWebDriverHelper.setValue(passwordInput, password);
    seleniumWebDriverHelper.waitAndClick(loginButton);

    waitOnClose();
  }

  public String getUsername() {
    return seleniumWebDriverHelper.waitVisibilityAndGetValue(usernameInput);
  }

  public String getInfoAlert() {
    return seleniumWebDriverHelper.waitVisibilityAndGetText(infoAlert);
  }

  public String getErrorAlert() {
    return seleniumWebDriverHelper.waitVisibilityAndGetText(errorAlert);
  }

  public void loginWithOpenShiftOAuth() {
    waitOnOpen();
    seleniumWebDriverHelper.waitAndClick(openShiftOAuthLink);
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
