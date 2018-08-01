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

import static org.eclipse.che.selenium.pageobject.Wizard.TypeProject.MAVEN;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.client.TestGitHubRepository;
import org.eclipse.che.selenium.core.client.TestUserPreferencesServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.git.Git;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author aleksandr shmaraev */
@Test(groups = TestGroup.GITHUB)
public class FetchAndMergeRemoteBranchIntoLocalTest {
  private static final String PROJECT_NAME = NameGenerator.generate("FetchAndMergeTest-", 4);
  private static final String NEW_CONTENT =
      String.format("//change_content-%s", String.valueOf(System.currentTimeMillis()));

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;
  @Inject private DefaultTestUser productUser;
  @Inject private TestGitHubRepository testRepo;

  @Inject
  @Named("github.username")
  private String gitHubUsername;

  @Inject private ProjectExplorer projectExplorer;
  @Inject private Menu menu;
  @Inject private Git git;
  @Inject private Events eventsPanel;
  @Inject private CodenvyEditor editor;
  @Inject private TestUserPreferencesServiceClient testUserPreferencesServiceClient;

  @BeforeClass
  public void prepare() throws Exception {
    testUserPreferencesServiceClient.addGitCommitter(gitHubUsername, productUser.getEmail());

    Path entryPath = Paths.get(getClass().getResource("/projects/guess-project").getPath());
    testRepo.addContent(entryPath);

    ide.open(ws);
  }

  @Test
  public void fetchUpdatesAndMergeRemoteBranchTolocal() throws IOException {
    // preconditions and import the test repo
    String textFile = "README.md";
    String javaFile = "AppController";
    String jspFile = "index.jsp";
    String pathToJavaFile = "src/main/java/org/eclipse/qa/examples";
    String pathToJspFile = "src/main/webapp";
    String originMaster = "origin/master";
    String fetchMess = String.format("Fetched from %s", testRepo.getHtmlUrl());
    String mergeMess1 = "Fast-forward Merged commits:";
    String mergeMess2 = "New HEAD commit: ";
    String mergeMess3 = "Already up-to-date";

    projectExplorer.waitProjectExplorer();
    git.importJavaApp(testRepo.getHtmlUrl(), PROJECT_NAME, MAVEN);

    // change content in the test repo on GitHub
    testRepo.deleteFile(String.format("%s/%s", pathToJspFile, jspFile));
    testRepo.changeFileContent(
        String.format("%s/%s.java", pathToJavaFile, javaFile), NEW_CONTENT, "file-" + NEW_CONTENT);
    testRepo.changeFileContent(textFile, NEW_CONTENT, "file-" + NEW_CONTENT);

    performFetch();
    git.waitGitStatusBarWithMess(fetchMess);

    // open project and check that content is not changed
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByPath(
        String.format("%s/%s/%s.java", PROJECT_NAME, pathToJavaFile, javaFile));
    editor.waitActive();
    editor.waitTextNotPresentIntoEditor(NEW_CONTENT);
    projectExplorer.openItemByPath(String.format("%s/%s", PROJECT_NAME, textFile));
    editor.waitActive();
    editor.waitTextNotPresentIntoEditor(NEW_CONTENT);
    projectExplorer.waitVisibilityByName(jspFile);

    mergeRemoteBranch(originMaster);

    git.waitGitStatusBarWithMess(mergeMess1);
    git.waitGitStatusBarWithMess(mergeMess2);
    eventsPanel.clickEventLogBtn();
    eventsPanel.waitExpectedMessage(mergeMess1);

    // check the content is changed
    editor.selectTabByName(javaFile);
    editor.waitActive();
    editor.waitTextIntoEditor(NEW_CONTENT);
    editor.selectTabByName(textFile);
    editor.waitActive();
    editor.waitTextIntoEditor(NEW_CONTENT);
    projectExplorer.waitItemInvisibility(
        String.format("%s/%s/%s", PROJECT_NAME, "src/main/webapp", jspFile));

    // merge again
    mergeRemoteBranch(originMaster);
    git.waitGitStatusBarWithMess(mergeMess3);

    // wait commit in git history
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.SHOW_HISTORY);
    git.waitTextInHistoryForm(NEW_CONTENT);
    git.clickOnHistoryRowInСommitsList(0);
    git.waitContentInHistoryEditor(NEW_CONTENT);
  }

  private void performFetch() {
    menu.runCommand(
        TestMenuCommandsConstants.Git.GIT,
        TestMenuCommandsConstants.Git.Remotes.REMOTES_TOP,
        TestMenuCommandsConstants.Git.Remotes.FETCH);

    git.waitFetchFormOpened();
    git.clickOnFetchButton();
    git.waitFetchFormClosed();
  }

  private void mergeRemoteBranch(String nameRemoteBranch) {
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.MERGE);

    git.waitMergeView();
    git.waitMergeReferencePanel();
    git.waitMergeExpandRemoteBranchIcon();
    git.clickMergeExpandRemoteBranchIcon();
    git.waitItemInMergeList(nameRemoteBranch);
    git.clickItemInMergeList(nameRemoteBranch);
    git.clickMergeBtn();
    git.waitMergeViewClosed();
  }
}
