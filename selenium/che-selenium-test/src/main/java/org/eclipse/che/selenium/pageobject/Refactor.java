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

import static java.lang.String.format;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.Refactor.Locators.ERROR_CONTAINER_OF_COMPILATION_FORM;
import static org.eclipse.che.selenium.pageobject.Refactor.Locators.EXPAND_ITEM_ICON;
import static org.eclipse.che.selenium.pageobject.Refactor.Locators.FLAG_ITEM;
import static org.eclipse.che.selenium.pageobject.Refactor.Locators.FLAG_ITEM_INPUT;
import static org.eclipse.che.selenium.pageobject.Refactor.Locators.ITEM_CHANGES_TO_BE_PERFORMED;
import static org.eclipse.che.selenium.pageobject.Refactor.Locators.LEASED_LINE_LEFT_EDITOR;
import static org.eclipse.che.selenium.pageobject.Refactor.Locators.LEASED_LINE_RIGHT_EDITOR;
import static org.eclipse.che.selenium.pageobject.Refactor.Locators.MOVE_DESTINATION_FOR_ITEM;
import static org.eclipse.che.selenium.pageobject.Refactor.Locators.MOVE_EXPAND_TREE_ICON;
import static org.eclipse.che.selenium.pageobject.Refactor.Locators.NEW_NAME_FIELD;
import static org.eclipse.che.selenium.pageobject.Refactor.Locators.PREVIEW_FORM;
import static org.eclipse.che.selenium.pageobject.Refactor.Locators.REFACTOR_CANCEL_BUTTON;
import static org.eclipse.che.selenium.pageobject.Refactor.Locators.RENAME_FIELD_FORM;
import static org.eclipse.che.selenium.pageobject.Refactor.Locators.RENAME_METHOD_FORM;
import static org.eclipse.che.selenium.pageobject.Refactor.Locators.RENAME_PACKAGE_FORM;
import static org.eclipse.che.selenium.pageobject.Refactor.Locators.RENAME_PARAMETERS_FORM;
import static org.eclipse.che.selenium.pageobject.Refactor.Locators.RENAME_SUBPACKAGES_CHECKBOX;
import static org.eclipse.che.selenium.pageobject.Refactor.Locators.TEXT_MESSAGE_MOVE_FORM;
import static org.eclipse.che.selenium.pageobject.Refactor.Locators.UPDATE_COMMENTS_STRINGS_CHECKBOX;
import static org.eclipse.che.selenium.pageobject.Refactor.Locators.UPDATE_NON_JAVA_FILES_CHECKBOX;
import static org.eclipse.che.selenium.pageobject.Refactor.Locators.UPDATE_REFERENCES_CHECKBOX;
import static org.openqa.selenium.support.ui.ExpectedConditions.attributeToBe;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementSelectionStateToBe;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfAllElementsLocatedBy;
import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElement;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfAllElementsLocatedBy;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Aleksandr Shmaraev on 30.10.15 */
@Singleton
public class Refactor {

  private final SeleniumWebDriver seleniumWebDriver;
  private final Loader loader;
  private final WebDriverWait redrawUiElementWait;
  private final WebDriverWait loadPageWait;
  private final WebDriverWait elementWait;

