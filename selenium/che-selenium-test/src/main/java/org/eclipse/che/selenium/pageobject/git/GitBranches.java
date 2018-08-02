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

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

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

/** @author Musienko Maxim */
@Singleton
public class GitBranches {
  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public GitBranches(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String CHECKOUT_IMG_BASE64 = "data:image/svg+xml;base64";
    String MAIN_FORM_ID = "gwt-debug-git-branches-branchesPanel";
    String BRANCH_NAME_IN_LIST_PREFIX = "gwt-debug-git-branches-%s";
    String BRANCH_NAME_WITH_CHECKOUT_IMG_XPATH =
        "//div[@id='gwt-debug-git-branches-%s']/parent::td/following-sibling::td/img";
    String CHECKOUT_BTN_ID = "git-branches-checkout";
    String CREATE_BTN_ID = "git-branches-create";
    String DELETE_BTN_ID = "git-branches-delete";
    String RENAME_BTN_ID = "git-branches-rename";
    String CLOSE_BTN_ID = "git-branches-close";
    String DELBRANCH_FORM = "//table[@title='Delete branch']"; // TODO CREATE ID
    String DELBRANCH_BUTN_OK = "ask-dialog-ok";
    String SEARCH_FILTER_LABEL_ID = "gwt-debug-git-branches-search_filter";
  }

  @FindBy(id = Locators.MAIN_FORM_ID)
  WebElement mainForm;

  @FindBy(id = Locators.CHECKOUT_BTN_ID)
  WebElement checkOutBtn;

  @FindBy(id = Locators.CREATE_BTN_ID)
  WebElement createBtn;

  @FindBy(id = Locators.DELETE_BTN_ID)
  WebElement deleteBtn;

  @FindBy(id = Locators.RENAME_BTN_ID)
  WebElement renameBtn;

  @FindBy(id = Locators.CLOSE_BTN_ID)
  WebElement closeBtn;

  @FindBy(xpath = Locators.DELBRANCH_FORM)
  WebElement delBranchForm;

  @FindBy(id = Locators.DELBRANCH_BUTN_OK)
  WebElement buttonOK;

  @FindBy(id = Locators.SEARCH_FILTER_LABEL_ID)
  WebElement searchFilterLabel;

  /** wait appearance of the IDE branches form */
  public void waitBranchesForm() {
    new WebDriverWait(seleniumWebDriver, 15).until(ExpectedConditions.visibilityOf(mainForm));
  }

