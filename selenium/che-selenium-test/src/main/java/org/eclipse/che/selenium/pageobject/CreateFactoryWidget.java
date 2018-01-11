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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Arrays;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Musienko Maxim */
@Singleton
public class CreateFactoryWidget {
  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public CreateFactoryWidget(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {

    // TODO  replace the Xpathes on ids and use it
    String CLOSE_BTN_ID = "gwt-debug-projectReadOnlyGitUrl-btnClose";
    String FACTORY_FIELD_XPATH = "//div[text()='Factory']/parent::div//input[@readonly]";
    String FACTORY_NAME_FIELD = "//input[@placeholder='new-factory-name']";
    String CREATE_BTN =
        "//input[@placeholder='new-factory-name']/following-sibling::button[text()='Create']";
    String CONFIGURE_FACTORY_BTN = "//*[local-name() = 'path' and @d[contains(.,'L16,6.864 Z')]]";
    String INVOKE_FACTORY_BTN =
        "//*[local-name() = 'g' and @class='changeOnHover greenColored']/ancestor-or-self::a";
  }

  @FindBy(xpath = Locators.INVOKE_FACTORY_BTN)
  WebElement invokeFactoryBtn;

  @FindBy(xpath = Locators.CONFIGURE_FACTORY_BTN)
  WebElement configureFactoryBtn;

  @FindBy(xpath = Locators.CREATE_BTN)
  WebElement createFactoryBtn;

  @FindBy(xpath = Locators.FACTORY_NAME_FIELD)
  WebElement nameFactoryField;

  @FindBy(xpath = Locators.FACTORY_FIELD_XPATH)
  WebElement factoryField;

  @FindBy(id = Locators.CLOSE_BTN_ID)
  WebElement closeBtnID;

  /** wait opening the widget */
  public void waitOpen() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            ExpectedConditions.visibilityOfAllElements(
                Arrays.asList(
                    invokeFactoryBtn,
                    configureFactoryBtn,
                    createFactoryBtn,
                    nameFactoryField,
                    factoryField,
                    closeBtnID)));
  }

  /**
   * wait appearance text into factory field text
   *
   * @param expectedText
   */
  public void waitTextIntoFactoryField(String expectedText) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.textToBePresentInElementValue(factoryField, expectedText));
  }

  /** wait the widget and click on create factory btn */
  public void clickOnCreateBtn() {
    waitOpen();
    createFactoryBtn.click();
  }

  /**
   * set a name for factory
   *
   * @param name
   */
  public void typeNameFactory(String name) {
    waitOpen();
    nameFactoryField.sendKeys(name);
  }

  /** click on invoke button */
  public void clickOnInvokeBtn() {
    waitOpen();
    try {
      invokeFactoryBtn.click();
    } catch (WebDriverException ex) {
      new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
          .until(ExpectedConditions.elementToBeClickable(invokeFactoryBtn))
          .click();
    }
  }
}
