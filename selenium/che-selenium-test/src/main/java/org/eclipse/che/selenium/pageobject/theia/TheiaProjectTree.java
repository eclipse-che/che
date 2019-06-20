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
import static org.eclipse.che.selenium.pageobject.theia.TheiaProjectTree.Locators.FILES_TAB_XPATH;
import static org.eclipse.che.selenium.pageobject.theia.TheiaProjectTree.Locators.OPEN_WORKSPACE_BUTTON_XPATH;
import static org.eclipse.che.selenium.pageobject.theia.TheiaProjectTree.Locators.PROJECT_TREE_CONTAINER_ID;
import static org.eclipse.che.selenium.pageobject.theia.TheiaProjectTree.Locators.ROOT_PROJECTS_FOLDER_ID;
import static org.eclipse.che.selenium.pageobject.theia.TheiaProjectTree.Locators.SELECTED_ITEM_XPATH_TEMPLATE;
import static org.eclipse.che.selenium.pageobject.theia.TheiaProjectTree.Locators.SELECTED_ROOT_ITEM_XPATH;
import static org.eclipse.che.selenium.pageobject.theia.TheiaProjectTree.Locators.TREE_ITEM_ID_TEMPLATE;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.pageobject.TestWebElementRenderChecker;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;

@Singleton
public class TheiaProjectTree {
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;
  private final TestWebElementRenderChecker renderChecker;
  private final TheiaEditor theiaEditor;

  @Inject
  private TheiaProjectTree(
      SeleniumWebDriverHelper seleniumWebDriverHelper,
      TestWebElementRenderChecker renderChecker,
      TheiaEditor theiaEditor) {
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    this.renderChecker = renderChecker;
    this.theiaEditor = theiaEditor;
  }

  public interface Locators {
    String PROJECT_TREE_CONTAINER_ID = "theia-left-side-panel";
    String ROOT_PROJECTS_FOLDER_ID = "/projects";
    String TREE_ITEM_ID_TEMPLATE = "/projects:/projects/%s";
    String ITEM_BY_VISIBLE_TEXT_XPATH_TEMPLATE =
        "//div[@class='theia-TreeNodeContent']//div[text()='%s']";
    String SELECTED_ITEM_XPATH_TEMPLATE =
        "//div[contains(@class, 'theia-mod-selected theia-mod-focus')]//div[@id='/projects:/projects/%s']";
    String SELECTED_ROOT_ITEM_XPATH =
        "//div[contains(@class, 'theia-mod-selected theia-mod-focus')]//div[@id='/projects']";
    String COLLAPSED_ITEM_XPATH_TEMPLATE =
        "//div[@data-node-id='/projects:/projects/%s' and contains(@class, 'theia-mod-collapsed')]";
    String EXPAND_ITEM_ICON_XPATH_TEMPLATE = "//div[@data-node-id='/projects:/projects/%s']";
    String FILES_TAB_XPATH =
        "//div[contains(@class, 'theia-app-left')]//ul[@class='p-TabBar-content']//li[@title='Explorer']";
    String OPEN_WORKSPACE_BUTTON_XPATH = "//button[@class='open-workspace-button']";
  }

