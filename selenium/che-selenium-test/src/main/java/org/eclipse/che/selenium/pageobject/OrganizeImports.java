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

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Andrey Chizhikov */
@Singleton
public class OrganizeImports {
  private interface Locators {
    String ORGANIZE_IMPORTS_DIALOG = "//table[@title='Organize Imports']";
    String FINISH_BUTTON = "imports-finish-button";
    String BACK_BUTTON = "imports-back-button";
    String IMPORT_FIELD = "//div[text()='%s']";
    String NEXT_BUTTON = "imports-next-button";
  }

  @FindBy(xpath = Locators.ORGANIZE_IMPORTS_DIALOG)
  WebElement organizeImportsDialog;

  @FindBy(id = Locators.FINISH_BUTTON)
  WebElement finishButton;

  @FindBy(id = Locators.BACK_BUTTON)
  WebElement backButton;

  @FindBy(id = Locators.NEXT_BUTTON)
  WebElement nextButton;

  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public OrganizeImports(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  /** wait dialog 'Organize Imports' is opened */
  public void waitOrganizeImportsDialog() {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(organizeImportsDialog));
  }

  /** click on 'Finish' button */
  public void clickOnFinishButton() {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(finishButton))
        .click();
  }

  /** click on 'Back' button */
  public void clickOnBackButton() {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(backButton))
        .click();
  }

  /** click on 'Next' button */
  public void clickOnNextButton() {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(nextButton))
        .click();
  }

  /**
   * select import for the title
   *
   * @param titleOfImport title of import
   */
  public void selectImport(String titleOfImport) {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format(Locators.IMPORT_FIELD, titleOfImport))))
        .click();
  }
}
