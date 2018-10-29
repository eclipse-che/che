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
package org.eclipse.che.selenium.pageobject;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.UPDATING_PROJECT_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/** @author Anna Shumilova */
@Singleton
public class Loader {

  private final SeleniumWebDriver seleniumWebDriver;
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;

  @Inject
  public Loader(
      SeleniumWebDriver seleniumWebDriver, SeleniumWebDriverHelper seleniumWebDriverHelper) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;

    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String PREFIX_ID = "gwt-debug-";
    String LOADER_ID = PREFIX_ID + "loader-panel";
    String MESSAGE_CONTAINER_ID = PREFIX_ID + "loader-message";
  }

  @FindBy(id = Locators.LOADER_ID)
  private WebElement loader;

  @FindBy(id = Locators.MESSAGE_CONTAINER_ID)
  private WebElement messContainer;

  /** Waits for Loader to hide. */
  public void waitOnClosed() {
    // in this pace pause, because loader can appear not at once
    WaitUtils.sleepQuietly(1);

    seleniumWebDriverHelper.waitInvisibility(loader, UPDATING_PROJECT_TIMEOUT_SEC);
  }
}
