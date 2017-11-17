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
package org.eclipse.che.selenium.pageobject.git;

import static java.util.Arrays.stream;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.constant.TestGitConstants;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
import org.eclipse.che.selenium.pageobject.ImportProjectFromLocation;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.MavenPluginStatusBar;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.openqa.selenium.support.PageFactory;

/** @author Aleksandr Shmaraev on 25.11.15 */
@Singleton
public class Git {
  public final GitStatusBar gitStatusBar;
  private final GitBranches gitBranchesForm;
  private final GitHistory gitHistoryForm;
  private final GitAddToIndex gitAddToIndex;
  private final GitRemoveFromIndex gitRemoveFromIndex;
  private final GitCommit gitCommit;
  private final GitPull gitPull;
  private final GitPush gitPush;
  private final GitMerge gitMerge;
  private final GitFetch gitFetch;
  private final GitCompare gitCompare;
  private final GitReset gitReset;
  private final GitReference gitReference;
  private final Loader loader;
  private final AskForValueDialog askForValueDialog;
  private ProjectExplorer projectExplorer;
  private Menu menu;
  private ImportProjectFromLocation importProject;
  private Wizard projectWizard;
  private MavenPluginStatusBar mavenPluginStatusBar;
  private AskDialog askDialog;

  @Inject
  public Git(
      GitStatusBar gitStatusBar,
      GitBranches gitBranchesForm,
      GitHistory gitHistoryForm,
      GitAddToIndex gitAddToIndex,
      GitRemoveFromIndex gitRemoveFromIndex,
      GitCommit gitCommit,
      GitPull gitPull,
      GitPush gitPush,
      GitMerge gitMerge,
      GitFetch gitFetch,
      GitCompare gitCompare,
      GitReset gitReset,
      GitReference gitReference,
      SeleniumWebDriver seleniumWebDriver,
      Loader loader,
      AskForValueDialog askForValueDialog,
      ProjectExplorer projectExplorer,
      Menu menu,
      ImportProjectFromLocation importProject,
      Wizard projectWizard,
      MavenPluginStatusBar mavenPluginStatusBar,
      AskDialog askDialog) {
    this.gitStatusBar = gitStatusBar;
    this.gitBranchesForm = gitBranchesForm;
    this.gitHistoryForm = gitHistoryForm;
    this.gitAddToIndex = gitAddToIndex;
    this.gitRemoveFromIndex = gitRemoveFromIndex;
    this.gitCommit = gitCommit;
    this.gitPull = gitPull;
    this.gitPush = gitPush;
    this.gitMerge = gitMerge;
    this.gitFetch = gitFetch;
    this.gitCompare = gitCompare;
    this.gitReset = gitReset;
    this.gitReference = gitReference;
    this.loader = loader;
    this.askForValueDialog = askForValueDialog;
    this.projectExplorer = projectExplorer;
    this.menu = menu;
    this.importProject = importProject;
    this.projectWizard = projectWizard;
    this.mavenPluginStatusBar = mavenPluginStatusBar;
    this.askDialog = askDialog;
    PageFactory.initElements(seleniumWebDriver, this);
  }

  /** click on git-info panel tab, wait while info panel will close */
  public void closeGitInfoPanel() {
    gitStatusBar.clickOnStatusBarMinimizeBtn();
    gitStatusBar.waitCloseGitStatusBarInfoPanel();
  }

  /**
   * wait opening git info panel with expected message
   *
   * @param expextedMess
   */
  public void waitGitStatusBarWithMess(String expextedMess) {
    loader.waitOnClosed();
    gitStatusBar.waitMessageInGitTab(expextedMess);
  }

  /** wait and click on the 'navigate button' in the 'git console' */
  public void clickNavigateBtnIntoGitInfoPanel() {
    gitStatusBar.waitAndClickOnNavigateBtn();
  }

  /** wait and click on the 'remove button' in the 'git console */
  public void clickRemoveBtnIntoGitInfoPanel() {
    gitStatusBar.waitAndClickOnRemoveBtn();
  }

  /**
   * wait all branches in the main form of branches
   *
   * @param listNames is the list of names branches
   */
  public void waitAllBranchesInMainForm(String listNames) {
    gitBranchesForm.waitListBranchesInMainForm(listNames);
  }

