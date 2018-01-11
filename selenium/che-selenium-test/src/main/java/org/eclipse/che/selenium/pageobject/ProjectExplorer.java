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
package org.eclipse.che.selenium.pageobject;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.COMMANDS;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.EXPECTED_MESS_IN_CONSOLE_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOADER_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.WIDGET_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.utils.WaitUtils.sleepQuietly;
import static org.eclipse.che.selenium.pageobject.ProjectExplorer.Locators.ALL_PROJECTS_XPATH;
import static org.eclipse.che.selenium.pageobject.ProjectExplorer.Locators.CONTEXT_MENU_ID;
import static org.eclipse.che.selenium.pageobject.ProjectExplorer.Locators.EXPLORER_RIGHT_TAB_ID;
import static org.eclipse.che.selenium.pageobject.ProjectExplorer.Locators.PROJECT_EXPLORER_TAB_IN_THE_LEFT_PANEL;
import static org.eclipse.che.selenium.pageobject.ProjectExplorer.Locators.PROJECT_EXPLORER_TREE_ITEMS;
import static org.eclipse.che.selenium.pageobject.ProjectExplorer.ProjectExplorerOptionsMenuItem.COLLAPSE_ALL;
import static org.eclipse.che.selenium.pageobject.ProjectExplorer.ProjectExplorerOptionsMenuItem.REFRESH_MAIN;
import static org.eclipse.che.selenium.pageobject.ProjectExplorer.ProjectExplorerOptionsMenuItem.REVEAL_RESOURCE;
import static org.openqa.selenium.Keys.ARROW_DOWN;
import static org.openqa.selenium.Keys.CONTROL;
import static org.openqa.selenium.Keys.ENTER;
import static org.openqa.selenium.Keys.F6;
import static org.openqa.selenium.Keys.LEFT;
import static org.openqa.selenium.Keys.RIGHT;
import static org.openqa.selenium.Keys.SHIFT;
import static org.openqa.selenium.Keys.UP;
import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.not;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfAllElementsLocatedBy;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfNestedElementLocatedBy;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfAllElementsLocatedBy;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Zaryana Dombrovskaya */
@Singleton
public class ProjectExplorer {

  private static final Logger LOG = LoggerFactory.getLogger(ProjectExplorer.class);

  private final SeleniumWebDriver seleniumWebDriver;
  private final Loader loader;
  private final ActionsFactory actionsFactory;
  private final NavigateToFile navigateToFile;
  private final Menu menu;
  private final CodenvyEditor editor;
  private final TestWebElementRenderChecker testWebElementRenderChecker;
  private WebDriverWait loadPageTimeout;
  private WebDriverWait redrawUiElementsWait;

