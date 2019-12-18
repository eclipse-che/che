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
package org.eclipse.che.selenium.pageobject.git;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Kuznetsov Mihail */
@Singleton
public class GitAddToIndex {
  interface Locators {
    String ADD_TO_INDEX_FORM = "gwt-debug-git-addToIndex-window";
    String ADD_BTN = "git-addToIndex-btnAdd";
    String UPDATE_CHKBOX = "gwt-debug-git-addToIndex-update-label";
    String ADD_TO_INDEX_MULTI_FILES =
        "//button[@id='git-addToIndex-btnAdd']/preceding::textarea[1]";
  }

  @FindBy(id = Locators.ADD_TO_INDEX_FORM)
  WebElement form;

  @FindBy(id = Locators.ADD_BTN)
  WebElement addBtn;

  @FindBy(id = Locators.UPDATE_CHKBOX)
  WebElement updateChkbox;

  @FindBy(xpath = Locators.ADD_TO_INDEX_MULTI_FILES)
  WebElement addIndexMultiSelect;

  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public GitAddToIndex(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  /**
   * Get name of file to be added to index
   *
   * @param text
   */
  public void waitFileNameToAdd(final String text) {
    new WebDriverWait(seleniumWebDriver, 7)
        .until(
            new ExpectedCondition<Boolean>() {
              @Override
              public Boolean apply(WebDriver driver) {
                return form.getText().contains(text);
              }
            });
  }

  public void waitFormToOpen() {
    new WebDriverWait(seleniumWebDriver, 10).until(ExpectedConditions.visibilityOf(form));
  }

  public void waitFormToClose() {
    new WebDriverWait(seleniumWebDriver, 7)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.id(Locators.ADD_TO_INDEX_FORM)));
  }

  /** Click add button */
  public void clickAddBtn() {
    addBtn.click();
  }

  public void selectUpdateCheckBox() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(updateChkbox))
        .click();
  }

  /**
   * wait the files selected by multi-select in the add to index form
   *
   * @param files is the selected files
   */
  public void waitFilesAddIndexByMultiSelect(final String files) {
    waitFormToOpen();
    new WebDriverWait(seleniumWebDriver, 10)
        .until(
            new ExpectedCondition<Boolean>() {
              @Override
              public Boolean apply(WebDriver webDriver) {
                return addIndexMultiSelect.getAttribute("value").contains(files);
              }
            });
  }
}
