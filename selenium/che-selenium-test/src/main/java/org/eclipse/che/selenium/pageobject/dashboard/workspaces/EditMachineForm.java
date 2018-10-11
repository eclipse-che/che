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

import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.EditMachineForm.Locators.CLOSE_BUTTON_XPATH;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.EditMachineForm.Locators.CLOSE_ICON_XPATH;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.EditMachineForm.Locators.EDIT_MACHINE_FORM_BODY_XPATH;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.EditMachineForm.Locators.NAME_ERROR_MESSAGE;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.EditMachineForm.Locators.NAME_TEXT_FIELD_XPATH;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.EditMachineForm.Locators.RAM_TEXT_FIELD;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.EditMachineForm.Locators.RECIPE_EDITOR_BODY_XPATH;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.EditMachineForm.Locators.RECIPE_EDITOR_CURSOR_XPATH;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.EditMachineForm.Locators.RECIPE_EDITOR_TEXT_XPATH;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.EditMachineForm.Locators.SAVE_BUTTON_XPATH;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.EditMachineForm.Locators.SLIDER_BAR_XPATH;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.EditMachineForm.Locators.SLIDER_BUTTON_XPATH;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.EditMachineForm.Locators.SLIDER_RAM_TEXT_FIELD_XPATH;

import com.google.inject.Inject;
import java.util.stream.Collectors;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.pageobject.TestWebElementRenderChecker;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

public class EditMachineForm {
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;
  private final TestWebElementRenderChecker testWebElementRenderChecker;

  @Inject
  private EditMachineForm(
      SeleniumWebDriverHelper seleniumWebDriverHelper,
      TestWebElementRenderChecker testWebElementRenderChecker) {
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    this.testWebElementRenderChecker = testWebElementRenderChecker;
  }

  public interface Locators {
    String EDIT_MACHINE_FORM_BODY_XPATH = "//md-dialog[@aria-label='Edit the machineName: ...']";
    String NAME_TEXT_FIELD_XPATH = EDIT_MACHINE_FORM_BODY_XPATH + "//input[@name='name']";
    String RECIPE_EDITOR_BODY_XPATH =
        EDIT_MACHINE_FORM_BODY_XPATH + "//div[@class='CodeMirror-scroll']";
    String RECIPE_EDITOR_TEXT_XPATH =
        RECIPE_EDITOR_BODY_XPATH + "//pre[@class=' CodeMirror-line ']";
    String CLOSE_ICON_XPATH = EDIT_MACHINE_FORM_BODY_XPATH + "//i";
    String SAVE_BUTTON_XPATH = EDIT_MACHINE_FORM_BODY_XPATH + "//button[span[text()='Save']]";
    String CLOSE_BUTTON_XPATH = EDIT_MACHINE_FORM_BODY_XPATH + "//button[span[text()='Close']]";
    String RAM_TEXT_FIELD = "//input[@aria-label='Amount of RAM']";
    String SLIDER_BUTTON_XPATH = "//div[@class='md-thumb']";
    String SLIDER_BAR_XPATH = "//md-slider[@aria-label='Amount of RAM']";
    String SLIDER_RAM_TEXT_FIELD_XPATH =
        EDIT_MACHINE_FORM_BODY_XPATH + "//div[@class='ng-binding']";
    String NAME_ERROR_MESSAGE = EDIT_MACHINE_FORM_BODY_XPATH + "//div/che-error-messages/div";
    String RECIPE_EDITOR_CURSOR_XPATH = "(//div[@class='CodeMirror-cursor'])[2]";
  }

