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

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElementValue;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * The ordinary object which describes dialog window with references to selected item, and contains
 * business logic which allows to find necessary {@link WebElement}, get values from them or compare
 * values from web element to expected values.
 *
 * @author Dmitry Shnurenko
 */
@Singleton
public class ShowReference {

  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public ShowReference(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String MAIN_DIALOG = "//div[@id='gwt-debug-show-reference-dialog']";
    String FQN_TEXT_BOX = MAIN_DIALOG + "//div[text()='Fqn:']//following-sibling::input";
    String PATH_TEXT_BOX = MAIN_DIALOG + "//div[text()='Path:']//following-sibling::input";
  }

  @FindBy(xpath = Locators.FQN_TEXT_BOX)
  WebElement fqnTextBox;

  @FindBy(xpath = Locators.PATH_TEXT_BOX)
  WebElement pathTextBox;

  /**
   * Checks whether value in fqn text box matches to expected value.
   *
   * @param expectedVal expected value
   */
  public void checkFqnFieldValue(String expectedVal) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(textToBePresentInElementValue(fqnTextBox, expectedVal));
  }

  /**
   * Checks whether value in path text box matches to expected value.
   *
   * @param expectedVal expected value
   */
  public void checkPathFieldValue(String expectedVal) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(textToBePresentInElementValue(pathTextBox, expectedVal));
  }
}
