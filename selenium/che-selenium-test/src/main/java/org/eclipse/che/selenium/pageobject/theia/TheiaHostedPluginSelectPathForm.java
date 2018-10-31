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

import static java.lang.String.format;
import static org.eclipse.che.selenium.pageobject.theia.TheiaHostedPluginSelectPathForm.Locators.CANCEL_BUTTON_XPATH;
import static org.eclipse.che.selenium.pageobject.theia.TheiaHostedPluginSelectPathForm.Locators.CLOSE_ICON_XPATH;
import static org.eclipse.che.selenium.pageobject.theia.TheiaHostedPluginSelectPathForm.Locators.DROP_DOWN_SUGGESTIONS_XPATH;
import static org.eclipse.che.selenium.pageobject.theia.TheiaHostedPluginSelectPathForm.Locators.DROP_DOWN_TITLE_XPATH;
import static org.eclipse.che.selenium.pageobject.theia.TheiaHostedPluginSelectPathForm.Locators.OPEN_BUTTON_XPATH;
import static org.eclipse.che.selenium.pageobject.theia.TheiaHostedPluginSelectPathForm.Locators.PROJECT_TREE_ITEM_ID_TEMPLATE;
import static org.eclipse.che.selenium.pageobject.theia.TheiaHostedPluginSelectPathForm.Locators.ROOT_PROJECTS_FOLDER_XPATH;
import static org.eclipse.che.selenium.pageobject.theia.TheiaHostedPluginSelectPathForm.Locators.SELECTED_ITEM_XPATH_TEMPLATE;
import static org.eclipse.che.selenium.pageobject.theia.TheiaHostedPluginSelectPathForm.Locators.SUGGESTION_XPATH_TEMPLATE;
import static org.eclipse.che.selenium.pageobject.theia.TheiaHostedPluginSelectPathForm.Locators.TITLE_XPATH;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

@Singleton
public class TheiaHostedPluginSelectPathForm {
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;

  @Inject
  TheiaHostedPluginSelectPathForm(SeleniumWebDriverHelper seleniumWebDriverHelper) {
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
  }

  public interface Locators {
    String TITLE_XPATH =
        "//div[@class='dialogBlock']//div[@class='dialogTitle']//div[text()='Hosted Plugin: Select Path']";
    String CLOSE_ICON_XPATH =
        "//div[@class='dialogBlock']//div[@class='dialogTitle']//i[contains(@class, 'closeButton')]";
    String DROP_DOWN_TITLE_XPATH =
        "//div[@class='dialogBlock']//select[@class='theia-LocationList']";
    String DROP_DOWN_SUGGESTIONS_XPATH = DROP_DOWN_TITLE_XPATH + "//option";
    String SUGGESTION_XPATH_TEMPLATE =
        "(//div[@class='dialogBlock']//select[@class='theia-LocationList']//option)[%s]";

    String ROOT_PROJECTS_FOLDER_XPATH =
        "//div[@class='dialogBlock']//div[@class='theia-TreeNodeContent']//div[@id='/projects']";
    String PROJECT_TREE_ITEM_ID_TEMPLATE = "/projects/%s";
    String SELECTED_ITEM_XPATH_TEMPLATE =
        "//div[@data-node-id='/projects/%s']/parent::div/parent::div[contains(@class, 'theia-mod-selected') and contains(@class, 'theia-mod-focus')]";
    String CANCEL_BUTTON_XPATH = "//div[@class='dialogBlock']//button[text()='Cancel']";
    String OPEN_BUTTON_XPATH = "//div[@class='dialogBlock']//button[text()='Open']";
  }

  private String getSelectedProjectTreeItemXpath(String itemPath) {
    return format(SELECTED_ITEM_XPATH_TEMPLATE, itemPath);
  }

  private String getProjectsTreeItemId(String itemPath) {
    return format(PROJECT_TREE_ITEM_ID_TEMPLATE, itemPath);
  }

  private String getDropDownSuggestionXpath(int suggestionIndex) {
    final int adoptedSuggestionIndex = suggestionIndex + 1;
    return format(SUGGESTION_XPATH_TEMPLATE, adoptedSuggestionIndex);
  }

