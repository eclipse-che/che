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

import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestUserPreferencesServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.utils.WorkspaceDtoDeserializer;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspaceImpl;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.git.Git;
import org.eclipse.che.selenium.pageobject.git.GitRevertCommit;
import org.eclipse.che.selenium.pageobject.git.GitStatusBar;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.GIT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.STATUS;
import static org.eclipse.che.selenium.core.workspace.WorkspaceTemplate.UBUNTU_JDK8;
import static org.eclipse.che.selenium.pageobject.git.Git.ResetModes.HARD;
import static org.eclipse.che.selenium.pageobject.git.Git.ResetModes.MIXED;
import static org.eclipse.che.selenium.pageobject.git.Git.ResetModes.SOFT;

public class GitResettingTest {
  @Inject private Ide ide;
  @Inject private TestUser productUser;
  @Inject private TestUser testUser;
  @Inject private WorkspaceDtoDeserializer workspaceDtoDeserializer;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private AskDialog askDialog;
  @Inject private Menu menu;
  @Inject private Git git;
  @Inject private CodenvyEditor editor;
  @Inject private GitRevertCommit gitRevertCommit;
  @Inject private TestUserPreferencesServiceClient testUserPreferencesServiceClient;
  @Inject private GitStatusBar gitStatusBar;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  private TestWorkspace testWorkspace;
  private String nameOfProjectForCheckingGitSoftReset = "checkGitSoftReset";
  private String nameOfProjectForCheckingGitHardReset = "checkGitHardReset";
  private String nameOfProjectForCheckingGitMixReset = "checkGitMixReset";

  @BeforeClass
  public void prepare() throws Exception {
    String phpRepoLocation = "https://github.com/che-samples/web-php-simple.git";

    List<ProjectConfigDto> list = new ArrayList<>();

    List<String> projects =
        Arrays.asList(
            nameOfProjectForCheckingGitSoftReset,
            nameOfProjectForCheckingGitHardReset,
            nameOfProjectForCheckingGitMixReset);
    SourceStorageDto sourceStorage =
        DtoFactory.getInstance()
            .createDto(SourceStorageDto.class)
            .withLocation(phpRepoLocation)
            .withType("git");

    projects.forEach(item -> list.add(configureTestProject(item, sourceStorage)));
    WorkspaceConfigDto workspace =
        workspaceDtoDeserializer.deserializeWorkspaceTemplate(UBUNTU_JDK8);

    workspace.setProjects(list);

    testWorkspace =
        new TestWorkspaceImpl(
            NameGenerator.generate("check-resetting-test", 4),
            testUser,
            4,
            workspace,
            workspaceServiceClient);

    ide.open(testWorkspace);
    projects.forEach(item -> projectExplorer.waitItem(item));
    notificationsPopupPanel.waitPopupPanelsAreClosed();
    projectExplorer.quickExpandWithJavaScript();
  }


  @AfterClass
  public void tearDown() {
    testWorkspace.delete();
  }

  @Test
  public void checkSoftReset() {
    String uniqueTextForSelection = "2016 Jan 6 16:18:39";
    String expectedTextInGitStatusConsole =
        "On branch master\n Changes to be committed:\n  new file:   .codenvy.json\n modified:   README.md";
    projectExplorer.waitAndSelectItem(nameOfProjectForCheckingGitSoftReset);
    git.doResetToCommitMessage(SOFT, uniqueTextForSelection);
    menu.runCommand(GIT, STATUS);
    git.waitGitStatusBarWithMess(expectedTextInGitStatusConsole);
  }

  @Test
  public void checkMixReset() {
    String uniqueTextForSelection = "2016 Jan 6 15:41:30";
    String expectedTextInGitStatusConsole =
        "On branch master\n Changes not staged for commit:\n  new file:   .codenvy.json\n modified:   README.md";
    projectExplorer.waitAndSelectItem(nameOfProjectForCheckingGitMixReset);
    git.doResetToCommitMessage(MIXED, uniqueTextForSelection);
    menu.runCommand(GIT, STATUS);
    git.waitGitStatusBarWithMess(expectedTextInGitStatusConsole);
  }

  @Test
  public void checkHardReset() {
    String textInEditorBeforeReset =
        "To access database, run `env | grep MYSQL` in the terminal. You will get MySQL user, password and database. `root` user is passwordless.";
    String uniqueTextForSelection = "2016 Jan 6 15:35:22";
    String expectedTextInEditorAfterHardResetting =
        "# web-php-apache2-simple\nA hello world PHP script";
    String expectedTextInGitStatusConsole = "On branch master\n nothing to commit";

    projectExplorer.openItemByPath(nameOfProjectForCheckingGitHardReset + "/README.md");
    editor.waitTextIntoEditor(textInEditorBeforeReset);
    git.doResetToCommitMessage(HARD, uniqueTextForSelection);
    menu.runCommand(GIT, STATUS);
    projectExplorer.waitDisappearItemByPath(nameOfProjectForCheckingGitHardReset + "/index.php");
    git.waitGitStatusBarWithMess(expectedTextInGitStatusConsole);
    editor.waitTextIntoEditor(expectedTextInEditorAfterHardResetting);
  }

  private ProjectConfigDto configureTestProject(String projectName, SourceStorageDto gitSource) {
    ProjectConfigDto projectConfigDto = DtoFactory.getInstance().createDto(ProjectConfigDto.class);
    projectConfigDto.setName(projectName);
    projectConfigDto.setType(projectName);
    projectConfigDto.setSource(gitSource);
    projectConfigDto.setPath("/" + projectName);
    return projectConfigDto;
  }
}
