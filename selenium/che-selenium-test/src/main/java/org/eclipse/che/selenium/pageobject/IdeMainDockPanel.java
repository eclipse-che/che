/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.pageobject;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** @author Andrienko Alexander on 10.11.14. */
@Singleton
public class IdeMainDockPanel {

  private final SeleniumWebDriver seleniumWebDriver;

  @Inject
  public IdeMainDockPanel(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private static final String CREATE_ICON_PREFIX = "toolbar/";

  private interface Locators {
    String CREATE_ICON_ID = "gwt-debug-PopupButton/New";
    String DELETE_ICON_ENABLED_ID = "gwt-debug-ActionButton/deleteItem-true";
  }

  public interface CreateMenuCommand {
    String FILE = CREATE_ICON_PREFIX + "File";
    String FOLDER = CREATE_ICON_PREFIX + "Folder";
    String PROJECT = CREATE_ICON_PREFIX + "Project...";
    String HTML_FILE = CREATE_ICON_PREFIX + "HTML File";
    String CSS_FILE = CREATE_ICON_PREFIX + "CSS File";
    String XML_FILE = CREATE_ICON_PREFIX + "XML File";
    String LESS_FILE = CREATE_ICON_PREFIX + "Less File";
    String JAVA_CLASS = CREATE_ICON_PREFIX + "Java Class";
    String JAVASCRIPT_FILE = CREATE_ICON_PREFIX + "JavaScript File";
    String PACKAGE = CREATE_ICON_PREFIX + "Java Package";
  }

  @FindBy(id = Locators.CREATE_ICON_ID)
  WebElement createIcon;

  @FindBy(id = Locators.DELETE_ICON_ENABLED_ID)
  WebElement deleteIconEnable;

  /** click on delete icon */
  public void clickDeleteIcon() {
    new WebDriverWait(seleniumWebDriver, 7)
        .until(ExpectedConditions.elementToBeClickable(deleteIconEnable));
    deleteIconEnable.click();
  }

  public void clickCreateIcon() {
    new WebDriverWait(seleniumWebDriver, 7)
        .until(ExpectedConditions.elementToBeClickable(createIcon));
    createIcon.click();
  }

  public void runCommandFromCreateIconList(String createMenuCommand) {
    (new WebDriverWait(seleniumWebDriver, 7)
            .until(ExpectedConditions.presenceOfElementLocated(By.id(createMenuCommand))))
        .click();
    new WebDriverWait(seleniumWebDriver, 3)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.id(createMenuCommand)));
  }
}
