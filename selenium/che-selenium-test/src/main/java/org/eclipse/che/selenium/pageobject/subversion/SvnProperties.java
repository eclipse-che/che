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
package org.eclipse.che.selenium.pageobject.subversion;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * @author Aleksandr Shmaraev
 * @author Andrey Chizhikov
 */
@Singleton
public class SvnProperties {

  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public SvnProperties(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String PROPERTIES_FORM = "gwt-debug-svn-property-edit-window";
    String PROPERTY_INPUT = "//*[text()='Property:']/following::input[1]";
    String FUTURE_PROPERTY_VALUE_TEXTAREA = "//*[text()='Set value to:']/following::textarea[1]";
    String OK_BUTTON = "svn-property-edit-ok";
    String CANCEL_BUTTON = "svn-property-edit-cancel";
  }

  @FindBy(id = Locators.PROPERTIES_FORM)
  WebElement propertiesForm;

  @FindBy(xpath = Locators.PROPERTY_INPUT)
  WebElement propertyInput;

  @FindBy(xpath = Locators.FUTURE_PROPERTY_VALUE_TEXTAREA)
  WebElement futurePropertyValueTextarea;

  @FindBy(id = Locators.OK_BUTTON)
  WebElement okButton;

  public void waitPropertyFormOpened() {
    new WebDriverWait(seleniumWebDriver, 20).until(ExpectedConditions.visibilityOf(propertiesForm));
  }

  public void waitPropertyFormClosed() {
    new WebDriverWait(seleniumWebDriver, 20)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.id(Locators.PROPERTIES_FORM)));
  }

  public void typeProperty(String property) {
    propertyInput.clear();
    propertyInput.sendKeys(property);
  }

  public void setPropertyValue(String value) {
    futurePropertyValueTextarea.clear();
    futurePropertyValueTextarea.sendKeys(value);
  }

  public void clickOkButton() {
    okButton.click();
  }
}
