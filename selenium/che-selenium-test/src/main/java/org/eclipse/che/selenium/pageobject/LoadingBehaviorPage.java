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

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.EXPECTED_MESS_IN_CONSOLE_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * @author Musienko Maxim
 * @author Andrey Chizhikov
 */
@Singleton
public class LoadingBehaviorPage {

  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public LoadingBehaviorPage(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String STATE_IDE_FRAME_CSS = "ide-iframe.ng-hide";
  }

  @FindBy(css = LoadingBehaviorPage.Locators.STATE_IDE_FRAME_CSS)
  WebElement loadPage;

  /** wait closing of the Staring workspace page */
  public void waitWhileLoadPageIsClosed() {
    new WebDriverWait(seleniumWebDriver, EXPECTED_MESS_IN_CONSOLE_SEC)
        .until(
            ExpectedConditions.invisibilityOfElementLocated(
                By.cssSelector(Locators.STATE_IDE_FRAME_CSS)));
  }
}
