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
package org.eclipse.che.selenium.pageobject.site;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.constant.TestTimeoutsConstants;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Dmytro Nochevnov */
@Singleton
public class CheLoginPage implements LoginPage {

  private final SeleniumWebDriver seleniumWebDriver;
  private final WebDriverWait webDriverWait;

  @FindBy(name = "username")
  private WebElement usernameInput;

  @FindBy(name = "password")
  private WebElement passwordInput;

  @FindBy(name = "login")
  private WebElement loginButton;

  @Inject
  public CheLoginPage(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);

    webDriverWait =
        new WebDriverWait(seleniumWebDriver, TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC);
  }

  public void login(String username, String password) {
    waitOnOpen();
    usernameInput.clear();
    usernameInput.sendKeys(username);
    passwordInput.clear();
    passwordInput.sendKeys(password);
    loginButton.click();
    waitOnClose();
  }

  public void waitOnOpen() {
    webDriverWait.until(ExpectedConditions.visibilityOf(loginButton));
  }

  public void waitOnClose() {
    webDriverWait.until(
        ExpectedConditions.invisibilityOfAllElements(
            ImmutableList.of(loginButton, passwordInput, usernameInput)));
  }

  public boolean isOpened() {
    try {
      waitOnOpen();
    } catch (TimeoutException e) {
      return false;
    }

    return true;
  }
}