  /**
   * wait appearance of the branches - list wit specified name
   *
   * @param nameOfTheBranch
   */
  public void waitBranchInTheList(String nameOfTheBranch) {
    gitBranchesForm.waitBranchesForm();
    gitBranchesForm.waitBranchWithName(nameOfTheBranch);
  }

  /**
   * wait appearance of the branches - list with specified name
   *
   * @param nameOfTheBranch
   */
  public void waitBranchInTheListWithCoState(String nameOfTheBranch) {
    gitBranchesForm.waitBranchesForm();
    gitBranchesForm.waitBranchWithNameCheckoutState(nameOfTheBranch);
    loader.waitOnClosed();
  }

  /** click on Close button and wait while form will be closed */
  public void closeBranchesForm() {
    gitBranchesForm.clickCloseBtn();
    gitBranchesForm.waitBranchesFormISIsClosed();
    loader.waitOnClosed();
  }

  /**
   * waiting disappearing a name deleting branch
   *
   * @param nameOfBranch is a name of branch
   */
  public void waitDisappearBranchName(String nameOfBranch) {
    gitBranchesForm.disappearBranchName(nameOfBranch);
  }

  /**
   * select needed branch click on delete branch button
   *
   * @param nameOfBranch is name of branch
   */
  public void selectBranchAndClickDelBranch(String nameOfBranch) {
    gitBranchesForm.selectBranchInListAndCheckEnabledButtonDelete(nameOfBranch);
    gitBranchesForm.clickDeleteBtn();
  }

  /**
   * select needed branch click on checkout button
   *
   * @param nameOfBranch is name of branch
   */
  public void selectBranchAndClickCheckoutBtn(String nameOfBranch) {
    gitBranchesForm.selectBranchAndCheckEnabledButtonCheckout(nameOfBranch);
    loader.waitOnClosed();
    gitBranchesForm.clickCheckOutBtn();
  }

  /** wait enabled button create click on create button */
  public void waitEnabledAndClickCreateBtn() {
    gitBranchesForm.waitEnabledButtonCreate();
    gitBranchesForm.clickCreateBtn();
  }

  /**
   * select needed branch click on rename button
   *
   * @param nameOfBranch is name of branch
   */
  public void selectBranchAndClickRenameBtn(String nameOfBranch) {
    gitBranchesForm.selectBranchInListAndCheckEnabledButtonRename(nameOfBranch);
    gitBranchesForm.clickRenameBtn();
  }

  /** wait ask dialog form for deleting selected branch */
  public void waitOpenedAskDelBranch() {
    gitBranchesForm.openDelBranchForm();
  }

  /** wait closing ask dialog form for deleting selected branch after click on button OK */
  public void confirmeAndCloseAskDelBranch() {
    gitBranchesForm.clickButtonOk();
    gitBranchesForm.closeDelBranch();
  }

  /**
   * send a text and click on OK button on codenvy window
   *
   * @param nameOfBranch is a name of branch
   */
  public void typeAndWaitNewBranchName(String nameOfBranch) {
    askForValueDialog.waitFormToOpen();
    askForValueDialog.deleteNameFromDialog();
    askForValueDialog.typeAndWaitText(nameOfBranch);
    askForValueDialog.clickOkBtn();
    askForValueDialog.waitFormToClose();
  }

  /** wait the history form is opened */
  public void waitHistoryFormToOpen() {
    loader.waitOnClosed();
    gitHistoryForm.waitHistoryForm();
  }

  /** wait the history form is closed */
  public void waitHistoryFormToClose() {
    gitHistoryForm.waitHistoryFormToClose();
  }

  /** click on the 'Close' button of the 'Git History' form */
  public void closeGitHistoryForm() {
    gitHistoryForm.clickOnCloseButtonHistory();
    waitHistoryFormToClose();
  }

  /** click on the 'Compare' button history */
  public void clickCompareBtnGitHistory() {
    gitHistoryForm.clickCompareButton();
    loader.waitOnClosed();
  }

  /** wait opening git history form and expected text */
  public void waitTextInHistoryForm(String expectedText) {
    gitHistoryForm.waitHistoryForm();
    gitHistoryForm.waitTextInHistoryList(expectedText);
  }

  /** select commit by number into history commits list numbers start - 0 */
  public void clickOnHistoryRowInСommitsList(int numberOfRow) {
    waitHistoryFormToOpen();
    gitHistoryForm.selectHistoryRowByIndex(numberOfRow);
  }

