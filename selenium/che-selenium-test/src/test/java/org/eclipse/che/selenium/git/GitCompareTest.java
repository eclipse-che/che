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

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Edit.DELETE;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Edit.EDIT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.ADD_TO_INDEX;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.COMMIT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.Compare.COMPARE_LATEST_VER;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.Compare.COMPARE_TOP;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.Compare.COMPARE_WITH_BRANCH;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.Compare.COMPARE_WITH_REVISION;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.GIT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project.New.JAVA_CLASS;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project.New.NEW;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project.PROJECT;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.client.TestGitHubRepository;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestUserPreferencesServiceClient;
import org.eclipse.che.selenium.core.constant.TestGitConstants;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.WarningDialog;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.eclipse.che.selenium.pageobject.git.Git;
import org.eclipse.che.selenium.pageobject.git.GitCompare;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev */
@Test(groups = TestGroup.GITHUB)
public class GitCompareTest {
  private static final String PROJECT_NAME = NameGenerator.generate("GitCompare_", 4);
  private static final String PATH_TO_APP_CONTROLLER =
      PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/AppController.java";
  private static final String PATH_TO_ACLASS =
      PROJECT_NAME + "/src/main/java/che/eclipse/sample/Aclass.java";
  private static final String PATH_TO_NEW_CLASS =
      PROJECT_NAME + "/src/main/java/che/eclipse/sample/NewClass.java";
  private static final String LEFT_COMPARE_ST = "Line 2 : Column 1";
  private static final String TEXT_GROUP =
      "src/main/java\n"
          + "che/eclipse/sample\n"
          + "Aclass.java\n"
          + "NewClass.java\n"
          + "org/eclipse/qa/examples\n"
          + "AppController.java";

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;
  @Inject private DefaultTestUser productUser;
  @Inject private TestGitHubRepository testRepo;

  @Inject
  @Named("github.username")
  private String gitHubUsername;

  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Menu menu;
  @Inject private Git git;
  @Inject private GitCompare gitCompare;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private AskForValueDialog askForValueDialog;
  @Inject private AskDialog askDialog;
  @Inject private WarningDialog warningDialog;
  @Inject private TestUserPreferencesServiceClient testUserPreferencesServiceClient;

  @BeforeClass
  public void prepare() throws Exception {
    testUserPreferencesServiceClient.addGitCommitter(gitHubUsername, productUser.getEmail());

    Path entryPath =
        Paths.get(getClass().getResource("/projects/default-spring-project").getPath());
    testRepo.addContent(entryPath);

    ide.open(ws);
    projectExplorer.waitProjectExplorer();

    git.importJavaApp(testRepo.getHtmlUrl(), PROJECT_NAME, Wizard.TypeProject.MAVEN);
    createBranch();
  }

  @Test(priority = 1)
  public void checkCompareWithLatestRepoVersion() throws Exception {
    // expand the project and do changes in the java files
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByPath(PATH_TO_APP_CONTROLLER);

    // check compare after update file
    testProjectServiceClient.updateFile(
        ws.getId(), PATH_TO_APP_CONTROLLER, "// <<< checking compare content >>>\n");

    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(GIT, COMPARE_TOP, COMPARE_LATEST_VER);
    git.waitGitCompareFormIsOpen();
    git.waitExpTextIntoCompareLeftEditor("// <<< checking compare content >>>");
    git.waitTextNotPresentIntoCompareRightEditor("// <<< checking compare content >>>");
    git.closeGitCompareForm();

    createNewJavaFile();
    deleteJavaFile();
    addToIndex();

    // check compare to deleting the file
    menu.runCommand(GIT, COMPARE_TOP, COMPARE_LATEST_VER);
    git.waitGroupGitCompareIsOpen();
    git.waitExpTextInGroupGitCompare(TEXT_GROUP);
    git.selectFileInChangedFilesTreePanel("Aclass.java");
    git.clickOnGroupCompareButton();
    git.waitGitCompareFormIsOpen();
    gitCompare.waitTextNotPresentIntoLeftEditor("public class Aclass");
    gitCompare.waitExpectedTextIntoRightEditor("public class Aclass");
    git.closeGitCompareForm();

    // check compare to adding the file
    git.waitGroupGitCompareIsOpen();
    git.selectFileInChangedFilesTreePanel("NewClass.java");
    git.clickOnGroupCompareButton();
    git.waitGitCompareFormIsOpen();
    git.waitExpTextIntoCompareLeftEditor("public class NewClass");
    git.waitTextNotPresentIntoCompareRightEditor("public class NewClass");
    git.closeGitCompareForm();
    git.closeGroupGitCompareForm();

    // check the compare warning dialog after commit
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(GIT, COMMIT);
    git.waitAndRunCommit("Change files");
    git.waitGitStatusBarWithMess(TestGitConstants.COMMIT_MESSAGE_SUCCESS);
    menu.runCommand(GIT, COMPARE_TOP, COMPARE_LATEST_VER);
    warningDialog.waitWaitWarnDialogWindowWithSpecifiedTextMess(
        "There are no changes in the selected item.");
    warningDialog.clickOkBtn();
  }

  @Test(priority = 2)
  public void checkCompareWithBranch() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitAndSelectItem(PROJECT_NAME);

