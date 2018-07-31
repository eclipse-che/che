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
    String PAGE_TITLE_CLASS_NAME = "post-title-main";
  }

  public String getTitle() {
    return seleniumWebDriverHelper.waitVisibilityAndGetText(
        By.className(PAGE_TITLE_CLASS_NAME), LOADER_TIMEOUT_SEC);
  }
}
