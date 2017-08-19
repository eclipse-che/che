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
public class SvnReleaseLock {

  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public SvnReleaseLock(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String RELEASE_LOCK_FORM = "//div[text()='Unlock files or directories']/ancestor::div[3]";
    String FORCE_UNLOCK_BUTTON = "ask-dialog-second";
    String UNLOCK_BUTTON = "ask-dialog-first";
  }

  @FindBy(xpath = Locators.RELEASE_LOCK_FORM)
  WebElement releaseLock;

  @FindBy(id = Locators.FORCE_UNLOCK_BUTTON)
  WebElement forceUnlockBtn;

  @FindBy(id = Locators.UNLOCK_BUTTON)
  WebElement unlockBtn;

  public void waitReleaseLockFormOpened() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(releaseLock));
  }

  public void clickForceUnlock() {
    forceUnlockBtn.click();
  }

  public void clickUnlock() {
    unlockBtn.click();
  }
}
