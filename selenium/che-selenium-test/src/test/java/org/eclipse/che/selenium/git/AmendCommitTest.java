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
package org.eclipse.che.selenium.git;

import static org.eclipse.che.selenium.core.constant.TestGitConstants.GIT_INITIALIZED_SUCCESS;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.COMMIT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.GIT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.INITIALIZE_REPOSITORY;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.SHOW_HISTORY;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.STATUS;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestUserPreferencesServiceClient;
import org.eclipse.che.selenium.core.constant.TestGitConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev */
@Test(groups = TestGroup.GITHUB)
public class AmendCommitTest {
  private static final String PROJECT_NAME = NameGenerator.generate("AmendCommit_", 4);
  private static final String JAVA_FILE_TAB_NAME = "AppController";
  private static final String CHANGE_CONTENT = "<!--change content-->";
  private static final String COMMIT_MESSAGE = "first commit";
  private static final String NOTHING_TO_COMMIT =
      " On branch master\n" + " nothing to commit, working directory clean";
  private static final String AMEND_COMMIT_MESS = "changed commit";
  private static final String AMEND_CONTENT = "amend previous content";
  private static final String AMEND_COMMIT = "amend previous commit";

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;
  @Inject private DefaultTestUser productUser;

  @Inject
  @Named("github.username")
  private String gitHubUsername;

  @Inject
  @Named("github.password")
  private String gitHubPassword;

  @Inject private ProjectExplorer projectExplorer;
  @Inject private Menu menu;
  @Inject private AskDialog askDialog;
  @Inject private org.eclipse.che.selenium.pageobject.git.Git git;
  @Inject private Events events;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private TestUserPreferencesServiceClient testUserPreferencesServiceClient;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void prepare() throws Exception {
    URL resource = getClass().getResource("/projects/default-spring-project");
    testUserPreferencesServiceClient.addGitCommitter(gitHubUsername, productUser.getEmail());

    testProjectServiceClient.importProject(
        ws.getId(), Paths.get(resource.toURI()), PROJECT_NAME, ProjectTemplates.MAVEN_SPRING);
    ide.open(ws);
  }

  @Test
  public void checkAmendPreviousCommit() {
    String pathToFileItem = PROJECT_NAME + "/src/main/java/org.eclipse.qa.examples";
    String javaFileName = "AppController.java";

    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(GIT, INITIALIZE_REPOSITORY);
    askDialog.confirmAndWaitClosed();
    git.waitGitStatusBarWithMess(GIT_INITIALIZED_SUCCESS);
    events.clickEventLogBtn();
    events.waitExpectedMessage(GIT_INITIALIZED_SUCCESS);
    loader.waitOnClosed();

    // perform init commit
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(GIT, COMMIT);
    git.waitAndRunCommit("init");
    loader.waitOnClosed();

    // edit java file and commit the change
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    projectExplorer.expandPathInProjectExplorerAndOpenFile(pathToFileItem, javaFileName);
    editor.waitActive();
    editor.setCursorToLine(15);
    editor.typeTextIntoEditor("//" + CHANGE_CONTENT);
    editor.waitTextIntoEditor("//" + CHANGE_CONTENT);
    menu.runCommand(GIT, COMMIT);
    git.waitAndRunCommit(COMMIT_MESSAGE);
    git.waitGitStatusBarWithMess(TestGitConstants.COMMIT_MESSAGE_SUCCESS);

    // need to repeat selection of the project (https://github.com/eclipse/che/issues/10520)
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(GIT, STATUS);
    git.waitGitStatusBarWithMess(NOTHING_TO_COMMIT);

    // view git history
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(GIT, SHOW_HISTORY);
    loader.waitOnClosed();
    git.waitTextInHistoryForm(COMMIT_MESSAGE);
    git.closeGitHistoryForm();

    // only amend commit message
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(GIT, COMMIT);
    git.waitAndRunAmendCommitMessage(AMEND_COMMIT_MESS);
    menu.runCommand(GIT, SHOW_HISTORY);
    git.waitHistoryFormToOpen();
    git.waitCommitInHistoryFormNotPresent(COMMIT_MESSAGE);
    git.waitCommitInHistoryForm(AMEND_COMMIT_MESS);
    git.closeGitHistoryForm();

    // perform amend previous commit
    editor.selectTabByName(JAVA_FILE_TAB_NAME);
    editor.waitActive();
    editor.setCursorToLine(12);
    editor.selectLineAndDelete();
    editor.typeTextIntoEditor("//" + AMEND_CONTENT);
    editor.waitTextIntoEditor("//" + AMEND_CONTENT);
    menu.runCommand(GIT, COMMIT);
    git.waitAndRunAmendPreviousCommit(AMEND_COMMIT);
    git.waitGitStatusBarWithMess(TestGitConstants.COMMIT_MESSAGE_SUCCESS);

    // need to repeat selection of the project (https://github.com/eclipse/che/issues/10520)
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(GIT, STATUS);
    git.waitGitStatusBarWithMess(NOTHING_TO_COMMIT);

    // view git history
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(GIT, SHOW_HISTORY);
    loader.waitOnClosed();
    git.waitTextInHistoryForm(AMEND_COMMIT);
    git.waitCommitInHistoryFormNotPresent(AMEND_COMMIT_MESS);
    loader.waitOnClosed();
    git.clickOnHistoryRowInСommitsList(0);
    git.waitContentInHistoryEditor(AMEND_COMMIT);
    git.clickCompareBtnGitHistory();
    loader.waitOnClosed();
    checkChangesIntoCompareForm(AMEND_CONTENT);
  }

  private void checkChangesIntoCompareForm(String expText) {
    git.waitGitCompareFormIsOpen();
    git.waitExpTextIntoCompareLeftEditor(expText);
    git.waitTextNotPresentIntoCompareRightEditor(expText);
  }
}
