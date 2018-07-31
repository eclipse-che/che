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
package org.eclipse.che.selenium.pageobject;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Andrienko Alexander */
@Singleton
public class GoogleLogin {

  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public GoogleLogin(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  public interface Locators {
    String EMAIL_ID = "Email";
    String PASSWORD_ID = "Passwd";
    String SIGN_IN_BUTTON = "signIn";
    String LOGIN_FORM = "gaia_loginform";
    String APPROVE_BUTTON = "submit_approve_access";
  }

  @FindBy(id = Locators.EMAIL_ID)
  WebElement emailInput;

  @FindBy(id = Locators.PASSWORD_ID)
  WebElement passwordInput;

  @FindBy(id = Locators.SIGN_IN_BUTTON)
  WebElement signInBtn;

  public void waitOpenForm() {
    new WebDriverWait(seleniumWebDriver, 10)
        .until(ExpectedConditions.visibilityOfElementLocated(By.id(Locators.LOGIN_FORM)));
  }

  public void waitCloseForm() {
    new WebDriverWait(seleniumWebDriver, 10)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.id(Locators.LOGIN_FORM)));
  }

  public void typeLogin(String login) {
    new WebDriverWait(seleniumWebDriver, 10)
        .until(ExpectedConditions.visibilityOf(emailInput))
        .sendKeys(login);
    emailInput.sendKeys(Keys.ENTER.toString());
  }

  /** type into password field on Google authorization page user - defined password */
  public void typePasswordDefinedByUser(String password) {
    new WebDriverWait(seleniumWebDriver, 10)
        .until(ExpectedConditions.visibilityOf(passwordInput))
        .sendKeys(password);
    passwordInput.sendKeys(Keys.ENTER.toString());
  }

  public void clickApproveButton() {
    (new WebDriverWait(seleniumWebDriver, 10))
        .until(ExpectedConditions.presenceOfElementLocated(By.id(Locators.APPROVE_BUTTON)));
    WebElement approveBtn =
        (new WebDriverWait(seleniumWebDriver, 10))
            .until(ExpectedConditions.elementToBeClickable(By.id(Locators.APPROVE_BUTTON)));
    approveBtn.click();
    WaitUtils.sleepQuietly(1);
  }
}
