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

import static org.eclipse.che.selenium.pageobject.theia.TheiaDeleteFileDialog.Locators.CANCEL_BUTTON_XPATH;
import static org.eclipse.che.selenium.pageobject.theia.TheiaDeleteFileDialog.Locators.DIALOG_TITLE_XPATH;
import static org.eclipse.che.selenium.pageobject.theia.TheiaDeleteFileDialog.Locators.OK_BUTTON_XPATH;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.By;

@Singleton
public class TheiaDeleteFileDialog {
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;

  @Inject
  private TheiaDeleteFileDialog(SeleniumWebDriverHelper seleniumWebDriverHelper) {
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
  }

  public interface Locators {
    String DIALOG_TITLE_XPATH = "//div[@class='dialogBlock']//div[text()='Delete File']";
    String OK_BUTTON_XPATH = "//div[@class='dialogBlock']//button[text()='OK']";
    String CANCEL_BUTTON_XPATH = "//div[@class='dialogBlock']//button[text()='Cancel']";
  }

  public void waitDialog() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(DIALOG_TITLE_XPATH));
  }

  public void waitDialogDesapearance() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(DIALOG_TITLE_XPATH));
  }

  public void clickOkButton() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(OK_BUTTON_XPATH));
  }

  public void clickCancelButton() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(CANCEL_BUTTON_XPATH));
  }
}
