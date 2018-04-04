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
package org.eclipse.che.selenium.pageobject.machineperspective;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.MINIMUM_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Musienko Maxim */
@Singleton
public class CreateMachineWidget {

  private final SeleniumWebDriver seleniumWebDriver;
  private final ActionsFactory actionsFactory;

  @Inject
  public CreateMachineWidget(SeleniumWebDriver seleniumWebDriver, ActionsFactory actionsFactory) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.actionsFactory = actionsFactory;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  public interface DockerImages {
    String UBUNTU_JDK8 = "UBUNTU_JDK8";
    String DEBIAN_JDK8 = "DEBIAN_JDK8";
  }

  private interface Locators {
    String MAIN_FORM_XPATH = "//button[@id='window-create-machine-cancel']/parent::div/parent::div";
    String NAME_FIELD_ID = "gwt-debug-createMachineView-machineName";
    String RECIPE_FIELD_ID = "gwt-debug-createMachineView-recipeURL";
    String FIND_BY_TAG_ID = "gwt-debug-createMachineView-tags";
    String CREATE_BTN_ID = "window-create-machine-create";
    String REPLACE_DEV_MASHINE_ID = "window-create-machine-replace";
    String CANCEL_ID = "window-create-machine-create";
  }

  @FindBy(xpath = Locators.MAIN_FORM_XPATH)
  WebElement mainForm;

  @FindBy(id = Locators.NAME_FIELD_ID)
  WebElement nameOfMashineField;

  @FindBy(id = Locators.RECIPE_FIELD_ID)
  WebElement recipeUrlOfMashine;

  @FindBy(id = Locators.FIND_BY_TAG_ID)
  WebElement findByNAmeField;

  @FindBy(id = Locators.CREATE_BTN_ID)
  WebElement createBtn;

  @FindBy(id = Locators.REPLACE_DEV_MASHINE_ID)
  WebElement replaceBtn;

  @FindBy(id = Locators.CANCEL_ID)
  WebElement cancelBtn;

  /** wait main widget for creating new machine */
  public void waitCreateMachinesWidget() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(mainForm));
  }

  /**
   * type the name of mashine into 'Name' field of the 'Create Machine' widget
   *
   * @param nameOfMachine
   */
  public void typeNameOfMachine(String nameOfMachine) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(nameOfMashineField))
        .sendKeys(nameOfMachine);
    new WebDriverWait(seleniumWebDriver, MINIMUM_SEC)
        .until(
            (WebDriver input) -> {
              return nameOfMashineField.getAttribute("value").equals(nameOfMachine);
            });
  }

  /**
   * type the tag name of mashine into 'Name' field of the 'Create Machine' widget
   *
   * @param tagName
   */
  public void typeFindByTags(String tagName, boolean withClearing) {
    WebElement currentElem =
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
            .until(ExpectedConditions.visibilityOf(findByNAmeField));
    if (withClearing) {
      currentElem.clear();
      // for server side
      WaitUtils.sleepQuietly(1);
      currentElem.sendKeys(tagName);
    } else {
      currentElem.sendKeys(tagName);
    }
  }

  /** wait and click on the create button of 'Create Machine' widget */
  public void clickCreateBTn() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(createBtn))
        .click();
  }

  /** wait and click on the create button of 'Cancel Machine' widget */
  public void clickCancelBtn() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(cancelBtn))
        .click();
  }

  /**
   * select a docker image from dropdown
   *
   * @param dockerImg the docker image into drop dawn
   */
  public void selectDockerImgFromDropDawn(String dockerImg) {
    String locator = "//div[@class='gwt-PopupPanel']//td[text()='" + dockerImg + "']";
    WebElement dropdownItem =
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
            .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locator)));
    actionsFactory.createAction(seleniumWebDriver).doubleClick(dropdownItem).perform();
  }
}
