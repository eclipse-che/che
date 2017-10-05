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
public class LoginPage {

  private final SeleniumWebDriver seleniumWebDriver;

  private final WebDriverWait loadPageTimeout;

  @FindBy(name = "username")
  private WebElement usernameInput;

  @FindBy(name = "password")
  private WebElement passwordInput;

  @FindBy(name = "login")
  private WebElement loginButton;

  @Inject
  public LoginPage(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
    loadPageTimeout =
        new WebDriverWait(seleniumWebDriver, TestTimeoutsConstants.ATTACHING_ELEM_TO_DOM_SEC);
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
    loadPageTimeout.until(ExpectedConditions.visibilityOf(loginButton));
  }

  public void waitOnClose() {
    loadPageTimeout.until(
        ExpectedConditions.invisibilityOfAllElements(ImmutableList.of(loginButton)));
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
