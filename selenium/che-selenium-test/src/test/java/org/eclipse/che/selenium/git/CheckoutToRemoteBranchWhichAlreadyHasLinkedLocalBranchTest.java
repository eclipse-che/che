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

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.BRANCHES;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.GIT;
import static org.eclipse.che.selenium.pageobject.Wizard.TypeProject.BLANK;

import com.google.inject.Inject;
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

  private static final String PROJECT_NAME = "testRepo";
  private static final String LOCAL_BRANCH = "master";
  private static final String REMOTE_BRANCH = "origin/master";
  private static final String GIT_MESSAGE = "Ref master already exists";

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;
  @Inject private TestGitHubRepository testRepo;
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
    projectExplorer.waitProjectExplorer();
    git.importJavaApp(testRepo.getHtmlUrl(), PROJECT_NAME, BLANK);
    projectExplorer.waitAndSelectItem(PROJECT_NAME);

    // Open branches form
    menu.runCommand(GIT, BRANCHES);
    git.waitBranchInTheList(LOCAL_BRANCH);
    git.waitBranchInTheList(REMOTE_BRANCH);
    git.waitBranchInTheListWithCoState(LOCAL_BRANCH);

    // Checkout to the master remote branch.
    git.selectBranchAndClickCheckoutBtn(REMOTE_BRANCH);
    git.closeBranchesForm();
    git.waitGitStatusBarWithMess(GIT_MESSAGE);

    eventsPanel.clickEventLogBtn();
    eventsPanel.waitExpectedMessage(GIT_MESSAGE);
  }
}
