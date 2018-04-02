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
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.GIT;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.che.commons.json.JsonParseException;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.client.TestGitHubRepository;
import org.eclipse.che.selenium.core.client.TestGitHubServiceClient;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestUserPreferencesServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ImportProjectFromLocation;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.Preferences;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.eclipse.che.selenium.pageobject.git.Git;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev */
@Test(groups = TestGroup.GITHUB)
public class ImportProjectIntoSpecifiedBranchTest {
  private static final String PROJECT_NAME = NameGenerator.generate("ImportIntoBranch_1-", 4);
  private static final String BRANCH_1 = "xxx";
  private static final String BRANCH_2 = "zzz";
  private static final String BRANCH_3 = "second_branch";

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;

  @Inject(optional = true)
  @Named("github.username")
  private String gitHubUsername;

  @Inject(optional = true)
  @Named("github.password")
  private String gitHubPassword;

  @Inject private ProjectExplorer projectExplorer;
  @Inject private Menu menu;
  @Inject private Git git;
  @Inject private Loader loader;
  @Inject private Preferences preferences;
  @Inject private Wizard projectWizard;
  @Inject private ImportProjectFromLocation importProject;
  @Inject private TestGitHubServiceClient gitHubClientService;
  @Inject private TestProjectServiceClient projectServiceClient;
  @Inject private TestUserPreferencesServiceClient testUserPreferencesServiceClient;
  @Inject private TestUser testUser;
  @Inject private TestGitHubRepository gitHubRepository;

  @BeforeClass
  public void prepare() throws Exception {
    testUserPreferencesServiceClient.addGitCommitter(gitHubUsername, testUser.getEmail());

    Path sourceProject = Paths.get(getClass().getResource("/projects/Repo_For_Test").toURI());
    gitHubRepository.addContent(sourceProject);

    gitHubRepository.createBranchFromMaster(BRANCH_1);
    gitHubRepository.createBranchFromMaster(BRANCH_2);
    gitHubRepository.createBranchFromMaster(BRANCH_3);

    ide.open(ws);
  }

  @AfterMethod
  public void tearDown() throws Exception {
    projectServiceClient.deleteResource(ws.getId(), PROJECT_NAME);
  }

  @Test
  public void checkImportProjectInBranchBySshUrl() throws IOException, JsonParseException {
    projectExplorer.waitProjectExplorer();
    performImportIntoBranch(gitHubRepository.getHtmlUrl(), PROJECT_NAME, BRANCH_1);
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.waitAndSelectItemByName(PROJECT_NAME);
    loader.waitOnClosed();
    menu.runCommand(GIT, BRANCHES);
    git.waitBranchInTheListWithCoState(BRANCH_1);
    git.closeBranchesForm();
    projectExplorer.quickExpandWithJavaScript();
    loader.waitOnClosed();
    projectExplorer.openItemByPath(
        PROJECT_NAME + "/src/main/java/com/codenvy/example/spring/GreetingController.java");
  }

  @Test(priority = 1)
  public void checkImportProjectInBranchByHttpsUrl() throws IOException, JsonParseException {
    projectExplorer.waitProjectExplorer();
    performImportIntoBranch(gitHubRepository.getHtmlUrl(), PROJECT_NAME, BRANCH_2);
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.waitAndSelectItemByName(PROJECT_NAME);
    loader.waitOnClosed();
    menu.runCommand(GIT, BRANCHES);
    git.waitBranchInTheListWithCoState(BRANCH_2);
    git.closeBranchesForm();
    projectExplorer.quickExpandWithJavaScript();
    loader.waitOnClosed();
    projectExplorer.openItemByPath(
        PROJECT_NAME + "/src/main/java/com/codenvy/example/spring/GreetingController.java");
  }

  @Test(priority = 2)
  public void checkImportProjectInBranchFromGitHub() throws Exception {
    projectExplorer.waitProjectExplorer();
    importIntoBranchFromGitHub();
    projectWizard.waitCreateProjectWizardForm();
    projectWizard.selectTypeProject(Wizard.TypeProject.MAVEN);
    loader.waitOnClosed();
    projectWizard.clickSaveButton();
    loader.waitOnClosed();
    projectWizard.waitCreateProjectWizardFormIsClosed();
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.waitAndSelectItemByName(PROJECT_NAME);
    loader.waitOnClosed();
    menu.runCommand(GIT, BRANCHES);
    git.waitBranchInTheListWithCoState(BRANCH_3);
    git.closeBranchesForm();
    projectExplorer.quickExpandWithJavaScript();
    loader.waitOnClosed();
    projectExplorer.openItemByPath(
        PROJECT_NAME + "/src/main/java/helloworld/GreetingController.java");
  }

  private void performImportIntoBranch(String url, String projectName, String branchName)
      throws IOException, JsonParseException {
    projectExplorer.waitProjectExplorer();
    menu.runCommand(
        TestMenuCommandsConstants.Workspace.WORKSPACE,
        TestMenuCommandsConstants.Workspace.IMPORT_PROJECT);
    importProject.waitMainForm();
    loader.waitOnClosed();
    importProject.selectGitSourceItem();
    loader.waitOnClosed();
    importProject.typeURi(url);
    importProject.typeProjectName(projectName);
    importProject.waitBranchIsNotSelected();
    importProject.clickBranchCheckbox();
    importProject.waitBranchIsSelected();
    importProject.typeBranchName(branchName);
    importProject.clickImportBtn();
    importProject.waitMainFormIsClosed();
    loader.waitOnClosed();
    projectWizard.waitCreateProjectWizardForm();
    projectWizard.selectTypeProject(Wizard.TypeProject.MAVEN);
    loader.waitOnClosed();
    projectWizard.clickSaveButton();
    loader.waitOnClosed();
    projectWizard.waitCreateProjectWizardFormIsClosed();
    projectExplorer.waitProjectExplorer();
  }

  private void importIntoBranchFromGitHub() throws Exception {
    menu.runCommand(
        TestMenuCommandsConstants.Workspace.WORKSPACE,
        TestMenuCommandsConstants.Workspace.IMPORT_PROJECT);
    importProject.waitMainForm();
    loader.waitOnClosed();
    importProject.selectGitHubSourceItem();
    importProject.waitLoadRepoBtn();
    importProject.typeURi(gitHubRepository.getHtmlUrl());
    importProject.typeProjectName(PROJECT_NAME);
    importProject.waitBranchIsNotSelected();
    importProject.clickBranchCheckbox();
    importProject.waitBranchIsSelected();
    importProject.typeBranchName(BRANCH_3);
    importProject.clickImportBtn();
    importProject.waitMainFormIsClosed();
    loader.waitOnClosed();
  }
}