  /** wait while specified content will appear into history field */
  public void waitContentInHistoryEditor(String historyEditorContent) {
    gitHistoryForm.waitHistoryEditor();
    gitHistoryForm.waitContentIntoHistoryEditor(historyEditorContent);
  }

  public void waitCommitInHistoryForm(String commit) {
    gitHistoryForm.waitCommitInHistoryForm(commit);
  }

  public void clickOnCommitInHistoryForm(String commit) {
    gitHistoryForm.clickCommitInHistoryForm(commit);
  }

  public void waitCommitInHistoryFormNotPresent(String commit) {
    gitHistoryForm.waitCommitInHistoryFormNotPresent(commit);
  }

  /** wait the 'add to index' form is opened */
  public void waitAddToIndexFormToOpen() {
    gitAddToIndex.waitFormToOpen();
  }

  /** click on the OK button in the 'add to index' form wait the 'add to index' form is closed */
  public void confirmAddToIndexForm() {
    gitAddToIndex.waitFormToOpen();
    gitAddToIndex.clickAddBtn();
    gitAddToIndex.waitFormToClose();
  }

  /**
   * wait name of file to be added to index
   *
   * @param text
   */
  public void waitAddToIndexFileName(String text) {
    gitAddToIndex.waitFileNameToAdd(text);
  }

  /**
   * wait the files selected by multi-select in the add to index form
   *
   * @param files is the selected files
   */
  public void waitFilesAddToIndexByMultiSelect(String files) {
    gitAddToIndex.waitFilesAddIndexByMultiSelect(files);
  }

  public void selectUpdateCheckBox() {
    gitAddToIndex.selectUpdateCheckBox();
  }

  public void waitRemoveFromIndexFormToOpen() {
    gitRemoveFromIndex.waitFormToOpen();
  }

  public void confirmRemoveFromIndexForm() {
    gitRemoveFromIndex.clickRemoveBtn();
    gitRemoveFromIndex.waitFormToClose();
    loader.waitOnClosed();
  }

  public void waitRemoveFromIndexFileName(String text) {
    gitRemoveFromIndex.waitFileNameToRemove(text);
  }

  public void selectRemoveOnlyFromIndexCheckBox() {
    gitRemoveFromIndex.setRemoveOnlyFromIndexCheckBox();
  }

  /** wait the commit main form is opened */
  public void waitCommitMainFormIsOpened() {
    gitCommit.waitMainFormCommit();
  }

  /** click on the "Cancel' button in tne 'Commit' main form wait the main form is closed */
  public void clickOnCancelBtnComitForm() {
    gitCommit.clickOnCancelBtn();
    gitCommit.waitMainFormCommitIsClosed();
  }

  /**
   * Wait opening form, type commit message, click on 'Commit' button and wait closing form.
   *
   * @param text text for commit
   */
  public void waitAndRunCommit(String text) {
    gitCommit.waitMainFormCommit();
    gitCommit.typeCommitMsg(text);
    gitCommit.clickOnBtnCommit();
    gitCommit.waitMainFormCommitIsClosed();
    loader.waitOnClosed();
  }

  /**
   * Wait opening form, type commit message, click on 'Push After commit' check-box, select remote
   * branch, click on 'Commit' button and wait closing form.
   *
   * @param text text for commit
   * @param branch remote branch to push in
   */
  public void waitAndRunCommitWithPush(String text, String branch) {
    gitCommit.waitMainFormCommit();
    gitCommit.typeCommitMsg(text);
    gitCommit.clickOnPushAfterCommitCheckBox();
    gitCommit.selectRemoteBranch(branch);
    gitCommit.clickOnBtnCommit();
    gitCommit.waitMainFormCommitIsClosed();
    loader.waitOnClosed();
  }

  /**
   * Wait amend proposal dialog, wait opening form, check 'amend commit' is selected, type new
   * commit message click on 'Commit' button and wait closing form
   *
   * @param text commit message
   */
  public void waitAndRunAmendCommitMessage(String text) {
    askDialog.acceptDialogWithText(
        "Nothing to commit, working directory is clean. Would you like to perform amend commit?");
    gitCommit.waitMainFormCommit();
    gitCommit.waitAmendCommitIsSelected();
    gitCommit.typeCommitMsg(text);
    gitCommit.clickOnBtnCommit();
    gitCommit.waitMainFormCommitIsClosed();
    loader.waitOnClosed();
  }

