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
package org.eclipse.che.selenium.pageobject.ocp;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.eclipse.che.selenium.pageobject.ocp.OpenShiftLoginPage.Locators.ADD_TO_EXISTING_ACCOUNT_BUTTON_ID;
import static org.eclipse.che.selenium.pageobject.ocp.OpenShiftLoginPage.Locators.ALLOW_PERMISSIONS_BUTTON_NAME;
import static org.eclipse.che.selenium.pageobject.ocp.OpenShiftLoginPage.Locators.APPROVE_BUTTON_NAME;
import static org.eclipse.che.selenium.pageobject.ocp.OpenShiftLoginPage.Locators.EMAIL_NAME;
import static org.eclipse.che.selenium.pageobject.ocp.OpenShiftLoginPage.Locators.FIRST_NAME_NAME;
import static org.eclipse.che.selenium.pageobject.ocp.OpenShiftLoginPage.Locators.IDENTITY_PROVIDER_LINK_XPATH;
import static org.eclipse.che.selenium.pageobject.ocp.OpenShiftLoginPage.Locators.LAST_NAME_NAME;
import static org.eclipse.che.selenium.pageobject.ocp.OpenShiftLoginPage.Locators.LOGIN_BUTTON_XPATH;
import static org.eclipse.che.selenium.pageobject.ocp.OpenShiftLoginPage.Locators.PASSWORD_INPUT_NAME;
import static org.eclipse.che.selenium.pageobject.ocp.OpenShiftLoginPage.Locators.SUBMIT_BUTTON_XPATH;
import static org.eclipse.che.selenium.pageobject.ocp.OpenShiftLoginPage.Locators.UPDATE_LOGIN_PAGE_ID;
import static org.eclipse.che.selenium.pageobject.ocp.OpenShiftLoginPage.Locators.USERNAME_INPUT_NAME;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.pageobject.site.CheLoginPage;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

@Singleton
public class OpenShiftLoginPage {

  private final SeleniumWebDriverHelper seleniumWebDriverHelper;
  private final SeleniumWebDriver seleniumWebDriver;
  private final CheLoginPage cheLoginPage;

  @Inject
  public OpenShiftLoginPage(
      SeleniumWebDriver seleniumWebDriver,
      SeleniumWebDriverHelper seleniumWebDriverHelper,
      CheLoginPage cheLoginPage) {
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    this.seleniumWebDriver = seleniumWebDriver;
    this.cheLoginPage = cheLoginPage;

    PageFactory.initElements(seleniumWebDriver, this);
  }

  @Inject(optional = true)
  @Named("env.openshift.regular.username")
  private String openShiftUsername;

  @Inject(optional = true)
  @Named("env.openshift.regular.password")
  private String openShiftPassword;

  @Inject(optional = true)
  @Named("env.openshift.regular.email")
  private String openShiftEmail;

  private static final String IDENTITY_PROVIDER_NAME = "htpasswd";

  protected interface Locators {
    String USERNAME_INPUT_NAME = "username";
    String PASSWORD_INPUT_NAME = "password";
    String LOGIN_BUTTON_XPATH = "//button[contains(text(),'Log In')]";
    String SUBMIT_BUTTON_XPATH = "//input[@value='Submit']";
    String FIRST_NAME_NAME = "firstName";
    String LAST_NAME_NAME = "lastName";
    String EMAIL_NAME = "email";
    String APPROVE_BUTTON_NAME = "approve";
    String IDENTITY_PROVIDER_LINK_XPATH = "//a[@title='Log in with %s']";
    String ADD_TO_EXISTING_ACCOUNT_BUTTON_ID = "linkAccount";
    String ALLOW_PERMISSIONS_BUTTON_NAME = "approve";
    String UPDATE_LOGIN_PAGE_ID = "kc-page-title";
  }

  @FindBy(name = ALLOW_PERMISSIONS_BUTTON_NAME)
  private WebElement allowPermissionsButton;

