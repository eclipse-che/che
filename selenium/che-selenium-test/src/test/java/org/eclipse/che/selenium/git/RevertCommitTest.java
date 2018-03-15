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

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.COMMIT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.GIT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.INITIALIZE_REPOSITORY;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.REVERT_COMMIT;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

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
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.git.Git;
import org.eclipse.che.selenium.pageobject.git.GitRevertCommit;
import org.eclipse.che.selenium.pageobject.git.GitStatusBar;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Anatolii Bazko
 * @author Aleksandr Shmaraiev
 */
@Test(groups = TestGroup.GITHUB)
public class RevertCommitTest {
  private static final String PROJECT_NAME = NameGenerator.generate("GitRevertProject-", 4);

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;
  @Inject private TestUser productUser;

  @Inject(optional = true)
  @Named("github.username")
  private String gitHubUsername;

  @Inject(optional = true)
  @Named("github.password")
  private String gitHubPassword;

  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private AskDialog askDialog;
  @Inject private Menu menu;
  @Inject private Git git;
  @Inject private CodenvyEditor editor;
  @Inject private GitRevertCommit gitRevertCommit;
  @Inject private TestUserPreferencesServiceClient testUserPreferencesServiceClient;
  @Inject private GitStatusBar gitStatusBar;

  @BeforeClass
  public void prepare() throws Exception {
    URL resource = getClass().getResource("/projects/git-pull-test");
    testUserPreferencesServiceClient.addGitCommitter(gitHubUsername, productUser.getEmail());
    testProjectServiceClient.importProject(
        ws.getId(), Paths.get(resource.toURI()), PROJECT_NAME, ProjectTemplates.MAVEN_SPRING);
    ide.open(ws);
  }

  @Test
  public void shouldRevertCommit() throws Exception {
    // preconditions
    String newFile = "newFile.xml";
    String htmlFile = "file.html";
    String changeContent = "<! change content>";
    String initRepoMessage = "init";
    String createFileMessage = "create newFile.xml";
    String updateFileMessage = "update file.html";
    String pathToNewFile = String.format("%s/%s", PROJECT_NAME, newFile);
    String pathToHtmlFile = String.format("%s/%s", PROJECT_NAME, htmlFile);

    // perform git initialize repository
    projectExplorer.waitProjectExplorer();

    gitInitRepo();
    commitFiles(initRepoMessage);

    // create new file and perform commit
    testProjectServiceClient.createFileInProject(ws.getId(), PROJECT_NAME, newFile, changeContent);
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.waitItem(pathToNewFile);

    commitFiles(createFileMessage);

    // perform git revert and check author and comment
    projectExplorer.waitAndSelectItem(PROJECT_NAME);

    performGitRevert();

    // check that 'newFile.xml' is disappear from the project tree
    projectExplorer.waitDisappearItemByPath(pathToNewFile);

    // update the 'file.html' and commit change
    testProjectServiceClient.updateFile(ws.getId(), pathToHtmlFile, changeContent);
    projectExplorer.waitAndSelectItem(PROJECT_NAME);

    commitFiles(updateFileMessage);

    // perform revert and check that 'change content' is not present in the editor
    performGitRevert();

    projectExplorer.openItemByPath(pathToHtmlFile);
    editor.waitActive();
    editor.waitTextNotPresentIntoEditor(changeContent);
  }

  private void gitInitRepo() {
    menu.runCommand(GIT, INITIALIZE_REPOSITORY);
    askDialog.waitFormToOpen();
    askDialog.clickOkBtn();
    askDialog.waitFormToClose();
    git.waitGitStatusBarWithMess(TestGitConstants.GIT_INITIALIZED_SUCCESS);
  }

  private void performGitRevert() {
    menu.runCommand(GIT, REVERT_COMMIT);

    String revision = gitRevertCommit.getTopCommitRevision();
    String comment = gitRevertCommit.getTopCommitComment();

    gitRevertCommit.waitRevertPanelOpened();
    gitRevertCommit.selectRevision(revision);
    gitRevertCommit.clickRevertButton();
    gitRevertCommit.waitRevertPanelClosed();

    gitStatusBar.waitMessageInGitTab("Reverted commits: - " + revision);

    menu.runCommand(GIT, REVERT_COMMIT);

    // TODO delete the message after resolve issue
    assertEquals(
        gitRevertCommit.getTopCommitAuthor(),
        gitHubUsername,
        "Known issue https://github.com/eclipse/che/issues/9066");

    assertTrue(gitRevertCommit.getTopCommitComment().contains("Revert \"" + comment + "\""));

    gitRevertCommit.clickCancelButton();
  }

  private void commitFiles(String commiitMess) {
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(GIT, COMMIT);

    git.waitAndRunCommit(commiitMess);
    git.waitGitStatusBarWithMess(TestGitConstants.COMMIT_MESSAGE_SUCCESS);
  }
}
