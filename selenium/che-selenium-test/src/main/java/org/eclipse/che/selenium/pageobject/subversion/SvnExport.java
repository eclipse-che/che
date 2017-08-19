/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.pageobject.subversion;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * @author Aleksandr Shmaraev
 * @author Andrey Chizhikov
 */
@Singleton
public class SvnExport {

  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public SvnExport(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  interface Locators {
    String EXPORT_MAIN_FORM = "gwt-debug-svn-export-window";
    String REVISION_CHECKBOX = "//label[text()='Revision']";
    String REVISION_TEXT_BOX = "//input[@class='gwt-TextBox']";
    String EXPORT_BUTTON = "svn-export-export";
    String CANCEL_BUTTON = "svn-export-cancel";
  }

  @FindBy(id = Locators.EXPORT_MAIN_FORM)
  WebElement mainFormExport;

  @FindBy(xpath = Locators.REVISION_CHECKBOX)
  WebElement checkboxRevision;

  @FindBy(xpath = Locators.REVISION_TEXT_BOX)
  WebElement textBoxRevision;

  @FindBy(id = Locators.EXPORT_BUTTON)
  WebElement buttonExport;

  @FindBy(id = Locators.CANCEL_BUTTON)
  WebElement buttonCancel;

  public void waitMainFormOpened() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(mainFormExport));
  }

  public void waitMainFormClosed() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.id(Locators.EXPORT_MAIN_FORM)));
  }

  public void clickRevisionCheckbox() {
    checkboxRevision.click();
  }

  public void typeTextRevision(String text) {
    textBoxRevision.clear();
    textBoxRevision.sendKeys(text);
  }

  public void clickExportBtn() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.elementToBeClickable(buttonExport));
    buttonExport.click();
  }

  public void clickCancelBtn() {
    buttonCancel.click();
  }
}
