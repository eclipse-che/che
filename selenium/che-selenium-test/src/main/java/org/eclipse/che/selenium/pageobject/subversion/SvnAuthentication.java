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
package org.eclipse.che.selenium.pageobject.subversion;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Andrey Chizhikov */
@Singleton
public class SvnAuthentication {
  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public SvnAuthentication(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String SVN_AUTHENTICATION_TITLE = "//div[text()='SVN Authentication']";
    String USER_NAME_INPUT = "//div[text()='User Name:']//ancestor::tr//input";
    String PASSWORD_INPUT = "//div[text()='Password:']//ancestor::tr//input";
    String AUTHENTICATE_BTN = "svn-authentication-username";
  }

  @FindBy(id = Locators.AUTHENTICATE_BTN)
  WebElement authenticateBtn;

  /**
   * Enter name and password to 'SVN Authentication' form
   *
   * @param userName svn user name
   * @param password svn user password
   */
  public void svnLogin(String userName, String password) {
    if (svnAuthenticationWindowIsPresent()) {
      new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
          .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Locators.USER_NAME_INPUT)))
          .sendKeys(userName);
      new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
          .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Locators.PASSWORD_INPUT)))
          .sendKeys(password);
      new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
          .until(ExpectedConditions.elementToBeClickable(authenticateBtn))
          .click();
    }
  }

  private boolean svnAuthenticationWindowIsPresent() {
    WaitUtils.sleepQuietly(10);
    List<WebElement> svnAuthWindow =
        seleniumWebDriver.findElements(By.xpath(Locators.SVN_AUTHENTICATION_TITLE));
    return svnAuthWindow.size() == 1;
  }
}