  /**
   * wait opening form, select 'amend commit' type new commit message click on button OK and wait
   * closing form
   *
   * @param text is new commit message
   */
  public void waitAndRunAmendPreviousCommit(String text) {
    gitCommit.waitMainFormCommit();
    gitCommit.clickAmendCommit();
    loader.waitOnClosed();
    gitCommit.waitAmendCommitIsSelected();
    gitCommit.typeCommitMsg(text);
    gitCommit.clickOnBtnCommit();
    gitCommit.waitMainFormCommitIsClosed();
    loader.waitOnClosed();
  }

  public void waitPullFormToOpen() {
    loader.waitOnClosed();
    gitPull.waitFormToOpen();
  }

  public void waitPullFormToClose() {
    gitPull.waitFormToClose();
  }

  public void clickPull() {
    gitPull.clickPullBtn();
  }

  public void clickCancelPull() {
    gitPull.clickCancelBtn();
  }

  public void waitPullRemoteRepository(String text) {
    gitPull.waitPullRemoteRepository(text);
  }

  public void waitPullLocalBranchName(String text) {
    gitPull.waitLocalBranchName(text);
  }

  public void waitPullRemoteBranchName(String text) {
    gitPull.waitRemoteBranchName(text);
  }

  public void selectPullRemoteRepository(String nameRepo) {
    gitPull.chooseRemoteRepository(nameRepo);
    loader.waitOnClosed();
  }

  public void waitPullListRemoteBranchesNames(String listNames) {
    gitPull.waitListRemoteBranches(listNames);
  }

  public void waitDisabledPullButton() {
    gitPull.waitPullBtnIsDisabled();
  }

  public void waitPushFormToOpen() {
    loader.waitOnClosed();
    gitPush.waitFormToOpen();
    loader.waitOnClosed();
  }

  public void waitPushFormToClose() {
    gitPush.waitFormToClose();
  }

  public void clickPush() {
    gitPush.clickPushBtn();
  }

  /** Set checked 'force push' check-box. */
  public void selectForcePushCheckBox() {
    gitPush.selectForcePushCheckBox();
  }

  public void clickCancelPush() {
    gitPush.clickCancelBtn();
  }

  public void selectPushRemoteRepository(String text) {
    gitPush.selectRemoteRepository(text);
    loader.waitOnClosed();
  }

  public void selectPushLocalBranchName(String text) {
    gitPush.selectLocalBranchName(text);
  }

  public void selectPushRemoteBranchName(String text) {
    gitPush.selectRemoteBranch(text);
  }

  public void waitPushListRemoteRepositoriesNames(String listNames) {
    gitPush.waitListRemoteRepo(listNames);
  }

  public void waitPushListRemoteBranchesNames(String listNames) {
    gitPush.waitListRemoteBranches(listNames);
  }

  public void waitMergeExpandRemoteBranchIcon() {
    gitMerge.waitMergeExpandRemoteBranchIcon();
  }

  public void waitMergeExpandRemoteBranchIconIsNotPresent() {
    gitMerge.waitMergeExpandRemoteBranchIconIsNotPresent();
  }

  public void waitMergeReferencePanel() {
    gitMerge.waitMergePanel();
  }

  public void waitMergeExpandLocalBranchIcon() {
    gitMerge.waitMergeExpandLocalBranchIcon();
  }

  public void clickMergeExpandLocalBranchIcon() {
    gitMerge.clickMergeExpandLocalBranchIcon();
  }

  public void waitMergeView() {
    gitMerge.waitMergeView();
  }

  public void clickMergeExpandRemoteBranchIcon() {
    gitMerge.clickMergeExpandRemoteBranchIcon();
  }

  public void waitMergeListRemoteBranchesNames(String listNames) {
    gitMerge.waitListRemoteBranches(listNames);
  }

  public void waitItemInMergeList(String branchName) {
    gitMerge.waitItemInMergeList(branchName);
  }

  public void clickItemInMergeList(String branchName) {
    gitMerge.selectItemInMergeList(branchName);
  }

  public void clickCancelMergeBtn() {
    gitMerge.clickCancelMergeBtn();
  }

  public void clickMergeBtn() {
    gitMerge.clickMergeBtn();
  }

  public void waitDisabledMergeButton() {
    gitMerge.waitMergeBtnIsDisabled();
  }

  public void waitMergeViewClosed() {
    gitMerge.waitMergeViewClosed();
  }

  public void waitFetchFormOpened() {
    gitFetch.waitFormToOpen();
  }

