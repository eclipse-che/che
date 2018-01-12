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

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.GIT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.REVERT_COMMIT;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestGitHubKeyUploader;
import org.eclipse.che.selenium.core.client.TestUserPreferencesServiceClient;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.eclipse.che.selenium.pageobject.git.GitRevertCommit;
import org.eclipse.che.selenium.pageobject.git.GitStatusBar;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Anatolii Bazko */
public class RevertCommitTest {
  private static final String PROJECT_NAME = NameGenerator.generate("project", 2);

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;
  @Inject private TestUser productUser;

  @Inject
  @Named("github.username")
  private String gitHubUsername;

  @Inject
  @Named("github.password")
  private String gitHubPassword;

  @Inject private ProjectExplorer projectExplorer;
  @Inject private Menu menu;
  @Inject private org.eclipse.che.selenium.pageobject.git.Git git;
  @Inject private TestGitHubKeyUploader testGitHubKeyUploader;
  @Inject private TestUserPreferencesServiceClient testUserPreferencesServiceClient;
  @Inject private GitRevertCommit gitRevertCommit;
  @Inject private GitStatusBar gitStatusBar;

  @BeforeClass
  public void prepare() throws Exception {
    testGitHubKeyUploader.updateGithubKey();
    testUserPreferencesServiceClient.addGitCommitter(gitHubUsername, productUser.getEmail());
    ide.open(ws);

    git.importJavaApp(
        "git@github.com:" + gitHubUsername + "/testRepo-1.git",
        PROJECT_NAME,
        Wizard.TypeProject.MAVEN);

    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.selectItem(PROJECT_NAME);
  }

  @Test
  public void shouldRevertCommit() {
    menu.runCommand(GIT, REVERT_COMMIT);

    String revision = gitRevertCommit.getTopCommitRevision();
    String comment = gitRevertCommit.getTopCommitComment();

    gitRevertCommit.selectRevision(revision);
    gitRevertCommit.clickRevertButton();

    gitStatusBar.waitMessageInGitTab("Reverted commits: - " + revision);

    menu.runCommand(GIT, REVERT_COMMIT);

    assertEquals(gitRevertCommit.getTopCommitAuthor(), gitHubUsername);
    assertTrue(gitRevertCommit.getTopCommitComment().contains("Revert \"" + comment + "\""));
  }
}