  public void waitOpenWorkspaceButton() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(OPEN_WORKSPACE_BUTTON_XPATH));
  }

  public void clickOnOpenWorkspaceButton() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(OPEN_WORKSPACE_BUTTON_XPATH));
  }

  public void clickOnFilesTab() {
    seleniumWebDriverHelper.waitNoExceptions(
        () -> seleniumWebDriverHelper.waitAndClick(By.xpath(FILES_TAB_XPATH)),
        WebDriverException.class);
  }

  public void waitFilesTab() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(FILES_TAB_XPATH));
  }

  public void waitProjectAreaOpened() {
    seleniumWebDriverHelper.waitVisibility(By.id(PROJECT_TREE_CONTAINER_ID));
    renderChecker.waitElementIsRendered(By.id(PROJECT_TREE_CONTAINER_ID));
  }

  private String getProjectItemId(String itemPath) {
    return format(TREE_ITEM_ID_TEMPLATE, itemPath);
  }

  private String getExpandItemIconXpath(String itemPath) {
    return String.format(EXPAND_ITEM_ICON_XPATH_TEMPLATE, itemPath);
  }

  public void waitItem(String itemPath) {
    String itemId = getProjectItemId(itemPath);
    seleniumWebDriverHelper.waitVisibility(By.id(itemId));
  }

  public void waitItemDisappearance(String itemPath) {
    String itemId = getProjectItemId(itemPath);
    seleniumWebDriverHelper.waitInvisibility(By.id(itemId));
  }

  public void waitProjectsRootItem() {
    seleniumWebDriverHelper.waitVisibility(By.id(ROOT_PROJECTS_FOLDER_ID));
  }

  public void clickOnProjectsRootItem() {
    seleniumWebDriverHelper.waitAndClick(By.id(ROOT_PROJECTS_FOLDER_ID));
  }

  public void clickOnItem(String itemPath) {
    String itemId = getProjectItemId(itemPath);
    seleniumWebDriverHelper.waitAndClick(By.id(itemId));
  }

  public void doubleClickOnItem(String itemPath) {
    String itemId = getProjectItemId(itemPath);
    seleniumWebDriverHelper.moveCursorToAndDoubleClick(By.id(itemId));
  }

  public boolean isItemExpanded(String itemPath) {
    final String itemXpath = getExpandItemIconXpath(itemPath);
    final String collapsedNodeClassName = "theia-mod-collapsed";

    return !seleniumWebDriverHelper
        .waitVisibilityAndGetAttribute(By.xpath(itemXpath), "class")
        .contains(collapsedNodeClassName);
  }

  public void waitItemExpanded(String itemPath) {
    seleniumWebDriverHelper.waitSuccessCondition(driver -> isItemExpanded(itemPath));
  }

  public void waitItemCollapsed(String itemPath) {
    seleniumWebDriverHelper.waitSuccessCondition(driver -> !isItemExpanded(itemPath));
  }

  public void waitItemSelected(String itemPath) {
    String itemXpath = format(SELECTED_ITEM_XPATH_TEMPLATE, itemPath);
    seleniumWebDriverHelper.waitVisibility(By.xpath(itemXpath));

    // Selection doesn't fully complete after the display. Have to wait for the end of the selection
    // logic.
    WaitUtils.sleepQuietly(2);
  }

  public void waitProjectsRootItemSelected() {
    seleniumWebDriverHelper.waitVisibility(By.xpath(SELECTED_ROOT_ITEM_XPATH));

    // Selection doesn't fully complete after the display. Have to wait for the end of the selection
    // logic.
    WaitUtils.sleepQuietly(2);
  }

  public boolean isRootItemSelected() {
    return seleniumWebDriverHelper.isVisible(By.xpath(SELECTED_ROOT_ITEM_XPATH));
  }

  public void openItem(String itemPath) {
    clickOnItem(itemPath);
    waitItemSelected(itemPath);
  }

  public boolean isItemVisible(String itemPath) {
    final String itemId = getProjectItemId(itemPath);

    return seleniumWebDriverHelper.isVisible(By.id(itemId));
  }

  public void expandPath(String expandPath) {
    List<String> itemsPaths = splitProjectPath(expandPath);

    for (int i = 0; i < itemsPaths.size(); i++) {
      String currentItemPath = itemsPaths.get(i);

      if (isItemExpanded(currentItemPath)) {
        continue;
      }

      waitItem(currentItemPath);
      openItem(currentItemPath);
    }
  }

  public void expandPathAndOpenFile(String expandPath, String fileName) {
    expandPath(expandPath);
    String pathToFile = expandPath.replace('.', '/') + '/' + fileName;
    waitItem(pathToFile);
    openItem(pathToFile);
    theiaEditor.waitEditorTab(fileName);
    theiaEditor.waitTabSelecting(fileName);
    theiaEditor.waitActiveEditor();
  }

  /**
   * Transforms specified {@code path} to paths of the each element.
   *
   * <p>For example {@code project/src/main/java/com.package}:
   *
   * <p>project
   *
   * <p>project/src
   *
   * <p>project/src/main
   *
   * <p>project/src/main/java
   *
   * <p>project/src/main/java/com
   *
   * <p>project/src/main/java/com/package
   *
   * @param path
   * @return paths of the each element
   */
  private List<String> splitProjectPath(String path) {
    Path fullPath = Paths.get(path);
    List<String> itemsPaths = new ArrayList<>();

    for (int i = 0; i < fullPath.getNameCount(); i++) {
      String currentItemPath = fullPath.subpath(0, i + 1).toString().replace(".", "/");
      itemsPaths.add(i, currentItemPath);
    }

    return itemsPaths;
  }
}
