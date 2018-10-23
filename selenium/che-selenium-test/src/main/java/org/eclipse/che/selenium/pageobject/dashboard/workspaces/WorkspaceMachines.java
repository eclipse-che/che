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
package org.eclipse.che.selenium.pageobject.dashboard.workspaces;

import static java.lang.String.format;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.pageobject.Loader;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

@Singleton
public class WorkspaceMachines {
  private final SeleniumWebDriver seleniumWebDriver;
  private final Loader loader;

  @Inject
  public WorkspaceMachines(SeleniumWebDriver seleniumWebDriver, Loader loader) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.loader = loader;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String MACHINE_BUTTON =
        "//che-machine-selector[@content-title='%s']//toggle-single-button[@id = 'workspace-machine-%s']//span";
    String MACHINE_NAME = "//span[@machine-name='%s']";
    String MACHINE_IMAGE = "//span[@machine-image='%s']";
    String EDIT_MACHINE = "//div[@edit-machine='%s']";
    String DELETE_MACHINE = "//div[@delete-machine='%s']";
    String NEW_MACHINE_NAME = "//div[@che-form='editMachineForm']//input";
    String SAVE_MACHINE_BUTTON = "save-machine-button";
    String EDIT_MACHINE_DIALOG_NAME = "//md-dialog/che-popup[@title='Edit the machine']";
    String REMOVE_MACHINE_DIALOG_NAME = "//md-dialog/che-popup[@title='Remove machine']";
    String ADD_MACHINE_DIALOG_NAME = "//md-dialog/che-popup[@title='Add a new machine']";
    String RAM_WORKSPACE = "//input[contains(@name, 'memory')]";
    String WARNING_MSG = "//div[@class='workspace-environments-input']//div[text()='%s']";
    String CONFIG_MACHINE_SWITCH =
        "//div[text()='%s']//following::div[@class='config-dev-machine-switch ng-scope'][1]";
    String ADD_MACHINE_BUTTON = "//che-button-primary[@che-button-title = 'Add machine']/button";
  }

  @FindBy(xpath = Locators.RAM_WORKSPACE)
  WebElement ramWorkspace;

  public void selectMachine(String tabName, String machineName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            visibilityOfElementLocated(
                By.xpath(format(Locators.MACHINE_BUTTON, tabName, machineName))))
        .click();
  }

  /**
   * Check if machine exists in machines list
   *
   * @param machineName the name of machine
   */
  public void checkMachineExists(String machineName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(format(Locators.MACHINE_NAME, machineName))));
    loader.waitOnClosed();
  }

  public void checkMachineIsNotExists(String machineName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(invisibilityOfElementLocated(By.xpath(format(Locators.MACHINE_NAME, machineName))));
    loader.waitOnClosed();
  }

  public void clickOnAddMachineButton() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(Locators.ADD_MACHINE_BUTTON)))
        .click();
  }

  /**
   * Click on the Edit Machine button
   *
   * @param machineName the name of machine
   */
  public void clickOnEditMachineButton(String machineName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(format(Locators.EDIT_MACHINE, machineName))))
        .click();
  }

  /**
   * Click on the Delete Machine button
   *
   * @param machineName the name of machine
   */
  public void clickOnDeleteMachineButton(String machineName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(format(Locators.DELETE_MACHINE, machineName))))
        .click();
  }

  /** Check that the Add New Machine dialog is opened */
  public void checkAddNewMachineDialogIsOpen() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(Locators.ADD_MACHINE_DIALOG_NAME)));
  }

  /** Check that the Edit Machine dialog is opened */
  public void checkEditTheMachineDialogIsOpen() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(Locators.EDIT_MACHINE_DIALOG_NAME)));
  }

  public void setMachineNameInDialog(String machineName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(Locators.NEW_MACHINE_NAME)))
        .clear();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(Locators.NEW_MACHINE_NAME)))
        .sendKeys(machineName);
  }

  /**
   * enter value RAM of workspace
   *
   * @param ram is value of RAM
   */
  public void enterRamWorkspace(int ram) {
    String ramValue = "//input[@name='memory']";
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(ramWorkspace))
        .clear();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(ramWorkspace))
        .sendKeys(String.valueOf(ram));
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(textToBePresentInElementLocated(By.xpath(ramValue), String.valueOf(ram)));
  }

  /**
   * wait the warning message when there is two or more machines
   *
   * @param mess is the message into workspace machine config
   */
  public void waitWarningMessage(String mess) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(format(Locators.WARNING_MSG, mess))));
  }

  /**
   * switch the config machine
   *
   * @param nameMachine is the machine name
   */
  public void switchConfigMachine(String nameMachine) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            visibilityOfElementLocated(
                By.xpath(format(Locators.CONFIG_MACHINE_SWITCH, nameMachine))))
        .click();
  }

  public void clickOnSaveNameDialogButton() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.id(Locators.SAVE_MACHINE_BUTTON)))
        .click();
  }
}
