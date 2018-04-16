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
package org.eclipse.che.selenium.git;

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.BRANCHES;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.CHECKOUT_REFERENCE;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.GIT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.SHOW_HISTORY;
import static org.eclipse.che.selenium.pageobject.Wizard.TypeProject.BLANK;
import static org.testng.Assert.assertTrue;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestGitHubRepository;
import org.eclipse.che.selenium.core.client.TestUserPreferencesServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.git.Git;
import org.eclipse.che.selenium.pageobject.git.GitHistory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev */
public class CheckoutReferenceTest {
  private static final String PROJECT_NAME = NameGenerator.generate("CheckoutReference_", 4);
  private static final String DEFAULT_BRANCH = "develop";
  private static final String JS_FILE = "app.js";
  private static final String CHANGE_FILE = "// change";
  private static final String CHANGE_FILE_1 = "// change content to version_1";
  private static final String CHANGE_FILE_2 = "// change content to version_2";
  private static final String UPDATE_FILE = "// update";
  private static final String TAG_NAME_1 = "version_1";
  private static final String TAG_NAME_2 = "version_2";
  private static final String COMMIT_MESSAGE = "Change file app.js";
  private static final String COMMIT_MESSAGE_TAG_1 = "Change the app.js file, tag version_1";
  private static final String COMMIT_MESSAGE_TAG_2 = "Change the app.js file, tag version_2";
  private static final String MESSAGE_TAG_WRONG = "Ref version1 can not be resolved";
  private static final String BRANCH_DETACHED_TAG_1 = "(detached from version_1)";
  private static final String BRANCH_DETACHED_TAG_2 = "(detached from version_2)";

  private String sha1;

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;
  @Inject private DefaultTestUser productUser;
  @Inject private TestGitHubRepository testRepo;

  @Inject(optional = true)
  @Named("github.username")
  private String gitHubUsername;

  @Inject private ProjectExplorer projectExplorer;
  @Inject private Menu menu;
  @Inject private Git git;
  @Inject private CodenvyEditor editor;
  @Inject private GitHistory gitHistory;
  @Inject private TestUserPreferencesServiceClient testUserPreferencesServiceClient;

  @BeforeClass
  public void prepare() throws Exception {
    testUserPreferencesServiceClient.addGitCommitter(gitHubUsername, productUser.getEmail());

    Path entryPath = Paths.get(getClass().getResource("/projects/git-pull-test").getPath());
    testRepo.addContent(entryPath);

    testRepo.createBranch(DEFAULT_BRANCH);
    testRepo.setDefaultBranch(DEFAULT_BRANCH);

    // create tags in the test repo
    testRepo.changeFileContent(JS_FILE, CHANGE_FILE, COMMIT_MESSAGE);

    sha1 = testRepo.getSha1(DEFAULT_BRANCH);

    testRepo.changeFileContent(JS_FILE, CHANGE_FILE_1, COMMIT_MESSAGE_TAG_1);
    testRepo.createTag(TAG_NAME_1);

    testRepo.changeFileContent(JS_FILE, CHANGE_FILE_2, COMMIT_MESSAGE_TAG_2);
    testRepo.createTag(TAG_NAME_2);

    testRepo.changeFileContent(JS_FILE, UPDATE_FILE, "Update the app.js file");

    ide.open(ws);
  }

  @Test(priority = 1)
  public void checkoutReferenceByHashCommit() throws Exception {
    // preconditions
    String branchDetachedMess = String.format("(detached from %s)", sha1);
    String hashCommit = sha1.substring(0, 8);
    String wrongHashCommit = String.format("%s ##", hashCommit);
    String failMessage = String.format("Branch name %s is not allowed", wrongHashCommit);

    // import the test repo
    projectExplorer.waitProjectExplorer();
    git.importJavaApp(testRepo.getHtmlUrl(), PROJECT_NAME, BLANK);

    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByPath(String.format("%s/%s", PROJECT_NAME, JS_FILE));
    editor.waitActive();
    editor.waitTextIntoEditor(UPDATE_FILE);

    // check the name of the default branch
    openBranchPanelAndWaitRefHeadName(DEFAULT_BRANCH);

    // check the 'Cancel' button of the 'Checkout Reference' form
    menu.runCommand(GIT, CHECKOUT_REFERENCE);
    git.waitReferenceFormIsOpened();
    git.clickCheckoutReferenceCancelButton();

    // perform checkout reference to wrong hash commit
    performCheckoutReference(wrongHashCommit);

    git.waitGitStatusBarWithMess(failMessage);

    // perform git checkout by not fully hash of specific commit
    performCheckoutReference(hashCommit);

    editor.selectTabByName(JS_FILE);
    editor.waitTextIntoEditor(CHANGE_FILE);
    editor.waitTextNotPresentIntoEditor(UPDATE_FILE);

    // switch to default branch
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(GIT, BRANCHES);
    git.waitBranchInTheListWithCoState(branchDetachedMess);
    git.selectBranchAndClickCheckoutBtn(DEFAULT_BRANCH);

    openBranchPanelAndWaitRefHeadName(DEFAULT_BRANCH);

    editor.selectTabByName(JS_FILE);
    editor.waitTextIntoEditor(UPDATE_FILE);

    // perform git checkout by hash of specific commit
    performCheckoutReference(sha1);

    openBranchPanelAndWaitRefHeadName(branchDetachedMess);

    // check the git history
    openGitHistoryForm();

    assertTrue(gitHistory.getTopCommitRevision().contains(hashCommit));

    git.clickOnHistoryRowInСommitsList(0);
    git.waitContentInHistoryEditor(COMMIT_MESSAGE);
    git.closeGitHistoryForm();
  }

  @Test(priority = 2)
  public void checkoutReferenceByTagName() {
    // git checkout to wrong tag name
    performCheckoutReference("version1");

    git.waitGitStatusBarWithMess(MESSAGE_TAG_WRONG);

    // git checkout to tag 'version_1'
    performCheckoutReference(TAG_NAME_1);

    openBranchPanelAndWaitRefHeadName(BRANCH_DETACHED_TAG_1);

    editor.selectTabByName(JS_FILE);
    editor.waitTextIntoEditor(CHANGE_FILE_1);

    // check the git history
    openGitHistoryForm();

    git.clickOnHistoryRowInСommitsList(0);
    git.waitContentInHistoryEditor(COMMIT_MESSAGE_TAG_1);
    git.closeGitHistoryForm();

    // switch to default branch
    menu.runCommand(GIT, BRANCHES);
    git.waitBranchInTheListWithCoState(BRANCH_DETACHED_TAG_1);
    git.selectBranchAndClickCheckoutBtn(DEFAULT_BRANCH);

    openBranchPanelAndWaitRefHeadName(DEFAULT_BRANCH);

    // switch to another tag
    performCheckoutReference(TAG_NAME_2);

    openBranchPanelAndWaitRefHeadName(BRANCH_DETACHED_TAG_2);

    editor.selectTabByName(JS_FILE);
    editor.waitTextIntoEditor(CHANGE_FILE_2);
  }

  private void openGitHistoryForm() {
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(GIT, SHOW_HISTORY);
    git.waitHistoryFormToOpen();
  }

  private void performCheckoutReference(String refName) {
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(GIT, CHECKOUT_REFERENCE);
    git.typeReferenceAndConfirm(refName);
  }

  private void openBranchPanelAndWaitRefHeadName(String refHeadName) {
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(GIT, BRANCHES);
    git.waitBranchInTheListWithCoState(refHeadName);
    git.closeBranchesForm();
  }
}
