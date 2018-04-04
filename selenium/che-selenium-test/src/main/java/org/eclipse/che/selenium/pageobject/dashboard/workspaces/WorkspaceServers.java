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
package org.eclipse.che.selenium.pageobject.dashboard.workspaces;

import static java.lang.String.format;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.pageobject.Loader;
import org.openqa.selenium.By;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WorkspaceServers {
  private final SeleniumWebDriver seleniumWebDriver;
  private final Loader loader;

  @Inject
  public WorkspaceServers(SeleniumWebDriver seleniumWebDriver, Loader loader) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.loader = loader;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private interface Locators {
    String SERVER = "server-name-%s";
    String SERVER_REFERENCE = "//span[@server-reference='%s']";
    String SERVER_PORT = "//div[@id='server-name-%s']//span[@server-port='%s']";
    String SERVER_PROTOCOL = "//div[@id='server-name-%s']//span[@server-protocol='%s']";
    String EDIT_SERVER_BUTTON = "//div[@edit-server='%s']";
    String DELETE_SERVER_BUTTON = "//div[@delete-server='%s']";
    String ADD_SERVER_BUTTON = "//che-button-primary[@che-button-title='Add Server']/button";
    String ADD_NEW_SERVER_DIALOG_NAME = "//md-dialog/che-popup[@title='Add a new server']";
    String ADD_SERVER_REFERENCE_FIELD = "server-reference-input";
    String ADD_SERVER_PORT_FIELD = "server-port-input";
    String ADD_SERVER_PROTOCOL_FIELD = "server-protocol-input";
  }

  public void clickOnAddServerButton() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(Locators.ADD_SERVER_BUTTON)))
        .click();
  }

  public void waitAddServerDialogIsOpen() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(Locators.ADD_NEW_SERVER_DIALOG_NAME)));
  }

  public void enterReference(String name) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.id(Locators.ADD_SERVER_REFERENCE_FIELD)))
        .clear();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.id(Locators.ADD_SERVER_REFERENCE_FIELD)))
        .sendKeys(name);
  }

  public void enterPort(String name) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.id(Locators.ADD_SERVER_PORT_FIELD)))
        .clear();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.id(Locators.ADD_SERVER_PORT_FIELD)))
        .sendKeys(name);
  }

  public void enterProtocol(String protocol) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.id(Locators.ADD_SERVER_PROTOCOL_FIELD)))
        .clear();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.id(Locators.ADD_SERVER_PROTOCOL_FIELD)))
        .sendKeys(protocol);
  }

  public void checkServerName(String serverName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.xpath(format(Locators.SERVER_REFERENCE, serverName))));
  }

  public void checkServerExists(String serverName, String port) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            visibilityOfElementLocated(By.xpath(format(Locators.SERVER_PORT, serverName, port))));
    loader.waitOnClosed();
  }

  public void checkServerIsNotExists(String serverName, String port) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            invisibilityOfElementLocated(By.xpath(format(Locators.SERVER_PORT, serverName, port))));
    loader.waitOnClosed();
  }

  public void clickOnDeleteServerButton(String serverName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            visibilityOfElementLocated(By.xpath(format(Locators.DELETE_SERVER_BUTTON, serverName))))
        .click();
  }

  public void clickOnEditServerButton(String serverName) {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            visibilityOfElementLocated(By.xpath(format(Locators.EDIT_SERVER_BUTTON, serverName))))
        .click();
  }
}
