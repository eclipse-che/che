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

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestGitHubServiceClient;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ImportProjectFromLocation;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.Preferences;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev */
public class ImportRecursiveSubmoduleTest {
  public static final String PROJECT_NAME = NameGenerator.generate("ProjectSubmodule-", 4);
  private static final String SUBMODULE_NAME_1 = "Repo_For_Test";
  private static final String SUBMODULE_NAME_2 = "testRepo";

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;

  @Inject
  @Named("github.username")
  private String gitHubUsername;

  @Inject
  @Named("github.password")
  private String gitHubPassword;

  @Inject private ProjectExplorer projectExplorer;
  @Inject private Menu menu;
  @Inject private Loader loader;
  @Inject private ImportProjectFromLocation importProject;
  @Inject private Preferences preferences;
  @Inject private Wizard projectWizard;
  @Inject private CodenvyEditor editor;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private TestGitHubServiceClient gitHubClientService;

  @BeforeClass
  public void prepare() throws Exception {
    ide.open(ws);

    // authorize application on GitHub
    menu.runCommand(
        TestMenuCommandsConstants.Profile.PROFILE_MENU,
        TestMenuCommandsConstants.Profile.PREFERENCES);
    preferences.waitPreferencesForm();
    preferences.generateAndUploadSshKeyOnGithub(gitHubUsername, gitHubPassword);
  }

  @AfterMethod
  public void tearDown() throws Exception {
    testProjectServiceClient.deleteResource(ws.getId(), PROJECT_NAME);
  }

  @Test
  public void checkImportProjectSubmoduleByHttpsUrl() throws Exception {
    projectExplorer.waitProjectExplorer();
    importRecursivelyFromGitUrl(
        "https://github.com/" + gitHubUsername + "/ProjectGitSubmodule.git", PROJECT_NAME);
    openSubmoduleOne(PROJECT_NAME);
    openSubmoduleTwo(PROJECT_NAME);
  }

  @Test(priority = 1)
  public void checkImportProjectSubmoduleBySshUrl() throws Exception {
    projectExplorer.waitProjectExplorer();
    importRecursivelyFromGitUrl(
        "git@github.com:" + gitHubUsername + "/ProjectGitSubmodule.git", PROJECT_NAME);
    openSubmoduleOne(PROJECT_NAME);
    openSubmoduleTwo(PROJECT_NAME);
  }

  @Test(priority = 2)
  public void checkImportProjectSubmoduleFromGithub() throws Exception {
    projectExplorer.waitProjectExplorer();
    importRecursivelyFromGitHub(PROJECT_NAME);
    openSubmoduleOne(PROJECT_NAME);
    openSubmoduleTwo(PROJECT_NAME);
  }

  private void importRecursivelyFromGitUrl(String url, String projectName) throws Exception {
    loader.waitOnClosed();
    menu.runCommand(
        TestMenuCommandsConstants.Workspace.WORKSPACE,
        TestMenuCommandsConstants.Workspace.IMPORT_PROJECT);
    importProject.waitMainForm();
    loader.waitOnClosed();
    importProject.selectGitSourceItem();
    loader.waitOnClosed();
    importProject.typeURi(url);
    importProject.typeProjectName(projectName);
    importProject.waitImportRecursivelyIsNotSelected();
    importProject.clickOnImportRecursivelyCheckbox();
    importProject.waitImportRecursivelyIsSelected();
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
    projectExplorer.waitItem(projectName);
  }

  private void importRecursivelyFromGitHub(String projectName) throws Exception {
    loader.waitOnClosed();
    menu.runCommand(
        TestMenuCommandsConstants.Workspace.WORKSPACE,
        TestMenuCommandsConstants.Workspace.IMPORT_PROJECT);
    importProject.waitMainForm();
    loader.waitOnClosed();
    importProject.selectGitHubSourceItem();
    loader.waitOnClosed();
    importProject.waitLoadRepoBtn();
    importProject.clickLoadRepoBtn();
    loader.waitOnClosed();
    importProject.selectItemInAccountList(
        gitHubClientService.getName(gitHubUsername, gitHubPassword));
    importProject.selectProjectByName("ProjectGitSubmodule");
    importProject.typeProjectName(projectName);
    importProject.waitImportRecursivelyIsNotSelected();
    importProject.clickOnImportRecursivelyCheckbox();
    importProject.waitImportRecursivelyIsSelected();
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
    projectExplorer.waitItem(projectName);
  }

  private void openSubmoduleOne(String projectName) throws Exception {
    projectExplorer.openItemByPath(projectName);
    projectExplorer.selectItem(projectName + "/" + SUBMODULE_NAME_1);
    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT,
        TestMenuCommandsConstants.Project.CONVERT_TO_PROJECT);
    projectWizard.waitOpenProjectConfigForm();
    projectWizard.waitTextParentDirectoryName("/" + projectName);
    projectWizard.waitTextProjectNameInput(SUBMODULE_NAME_1);
    projectWizard.selectTypeProject(Wizard.TypeProject.MAVEN);
    loader.waitOnClosed();
    projectWizard.clickSaveButton();
    projectWizard.waitCloseProjectConfigForm();
    loader.waitOnClosed();
    projectExplorer.waitItem(projectName);
    projectExplorer.expandPathInProjectExplorerAndOpenFile(
        projectName + "/" + SUBMODULE_NAME_1 + "/src/main/java/com.codenvy.example.spring",
        "GreetingController.java");
    projectExplorer.waitItemInVisibleArea("HelloWorld.java");
    editor.closeFileByNameWithSaving("GreetingController");
  }

  private void openSubmoduleTwo(String projectName) throws Exception {
    projectExplorer.selectItem(projectName + "/" + SUBMODULE_NAME_2);
    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT,
        TestMenuCommandsConstants.Project.CONVERT_TO_PROJECT);
    projectWizard.waitOpenProjectConfigForm();
    projectWizard.waitTextParentDirectoryName("/" + projectName);
    projectWizard.waitTextProjectNameInput(SUBMODULE_NAME_2);
    projectWizard.selectTypeProject(Wizard.TypeProject.MAVEN);
    loader.waitOnClosed();
    projectWizard.clickSaveButton();
    projectWizard.waitCloseProjectConfigForm();
    loader.waitOnClosed();
    projectExplorer.waitItem(projectName);
    projectExplorer.expandPathInProjectExplorerAndOpenFile(
        projectName + "/" + SUBMODULE_NAME_2 + "/src/main/java/com.company.example", "A.java");
    projectExplorer.openItemByPath(
        projectName + "/" + SUBMODULE_NAME_2 + "/src/main/java/commenttest");
    projectExplorer.waitItemInVisibleArea("GitPullTest.java");
    projectExplorer.waitItemInVisibleArea("JavaCommentsTest.java");
    editor.closeFileByNameWithSaving("A");
  }
}
