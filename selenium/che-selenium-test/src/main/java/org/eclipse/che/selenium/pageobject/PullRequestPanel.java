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

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ATTACHING_ELEM_TO_DOM_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.WIDGET_TIMEOUT_SEC;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

/**
 * @author Andrey chzhikov
 * @author Aleksandr Shmaraiev
 */
@Singleton
public class PullRequestPanel {

  private final SeleniumWebDriverHelper seleniumWebDriverHelper;
  private final SeleniumWebDriver seleniumWebDriver;

  @Inject private Loader loader;

  @Inject
  public PullRequestPanel(
      SeleniumWebDriver seleniumWebDriver,
      SeleniumWebDriverHelper seleniumWebDriverHelper,
      SeleniumWebDriver seleniumWebDriver1) {
    this.seleniumWebDriverHelper = seleniumWebDriverHelper;
    this.seleniumWebDriver = seleniumWebDriver1;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  private static final class PullRequestLocators {
    static final String PANEL = "gwt-debug-toolPanel";
    static final String SELECT_BRANCH = "//div[text()='Branch name:']/following-sibling::div/div";
    static final String TITLE_INPUT = "//div[text()='Title:']/following::input";
    static final String COMMENT_TEXTAREA = "//div[text()='Comment:']/following::textarea";
    static final String UPDATE_PR_BTN = "//button[text()='Update PR']";
    static final String CREATE_PR_BTN = "//button[text()='Create PR']";
    static final String OPEN_GITHUB_BTN = "//button[text()='Open on GitHub']";
    static final String PULL_REQUEST_BTN = "gwt-debug-partButton-Pull Request";
    static final String BRANCH_NAME =
        "//div[text()='Branch name:']/following-sibling::div//span/label[text()='%s']";
    static final String OK_COMMIT_BTN = "commit-dialog-ok";
    static final String STATUS_OK = "//div[text()='%s']/parent::div//i[@class='fa fa-check']";
    static final String MESSAGE = "gwt-debug-statusSectionMessage";
    static final String PROJECT_ITEM =
        "//div[text()='Repository']/parent::div//i[contains(@class,'fa-bookmark')]/following-sibling::div[text()='%s']";
    static final String BRANCH_ITEM =
        "//div[text()='Repository']/parent::div//i[contains(@class,'fa-code-fork')]/following-sibling::div[text()='%s']";
    static final String URL_ITEM =
        "//div[text()='Repository']/parent::div//i[contains(@class,'fa-link')]/following-sibling::a[@href='%s']";
    static final String MENU_BUTTON_XPATH = "(//div[@id='gwt-debug-menuButton'])[position()=2]";
    static final String CONTEXT_HIDE_BUTTON_ID = "gwt-debug-contextMenu/Hide";
    static final String HIDE_BUTTON_XPATH = "(//div[@id='gwt-debug-hideButton'])[position()=2]";

    private PullRequestLocators() {}
  }

  public enum Status {
    BRANCH_PUSHED_ON_YOUR_ORIGIN("Branch pushed on your origin"),
    PULL_REQUEST_ISSUED("Pull request issued"),
    NEW_COMMITS_PUSHED("New commits pushed"),
    PULL_REQUEST_UPDATED("Pull request updated"),
    FORK_CREATED("Fork created"),
    BRANCH_PUSHED_ON_YOUR_FORK("Branch pushed on your fork");

    private final String message;

    Status(String message) {
      this.message = message;
    }

    public String getMessage() {
      return message;
    }
  }

  @FindBy(id = PullRequestLocators.PANEL)
  WebElement panel;

  @FindBy(xpath = PullRequestLocators.SELECT_BRANCH)
  WebElement selectBranch;

  @FindBy(xpath = PullRequestLocators.TITLE_INPUT)
  WebElement titleInput;

  @FindBy(xpath = PullRequestLocators.COMMENT_TEXTAREA)
  WebElement commentTextarea;

  @FindBy(xpath = PullRequestLocators.CREATE_PR_BTN)
  WebElement createPRBtn;

  @FindBy(xpath = PullRequestLocators.UPDATE_PR_BTN)
  WebElement updatePRBtn;

  @FindBy(id = PullRequestLocators.PULL_REQUEST_BTN)
  WebElement pullRequestBtn;

  @FindBy(id = PullRequestLocators.OK_COMMIT_BTN)
  WebElement okCommitBtn;

  @FindBy(xpath = PullRequestLocators.HIDE_BUTTON_XPATH)
  WebElement hideButton;

  @FindBy(xpath = PullRequestLocators.MENU_BUTTON_XPATH)
  WebElement menuButton;

  @FindBy(id = PullRequestLocators.CONTEXT_HIDE_BUTTON_ID)
  WebElement contextHideButton;

  /** Wait that 'Pull Request' panel is open */
  public void waitOpenPanel() {
    seleniumWebDriverHelper.waitVisibility(panel);
  }

  /** Wait that 'Pull Request' panel is close */
  public void waitClosePanel() {
    seleniumWebDriverHelper.waitInvisibility(By.xpath(PullRequestLocators.PANEL));
  }

  /**
   * Wait text in the PR panel if a project does not provide VCS
   *
   * @param expText expected text which should be in the PR panel
   */
  public void waitTextNotVcsProject(String expText) {
    seleniumWebDriverHelper.waitTextEqualsTo(panel, expText);
  }

  /**
   * Fill the 'Title:' on the 'Pull Request' panel
   *
   * @param title text of title
   */
  public void enterTitle(String title) {
    seleniumWebDriverHelper.waitAndSendKeysTo(titleInput, title);
  }

  /**
   * Fill the 'Comment:' on the 'Pull Request' panel
   *
   * @param comment text of comment
   */
  public void enterComment(String comment) {
    seleniumWebDriverHelper.waitAndSendKeysTo(commentTextarea, comment);
  }

  /** Click 'Create PR' button on the 'Pull Request' panel */
  public void clickCreatePullRequestButton() {
    seleniumWebDriverHelper.waitAndClick(createPRBtn);
  }

  /** Click 'Update PR' button on the 'Pull Request' panel */
  public void clickUpdatePullRequestButton() {
    seleniumWebDriverHelper.waitAndClick(updatePRBtn, ELEMENT_TIMEOUT_SEC);
  }

  /** Click 'Pull Request' button on right panel in the IDE */
  public void clickPullRequestBtn() {
    seleniumWebDriverHelper.waitAndClick(pullRequestBtn, ELEMENT_TIMEOUT_SEC);
  }

  /**
   * Select branch on the 'Pull Request' panel
   *
   * @param branchName name of branch
   */
  public void selectBranch(String branchName) {
    seleniumWebDriverHelper.waitAndClick(selectBranch);
    seleniumWebDriverHelper.waitAndClick(
        By.xpath(String.format(PullRequestLocators.BRANCH_NAME, branchName)));
  }

  /** Click 'Ok' button in the 'Commit your changes' window */
  public void clickOkCommitBtn() {
    seleniumWebDriverHelper.waitAndClick(okCommitBtn);
  }

  /** Wait status is 'Ok' in the 'Pull Request' panel */
  public void waitStatusOk(Status status) {
    seleniumWebDriverHelper.waitVisibility(
        By.xpath(String.format(PullRequestLocators.STATUS_OK, status.getMessage())),
        WIDGET_TIMEOUT_SEC);
    loader.waitOnClosed();
  }

  /**
   * Wait message in the 'Pull Request' panel
   *
   * @param message text of message
   */
  public void waitMessage(String message) {
    seleniumWebDriverHelper.waitTextEqualsTo(By.id(PullRequestLocators.MESSAGE), message);
  }

  /**
   * Wait project name in repository area on the 'Pull Request' panel
   *
   * @param project name of current project
   */
  public void waitProjectName(String project) {
    seleniumWebDriverHelper.waitVisibility(
        By.xpath(String.format(PullRequestLocators.PROJECT_ITEM, project)));
  }

  /**
   * Wait branch name in repository area on the 'Pull Request' panel
   *
   * @param branch name of branch
   */
  public void waitBranchName(String branch) {
    seleniumWebDriverHelper.waitVisibility(
        By.xpath(String.format(PullRequestLocators.BRANCH_ITEM, branch)));
  }

  /**
   * Wait branch name in repository area on the 'Pull Request' panel
   *
   * @param url URL of current project
   */
  public void waitRepoUrl(String url) {
    seleniumWebDriverHelper.waitVisibility(
        By.xpath(String.format(PullRequestLocators.URL_ITEM, url)));
  }

  public void openPullRequestOnGitHub() {
    Wait<WebDriver> wait =
        new FluentWait(seleniumWebDriver)
            .withTimeout(ATTACHING_ELEM_TO_DOM_SEC, SECONDS)
            .pollingEvery(500, MILLISECONDS)
            .ignoring(WebDriverException.class);
    wait.until(visibilityOfElementLocated(By.xpath(PullRequestLocators.OPEN_GITHUB_BTN))).click();
  }

  /** click on the 'Hide' button */
  public void closePanelByHideButton() {
    seleniumWebDriverHelper.waitAndClick(hideButton);
    waitClosePanel();
  }

  /** open the menu 'Options' */
  public void openOptionsMenu() {
    seleniumWebDriverHelper.waitAndClick(menuButton);
  }

  /** click on the 'Hide' button in the menu 'Options' */
  public void closePanelFromContextMenu() {
    seleniumWebDriverHelper.waitAndClick(contextHideButton);
    waitClosePanel();
  }
}
