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
package org.eclipse.che.selenium.pageobject;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.MINIMUM_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * @author Mykola Morhun
 * @author Andrey Chizhikov
 */
@Singleton
public class KeyBindings {

  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public KeyBindings(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String KEY_BINDINGS_FORM = "//table[@title='Key Bindings']";
    String OK_BUTTON_ID = "keybindings-saveButton-btn";
    String ID_KEY_BY_KEY_BINDINGS = "gwt-debug-projectWizard";
    String SEARCH_INPUT = "//input[@placeholder='Search']";
  }

  @FindBy(xpath = Locators.KEY_BINDINGS_FORM)
  WebElement keyBindingsForm;

  @FindBy(id = Locators.OK_BUTTON_ID)
  WebElement okButton;

  /** Waits the 'key bindings form' is open */
  public void waitKeyBindingsFormIsOpened() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(keyBindingsForm));
  }

  /** Clicks on the 'OK' button */
  public void clickOkButton() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.elementToBeClickable(okButton))
        .click();
  }

  /**
   * Waits the 'key bindings form' is loaded and checked text in 'key bindings' lines
   *
   * @param keyBindingsList is expected list binding keys
   */
  public boolean checkAvailabilityAllKeyBindings(List<String> keyBindingsList) {

    List<String> itemsFromWidget =
        getListElementsKeyBindingById(Locators.ID_KEY_BY_KEY_BINDINGS)
            .stream()
            .map(WebElement::getText)
            .map(e -> e.replace("\n", " "))
            .collect(Collectors.toList());
    return itemsFromWidget.containsAll(keyBindingsList);
  }

  /**
   * Waits the 'key bindings form' is loaded and checked search result
   *
   * @param keyBinding is name or part of name binding key
   * @param result is expected number of matches
   */
  public void checkSearchResultKeyBinding(String keyBinding, int result) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(Locators.SEARCH_INPUT)))
        .sendKeys(keyBinding);
    List<WebElement> webElementList =
        getListElementsKeyBindingById(Locators.ID_KEY_BY_KEY_BINDINGS);
    new WebDriverWait(seleniumWebDriver, MINIMUM_SEC)
        .until((WebDriver webDriver) -> webElementList.size() == result);
  }

  /**
   * Waits the tag 'body' is loaded and enter key combination
   *
   * @param key is key combination
   */
  public void enterKeyCombination(Keys... key) {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")))
        .sendKeys(Keys.chord(key));
  }

  private List<WebElement> getListElementsKeyBindingById(String id) {
    waitKeyBindingsFormIsOpened();
    return new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.xpath("//div[contains(@id,'" + id + "')]")));
  }
}
