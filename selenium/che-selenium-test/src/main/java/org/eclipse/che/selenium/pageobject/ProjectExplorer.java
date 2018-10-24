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
package org.eclipse.che.selenium.pageobject;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuFirstLevelItems.COMMANDS;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.SubMenuNew;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.EXPECTED_MESS_IN_CONSOLE_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOADER_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.WIDGET_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.ProjectExplorer.FolderTypes.PROJECT_FOLDER;
import static org.eclipse.che.selenium.pageobject.ProjectExplorer.Locators.ALL_PROJECTS_XPATH;
import static org.eclipse.che.selenium.pageobject.ProjectExplorer.Locators.COLOURED_ITEM_TEMPLATE;
import static org.eclipse.che.selenium.pageobject.ProjectExplorer.Locators.EXPLORER_RIGHT_TAB_ID;
import static org.eclipse.che.selenium.pageobject.ProjectExplorer.Locators.PROJECT_EXPLORER_ITEM_TEMPLATE;
import static org.eclipse.che.selenium.pageobject.ProjectExplorer.Locators.PROJECT_EXPLORER_TREE_ITEMS;
import static org.eclipse.che.selenium.pageobject.ProjectExplorer.Locators.SELECTED_ITEM_BY_NAME_XPATH_TEMPLATE;
import static org.eclipse.che.selenium.pageobject.ProjectExplorer.ProjectExplorerItemColors.BLUE;
import static org.eclipse.che.selenium.pageobject.ProjectExplorer.ProjectExplorerItemColors.DEFAULT;
import static org.eclipse.che.selenium.pageobject.ProjectExplorer.ProjectExplorerItemColors.GREEN;
import static org.eclipse.che.selenium.pageobject.ProjectExplorer.ProjectExplorerItemColors.YELLOW;
import static org.eclipse.che.selenium.pageobject.ProjectExplorer.ProjectExplorerOptionsMenuItem.COLLAPSE_ALL;
import static org.eclipse.che.selenium.pageobject.ProjectExplorer.ProjectExplorerOptionsMenuItem.REFRESH_MAIN;
import static org.eclipse.che.selenium.pageobject.ProjectExplorer.ProjectExplorerOptionsMenuItem.REVEAL_RESOURCE;
import static org.openqa.selenium.Keys.ARROW_DOWN;
import static org.openqa.selenium.Keys.COMMAND;
import static org.openqa.selenium.Keys.CONTROL;
import static org.openqa.selenium.Keys.ENTER;
import static org.openqa.selenium.Keys.F6;
import static org.openqa.selenium.Keys.LEFT;
import static org.openqa.selenium.Keys.RIGHT;
import static org.openqa.selenium.Keys.SHIFT;
import static org.openqa.selenium.Keys.UP;
import static org.openqa.selenium.support.ui.ExpectedConditions.not;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfAllElementsLocatedBy;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfNestedElementLocatedBy;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfAllElementsLocatedBy;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals;
import org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuFirstLevelItems;
import org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuItems;
import org.eclipse.che.selenium.core.utils.PlatformUtils;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.core.webdriver.WebDriverWaitFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ProjectExplorer {

  private static final Logger LOG = LoggerFactory.getLogger(ProjectExplorer.class);

  private final SeleniumWebDriver seleniumWebDriver;
  private final Loader loader;
  private final ActionsFactory actionsFactory;
  private final NavigateToFile navigateToFile;
  private final CodenvyEditor editor;
  private final TestWebElementRenderChecker testWebElementRenderChecker;
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;
  private final WebDriverWaitFactory waitFactory;
  private final NotificationsPopupPanel notificationsPopupPanel;
  private final int DEFAULT_TIMEOUT;

  @FindBy(id = "git.reference.name")
  WebElement projectReference;

  @Inject
  public ProjectExplorer(
      SeleniumWebDriver seleniumWebDriver,
      Loader loader,
      ActionsFactory actionsFactory,
      NavigateToFile navigateToFile,
      CodenvyEditor editor,
      TestWebElementRenderChecker testWebElementRenderChecker,
      SeleniumWebDriverHelper seleniumWebDriverHelper,
      WebDriverWaitFactory webDriverWaitFactory,
      NotificationsPopupPanel notificationsPopupPanel) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.loader = loader;
    this.actionsFactory = actionsFactory;
    this.navigateToFile = navigateToFile;
    this.editor = editor;
    this.testWebElementRenderChecker = testWebElementRenderChecker;
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    this.waitFactory = webDriverWaitFactory;
    this.notificationsPopupPanel = notificationsPopupPanel;
    this.DEFAULT_TIMEOUT = LOAD_PAGE_TIMEOUT_SEC;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  /** Colors are defined for coloring files in Project Explorer */
  public enum ProjectExplorerItemColors {
    YELLOW("color: #e0b91d;"),
    GREEN("color: #72ad42;"),
    BLUE("color: #3193d4;"),
    DEFAULT("opacity:1;");

    private final String color;

    ProjectExplorerItemColors(String color) {
      this.color = color;
    }

    private String get() {
      return this.color;
    }
  }

  /** Displaying folder types */
  public enum FolderTypes {
    SIMPLE_FOLDER("simpleFolder"),
    PROJECT_FOLDER("projectFolder"),
    JAVA_SOURCE_FOLDER("javaSourceFolder");

    private final String folderType;

    FolderTypes(String folderType) {
      this.folderType = folderType;
    }

    private String get() {
      return this.folderType;
    }
  }

  /** Items which displayed after click on {@code Options} button in the project explorer area */
  public enum ProjectExplorerOptionsMenuItem {
    MAXIMIZE("gwt-debug-contextMenu/Maximize"),
    HIDE("gwt-debug-contextMenu/Hide"),
    COLLAPSE_ALL("gwt-debug-contextMenu/collapseAll"),
    REVEAL_RESOURCE("gwt-debug-contextMenu/revealResourceInProjectTree"),
    REFRESH_MAIN("gwt-debug-contextMenu/refreshPathAction"),
    LINK_WITH_EDITOR("gwt-debug-contextMenu/linkWithEditor");

    private final String id;

    ProjectExplorerOptionsMenuItem(String id) {
      this.id = id;
    }

    private String get() {
      return this.id;
    }
  }

  /** Locators constants for project explorer elements */
  protected interface Locators {
    String PROJECT_EXPLORER_TREE_ITEMS = "gwt-debug-projectTree";
    String EXPLORER_RIGHT_TAB_ID = "gwt-debug-partButton-Projects";
    String ALL_PROJECTS_XPATH = "//div[@path=@project]";
    String PROJECT_EXPLORER_ITEM_TEMPLATE = "//div[@path='/%s']/div";
    String COLOURED_ITEM_TEMPLATE =
        "//div[@id='gwt-debug-projectTree']//div[@path='/%s']/descendant::div[@style='%s']";
    String MAXIMIZE_BUTTON_XPATH = "(//div[@id='gwt-debug-maximizeButton'])[position()=1]";
    String SELECTED_ITEM_BY_NAME_XPATH_TEMPLATE =
        "//div[contains(@class, 'selected')]//div[text()='%s']";
    // "//div[@name='%s' or @name='%s.class']//div[contains(@class, 'selected')] ";
  }

  private String getItemXpathByPath(String itemPath) {
    return format(PROJECT_EXPLORER_ITEM_TEMPLATE, itemPath);
  }

  /**
   * Waits for visibility of the {@link WebElement} with specified {@code path} for a during {@code
   * timeout} and gets this {@link WebElement}
   *
   * @param path path to item in format: 'RootFolder/src/main/java/org/qe/package'
   * @param timeout timeout in seconds
   * @return found {@link WebElement}
   */
  private WebElement waitAndGetItem(String path, int timeout) {
    return seleniumWebDriverHelper.waitVisibility(By.xpath(getItemXpathByPath(path)), timeout);
  }

  /**
   * Waits for visibility of the {@link WebElement} with specified {@code path} and gets this {@link
   * WebElement}
   *
   * @param path path to item in format: 'RootFolder/src/main/java/org/qe/package'
   * @return found {@link WebElement}
   */
  private WebElement waitAndGetItem(String path) {
    return waitAndGetItem(path, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
  }

  /** Waits for visibility and clicks on {@code Options} button in the project explorer area */
  public void clickOnProjectExplorerOptionsButton() {
    seleniumWebDriverHelper.waitAndClick(
        By.xpath("//div[@id='gwt-debug-navPanel']//div[@name='workBenchIconPartStackOptions']"),
        REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
  }

  /**
   * Waits for visibility and clicks on {@code item} with specified {@code id}
   *
   * @param item 'id' which stores in {@link ProjectExplorerOptionsMenuItem}
   */
  public void clickOnOptionsMenuItem(ProjectExplorerOptionsMenuItem item) {
    seleniumWebDriverHelper.waitAndClick(
        By.xpath(format("//tr[@id='%s']", item.get())), REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
  }

  /** Clicks on project explorer tab and waits its appearance. */
  public void openAndWait() {
    clickOnProjectExplorerTab();
    waitProjectExplorer();
  }

  /** Clicks on the project explorer tab */
  public void clickOnProjectExplorerTab() {
    seleniumWebDriverHelper.waitAndClick(
        By.id(EXPLORER_RIGHT_TAB_ID), REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
  }

  /**
   * Waits project explorer and click on empty area
   *
   * @param timeOutForWaiting defined timeout for appearance project explorer page
   */
  public void clickOnEmptyAreaOfProjectTree(int timeOutForWaiting) {
    waitProjectExplorer(timeOutForWaiting);
    seleniumWebDriverHelper.waitAndClick(By.id(PROJECT_EXPLORER_TREE_ITEMS));
  }

  /** wait appearance of the IDE Project Explorer */
  public void waitProjectExplorer() {
    waitProjectExplorer(EXPECTED_MESS_IN_CONSOLE_SEC);
  }

  public void waitProjectExplorerDisappearance(int timeout) {
    seleniumWebDriverHelper.waitInvisibility(By.id(PROJECT_EXPLORER_TREE_ITEMS), timeout);
  }

  /**
   * Waits appearance of the IDE Project Explorer
   *
   * @param timeout waiting timeout in seconds
   */
  public void waitProjectExplorer(int timeout) {
    try {
      seleniumWebDriverHelper.waitVisibility(By.id(PROJECT_EXPLORER_TREE_ITEMS), timeout);
      loader.waitOnClosed();
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      if (seleniumWebDriverHelper.isVisible(By.id("ide-loader-progress-bar"))) {
        fail("Known issue https://github.com/eclipse/che/issues/8468", ex);
      }

      throw ex;
    }
  }

  /**
   * Waits item in project explorer with specified {@code path}.
   *
   * @param path item's path in format: "Test/src/pom.xml"
   */
  public void waitItem(String path) {
    waitItem(path, EXPECTED_MESS_IN_CONSOLE_SEC);
  }

  /**
   * Waits item in project explorer with specified {@code path} and customized {@code timeout}.
   *
   * @param path item's path in format: "Test/src/pom.xml"
   * @param timeout waiting timeout in seconds
   */
  public void waitItem(String path, int timeout) {
    loader.waitOnClosed();

    seleniumWebDriverHelper.waitVisibility(
        By.xpath(format(PROJECT_EXPLORER_ITEM_TEMPLATE, path)), timeout);
  }

  /**
   * Wait the specified reference to be present near the project name in the project explorer.
   *
   * @param reference git reference e.g. branch, tag or commit
   */
  public void waitReferenceName(String reference) {
    loader.waitOnClosed();

    seleniumWebDriverHelper.waitTextEqualsTo(projectReference, "(" + reference + ")");
  }

  /**
   * Waits until library will be present in External Libraries folder
   *
   * @param libraryName visible name of library
   */
  public void waitLibraryIsPresent(String libraryName) {
    seleniumWebDriverHelper.waitVisibility(
        By.xpath(format("//div[@synthetic='true'and @name='%s']", libraryName)),
        WIDGET_TIMEOUT_SEC);
  }

  /**
   * Waits until library will be not present in External Libraries folder
   *
   * @param libraryName visible name of library
   */
  public void waitLibraryIsNotPresent(String libraryName) {
    seleniumWebDriverHelper.waitInvisibility(
        By.xpath(format("//div[@synthetic='true'and @name='%s']", libraryName)),
        WIDGET_TIMEOUT_SEC);
  }

  /**
   * Waits visible item in project explorer with specified {@code path}.
   *
   * <p>With [string-length(text()) > 0] we will be sure that 'div' not empty and contains some text
   * here we suggest it will be name of node but for now we don't check name to equals
   *
   * @param path item's path in format: "Test/src/pom.xml"
   */
  public void waitVisibleItem(String path) {
    seleniumWebDriverHelper.waitVisibility(
        By.xpath("//div[@path='/" + path + "']//div[string-length(text()) > 0]"),
        ELEMENT_TIMEOUT_SEC);
  }

  /**
   * Waits item with specified {@code path} will be invisible in project explorer
   *
   * @param path item's path in format: "Test/src/pom.xml"
   */
  public void waitItemInvisibility(String path) {
    seleniumWebDriverHelper.waitInvisibility(
        By.xpath(format(PROJECT_EXPLORER_ITEM_TEMPLATE, path)), LOADER_TIMEOUT_SEC);
  }

  /**
   * Waits visibility of yellow project explorer's item with specified {@code path}
   *
   * @param path item's path in format: "Test/src/pom.xml"
   */
  public void waitYellowNode(String path) {
    waitNodeColor(path, YELLOW);
  }

  /**
   * Waits visibility of green project explorer's item with specified {@code path}
   *
   * @param path item's path in format: "Test/src/pom.xml"
   */
  public void waitGreenNode(String path) {
    waitNodeColor(path, GREEN);
  }

  /**
   * Waits visibility of blue project explorer's item with specified {@code path}
   *
   * @param path item's path in format: "Test/src/pom.xml"
   */
  public void waitBlueNode(String path) {
    waitNodeColor(path, BLUE);
  }

  /**
   * Waits visibility of project explorer's item with specified {@code path} and default color
   *
   * @param path item's path in format: "Test/src/pom.xml"
   */
  public void waitDefaultColorNode(String path) {
    waitNodeColor(path, DEFAULT);
  }

  /**
   * Waits visibility of project explorer's item with specified {@code path} and {@code color}
   *
   * @param path item's path in format: "Test/src/pom.xml"
   * @param color item's color which stores in {@link ProjectExplorerItemColors}
   */
  public void waitNodeColor(String path, ProjectExplorerItemColors color) {
    seleniumWebDriverHelper.waitVisibility(
        By.xpath(format(COLOURED_ITEM_TEMPLATE, path, color.get())));
  }

  /**
   * Waits item with specified {@code name} in project explorer tree area.
   *
   * <p>Note! Item must be unique.
   *
   * <p>For example: in a project can be some folder. Into each folder can be a file with same name.
   * This method will be track only first item.
   *
   * @param name visible item's name
   * @return found {@link WebElement}
   */
  public WebElement waitVisibilityByName(String name) {
    return waitVisibilityByName(name, DEFAULT_TIMEOUT);
  }

  /**
   * Invokes the {@link ProjectExplorer#waitVisibilityByName(String)} method for each specified
   * {@code names}.
   *
   * @param names visible names of project explorer items.
   * @return found elements.
   */
  public List<WebElement> waitVisibilitySeveralItemsByName(String... names) {
    return asList(names)
        .stream()
        .map(itemName -> waitVisibilityByName(itemName))
        .collect(Collectors.toList());
  }

  /**
   * Waits item with specified {@code name} in project explorer tree area, during specified {@code
   * timeOut}
   *
   * <p>Note! Item must be unique.
   *
   * <p>For example: in a project can be some folder. Into each folder can be a file with same name.
   * This method will be track only first item.
   *
   * @param name visible item's name
   * @param timeOut waiting timeout in seconds
   * @return found {@link WebElement}
   */
  public WebElement waitVisibilityByName(String name, int timeOut) {
    loader.waitOnClosed();

    return seleniumWebDriverHelper.waitVisibility(
        By.xpath(format("//div[@id='gwt-debug-projectTree']//div[text()='%s']", name)), timeOut);
  }

  /**
   * Waits until item with specified {@code name} is not visible in project explorer tree area.
   *
   * <p>Note! Items must not repeat.
   *
   * <p>For example: in a project can be some folder. Into each folder can be a file with same name.
   * This method will be track only first item.
   *
   * @param name item's visible name
   */
  public void waitItemIsNotPresentVisibleArea(String name) {
    seleniumWebDriverHelper.waitInvisibility(By.name(name));
  }

  /**
   * Waits visibility and selects item with specified {@code path} in project explorer.
   *
   * @param path item's path in format: "Test/src/pom.xml"
   */
  public void waitAndSelectItem(String path) {
    waitAndSelectItem(path, EXPECTED_MESS_IN_CONSOLE_SEC);
  }

  /**
   * Waits during {@code timeout} for visibility and selects item with specified {@code path} in
   * project explorer.
   *
   * @param path item's path in format: 'Test/src/pom.xml'
   * @param timeout waiting timeout in seconds
   */
  public void waitAndSelectItem(String path, int timeout) {
    waitItem(path);
    seleniumWebDriverHelper.waitNoExceptions(
        () -> waitAndGetItem(path, timeout).click(),
        LOAD_PAGE_TIMEOUT_SEC,
        StaleElementReferenceException.class);
  }

  /**
   * Selects item in project explorer with specified {@code name} attribute. It means that if exist
   * two files with the same name in the different folders, and both file is visible, than the first
   * file in the DOM will be selected
   *
   * @param name item's visible name
   */
  public void waitAndSelectItemByName(String name) {
    seleniumWebDriverHelper.waitNoExceptions(
        () -> waitVisibilityByName(name).click(),
        ELEMENT_TIMEOUT_SEC,
        StaleElementReferenceException.class);
  }

  /**
   * Waits external maven Libraries relative module
   *
   * @param modulePath
   */
  public WebElement waitLibraries(String modulePath) {
    return seleniumWebDriverHelper.waitVisibility(
        By.xpath(format("//div [@name='External Libraries' and @project='/%s']", modulePath)));
  }

  /** Waits disappearance external maven Libraries relative module */
  public void waitLibrariesAreNotPresent(String modulePath) {
    seleniumWebDriverHelper.waitInvisibility(
        By.xpath(format("//div [@name='External Libraries' and @project='/%s']", modulePath)));
  }

  /** Selects external maven Library relative module */
  public void selectLibraries(String modulePath) {
    waitLibraries(modulePath).click();
  }

  /**
   * Selects item with specified {@code path} in project explorer.
   *
   * @param path item's path in format: 'Test/src/pom.xml'
   */
  public void scrollAndSelectItem(String path) {
    waitItem(path);
    actionsFactory
        .createAction(seleniumWebDriver)
        .moveToElement(waitAndGetItem(path))
        .click()
        .perform();
  }

  /**
   * Opens an item with specified {@code path} in the project explorer
   *
   * @param path item's path in format: 'Test/src/pom.xml'
   */
  public void openItemByPath(String path) {
    Actions action = seleniumWebDriverHelper.getAction();

    seleniumWebDriverHelper.waitNoExceptions(
        () -> {
          waitAndSelectItem(path);
          waitItemIsSelected(path);
        },
        (EXPECTED_MESS_IN_CONSOLE_SEC + ELEMENT_TIMEOUT_SEC) * 2,
        NoSuchElementException.class);

    seleniumWebDriverHelper.waitNoExceptions(
        () -> {
          action.doubleClick(waitAndGetItem(path)).perform();
        },
        LOAD_PAGE_TIMEOUT_SEC,
        StaleElementReferenceException.class);

    loader.waitOnClosed();
  }

  /**
   * Opens item with specified {@code name} by double click.
   *
   * <p>It means, if we have several items with same name, and they are visible, than the first item
   * in the DOM will be opened.
   *
   * <p>If need to opens item in concrete folder, use method {@link
   * ProjectExplorer#openItemByPath(String)}
   *
   * @param name item's visible name
   */
  public void openItemByVisibleNameInExplorer(String name) {
    seleniumWebDriverHelper.waitNoExceptions(
        () -> openItemByVisibleName(name), StaleElementReferenceException.class);
  }

  private void openItemByVisibleName(String name) {

    waitAndSelectItemByName(name);
    waitItemSelectedByName(name);
    actionsFactory
        .createAction(seleniumWebDriver)
        .moveToElement(waitVisibilityByName(name))
        .doubleClick()
        .perform();
    loader.waitOnClosed();
  }

  /**
   * Invokes the {@link ProjectExplorer#openItemByVisibleNameInExplorer(String)} method for each
   * specified {@code names}.
   *
   * @param names visible names of the project explorer items.
   */
  public void openSeveralItemsByVisibleNameInExplorer(String... names) {
    for (String itemName : names) {
      openItemByVisibleNameInExplorer(itemName);
    }
  }

  /**
   * Opens visible package with specified {@code name} by double click in the Project Explorer tree
   *
   * @param name item's visible name. For example: "com.qe.package" if packages displaying together,
   *     or just "org" if package displaying separately
   */
  public void openVisiblePackage(String name) {
    actionsFactory
        .createAction(seleniumWebDriver)
        .doubleClick(waitVisibilityByName(name))
        .perform();
  }

  /**
   * Waits invisibility of the item with specified {@code path} in project explorer
   *
   * @param path item's path in format: 'Test/src/pom.xml'
   */
  public void waitDisappearItemByPath(String path) {
    seleniumWebDriverHelper.waitInvisibility(
        By.xpath(format(PROJECT_EXPLORER_ITEM_TEMPLATE, path)), ELEMENT_TIMEOUT_SEC);
  }

  /**
   * Waits visibility of the folder with defined {@code path} and {@code folderType}
   *
   * @param path folder's path in format: "TestProject/src/main/test"
   * @param folderType folder's type, defined in {@link FolderTypes}
   */
  public void waitDefinedTypeOfFolder(String path, FolderTypes folderType) {
    loader.waitOnClosed();
    seleniumWebDriverHelper.waitVisibility(
        By.xpath(
            format(
                "//div[@path='/%s']/div/*[local-name() = 'svg' and @id='%s']",
                path, folderType.get())),
        ELEMENT_TIMEOUT_SEC);
  }

  /**
   * Waits removing item from DOM
   *
   * @param pathToItem item's path in format: "TestProject/src/main/test"
   */
  public void waitRemoveItemsByPath(String pathToItem) {
    waitFactory
        .get(DEFAULT_TIMEOUT)
        .until(
            not(
                presenceOfAllElementsLocatedBy(
                    By.xpath(format(PROJECT_EXPLORER_ITEM_TEMPLATE, pathToItem)))));
  }

  /**
   * Opens context menu on item with specified {@code path} Selecting item and invoking context menu
   * are two separate operations. If some item retakes focus then context menu will be invoked at
   * wrong item. It might happen because project explorer is updated asynchronously and new appeared
   * items retake focus. So, there are several tries to invoke context menu at correct item.
   *
   * @param path item's path in format: "Test/src/pom.xml".
   */
  public void openContextMenuByPathSelectedItem(String path) {
    final String itemXpath = getItemXpathByPath(path);

    for (int i = 1; ; i++) {
      waitAndSelectItem(path);
      waitItemIsSelected(path);
      seleniumWebDriverHelper.waitAndContextClick(By.xpath(itemXpath));
      waitContextMenu();
      try {
        waitItemIsSelected(path, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
        return;
      } catch (TimeoutException e) {
        seleniumWebDriverHelper.hideContextMenu();
        waitContextMenuPopUpClosed();
        if (i > 1) {
          final String errorMessage =
              format(
                  "Selection of the project tree item which located by \"%s\" has been lost, context menu event can't be performed for this element",
                  getItemXpathByPath(path));
          throw new RuntimeException(errorMessage);
        }
      }
    }
  }

  /** Waits on context menu body's visibility. */
  public void waitContextMenu() {
    testWebElementRenderChecker.waitElementIsRendered(
        By.xpath("//tr[@id='gwt-debug-contextMenu/newGroup']/parent::tbody"));

    seleniumWebDriverHelper.waitVisibility(
        By.id(ContextMenuFirstLevelItems.NEW.get()), WIDGET_TIMEOUT_SEC);
  }

  /** Waits on context menu item is not visible */
  public void waitContexMenuItemIsNotVisible(ContextMenuItems item) {
    waitContextMenu();
    seleniumWebDriverHelper.waitInvisibility(
        By.xpath(format("//tr[@item-enabled='true' and @id='%s']", item.get())));
  }

  /**
   * Clicks on element from context menu by specified {@code itemId}
   *
   * @param item
   */
  public void clickOnItemInContextMenu(ContextMenuItems item) {
    seleniumWebDriverHelper.waitAndClick(
        By.xpath(format("//tr[@item-enabled='true' and @id='%s']", item.get())));
  }

  /**
   * Clicks on item from {@link SubMenuNew}.
   *
   * <p>This is submenu which displays after click on {@link ContextMenuFirstLevelItems#NEW}.
   *
   * @param item
   */
  public void clickOnNewContextMenuItem(ContextMenuItems item) {
    seleniumWebDriverHelper.waitAndClick(
        By.xpath(format("//tr[@item-enabled='true' and @id='%s']", item.get())));
    waitContextMenuPopUpClosed();
  }

  /**
   * Waits until context menu, which includes {@link ContextMenuFirstLevelItems#NEW} item, be
   * invisible
   */
  public void waitContextMenuPopUpClosed() {
    seleniumWebDriverHelper.waitInvisibility(By.id(ContextMenuFirstLevelItems.NEW.get()));
  }

  /**
   * Performs the multi-select by {@code Ctrl} key
   *
   * @param path item's path in format: "Test/src/pom.xml".
   */
  public void selectMultiFilesByCtrlKeys(String path) {
    if (PlatformUtils.isMac()) {
      Actions actions = actionsFactory.createAction(seleniumWebDriver);
      actions.keyDown(COMMAND).perform();
      waitAndSelectItem(path);
      waitItemIsSelected(path);
      actions.keyUp(COMMAND).perform();
    } else {
      Actions actions = actionsFactory.createAction(seleniumWebDriver);
      actions.keyDown(CONTROL).perform();
      waitAndSelectItem(path);
      waitItemIsSelected(path);
      actions.keyUp(CONTROL).perform();
    }
  }

  /**
   * Opens {@link ProjectExplorerOptionsMenuItem} menu and clicks on {@link
   * ProjectExplorerOptionsMenuItem#REVEAL_RESOURCE}
   */
  public void revealResourceByOptionsButton() {
    clickOnProjectExplorerOptionsButton();
    clickOnOptionsMenuItem(REVEAL_RESOURCE);
  }

  /**
   * Performs the multi-select by {@code Shift} key
   *
   * @param path item's path in format: "Test/src/pom.xml".
   */
  public void selectMultiFilesByShiftKey(String path) {
    Actions actions = actionsFactory.createAction(seleniumWebDriver);
    actions.keyDown(SHIFT).perform();
    waitAndSelectItem(path);
    waitItemIsSelected(path);
    actions.keyUp(Keys.SHIFT).perform();
  }

  /**
   * Performs the multi-select by {@code Shift} key with check selected items.
   *
   * <p>Note! Before using this method, one item in project explorer must be selected.
   *
   * @param clickItemPath is the path to item which will be clicked in format: "Test/src/pom.xml".
   * @param selectedItemsPaths is the paths to the each items which should be checked.
   */
  public void selectMultiFilesByShiftKeyWithCheckMultiselection(
      String clickItemPath, List<String> selectedItemsPaths) {
    Actions actions = actionsFactory.createAction(seleniumWebDriver);
    actions.keyDown(SHIFT).perform();
    waitAndSelectItem(clickItemPath);
    waitAllItemsIsSelected(selectedItemsPaths);
    actions.keyUp(SHIFT).perform();
  }

  /**
   * Performs the multi-select by {@code Shift} key with check selected items, for items between
   * {@code firstItemPath} and {@code secondItemPath} including this two items.
   *
   * @param firstItemPath is the path to the first item which will be clicked in format:
   *     "Test/src/pom.xml".
   * @param secondItemPath is the path to the second item which will be clicked in format:
   *     "Test/src/pom.xml".
   * @param selectedItemsPaths is the paths to the each items which should be checked.
   */
  public void selectMultiFilesByShiftKeyWithCheckMultiselection(
      String firstItemPath, String secondItemPath, List<String> selectedItemsPaths) {
    waitAndSelectItem(firstItemPath);
    waitItemIsSelected(firstItemPath);

    Actions actions = actionsFactory.createAction(seleniumWebDriver);
    actions.keyDown(SHIFT).perform();
    waitAndSelectItem(secondItemPath);
    waitAllItemsIsSelected(selectedItemsPaths);
    actions.keyUp(SHIFT).perform();
  }

  /**
   * Opens {@link ProjectExplorerOptionsMenuItem} and clicks on {@link
   * ProjectExplorerOptionsMenuItem#COLLAPSE_ALL}
   */
  public void collapseProjectTreeByOptionsButton() {
    clickOnProjectExplorerOptionsButton();
    clickOnOptionsMenuItem(COLLAPSE_ALL);
  }

  /**
   * Launches the 'Refactor Rename' form.
   *
   * <p>Note! Before using this method the project explorer's item should be selected
   */
  public void launchRefactorByKeyboard() {
    loader.waitOnClosed();
    actionsFactory
        .createAction(seleniumWebDriver)
        .keyDown(SHIFT)
        .sendKeys(F6)
        .keyUp(SHIFT)
        .perform();
  }

  /**
   * Launches the 'Refactor Move' form by keyboard.
   *
   * <p>Note! Before using this method the project explorer's item should be selected
   */
  public void launchRefactorMoveByKeyboard() {
    loader.waitOnClosed();
    actionsFactory.createAction(seleniumWebDriver).sendKeys(F6).perform();
  }

  /**
   * Waits until all projects in project explorer be visible and gets their names
   *
   * @return names of all projects
   */
  public List<String> getNamesAllProjects() {
    return waitFactory
        .get(DEFAULT_TIMEOUT)
        .until(visibilityOfAllElementsLocatedBy(By.xpath(ALL_PROJECTS_XPATH)))
        .stream()
        .map((webElement) -> webElement.getAttribute("name"))
        .collect(toList());
  }

  /**
   * Returns all names of items from tree of the project explorer
   *
   * @return list of items
   */
  public List<String> getNamesOfAllOpenItems() {
    return asList(
        seleniumWebDriverHelper
            .waitVisibility(By.id(PROJECT_EXPLORER_TREE_ITEMS))
            .getText()
            .split("\n"));
  }

  /** Performs right arrow key pressed in a browser */
  public void sendToItemRightArrowKey() {
    seleniumWebDriverHelper.sendKeys(RIGHT.toString());
  }

  /** Performs left arrow key pressed in a browser */
  public void sendToItemLeftArrowKey() {
    seleniumWebDriverHelper.sendKeys(LEFT.toString());
  }

  /** Performs up arrow key pressed in a browser */
  public void sendToItemUpArrowKey() {
    seleniumWebDriverHelper.sendKeys(UP.toString());
  }

  /** Performs down arrow key pressed in a browser */
  public void sendToItemDownArrowKey() {
    seleniumWebDriverHelper.sendKeys(ARROW_DOWN.toString());
  }

  /** Performs enter key pressed in a browser */
  public void sendToItemEnterKey() {
    seleniumWebDriverHelper.sendKeys(ENTER.toString());
  }

  /**
   * Opens {@link ProjectExplorerOptionsMenuItem} and clicks on {@link
   * ProjectExplorerOptionsMenuItem#REFRESH_MAIN}
   */
  public void clickOnRefreshTreeButton() {
    clickOnProjectExplorerOptionsButton();
    clickOnOptionsMenuItem(REFRESH_MAIN);
    loader.waitOnClosed();
  }

  /**
   * Waits during {@code userTimeout} until 'Import Project' link be visible and clicks on it.
   *
   * @param userTimeout waiting timeout in seconds
   */
  public void clickOnImportProjectLink(int userTimeout) {
    waitProjectExplorer();
    waitFactory
        .get(userTimeout)
        .until(
            presenceOfNestedElementLocatedBy(
                By.id(PROJECT_EXPLORER_TREE_ITEMS), By.xpath("//div[text()='Import Project...']")))
        .click();
  }

  /**
   * Waits during {@code userTimeout} until 'Create Project' link be visible and clicks on it.
   *
   * @param userTimeout waiting timeout in seconds
   */
  public void clickOnCreateProjectLink(int userTimeout) {
    waitProjectExplorer();
    waitFactory
        .get(userTimeout)
        .until(
            presenceOfNestedElementLocatedBy(
                By.id(PROJECT_EXPLORER_TREE_ITEMS), By.xpath("//div[text()='Create Project...']")))
        .click();
  }

  /** Invokes command from context menu. */
  public void invokeCommandWithContextMenu(
      ContextMenuCommandGoals commandsGoal, String pathToItem, String commandName) {
    try {
      openContextMenuByPathSelectedItem(pathToItem);
    } catch (NoSuchElementException e) {
      clickOnRefreshTreeButton();
      openContextMenuByPathSelectedItem(
          pathToItem); // there context menu may not be opened from the first try
    }

    clickOnItemInContextMenu(COMMANDS);
    clickOnItemInContextMenu(commandsGoal);
    seleniumWebDriverHelper.waitAndClick(
        By.xpath(format("//tr[@id[contains(.,'%s')]]", commandName)),
        REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
  }

  /** Invokes command from context menu. */
  public void invokeCommandWithContextMenu(
      ContextMenuCommandGoals commandsGoal,
      String pathToItem,
      String commandName,
      String machineName) {
    invokeCommandWithContextMenu(commandsGoal, pathToItem, commandName);

    loader.waitOnClosed();
    actionsFactory
        .createAction(seleniumWebDriver)
        .doubleClick(
            seleniumWebDriverHelper.waitVisibility(
                By.xpath(
                    format(
                        "//select[@class = 'gwt-ListBox']//option[contains(.,'%s')]",
                        machineName))))
        .perform();
  }

  /**
   * Moves mouse`s cursor to item with specified {@code path}, and scrolls project explorer tree
   * until this item be visible
   *
   * @param path item's path in format: "Test/src/pom.xml".
   */
  public void scrollToItemByPath(String path) {
    waitItem(path);
    actionsFactory.createAction(seleniumWebDriver).moveToElement(waitAndGetItem(path)).perform();
  }

  /**
   * Expands specified {@code path} in project explorer. Path given in format like below:
   *
   * <p>"TestProject/src/main/java/org.eclipse.che.examples"
   *
   * @param path path for expand
   */
  public void expandPathInProjectExplorer(String path) {
    List<String> itemsPaths = splitJavaProjectPath(path);

    for (int i = 0; i < itemsPaths.size(); i++) {
      String currentItem = itemsPaths.get(i);
      int nextItemIndex = i + 1;

      // don't open if next item has already been visible
      if (nextItemIndex < itemsPaths.size() && isItemVisible(itemsPaths.get(nextItemIndex))) {
        continue;
      }

      waitItem(currentItem);
      openItemByPath(currentItem);
    }
  }

  /**
   * Checks without wait a visibility state of the item with specified {@code itemPath}
   *
   * @param itemPath Path in format "RootFolder/src/main/java/org/package/ExampleClass.java".
   * @return state of visibility
   */
  public boolean isItemVisible(String itemPath) {
    return seleniumWebDriverHelper.isVisible(
        By.xpath(format(PROJECT_EXPLORER_ITEM_TEMPLATE, itemPath)));
  }

  /**
   * Expands specified path in project explorer and open a file Path given in format like like
   * below:
   *
   * <p>"TestProject/src/main/java/org.eclipse.che.examples"
   *
   * @param path path for expand
   * @param fileName
   */
  public void expandPathInProjectExplorerAndOpenFile(String path, String fileName) {
    expandPathInProjectExplorer(path);
    String pathToFile = path.replace('.', '/') + '/' + fileName;
    waitItem(pathToFile);
    openItemByPath(pathToFile);
    editor.waitActive();
  }

  /**
   * Parses {@code path} and makes full path for each element which separated by "/".
   *
   * <p>Items like 'org.package.sub' will be treated as 'org/package/sub'.
   *
   * @param path Path in format "RootFolder/src/main/java/org.package.sub".
   * @return Paths in format:
   *     <p>["RootFolder",
   *     <p>"RootFolder/src",
   *     <p>"RootFolder/src/main",
   *     <p>"RootFolder/src/main/java",
   *     <p>"RootFolder/src/main/java/org/package/sub"]
   */
  private List<String> splitJavaProjectPath(String path) {
    Path fullPath = Paths.get(path);
    List<String> itemsPaths = new ArrayList<>();

    for (int i = 0; i < fullPath.getNameCount(); i++) {
      String currentItemPath = fullPath.subpath(0, i + 1).toString().replace(".", "/");
      itemsPaths.add(i, currentItemPath);
    }

    return itemsPaths;
  }

  /**
   * Finds a file by name using Navigate to file feature. Expand the tree using Reveal resource
   * feature
   *
   * @param file file for search with Navigate to file feature.
   * @param pathToFile
   */
  public void expandToFileWithRevealResource(String file, String pathToFile) {
    loader.waitOnClosed();
    navigateToFile.waitFormToOpen();
    loader.waitOnClosed();
    navigateToFile.typeSymbolInFileNameField(file);
    navigateToFile.waitSuggestedPanel();
    navigateToFile.isFilenameSuggested(file);
    navigateToFile.selectFileByName(file);
    navigateToFile.waitFormToClose();
    revealResourceByOptionsButton();
    waitItem(pathToFile);
  }

  /** Invokes special javascript function for 'quick' expand project(s) tree */
  public void quickExpandWithJavaScript() {
    String jsScript = "IDE.ProjectExplorer.expandAll();";
    seleniumWebDriver.executeScript(jsScript);
    loader.waitOnClosed();
  }

  /**
   * Invokes special javascript function for 'quick' collapse all opened nodes of project(s) tree
   */
  public void quickCollapseJavaScript() {
    String jsScript = "IDE.ProjectExplorer.collapseAll()";
    seleniumWebDriver.executeScript(jsScript);
    loader.waitOnClosed();
  }

  /**
   * Invokes special javascript function for 'quick' open the project nodes to the resource (path or
   * file), defined by user.
   *
   * @param absPathToResource absolute path to necessary file or folder
   */
  public void quickRevealToItemWithJavaScript(String absPathToResource) {
    String jsScript = "IDE.ProjectExplorer.reveal(\"" + absPathToResource.replace(".", "/") + "\")";
    seleniumWebDriver.executeScript(jsScript);
    loader.waitOnClosed();
  }

  /**
   * Waits until item with specified {@code path} be selected
   *
   * @param path item's path in format: "Test/src/pom.xml".
   */
  public void waitItemIsSelected(String path) {
    waitItemIsSelected(path, ELEMENT_TIMEOUT_SEC);
  }

  /**
   * Waits until item with specified {@code path} be selected
   *
   * @param path item's path in format: "Test/src/pom.xml".
   */
  public void waitItemIsSelected(String path, int timeout) {
    seleniumWebDriverHelper.waitVisibility(
        By.xpath(
            format(
                "//div[@path='/%s']/div[contains(concat(' ', normalize-space(@class), ' '), ' selected')]",
                path)),
        timeout);
  }

  public void waitItemSelectedByName(String visibleName) {
    final String itemXpath = format(SELECTED_ITEM_BY_NAME_XPATH_TEMPLATE, visibleName);

    seleniumWebDriverHelper.waitVisibility(By.xpath(itemXpath));
  }

  /**
   * Waits until all items with specified {@code paths} be selected
   *
   * @param paths items paths in format: "Test/src/pom.xml".
   */
  public void waitAllItemsIsSelected(List<String> paths) {
    paths.forEach(this::waitItemIsSelected);
  }

  /** click on the 'Maximize' button */
  public void clickOnMaximizeButton() {
    seleniumWebDriverHelper.waitAndClick(By.xpath(Locators.MAXIMIZE_BUTTON_XPATH));
  }

  // Wait project is completely initialized
  public void waitProjectInitialization(String projectName) {
    waitItem(projectName);
    notificationsPopupPanel.waitPopupPanelsAreClosed();
    waitDefinedTypeOfFolder(projectName, PROJECT_FOLDER);
  }
}
