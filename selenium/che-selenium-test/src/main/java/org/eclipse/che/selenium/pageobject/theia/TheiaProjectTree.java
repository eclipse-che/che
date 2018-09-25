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
package org.eclipse.che.selenium.pageobject.theia;

import static java.lang.String.format;
import static org.eclipse.che.selenium.pageobject.theia.TheiaProjectTree.Locators.EXPAND_ITEM_ICON_XPATH_TEMPLATE;
import static org.eclipse.che.selenium.pageobject.theia.TheiaProjectTree.Locators.ROOT_PROJECTS_FOLDER_ID;
import static org.eclipse.che.selenium.pageobject.theia.TheiaProjectTree.Locators.SELECTED_ITEM_XPATH_TEMPLATE;
import static org.eclipse.che.selenium.pageobject.theia.TheiaProjectTree.Locators.TREE_ITEM_ID_TEMPLATE;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.By;

@Singleton
public class TheiaProjectTree {
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;

  @Inject
  private TheiaProjectTree(SeleniumWebDriverHelper seleniumWebDriverHelper) {
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
  }

  public interface Locators {
    String ROOT_PROJECTS_FOLDER_ID = "/projects";
    String TREE_ITEM_ID_TEMPLATE = "/projects:/projects/%s";
    String ITEM_BY_VISIBLE_TEXT_XPATH_TEMPLATE =
        "//div[@class='theia-TreeNodeContent']//div[text()='%s']";
    String SELECTED_ITEM_XPATH_TEMPLATE =
        "//div[contains(@class, 'theia-mod-selected theia-mod-focus')]//div[@id='/projects:/projects/%s']";
    String COLLAPSED_ITEM_XPATH_TEMPLATE =
        "//div[@data-node-id='/projects:/projects/%s' and contains(@class, 'theia-mod-collapsed')]";
    String EXPAND_ITEM_ICON_XPATH_TEMPLATE = "//div[@data-node-id='/projects:/projects/%s']";
  }

  private String getProjectItemId(String itemPath) {
    return format(TREE_ITEM_ID_TEMPLATE, itemPath);
  }

  private String getExpandNodeIconXpath(String itemPath) {
    return String.format(EXPAND_ITEM_ICON_XPATH_TEMPLATE, itemPath);
  }

  public void waitProjectsRootFolder() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(ROOT_PROJECTS_FOLDER_ID));
  }

  public void clickOnProjectRootFolder() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(ROOT_PROJECTS_FOLDER_ID));
  }

  public void clickOnItem(String itemPath) {
    String itemId = getProjectItemId(itemPath);
    seleniumWebDriverHelper.waitAndClick(By.id(itemId));
  }

  public void doubleClickOnItem(String itemPath) {
    String itemId = getProjectItemId(itemPath);
    seleniumWebDriverHelper.moveCursorToAndDoubleClick(By.xpath(itemId));
  }

  public boolean isItemExpanded(String itemPath) {
    final String itemXpath = getExpandNodeIconXpath(itemPath);
    final String collapsedNodeClassName = "theia-mod-collapsed";

    return !seleniumWebDriverHelper
        .waitVisibilityAndGetAttribute(By.xpath(itemXpath), "class")
        .contains(collapsedNodeClassName);
  }

  public void waitItemExpanding(String itemPath) {
    seleniumWebDriverHelper.waitSuccessCondition(driver -> isItemExpanded(itemPath));
  }

  public void waitItemCollapsing(String itemPath) {
    seleniumWebDriverHelper.waitSuccessCondition(driver -> !isItemExpanded(itemPath));
  }

  public void waitItemSelecting(String itemPath) {
    String itemXpath = format(SELECTED_ITEM_XPATH_TEMPLATE, itemPath);
    seleniumWebDriverHelper.waitVisibility(By.xpath(itemXpath));
  }

  public void openItem(String itemPath) {
    clickOnItem(itemPath);
    waitItemSelecting(itemPath);
    doubleClickOnItem(itemPath);
  }
}