    // check the 'Close' button
    menu.runCommand(GIT, COMPARE_TOP, COMPARE_WITH_BRANCH);
    git.waitGitCompareBranchFormIsOpen();
    gitCompare.clickOnCloseBranchCompareButton();

    // check the 'git compare' for another local branch
    menu.runCommand(GIT, COMPARE_TOP, COMPARE_WITH_BRANCH);
    git.waitGitCompareBranchFormIsOpen();
    git.selectBranchIntoGitCompareBranchForm("newbranch");
    git.clickOnCompareBranchFormButton();
    git.waitGroupGitCompareIsOpen();
    git.waitExpTextInGroupGitCompare(TEXT_GROUP);

    // check the 'Next' diff button
    git.selectFileInChangedFilesTreePanel("Aclass.java");
    git.clickOnGroupCompareButton();
    git.waitGitCompareFormIsOpen();
    gitCompare.clickOnNextDiffButton();
    git.setFocusOnLeftGitCompareEditor();
    git.waitExpTextIntoCompareLeftEditor("public class NewClass");
    gitCompare.clickOnNextDiffButton();
    git.waitExpTextIntoCompareLeftEditor("// <<< checking compare content >>>");

    // check the 'Previous' diff button
    gitCompare.clickOnPreviousDiffButton();
    git.waitExpTextIntoCompareLeftEditor("public class NewClass");
    git.closeGitCompareForm();
    git.closeGroupGitCompareForm();

    // check the 'git compare' for remote branch
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(GIT, COMPARE_TOP, COMPARE_WITH_BRANCH);
    git.waitGitCompareBranchFormIsOpen();
    git.selectBranchIntoGitCompareBranchForm("origin/master");
    git.clickOnCompareBranchFormButton();
    git.waitGroupGitCompareIsOpen();
    git.waitExpTextInGroupGitCompare(TEXT_GROUP);
    git.closeGroupGitCompareForm();
  }

  @Test(priority = 3)
  public void checkCompareWithRevision() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitAndSelectItem(PATH_TO_APP_CONTROLLER);

    // check the 'Close' button
    menu.runCommand(GIT, COMPARE_TOP, COMPARE_WITH_REVISION);
    git.waitGitCompareRevisionFormIsOpen();
    git.clickOnCloseRevisionButton();

    // check the compare with revision
    menu.runCommand(GIT, COMPARE_TOP, COMPARE_WITH_REVISION);
    git.waitGitCompareRevisionFormIsOpen();
    git.selectRevisionIntoCompareRevisionForm(1);
    git.clickOnRevisionCompareButton();
    git.waitGitCompareFormIsOpen();
    git.waitExpTextIntoCompareLeftEditor("// <<< checking compare content >>>");
    git.waitTextNotPresentIntoCompareRightEditor("// <<< checking compare content >>>");

    // add change to file in the compare editor
    git.setFocusOnLeftGitCompareEditor();
    git.setCursorToLine(2, LEFT_COMPARE_ST);
    git.typeTextIntoGitCompareEditor("//change content from compare editor");
    git.waitExpTextIntoCompareLeftEditor("//change content from compare editor");
    git.clickOnGitCompareCloseButton();
    askDialog.confirmAndWaitClosed();
    git.waitGitCompareFormIsClosed();
    git.waitGitCompareRevisionFormIsOpen();
    git.clickOnCloseRevisionButton();

    // check the change in the editor
    projectExplorer.openItemByPath(PATH_TO_APP_CONTROLLER);
    editor.waitActive();
    editor.waitTextIntoEditor("//change content from compare editor");
  }

  private void createBranch() {
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(GIT, TestMenuCommandsConstants.Git.BRANCHES);
    git.waitBranchInTheList("master");
    git.waitDisappearBranchName("newbranch");
    git.waitEnabledAndClickCreateBtn();
    git.typeAndWaitNewBranchName("newbranch");
    git.waitBranchInTheList("master");
    git.waitBranchInTheList("newbranch");
    git.closeBranchesForm();
  }

  private void deleteJavaFile() {
    projectExplorer.clickOnRefreshTreeButton();
    projectExplorer.waitAndSelectItem(PATH_TO_ACLASS);
    projectExplorer.waitItemIsSelected(PATH_TO_ACLASS);
    menu.runCommand(EDIT, DELETE);
    askDialog.waitFormToOpen();
    askDialog.containsText("Delete file \"Aclass.java\"?");
    askDialog.clickOkBtn();
    loader.waitOnClosed();
  }

  private void createNewJavaFile() {
    projectExplorer.waitAndSelectItem(PROJECT_NAME + "/src/main/java/che/eclipse/sample");
    menu.runCommand(PROJECT, NEW, JAVA_CLASS);
    loader.waitOnClosed();
    askForValueDialog.waitNewJavaClassOpen();
    askForValueDialog.typeTextInFieldName("NewClass");
    askForValueDialog.clickOkBtnNewJavaClass();
    askForValueDialog.waitNewJavaClassClose();
    projectExplorer.waitItem(PATH_TO_NEW_CLASS);
  }

  private void addToIndex() {
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(GIT, ADD_TO_INDEX);
    git.confirmAddToIndexForm();
  }
}
