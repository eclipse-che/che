/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.pageobject.ocp;

import static org.eclipse.che.selenium.pageobject.ocp.AuthorizeOpenShiftAccessPage.Locators.ALLOW_PERMISSIONS_BUTTON_NAME;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

@Singleton
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
