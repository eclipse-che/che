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
package org.eclipse.che.selenium.pageobject.dashboard;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toMap;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.dashboard.AddOrImportForm.Locators.ADD_BUTTON_ID;
import static org.eclipse.che.selenium.pageobject.dashboard.AddOrImportForm.Locators.ADD_OR_CANCEL_BUTTON_XPATH_TEMPLATE;
import static org.eclipse.che.selenium.pageobject.dashboard.AddOrImportForm.Locators.ADD_OR_IMPORT_PROJECT_BUTTON_ID;
import static org.eclipse.che.selenium.pageobject.dashboard.AddOrImportForm.Locators.BLANK_BUTTON_ID;
import static org.eclipse.che.selenium.pageobject.dashboard.AddOrImportForm.Locators.BLANK_FORM_DESCRIPTION_FIELD_XPATH;
import static org.eclipse.che.selenium.pageobject.dashboard.AddOrImportForm.Locators.BLANK_FORM_NAME_ERROR_MESSAGE_XPATH;
import static org.eclipse.che.selenium.pageobject.dashboard.AddOrImportForm.Locators.BLANK_FORM_NAME_FIELD_XPATH;
import static org.eclipse.che.selenium.pageobject.dashboard.AddOrImportForm.Locators.CANCEL_BUTTON_ID;
import static org.eclipse.che.selenium.pageobject.dashboard.AddOrImportForm.Locators.CHECKBOX_BY_SAMPLE_NAME_ID_TEMPLATE;
import static org.eclipse.che.selenium.pageobject.dashboard.AddOrImportForm.Locators.GITHUG_BUTTON_ID;
import static org.eclipse.che.selenium.pageobject.dashboard.AddOrImportForm.Locators.GIT_BUTTON_ID;
import static org.eclipse.che.selenium.pageobject.dashboard.AddOrImportForm.Locators.PROJECT_TAB_XPATH_TEMPLATE;
import static org.eclipse.che.selenium.pageobject.dashboard.AddOrImportForm.Locators.SAMPLES_BUTTON_ID;
import static org.eclipse.che.selenium.pageobject.dashboard.AddOrImportForm.Locators.ZIP_BUTTON_ID;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.core.webdriver.WebDriverWaitFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;

/** @author Ihor Okhrimenko */
@Singleton
public class AddOrImportForm {

  private final SeleniumWebDriver seleniumWebDriver;
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;
  private final WebDriverWaitFactory webDriverWaitFactory;
  private static final int DEFAULT_TIMEOUT = LOAD_PAGE_TIMEOUT_SEC;

  @Inject
  AddOrImportForm(
      SeleniumWebDriver seleniumWebDriver,
      SeleniumWebDriverHelper seleniumWebDriverHelper,
      WebDriverWaitFactory webDriverWaitFactory) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    this.webDriverWaitFactory = webDriverWaitFactory;

