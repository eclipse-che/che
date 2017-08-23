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

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * @author Anton Korneta
 * @author Andrey Chizhikov
 */
@Singleton
public class SvnGetLock {

  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public SvnGetLock(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String GET_LOCK_FORM = "//div[text()='Lock files or directories']/ancestor::div[3]";
    String LOCK_BUTTON = "ask-dialog-first";
  }

  @FindBy(xpath = Locators.GET_LOCK_FORM)
  WebElement getLockForm;

  @FindBy(id = Locators.LOCK_BUTTON)
  WebElement lockBtn;

  public void waitGetLockFormOpened() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(getLockForm));
  }

  public void clickLock() {
    lockBtn.click();
  }
}
