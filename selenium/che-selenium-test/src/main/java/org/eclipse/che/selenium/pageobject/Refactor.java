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
package org.eclipse.che.selenium.pageobject;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ATTACHING_ELEM_TO_DOM_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Aleksandr Shmaraev on 30.10.15 */
@Singleton
public class Refactor {

  private final SeleniumWebDriver seleniumWebDriver;
  private final Loader loader;

  @Inject
  public Refactor(SeleniumWebDriver seleniumWebDriver, Loader loader) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.loader = loader;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String RENAME_PACKAGE_FORM = "//div[text()='Rename Package']/ancestor::div[3]";
    String RENAME_JAVA_CLASS_FORM = "//div[text()='Rename Type']/ancestor::div[3]";
    String RENAME_COMPILATION_UNIT_FORM =
        "//div[text()='Rename Compilation Unit']/ancestor::div[3]";
    String RENAME_METHOD_FORM = "//div[text()='Rename Method']/ancestor::div[3]";
    String RENAME_FIELD_FORM = "//div[text()='Rename Field']/ancestor::div[3]";
    String RENAME_PARAMETERS_FORM = "//div[text()='Rename Local Variable']/ancestor::div[3]";
    String REFACTOR_PREVIEW_BUTTON = "move-preview-button";
    String REFACTOR_CANCEL_BUTTON = "move-cancel-button";
    String REFACTOR_OK_BUTTON = "move-accept-button";

    String MOVE_ITEM_FORM = "//div[text()='Move item']/ancestor::div[3]";
    String MOVE_DESTINATION_FOR_ITEM =
        "//div[text()='Choose destination for']//following::div[text()=' %s']";
    String MOVE_EXPAND_TREE_ICON = "//div[text()=' %s']/preceding::div[1]";

    String PREVIEW_FORM = "//div[text()='Rename Item']/ancestor::div[3]";
    String PREVIEW_OK_BUTTON = "preview-ok-button";

