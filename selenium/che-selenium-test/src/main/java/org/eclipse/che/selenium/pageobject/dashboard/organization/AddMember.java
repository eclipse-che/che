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

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.common.base.Function;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Sergey Skorik */
@Singleton
public class AddMember {

  private interface Locators {
    String ADD_MEMBER_TITLE_XPATH =
        "//div[contains(@class, 'che-popup')]//div[text() = 'Invite member to collaborate']";
    String ADD_MEMBER_WIDGET_XPATH = ADD_MEMBER_TITLE_XPATH + "/ancestor::che-popup";
    String CLOSE_WIDGET_ICON_XPATH = "//div[@ng-click = 'onClose()']";
    String MEMBERS_EMAIL_INPUT_XPATH = "//input[@name = 'email']";
    String MEMBER_RADIO_BUTTON_XPATH =
        "//md-radio-button//span[text()='Member' or text()='Team Developer']/ancestor::md-radio-button";
    String ADMIN_RADIO_BUTTON_XPATH =
        "//md-radio-button//span[text()='Admin' or text()='Team Admin']/ancestor::md-radio-button";
    String ADD_MEMBER_BUTTON_XPATH = "//che-button-primary[@che-button-title = 'Add']//button";
    String CANCEL_BUTTON_XPATH = "//che-button-cancel-flat//button";
    String SAVE_BUTTON_XPATH = "//che-button-primary[@che-button-title =  'Save']/button";
    String DELETE_BUTTON = "//che-button-primary[@che-button-title =  'Delete']/button";
    String EDIT_PERMISSIONS_BUTTON =
        "//span[text() = '%s']/ancestor::div[2]//div//a[@uib-tooltip =  'Edit developer permissions']";
  }

  private static final String TITLE = "Invite member to collaborate";

  @FindBy(xpath = Locators.ADD_MEMBER_TITLE_XPATH)
  WebElement addMemberTitle;

  @FindBy(xpath = Locators.CLOSE_WIDGET_ICON_XPATH)
  WebElement closeWidget;

  @FindBy(xpath = Locators.MEMBERS_EMAIL_INPUT_XPATH)
  WebElement membersEmail;

  @FindBy(xpath = Locators.MEMBER_RADIO_BUTTON_XPATH)
  WebElement memberRadioButton;

  @FindBy(xpath = Locators.ADMIN_RADIO_BUTTON_XPATH)
  WebElement adminRadioButton;

  @FindBy(xpath = Locators.ADD_MEMBER_BUTTON_XPATH)
  WebElement addMemberButton;

  @FindBy(xpath = Locators.CANCEL_BUTTON_XPATH)
  WebElement cancelButton;

  @FindBy(xpath = Locators.SAVE_BUTTON_XPATH)
  WebElement saveButton;

  @FindBy(xpath = Locators.DELETE_BUTTON)
  WebElement deleteButton;

  @FindBy(xpath = Locators.EDIT_PERMISSIONS_BUTTON)
  WebElement editPermissionsButton;

  private final SeleniumWebDriver seleniumWebDriver;
  private final WebDriverWait redrawUiElementsTimeout;

  @Inject
  public AddMember(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.redrawUiElementsTimeout =
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
    PageFactory.initElements(seleniumWebDriver, this);
  }

  public void waitAddMemberWidget() {
    this.waitForPopupAppearence(Locators.ADD_MEMBER_WIDGET_XPATH);
  }

  public void setMembersEmail(String email) {
    redrawUiElementsTimeout.until(ExpectedConditions.visibilityOf(membersEmail)).clear();
    redrawUiElementsTimeout.until(ExpectedConditions.visibilityOf(membersEmail)).sendKeys(email);
    redrawUiElementsTimeout
        .until(ExpectedConditions.visibilityOf(membersEmail))
        .getText()
        .equals(email);
  }

  public void onlySetMembersEmail(String email) {
    redrawUiElementsTimeout.until(ExpectedConditions.visibilityOf(membersEmail)).clear();
    redrawUiElementsTimeout.until(ExpectedConditions.visibilityOf(membersEmail)).sendKeys(email);
  }

  public WebElement getMembersEmailElement() {
    return membersEmail;
  }

  public String getMembersEmailValue() {
    return membersEmail.getAttribute("value");
  }

  public void clickMemberButton() {
    redrawUiElementsTimeout.until(ExpectedConditions.visibilityOf(memberRadioButton)).click();
  }

  public void clickAdminButton() {
    redrawUiElementsTimeout.until(ExpectedConditions.visibilityOf(adminRadioButton)).click();
  }

  public void waitForWidgetClosed() {
    this.waitForElementDisappearance(Locators.ADD_MEMBER_TITLE_XPATH);
  }

  public void closeWidget() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(closeWidget))
        .click();
  }

  public boolean checkAddButtonIsEnabled() {
    return redrawUiElementsTimeout
        .until(ExpectedConditions.visibilityOf(addMemberButton))
        .isEnabled();
  }

  public void clickAddButton() {
    redrawUiElementsTimeout.until(ExpectedConditions.visibilityOf(addMemberButton)).click();
    WaitUtils.sleepQuietly(1);
  }

  public void clickCancelButton() {
    redrawUiElementsTimeout.until(ExpectedConditions.visibilityOf(cancelButton)).click();
  }

  public void clickSaveButton() {
    redrawUiElementsTimeout.until(ExpectedConditions.visibilityOf(saveButton)).click();
  }

  public void clickDeleteButton() {
    redrawUiElementsTimeout.until(ExpectedConditions.visibilityOf(saveButton)).click();
  }

  public void clickEditPermissionsButton(String email) {
    redrawUiElementsTimeout
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format(Locators.EDIT_PERMISSIONS_BUTTON, email))))
        .click();
  }

  private void waitForElementDisappearance(String xpath) {
    FluentWait<WebDriver> wait =
        new FluentWait<WebDriver>(seleniumWebDriver)
            .withTimeout(REDRAW_UI_ELEMENTS_TIMEOUT_SEC, TimeUnit.SECONDS)
            .pollingEvery(200, TimeUnit.MILLISECONDS)
            .ignoring(StaleElementReferenceException.class);

    wait.until(
        (Function<WebDriver, Boolean>)
            driver -> {
              List<WebElement> elements = seleniumWebDriver.findElements(By.xpath(xpath));
              return elements.isEmpty() || elements.get(0).isDisplayed();
            });
  }

  /**
   * Wait for popup is attached to DOM and animation ends.
   *
   * @param xpath xpath to match the 'che-popup' element
   */
  private void waitForPopupAppearence(String xpath) {
    FluentWait<WebDriver> wait =
        new FluentWait<WebDriver>(seleniumWebDriver)
            .withTimeout(REDRAW_UI_ELEMENTS_TIMEOUT_SEC, TimeUnit.SECONDS)
            .pollingEvery(200, TimeUnit.MILLISECONDS)
            .ignoring(StaleElementReferenceException.class);

    Map<String, Integer> lastSize = new HashMap<>();
    lastSize.put("height", 0);
    lastSize.put("width", 0);

    wait.until(
        (Function<WebDriver, Boolean>)
            driver -> {
              List<WebElement> elements = seleniumWebDriver.findElements(By.xpath(xpath));
              if (elements.isEmpty()) {
                return false;
              }

              Dimension size = elements.get(0).getSize();
              if (lastSize.get("height") < size.getHeight()
                  || lastSize.get("width") < size.getHeight()) {
                lastSize.put("height", size.getHeight());
                lastSize.put("width", size.getWidth());

                return false;
              }

              return true;
            });
  }
}