  public void waitFetchFormClosed() {
    gitFetch.waitFormToClose();
  }

  public void waitCommitFormClosed() {
    gitCommit.waitMainFormCommitIsClosed();
  }

  public void selectRemoteRepoOnFetchForm(String nameRemoteRepo) {
    gitFetch.chooseRemoteRepository(nameRemoteRepo);
  }

  public void clickOnFetchAllBranchesCheckBox() {
    gitFetch.clickFetchAllBranches();
  }

  public void selectRemoteBranchOnFetchForm(String nameRemoteBranch) {
    gitFetch.chooseRemoteBranch(nameRemoteBranch);
  }

  public void waitFetchListRemoteBranchesNames(String listNames) {
    gitFetch.waitListRemoteBranches(listNames);
  }

  public void selectLocalBranchOnFetchForm(String nameLocalBranch) {
    gitFetch.chooseLocalBranch(nameLocalBranch);
  }

  public void clickOnFetchButton() {
    gitFetch.clickFetchBtn();
  }

  public void waitDisabledFetchButton() {
    gitFetch.waitFetchBtnIsDisabled();
  }

  public void clickCancelFetch() {
    gitFetch.clickFetchCancelBtn();
  }

  /** wait the main form 'Git Compare' is open */
  public void waitGitCompareFormIsOpen() {
    gitCompare.waitGitCompareFormIsOpen();
  }

  /** wait the main form 'Git Compare' is closed */
  public void waitGitCompareFormIsClosed() {
    gitCompare.waitGitCompareFormIsClosed();
  }

  /**
   * wait expected text into left editor of the 'Git Compare'
   *
   * @param expText expected value
   */
  public void waitExpTextIntoCompareLeftEditor(String expText) {
    gitCompare.waitExpectedTextIntoLeftEditor(expText);
  }

  /**
   * wait expected text into right editor of the 'Git Compare'
   *
   * @param expText expected value
   */
  public void waitTextNotPresentIntoCompareRightEditor(String expText) {
    gitCompare.waitTextNotPresentIntoRightEditor(expText);
  }

  /** set focus on the left compare editor */
  public void setFocusOnLeftGitCompareEditor() {
    loader.waitOnClosed();
    gitCompare.setFocusOnLeftCompareEditor();
  }

  /**
   * set the cursor in the specified line
   *
   * @param positionLine is the specified number line
   * @param status is expected line and column position
   */
  public void setCursorToLine(int positionLine, String status) {
    loader.waitOnClosed();
    gitCompare.setCursorToLine(positionLine, status);
  }

  /**
   * type text to git compare form
   *
   * @param text
   */
  public void typeTextIntoGitCompareEditor(String text) {
    gitCompare.typeTextIntoGitCompareEditor(text);
  }

  /** close the git compare form */
  public void closeGitCompareForm() {
    gitCompare.clickOnCompareCloseButton();
    waitGitCompareFormIsClosed();
  }

  /** click on the 'Close' button git compare form */
  public void clickOnGitCompareCloseButton() {
    gitCompare.clickOnCompareCloseButton();
  }

  /** wait the git 'Compare with branch' form is open */
  public void waitGitCompareBranchFormIsOpen() {
    gitCompare.waitCompareBranchIsOpen();
  }

  /** wait the git 'Compare with branch' form is closed */
  public void waitGitCompareBranchFormIsClosed() {
    gitCompare.waitCompareBranchIsClosed();
  }

  /**
   * select a branch into the git 'Compare with branch' form
   *
   * @param branchName is name of the branch
   */
  public void selectBranchIntoGitCompareBranchForm(String branchName) {
    gitCompare.selectBranchIntoCompareBranchForm(branchName);
  }

  /** click on the 'Compare' button into git 'Compare with branch' form */
  public void clickOnCompareBranchFormButton() {
    gitCompare.clickOnBranchCompareButton();
    waitGitCompareBranchFormIsClosed();
  }

  /** wait the git 'Compare with revision' form is open */
  public void waitGitCompareRevisionFormIsOpen() {
    loader.waitOnClosed();
    gitCompare.waitCompareRevisionIsOpen();
  }

  /** wait the git 'Compare with revision' form is closed */
  public void waitGitCompareRevisionFormIsClosed() {
    gitCompare.waitCompareRevisionIsClosed();
  }

