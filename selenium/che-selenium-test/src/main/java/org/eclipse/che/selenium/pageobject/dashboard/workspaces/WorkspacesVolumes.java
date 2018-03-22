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

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.pageobject.SeleniumWebDriverHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class WorkspacesVolumes {
  private final SeleniumWebDriver seleniumWebDriver;
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;

  @Inject
  public WorkspacesVolumes(
      SeleniumWebDriver seleniumWebDriver, SeleniumWebDriverHelper seleniumWebDriverHelper) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String ADD_VOLUME_BUTTON = "//che-button-primary[@che-button-title='Add Volume']";
    String VOLUME_ITEM_XPATH_PATTERN = "//div[@id='machine-volume-name-%s']";
    String VOLUME_NAME_XPATH = VOLUME_ITEM_XPATH_PATTERN + "//span[@variable-name]";
    String VOLUME_PATH_XPATH = VOLUME_ITEM_XPATH_PATTERN + "//span[@variable-value]";
    String EDIT_VOLUME_BUTTON_XPATH = VOLUME_ITEM_XPATH_PATTERN + "//div[@edit-variable]";
    String REMOVE_VOLUME_BUTTON_XPATH = VOLUME_ITEM_XPATH_PATTERN + "//div[@delete-variable]";
    String NEW_VOLUME_NAME_ID = "volume-name-input";
    String NEW_VOLUME_PATH_XPATH = "//textarea[@name='deskpath']";
  }

  @FindBy(id = Locators.NEW_VOLUME_NAME_ID)
  WebElement newVolumeName;

  @FindBy(xpath = Locators.NEW_VOLUME_PATH_XPATH)
  WebElement newVolumePath;

  public void clickOnAddVolumeButton() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(Locators.ADD_VOLUME_BUTTON));
  }

  public void checkVolumeExists(String name) {
    seleniumWebDriverHelper.waitVisibility(By.xpath(format(Locators.VOLUME_NAME_XPATH, name)));
  }

  public void checkVolumeExists(String name, String path) {
    seleniumWebDriverHelper.waitVisibility(By.xpath(format(Locators.VOLUME_NAME_XPATH, name)));
    seleniumWebDriverHelper.waitVisibility(By.xpath(format(Locators.VOLUME_PATH_XPATH, name)));
  }

  public void waitVolumeNotExists(String name) {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(format(Locators.VOLUME_NAME_XPATH, name)));
  }

  public void clickOnEditVolumeButton(String name) {
    seleniumWebDriverHelper.waitAndClick(By.xpath(format(Locators.EDIT_VOLUME_BUTTON_XPATH, name)));
  }

  public void clickOnRemoveVolumeButton(String name) {
    seleniumWebDriverHelper.waitAndClick(
        By.xpath(format(Locators.REMOVE_VOLUME_BUTTON_XPATH, name)));
  }

  public void enterVolumeName(String name) {
    seleniumWebDriverHelper.waitVisibility(newVolumeName).clear();
    seleniumWebDriverHelper.waitVisibility(newVolumeName).sendKeys(name);
  }

  public void enterVolumePath(String path) {
    seleniumWebDriverHelper.waitVisibility(newVolumePath).clear();
    seleniumWebDriverHelper.waitVisibility(newVolumePath).sendKeys(path);
  }
}
