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
import static org.eclipse.che.selenium.pageobject.Wizard.TypeProject.BLANK;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.client.TestGitHubRepository;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.git.Git;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev */
public class CheckoutToRemoteBranchWhichAlreadyHasLinkedLocalBranchTest {

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;
  @Inject private TestGitHubRepository testRepo;

  @Inject(optional = true)
  @Named("github.username")
  private String gitHubUsername;

  @Inject(optional = true)
  @Named("github.password")
  private String gitHubPassword;

  @Inject private ProjectExplorer projectExplorer;
  @Inject private Menu menu;
  @Inject private Git git;
  @Inject private Events eventsPanel;

  @BeforeClass
  public void prepare() throws Exception {
    Path entryPath = Paths.get(getClass().getResource("/projects/git-pull-test").getPath());
    testRepo.addContent(entryPath);

    ide.open(ws);
  }

  @Test
  public void checkoutRemoteBranchToExistingLocalBranchTest() throws Exception {
    // preconditions and import the test repo
    String projectName = "testRepo";
    String localBranch = "master";
    String remoteBranch = "origin/master";
    String gitMessage = "Ref master already exists";

    projectExplorer.waitProjectExplorer();
    git.importJavaApp(testRepo.getHtmlUrl(), projectName, BLANK);
    projectExplorer.waitAndSelectItem(projectName);

    // Open branches form
    menu.runCommand(GIT, BRANCHES);
    git.waitBranchInTheList(localBranch);
    git.waitBranchInTheList(remoteBranch);
    git.waitBranchInTheListWithCoState(localBranch);

    // Checkout to the master remote branch.
    git.selectBranchAndClickCheckoutBtn(remoteBranch);
    git.closeBranchesForm();
    git.waitGitStatusBarWithMess(gitMessage);

    eventsPanel.clickEventLogBtn();
    eventsPanel.waitExpectedMessage(gitMessage);
  }
}