    String NEW_NAME_FIELD = "//div[text()='New name:']/following-sibling::input";
    String UPDATE_REFERENCES_CHECKBOX_SPAN =
        "//div[contains(text(), 'Update references')]/preceding-sibling::span";
    String UPDATE_REFERENCES_CHECKBOX =
        "//div[contains(text(), 'Update references')]/preceding-sibling::span/input";
    String UPDATE_VARIABLES_METHODS_CHECKBOX =
        "//div[text()='Update similarly named variables and methods']/preceding::span[1]";
    String RENAME_SUBPACKAGES_CHECKBOX_SPAN =
        "//div[text()='Rename subpackages']/preceding-sibling::span";
    String RENAME_SUBPACKAGES_CHECKBOX =
        "//div[text()='Rename subpackages']/preceding-sibling::span/input";
    String UPDATE_COMMENTS_STRINGS_CHECKBOX =
        "//div[text()='Update textual occurrences in comments and strings (forces preview)']"
            + "/preceding-sibling::span/input";
    String UPDATE_COMMENT_STRING_CHECKBOX_SPAN =
        "//div[text()='Update textual occurrences in comments and strings (forces preview)']"
            + "/preceding-sibling::span";
    String UPDATE_NON_JAVA_FILES_CHECKBOX =
        "//div[text()='Update fully qualified names in non-Java text files (forces preview)']"
            + "/preceding-sibling::span/input";
    String UPDATE_NON_JAVA_FILES_CHECKBOX_SPAN =
        "//div[text()='Update fully qualified names in non-Java text files (forces preview)']"
            + "/preceding-sibling::span";
    String NAME_PATTERNS_FIELD = "//div[text()='File name patterns:']/following-sibling::input";
    String ERROR_CONTAINER_OF_COMPILATION_FORM =
        "//div[@style[contains(.,'color: rgb(195, 77, 77)')]]";
    String TEXT_MESSAGE_MOVE_FORM =
        "//div[text()='Move item']/parent::div/parent::div/parent::div//div[text()='%s']";
    String ITEM_CHANGES_TO_BE_PERFORMED =
        "//div[contains(text(),'Rename')]/ancestor::div[3]//div[contains(text(),'%s')]";
    String EXPAND_ITEM_ICON =
        "//div[@id='tree-of-changes']//div[contains(text(),'%s')]//preceding::td[1]";
    String FLAG_ITEM =
        "//div[@id='tree-of-changes']//div[contains(text(),'%s')]/preceding-sibling::span";
    String FLAG_ITEM_INPUT =
        "//div[@id='tree-of-changes']//div[contains(text(),'%s')]/preceding-sibling::span/input";
    String PREVIEW_EDITOR_FRAME = "//iframe[contains(@src, 'Compare.html')]";
    String LEFT_EDITOR = "compareParentDiv_left_editor_id";
    String RIGHT_EDITOR = "compareParentDiv_right_editor_id";
    String LEASED_LINE_LEFT_EDITOR = "//div[contains(@class,'annotationLine addedBlockDiff')]";
    String LEASED_LINE_RIGHT_EDITOR = "//div[contains(@class,'annotationLine deletedBlockDiff')]";
  }

  @FindBy(xpath = Locators.RENAME_PACKAGE_FORM)
  WebElement renamePackageForm;

  @FindBy(xpath = Locators.RENAME_JAVA_CLASS_FORM)
  WebElement renameJavaClass;

  @FindBy(xpath = Locators.RENAME_METHOD_FORM)
  WebElement renameMethodForm;

  @FindBy(xpath = Locators.RENAME_PARAMETERS_FORM)
  WebElement renameParametersForm;

  @FindBy(xpath = Locators.MOVE_ITEM_FORM)
  WebElement moveItemForm;

  @FindBy(xpath = Locators.RENAME_FIELD_FORM)
  WebElement renameFieldForm;

  @FindBy(id = Locators.REFACTOR_PREVIEW_BUTTON)
  WebElement previewRefactorButton;

  @FindBy(id = Locators.REFACTOR_CANCEL_BUTTON)
  WebElement cancelRefactorButton;

  @FindBy(id = Locators.REFACTOR_OK_BUTTON)
  WebElement okRefactorButton;

  @FindBy(xpath = Locators.PREVIEW_FORM)
  WebElement previewRefactorForm;

  @FindBy(id = Locators.PREVIEW_OK_BUTTON)
  WebElement previewOkButton;

  @FindBy(xpath = Locators.UPDATE_REFERENCES_CHECKBOX_SPAN)
  WebElement updateReferencesCheckBoxSpan;

  @FindBy(xpath = Locators.UPDATE_REFERENCES_CHECKBOX)
  WebElement updateReferencesCheckBox;

  @FindBy(xpath = Locators.UPDATE_VARIABLES_METHODS_CHECKBOX)
  WebElement updateVarMethodsBox;

  @FindBy(xpath = Locators.RENAME_SUBPACKAGES_CHECKBOX_SPAN)
  WebElement renameSubpackagesCheckBoxSpan;

  @FindBy(xpath = Locators.RENAME_SUBPACKAGES_CHECKBOX)
  WebElement renameSubpackagesCheckBox;

  @FindBy(xpath = Locators.UPDATE_COMMENTS_STRINGS_CHECKBOX)
  WebElement updateCommentAndStringsBox;

  @FindBy(xpath = Locators.UPDATE_COMMENT_STRING_CHECKBOX_SPAN)
  WebElement updateCommentAndStringBoxSpan;

  @FindBy(xpath = Locators.UPDATE_NON_JAVA_FILES_CHECKBOX_SPAN)
  WebElement updateNonJavaFilesBoxSpan;

  @FindBy(xpath = Locators.UPDATE_NON_JAVA_FILES_CHECKBOX)
  WebElement updateNonJavaFilesBox;

  @FindBy(xpath = Locators.NEW_NAME_FIELD)
  WebElement newNameFileInput;

  @FindBy(xpath = Locators.NAME_PATTERNS_FIELD)
  WebElement namePatternsInput;

  @FindBy(xpath = Locators.RENAME_COMPILATION_UNIT_FORM)
  WebElement renameCompilationUnit;

  @FindBy(id = Locators.LEFT_EDITOR)
  WebElement leftEditor;

  @FindBy(id = Locators.RIGHT_EDITOR)
  WebElement rightEditor;

  /** wait the 'rename package form' is open */
  public void waitRenamePackageFormIsOpen() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(renamePackageForm));
  }

  /** wait the 'rename package form' is closed */
  public void waitRenamePackageFormIsClosed() {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(
            ExpectedConditions.invisibilityOfElementLocated(
                By.xpath(Locators.RENAME_PACKAGE_FORM)));
  }

  /** wait the 'rename compilation unit form' is open */
  public void waitRenameCompilationUnitFormIsOpen() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(renameCompilationUnit));
  }

  /** wait the 'Rename Method' form is open */
  public void waitRenameMethodFormIsOpen() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(renameMethodForm));
  }

  /** wait the 'Rename Method' form is closed */
  public void waitRenameMethodFormIsClosed() {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(
            ExpectedConditions.invisibilityOfElementLocated(By.xpath(Locators.RENAME_METHOD_FORM)));
  }

  /** wait the 'Rename Field' form is open */
  public void waitRenameFieldFormIsOpen() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(renameFieldForm));
  }

  /** wait the 'Rename Field' form is closed */
  public void waitRenameFieldFormIsClosed() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.invisibilityOfElementLocated(By.xpath(Locators.RENAME_FIELD_FORM)));
  }

  /** wait the 'Rename Parameters' form is open */
  public void waitRenameParametersFormIsOpen() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(renameParametersForm));
  }

  /** wait the 'Rename Parameters' form is closed */
  public void waitRenameParametersFormIsClosed() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.invisibilityOfElementLocated(
                By.xpath(Locators.RENAME_PARAMETERS_FORM)));
  }

  /** wait the 'Move item' form is open */
  public void waitMoveItemFormIsOpen() {
    loader.waitOnClosed();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(moveItemForm));
  }

  /** wait the 'Move item' form is closed */
  public void waitMoveItemFormIsClosed() {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(Locators.MOVE_ITEM_FORM)));
  }

  /**
   * click on the expand tree icon into the 'Move item' form
   *
   * @param name is name of destination
   */
  public void clickOnExpandIconTree(String name) {
    loader.waitOnClosed();
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format(Locators.MOVE_EXPAND_TREE_ICON, name))))
        .click();
  }

  /**
   * choose destination for item
   *
   * @param name is name of destination
   */
  public void chooseDestinationForItem(String name) {
    loader.waitOnClosed();
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format(Locators.MOVE_DESTINATION_FOR_ITEM, name))))
        .click();
  }

  /** click on the 'Preview' button */
  public void clickPreviewButtonRefactorForm() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.elementToBeClickable(previewRefactorButton))
        .click();
  }

  /** click on the 'Cancel' button */
  public void clickCancelButtonRefactorForm() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.elementToBeClickable(cancelRefactorButton))
        .click();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.invisibilityOfElementLocated(
                By.id(Locators.REFACTOR_CANCEL_BUTTON)));
  }

  /** click on the 'OK' button */
  public void clickOkButtonRefactorForm() {
    String someFailMessage = "//div[@style='color: rgb(195, 77, 77);' and contains(text(), ' ')]";
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(someFailMessage)));
    String activeStateLocator = "//button[@id='move-accept-button' and not(@disabled)]";
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(activeStateLocator)))
        .click();
    loader.waitOnClosed();
  }

  /** click on the 'Update references' checkbox */
  public void clickOnUpdateReferencesCheckbox() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(updateReferencesCheckBoxSpan))
        .click();
  }

  /** click on the 'OK' button in preview form */
  public void clickOkButtonPreviewForm() {
    previewOkButton.click();
    waitRefactorPreviewFormIsClosed();
  }

  /** wait the 'Update references' checkbox is selected */
  public void waitUpdateReferencesIsSelected() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.elementSelectionStateToBe(
                By.xpath(Locators.UPDATE_REFERENCES_CHECKBOX), true));
  }

  /** wait the 'Update references' checkbox is not selected */
  public void waitUpdateReferencesIsNotSelected() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.elementSelectionStateToBe(
                By.xpath(Locators.UPDATE_REFERENCES_CHECKBOX), false));
  }

  /** click on the 'Rename subpackages' checkbox */
  public void clickOnRenameSubpackagesCheckbox() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(renameSubpackagesCheckBoxSpan))
        .click();
  }

  /** wait the 'Rename subpackages' checkbox is selected */
  public void waitRenameSubpackagesIsSelected() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.elementSelectionStateToBe(
                By.xpath(Locators.RENAME_SUBPACKAGES_CHECKBOX), true));
  }

  /** wait the 'Rename subpackages' checkbox is not selected */
  public void waitRenameSubpackagesIsNotSelected() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.elementSelectionStateToBe(
                By.xpath(Locators.RENAME_SUBPACKAGES_CHECKBOX), false));
  }

  /**
   * Set rename subpackages checkbox to specified state and wait it state
   *
   * @param state state of checkbox (true if checkbox must be selected)
   */
  public void setAndWaitStateRenameSubpackagesCheckbox(boolean state) {
    if (state) {
      if (!renameSubpackagesCheckBox.isSelected()) {
        clickOnRenameSubpackagesCheckbox();
        waitRenameSubpackagesIsSelected();
      }
    } else {
      if (renameSubpackagesCheckBox.isSelected()) {
        clickOnRenameSubpackagesCheckbox();
        waitRenameSubpackagesIsNotSelected();
      }
    }
  }

  /**
   * Set update references checkbox to specified state and wait it state
   *
   * @param state state of checkbox (true if checkbox must be selected)
   */
  public void setAndWaitStateUpdateReferencesCheckbox(boolean state) {
    if (state) {
      if (!updateReferencesCheckBox.isSelected()) {
        clickOnUpdateReferencesCheckbox();
        waitUpdateReferencesIsSelected();
      }
    } else {
      if (updateReferencesCheckBox.isSelected()) {
        clickOnUpdateReferencesCheckbox();
        waitUpdateReferencesIsNotSelected();
      }
    }
  }

  /**
   * Set update the 'Update...in comments and strings' checkbox to specified state and wait it state
   *
   * @param state state of checkbox (true if checkbox must be selected)
   */
  public void setAndWaitStateCommentsAndStringsCheckbox(boolean state) {
    if (state) {
      if (!updateCommentAndStringsBox.isSelected()) {
        clickOnUpdateCommentsAndStringsCheckbox();
        waitUpdateCommentsAndStringsIsSelected();
      }
    } else {
      if (updateCommentAndStringsBox.isSelected()) {
        clickOnUpdateCommentsAndStringsCheckbox();
        waitUpdateCommentsAndStringsIsNotSelected();
      }
    }
  }

  /** click on the 'Update...in comments and strings' checkbox */
  public void clickOnUpdateCommentsAndStringsCheckbox() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(updateCommentAndStringBoxSpan))
        .click();
  }

  /** wait the 'Update...in comments and strings' checkbox is selected */
  public void waitUpdateCommentsAndStringsIsSelected() {
    String locator = Locators.UPDATE_COMMENTS_STRINGS_CHECKBOX;
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.elementSelectionStateToBe(By.xpath(locator), true));
  }

  /** wait the 'Update...in comments and strings' checkbox is not selected */
  public void waitUpdateCommentsAndStringsIsNotSelected() {
    String locator = Locators.UPDATE_COMMENTS_STRINGS_CHECKBOX;
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.elementSelectionStateToBe(By.xpath(locator), false));
  }

  /**
   * Set update non Java files checkbox to specified state and wait it state
   *
   * @param state state of checkbox (true if checkbox must be selected)
   */
  public void setAndWaitStateUpdateNonJavaFilesCheckbox(boolean state) {
    if (state) {
      if (!updateNonJavaFilesBox.isSelected()) {
        clickOnUpdateNonJavaFilesCheckbox();
        waitUpdateNonJavaFilesIsSelected();
      }
    } else {
      if (updateNonJavaFilesBox.isSelected()) {
        clickOnUpdateNonJavaFilesCheckbox();
        waitUpdateNonJavaFilesIsNotSelected();
      }
    }
  }

  /** click on the 'Update...in non-java text files' checkbox */
  public void clickOnUpdateNonJavaFilesCheckbox() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(updateNonJavaFilesBoxSpan))
        .click();
  }

  /** wait the 'Update...in non-java text files' checkbox is selected */
  public void waitUpdateNonJavaFilesIsSelected() {
    String locator = Locators.UPDATE_NON_JAVA_FILES_CHECKBOX;
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.elementSelectionStateToBe(By.xpath(locator), true));
  }

  /** wait the 'Update...in non-java text files' checkbox is selected */
  public void waitUpdateNonJavaFilesIsNotSelected() {
    String locator = Locators.UPDATE_NON_JAVA_FILES_CHECKBOX;
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.elementSelectionStateToBe(By.xpath(locator), false));
  }

  /**
   * type and wait new user value into 'New Name field'
   *
   * @param newValue new name
   */
  public void typeAndWaitNewName(String newValue) {
    typeNewName(newValue);
    waitTextIntoNewNameField(newValue);
  }

  /**
   * clear field, type a new user value into 'New Name field'
   *
   * @param newValue new name
   */
  public void typeNewName(String newValue) {
    try {
      new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
          .until(ExpectedConditions.visibilityOf(newNameFileInput))
          .clear();
      loader.waitOnClosed();
      newNameFileInput.sendKeys(newValue);
    } catch (Exception e) {
      new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
          .until(ExpectedConditions.visibilityOf(newNameFileInput))
          .clear();
      newNameFileInput.sendKeys(newValue);
    }
    new WebDriverWait(seleniumWebDriver, ATTACHING_ELEM_TO_DOM_SEC)
        .until(ExpectedConditions.textToBePresentInElementValue(newNameFileInput, newValue));
  }

  /**
   * type into refactoring widget field any key without clear
   *
   * @param keys
   */
  public void sendKeysIntoField(String keys) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(newNameFileInput))
        .sendKeys(keys);
    loader.waitOnClosed();
  }

  /**
   * wait new name into the ''New Name field'
   *
   * @param expectedName expected name in new name field
   */
  public void waitTextIntoNewNameField(final String expectedName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            (ExpectedCondition<Boolean>)
                webDriver ->
                    seleniumWebDriver
                        .findElement(By.xpath(Locators.NEW_NAME_FIELD))
                        .getAttribute("value")
                        .equals(expectedName));
  }

  /**
   * type and wait file name into 'File name patterns'
   *
   * @param fileName name of file
   */
  public void typeAndWaitFileNamePatterns(String fileName) {
    typeFileNamePatterns(fileName);
    waitTextIntoNamePatternsField(fileName);
  }

  /**
   * type a file name into 'File name patterns'
   *
   * @param fileName name of file
   */
  public void typeFileNamePatterns(String fileName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(namePatternsInput))
        .clear();
    namePatternsInput.sendKeys(fileName);
  }

  /**
   * wait file name into the 'File name patterns'
   *
   * @param expectedName expected file name
   */
  public void waitTextIntoNamePatternsField(final String expectedName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            (ExpectedCondition<Boolean>)
                webDriver ->
                    seleniumWebDriver
                        .findElement(By.xpath(Locators.NAME_PATTERNS_FIELD))
                        .getAttribute("value")
                        .equals(expectedName));
  }

  /**
   * wait error text fragment in the 'Rename compilation form'
   *
   * @param mess expected error
   */
  public void waitTextInErrorMessage(String mess) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.textToBePresentInElementLocated(
                By.xpath(Locators.ERROR_CONTAINER_OF_COMPILATION_FORM), mess));
  }

  /**
   * wait text message in the 'Move item' form
   *
   * @param mess expected message
   */
  public void waitTextInMoveForm(String mess) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            ExpectedConditions.presenceOfElementLocated(
                By.xpath(String.format(Locators.TEXT_MESSAGE_MOVE_FORM, mess))));
  }

  /**
   * check open state of refactoring wizard
   *
   * @return true if widget is open
   */
  public boolean isWidgetOpened() {
    try {
      return cancelRefactorButton.isDisplayed();
    } catch (NoSuchElementException ex) {
      return false;
    }
  }

  /** wait appearance of refactor preview form */
  public void waitRefactorPreviewFormIsOpened() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(previewRefactorForm));
  }

  /** wait the refactor preview form is closed */
  public void waitRefactorPreviewFormIsClosed() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.id(Locators.PREVIEW_FORM)));
  }

  /**
   * click on item by name or part of the name and number of position The numbering starts with
   * zero.
   *
   * @param nameItem name or part of name
   * @param position position of item
   */
  public void clickOnItemByNameAndPosition(String nameItem, int position) {
    List<WebElement> itemList =
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
            .until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(
                    By.xpath(String.format(Locators.ITEM_CHANGES_TO_BE_PERFORMED, nameItem))));
    itemList.get(position).click();
  }

  /**
   * check text from left editor on the number of coincidences
   *
   * @param expectedText expected text
   */
  public void checkTextFromLeftEditor(String expectedText) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.textToBePresentInElement(leftEditor, expectedText));
  }

  /**
   * check text from right editor on the number of coincidences
   *
   * @param expectedText expected text
   */
  public void checkTextFromRightEditor(String expectedText) {

    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.textToBePresentInElement(rightEditor, expectedText));
  }

  /**
   * Click on expand item by name or part of the name and number of position. The numbering starts
   * with zero.
   *
   * @param nameItem name of item
   * @param position number of position
   */
  public void clickOnExpandItemByNameAndPosition(String nameItem, int position) {

    List<WebElement> expandItemList =
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
            .until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(
                    By.xpath(String.format(Locators.EXPAND_ITEM_ICON, nameItem))));
    expandItemList.get(position).click();
  }

  /**
   * Click on flag item by name or part of the name and number of position. The numbering starts
   * with zero.
   *
   * @param nameItem name of item
   * @param position number of position
   */
  public void setFlagItemByNameAndPosition(String nameItem, int position) {
    clickOnItemByNameAndPosition(nameItem, position);
    List<WebElement> flagItemList =
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
            .until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(
                    By.xpath(String.format(Locators.FLAG_ITEM, nameItem))));
    flagItemList.get(position).click();
  }

  /**
   * This method return value of attribute "checked" for position of tree
   *
   * @param nameItem name of item
   * @param position number of position
   * @return value of attribute "checked"
   */
  public boolean itemIsSelectedByNameAndPosition(String nameItem, int position) {
    List<WebElement> itemList =
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
            .until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(
                    By.xpath(String.format(Locators.FLAG_ITEM_INPUT, nameItem))));
    return itemList.get(position).isSelected();
  }

  /**
   * Get quantity of leased lines in left Editor
   *
   * @return quantity of leased lines
   */
  public int getQuantityLeasedLineInLeftEditor() {
    int lineQuantity =
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
            .until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(
                    By.xpath(Locators.LEASED_LINE_LEFT_EDITOR)))
            .size();
    return lineQuantity;
  }

  /**
   * Get quantity of leased lines in right Editor
   *
   * @return quantity of leased lines
   */
  public int getQuantityLeasedLineInRightEditor() {
    int lineQuantity =
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
            .until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(
                    By.xpath(Locators.LEASED_LINE_RIGHT_EDITOR)))
            .size();
    return lineQuantity;
  }
}
