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
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.EXPECTED_MESS_IN_CONSOLE_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOADER_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.PREPARING_WS_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.REDRAW_UI_ELEMENTS_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces.Locators.ERROR_NOTIFICATION_MESSAGE_XPATH;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces.Locators.PROGRESS_BAR_XPATH;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces.Locators.WORKSPACE_ITEM_RAM;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces.Locators.WORKSPACE_ITEM_STOP_START_WORKSPACE_BUTTON;
import static org.openqa.selenium.support.ui.ExpectedConditions.*;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.core.webdriver.WebDriverWaitFactory;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

@Singleton
public class Workspaces {
  private final SeleniumWebDriver seleniumWebDriver;
  private final WebDriverWait redrawUiElementsTimeout;
  private final Dashboard dashboard;
  private final SeleniumWebDriverHelper seleniumWebDriverHelper;
  private final WebDriverWaitFactory webDriverWaitFactory;

  @Inject
  public Workspaces(
      SeleniumWebDriver seleniumWebDriver,
      Dashboard dashboard,
      SeleniumWebDriverHelper seleniumWebDriverHelper,
      WebDriverWaitFactory webDriverWaitFactory) {
    this.seleniumWebDriver = seleniumWebDriver;
    this.redrawUiElementsTimeout =
        new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC);
    this.dashboard = dashboard;
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    this.webDriverWaitFactory = webDriverWaitFactory;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  public interface Locators {
    String TOOLBAR = "Workspaces";
    String DOCUMENTATION_LINK = "//div[@che-link-title='Learn more.']/a";
    String ADD_WORKSPACE_BTN = "add-item-button";
    String DELETE_WORKSPACE_BTN = "delete-item-button";
    String DELETE_DIALOG_BUTTON = "//md-dialog[@role='dialog']//button/span[text()='Delete']";
    String BULK_CHECKBOX = "//md-checkbox[@aria-label='Workspace list']";
    String SEARCH_WORKSPACE_FIELD = "//input[@ng-placeholder='Search']";
    String NO_WORKSPACE_FOUND = "//span[text()='No workspaces found.']";
    String WORKSPACE_ITEM_NAME = "//div[@class='workspace-name-clip' and contains(@id, '/%s')]";
    String WORKSPACE_ITEM_FULL_NAME = "//div[@id='ws-full-name-%s']";
    String WORKSPACE_ITEM_CHECKBOX = "//div[@id='ws-name-%s']//md-checkbox";
    String WORKSPACE_ITEM_RAM = "//div[@id='ws-name-%s']//span[@name='workspace-ram-value']";
    String WORKSPACE_ITEM_PROJECTS =
        "//div[@id='ws-name-%s']//span[@name='workspace-projects-value']";
    String WORKSPACE_ITEM_STACK = "//div[@id='ws-name-%s']//span[@name='workspace-stacks-name']";
    String WORKSPACE_ITEM_ACTIONS =
        "//div[@id='ws-name-%s']//*[@name='workspace-stop-start-button']/div";
    String WORKSPACE_ITEM_CONFIGURE_BUTTON =
        "//div[@id='ws-name-%s']//a[@name='configure-workspace-button']";
    String WORKSPACE_ITEM_ADD_PROJECT_BUTTON =
        "//div[@id='ws-name-%s']//span[@name='add-project-button']";
    String WORKSPACE_ITEM_STOP_START_WORKSPACE_BUTTON =
        "//div[@id='ws-name-%s']//*[@name='workspace-stop-start-button']/div";
    String WORKSPACE_LIST_HEADER = "//md-item[@class='noselect']//span";
    String WORKSPACE_LIST_ITEM =
        "(//div[@class='workspace-name-clip']/parent::div/parent::div/parent::div)[%s]";
    String PROGRESS_BAR_XPATH = "(//md-progress-linear[@role='progressbar'])[2]";
    String ERROR_NOTIFICATION_MESSAGE_XPATH = "//md-toast[@che-error-text]";
  }

  public interface Status {
    String STARTING = "STARTING";
    String RUNNING = "RUNNING";
    String STOPPING = "STOPPING";
    String STOPPED = "STOPPED";
  }

