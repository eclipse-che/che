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
package org.eclipse.che.selenium.pageobject.dashboard.workspaces;

import static java.lang.String.format;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

@Singleton
public class WorkspacesVolumes {
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;

  @Inject
  public WorkspacesVolumes(
      SeleniumWebDriver seleniumWebDriver, SeleniumWebDriverHelper seleniumWebDriverHelper) {
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String ADD_VOLUME_BUTTON = "//che-button-primary[@che-button-title='Add Volume']";
    String VOLUME_ITEM_XPATH_PATTERN = "//div[@id='machine-volume-name-%s']";
    String VOLUME_NAME_XPATH_PATTERN = VOLUME_ITEM_XPATH_PATTERN + "//span[@variable-name]";
    String VOLUME_PATH_XPATH_PATTERN = VOLUME_ITEM_XPATH_PATTERN + "//span[@variable-value]";
    String EDIT_VOLUME_BUTTON_XPATH_PATTERN = VOLUME_ITEM_XPATH_PATTERN + "//div[@edit-variable]";
    String REMOVE_VOLUME_BUTTON_XPATH_PATTERN =
        VOLUME_ITEM_XPATH_PATTERN + "//div[@delete-variable]";
    String NEW_VOLUME_NAME_ID = "volume-name-input";
    String NEW_VOLUME_PATH_XPATH = "//textarea[@name='deskpath']";
  }

  @FindBy(id = Locators.NEW_VOLUME_NAME_ID)
  WebElement newVolumeNameElement;

  @FindBy(xpath = Locators.NEW_VOLUME_PATH_XPATH)
  WebElement newVolumePathElement;

  public void clickOnAddVolumeButton() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(Locators.ADD_VOLUME_BUTTON));
  }

  public Boolean checkVolumeExists(String name) {
    return seleniumWebDriverHelper
        .waitVisibilityAndGetText(By.xpath(format(Locators.VOLUME_NAME_XPATH_PATTERN, name)))
        .equals(name);
  }

  public Boolean checkVolumeExists(String name, String path) {
    return seleniumWebDriverHelper
        .waitVisibilityAndGetText(By.xpath(format(Locators.VOLUME_PATH_XPATH_PATTERN, name)))
        .equals(path);
  }

  public void waitVolumeNotExists(String name) {
    seleniumWebDriverHelper.waitInvisibility(
        By.xpath(format(Locators.VOLUME_NAME_XPATH_PATTERN, name)));
  }

  public void clickOnEditVolumeButton(String name) {
    seleniumWebDriverHelper.waitAndClick(
        By.xpath(format(Locators.EDIT_VOLUME_BUTTON_XPATH_PATTERN, name)));
  }

  public void clickOnRemoveVolumeButton(String name) {
    seleniumWebDriverHelper.waitAndClick(
        By.xpath(format(Locators.REMOVE_VOLUME_BUTTON_XPATH_PATTERN, name)));
  }

  public void enterVolumeName(String name) {
    seleniumWebDriverHelper.setValue(newVolumeNameElement, name);
  }

  public void enterVolumePath(String path) {
    seleniumWebDriverHelper.setValue(newVolumePathElement, path);
  }
}
