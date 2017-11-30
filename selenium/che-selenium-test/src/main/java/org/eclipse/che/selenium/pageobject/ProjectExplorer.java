/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.pageobject;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.EXPECTED_MESS_IN_CONSOLE_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOADER_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.MULTIPLE;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.WIDGET_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants;
import org.eclipse.che.selenium.core.constant.TestTimeoutsConstants;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
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
    String REIMPORT = "contextMenu/Maven/Reimport";
  }

  public interface FileWatcherExcludeOperations {
    String ADD_TO_FILE_WATCHER_EXCLUDES = "contextMenu/Add to File Watcher excludes";
    String REMOVE_FROM_FILE_WATCHER_EXCLUDES = "contextMenu/Remove from File Watcher excludes";
  }

  private interface Locators {
    String PROJECT_EXPLORER_TREE_ITEMS = "gwt-debug-projectTree";
    String EXPLORER_RIGHT_TAB_ID = "gwt-debug-partButton-Explorer";
    String CONTEXT_MENU_ID = "gwt-debug-contextMenu/newGroup";
    String GO_BACK_BUTTON = "gwt-debug-goBackButton";
    String COLLAPSE_ALL_BUTTON = "gwt-debug-collapseAllButton";
    String ALL_PROJECTS_XPATH = "//div[@path=@project]";
    String REFRESH_BUTTON_ID = "gwt-debug-refreshSelectedPath";
    String PROJECT_EXPLORER_TAB_IN_THE_LEFT_PANEL =
        "//div[@id='gwt-debug-leftPanel']//div[text()='Projects']";
  }

  public interface FolderTypes {
    String SIMPLE_FOLDER = "simpleFolder";
    String PROJECT_FOLDER = "projectFolder";
    String JAVA_SOURCE_FOLDER = "javaSourceFolder";
  }

  @FindBy(id = Locators.PROJECT_EXPLORER_TREE_ITEMS)
  WebElement projectExplorerTree;

  @FindBy(id = Locators.EXPLORER_RIGHT_TAB_ID)
  WebElement projectExplorerTab;

  @FindBy(id = Locators.GO_BACK_BUTTON)
  WebElement goBackBtn;

  @FindBy(id = Locators.COLLAPSE_ALL_BUTTON)
  WebElement collapseAllBtn;

  @FindBy(xpath = Locators.ALL_PROJECTS_XPATH)
  WebElement allProjects;

  @FindBy(id = Locators.REFRESH_BUTTON_ID)
  WebElement refreshProjectButton;

  @FindBy(xpath = Locators.PROJECT_EXPLORER_TAB_IN_THE_LEFT_PANEL)
  WebElement projectExplorerTabInTheLeftPanel;

  /**
   * @param path path to item in Project Explorer
   * @return item by path
   */
  private WebElement getProjectExplorerItem(String path) {
    return redrawUiElementsWait.until(
        ExpectedConditions.visibilityOfElementLocated(
            By.xpath(String.format("//div[@path='/%s']/div", path))));
  }

  /** wait appearance of the IDE Project Explorer */
  public void waitProjectExplorer() {
    new WebDriverWait(seleniumWebDriver, EXPECTED_MESS_IN_CONSOLE_SEC)
        .until(ExpectedConditions.visibilityOf(projectExplorerTree));
  }

  /** press on the project explorer tab */
  public void clickOnProjectExplorerTab() {
    redrawUiElementsWait.until(ExpectedConditions.visibilityOf(projectExplorerTab)).click();
  }

  /**
   * wait project explorer and click on empty area
   *
   * @param timeOutForWaiting defined timeout for appearance project explorer page
   */
  public void clickOnEmptyAreaOfProjectTree(int timeOutForWaiting) {
    waitProjectExplorer(timeOutForWaiting);
    projectExplorerTree.click();
  }

  /**
   * wait appearance of the IDE Project Explorer
   *
   * @param timeout user timeout
   */
  public void waitProjectExplorer(int timeout) {
    new WebDriverWait(seleniumWebDriver, timeout)
        .until(ExpectedConditions.visibilityOf(projectExplorerTree));
  }

  /**
   * wait item in project explorer with path (For Example in project with name:'Test' presents
   * folder 'src' and there is present locates file 'pom.xml' path will be next:'Test/src/pom.xml')
   * Note! in this method visibility is not checked
   *
   * @param path
   */
  public void waitItem(String path) {
    String locator = "//div[@path='/%s']/div";
    loader.waitOnClosed();
    new WebDriverWait(seleniumWebDriver, TestTimeoutsConstants.EXPECTED_MESS_IN_CONSOLE_SEC)
        .until(ExpectedConditions.presenceOfElementLocated(By.xpath(String.format(locator, path))));
    loader.waitOnClosed();
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
    String locator = "//div[@path='/" + path + "']/div";
    new WebDriverWait(seleniumWebDriver, timeout)
        .until(ExpectedConditions.presenceOfElementLocated(By.xpath(locator)));
  }

  /**
   * wait until library will be present in External Libraries
   *
   * @param libraryName name of library
   */
  public void waitLibraryIsPresent(String libraryName) {
    new WebDriverWait(seleniumWebDriver, WIDGET_TIMEOUT_SEC)
        .until(
            ExpectedConditions.presenceOfElementLocated(
                By.xpath(String.format("//div[@synthetic='true'and @name='%s']", libraryName))));
  }

  /**
   * wait until library will be present in External Libraries
   *
   * @param libraryName name of library
   */
  public void waitLibraryIsNotPresent(String libraryName) {
    new WebDriverWait(seleniumWebDriver, WIDGET_TIMEOUT_SEC)
        .until(
            ExpectedConditions.invisibilityOfElementLocated(
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
    String locator = "//div[@path='/" + path + "']//div[string-length(text()) > 0]";
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locator)));
  }

  /**
   * wait item will disappear in project explorer with path
   *
   * @param path path to item
   */
  public void waitItemIsDisappeared(String path) {
    String locator = "//div[@path='/" + path + "']/div";
    new WebDriverWait(seleniumWebDriver, LOADER_TIMEOUT_SEC)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(locator)));
  }

  public void waitYellowNode(String path) {
    redrawUiElementsWait.until(
        ExpectedConditions.visibilityOfElementLocated(
            By.xpath(
                String.format(
                    "//div[@id='gwt-debug-projectTree']//div[@path='/%s']/descendant::div[@style='%s']",
                    path, "color: #e0b91d;"))));
  }

  public void waitGreenNode(String path) {
    redrawUiElementsWait.until(
        ExpectedConditions.visibilityOfElementLocated(
            By.xpath(
                String.format(
                    "//div[@id='gwt-debug-projectTree']//div[@path='/%s']/descendant::div[@style='%s']",
                    path, "color: #72ad42;"))));
  }

  public void waitBlueNode(String path) {
    redrawUiElementsWait.until(
        ExpectedConditions.visibilityOfElementLocated(
            By.xpath(
                String.format(
                    "//div[@id='gwt-debug-projectTree']//div[@path='/%s']/descendant::div[@style='%s']",
                    path, "color: #3193d4;"))));
  }

  public void waitDefaultColorNode(String path) {
    redrawUiElementsWait.until(
        ExpectedConditions.visibilityOfElementLocated(
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
    String locator = String.format("//div[@id='gwt-debug-projectTree']//div[text()='%s']", item);
    loadPageTimeout.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locator)));
  }

  /**
   * wait item in project explorer tree area Note! Items must not repeat (for example: in a project
   * can be some folder. Into each folder can be a file with same name. This method will be track
   * only first item.)
   *
   * @param item
   */
  public void waitItemInVisibleArea(String item, final int timeOut) {
    new WebDriverWait(seleniumWebDriver, timeOut)
        .until(ExpectedConditions.visibilityOfElementLocated(By.name(item)));
  }

  /**
   * wait item is not in project explorer tree area Note! Items must not repeat (for example: in a
   * project can be some folder. Into each folder can be a file with same name. This method will be
   * track only first item.)
   *
   * @param item
   */
  public void waitItemIsNotPresentVisibleArea(String item) {
    loadPageTimeout.until(ExpectedConditions.invisibilityOfElementLocated(By.name(item)));
  }

  /**
   * select item in project explorer with path For Example in project with name:'Test' presents
   * folder 'src' and there is present locates file 'pom.xml' path will be next:'Test/src/pom.xml')
   *
   * @param path full path to project item
   */
  public void selectItem(String path) {
    String locator = "//div[@path='/" + path + "']/div";

    waitItem(path);
    try {
      new WebDriverWait(seleniumWebDriver, EXPECTED_MESS_IN_CONSOLE_SEC)
          .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locator)))
          .click();
    }
    // sometimes an element in the project explorer may not be attached to the DOM. We should
    // refresh all items.
    catch (StaleElementReferenceException ex) {
      LOG.warn(ex.getLocalizedMessage(), ex);

      waitProjectExplorer();
      clickOnRefreshTreeButton();
      waitItem(path);
      new WebDriverWait(seleniumWebDriver, EXPECTED_MESS_IN_CONSOLE_SEC)
          .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locator)))
          .click();
    }
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
    String locator = "//div[@path='/" + path + "']/div";
    waitItem(path, timeoutForWaitingItem);
    try {
      new WebDriverWait(seleniumWebDriver, EXPECTED_MESS_IN_CONSOLE_SEC)
          .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locator)))
          .click();
    }
    // sometimes an element in the project explorer may not be attached to the DOM. We should
    // refresh all items.
    catch (StaleElementReferenceException ex) {
      LOG.warn(ex.getLocalizedMessage(), ex);

      waitProjectExplorer();
      clickOnRefreshTreeButton();
      waitItem(path);
      new WebDriverWait(seleniumWebDriver, EXPECTED_MESS_IN_CONSOLE_SEC)
          .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locator)))
          .click();
    }
  }

  /**
   * select item in project explorer with 'name' attribute This is mean if will be two same files in
   * the different folders, will be selected file, that first in the DOM
   *
   * @param item
   */
  public void selectVisibleItem(String item) {
    String locator = "//div[@name='" + item + "']/div";
    redrawUiElementsWait
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locator)))
        .click();
  }

  /** wait external maven Libraries relative module */
  public void waitLibraries(String modulePath) {
    String locator = "//div [@name='External Libraries' and @project='/" + modulePath + "']";
    new WebDriverWait(seleniumWebDriver, 10)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locator)));
  }

  /** wait disappearance external maven Libraries relative module */
  public void waitLibrariesIsNotPresent(String modulePath) {
    String locator = "//div [@name='External Libraries' and @project='/" + modulePath + "']";
    new WebDriverWait(seleniumWebDriver, 10)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(locator)));
  }

  /** select external maven Library relative module */
  public void selectLibraries(String modulePath) {
    String locator = "//div [@name='External Libraries' and @project='/" + modulePath + "']";
    new WebDriverWait(seleniumWebDriver, 10)
        .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locator)))
        .click();
  }

  /**
   * select item in project explorer with path For Example in project with name:'Test' presents
   * folder 'src' and there is present locates file 'pom.xpl' path will be next:'Test/src/pom.xml')
   *
   * @param path
   */
  public void scrollAndselectItem(String path) {
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
    waitItem(path);
    selectItem(path);
    waitItemIsSelected(path);

    try {
      getProjectExplorerItem(path).click();
      waitItemIsSelected(path);
      action.moveToElement(getProjectExplorerItem(path)).perform();
      action.doubleClick().perform();
    }
    // sometimes an element in the project explorer may not be attached to the DOM. We should
    // refresh all items.
    catch (StaleElementReferenceException ex) {
      LOG.warn(ex.getLocalizedMessage(), ex);

      clickOnRefreshTreeButton();
      waitItem(path);
      getProjectExplorerItem(path).click();
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
    loader.waitOnClosed();
    waitItemInVisibleArea(visibleItem);
    String locator =
        String.format("//div[@id='gwt-debug-projectTree']//div[text()='%s']", visibleItem);
    WebElement item = seleniumWebDriver.findElement(By.xpath(locator));
    item.click();
    actionsFactory.createAction(seleniumWebDriver).doubleClick(item).perform();
    loader.waitOnClosed();
  }

  /**
   * open visible package by double click in the Project Explorer tree
   *
   * @param packageName
   */
  public void openVisiblePackage(String packageName) {
    String locator = "//div[text()='" + packageName + "']";
    loadPageTimeout.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locator)));
    WebElement visiblePacksge = seleniumWebDriver.findElement(By.xpath(locator));
    actionsFactory.createAction(seleniumWebDriver).doubleClick(visiblePacksge).perform();
  }

  /**
   * wait disappearance the item in codenvy project explorer
   *
   * @param pathToItem full path to item in the codenvy project explorer
   */
  public void waitDisappearItemByPath(String pathToItem) {
    String locator = "//div[@path='/" + pathToItem + "']/div";
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(locator)));
  }

  /**
   * wait defined folder by user Path
   *
   * @param pathToFolder the path to folder (fpr example: TestProject/src/main/test )
   * @param typeFolder type folder in java project may be simple folder, projectFolder and
   *     javaSource folder. We can use this types from ProjectExlorer.FolderTypes public interface.
   */
  public void waitFolderDefinedTypeOfFolderByPath(String pathToFolder, String typeFolder) {
    String locator =
        "//div[@path='/"
            + pathToFolder
            + "']/div/*[local-name() = 'svg' and @id='"
            + typeFolder
            + "']";
    loadPageTimeout.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locator)));
  }

  /**
   * wait removing item from DOM
   *
   * @param pathToItem full path to item in the codenvy project explorer
   */
  public void waitRemoveItemsByPath(String pathToItem) {
    String locator = "//div[@path='/" + pathToItem + "']/div";
    new WebDriverWait(seleniumWebDriver, 10)
        .until(
            ExpectedConditions.not(
                ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath(locator))));
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
        "//tr[@id='gwt-debug-contextMenu/newGroup']/parent::tbody");

    new WebDriverWait(seleniumWebDriver, WIDGET_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOfElementLocated(By.id(Locators.CONTEXT_MENU_ID)));
  }

  /**
   * click on element from context menu by name
   *
   * @param item
   */
  public void clickOnItemInContextMenu(String item) {
    loadPageTimeout.until(ExpectedConditions.visibilityOfElementLocated(By.id(item))).click();
  }

  /**
   * Click on item in 'New' menu
   *
   * @param item item form {@code SubMenuNew}
   */
  public void clickOnNewContextMenuItem(String item) {
    new WebDriverWait(seleniumWebDriver, 10)
        .until(ExpectedConditions.visibilityOf(seleniumWebDriver.findElement(By.id(item))))
        .click();

    waitContextMenuPopUpClosed();
  }

  public void waitContextMenuPopUpClosed() {
    loadPageTimeout.until(
        ExpectedConditions.invisibilityOfElementLocated(By.id(Locators.CONTEXT_MENU_ID)));
  }

  /**
   * perform the multi-select by Ctrl keys
   *
   * @param path is the path to the selected item
   */
  public void selectMultiFilesByCtrlKeys(String path) {
    Actions actions = actionsFactory.createAction(seleniumWebDriver);
    actions.keyDown(Keys.CONTROL).perform();
    selectItem(path);
    actions.keyUp(Keys.CONTROL).perform();
  }

  /** click on the 'collapse all' in the project explorer */
  public void clickCollapseAllButton() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.elementToBeClickable(collapseAllBtn));
    collapseAllBtn.click();
  }

  /** click on the 'go back' in the project explorer */
  public void clickGoBackButton() {
    loadPageTimeout.until(ExpectedConditions.elementToBeClickable(goBackBtn));
    goBackBtn.click();
  }

  /** launch the 'Refactor Rename' form by keyboard after select a package or Java class */
  public void launchRefactorByKeyboard() {
    loader.waitOnClosed();
    Actions action = actionsFactory.createAction(seleniumWebDriver);
    action.keyDown(Keys.SHIFT).sendKeys(Keys.F6).keyUp(Keys.SHIFT).perform();
  }

  /** launch the 'Refactor Move' form by keyboard after select a package or Java class */
  public void launchRefactorMoveByKeyboard() {
    loader.waitOnClosed();
    actionsFactory.createAction(seleniumWebDriver).sendKeys(Keys.F6).perform();
  }

  /**
   * Returns all names of projects from tree of the project explorer
   *
   * @return list of names
   */
  public List<String> getNamesAllProjects() {
    List<WebElement> projects =
        loadPageTimeout.until(
            ExpectedConditions.visibilityOfAllElementsLocatedBy(
                By.xpath(Locators.ALL_PROJECTS_XPATH)));
    return projects
        .stream()
        .map((webElement) -> webElement.getAttribute("name"))
        .collect(Collectors.toList());
  }

  /**
   * Returns all names of items from tree of the project explorer
   *
   * @return list of items
   */
  public List<String> getNamesOfAllOpenItems() {
    List<String> result = Arrays.asList(projectExplorerTree.getText().split("\n"));
    return result;
  }

  /** perform right arrow key pressed in a browser */
  public void sendToItemRightArrowKey() {
    actionsFactory.createAction(seleniumWebDriver).sendKeys(Keys.RIGHT.toString()).perform();
  }

  /** perform left arrow key pressed in a browser */
  public void sendToItemLeftArrowKey() {
    actionsFactory.createAction(seleniumWebDriver).sendKeys(Keys.LEFT.toString()).perform();
  }

  /** perform up arrow key pressed in a browser */
  public void sendToItemUpArrowKey() {
    actionsFactory.createAction(seleniumWebDriver).sendKeys(Keys.UP.toString()).perform();
  }

  /** perform down arrow key pressed in a browser */
  public void sendToItemDownArrowKey() {
    actionsFactory.createAction(seleniumWebDriver).sendKeys(Keys.ARROW_DOWN.toString()).perform();
  }

  /** perform enter key pressed in a browser */
  public void sendToItemEnterKey() {
    actionsFactory.createAction(seleniumWebDriver).sendKeys(Keys.ENTER.toString()).perform();
  }

  /** wait refresh button and click this one */
  public void clickOnRefreshTreeButton() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(ExpectedConditions.visibilityOf(refreshProjectButton))
        .click();
  }

  public void clickOnImportProjectLink(int userTimeout) {
    waitProjectExplorer();
    projectExplorerTree.findElement(By.xpath("//div[text()='Import Project...']")).click();
  }

  public void clickOnCreateProjectLink(int userTimeout) {
    waitProjectExplorer();
    projectExplorerTree.findElement(By.xpath("//div[text()='Create Project...']")).click();
  }

  public void clickOnProjectExplorerTabInTheLeftPanel() {
    redrawUiElementsWait
        .until(ExpectedConditions.visibilityOf(projectExplorerTabInTheLeftPanel))
        .click();
  }

  /** invoke command from context menu. Work for Common scope commands only */
  public void invokeCommandWithContextMenu(
      String commandsGoal, String pathToItem, String commandName) {
    String commandNameLocator = "//tr[@id[contains(.,'%s')]]";

    try {
      openContextMenuByPathSelectedItem(pathToItem);
    } catch (NoSuchElementException e) {
      clickOnRefreshTreeButton();
      openContextMenuByPathSelectedItem(
          pathToItem); // there context menu may not be opened from the first try
    }

    clickOnItemInContextMenu(TestProjectExplorerContextMenuConstants.COMMANDS);
    clickOnItemInContextMenu(commandsGoal);
    new WebDriverWait(seleniumWebDriver, MULTIPLE)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format(commandNameLocator, commandName))))
        .click();
  }

  public void invokeCommandWithContextMenu(
      String commandsGoal, String pathToItem, String commandName, String machineName) {
    String commandNameLocator = "//tr[@id[contains(.,'%s')]]";
    String machineNameLocator = "//select[@class = 'gwt-ListBox']//option[contains(.,'%s')]";
    selectItem(pathToItem);
    openContextMenuByPathSelectedItem(pathToItem);
    clickOnItemInContextMenu(TestProjectExplorerContextMenuConstants.COMMANDS);
    clickOnItemInContextMenu(commandsGoal);

    new WebDriverWait(seleniumWebDriver, TestTimeoutsConstants.MULTIPLE)
        .until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(String.format(commandNameLocator, commandName))))
        .click();
    loader.waitOnClosed();
    new Actions(seleniumWebDriver)
        .doubleClick(
            seleniumWebDriver.findElement(By.xpath(String.format(machineNameLocator, machineName))))
        .build()
        .perform();
  }

  public void scrollToItemByPath(String path) {
    waitItem(path);
    String locator = "//div[@path='/" + path + "']/div";
    WebElement item = seleniumWebDriver.findElement(By.xpath(locator));
    actionsFactory.createAction(seleniumWebDriver).moveToElement(item).perform();
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
    editor.waitActiveEditor();
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
    editor.waitActiveEditor();
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
      WaitUtils.sleepQuietly(1);
    }
    navigateToFile.waitListOfFilesNames(file);
    navigateToFile.selectFileByName(file);
    navigateToFile.waitFormToClose();
    menu.runCommand(
        TestMenuCommandsConstants.Edit.EDIT, TestMenuCommandsConstants.Edit.REVEAL_RESOURCE);
    waitItem(pathToFile);
  }

  /** Invokes special javascript function for 'quick' expand project(s) tree */
  public void quickExpandWithJavaScript() {
    String jsScript = "IDE.ProjectExplorer.expandAll();";
    JavascriptExecutor js = (JavascriptExecutor) seleniumWebDriver;
    js.executeScript(jsScript);
  }

  /**
   * Invokes special javascript function for 'quick' collapse all opened nodes of project(s) tree
   */
  public void quickCollapseJavaScript() {
    String jsScript = "IDE.ProjectExplorer.collapseAll()";
    JavascriptExecutor js = (JavascriptExecutor) seleniumWebDriver;
    js.executeScript(jsScript);
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
  }

  public void waitItemIsSelected(String path) {
    String locator =
        "//div[@path='/%s']/div[contains(concat(' ', normalize-space(@class), ' '), ' selected')]";
    new WebDriverWait(seleniumWebDriver, TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC)
        .until(ExpectedConditions.presenceOfElementLocated(By.xpath(String.format(locator, path))));
  }
}
