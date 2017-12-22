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

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ATTACHING_ELEM_TO_DOM_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.WIDGET_TIMEOUT_SEC;
import static org.openqa.selenium.support.ui.ExpectedConditions.invisibilityOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.textToBe;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * @author Andrey chzhikov
 * @author Aleksandr Shmaraiev
 */
@Singleton
public class PullRequestPanel {

  private final WebDriverWait loadPageDriverWait;
  private final WebDriverWait elemDriverWait;
  private final WebDriverWait widgetDriverWait;

  private final SeleniumWebDriver seleniumWebDriver;

  @Inject private Loader loader;

  @Inject
  public PullRequestPanel(SeleniumWebDriver seleniumWebDriver) {
    this.seleniumWebDriver = seleniumWebDriver;
    loadPageDriverWait = new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC);
    elemDriverWait = new WebDriverWait(seleniumWebDriver, ELEMENT_TIMEOUT_SEC);
    widgetDriverWait = new WebDriverWait(seleniumWebDriver, WIDGET_TIMEOUT_SEC);
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

  /** Wait that 'Pull Request' panel is open */
  public void waitOpenPanel() {
    loadPageDriverWait.until(visibilityOf(panel));
  }

  /** Wait that 'Pull Request' panel is close */
  public void waitClosePanel() {
    loadPageDriverWait.until(invisibilityOfElementLocated(By.xpath(PullRequestLocators.PANEL)));
  }

  /**
   * Fill the 'Title:' on the 'Pull Request' panel
   *
   * @param title text of title
   */
  public void enterTitle(String title) {
    loadPageDriverWait.until(visibilityOf(titleInput)).sendKeys(title);
  }

  /**
   * Fill the 'Comment:' on the 'Pull Request' panel
   *
   * @param comment text of comment
   */
  public void enterComment(String comment) {
    loadPageDriverWait.until(visibilityOf(commentTextarea)).sendKeys(comment);
  }

  /** Click 'Create PR' button on the 'Pull Request' panel */
  public void clickCreatePRBtn() {
    elemDriverWait.until(visibilityOf(createPRBtn)).click();
  }

  /** Click 'Update PR' button on the 'Pull Request' panel */
  public void clickUpdatePRBtn() {
    elemDriverWait.until(visibilityOf(updatePRBtn)).click();
  }

  /** Click 'Pull Request' button on right panel in the IDE */
  public void clickPullRequestBtn() {
    elemDriverWait.until(visibilityOf(pullRequestBtn)).click();
  }

  /**
   * Select branch on the 'Pull Request' panel
   *
   * @param branchName name of branch
   */
  public void selectBranch(String branchName) {
    loadPageDriverWait.until(visibilityOf(selectBranch)).click();
    loadPageDriverWait
        .until(
            visibilityOfElementLocated(
                By.xpath(String.format(PullRequestLocators.BRANCH_NAME, branchName))))
        .click();
  }

  /** Click 'Ok' button in the 'Commit your changes' window */
  public void clickOkCommitBtn() {
    elemDriverWait.until(visibilityOf(okCommitBtn)).click();
  }

  /** Wait status is 'Ok' in the 'Pull Request' panel */
  public void waitStatusOk(Status status) {
    widgetDriverWait.until(
        visibilityOfElementLocated(
            By.xpath(String.format(PullRequestLocators.STATUS_OK, status.getMessage()))));
    loader.waitOnClosed();
  }

  /**
   * Wait message in the 'Pull Request' panel
   *
   * @param message text of message
   */
  public void waitMessage(String message) {
    loadPageDriverWait.until(textToBe(By.id(PullRequestLocators.MESSAGE), message));
  }

  /**
   * Wait project name in repository area on the 'Pull Request' panel
   *
   * @param project name of current project
   */
  public void waitProjectName(String project) {
    loadPageDriverWait.until(
        visibilityOfElementLocated(
            By.xpath(String.format(PullRequestLocators.PROJECT_ITEM, project))));
  }

  /**
   * Wait branch name in repository area on the 'Pull Request' panel
   *
   * @param branch name of branch
   */
  public void waitBranchName(String branch) {
    loadPageDriverWait.until(
        visibilityOfElementLocated(
            By.xpath(String.format(PullRequestLocators.BRANCH_ITEM, branch))));
  }

  /**
   * Wait branch name in repository area on the 'Pull Request' panel
   *
   * @param url URL of current project
   */
  public void waitRepoUrl(String url) {
    loadPageDriverWait.until(
        visibilityOfElementLocated(By.xpath(String.format(PullRequestLocators.URL_ITEM, url))));
  }

  public void openPullRequestOnGitHub() {
    Wait<WebDriver> wait =
        new FluentWait(seleniumWebDriver)
            .withTimeout(ATTACHING_ELEM_TO_DOM_SEC, SECONDS)
            .pollingEvery(500, MILLISECONDS)
            .ignoring(WebDriverException.class);
    wait.until(visibilityOfElementLocated(By.xpath(PullRequestLocators.OPEN_GITHUB_BTN))).click();
  }
}