  public void waitRecipeCursorVisibility() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(RECIPE_EDITOR_CURSOR_XPATH));
  }

  public void waitRecipeCursorInvisibility() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(RECIPE_EDITOR_CURSOR_XPATH));
  }

  public void clickOnRecipeForm() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(RECIPE_EDITOR_TEXT_XPATH));
  }

  public void typeToRecipe(String text) {
    seleniumWebDriverHelper.setText(By.xpath(RECIPE_EDITOR_TEXT_XPATH), text);
  }

  public void waitNameErrorMessage(String expectedMessage) {
    seleniumWebDriverHelper.waitTextEqualsTo(By.xpath(NAME_ERROR_MESSAGE), expectedMessage);
  }

  public String getSliderRamValue() {
    return seleniumWebDriverHelper.waitVisibilityAndGetText(By.xpath(SLIDER_RAM_TEXT_FIELD_XPATH));
  }

  public void waitSliderRamValue(String expectedValue) {
    seleniumWebDriverHelper.waitSuccessCondition(
        driver -> getSliderRamValue().equals(expectedValue));
  }

  public WebElement waitSliderButton() {
    return seleniumWebDriverHelper.waitVisibility(By.xpath(SLIDER_BUTTON_XPATH));
  }

  public WebElement waitSliderBar() {
    return seleniumWebDriverHelper.waitVisibility(By.xpath(SLIDER_BAR_XPATH));
  }

  public void dragSliderButtonToSliderBarCenter() {
    Actions action = seleniumWebDriverHelper.getAction();

    action.clickAndHold(waitSliderButton()).perform();
    seleniumWebDriverHelper.moveCursorTo(waitSliderBar());
    action.release();
  }

  public void clickOnSlideBarCenter() {
    seleniumWebDriverHelper.moveCursorTo(waitSliderBar());
    seleniumWebDriverHelper.getAction().click();
  }

  public WebElement waitRamTextField() {
    return seleniumWebDriverHelper.waitVisibility(By.xpath(RAM_TEXT_FIELD));
  }

  public void typeRam(String ramValue) {
    seleniumWebDriverHelper.setValue(waitRamTextField(), ramValue);
  }

  public void waitRamFieldText(String expectedText) {
    seleniumWebDriverHelper.waitValueEqualsTo(waitRamTextField(), expectedText);
  }

  public boolean isRamValueValid() {
    final String invalidRamFlagAttribute = "aria-invalid";
    return seleniumWebDriverHelper
        .waitVisibilityAndGetAttribute(waitRamTextField(), invalidRamFlagAttribute)
        .equals("false");
  }

  public void waitValidRamFieldHighlighting() {
    seleniumWebDriverHelper.waitSuccessCondition(driver -> isRamValueValid());
  }

  public void waitInvalideRamFieldHighlighting() {
    seleniumWebDriverHelper.waitSuccessCondition(driver -> !isRamValueValid());
  }

  public WebElement waitSaveButton() {
    return seleniumWebDriverHelper.waitVisibility(By.xpath(SAVE_BUTTON_XPATH));
  }

  public WebElement waitCloseButton() {
    return seleniumWebDriverHelper.waitVisibility(By.xpath(CLOSE_BUTTON_XPATH));
  }

  public void clickOnSaveButton() {
    // leads to saving of the form properties,
    // for real user can't be reproduced because real user does not able to perform so fast click.
    WaitUtils.sleepQuietly(1);

    waitSaveButton().click();
  }

  public void clickOnCancelButton() {
    waitCloseButton().click();
  }

  public boolean isSaveButtonEnabled() {
    final String enablingAttribute = "aria-disabled";
    return seleniumWebDriverHelper
        .waitVisibilityAndGetAttribute(waitSaveButton(), enablingAttribute)
        .equals("false");
  }

  public void waitSaveButtonEnabling() {
    seleniumWebDriverHelper.waitSuccessCondition(driver -> isSaveButtonEnabled());
  }

  public void waitSaveButtonDisabling() {
    seleniumWebDriverHelper.waitSuccessCondition(driver -> !isSaveButtonEnabled());
  }

  public void clickOnCloseIcon() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(CLOSE_ICON_XPATH));
  }

  public void waitForm() {
    testWebElementRenderChecker.waitElementIsRendered(By.xpath(EDIT_MACHINE_FORM_BODY_XPATH));
    waitSaveButton();
    waitCloseButton();
    WaitUtils.sleepQuietly(1);
  }

  public void waitFormInvisibility() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(EDIT_MACHINE_FORM_BODY_XPATH));
  }

  public WebElement waitNameField() {
    return seleniumWebDriverHelper.waitVisibility(By.xpath(NAME_TEXT_FIELD_XPATH));
  }

  public void typeName(String name) {
    // wait until machine name can be set into the input field
    WaitUtils.sleepQuietly(1);

    seleniumWebDriverHelper.setValue(waitNameField(), name);
  }

  public void waitName(String expectedName) {
    seleniumWebDriverHelper.waitValueEqualsTo(waitNameField(), expectedName);
  }

  public boolean isNameHighlightedByRed() {
    String errorHighlightingAttribute = "aria-invalid";
    return seleniumWebDriverHelper
        .waitVisibilityAndGetAttribute(waitNameField(), errorHighlightingAttribute)
        .equals("true");
  }

  public void waitInvalidNameHighlighting() {
    seleniumWebDriverHelper.waitSuccessCondition(driver -> isNameHighlightedByRed());
  }

  public void waitValidNameHighlighting() {
    seleniumWebDriverHelper.waitSuccessCondition(driver -> !isNameHighlightedByRed());
  }

  public void waitRecipeField() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(RECIPE_EDITOR_BODY_XPATH));
  }

  public String getRecipeText() {
    return seleniumWebDriverHelper
        .waitVisibilityOfAllElements(By.xpath(RECIPE_EDITOR_TEXT_XPATH))
        .stream()
        .map(element -> element.getText())
        .collect(Collectors.joining("\n"));
  }

  public void waitRecipeText(String expectedText) {
    seleniumWebDriverHelper.waitNoExceptions(
        () ->
            seleniumWebDriverHelper.waitSuccessCondition(
                driver -> getRecipeText().equals(expectedText)),
        StaleElementReferenceException.class);
  }
}
