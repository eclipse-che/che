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
package org.eclipse.che.selenium.pageobject.theia;

import static org.eclipse.che.selenium.pageobject.theia.TheiaNewFileDialog.Locators.CLOSE_ICON_XPATH;
import static org.eclipse.che.selenium.pageobject.theia.TheiaNewFileDialog.Locators.FILE_NAME_FIELD_XPATH;
import static org.eclipse.che.selenium.pageobject.theia.TheiaNewFileDialog.Locators.OK_BUTTON_XPATH;
import static org.eclipse.che.selenium.pageobject.theia.TheiaNewFileDialog.Locators.TITLE_XPATH;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.By;

@Singleton
public class TheiaNewFileDialog {
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;

  @Inject
  private TheiaNewFileDialog(SeleniumWebDriverHelper seleniumWebDriverHelper) {
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
  }

  public interface Locators {
    String TITLE_XPATH = "//div[@class='dialogBlock']//div[text()='New File']";
    String FILE_NAME_FIELD_XPATH = "//div[@class='dialogBlock']//div[@class='dialogContent']/input";
    String OK_BUTTON_XPATH =
        "//div[@class='dialogBlock']//div[@class='dialogControl']//button[text()='OK']";
    String CLOSE_ICON_XPATH =
        "//div[@class='dialogBlock']//div[@class='dialogTitle']//i[contains(@class, 'closeButton')]";
  }

  public void waitDialog() {
    waitTitle();
    waitNewFileNameField();
    waitOkButton();
  }

  public void waitDialogClosed() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(TITLE_XPATH));
  }

  public void waitTitle() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(TITLE_XPATH));
  }

  public void waitNewFileNameField() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(FILE_NAME_FIELD_XPATH));
  }

  public void enterNewFileName(String fileName) {
    seleniumWebDriverHelper.setValue(By.xpath(FILE_NAME_FIELD_XPATH), fileName);
  }

  public void waitNewFileNameValue(String expectedFileName) {
    seleniumWebDriverHelper.waitValueEqualsTo(By.xpath(FILE_NAME_FIELD_XPATH), expectedFileName);
  }

  public void waitOkButton() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(OK_BUTTON_XPATH));
  }

  public void clickOkButton() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(OK_BUTTON_XPATH));
  }

  public void clickCloseIcon() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(CLOSE_ICON_XPATH));
  }
}