  @FindBy(name = USERNAME_INPUT_NAME)
  private WebElement usernameInput;

  @FindBy(name = PASSWORD_INPUT_NAME)
  private WebElement passwordInput;

  @FindBy(xpath = LOGIN_BUTTON_XPATH)
  private WebElement loginButton;

  @FindBy(xpath = SUBMIT_BUTTON_XPATH)
  private WebElement submitButton;

  @FindBy(name = FIRST_NAME_NAME)
  private WebElement firstUsername;

  @FindBy(name = LAST_NAME_NAME)
  private WebElement lastUsername;

  @FindBy(name = EMAIL_NAME)
  private WebElement emailName;

  @FindBy(name = APPROVE_BUTTON_NAME)
  private WebElement approveButton;

  @FindBy(id = ADD_TO_EXISTING_ACCOUNT_BUTTON_ID)
  private WebElement addToExistingAccountButton;

  public void login(String username, String password) {
    waitOnOpen();

    seleniumWebDriverHelper.setValue(usernameInput, username);
    seleniumWebDriverHelper.setValue(passwordInput, password);
    seleniumWebDriverHelper.waitAndClick(loginButton);

    waitOnClose();
  }

  public void waitOnOpen() {
    seleniumWebDriverHelper.waitAllVisibility(asList(usernameInput, passwordInput, loginButton));
  }

  private void waitOnClose() {
    seleniumWebDriverHelper.waitAllInvisibility(asList(loginButton));
  }

  public void submit(String userName, String email) {
    seleniumWebDriverHelper.setValue(firstUsername, userName);
    seleniumWebDriverHelper.setValue(lastUsername, userName);
    seleniumWebDriverHelper.setValue(emailName, email);
    seleniumWebDriverHelper.setValue(usernameInput, "admin");

    seleniumWebDriverHelper.waitAndClick(submitButton);
  }

  public Boolean isApproveButtonVisible() {
    return seleniumWebDriverHelper.isVisible(approveButton);
  }

  public Boolean isIdentityProviderLinkVisible(String identityProviderName) {
    try {
      seleniumWebDriverHelper.waitVisibility(
          By.xpath(format(IDENTITY_PROVIDER_LINK_XPATH, identityProviderName)), 2);
    } catch (TimeoutException e) {
      return false;
    }

    return true;
  }

  public Boolean isOpenshiftUpdateLoginPageVisible() {
    try {
      seleniumWebDriverHelper.waitVisibility(By.id(UPDATE_LOGIN_PAGE_ID));
    } catch (TimeoutException e) {
      return false;
    }

    return true;
  }

  public void clickOnIdentityProviderLink(String identityProviderName) {
    seleniumWebDriverHelper.waitAndClick(
        By.xpath(format(IDENTITY_PROVIDER_LINK_XPATH, identityProviderName)));
  }

  public void addToExistingAccount() {
    seleniumWebDriverHelper.waitAndClick(addToExistingAccountButton);
  }

  public void login() {
    if (isOpened()) {
      if (isIdentityProviderLinkVisible(IDENTITY_PROVIDER_NAME)) {
        clickOnIdentityProviderLink(IDENTITY_PROVIDER_NAME);
      }

      login(openShiftUsername, openShiftPassword);

      if (isApproveButtonVisible()) {
        allowPermissions();
      }

      if (isOpenshiftUpdateLoginPageVisible()) {
        submit(openShiftUsername, openShiftEmail);

        addToExistingAccount();
        cheLoginPage.loginWithPredefinedUsername("admin");
      }
    }
  }

  public boolean isOpened() {
    try {
      waitOnOpen();
    } catch (TimeoutException e) {
      return false;
    }

    return true;
  }

  public void allowPermissions() {
    seleniumWebDriverHelper.waitAndClick(allowPermissionsButton);
    seleniumWebDriverHelper.waitInvisibility(allowPermissionsButton);
  }
}