    PageFactory.initElements(seleniumWebDriver, this);
  }

  public interface Locators {
    String ADD_OR_IMPORT_PROJECT_BUTTON_ID = "ADD_PROJECT";
    String ADD_OR_CANCEL_BUTTON_XPATH_TEMPLATE = "//*[@id='%s']/button";

    // "Add or Import Project" form buttons
    String SAMPLES_BUTTON_ID = "samples-button";
    String BLANK_BUTTON_ID = "blank-button";
    String GIT_BUTTON_ID = "git-button";
    String GITHUG_BUTTON_ID = "github-button";
    String ZIP_BUTTON_ID = "zip-button";
    String ADD_BUTTON_ID = "add-project-button";
    String CANCEL_BUTTON_ID = "cancel-button";

    String CHECKBOX_BY_SAMPLE_NAME_ID_TEMPLATE = "sample-%s";
    String PROJECT_TAB_XPATH_TEMPLATE = "//toggle-single-button[@id='%s']/div/button/div";
    String PROJECT_TAB_BY_NAME_XPATH_TEMPLATE =
        "//div[@class='%s']//span[text()='console-java-simple']";

    // Blank tab fields
    String BLANK_FORM_NAME_FIELD_XPATH = "//input[@name='name']";
    String BLANK_FORM_DESCRIPTION_FIELD_XPATH = "//input[@name='description']";
    String BLANK_FORM_NAME_ERROR_MESSAGE_XPATH =
        "//div[@id='project-source-selector']//div[@id='new-workspace-error-message']/div";
  }

  public WebElement waitAddOrImportProjectButton(int timeout) {
    return seleniumWebDriverHelper.waitVisibility(By.id(ADD_OR_IMPORT_PROJECT_BUTTON_ID), timeout);
  }

  public WebElement waitAddOrImportProjectButton() {
    return waitAddOrImportProjectButton(DEFAULT_TIMEOUT);
  }

  public void clickOnAddOrImportProjectButton() {
    waitAddOrImportProjectButton().click();
  }

  public void clickOnSamplesButton() {
    seleniumWebDriverHelper.waitAndClick(By.id(SAMPLES_BUTTON_ID));
  }

  public void clickOnBlankButton() {
    seleniumWebDriverHelper.waitAndClick(By.id(BLANK_BUTTON_ID));
  }

  public void clickOnGitButton() {
    seleniumWebDriverHelper.waitAndClick(By.id(GIT_BUTTON_ID));
  }

  public void clickOnGitHubButton() {
    seleniumWebDriverHelper.waitAndClick(By.id(GITHUG_BUTTON_ID));
  }

  public void clickOnZipButton() {
    seleniumWebDriverHelper.waitAndClick(By.id(ZIP_BUTTON_ID));
  }

  public void clickOnAddButton() {
    seleniumWebDriverHelper.waitAndClick(By.id(ADD_BUTTON_ID));
  }

  public void clickOnCancelButton() {
    seleniumWebDriverHelper.waitAndClick(By.id(CANCEL_BUTTON_ID));
  }

  public void waitAddOrImportFormOpened() {
    seleniumWebDriverHelper.waitAllVisibilityBy(
        asList(
            By.id(SAMPLES_BUTTON_ID),
            By.id(BLANK_BUTTON_ID),
            By.id(GIT_BUTTON_ID),
            By.id(GITHUG_BUTTON_ID),
            By.id(ZIP_BUTTON_ID),
            By.id(ADD_BUTTON_ID),
            By.id(CANCEL_BUTTON_ID)));
  }

  public void waitAddOrImportFormClosed() {
    seleniumWebDriverHelper.waitAllInvisibilityBy(
        asList(
            By.id(SAMPLES_BUTTON_ID),
            By.id(BLANK_BUTTON_ID),
            By.id(GIT_BUTTON_ID),
            By.id(GITHUG_BUTTON_ID),
            By.id(ZIP_BUTTON_ID),
            By.id(ADD_BUTTON_ID),
            By.id(CANCEL_BUTTON_ID)));
  }

  private void waitSelectionOfHeaderButton(String buttonId) {
    String locator = format("//che-toggle-joined-button[@id='%s']/button", buttonId);
    seleniumWebDriverHelper.waitAttributeContainsValue(
        By.xpath(locator), "class", "che-toggle-button-enabled");
  }

  public void waitSamplesButtonSelected() {
    waitSelectionOfHeaderButton(SAMPLES_BUTTON_ID);
  }

  public void waitBlankButtonSelected() {
    waitSelectionOfHeaderButton(BLANK_BUTTON_ID);
  }

  public void waitGitButtonSelected() {
    waitSelectionOfHeaderButton(GIT_BUTTON_ID);
  }

  public void waitGitHubButtonSelected() {
    waitSelectionOfHeaderButton(GITHUG_BUTTON_ID);
  }

  public void waitZipButtonSelected() {
    waitSelectionOfHeaderButton(ZIP_BUTTON_ID);
  }

  private void waitButtonDisableState(WebElement button, boolean state) {
    seleniumWebDriverHelper.waitAttributeEqualsTo(button, "aria-disabled", Boolean.toString(state));
  }

  private WebElement waitAddButton() {
    return seleniumWebDriverHelper.waitVisibility(
        By.xpath(format(ADD_OR_CANCEL_BUTTON_XPATH_TEMPLATE, ADD_BUTTON_ID)));
  }

  private WebElement waitCancelButton() {
    return seleniumWebDriverHelper.waitVisibility(
        By.xpath(format(ADD_OR_CANCEL_BUTTON_XPATH_TEMPLATE, CANCEL_BUTTON_ID)));
  }

  public void waitAddButtonDisabled() {
    waitButtonDisableState(waitAddButton(), true);
  }

  public void waitCancelButtonDisabled() {
    waitButtonDisableState(waitCancelButton(), true);
  }

  public void waitAddButtonEnabled() {
    waitButtonDisableState(waitAddButton(), false);
  }

  public void waitCancelButtonEnabled() {
    waitButtonDisableState(waitCancelButton(), false);
  }

  public void waitGitTabOpened() {
    waitGitButtonSelected();
    waitGitUrlField();
  }

  public WebElement waitGitUrlField() {
    return seleniumWebDriverHelper.waitVisibility(waitElementByNameAttribute("remoteGitURL"));
  }

  public void typeToGitUrlField(String text) {
    seleniumWebDriverHelper.setValue(waitGitUrlField(), text);
  }

  public WebElement waitConnectYourGithubAccountButton() {
    return seleniumWebDriverHelper.waitVisibility(
        By.xpath("//che-button-default[@class='import-github-project-button']/button"));
  }

  public void clickConnectYourGithubAccountButton() {
    seleniumWebDriverHelper.waitAndClick(waitConnectYourGithubAccountButton());
  }

  private WebElement waitElementByNameAttribute(String nameAttribute) {
    return seleniumWebDriverHelper.waitVisibility(
        By.xpath(format("//input[@name='%s']", nameAttribute)));
  }

  private void waitTextInTooltip(String expectedText) {
    seleniumWebDriverHelper.waitTextEqualsTo(
        By.xpath("//*[contains(@class, 'tooltip')]"), expectedText);
  }

  public void clickOnProjectTab(String tabName) {
    waitProjectTabAppearance(tabName);
    String locator = format(PROJECT_TAB_XPATH_TEMPLATE, tabName);
    seleniumWebDriverHelper.moveCursorTo(By.xpath(locator));

    try {
      waitTextInTooltip(tabName);
    } catch (TimeoutException ex) {
      // sometimes the tooltip does not display on the CI
    }

    seleniumWebDriverHelper.waitAndClick(By.xpath(locator));
  }

  private List<WebElement> getSamples() {
    return seleniumWebDriverHelper.waitVisibilityOfAllElements(
        By.xpath("//div[@class='add-import-project-sources']//md-item"));
  }

  private String getSampleName(WebElement samplesItem) {
    return asList(seleniumWebDriverHelper.waitVisibilityAndGetText(samplesItem).split("\n")).get(0);
  }

  private String getSampleDescription(WebElement samplesItem) {
    return asList(seleniumWebDriverHelper.waitVisibilityAndGetText(samplesItem).split("\n")).get(1);
  }

  private Map<String, String> getSamplesNamesAndDescriptions() {
    return getSamples()
        .stream()
        .collect(
            toMap(element -> getSampleName(element), element -> getSampleDescription(element)));
  }

  public Set<String> getSamplesNames() {
    return getSamplesNamesAndDescriptions().keySet();
  }

  public String getSampleDescription(String sampleName) {
    return getSamplesNamesAndDescriptions().get(sampleName);
  }

  public void waitSamplesWithDescriptions(Map<String, String> expectedSamplesWithDescriptions) {
    webDriverWaitFactory
        .get()
        .until(
            (ExpectedCondition<Boolean>)
                driver -> expectedSamplesWithDescriptions.equals(getSamplesNamesAndDescriptions()));
  }

  public void clickOnSampleCheckbox(String sampleName) {
    String checkboxId = format(CHECKBOX_BY_SAMPLE_NAME_ID_TEMPLATE, sampleName);

    seleniumWebDriverHelper.waitAndClick(
        By.xpath(format("//*[@id='%s']/md-checkbox/div", checkboxId)));
  }

  private void waitSampleCheckboxState(String sampleName, boolean state) {
    String checkboxId = format(CHECKBOX_BY_SAMPLE_NAME_ID_TEMPLATE, sampleName);
    String locator = format("//div[@id='%s']/md-checkbox", checkboxId);

    seleniumWebDriverHelper.waitAttributeEqualsTo(
        By.xpath(locator), "aria-checked", Boolean.toString(state));
  }

  public void waitSampleCheckboxEnabled(String sampleName) {
    waitSampleCheckboxState(sampleName, true);
  }

  public void waitSampleCheckboxDisabled(String sampleName) {
    waitSampleCheckboxState(sampleName, false);
  }

  public void waitProjectTabAppearance(String tabName) {
    seleniumWebDriverHelper.waitVisibility(By.id(tabName));
  }

  public void waitProjectTabDisappearance(String tabName) {
    seleniumWebDriverHelper.waitInvisibility(By.id(tabName));
  }

  public void waitTextInBlankNameField(String expectedText) {
    seleniumWebDriverHelper.waitAttributeEqualsTo(
        By.xpath(BLANK_FORM_NAME_FIELD_XPATH), "value", expectedText);
  }

  public void typeToBlankNameField(String text) {
    seleniumWebDriverHelper.setValue(By.xpath(BLANK_FORM_NAME_FIELD_XPATH), text);
  }

  public void waitNameFieldErrorMessageInBlankForm(String errorMessage) {
    seleniumWebDriverHelper.waitTextEqualsTo(
        By.xpath(BLANK_FORM_NAME_ERROR_MESSAGE_XPATH), errorMessage);
  }

  public void waitErrorMessageDissappearanceInBlankNameField() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(BLANK_FORM_NAME_ERROR_MESSAGE_XPATH));
  }

  public void waitTextInBlankDescriptionField(String expectedText) {
    seleniumWebDriverHelper.waitAttributeEqualsTo(
        By.xpath(BLANK_FORM_DESCRIPTION_FIELD_XPATH), "value", expectedText);
  }

  public void typeToBlankDescriptionField(String text) {
    seleniumWebDriverHelper.setValue(By.xpath(BLANK_FORM_DESCRIPTION_FIELD_XPATH), text);
  }
}