  @Inject
  public Refactor(SeleniumWebDriver seleniumWebDriver, Loader loader) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.loader = loader;
    this.redrawUiElementWait = new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
    this.loadPageWait = new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC);
    this.elementWait = new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC);
    PageFactory.initElements(seleniumWebDriver, this);
  }

  protected interface Locators {
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

  @FindBy(xpath = RENAME_PACKAGE_FORM)
  WebElement renamePackageForm;

  @FindBy(xpath = Locators.RENAME_JAVA_CLASS_FORM)
  WebElement renameJavaClass;

  @FindBy(xpath = RENAME_METHOD_FORM)
  WebElement renameMethodForm;

  @FindBy(xpath = RENAME_PARAMETERS_FORM)
  WebElement renameParametersForm;

  @FindBy(xpath = Locators.MOVE_ITEM_FORM)
  WebElement moveItemForm;

  @FindBy(xpath = RENAME_FIELD_FORM)
  WebElement renameFieldForm;

  @FindBy(id = Locators.REFACTOR_PREVIEW_BUTTON)
  WebElement previewRefactorButton;

  @FindBy(id = REFACTOR_CANCEL_BUTTON)
  WebElement cancelRefactorButton;

  @FindBy(id = Locators.REFACTOR_OK_BUTTON)
  WebElement okRefactorButton;

  @FindBy(xpath = PREVIEW_FORM)
  WebElement previewRefactorForm;

  @FindBy(id = Locators.PREVIEW_OK_BUTTON)
  WebElement previewOkButton;

  @FindBy(xpath = Locators.UPDATE_REFERENCES_CHECKBOX_SPAN)
  WebElement updateReferencesCheckBoxSpan;

  @FindBy(xpath = UPDATE_REFERENCES_CHECKBOX)
  WebElement updateReferencesCheckBox;

  @FindBy(xpath = Locators.UPDATE_VARIABLES_METHODS_CHECKBOX)
  WebElement updateVarMethodsBox;

  @FindBy(xpath = Locators.RENAME_SUBPACKAGES_CHECKBOX_SPAN)
  WebElement renameSubpackagesCheckBoxSpan;

  @FindBy(xpath = RENAME_SUBPACKAGES_CHECKBOX)
  WebElement renameSubpackagesCheckBox;

  @FindBy(xpath = UPDATE_COMMENTS_STRINGS_CHECKBOX)
  WebElement updateCommentAndStringsBox;

  @FindBy(xpath = Locators.UPDATE_COMMENT_STRING_CHECKBOX_SPAN)
  WebElement updateCommentAndStringBoxSpan;

  @FindBy(xpath = Locators.UPDATE_NON_JAVA_FILES_CHECKBOX_SPAN)
  WebElement updateNonJavaFilesBoxSpan;

  @FindBy(xpath = UPDATE_NON_JAVA_FILES_CHECKBOX)
  WebElement updateNonJavaFilesBox;

  @FindBy(xpath = NEW_NAME_FIELD)
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
    redrawUiElementWait.until(visibilityOf(renamePackageForm));
  }

  /** wait the 'rename package form' is closed */
  public void waitRenamePackageFormIsClosed() {
    elementWait.until(invisibilityOfElementLocated(By.xpath(RENAME_PACKAGE_FORM)));
  }

  /** wait the 'rename compilation unit form' is open */
  public void waitRenameCompilationUnitFormIsOpen() {
    elementWait.until(visibilityOf(renameCompilationUnit));
  }

  /** wait the 'Rename Method' form is open */
  public void waitRenameMethodFormIsOpen() {
    redrawUiElementWait.until(visibilityOf(renameMethodForm));
  }

  /** wait the 'Rename Method' form is closed */
  public void waitRenameMethodFormIsClosed() {
    elementWait.until(invisibilityOfElementLocated(By.xpath(RENAME_METHOD_FORM)));
  }

  /** wait the 'Rename Field' form is open */
  public void waitRenameFieldFormIsOpen() {
    redrawUiElementWait.until(visibilityOf(renameFieldForm));
  }

  /** wait the 'Rename Field' form is closed */
  public void waitRenameFieldFormIsClosed() {
    redrawUiElementWait.until(invisibilityOfElementLocated(By.xpath(RENAME_FIELD_FORM)));
  }

  /** wait the 'Rename Parameters' form is open */
  public void waitRenameParametersFormIsOpen() {
    redrawUiElementWait.until(visibilityOf(renameParametersForm));
  }

  /** wait the 'Rename Parameters' form is closed */
  public void waitRenameParametersFormIsClosed() {
    redrawUiElementWait.until(invisibilityOfElementLocated(By.xpath(RENAME_PARAMETERS_FORM)));
  }

  /** wait the 'Move item' form is open */
  public void waitMoveItemFormIsOpen() {
    loader.waitOnClosed();
    redrawUiElementWait.until(visibilityOf(moveItemForm));
  }

  /** wait the 'Move item' form is closed */
  public void waitMoveItemFormIsClosed() {
    elementWait.until(invisibilityOfElementLocated(By.xpath(Locators.MOVE_ITEM_FORM)));
  }

  /**
   * click on the expand tree icon into the 'Move item' form
   *
   * @param name is name of destination
   */
  public void clickOnExpandIconTree(String name) {
    loader.waitOnClosed();
    loadPageWait
        .until(visibilityOfElementLocated(By.xpath(format(MOVE_EXPAND_TREE_ICON, name))))
        .click();
  }

  /**
   * choose destination for item
   *
   * @param name is name of destination
   */
  public void chooseDestinationForItem(String name) {
    loader.waitOnClosed();
    loadPageWait
        .until(visibilityOfElementLocated(By.xpath(format(MOVE_DESTINATION_FOR_ITEM, name))))
        .click();
  }

  /** click on the 'Preview' button */
  public void clickPreviewButtonRefactorForm() {
    redrawUiElementWait.until(elementToBeClickable(previewRefactorButton)).click();
  }

  /** click on the 'Cancel' button */
  public void clickCancelButtonRefactorForm() {
    redrawUiElementWait.until(elementToBeClickable(cancelRefactorButton)).click();
    redrawUiElementWait.until(invisibilityOfElementLocated(By.id(REFACTOR_CANCEL_BUTTON)));
  }

  /** click on the 'OK' button */
  public void clickOkButtonRefactorForm() {
    redrawUiElementWait.until(
        invisibilityOfElementLocated(
            By.xpath("//div[@style='color: rgb(195, 77, 77);' and contains(text(), ' ')]")));
    redrawUiElementWait
        .until(
            visibilityOfElementLocated(
                By.xpath("//button[@id='move-accept-button' and not(@disabled)]")))
        .click();
    loader.waitOnClosed();
  }

  /** click on the 'Update references' checkbox */
  public void clickOnUpdateReferencesCheckbox() {
    redrawUiElementWait.until(visibilityOf(updateReferencesCheckBoxSpan)).click();
  }

  /** click on the 'OK' button in preview form */
  public void clickOkButtonPreviewForm() {
    previewOkButton.click();
    waitRefactorPreviewFormIsClosed();
  }

  /** wait the 'Update references' checkbox is selected */
  public void waitUpdateReferencesIsSelected() {
    redrawUiElementWait.until(
        elementSelectionStateToBe(By.xpath(UPDATE_REFERENCES_CHECKBOX), true));
  }

  /** wait the 'Update references' checkbox is not selected */
  public void waitUpdateReferencesIsNotSelected() {
    redrawUiElementWait.until(
        elementSelectionStateToBe(By.xpath(UPDATE_REFERENCES_CHECKBOX), false));
  }

  /** click on the 'Rename subpackages' checkbox */
  public void clickOnRenameSubpackagesCheckbox() {
    redrawUiElementWait.until(visibilityOf(renameSubpackagesCheckBoxSpan)).click();
  }

  /** wait the 'Rename subpackages' checkbox is selected */
  public void waitRenameSubpackagesIsSelected() {
    redrawUiElementWait.until(
        elementSelectionStateToBe(By.xpath(RENAME_SUBPACKAGES_CHECKBOX), true));
  }

  /** wait the 'Rename subpackages' checkbox is not selected */
  public void waitRenameSubpackagesIsNotSelected() {
    redrawUiElementWait.until(
        elementSelectionStateToBe(By.xpath(RENAME_SUBPACKAGES_CHECKBOX), false));
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
    redrawUiElementWait.until(visibilityOf(updateCommentAndStringBoxSpan)).click();
  }

  /** wait the 'Update...in comments and strings' checkbox is selected */
  public void waitUpdateCommentsAndStringsIsSelected() {
    redrawUiElementWait.until(
        elementSelectionStateToBe(By.xpath(UPDATE_COMMENTS_STRINGS_CHECKBOX), true));
  }

  /** wait the 'Update...in comments and strings' checkbox is not selected */
  public void waitUpdateCommentsAndStringsIsNotSelected() {
    redrawUiElementWait.until(
        elementSelectionStateToBe(By.xpath(UPDATE_COMMENTS_STRINGS_CHECKBOX), false));
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
    redrawUiElementWait.until(visibilityOf(updateNonJavaFilesBoxSpan)).click();
  }

  /** wait the 'Update...in non-java text files' checkbox is selected */
  public void waitUpdateNonJavaFilesIsSelected() {
    redrawUiElementWait.until(
        elementSelectionStateToBe(By.xpath(UPDATE_NON_JAVA_FILES_CHECKBOX), true));
  }

  /** wait the 'Update...in non-java text files' checkbox is selected */
  public void waitUpdateNonJavaFilesIsNotSelected() {
    redrawUiElementWait.until(
        elementSelectionStateToBe(By.xpath(UPDATE_NON_JAVA_FILES_CHECKBOX), false));
  }

  /**
   * type and wait new user value into 'New Name field'
   *
   * @param newValue new name
   */
  public void typeAndWaitNewName(String newValue) {
    typeAndWaitText(newNameFileInput, newValue);
  }

  /**
   * type into refactoring widget field any key without clear
   *
   * @param keys
   */
  public void sendKeysIntoField(String keys) {
    waitElementVisibility(newNameFileInput).sendKeys(keys);
  }

  /**
   * type into refactoring widget field any key with clear
   *
   * @param keys
   */
  public void clearFieldAndSendKeys(String keys) {
    waitElementVisibility(newNameFileInput).clear();
    waitExpectedText(newNameFileInput, "");
    waitElementVisibility(newNameFileInput).sendKeys(keys);
  }

  /**
   * wait new name into the ''New Name field'
   *
   * @param expectedName expected name in new name field
   */
  public void waitTextIntoNewNameField(final String expectedName) {
    waitExpectedText(By.xpath(NEW_NAME_FIELD), expectedName);
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
    typeAndWaitText(namePatternsInput, fileName);
  }

  /**
   * wait file name into the 'File name patterns'
   *
   * @param expectedText expected file name
   */
  public void waitTextIntoNamePatternsField(final String expectedText) {
    waitExpectedText(namePatternsInput, expectedText);
  }

  /**
   * wait error text fragment in the 'Rename compilation form'
   *
   * @param expectedText expected error
   */
  public void waitTextInErrorMessage(String expectedText) {
    waitExpectedText(By.xpath(ERROR_CONTAINER_OF_COMPILATION_FORM), expectedText);
  }

  /**
   * wait text message in the 'Move item' form
   *
   * @param expectedText expected message
   */
  public void waitTextInMoveForm(String expectedText) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(format(TEXT_MESSAGE_MOVE_FORM, expectedText))));
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
    waitElementVisibility(previewRefactorForm);
  }

  /** wait the refactor preview form is closed */
  public void waitRefactorPreviewFormIsClosed() {
    redrawUiElementWait.until(invisibilityOfElementLocated(By.id(PREVIEW_FORM)));
  }

  /**
   * click on item by name or part of the name and number of position The numbering starts with
   * zero.
   *
   * @param nameItem name or part of name
   * @param position position of item
   */
  public void clickOnItemByNameAndPosition(String nameItem, int position) {
    redrawUiElementWait
        .until(
            visibilityOfAllElementsLocatedBy(
                By.xpath(format(ITEM_CHANGES_TO_BE_PERFORMED, nameItem))))
        .get(position)
        .click();
  }

  /**
   * check text from left editor on the number of coincidences
   *
   * @param expectedText expected text
   */
  public void checkTextFromLeftEditor(String expectedText) {
    loadPageWait.until(textToBePresentInElement(leftEditor, expectedText));
  }

  /**
   * check text from right editor on the number of coincidences
   *
   * @param expectedText expected text
   */
  public void checkTextFromRightEditor(String expectedText) {
    loadPageWait.until(textToBePresentInElement(rightEditor, expectedText));
  }

  /**
   * Click on expand item by name or part of the name and number of position. The numbering starts
   * with zero.
   *
   * @param nameItem name of item
   * @param position number of position
   */
  public void clickOnExpandItemByNameAndPosition(String nameItem, int position) {
    redrawUiElementWait
        .until(presenceOfAllElementsLocatedBy(By.xpath(format(EXPAND_ITEM_ICON, nameItem))))
        .get(position)
        .click();
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
    redrawUiElementWait
        .until(presenceOfAllElementsLocatedBy(By.xpath(format(FLAG_ITEM, nameItem))))
        .get(position)
        .click();
  }

  /**
   * This method return value of attribute "checked" for position of tree
   *
   * @param nameItem name of item
   * @param position number of position
   * @return value of attribute "checked"
   */
  public boolean itemIsSelectedByNameAndPosition(String nameItem, int position) {
    return redrawUiElementWait
        .until(presenceOfAllElementsLocatedBy(By.xpath(format(FLAG_ITEM_INPUT, nameItem))))
        .get(position)
        .isSelected();
  }

  /**
   * Get quantity of leased lines in left Editor
   *
   * @return quantity of leased lines
   */
  public int getQuantityLeasedLineInLeftEditor() {
    return redrawUiElementWait
        .until(visibilityOfAllElementsLocatedBy(By.xpath(LEASED_LINE_LEFT_EDITOR)))
        .size();
  }

  /**
   * Get quantity of leased lines in right Editor
   *
   * @return quantity of leased lines
   */
  public int getQuantityLeasedLineInRightEditor() {
    return redrawUiElementWait
        .until(presenceOfAllElementsLocatedBy(By.xpath(LEASED_LINE_RIGHT_EDITOR)))
        .size();
  }

  private void typeAndWaitText(WebElement element, String newValue) {
    waitElementVisibility(element).clear();
    waitExpectedText(element, "");
    waitElementVisibility(element).sendKeys(newValue);
    waitExpectedText(element, newValue);
  }

  private void waitExpectedText(WebElement element, String expectedText) {
    waitElementVisibility(element);
    loadPageWait.until(attributeToBe(element, "value", expectedText));
  }

  private void waitExpectedText(By locator, String expectedText) {
    loadPageWait.until(
        (ExpectedCondition<Boolean>)
            driver -> waitElementVisibility(locator).getAttribute("value").equals(expectedText));
  }

  private WebElement waitElementVisibility(WebElement element) {
    return loadPageWait.until(visibilityOf(element));
  }

  private WebElement waitElementVisibility(By locator) {
    return loadPageWait.until(visibilityOfElementLocated(locator));
  }
}