  @FindBy(id = Locators.ADD_WORKSPACE_BTN)
  WebElement addWorkspaceBtn;

  @FindBy(id = Locators.DELETE_WORKSPACE_BTN)
  WebElement deleleWorkspaceButton;

  @FindBy(xpath = Locators.DELETE_DIALOG_BUTTON)
  WebElement deleteBtn;

  @FindBy(xpath = Locators.SEARCH_WORKSPACE_FIELD)
  WebElement searchWorkspaceField;

  public void waitErrorNotificationContainsText(String expectedText) {
    final String textAttribute = "che-error-text";
    seleniumWebDriverHelper.waitAttributeContainsValue(
        By.xpath(ERROR_NOTIFICATION_MESSAGE_XPATH),
        textAttribute,
        expectedText,
        ELEMENT_TIMEOUT_SEC);
  }

  public void waitProgressBarInvisibility() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(PROGRESS_BAR_XPATH));
  }

  public String getWorkspaceStatus(String workspaceName) {
    return new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(
            visibilityOfElementLocated(
                By.xpath(format(Locators.WORKSPACE_ITEM_NAME, workspaceName))))
        .getAttribute("data-ws-status");
  }

  public void waitWorkspaceStatus(String workspaceName, String workspaceStatus) {
    // we need long timeout for OCP
    new WebDriverWait(seleniumWebDriver, PREPARING_WS_TIMEOUT_SEC)
        .until(
            new ExpectedCondition<Boolean>() {
              @Override
              public Boolean apply(WebDriver webDriver) {
                String status = getWorkspaceStatus(workspaceName);
                return status.equals(workspaceStatus);
              }
            });
  }

  public void waitPageLoading() {
    waitToolbarTitleName();
    waitDocumentationLink();
    waitAddWorkspaceButton();
    waitSearchWorkspaceByNameField();
  }

  public void waitDocumentationLink() {
    redrawUiElementsTimeout.until(
        visibilityOfElementLocated(By.xpath(Locators.DOCUMENTATION_LINK)));
  }

  public void clickOnDocumentationLink() {
    redrawUiElementsTimeout
        .until(visibilityOfElementLocated(By.xpath(Locators.DOCUMENTATION_LINK)))
        .click();
  }

  public void waitSearchWorkspaceByNameField() {
    redrawUiElementsTimeout.until(visibilityOf(searchWorkspaceField));
  }

  public void typeToSearchInput(String value) {
    redrawUiElementsTimeout.until(visibilityOf(searchWorkspaceField)).clear();
    searchWorkspaceField.sendKeys(value);
  }

  public void waitNoWorkspacesFound() {
    redrawUiElementsTimeout.until(
        visibilityOfElementLocated(By.xpath(Locators.NO_WORKSPACE_FOUND)));
  }

  // select workspace by checkbox
  public void selectWorkspaceByCheckbox(String workspaceName) {
    redrawUiElementsTimeout
        .until(
            visibilityOfElementLocated(
                By.xpath(format(Locators.WORKSPACE_ITEM_CHECKBOX, workspaceName))))
        .click();
  }

  public void waitBulkCheckbox() {
    redrawUiElementsTimeout.until(visibilityOfElementLocated(By.xpath(Locators.BULK_CHECKBOX)));
  }

  public void selectAllWorkspacesByBulk() {
    redrawUiElementsTimeout
        .until(visibilityOfElementLocated(By.xpath(Locators.BULK_CHECKBOX)))
        .click();
  }

  public boolean isBulkCheckboxEnabled() {
    return seleniumWebDriverHelper
        .waitVisibility(By.xpath("//md-checkbox[@aria-label='Workspace list']"))
        .getAttribute("class")
        .contains("md-checked");
  }

  public boolean isWorkspaceChecked(String workspaceName) {
    String attrValue =
        redrawUiElementsTimeout
            .until(
                visibilityOfElementLocated(
                    By.xpath(format(Locators.WORKSPACE_ITEM_CHECKBOX, workspaceName))))
            .getAttribute("aria-checked");

    return Boolean.parseBoolean(attrValue);
  }

  public void waitWorkspaceCheckboxEnabled(String workspaceName) {
    webDriverWaitFactory
        .get()
        .until((ExpectedCondition<Boolean>) driver -> isWorkspaceChecked(workspaceName));
  }

  public void waitWorkspaceCheckboxDisabled(String workspaceName) {
    webDriverWaitFactory
        .get()
        .until((ExpectedCondition<Boolean>) driver -> !isWorkspaceChecked(workspaceName));
  }

  public void waitBulkCheckboxEnabled() {
    webDriverWaitFactory
        .get()
        .until((ExpectedCondition<Boolean>) driver -> isBulkCheckboxEnabled());
  }

  public void waitBulkCheckboxDisabled() {
    webDriverWaitFactory
        .get()
        .until((ExpectedCondition<Boolean>) driver -> !isBulkCheckboxEnabled());
  }

  public String getWorkspaceRamValue(String workspaceName) {
    return redrawUiElementsTimeout
        .until(visibilityOfElementLocated(By.xpath(format(WORKSPACE_ITEM_RAM, workspaceName))))
        .getText();
  }

  public String getWorkspaceProjectsValue(String workspaceName) {
    return redrawUiElementsTimeout
        .until(
            visibilityOfElementLocated(
                By.xpath(format(Locators.WORKSPACE_ITEM_PROJECTS, workspaceName))))
        .getText();
  }

  public String getWorkspaceStackName(String workspaceName) {
    return redrawUiElementsTimeout
        .until(
            visibilityOfElementLocated(
                By.xpath(format(Locators.WORKSPACE_ITEM_STACK, workspaceName))))
        .getText();
  }

  public void clickOnWorkspaceActionsButton(String workspaceName) {
    redrawUiElementsTimeout
        .until(
            visibilityOfElementLocated(
                By.xpath(format(Locators.WORKSPACE_ITEM_ACTIONS, workspaceName))))
        .click();
  }

  public void clickOnWorkspaceConfigureButton(String workspaceName) {
    redrawUiElementsTimeout
        .until(
            visibilityOfElementLocated(
                By.xpath(format(Locators.WORKSPACE_ITEM_CONFIGURE_BUTTON, workspaceName))))
        .click();
  }

  public void clickOnWorkspaceListItem(String userName, String workspaceName) {
    String itemId = String.format("ws-full-name-%s/%s", userName, workspaceName);
    seleniumWebDriverHelper.waitAndClick(By.id(itemId));
  }

  public void clickOnWorkspaceAddProjectButton(String workspaceName) {
    redrawUiElementsTimeout
        .until(
            visibilityOfElementLocated(
                By.xpath(format(Locators.WORKSPACE_ITEM_ADD_PROJECT_BUTTON, workspaceName))))
        .click();
  }

  public void clickOnRamButton() {
    seleniumWebDriverHelper.waitVisibility(By.xpath("//div[@che-column-title='RAM']/div")).click();
  }

  public void clickOnProjectsButton() {
    seleniumWebDriverHelper.waitAndClick(By.xpath("//div[@che-column-title='Projects']/div"));
  }

  public void clickOnStackButton() {
    seleniumWebDriverHelper.waitAndClick(By.xpath("//div[@che-column-title='Stack']/div"));
  }

  public void clickOnWorkspaceStopStartButton(String workspaceName) {
    String buttonXpath = String.format(WORKSPACE_ITEM_STOP_START_WORKSPACE_BUTTON, workspaceName);

    // to avoid clicking on the tooltip
    moveCursorToWorkspaceRamSection(workspaceName);
    waitWorkspaceActionTooltipDisappearance();

    seleniumWebDriverHelper.waitAndClick(By.xpath(buttonXpath));

    waitProgressBarInvisibility();
  }

  public void waitWorkspaceActionTooltipDisappearance() {
    String tooltipXpath = "//che-workspace-status//div[contains(@class, 'tooltip')]";
    seleniumWebDriverHelper.waitInvisibility(By.xpath(tooltipXpath));
  }

  public void moveCursorToWorkspaceRamSection(String workspaceName) {
    seleniumWebDriverHelper.moveCursorTo(By.xpath(format(WORKSPACE_ITEM_RAM, workspaceName)));
  }

  public void selectWorkspaceItemName(String wsName) {
    waitWorkspaceIsPresent(wsName);
    seleniumWebDriverHelper.waitAndClick(By.xpath(format(Locators.WORKSPACE_ITEM_NAME, wsName)));
  }

  public void waitWorkspaceIsPresent(String workspaceName) {
    seleniumWebDriverHelper.waitVisibility(
        By.xpath(format(Locators.WORKSPACE_ITEM_NAME, workspaceName)));
  }

  public void waitWorkspaceIsPresent(String organizationName, String workspaceName) {
    String fullWorkspaceName = organizationName + "/" + workspaceName;
    new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC)
        .until(
            visibilityOfElementLocated(
                By.xpath(format(Locators.WORKSPACE_ITEM_FULL_NAME, fullWorkspaceName))));
  }

  /** wait the workspace is not present on dashboard */
  public void waitWorkspaceIsNotPresent(String workspaceName) {
    // we need long timeout for OCP
    new WebDriverWait(seleniumWebDriver, EXPECTED_MESS_IN_CONSOLE_SEC)
        .until(
            invisibilityOfElementLocated(
                By.xpath(format(Locators.WORKSPACE_ITEM_NAME, workspaceName))));
  }

  /** Wait toolbar name is present on dashboard */
  public void waitToolbarTitleName() {
    new WebDriverWait(seleniumWebDriver, LOADER_TIMEOUT_SEC)
        .until(visibilityOfElementLocated(By.id(Locators.TOOLBAR)));
  }

  // Click on the Add Workspace button
  public void clickOnAddWorkspaceBtn() {
    dashboard.waitNotificationIsClosed();
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(addWorkspaceBtn))
        .click();
  }

  public void waitAddWorkspaceButton() {
    new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC)
        .until(visibilityOf(addWorkspaceBtn));
  }

  public void clickOnDeleteWorkspacesBtn() {
    waitDeleteWorkspaceBtn().click();
  }

  public WebElement waitDeleteWorkspaceBtn() {
    dashboard.waitNotificationIsClosed();
    return seleniumWebDriverHelper.waitVisibility(deleleWorkspaceButton);
  }

  public void waitDeleteWorkspaceBtnDisappearance() {
    dashboard.waitNotificationIsClosed();
    seleniumWebDriverHelper.waitInvisibility(deleleWorkspaceButton);
  }

  /** Click on the delete/remove button in the dialog window */
  public void clickOnDeleteButtonInDialogWindow() {
    new WebDriverWait(seleniumWebDriver, REDRAW_UI_ELEMENTS_TIMEOUT_SEC)
        .until(visibilityOf(deleteBtn))
        .click();
  }

  public ArrayList<String> getWorkspaceListHeaders() {
    ArrayList<String> titles = new ArrayList<>();
    List<WebElement> headers =
        seleniumWebDriver.findElements(By.xpath(Locators.WORKSPACE_LIST_HEADER));
    headers.forEach(
        header -> {
          titles.add(header.getText());
        });

    return titles;
  }

  public int getVisibleWorkspacesCount() {
    return seleniumWebDriverHelper
        .waitVisibilityOfAllElements(
            By.xpath("//div[@class='workspace-name-clip']/parent::div/parent::div/parent::div"))
        .size();
  }

  public String getFullNameOfWorkspacesListItem(int index) {
    String itemXpath = String.format(Locators.WORKSPACE_LIST_ITEM, index);
    String fullNameXpath = itemXpath + "//div[@class='workspace-name-clip']";
    return seleniumWebDriverHelper
        .waitVisibilityAndGetAttribute(By.xpath(fullNameXpath), "id")
        .replace("ws-full-name-", "");
  }

  public Workspaces.WorkspaceListItem getWorkspacesListItemByWorkspaceName(
      List<Workspaces.WorkspaceListItem> itemsList, String workspaceName) {
    return itemsList
        .stream()
        .filter(item -> item.getWorkspaceName().equals(workspaceName))
        .collect(Collectors.toList())
        .get(0);
  }

  public List<WorkspaceListItem> getVisibleWorkspaces() {
    List<WorkspaceListItem> items = new ArrayList<>();

    for (int i = 1; i <= getVisibleWorkspacesCount(); i++) {
      String fullName = getFullNameOfWorkspacesListItem(i);
      String ownerName = Arrays.asList(fullName.split("/")).get(0);
      String workspaceName = Arrays.asList(fullName.split("/")).get(1);
      int ramCount =
          Integer.parseInt(Arrays.asList(getWorkspaceRamValue(workspaceName).split(" ")).get(0));
      int projectsCount = Integer.parseInt(getWorkspaceProjectsValue(workspaceName));
      items.add(new WorkspaceListItem(ownerName, workspaceName, ramCount, projectsCount));
    }

    return items;
  }

  public void waitVisibleWorkspacesCount(int expectedCount) {
    webDriverWaitFactory
        .get()
        .until((ExpectedCondition<Boolean>) driver -> expectedCount == getVisibleWorkspacesCount());
  }

  /**
   * Compares specified {@code expectedWorkspacesNames} with an existing workspaces list and gets
   * difference between them.
   *
   * @param expectedWorkspacesNames names of workspaces which should be present in the list
   * @return all workspaces which are present in the workspaces list and don't matches with provided
   */
  public List<Workspaces.WorkspaceListItem> getUnexpectedWorkspaces(
      List<String> expectedWorkspacesNames) {
    return getVisibleWorkspaces()
        .stream()
        .filter(workspace -> !expectedWorkspacesNames.contains(workspace.getWorkspaceName()))
        .collect(Collectors.toList());
  }

  /**
   * Waits until checkbox of the workspace with specified {@code workspaceName} has an opposite
   * state than {@code currentCheckboxState}.
   *
   * @param workspaceName name of the workspace where checkbox is placed
   * @param currentCheckboxState current state of the checkbox
   *     <p><b>true</b> - if checkbox is enabled,
   *     <p><b>false</b> - if checkbox is disabled.
   */
  public void waitOnOppositeCheckboxState(String workspaceName, boolean currentCheckboxState) {
    if (currentCheckboxState) {
      waitWorkspaceCheckboxDisabled(workspaceName);
      return;
    }

    waitWorkspaceCheckboxEnabled(workspaceName);
  }

  /**
   * Clicks on checkboxes of the unexpected workspaces {@link
   * Workspaces#getUnexpectedWorkspaces(List)}.
   *
   * @param expectedWorkspaces names of the expected workspaces in the workspaces list
   */
  public void clickOnUnexpectedWorkspacesCheckboxes(List<String> expectedWorkspaces) {
    List<Workspaces.WorkspaceListItem> unexpectedWorkspaces =
        getUnexpectedWorkspaces(expectedWorkspaces);

    if (unexpectedWorkspaces.isEmpty()) {
      return;
    }

    unexpectedWorkspaces.forEach(
        workspace -> {
          boolean checkboxState = isWorkspaceChecked(workspace.getWorkspaceName());
          selectWorkspaceByCheckbox(workspace.getWorkspaceName());
          waitOnOppositeCheckboxState(workspace.getWorkspaceName(), checkboxState);
        });
  }

  public static class WorkspaceListItem {
    private String ownerName;
    private String workspaceName;
    private int ramAmount;
    private int projectsAmount;

    public WorkspaceListItem(
        String ownerName, String workspaceName, int ramAmount, int projectsAmount) {
      this.ownerName = ownerName;
      this.workspaceName = workspaceName;
      this.ramAmount = ramAmount;
      this.projectsAmount = projectsAmount;
    }

    public String getOwnerName() {
      return ownerName;
    }

    public String getWorkspaceName() {
      return workspaceName;
    }

    public int getRamAmount() {
      return ramAmount;
    }

    public int getProjectsAmount() {
      return projectsAmount;
    }

    @Override
    public int hashCode() {
      return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof WorkspaceListItem) {
        WorkspaceListItem itemForCompare = (WorkspaceListItem) obj;

        return this.ownerName.equals(itemForCompare.ownerName)
            && this.workspaceName.equals(itemForCompare.workspaceName)
            && this.ramAmount == itemForCompare.ramAmount
            && this.projectsAmount == itemForCompare.projectsAmount;
      }

      return false;
    }
  }
}
