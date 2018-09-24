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
package org.eclipse.che.selenium.factory;

import static org.eclipse.che.selenium.core.CheSeleniumSuiteModule.AUXILIARY;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.UPDATING_PROJECT_TIMEOUT_SEC;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.client.TestGitHubRepository;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.factory.TestFactory;
import org.eclipse.che.selenium.core.factory.TestFactoryInitializer;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.PullRequestPanel;
import org.openqa.selenium.NoSuchElementException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = TestGroup.GITHUB)
public class DirectUrlFactoryWithSpecificBranchTest {
  private static final String SECOND_BRANCH_NAME = "contrib";

  @Inject
  @Named(AUXILIARY)
  private TestGitHubRepository testAuxiliaryRepo;

  @Inject
  @Named("github.auxiliary.username")
  private String gitHubAuxiliaryUserName;

  @Inject private ProjectExplorer projectExplorer;
  @Inject private DefaultTestUser testUser;
  @Inject private TestFactoryInitializer testFactoryInitializer;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private Events events;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private PullRequestPanel pullRequestPanel;

  private TestFactory testFactoryWithSpecificBranch;

  @BeforeClass
  public void setUp() throws Exception {
    // preconditions - add the project to the test repository
    Path entryPath = Paths.get(getClass().getResource("/projects/java-multimodule").getPath());
    testAuxiliaryRepo.addContent(entryPath);
    String repositoryUrl = testAuxiliaryRepo.getHtmlUrl();

    // create another branch in the test repo
    testAuxiliaryRepo.createBranch(SECOND_BRANCH_NAME);

    testFactoryWithSpecificBranch =
        testFactoryInitializer.fromUrl(repositoryUrl + "/tree/" + SECOND_BRANCH_NAME);
  }

  @AfterClass
  public void tearDown() throws Exception {
    if (workspaceServiceClient.exists(gitHubAuxiliaryUserName, testUser.getName())) {
      testFactoryWithSpecificBranch.delete();
    }
  }

  @Test
  public void factoryWithDirectUrlWithSpecificBranch() throws Exception {
    String repositoryName = testAuxiliaryRepo.getName();

    try {
      testFactoryWithSpecificBranch.authenticateAndOpen();
    } catch (NoSuchElementException ex) {
      // remove try-catch block after issue has been resolved
      fail("https://github.com/eclipse/che/issues/8671");
    }

    projectExplorer.waitProjectExplorer();
    notificationsPopupPanel.waitProgressPopupPanelClose();
    events.clickEventLogBtn();

    events.waitExpectedMessage(
        "Project " + repositoryName + " imported", UPDATING_PROJECT_TIMEOUT_SEC);
    events.waitExpectedMessage(
        "Successfully configured and cloned source code of " + repositoryName,
        UPDATING_PROJECT_TIMEOUT_SEC);
    events.waitExpectedMessage(
        String.format(
            "Project: %s | cloned from: %s | remote branch: refs/remotes/origin/%s | local branch: %s",
            repositoryName, repositoryName, SECOND_BRANCH_NAME, SECOND_BRANCH_NAME),
        UPDATING_PROJECT_TIMEOUT_SEC);

    projectExplorer.waitAndSelectItem(repositoryName);
    pullRequestPanel.waitOpenPanel();

    projectExplorer.expandPathInProjectExplorer(repositoryName + "/my-lib");
    projectExplorer.waitItem(repositoryName + "/my-lib/pom.xml");

    String wsId =
        workspaceServiceClient
            .getByName(seleniumWebDriver.getWorkspaceNameFromBrowserUrl(), testUser.getName())
            .getId();

    List<String> visibleItems = projectExplorer.getNamesOfAllOpenItems();
    assertTrue(
        visibleItems.containsAll(
            ImmutableList.of(repositoryName, "my-lib", "my-webapp", "src", "pom.xml")));

    String projectType = testProjectServiceClient.getFirstProject(wsId).getType();
    assertTrue(projectType.equals("blank"));
  }
}
