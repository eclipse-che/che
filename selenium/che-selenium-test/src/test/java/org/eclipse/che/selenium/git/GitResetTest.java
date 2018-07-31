/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.git;

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.GIT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.STATUS;
import static org.eclipse.che.selenium.core.workspace.WorkspaceTemplate.DEFAULT_WITH_GITHUB_PROJECTS;
import static org.eclipse.che.selenium.pageobject.git.Git.ResetModes.HARD;
import static org.eclipse.che.selenium.pageobject.git.Git.ResetModes.MIXED;
import static org.eclipse.che.selenium.pageobject.git.Git.ResetModes.SOFT;

import com.google.inject.Inject;
import java.util.Arrays;
import java.util.List;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestUserPreferencesServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.utils.WorkspaceDtoDeserializer;
import org.eclipse.che.selenium.core.workspace.InjectTestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class GitResetTest {
  @Inject private Ide ide;
  @Inject private DefaultTestUser testUser;
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

  @InjectTestWorkspace(template = DEFAULT_WITH_GITHUB_PROJECTS)
  private TestWorkspace ws;

  private final String NAME_OF_PROJECT_FOR_CHECKING_GIT_SOFT_RESET = "checkGitSoftReset";
  private final String NAME_OF_PROJECT_FOR_CHECKING_GIT_HARD_RESET = "checkGitHardReset";
  private final String NAME_OF_PROJECT_FOR_CHECKING_GIT_MIX_RESET = "checkGitMixedReset";
  private final String COMMIT_MESSAGE = "added php samples";

  @BeforeClass
  public void prepare() throws Exception {
    List<String> projects =
        Arrays.asList(
            NAME_OF_PROJECT_FOR_CHECKING_GIT_SOFT_RESET,
            NAME_OF_PROJECT_FOR_CHECKING_GIT_HARD_RESET,
            NAME_OF_PROJECT_FOR_CHECKING_GIT_MIX_RESET);

    ide.open(ws);
    projects.forEach(item -> projectExplorer.waitItem(item));
    notificationsPopupPanel.waitPopupPanelsAreClosed();
    projectExplorer.quickExpandWithJavaScript();
  }

  @AfterClass
  public void tearDown() {
    ws.delete();
  }

  @Test
  public void checkSoftReset() {
    String expectedTextInGitStatusConsole =
        "On branch master\n Changes to be committed:\n  new file:   .codenvy.json\n modified:   README.md";
    projectExplorer.waitAndSelectItem(NAME_OF_PROJECT_FOR_CHECKING_GIT_SOFT_RESET);
    git.doResetToCommitMessage(SOFT, COMMIT_MESSAGE);
    menu.runCommand(GIT, STATUS);
    git.waitGitStatusBarWithMess(expectedTextInGitStatusConsole);
  }

  @Test
  public void checkMixReset() {
    String expectedTextInGitStatusConsole =
        "On branch master\n Changes not staged for commit:\n  new file:   .codenvy.json\n modified:   README.md";
    projectExplorer.waitAndSelectItem(NAME_OF_PROJECT_FOR_CHECKING_GIT_MIX_RESET);
    git.doResetToCommitMessage(MIXED, COMMIT_MESSAGE);
    menu.runCommand(GIT, STATUS);
    git.waitGitStatusBarWithMess(expectedTextInGitStatusConsole);
  }

  @Test
  public void checkHardReset() {
    String textInEditorBeforeReset =
        "To access database, run `env | grep MYSQL` in the terminal. You will get MySQL user, password and database. `root` user is passwordless.";
    String commitMessage = "Initial commit";
    String expectedTextInEditorAfterHardResetting =
        "# web-php-apache2-simple\nA hello world PHP script";
    String expectedTextInGitStatusConsole = "On branch master\n nothing to commit";

    projectExplorer.openItemByPath(NAME_OF_PROJECT_FOR_CHECKING_GIT_HARD_RESET + "/README.md");
    editor.waitTextIntoEditor(textInEditorBeforeReset);
    git.doResetToCommitMessage(HARD, commitMessage);
    menu.runCommand(GIT, STATUS);
    projectExplorer.waitDisappearItemByPath(
        NAME_OF_PROJECT_FOR_CHECKING_GIT_HARD_RESET + "/index.php");
    git.waitGitStatusBarWithMess(expectedTextInGitStatusConsole);
    editor.waitTextIntoEditor(expectedTextInEditorAfterHardResetting);
  }
}
