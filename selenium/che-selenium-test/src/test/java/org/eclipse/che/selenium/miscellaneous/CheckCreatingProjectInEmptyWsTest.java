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
package org.eclipse.che.selenium.miscellaneous;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ImportProjectFromLocation;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class CheckCreatingProjectInEmptyWsTest {

  @Inject private Loader loader;
  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Wizard wizard;
  @Inject private ImportProjectFromLocation importProjectFromLocation;
  @Inject private ActionsFactory actionsFactory;
  @Inject private SeleniumWebDriver seleniumWebDriver;

  @BeforeClass
  public void setUp() throws Exception {
    ide.open(workspace);
  }

  @Test
  public void checkImportAndCreatingFromProjectExplorer() throws InterruptedException {
    ide.waitOpenedWorkspaceIsReadyToUse();
    loader.waitOnClosed();
    projectExplorer.clickOnCreateProjectLink(10);
    wizard.closeWithIcon();
    projectExplorer.clickOnEmptyAreaOfProjectTree(1);
    projectExplorer.clickOnImportProjectLink(1);
    importProjectFromLocation.closeWithIcon();

    projectExplorer.clickOnEmptyAreaOfProjectTree(1);
    try {
      actionsFactory.createAction(seleniumWebDriver).sendKeys(Keys.ALT.toString() + "x").perform();
      wizard.closeWithIcon();
    } catch (org.openqa.selenium.TimeoutException e) {
      actionsFactory.createAction(seleniumWebDriver).sendKeys(Keys.ALT.toString() + "x").perform();
      wizard.closeWithIcon();
    }
    projectExplorer.clickOnEmptyAreaOfProjectTree(1);
    try {
      actionsFactory
          .createAction(seleniumWebDriver)
          .sendKeys(Keys.ALT.toString() + Keys.SHIFT.toString() + "A")
          .perform();
      importProjectFromLocation.closeWithIcon();
    } catch (org.openqa.selenium.TimeoutException e) {
      actionsFactory
          .createAction(seleniumWebDriver)
          .sendKeys(Keys.ALT.toString() + Keys.SHIFT.toString() + "A")
          .perform();
      importProjectFromLocation.closeWithIcon();
    }
  }

  @Test(priority = 1)
  public void checkImportAndCreatingFromEditorPanel() {
    WebDriverWait waitForWebElements = new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC);
    String locatorToEditorContaiPaineId =
        "//div[@id='gwt-debug-editorMultiPartStack-contentPanel']";
    String locatorToImportProjectLnk =
        locatorToEditorContaiPaineId + "//div[text()='Import Project...']";
    String locatorToCreateProjectLnk =
        locatorToEditorContaiPaineId + "//div[text()='Create Project...']";

    loader.waitOnClosed();
    waitForWebElements
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locatorToImportProjectLnk)))
        .click();
    importProjectFromLocation.closeWithIcon();
    waitForWebElements
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locatorToCreateProjectLnk)))
        .click();
    wizard.closeWithIcon();

    waitForWebElements
        .until(
            ExpectedConditions.visibilityOfElementLocated((By.xpath(locatorToEditorContaiPaineId))))
        .click();
    try {
      actionsFactory.createAction(seleniumWebDriver).sendKeys(Keys.ALT.toString() + "x").perform();
      wizard.closeWithIcon();
    } catch (org.openqa.selenium.TimeoutException e) {
      actionsFactory.createAction(seleniumWebDriver).sendKeys(Keys.ALT.toString() + "x").perform();
      wizard.closeWithIcon();
    }
    waitForWebElements
        .until(
            ExpectedConditions.visibilityOf(
                seleniumWebDriver.findElement(By.xpath(locatorToEditorContaiPaineId))))
        .click();
    try {
      actionsFactory
          .createAction(seleniumWebDriver)
          .sendKeys(Keys.ALT.toString() + Keys.SHIFT.toString() + "A")
          .perform();
      importProjectFromLocation.closeWithIcon();
    } catch (org.openqa.selenium.TimeoutException e) {
      actionsFactory
          .createAction(seleniumWebDriver)
          .sendKeys(Keys.ALT.toString() + Keys.SHIFT.toString() + "A")
          .perform();
      importProjectFromLocation.closeWithIcon();
    }
  }
}