  public void waitTitle() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(TITLE_XPATH));
  }

  public void waitTitleDissappearance() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(TITLE_XPATH));
  }

  public void waitCloseIcon() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(CLOSE_ICON_XPATH));
  }

  public void clickOnCloseIcon() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(CLOSE_ICON_XPATH));
  }

  public String getDropDownTitleText() {
    return seleniumWebDriverHelper.waitVisibilityAndGetValue(By.xpath(DROP_DOWN_TITLE_XPATH));
  }

  public void waitSuggestionsTitleText(String expectedText) {
    seleniumWebDriverHelper.waitValueEqualsTo(By.xpath(DROP_DOWN_TITLE_XPATH), expectedText);
  }

  private int getDropDownSuggestionsCount() {
    return seleniumWebDriverHelper
        .waitVisibilityOfAllElements(By.xpath(DROP_DOWN_SUGGESTIONS_XPATH))
        .size();
  }

  public List<String> getSuggestions() {
    // realized by "for" loop for avoiding the "StaleElementReferenceException".
    // in this case each element will be found before text extracting
    final int suggestionsCount = getDropDownSuggestionsCount();
    List<String> result = new ArrayList<>();

    for (int i = 0; i < suggestionsCount; i++) {
      String suggestionXpath = getDropDownSuggestionXpath(i);
      String suggestionText =
          seleniumWebDriverHelper.waitVisibilityAndGetText(By.xpath(suggestionXpath));
      result.add(suggestionText);
    }

    return result;
  }

  public String getDropDownSuggestion(int suggestionIndex) {
    final String suggestionXpath = getDropDownSuggestionXpath(suggestionIndex);

    return seleniumWebDriverHelper.waitVisibilityAndGetText(By.xpath(suggestionXpath));
  }

  private int getDropDownSuggestionIndex(String suggestionText) {
    return getSuggestions().indexOf(suggestionText);
  }

  public void clickOnDropDownSuggestion(int suggestionIndex) {
    final String suggestionXpath = getDropDownSuggestionXpath(suggestionIndex);

    seleniumWebDriverHelper.waitAndClick(By.xpath(suggestionXpath));
  }

  public void clickOnDropDownSuggestion(String suggestionText) {
    final int suggestionIndex = getDropDownSuggestionIndex(suggestionText);

    clickOnDropDownSuggestion(suggestionIndex);
  }

  public void waitProjectsRootFolder() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(ROOT_PROJECTS_FOLDER_XPATH));
  }

  public void doubleClickOnProjectsRootFolder() {
    seleniumWebDriverHelper.moveCursorToAndDoubleClick(By.xpath(ROOT_PROJECTS_FOLDER_XPATH));
  }

  public void clickOnItem(String itemPath) {
    waitItem(itemPath).click();
  }

  public WebElement waitItem(String itemPath) {
    final String itemId = getProjectsTreeItemId(itemPath);

    return seleniumWebDriverHelper.waitVisibility(By.id(itemId));
  }

  public void waitItemDisappearance(String itemPath) {
    final String itemId = getProjectsTreeItemId(itemPath);

    seleniumWebDriverHelper.waitInvisibility(By.id(itemId));
  }

  public void waitItemSelected(String itemPath) {
    final String selectedItemXpath = getSelectedProjectTreeItemXpath(itemPath);

    seleniumWebDriverHelper.waitVisibility(By.xpath(selectedItemXpath));
  }

  public void pressEnter() {
    seleniumWebDriverHelper.pressEnter();
  }

  public void pressArrowDown() {
    seleniumWebDriverHelper.pressArrowDown();
  }

  public void pressArrowUp() {
    seleniumWebDriverHelper.pressArrowUp();
  }

  public void pressArrowLeft() {
    seleniumWebDriverHelper.pressArrowLeft();
  }

  public void pressArrowRight() {
    seleniumWebDriverHelper.pressArrowRight();
  }

  public void waitCancelButton() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(CANCEL_BUTTON_XPATH));
  }

  public void waitOpenButton() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(OPEN_BUTTON_XPATH));
  }

  public void clickOnCancelButton() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(CANCEL_BUTTON_XPATH));
  }

  public void clickOnOpenButton() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(OPEN_BUTTON_XPATH));
  }

  public void waitForm() {
    waitTitle();
    waitCloseIcon();
    waitCancelButton();
    waitOpenButton();
  }

  public void waitFormClosed() {
    waitTitleDissappearance();
  }
}
