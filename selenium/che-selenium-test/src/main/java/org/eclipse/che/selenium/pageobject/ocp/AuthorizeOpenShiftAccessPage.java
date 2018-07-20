/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.pageobject.ocp;

import static org.eclipse.che.selenium.pageobject.ocp.AuthorizeOpenShiftAccessPage.Locators.ALLOW_PERMISSIONS_BUTTON_NAME;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class AuthorizeOpenShiftAccessPage {
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;

  protected interface Locators {
    String ALLOW_PERMISSIONS_BUTTON_NAME = "approve";
  }

  @FindBy(name = ALLOW_PERMISSIONS_BUTTON_NAME)
  private WebElement allowPermissionsButton;

  @Inject
  public AuthorizeOpenShiftAccessPage(
      SeleniumWebDriver seleniumWebDriver, SeleniumWebDriverHelper seleniumWebDriverHelper) {
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;

    PageFactory.initElements(seleniumWebDriver, this);
  }

  public void allowPermissions() {
    seleniumWebDriverHelper.waitAndClick(allowPermissionsButton);
    waitOnClose();
  }

  public void waitOnOpen() {
    seleniumWebDriverHelper.waitVisibility(allowPermissionsButton);
  }

  private void waitOnClose() {
    seleniumWebDriverHelper.waitInvisibility(allowPermissionsButton);
  }
}
