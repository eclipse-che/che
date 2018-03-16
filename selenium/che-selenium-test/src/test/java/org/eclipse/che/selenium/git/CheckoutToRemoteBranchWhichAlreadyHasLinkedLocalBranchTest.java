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
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.client.TestGitHubKeyUploader;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ImportProjectFromLocation;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.eclipse.che.selenium.pageobject.git.Git;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev */
@Test(groups = TestGroup.GITHUB)
public class CheckoutToRemoteBranchWhichAlreadyHasLinkedLocalBranchTest {
  private static final String PROJECT_NAME = "testRepo";
  private static final String MASTER_BRANCH = "master";
  private static final String ORIGIN_MASTER = "origin/master";
  private static final String GIT_MSG = "Ref master already exists";

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;
  @Inject private TestUser productUser;

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
  @Inject private ImportProjectFromLocation importFromLocation;
  @Inject private Wizard projectWizard;
  @Inject private TestGitHubKeyUploader testGitHubKeyUploader;

  @BeforeClass
  public void prepare() throws Exception {
    testGitHubKeyUploader.updateGithubKey();
    ide.open(ws);
  }

  @Test
  public void checkoutRemoteBranchToExistingLocalBranchTest() throws Exception {
    // Clone test repository with specific remote name.
    projectExplorer.waitProjectExplorer();
    String repoUrl = "https://github.com/" + gitHubUsername + "/gitPullTest.git";
    git.importJavaApp(repoUrl, PROJECT_NAME, Wizard.TypeProject.MAVEN);
    projectExplorer.waitAndSelectItem(PROJECT_NAME);

    // Open branches form
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.BRANCHES);
    git.waitBranchInTheList(MASTER_BRANCH);
    git.waitBranchInTheList(ORIGIN_MASTER);
    git.waitBranchInTheListWithCoState(MASTER_BRANCH);

    // Checkout to the master remote branch.
    git.selectBranchAndClickCheckoutBtn(ORIGIN_MASTER);
    git.closeBranchesForm();
    git.waitGitStatusBarWithMess(GIT_MSG);
  }
}
