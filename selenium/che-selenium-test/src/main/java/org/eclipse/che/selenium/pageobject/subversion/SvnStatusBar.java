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
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * @author Aleksandr Shmaraev
 * @author Andrey Chizhikov
 */
@Singleton
public class SvnStatusBar {

  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public SvnStatusBar(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String STATUS_BAR_TAB = "gwt-debug-tabButton-Subversion";
    String STATUS_BAR_INFO_PANEL = "gwt-debug-consolePart";
  }

  @FindBy(id = Locators.STATUS_BAR_TAB)
  WebElement statusBarTab;

  @FindBy(id = Locators.STATUS_BAR_INFO_PANEL)
  WebElement statusBarInfoPanel;

  public void waitSvnStatusBarInfoPanelOpened() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(statusBarInfoPanel));
  }

  public void waitMessageIntoSvnInfoPanel(final String message) {
    waitSvnStatusBarInfoPanelOpened();
    new WebDriverWait(seleniumWebDriver, 20)
        .until(
            (ExpectedCondition<Boolean>)
                webDriver -> statusBarInfoPanel.getText().contains(message));
  }

  public String getAllMessageFromStatusBar() {
    return statusBarInfoPanel.getText();
  }
}
