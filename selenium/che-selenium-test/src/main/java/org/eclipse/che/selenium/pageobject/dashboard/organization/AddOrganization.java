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
package org.eclipse.che.selenium.pageobject.dashboard.organization;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Sergey Skorik */
@Singleton
public class AddOrganization {

  private interface Locators {
    String ADD_ORGANIZATION_TITLE = "//div[contains(@class, 'che-toolbar-title')]//span";
    String NEW_ORGANIZATION_NAME = "//input[@name='name']";
    String ADD_MEMBER_BUTTON = "//che-button-default[@class = 'che-list-add-button']//button";
    String CREATE_ORGANIZATION_BUTTON =
        "//che-button-primary[@id='create-organization-button']//button";
  }

  private static final String NEW_ORGANIZATION = "Create New Organization";

  private static final String NEW_SUB_ORGANIZATION = "Create Sub-Organization";

  private final WebDriverWait redrawUiElementsTimeout;
  private final SeleniumWebDriver seleniumWebDriver;

  @FindBy(xpath = Locators.ADD_ORGANIZATION_TITLE)
  WebElement addOrganizationTitle;

  @FindBy(xpath = Locators.NEW_ORGANIZATION_NAME)
  WebElement newOrganizationName;

  @FindBy(xpath = Locators.ADD_MEMBER_BUTTON)
  WebElement addMemberButton;

  @FindBy(xpath = Locators.CREATE_ORGANIZATION_BUTTON)
  WebElement createOrganizationButton;

  @Inject
  public AddOrganization(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.redrawUiElementsTimeout =
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
    PageFactory.initElements(seleniumWebDriver, this);
  }

  public void waitAddOrganization() {
    WaitUtils.sleepQuietly(1);
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            (WebDriver driver) ->
                driver
                    .findElement(By.xpath(Locators.ADD_ORGANIZATION_TITLE))
                    .getText()
                    .equals(NEW_ORGANIZATION));
  }

  public void waitAddSubOrganization() {
    redrawUiElementsTimeout.until(
        (WebDriver driver) ->
            driver
                .findElement(By.xpath(Locators.ADD_ORGANIZATION_TITLE))
                .getText()
                .equals(NEW_SUB_ORGANIZATION));
  }

  public void setOrganizationName(String name) {
    redrawUiElementsTimeout.until(ExpectedConditions.visibilityOf(newOrganizationName)).clear();
    redrawUiElementsTimeout
        .until(ExpectedConditions.visibilityOf(newOrganizationName))
        .sendKeys(name);
    redrawUiElementsTimeout.until(
        (WebDriver driver) ->
            driver
                .findElement(By.xpath(Locators.NEW_ORGANIZATION_NAME))
                .getAttribute("value")
                .equals(name));
  }

  public void clickAddMemberButton() {
    redrawUiElementsTimeout.until(ExpectedConditions.visibilityOf(addMemberButton)).click();
  }

  public void clickCreateOrganizationButton() {
    redrawUiElementsTimeout
        .until(ExpectedConditions.visibilityOf(createOrganizationButton))
        .click();
  }

  public boolean checkAddOrganizationButtonEnabled() {
    return redrawUiElementsTimeout
        .until(ExpectedConditions.visibilityOf(createOrganizationButton))
        .isEnabled();
  }

  public void waitAddOrganizationButtonIsNotVisible() {
    redrawUiElementsTimeout.until(
        ExpectedConditions.invisibilityOfElementLocated(
            By.xpath(Locators.CREATE_ORGANIZATION_BUTTON)));
  }
}
