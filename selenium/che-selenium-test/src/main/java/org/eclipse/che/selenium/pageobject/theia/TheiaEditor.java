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
import static java.lang.String.join;
import static java.util.Arrays.asList;
import static org.eclipse.che.selenium.pageobject.theia.TheiaEditor.Locators.ACTIVE_LINE_XPATH;
import static org.eclipse.che.selenium.pageobject.theia.TheiaEditor.Locators.EDITOR_LINE_XPATH;
import static org.eclipse.che.selenium.pageobject.theia.TheiaEditor.Locators.EDITOR_TAB_XPATH_TEMPLATE;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

@Singleton
public class TheiaEditor {
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;
  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  private TheiaEditor(
      SeleniumWebDriverHelper seleniumWebDriverHelper, SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    this.seleniumWebDriver = seleniumWebDriver;
  }

  public interface Locators {
    String EDITOR_TAB_XPATH_TEMPLATE = "//ul[@class='p-TabBar-content']//li[@title='%s']";
    String EDITOR_LINE_XPATH =
        "//div[@data-mode-id='plaintext']//span[@class='mtk1']/parent::span/parent::div";
    String ACTIVE_LINE_XPATH =
        "//div[@data-mode-id='plaintext']//div[contains(@class, 'monaco-editor') and contains(@class, 'focused')]";
  }

  public void waitActiveEditor() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(ACTIVE_LINE_XPATH));
  }

  private int getEditorLinePixelCoordinate(WebElement editorLine) {
    String containingCoordinatesAttribute = "style";
    String styleText =
        seleniumWebDriverHelper.waitVisibilityAndGetAttribute(
            editorLine, containingCoordinatesAttribute);

    styleText = styleText.replace("top: ", "");
    styleText = styleText.replace("px; height: ", "\n");
    styleText = styleText.replace("px;", "");

    int lineCoordinate = Integer.parseInt(asList(styleText.split("\n")).get(0));
    return lineCoordinate;
  }

  private String getEditorTabXpath(String tabTitle) {
    return format(EDITOR_TAB_XPATH_TEMPLATE, tabTitle);
  }

  public void waitEditorTab(String tabTitle) {
    seleniumWebDriverHelper.waitVisibility(By.xpath(getEditorTabXpath(tabTitle)));
  }

  public void waitEditorTabDesapearance(String tabTitle) {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(getEditorTabXpath(tabTitle)));
  }

  public void clickOnEditorTab(String tabTitle) {
    seleniumWebDriverHelper.waitAndClick(By.xpath(getEditorTabXpath(tabTitle)));
  }

  public void waitTabSelecting(String tabTitle) {
    final String selectMarkerKeeper = "class";
    final String selectMarker = "theia-mod-active";
    final String editorTabXpath = getEditorTabXpath(tabTitle);
    seleniumWebDriverHelper.waitAttributeContainsValue(
        By.xpath(editorTabXpath), selectMarkerKeeper, selectMarker);
  }

  public void performPasteAction() {
    String keysCombination = Keys.chord(Keys.CONTROL, "v");
    seleniumWebDriverHelper.sendKeys(keysCombination);
  }

  public List<String> getEditorText() {
    List<WebElement> editorLinesElements =
        seleniumWebDriverHelper.waitVisibilityOfAllElements(By.xpath(EDITOR_LINE_XPATH));

    editorLinesElements.sort(
        (firstElement, secondElement) ->
            getEditorLinePixelCoordinate(firstElement)
                - getEditorLinePixelCoordinate(secondElement));

    return editorLinesElements
        .stream()
        .map(element -> seleniumWebDriverHelper.waitVisibilityAndGetText(element))
        .collect(Collectors.toList());
  }

  public String getEditorTextAsString() {
    return join("\n", getEditorText());
  }

  public String getEditorLineText(int lineNumber) {
    final List<String> editorText = getEditorText();

    if (0 == lineNumber) {
      String errorMessage =
          "The \"0\" is uncompatible argument, the lines numeration start from \"1\".";
      throw new ArrayIndexOutOfBoundsException(errorMessage);
    }

    if (lineNumber > editorText.size()) {
      String errorMessage =
          format(
              "The \"%s\" is uncompatible argument, the last available argument is \"%s\"",
              lineNumber, editorText.size());
      throw new ArrayIndexOutOfBoundsException(errorMessage);
    }

    return getEditorText().get(lineNumber - 1);
  }

  public void waitEditorText(String expectedText) {
    seleniumWebDriverHelper.waitSuccessCondition(driver -> isEditorContains(expectedText));
  }

  public boolean isEditorContains(String expectedText) {
    return getEditorTextAsString().contains(expectedText);
  }
}
