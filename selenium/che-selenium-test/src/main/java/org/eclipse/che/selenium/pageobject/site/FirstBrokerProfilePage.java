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
package org.eclipse.che.selenium.pageobject.site;

import static java.util.Arrays.asList;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/**
 * This class describes page where OpenShift user is registering in Che at.
 *
 * @author Dmytro Nochevnov
 */
@Singleton
public class FirstBrokerProfilePage {

  private static final String FIRST_NAME = "first name";
  private static final String LAST_NAME = "last name";

  private interface Locators {
    String USERNAME_INPUT_ID = "username";
    String EMAIL_INPUT_ID = "email";
    String FIRST_NAME_INPUT_ID = "firstName";
    String LAST_NAME_INPUT_ID = "lastName";
    String SUBMIT_BUTTON_XPATH = "//input[@value='Submit']";
    String ERROR_ALERT_XPATH = "//div[@class='alert alert-error']/span[@class='kc-feedback-text']";
    String ADD_TO_EXISTING_ACCOUNT_BUTTON_ID = "linkAccount";
  }

  private SeleniumWebDriverHelper seleniumWebDriverHelper;

  @FindBy(id = Locators.USERNAME_INPUT_ID)
  private WebElement usernameInput;

  @FindBy(id = Locators.EMAIL_INPUT_ID)
  private WebElement emailInput;

  @FindBy(id = Locators.FIRST_NAME_INPUT_ID)
  private WebElement firstNameInput;

  @FindBy(id = Locators.LAST_NAME_INPUT_ID)
  private WebElement lastNameInput;

  @FindBy(xpath = Locators.SUBMIT_BUTTON_XPATH)
  private WebElement submitButton;

  @FindBy(xpath = Locators.ERROR_ALERT_XPATH)
  private WebElement errorAlert;

  @FindBy(id = Locators.ADD_TO_EXISTING_ACCOUNT_BUTTON_ID)
  private WebElement addToExistingAccountButton;

  @Inject
  public FirstBrokerProfilePage(
      SeleniumWebDriver seleniumWebDriver, SeleniumWebDriverHelper seleniumWebDriverHelper) {
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;

    PageFactory.initElements(seleniumWebDriver, this);
  }

  public void submit(TestUser user) {
    waitOnOpen();

    seleniumWebDriverHelper.setValue(usernameInput, user.getName());
    seleniumWebDriverHelper.setValue(emailInput, user.getEmail());
    seleniumWebDriverHelper.setValue(firstNameInput, FIRST_NAME);
    seleniumWebDriverHelper.setValue(lastNameInput, LAST_NAME);

    seleniumWebDriverHelper.waitAndClick(submitButton);

    waitOnClose();
  }

  public String getErrorAlert() {
    return seleniumWebDriverHelper.waitVisibilityAndGetText(errorAlert);
  }

  public void addToExistingAccount() {
    seleniumWebDriverHelper.waitAndClick(addToExistingAccountButton);
  }

  private void waitOnOpen() {
    seleniumWebDriverHelper.waitAllVisibility(
        asList(usernameInput, emailInput, lastNameInput, submitButton));
  }

  private void waitOnClose() {
    seleniumWebDriverHelper.waitAllInvisibility(
        asList(usernameInput, emailInput, lastNameInput, submitButton));
  }
}