  /**
   * select a revision with specified number into the 'Compare with revision' form numbers start
   * with 0
   *
   * @param revision
   */
  public void selectRevisionIntoCompareRevisionForm(int revision) {
    gitCompare.selectRevisionIntoCompareRevisionForm(revision);
  }

  /** click on the 'Compare' button into 'Compare with revision' form */
  public void clickOnRevisionCompareButton() {
    gitCompare.clickOnRevisionCompareButton();
    loader.waitOnClosed();
  }

  /** click on the 'Close' button into 'Compare with revision' form */
  public void clickOnCloseRevisionButton() {
    gitCompare.clickOnCloseRevisionButton();
    loader.waitOnClosed();
    waitGitCompareRevisionFormIsClosed();
  }

  /** wait the group 'Git Compare' form is open */
  public void waitGroupGitCompareIsOpen() {
    loader.waitOnClosed();
    gitCompare.waitGroupGitCompareIsOpen();
  }

  /** wait the group 'Git Compare' form is closed */
  public void waitGroupGitCompareIsClosed() {
    gitCompare.waitGroupGitCompareIsClosed();
  }

  /**
   * wait expected text into group git compare
   *
   * @param expText is expected value
   */
  public void waitExpTextInGroupGitCompare(String expText) {
    gitCompare.waitExpTextInGroupGitCompare(expText);
  }

  /** click on the 'Compare' button into group 'Git Compare' form */
  public void clickOnGroupCompareButton() {
    gitCompare.clickOnGroupCompareButton();
    loader.waitOnClosed();
  }

  /** click on the 'Close' button into group 'Git Compare' form */
  public void closeGroupGitCompareForm() {
    gitCompare.clickOnGroupCloseButton();
    waitGroupGitCompareIsClosed();
  }

  /**
   * Select file in the 'Git changed files tree panel'.
   *
   * @param name name of the file
   */
  public void selectFileInChangedFilesTreePanel(String name) {
    gitCompare.selectChangedFile(name);
  }

  /**
   * Click the check-box of the item in the 'Git changed files tree panel' of the 'Commit' window.
   *
   * @param itemName name of the item
   */
  public void clickItemCheckBoxInCommitWindow(String itemName) {
    gitCommit.clickItemCheckBox(itemName);
  }

  /**
   * Wait for item check-box in the 'Git changed files tree panel' in 'Commit' window to be
   * selected.
   *
   * @param itemName name of the item
   */
  public void waitItemCheckBoxToBeSelectedInCommitWindow(String... itemName) {
    stream(itemName).forEach(gitCommit::waitItemCheckBoxToBeSelected);
  }

  /**
   * Wait for item check-box in the 'Git changed files tree panel' in 'Commit' window to be
   * unselected.
   *
   * @param itemName name of the item
   */
  public void waitItemCheckBoxToBeUnSelectedInCommitWindow(String... itemName) {
    stream(itemName).forEach(gitCommit::waitItemCheckBoxToBeUnSelected);
  }

  /**
   * Wait for item check-box in the 'Git changed files tree panel' in 'Commit' window to be
   * indeterminate.
   *
   * @param itemName name of the item
   */
  public void waitItemCheckBoxToBeIndeterminateInCommitWindow(String... itemName) {
    stream(itemName).forEach(gitCommit::waitItemCheckBoxToBeIndeterminate);
  }

  /** Wait 'Reset to commit' window is open */
  public void waitResetWindowOpen() {
    gitReset.waitOpen();
  }

  /** Wait 'Reset to commit' window is close */
  public void waitResetWindowClose() {
    gitReset.waitClose();
  }

  /** Click on 'Reset' button in the 'Reset Commit' window */
  public void clickResetBtn() {
    gitReset.clickResetBtn();
  }

  /** Select 'hard' in the 'Reset Commit' window */
  public void selectHardReset() {
    gitReset.selectHardReset();
  }

  /** Select 'soft' in the 'Reset Commit' window */
  public void selectSoftReset() {
    gitReset.selectSoftReset();
  }

  /**
   * Wait commit is present in the 'Reset Commit' window
   *
   * @param text text from comment
   */
  public void waitCommitIsPresentResetWindow(String text) {
    gitReset.waitCommitIsPresent(text);
  }

  /**
   * Select commit by number of line. Numbering starts at zero.
   *
   * @param numberLine number of line for commit
   */
  public void selectCommitResetWindow(int numberLine) {
    gitReset.selectCommit(numberLine);
  }