  @Inject
  public ProjectExplorer(
      SeleniumWebDriver seleniumWebDriver,
      Loader loader,
      ActionsFactory actionsFactory,
      NavigateToFile navigateToFile,
      Menu menu,
      CodenvyEditor editor,
      TestWebElementRenderChecker testWebElementRenderChecker) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.loader = loader;
    this.actionsFactory = actionsFactory;
    this.navigateToFile = navigateToFile;
    this.menu = menu;
    this.editor = editor;
    this.testWebElementRenderChecker = testWebElementRenderChecker;
    loadPageTimeout = new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC);
    redrawUiElementsWait = new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
    PageFactory.initElements(seleniumWebDriver, this);
  }

  public interface CommandsGoal {
    String COMMON = "gwt-debug-contextMenu/Commands/goal_Common";
    String BUILD = "gwt-debug-contextMenu/Commands/goal_Build";
    String RUN = "gwt-debug-contextMenu/Commands/goal_Run";
  }

  public interface PROJECT_EXPLORER_CONTEXT_MENU_MAVEN {
    String REIMPORT = "gwt-debug-contextMenu/Maven/reimportMavenDependenciesAction";
  }

  public interface FileWatcherExcludeOperations {
    String ADD_TO_FILE_WATCHER_EXCLUDES = "gwt-debug-contextMenu/Add to File Watcher excludes";
    String REMOVE_FROM_FILE_WATCHER_EXCLUDES =
        "gwt-debug-contextMenu/Remove from File Watcher excludes";
  }

  protected interface Locators {
    String PROJECT_EXPLORER_TREE_ITEMS = "gwt-debug-projectTree";
    String EXPLORER_RIGHT_TAB_ID = "gwt-debug-partButton-Explorer";
    String CONTEXT_MENU_ID = "gwt-debug-contextMenu/newGroup";
    String GO_BACK_BUTTON = "gwt-debug-goBackButton";
    String COLLAPSE_ALL_BUTTON = "gwt-debug-collapseAllButton";
    String ALL_PROJECTS_XPATH = "//div[@path=@project]";
    String MENU_BUTTON_PART_STACK_ID = "gwt-debug-menuButton";
    String REFRESH_CONTEXT_MENU_ID = "gwt-debug-contextMenu/refreshPathAction";
    String PROJECT_EXPLORER_TAB_IN_THE_LEFT_PANEL =
        "//div[@id='gwt-debug-navPanel']//div[@id='gwt-debug-partButton-Projects']";
  }

  public interface FolderTypes {
    String SIMPLE_FOLDER = "simpleFolder";
    String PROJECT_FOLDER = "projectFolder";
    String JAVA_SOURCE_FOLDER = "javaSourceFolder";
  }

  public interface ProjectExplorerOptionsMenuItem {
    String MAXIMIZE = "gwt-debug-contextMenu/Maximize";
    String HIDE = "gwt-debug-contextMenu/Hide";
    String COLLAPSE_ALL = "gwt-debug-contextMenu/collapseAll";
    String REVEAL_RESOURCE = "gwt-debug-contextMenu/revealResourceInProjectTree";
    String REFRESH_MAIN = "gwt-debug-contextMenu/refreshPathAction";
    String LINK_WITH_EDITOR = "gwt-debug-contextMenu/linkWithEditor";
  }

  @FindBy(id = PROJECT_EXPLORER_TREE_ITEMS)
  WebElement projectExplorerTree;

  @FindBy(id = EXPLORER_RIGHT_TAB_ID)
  WebElement projectExplorerTab;

  @FindBy(id = Locators.GO_BACK_BUTTON)
  WebElement goBackBtn;

  @FindBy(id = Locators.COLLAPSE_ALL_BUTTON)
  WebElement collapseAllBtn;

  @FindBy(xpath = ALL_PROJECTS_XPATH)
  WebElement allProjects;

  @FindBy(id = Locators.MENU_BUTTON_PART_STACK_ID)
  WebElement refreshButtonPartStack;

  @FindBy(id = Locators.REFRESH_CONTEXT_MENU_ID)
  WebElement refreshContextMenuItem;

  @FindBy(xpath = PROJECT_EXPLORER_TAB_IN_THE_LEFT_PANEL)
  WebElement projectExplorerTabInTheLeftPanel;

  /**
   * @param path path to item in Project Explorer
   * @param timeout timeout in seconds
   * @return found WebElement
   */
  private WebElement getProjectExplorerItem(String path, int timeout) {
    return new WebDriverWait(seleniumWebDriver, timeout)
        .until(visibilityOfElementLocated(By.xpath(String.format("//div[@path='/%s']/div", path))));
  }

  /**
   * @param path path to item in Project Explorer
   * @return item by path
   */
  private WebElement getProjectExplorerItem(String path) {
    return getProjectExplorerItem(path, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
  }

  public void clickOnProjectExplorerOptionsButton() {
    redrawUiElementsWait
        .until(
            visibilityOfElementLocated(
                By.xpath(
                    "//div[@id='gwt-debug-navPanel']//div[@name='workBenchIconPartStackOptions']")))
        .click();
  }

  public void clickOnOptionsMenuItem(String menuID) {
    redrawUiElementsWait
        .until(visibilityOfElementLocated(By.xpath(String.format("//tr[@id='%s']", menuID))))
        .click();
  }

  /** Clicks on project explorer tab and waits its appearance. */
  public void openPanel() {
    clickOnProjectExplorerTabInTheLeftPanel();
    waitProjectExplorer();
  }

  /** press on the project explorer tab */
  public void clickOnProjectExplorerTab() {
    redrawUiElementsWait.until(visibilityOfElementLocated(By.id(EXPLORER_RIGHT_TAB_ID))).click();
  }

  /**
   * wait project explorer and click on empty area
   *
   * @param timeOutForWaiting defined timeout for appearance project explorer page
   */
  public void clickOnEmptyAreaOfProjectTree(int timeOutForWaiting) {
    waitProjectExplorer(timeOutForWaiting);
    loadPageTimeout.until(visibilityOf(projectExplorerTree)).click();
  }

  /** wait appearance of the IDE Project Explorer */
  public void waitProjectExplorer() {
    waitProjectExplorer(EXPECTED_MESS_IN_CONSOLE_SEC);
  }

  /**
   * wait appearance of the IDE Project Explorer
   *
   * @param timeout user timeout
   */
  public void waitProjectExplorer(int timeout) {
    new WebDriverWait(seleniumWebDriver, timeout)
        .until(visibilityOfElementLocated(By.id(PROJECT_EXPLORER_TREE_ITEMS)));
  }

  /**
   * wait item in project explorer with path (For Example in project with name:'Test' presents
   * folder 'src' and there is present locates file 'pom.xml' path will be next:'Test/src/pom.xml')
   * Note! in this method visibility is not checked
   *
   * @param path
   */
  public void waitItem(String path) {
    waitItem(path, EXPECTED_MESS_IN_CONSOLE_SEC);
  }

  /**
   * wait item in project explorer with customized timeout (For Example in project with name:'Test'
   * presents folder 'src' and there is present locates file 'pom.xpl' path will be
   * next:'Test/src/pom.xml')
   *
   * @param path
   * @param timeout user timeout
   */
  public void waitItem(String path, int timeout) {
    loader.waitOnClosed();
    new WebDriverWait(seleniumWebDriver, timeout)
        .until(visibilityOfElementLocated(By.xpath(String.format("//div[@path='/%s']/div", path))));
  }

  /**
   * wait until library will be present in External Libraries
   *
   * @param libraryName name of library
   */
  public void waitLibraryIsPresent(String libraryName) {
    new WebDriverWait(seleniumWebDriver, WIDGET_TIMEOUT_SEC)
        .until(
            visibilityOfElementLocated(
                By.xpath(String.format("//div[@synthetic='true'and @name='%s']", libraryName))));
  }

  /**
   * wait until library will be not present in External Libraries
   *
   * @param libraryName name of library
   */
  public void waitLibraryIsNotPresent(String libraryName) {
    new WebDriverWait(seleniumWebDriver, WIDGET_TIMEOUT_SEC)
        .until(
            invisibilityOfElementLocated(
                By.xpath(String.format("//div[@synthetic='true'and @name='%s']", libraryName))));
  }

  /**
   * wait visible item in project explorer with path (For Example in project with name:'Test'
   * presents folder 'src' and there is present locates file 'pom.xml' path will be
   * next:'Test/src/pom.xml')
   *
   * <p>With [string-length(text()) > 0] we will be sure 'div' not empty and contains some text here
   * we suggest it will be name of node but for now we don't check name to equals
   *
   * @param path
   */
  public void waitVisibleItem(String path) {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(
            visibilityOfElementLocated(
                By.xpath("//div[@path='/" + path + "']//div[string-length(text()) > 0]")));
  }

  /**
   * wait item will disappear in project explorer with path
   *
   * @param path path to item
   */
  public void waitItemIsDisappeared(String path) {
    new WebDriverWait(seleniumWebDriver, LOADER_TIMEOUT_SEC)
        .until(
            invisibilityOfElementLocated(By.xpath(String.format("//div[@path='/%s']/div", path))));
  }

  public void waitYellowNode(String path) {
    loadPageTimeout.until(
        visibilityOfElementLocated(
            By.xpath(
                String.format(
                    "//div[@id='gwt-debug-projectTree']//div[@path='/%s']/descendant::div[@style='%s']",
                    path, "color: #e0b91d;"))));
  }

  public void waitGreenNode(String path) {
    loadPageTimeout.until(
        visibilityOfElementLocated(
            By.xpath(
                String.format(
                    "//div[@id='gwt-debug-projectTree']//div[@path='/%s']/descendant::div[@style='%s']",
                    path, "color: #72ad42;"))));
  }

  public void waitBlueNode(String path) {
    loadPageTimeout.until(
        visibilityOfElementLocated(
            By.xpath(
                String.format(
                    "//div[@id='gwt-debug-projectTree']//div[@path='/%s']/descendant::div[@style='%s']",
                    path, "color: #3193d4;"))));
  }

  public void waitDefaultColorNode(String path) {
    loadPageTimeout.until(
        visibilityOfElementLocated(
            By.xpath(
                String.format(
                    "//div[@id='gwt-debug-projectTree']//div[@path='/%s']/descendant::div[@style='%s']",
                    path, "opacity:1;"))));
  }

  /**
   * wait item in project explorer tree area Note! Items must not repeat (for example: in a project
   * can be some folder. Into each folder can be a file with same name. This method will be track
   * only first item.)
   *
   * @param item
   */
  public void waitItemInVisibleArea(String item) {
    waitItemInVisibleArea(item, LOAD_PAGE_TIMEOUT_SEC);
  }

  /**
   * wait item in project explorer tree area Note! Items must not repeat (for example: in a project
   * can be some folder. Into each folder can be a file with same name. This method will be track
   * only first item.)
   *
   * @param item
   */
  public void waitItemInVisibleArea(String item, final int timeOut) {
    loader.waitOnClosed();
    new WebDriverWait(seleniumWebDriver, timeOut)
        .until(
            visibilityOfElementLocated(
                By.xpath(
                    String.format("//div[@id='gwt-debug-projectTree']//div[text()='%s']", item))));
  }

  /**
   * get web element by his visible name
   *
   * @param item visible name
   * @return found WebElement
   */
  private WebElement getVisibleElement(String item) {
    return new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(
            visibilityOfElementLocated(
                By.xpath(
                    String.format("//div[@id='gwt-debug-projectTree']//div[text()='%s']", item))));
  }

  /**
   * wait item is not in project explorer tree area Note! Items must not repeat (for example: in a
   * project can be some folder. Into each folder can be a file with same name. This method will be
   * track only first item.)
   *
   * @param item visible name
   */
  public void waitItemIsNotPresentVisibleArea(String item) {
    loadPageTimeout.until(invisibilityOfElementLocated(By.name(item)));
  }

  /**
   * select item in project explorer with path For Example in project with name:'Test' presents
   * folder 'src' and there is present locates file 'pom.xml' path will be next:'Test/src/pom.xml')
   *
   * @param path full path to project item
   */
  public void selectItem(String path) {
    selectItem(path, EXPECTED_MESS_IN_CONSOLE_SEC);
  }

  /**
   * select item in project explorer with path, with user timeout for waiting of the item For
   * Example in project with name:'Test' presents folder 'src' and there is present locates file
   * 'pom.xpl' path will be next:'Test/src/pom.xml')
   *
   * @param path full path to project item
   * @param timeoutForWaitingItem user timeout for waiting expected item in the Project Explorer
   *     tree
   */
  public void selectItem(String path, int timeoutForWaitingItem) {
    waitItem(path);
    try {
      getProjectExplorerItem(path, timeoutForWaitingItem).click();
    }
    // sometimes an element in the project explorer may not be attached to the DOM. We should
    // refresh all items.
    catch (StaleElementReferenceException ex) {
      LOG.debug(ex.getLocalizedMessage(), ex);

      waitProjectExplorer();
      clickOnRefreshTreeButton();
      waitItem(path);
      getProjectExplorerItem(path, EXPECTED_MESS_IN_CONSOLE_SEC).click();
    }
  }

  /**
   * select item in project explorer with 'name' attribute This is mean if will be two same files in
   * the different folders, will be selected file, that first in the DOM
   *
   * @param item
   */
  public void selectVisibleItem(String item) {
    getVisibleElement(item).click();
  }

  /** wait external maven Libraries relative module */
  public WebElement waitLibraries(String modulePath) {
    return loadPageTimeout.until(
        visibilityOfElementLocated(
            By.xpath(
                String.format(
                    "//div [@name='External Libraries' and @project='/%s']", modulePath))));
  }

  /** wait disappearance external maven Libraries relative module */
  public void waitLibrariesIsNotPresent(String modulePath) {
    loadPageTimeout.until(
        invisibilityOfElementLocated(
            By.xpath(
                String.format(
                    "//div [@name='External Libraries' and @project='/%s']", modulePath))));
  }

  /** select external maven Library relative module */
  public void selectLibraries(String modulePath) {
    waitLibraries(modulePath).click();
  }

  /**
   * select item in project explorer with path For Example in project with name:'Test' presents
   * folder 'src' and there is present locates file 'pom.xpl' path will be next:'Test/src/pom.xml')
   *
   * @param path
   */
  public void scrollAndSelectItem(String path) {
    waitItem(path);
    actionsFactory
        .createAction(seleniumWebDriver)
        .moveToElement(getProjectExplorerItem(path))
        .click()
        .perform();
  }

  /**
   * open a item using full path of root folder the project
   *
   * @param path
   */
  public void openItemByPath(String path) {
    Actions action = actionsFactory.createAction(seleniumWebDriver);
    selectItem(path);
    waitItemIsSelected(path);

    try {
      action.moveToElement(getProjectExplorerItem(path)).perform();
      action.doubleClick().perform();
    }
    // sometimes an element in the project explorer may not be attached to the DOM. We should
    // refresh all items.
    catch (StaleElementReferenceException ex) {
      LOG.debug(ex.getLocalizedMessage(), ex);

      clickOnRefreshTreeButton();
      selectItem(path);
      waitItemIsSelected(path);
      action.moveToElement(getProjectExplorerItem(path)).perform();
      action.doubleClick().perform();
    }
    loader.waitOnClosed();
  }

  /**
   * open item with double click in visible area (this mean if we have in project explorer several
   * items with same name: in folder /webapp and in folder /src. And the folders are opened, will be
   * opened first item). If we need open item in concrete folder use method openItemByPath
   *
   * @param visibleItem
   */
  public void openItemByVisibleNameInExplorer(String visibleItem) {
    getVisibleElement(visibleItem).click();
    actionsFactory
        .createAction(seleniumWebDriver)
        .moveToElement(getVisibleElement(visibleItem))
        .doubleClick()
        .perform();
    loader.waitOnClosed();
  }

  /**
   * open visible package by double click in the Project Explorer tree
   *
   * @param packageName
   */
  public void openVisiblePackage(String packageName) {
    actionsFactory
        .createAction(seleniumWebDriver)
        .doubleClick(getVisibleElement(packageName))
        .perform();
  }

  /**
   * wait disappearance the item in codenvy project explorer
   *
   * @param pathToItem full path to item in the codenvy project explorer
   */
  public void waitDisappearItemByPath(String pathToItem) {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(
            invisibilityOfElementLocated(
                By.xpath(String.format("//div[@path='/%s']/div", pathToItem))));
  }

  /**
   * wait defined folder by user Path
   *
   * @param pathToFolder the path to folder (fpr example: TestProject/src/main/test )
   * @param typeFolder type folder in java project may be simple folder, projectFolder and
   *     javaSource folder. We can use this types from ProjectExlorer.FolderTypes public interface.
   */
  public void waitFolderDefinedTypeOfFolderByPath(String pathToFolder, String typeFolder) {
    loader.waitOnClosed();
    loadPageTimeout.until(
        visibilityOfElementLocated(
            By.xpath(
                String.format(
                    "//div[@path='/%s']/div/*[local-name() = 'svg' and @id='%s']",
                    pathToFolder, typeFolder))));
  }

  /**
   * wait removing item from DOM
   *
   * @param pathToItem full path to item in the codenvy project explorer
   */
  public void waitRemoveItemsByPath(String pathToItem) {
    loadPageTimeout.until(
        not(
            presenceOfAllElementsLocatedBy(
                By.xpath(String.format("//div[@path='/%s']/div", pathToItem)))));
  }

  /**
   * context menu on item with specified name
   *
   * @param path
   */
  public void openContextMenuByPathSelectedItem(String path) {
    waitItem(path);
    selectItem(path);
    waitItemIsSelected(path);

    Actions action = actionsFactory.createAction(seleniumWebDriver);
    action.moveToElement(getProjectExplorerItem(path)).contextClick().perform();

    waitContextMenu();
  }

  /** wait for context menu. */
  public void waitContextMenu() {
    testWebElementRenderChecker.waitElementIsRendered(
        By.xpath("//tr[@id='gwt-debug-contextMenu/newGroup']/parent::tbody"));

    new WebDriverWait(seleniumWebDriver, WIDGET_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.id(CONTEXT_MENU_ID)));
  }

  /**
   * click on element from context menu by name
   *
   * @param item
   */
  public void clickOnItemInContextMenu(String item) {
    loadPageTimeout
        .until(
            visibilityOfElementLocated(
                By.xpath(String.format("//tr[@item-enabled='true' and @id='%s']", item))))
        .click();
  }

  /**
   * Click on item in 'New' menu
   *
   * @param item item form {@code SubMenuNew}
   */
  public void clickOnNewContextMenuItem(String item) {
    loadPageTimeout
        .until(
            visibilityOfElementLocated(
                By.xpath(String.format("//tr[@item-enabled='true' and @id='%s']", item))))
        .click();

    waitContextMenuPopUpClosed();
  }

  public void waitContextMenuPopUpClosed() {
    loadPageTimeout.until(invisibilityOfElementLocated(By.id(CONTEXT_MENU_ID)));
  }

  /**
   * perform the multi-select by Ctrl keys
   *
   * @param path is the path to the selected item
   */
  public void selectMultiFilesByCtrlKeys(String path) {
    Actions actions = actionsFactory.createAction(seleniumWebDriver);
    actions.keyDown(CONTROL).perform();
    selectItem(path);
    waitItemIsSelected(path);
    actions.keyUp(CONTROL).perform();
  }

  /** Click on the 'Reveal in project explorer' in 'Options'menu */
  public void revealResourceByOptionsButton() {
    clickOnProjectExplorerOptionsButton();
    clickOnOptionsMenuItem(REVEAL_RESOURCE);
  }

  /**
   * perform the multi-select by Shift key
   *
   * @param path is the path to the selected item
   */
  public void selectMultiFilesByShiftKey(String path) {
    Actions actions = actionsFactory.createAction(seleniumWebDriver);
    actions.keyDown(SHIFT).perform();
    selectItem(path);
    waitItemIsSelected(path);
    actions.keyUp(Keys.SHIFT).perform();
  }

  /**
   * perform the multi-select by Shift key with check selected items
   *
   * @param clickItemPath is the path to the selected item
   * @param selectedItemsPaths is the paths to the each selected items after click with shift button
   */
  public void selectMultiFilesByShiftKeyWithCheckMultiselection(
      String clickItemPath, List<String> selectedItemsPaths) {
    Actions actions = actionsFactory.createAction(seleniumWebDriver);
    actions.keyDown(SHIFT).perform();
    selectItem(clickItemPath);
    waitAllItemsIsSelected(selectedItemsPaths);
    actions.keyUp(SHIFT).perform();
  }

  /** click on the 'collapse all' in the project explorer */
  public void collapseProjectTreeByOptionsButton() {
    clickOnProjectExplorerOptionsButton();
    clickOnOptionsMenuItem(COLLAPSE_ALL);
  }

  /** click on the 'go back' in the project explorer */
  public void clickGoBackButton() {
    loadPageTimeout.until(visibilityOf(goBackBtn)).click();
  }

  /** launch the 'Refactor Rename' form by keyboard after select a package or Java class */
  public void launchRefactorByKeyboard() {
    loader.waitOnClosed();
    actionsFactory
        .createAction(seleniumWebDriver)
        .keyDown(SHIFT)
        .sendKeys(F6)
        .keyUp(SHIFT)
        .perform();
  }

  /** launch the 'Refactor Move' form by keyboard after select a package or Java class */
  public void launchRefactorMoveByKeyboard() {
    loader.waitOnClosed();
    actionsFactory.createAction(seleniumWebDriver).sendKeys(F6).perform();
  }

  /**
   * Returns all names of projects from tree of the project explorer
   *
   * @return list of names
   */
  public List<String> getNamesAllProjects() {
    return loadPageTimeout
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
    return asList(projectExplorerTree.getText().split("\n"));
  }

  /** perform right arrow key pressed in a browser */
  public void sendToItemRightArrowKey() {
    actionsFactory.createAction(seleniumWebDriver).sendKeys(RIGHT.toString()).perform();
  }

  /** perform left arrow key pressed in a browser */
  public void sendToItemLeftArrowKey() {
    actionsFactory.createAction(seleniumWebDriver).sendKeys(LEFT.toString()).perform();
  }

  /** perform up arrow key pressed in a browser */
  public void sendToItemUpArrowKey() {
    actionsFactory.createAction(seleniumWebDriver).sendKeys(UP.toString()).perform();
  }

  /** perform down arrow key pressed in a browser */
  public void sendToItemDownArrowKey() {
    actionsFactory.createAction(seleniumWebDriver).sendKeys(ARROW_DOWN.toString()).perform();
  }

  /** perform enter key pressed in a browser */
  public void sendToItemEnterKey() {
    actionsFactory.createAction(seleniumWebDriver).sendKeys(ENTER.toString()).perform();
  }

  /** wait refresh button and click this one */
  public void clickOnRefreshTreeButton() {
    clickOnProjectExplorerOptionsButton();
    clickOnOptionsMenuItem(REFRESH_MAIN);
  }

  public void clickOnImportProjectLink(int userTimeout) {
    waitProjectExplorer();
    WebElement importProjectLink =
        new WebDriverWait(seleniumWebDriver, userTimeout)
            .until(
                presenceOfNestedElementLocatedBy(
                    projectExplorerTree, By.xpath("//div[text()='Import Project...']")));

    loadPageTimeout.until(visibilityOf(importProjectLink)).click();
  }

  public void clickOnCreateProjectLink(int userTimeout) {
    waitProjectExplorer();
    WebElement createProjectLink =
        new WebDriverWait(seleniumWebDriver, userTimeout)
            .until(
                presenceOfNestedElementLocatedBy(
                    projectExplorerTree, By.xpath("//div[text()='Create Project...']")));

    loadPageTimeout.until(visibilityOf(createProjectLink)).click();
  }

  public void clickOnProjectExplorerTabInTheLeftPanel() {
    redrawUiElementsWait
        .until(visibilityOfElementLocated(By.xpath(PROJECT_EXPLORER_TAB_IN_THE_LEFT_PANEL)))
        .click();
  }

  /** invoke command from context menu. Work for Common scope commands only */
  public void invokeCommandWithContextMenu(
      String commandsGoal, String pathToItem, String commandName) {
    try {
      openContextMenuByPathSelectedItem(pathToItem);
    } catch (NoSuchElementException e) {
      clickOnRefreshTreeButton();
      openContextMenuByPathSelectedItem(
          pathToItem); // there context menu may not be opened from the first try
    }

    clickOnItemInContextMenu(COMMANDS);
    clickOnItemInContextMenu(commandsGoal);
    redrawUiElementsWait
        .until(
            visibilityOfElementLocated(
                By.xpath(String.format("//tr[@id[contains(.,'%s')]]", commandName))))
        .click();
  }

  public void invokeCommandWithContextMenu(
      String commandsGoal, String pathToItem, String commandName, String machineName) {
    invokeCommandWithContextMenu(commandsGoal, pathToItem, commandName);

    loader.waitOnClosed();
    actionsFactory
        .createAction(seleniumWebDriver)
        .doubleClick(
            seleniumWebDriver.findElement(
                By.xpath(
                    String.format(
                        "//select[@class = 'gwt-ListBox']//option[contains(.,'%s')]",
                        machineName))))
        .perform();
  }

  public void scrollToItemByPath(String path) {
    waitItem(path);
    actionsFactory
        .createAction(seleniumWebDriver)
        .moveToElement(getProjectExplorerItem(path))
        .perform();
  }

  /**
   * Expands specified path in project explorer Path given in format like like below:
   * TestProject/src/main/java/org.eclipse.che.examples
   *
   * @param path path for expand
   */
  public void expandPathInProjectExplorer(String path) {
    String[] pathItems = path.split("/");
    String currentItem = pathItems[0].replace('.', '/');
    String currentPath = currentItem;

    waitItem(currentPath);
    openItemByPath(currentPath);
    for (int i = 1; i < pathItems.length; i++) {
      currentItem = pathItems[i].replace('.', '/');
      currentPath += "/" + currentItem;
      waitItem(currentPath);
      loader.waitOnClosed();
      openItemByPath(currentPath);
    }
  }

  /**
   * Expands specified path in project explorer from defined item (will be try to open from defined
   * item in path) Path given in format like like below:
   * TestProject/src/main/java/org.eclipse.che.examples
   *
   * @param path path for expand
   * @param numItem
   */
  public void expandPathInProjectExplorer(String path, int numItem) {
    String[] pathItems = path.split("/");
    String currentItem = "";
    for (int i = 0; i < numItem + 1; i++) {
      currentItem += "/" + pathItems[i].replace('.', '/');
    }
    String currentPath = currentItem.substring(1);
    openItemByPath(currentPath);
    for (int i = numItem + 1; i < pathItems.length; i++) {
      currentItem = pathItems[i].replace('.', '/');
      currentPath += "/" + currentItem;
      waitItem(currentPath);
      loader.waitOnClosed();
      openItemByPath(currentPath);
    }
  }

  /**
   * Expands specified path in project explorer and open a file Path given in format like like
   * below: TestProject/src/main/java/org.eclipse.che.examples
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
   * Expands specified path in project explorer from defined item (will be try to open from defined
   * item in path) and open a file Path given in format like like below:
   * TestProject/src/main/java/org.eclipse.che.examples
   *
   * @param path path for expand
   * @param numItem number of node from will be opened nex item
   * @param fileName
   */
  public void expandPathInProjectExplorerAndOpenFile(String path, int numItem, String fileName) {
    expandPathInProjectExplorer(path, numItem);
    String pathToFile = path.replace('.', '/') + '/' + fileName;
    waitItem(pathToFile);
    openItemByPath(pathToFile);
    editor.waitActive();
  }

  /**
   * Finds a file by name using Navigate to file feature. Expand the tree using Reveal resource
   * feature
   *
   * @param file file for serch with Navigate to file feature.
   * @param pathToFile
   */
  public void expandToFileWithRevealResource(String file, String pathToFile) {
    loader.waitOnClosed();
    navigateToFile.waitFormToOpen();
    loader.waitOnClosed();
    navigateToFile.typeSymbolInFileNameField(file);
    try {
      navigateToFile.waitFileNamePopUp();
    } catch (StaleElementReferenceException ex) {
      sleepQuietly(1);
    }
    navigateToFile.isFilenameSuggested(file);
    navigateToFile.selectFileByName(file);
    navigateToFile.waitFormToClose();
    revealResourceByOptionsButton();
    waitItem(pathToFile);
  }

  /** Invokes special javascript function for 'quick' expand project(s) tree */
  public void quickExpandWithJavaScript() {
    String jsScript = "IDE.ProjectExplorer.expandAll();";
    JavascriptExecutor js = (JavascriptExecutor) seleniumWebDriver;
    js.executeScript(jsScript);
    loader.waitOnClosed();
  }

  /**
   * Invokes special javascript function for 'quick' collapse all opened nodes of project(s) tree
   */
  public void quickCollapseJavaScript() {
    String jsScript = "IDE.ProjectExplorer.collapseAll()";
    JavascriptExecutor js = (JavascriptExecutor) seleniumWebDriver;
    js.executeScript(jsScript);
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
    JavascriptExecutor js = (JavascriptExecutor) seleniumWebDriver;
    js.executeScript(jsScript);
    loader.waitOnClosed();
  }

  public void waitItemIsSelected(String path) {
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(
            visibilityOfElementLocated(
                By.xpath(
                    String.format(
                        "//div[@path='/%s']/div[contains(concat(' ', normalize-space(@class), ' '), ' selected')]",
                        path))));
  }

  public void waitAllItemsIsSelected(List<String> paths) {
    paths.forEach(this::waitItemIsSelected);
  }
}
