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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.eclipse.che.selenium.core.workspace.WorkspaceTemplate.UBUNTU_JDK8;

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

  @BeforeClass
  public void prepare() throws Exception {
    String phpRepoLocation = "https://github.com/che-samples/web-php-simple.git";
    String nameOfProjectForCheckingGitSoftReset = "checkGitSoftReset";
    String nameOfProjectForCheckingGitHardReset = "checkGitHardReset";
    String nameOfProjectForCheckingGitMixReset = "checkGitMixReset";

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

    projects.forEach(item -> list.add(setUpTestProject(item, sourceStorage)));
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
  }

  @Test
  public void checkSoftReset() {

  }

  private ProjectConfigDto setUpTestProject(String projectName, SourceStorageDto gitSource) {
    ProjectConfigDto projectConfigDto = DtoFactory.getInstance().createDto(ProjectConfigDto.class);
    projectConfigDto.setName(projectName);
    projectConfigDto.setType(projectName);
    projectConfigDto.setSource(gitSource);
    projectConfigDto.setPath("/" + projectName);
    return projectConfigDto;
  }
}
