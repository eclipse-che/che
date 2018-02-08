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
package org.eclipse.che.selenium.pageobject.dashboard.workspaces;

import static java.lang.String.format;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WorkspaceInstallers {
  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public WorkspaceInstallers(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String INSTALLER_NAME = "//span[@agent-name='%s']";
    String INSTALLER_DESCRIPTION = "//div[@agent-item-name='%s']//span[@agent-description]";
    String INSTALLER_STATE = "//md-switch[@agent-switch='%s']";
    String INSTALLER_VERSION_STATE =
        "//div[@agent-item-name='%s' and @agent-item-version='%s']//md-switch[@agent-switch='%s']";
  }

  public void checkInstallerExists(String installerName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(format(Locators.INSTALLER_NAME, installerName))));
  }

  public void switchInstallerState(String installerName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(format(Locators.INSTALLER_STATE, installerName))))
        .click();
  }

  public void switchInstallerState(String installerName, String installerVersion) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(
                    format(
                        Locators.INSTALLER_VERSION_STATE,
                        installerName,
                        installerVersion,
                        installerName))))
        .click();
  }

  public Boolean isInstallerStateTurnedOn(String installerName) {
    String state =
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
            .until(
                ExpectedConditions.visibilityOfElementLocated(
                    By.xpath(format(Locators.INSTALLER_STATE, installerName))))
            .getAttribute("aria-checked");

    return Boolean.parseBoolean(state);
  }

  public Boolean isInstallerStateNotChangeable(String installerName) {
    String state =
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
            .until(
                ExpectedConditions.visibilityOfElementLocated(
                    By.xpath(format(Locators.INSTALLER_STATE, installerName))))
            .getAttribute("aria-disabled");

    return Boolean.parseBoolean(state);
  }

  public String getInstallerDescription(String installerName) {
    return new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(format(Locators.INSTALLER_DESCRIPTION, installerName))))
        .getText();
  }
}
