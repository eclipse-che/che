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
package org.eclipse.che.selenium.pageobject.dashboard;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOADER_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.dashboard.DocumentationPage.Locators.PAGE_TITLE_CLASS_NAME;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.By;

@Singleton
public class DocumentationPage {
  private final SeleniumWebDriver seleniumWebDriver;
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;

  @Inject
  public DocumentationPage(
      SeleniumWebDriver seleniumWebDriver, SeleniumWebDriverHelper seleniumWebDriverHelper) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
  }

  public interface Locators {
    String PAGE_TITLE_CLASS_NAME = "projectTitle";
  }

  public String getTitle() {
    return seleniumWebDriverHelper.waitVisibilityAndGetText(
        By.className(PAGE_TITLE_CLASS_NAME), LOADER_TIMEOUT_SEC);
  }

  public void waitTitle(String title) {
    seleniumWebDriverHelper.waitTextContains(
        By.className(PAGE_TITLE_CLASS_NAME), title, LOADER_TIMEOUT_SEC);
  }
}
