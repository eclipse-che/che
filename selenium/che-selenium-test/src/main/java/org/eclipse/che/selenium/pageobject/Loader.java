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

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.UPDATING_PROJECT_TIMEOUT_SEC;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Anna Shumilova */
@Singleton
public class Loader {

  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public Loader(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
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
    try {
      new WebDriverWait(seleniumWebDriver, UPDATING_PROJECT_TIMEOUT_SEC)
          .until(ExpectedConditions.invisibilityOfElementLocated(By.id(Locators.LOADER_ID)));
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/8300", ex);
    }
  }
}
