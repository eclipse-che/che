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
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOADER_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.WIDGET_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.theia.TheiaEditor.Locators.ACTIVE_LINE_XPATH;
import static org.eclipse.che.selenium.pageobject.theia.TheiaEditor.Locators.EDITOR_LINE_BY_INDEX_XPATH_TEMPLATE;
import static org.eclipse.che.selenium.pageobject.theia.TheiaEditor.Locators.EDITOR_LINE_BY_PIXEL_COORDINATES_XPATH_TEMPLATE;
import static org.eclipse.che.selenium.pageobject.theia.TheiaEditor.Locators.EDITOR_LINE_XPATH;
import static org.eclipse.che.selenium.pageobject.theia.TheiaEditor.Locators.EDITOR_TAB_XPATH_TEMPLATE;
import static org.eclipse.che.selenium.pageobject.theia.TheiaEditor.Locators.TAB_CLOSE_BUTTON_XPATH_TEMPLATE;
import static org.openqa.selenium.Keys.CONTROL;
import static org.openqa.selenium.Keys.END;
import static org.openqa.selenium.Keys.ESCAPE;
import static org.openqa.selenium.Keys.SHIFT;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;

@Singleton
public class TheiaEditor {
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;
  private final SeleniumWebDriver seleniumWebDriver;
  private final TheiaIde theiaIde;
  private final TheiaProposalForm theiaProposalForm;

  @Inject
  private TheiaEditor(
      SeleniumWebDriverHelper seleniumWebDriverHelper,
      SeleniumWebDriver seleniumWebDriver,
      TheiaIde theiaIde,
      TheiaProposalForm theiaProposalForm) {
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    this.seleniumWebDriver = seleniumWebDriver;
    this.theiaIde = theiaIde;
    this.theiaProposalForm = theiaProposalForm;
  }

  public interface Locators {
    String EDITOR_TAB_XPATH_TEMPLATE =
        "//ul[@class='p-TabBar-content']//li[contains(@title, '%s')]";
    String EDITOR_LINE_XPATH = "//div[@data-mode-id]//div[@class='view-line']";
    String ACTIVE_LINE_XPATH =
        "//div[contains(@class, 'monaco-scrollable-element')]//div[contains(@class, 'monaco-editor-background')]//div[contains(@class, 'focused')]";
    String EDITOR_LINE_BY_INDEX_XPATH_TEMPLATE = "(" + EDITOR_LINE_XPATH + ")[%s]";
    String EDITOR_LINE_BY_PIXEL_COORDINATES_XPATH_TEMPLATE =
        "//div[@data-mode-id]//div[@class='view-line' and contains(@style, 'top:%spx;')]";
    String TAB_CLOSE_BUTTON_XPATH_TEMPLATE =
        "//li[contains(@title, '%s')]//div[@class='p-TabBar-tabCloseIcon']";
  }

  private String getCloseButtonXpath(String tabTitle) {
    return format(TAB_CLOSE_BUTTON_XPATH_TEMPLATE, tabTitle);
  }

  public void clickOnTabCloseButton(String tabTitle) {
    final String tabCloseButtonXpath = getCloseButtonXpath(tabTitle);

    seleniumWebDriverHelper.waitAndClick(By.xpath(tabCloseButtonXpath));
  }

  public boolean isTabWithSavedStatus(String tabTitle) {
    final String backgroundImageProperty = "background-image";
    final String savedBackgroundImageUrl = "url(\"data:image/svg+xml;base64,PHN2ZyBmaWxsPSI";

    return seleniumWebDriverHelper
        .waitAndGetCssValue(By.xpath(getCloseButtonXpath(tabTitle)), backgroundImageProperty)
        .contains(savedBackgroundImageUrl);
  }

  public void waitTabSavedStatus(String tabTitle) {
    seleniumWebDriverHelper.waitSuccessCondition(driver -> isTabWithSavedStatus(tabTitle));
  }