  /** wait the 'Checkout Reference' form is open */
  public void waitReferenceFormIsOpened() {
    gitReference.waitOpeningMainForm();
  }

  /** wait the 'Checkout Reference' form is closed */
  public void waitReferenceFormIsClosed() {
    gitReference.waitClosingMainForm();
  }

  /**
   * wait opening reference form, type new reference click on checkout button and wait closing the
   * widget
   *
   * @param reference the reference defined by user
   */
  public void typeReferenceAndConfirm(String reference) {
    gitReference.waitOpeningMainForm();
    gitReference.typeReference(reference);
    gitReference.clickOnCheckoutBtn();
    gitReference.waitClosingMainForm();
  }

  /** click on the 'Cancel' button wait the 'Checkout Reference' form is closed */
  public void clickCheckoutReferenceCancelButton() {
    gitReference.clickOnCancelBtn();
    waitReferenceFormIsClosed();
  }

  /**
   * Creates a new file, then makes commit and pushes to remote repository.
   *
   * @param path - full path to the folder in which a new file will be created (e.g.
   *     "MyProject/src/main/webapp")
   * @param fileName - name of the new file
   */
  public void createNewFileAndPushItToGitHub(String path, String fileName) {
    projectExplorer.selectItem(path);
    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT,
        TestMenuCommandsConstants.Project.New.NEW,
        TestMenuCommandsConstants.Project.New.FILE);
    askForValueDialog.waitFormToOpen();
    askForValueDialog.typeAndWaitText(fileName);
    askForValueDialog.clickOkBtn();
    askForValueDialog.waitFormToClose();
    loader.waitOnClosed();
    loader.waitOnClosed();
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.COMMIT);
    waitAndRunCommit("new file");
    waitGitStatusBarWithMess(TestGitConstants.COMMIT_MESSAGE_SUCCESS);
    menu.runCommand(
        TestMenuCommandsConstants.Git.GIT,
        TestMenuCommandsConstants.Git.Remotes.REMOTES_TOP,
        TestMenuCommandsConstants.Git.Remotes.PUSH);
    waitPushFormToOpen();
    clickPush();
    waitPushFormToClose();
  }

  public void importJavaApp(String url, String nameApp, String typeProject) {
    loader.waitOnClosed();
    menu.runCommand(
        TestMenuCommandsConstants.Workspace.WORKSPACE,
        TestMenuCommandsConstants.Workspace.IMPORT_PROJECT);
    importProject.waitAndTypeImporterAsGitInfo(url, nameApp);
    projectWizard.waitCreateProjectWizardForm();
    projectWizard.selectTypeProject(typeProject);
    loader.waitOnClosed();
    projectWizard.clickSaveButton();
    loader.waitOnClosed();
    projectWizard.waitCreateProjectWizardFormIsClosed();
    projectExplorer.waitItem(nameApp);
    loader.waitOnClosed();
  }

  public void importJavaAppAndCheckMavenPluginBar(
      String url, String nameApp, String typeProject, String expectedMessage) {
    loader.waitOnClosed();
    menu.runCommand(
        TestMenuCommandsConstants.Workspace.WORKSPACE,
        TestMenuCommandsConstants.Workspace.IMPORT_PROJECT);
    importProject.waitAndTypeImporterAsGitInfo(url, nameApp);
    projectWizard.waitCreateProjectWizardForm();
    projectWizard.selectTypeProject(typeProject);
    loader.waitOnClosed();
    projectWizard.clickSaveButton();
    loader.waitOnClosed();
    projectWizard.waitCreateProjectWizardFormIsClosed();
    projectExplorer.waitItem(nameApp);
    loader.waitOnClosed();
  }

  public void importJavaAppAndCheckMavenPluginBar(String url, String nameApp, String typeProject) {
    loader.waitOnClosed();
    menu.runCommand(
        TestMenuCommandsConstants.Workspace.WORKSPACE,
        TestMenuCommandsConstants.Workspace.IMPORT_PROJECT);
    importProject.waitAndTypeImporterAsGitInfo(url, nameApp);
    projectWizard.waitCreateProjectWizardForm();
    projectWizard.selectTypeProject(typeProject);
    loader.waitOnClosed();
    projectWizard.clickSaveButton();
    loader.waitOnClosed();
    projectWizard.waitCreateProjectWizardFormIsClosed();
    projectExplorer.waitItem(nameApp);
    loader.waitOnClosed();
  }
}