  /** wait disappear of the IDE branches form */
  public void waitBranchesFormISIsClosed() {
    new WebDriverWait(seleniumWebDriver, 15)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.id(Locators.MAIN_FORM_ID)));
  }

  public void waitListBranchesInMainForm(final String listNames) {
    waitBranchesForm();
    new WebDriverWait(seleniumWebDriver, 5)
        .until(
            new ExpectedCondition<Boolean>() {
              @Override
              public Boolean apply(WebDriver webDriver) {
                return mainForm.getText().equals(listNames);
              }
            });
  }

  /** wait branch with specified name */
  public void waitBranchWithName(String nameOfBranch) {
    new WebDriverWait(seleniumWebDriver, 5)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.id(String.format(Locators.BRANCH_NAME_IN_LIST_PREFIX, nameOfBranch))));
  }

  public void disappearBranchName(String nameOfBranch) {
    new WebDriverWait(seleniumWebDriver, 5)
        .until(
            ExpectedConditions.invisibilityOfElementLocated(
                By.id(String.format(Locators.BRANCH_NAME_IN_LIST_PREFIX, nameOfBranch))));
  }

  /** wait branch with specified name and check out status */
  public void waitBranchWithNameCheckoutState(final String nameOfBranch) {
    new WebDriverWait(seleniumWebDriver, 10)
        .until(
            (WebDriver webDriver) -> {
              return seleniumWebDriver
                  .findElement(
                      By.xpath(
                          String.format(
                              Locators.BRANCH_NAME_WITH_CHECKOUT_IMG_XPATH, nameOfBranch)))
                  .getAttribute("src")
                  .contains(Locators.CHECKOUT_IMG_BASE64);
            });
  }

  /** */
  public void waitEnabledButtonCreate() {
    new WebDriverWait(seleniumWebDriver, 10)
        .until(
            new ExpectedCondition<Boolean>() {
              @Override
              public Boolean apply(WebDriver webDriver) {
                return (seleniumWebDriver.findElement(By.id(Locators.CREATE_BTN_ID)).isEnabled());
              }
            });
  }

  /**
   * select given branch check enabled or not button delete
   *
   * @param nameOfBranch is name of branch
   */
  public void selectBranchInListAndCheckEnabledButtonDelete(final String nameOfBranch) {
    new WebDriverWait(seleniumWebDriver, 10)
        .until(
            new ExpectedCondition<Boolean>() {
              @Override
              public Boolean apply(WebDriver webDriver) {
                seleniumWebDriver
                    .findElement(
                        By.id(String.format(Locators.BRANCH_NAME_IN_LIST_PREFIX, nameOfBranch)))
                    .click();
                return (seleniumWebDriver.findElement(By.id(Locators.DELETE_BTN_ID)).isEnabled());
              }
            });
  }

  /**
   * select given branch check enabled or not button rename
   *
   * @param nameOfBranch is name of branch
   */
  public void selectBranchInListAndCheckEnabledButtonRename(final String nameOfBranch) {
    new WebDriverWait(seleniumWebDriver, 15)
        .until(
            new ExpectedCondition<Boolean>() {
              @Override
              public Boolean apply(WebDriver webDriver) {
                seleniumWebDriver
                    .findElement(
                        By.id(String.format(Locators.BRANCH_NAME_IN_LIST_PREFIX, nameOfBranch)))
                    .click();
                return (seleniumWebDriver.findElement(By.id(Locators.RENAME_BTN_ID)).isEnabled());
              }
            });
  }

  /**
   * select given branch check enabled or not button checkout
   *
   * @param nameOfBranch is name of branch
   */
  public void selectBranchAndCheckEnabledButtonCheckout(final String nameOfBranch) {
    new WebDriverWait(seleniumWebDriver, 5)
        .until(
            new ExpectedCondition<Boolean>() {
              @Override
              public Boolean apply(WebDriver webDriver) {
                new WebDriverWait(seleniumWebDriver, 5)
                    .until(
                        ExpectedConditions.visibilityOfElementLocated(
                            By.id(
                                String.format(Locators.BRANCH_NAME_IN_LIST_PREFIX, nameOfBranch))))
                    .click();
                return seleniumWebDriver.findElement(By.id(Locators.CHECKOUT_BTN_ID)).isEnabled();
              }
            });
  }

  /** wait ask dialog form for deleting selected branch */
  public void openDelBranchForm() {
    new WebDriverWait(seleniumWebDriver, 3).until(ExpectedConditions.visibilityOf(delBranchForm));
  }

  /** wait closing ask dialog form for deleting selected branch */
  public void closeDelBranch() {
    new WebDriverWait(seleniumWebDriver, 3)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(Locators.DELBRANCH_FORM)));
  }

  /** click on OK button */
  public void clickButtonOk() {
    buttonOK.click();
  }

  /** click on checkout button */
  public void clickCheckOutBtn() {
    new WebDriverWait(seleniumWebDriver, 10)
        .until(
            new ExpectedCondition<Boolean>() {
              @Override
              public Boolean apply(WebDriver webDriver) {
                return (seleniumWebDriver.findElement(By.id(Locators.CHECKOUT_BTN_ID)).isEnabled());
              }
            });
    checkOutBtn.click();
  }

  /** click on create button */
  public void clickCreateBtn() {
    createBtn.click();
  }

  /** click on delete button */
  public void clickDeleteBtn() {
    deleteBtn.click();
  }

  /** click on close button */
  public void clickCloseBtn() {
    closeBtn.click();
  }

  /** click on rename button */
  public void clickRenameBtn() {
    renameBtn.click();
  }

  /**
   * wait for the search filter label to be with given text.
   *
   * @param text text to check
   */
  public void waitSearchFilerWithText(String text) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.textToBePresentInElement(searchFilterLabel, text));
  }

  /**
   * Type text to the branch search filter.
   *
   * @param text typed text
   */
  public void typeSearchFilter(String text) {
    seleniumWebDriver.getKeyboard().sendKeys(text);
  }
}