  public void waitActiveEditor() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(ACTIVE_LINE_XPATH));
  }

  private int getEditorLinePixelCoordinate(String editorLineXpath) {
    String styleText = getEditorLineStyleAttribute(editorLineXpath);

    styleText = styleText.replace("top: ", "");
    styleText = styleText.replace("px; height: ", "\n");
    styleText = styleText.replace("px;", "");

    int lineCoordinate = Integer.parseInt(asList(styleText.split("\n")).get(0));
    return lineCoordinate;
  }

  private String getEditorLineStyleAttribute(String editorLineXpath) {
    final String containingCoordinatesAttribute = "style";

    return seleniumWebDriverHelper.waitVisibilityAndGetAttribute(
        By.xpath(editorLineXpath), containingCoordinatesAttribute, ELEMENT_TIMEOUT_SEC);
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
    // In this realization each line element will be found exactly before using
    // This realization allows us to avoid "StaleElementReferenceException"
    final int editorLinesCount = getLinesCount();

    List<Integer> linePixelCoordinates = getEditorLinePixelCoordinates(editorLinesCount);

    List<String> linesText =
        linePixelCoordinates
            .stream()
            .map(
                linePixelCoordinate -> {
                  String lineByPixelCoordinatesXpath =
                      getEditorLineByPixelCoordinateXpath(linePixelCoordinate);
                  return seleniumWebDriverHelper.waitVisibilityAndGetText(
                      By.xpath(lineByPixelCoordinatesXpath), ELEMENT_TIMEOUT_SEC);
                })
            .collect(Collectors.toList());

    return linesText;
  }

  private int getLinesCount() {
    AtomicInteger linesCount = new AtomicInteger(0);

    seleniumWebDriverHelper.waitNoExceptions(
        () ->
            linesCount.set(
                seleniumWebDriverHelper
                    .waitVisibilityOfAllElements(By.xpath(EDITOR_LINE_XPATH), ELEMENT_TIMEOUT_SEC)
                    .size()),
        WIDGET_TIMEOUT_SEC,
        StaleElementReferenceException.class);

    if (linesCount.get() == 0) {
      throw new RuntimeException("The lines count is \"0\". Lines count can't be less than 1");
    }

    return linesCount.get();
  }

  private String getEditorLineByPixelCoordinateXpath(int pixelCoordinate) {
    return format(EDITOR_LINE_BY_PIXEL_COORDINATES_XPATH_TEMPLATE, pixelCoordinate);
  }

  private String getEditorLineByIndexXpath(int lineIndex) {
    return format(EDITOR_LINE_BY_INDEX_XPATH_TEMPLATE, lineIndex);
  }

  private List<Integer> getEditorLinePixelCoordinates(int editorLinesCount) {
    List<Integer> result = new ArrayList<>();

    for (int i = 1; i <= editorLinesCount; i++) {
      String editorLineXpath = getEditorLineByIndexXpath(i);
      int linePixelCoordinate = getEditorLinePixelCoordinate(editorLineXpath);
      result.add(linePixelCoordinate);
    }

    // should be sorted by coordinates because found lines may be mixed by indexes
    // and don't match with their expected places
    result.sort((first, second) -> first - second);
    return result;
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
    seleniumWebDriverHelper.waitSuccessCondition(
        driver -> isEditorContains(expectedText), LOADER_TIMEOUT_SEC);
  }

  public boolean isEditorContains(String expectedText) {
    return getEditorTextAsString().contains(expectedText);
  }

  public void placeCursorToLine(int lineNumber) {
    final String convertedLineNumber = Integer.toString(lineNumber);

    theiaIde.pressKeyCombination(CONTROL, "g");

    theiaProposalForm.waitForm();
    theiaProposalForm.enterTextToSearchField(convertedLineNumber);
    theiaProposalForm.pressEnter();

    theiaProposalForm.waitFormDisappearance();
    waitActiveEditor();
    waitLineSelected(lineNumber);
  }

  public boolean isLineSelected(int lineNumber) {
    final String borderColorCssAttribute = "border-top-color";
    final String selectedLineBorderColor = "rgba(198, 198, 198, 1)";
    final String selectedLineXpathTemplate =
        "//div[contains(@class, 'line-numbers') and text()='%s']";
    final String selectedLineXpath = format(selectedLineXpathTemplate, lineNumber);

    return seleniumWebDriverHelper
        .waitAndGetCssValue(By.xpath(selectedLineXpath), borderColorCssAttribute)
        .equals(selectedLineBorderColor);
  }

  public void waitLineSelected(int lineNumber) {
    seleniumWebDriverHelper.waitSuccessCondition(driver -> isLineSelected(lineNumber));
  }

  public void selectLineText(int lineNumber) {
    placeCursorToLine(lineNumber);

    theiaIde.pressKeyCombination(SHIFT, END);

    // waits selection logic end
    WaitUtils.sleepQuietly(1);
  }

  public void enterText(String text) {
    seleniumWebDriverHelper.sendKeys(text);
  }

  public void enterTextByTypingEachChar(String text) {
    for (char character : text.toCharArray()) {
      // for avoiding unexpected autocomplete during typing
      seleniumWebDriverHelper.sendKeys(Character.toString(character));
      seleniumWebDriverHelper.sendKeys(ESCAPE.toString());
    }
  }
}
