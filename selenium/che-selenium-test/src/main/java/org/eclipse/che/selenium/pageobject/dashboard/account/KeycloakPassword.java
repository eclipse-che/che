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
package org.eclipse.che.selenium.pageobject.dashboard.account;

import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.By;

@Singleton
public class KeycloakPassword extends KeycloakAbstract {

  @Inject
  public KeycloakPassword(SeleniumWebDriver seleniumWebDriver) {
    super(seleniumWebDriver);
  }

  protected interface PasswordLocators {
    String PASSWORD_FIELD_ID = "password";
    String NEW_PASSWORD_FIELD_ID = "password-new";
    String NEW_PASSWORD_CONFIRMATION_ID = "password-confirm";
    String SAVE_BUTTON = "//button[text()='Save']";
  }

  public void setPasswordFieldValue(String value) {
    setFieldValue(By.id(PasswordLocators.PASSWORD_FIELD_ID), value);
  }

  public void setNewPasswordFieldValue(String value) {
    setFieldValue(By.id(PasswordLocators.NEW_PASSWORD_FIELD_ID), value);
  }

  public void setNewPasswordConfirmationFieldValue(String value) {
    setFieldValue(By.id(PasswordLocators.NEW_PASSWORD_CONFIRMATION_ID), value);
  }

  public void clickOnSavePasswordButton() {
    loadPageWait.until(visibilityOfElementLocated(By.xpath(PasswordLocators.SAVE_BUTTON))).click();
  }
}
