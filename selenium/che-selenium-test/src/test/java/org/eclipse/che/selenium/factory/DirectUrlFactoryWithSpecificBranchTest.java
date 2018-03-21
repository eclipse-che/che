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
package org.eclipse.che.selenium.factory;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.UPDATING_PROJECT_TIMEOUT_SEC;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.List;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.factory.TestFactory;
import org.eclipse.che.selenium.core.factory.TestFactoryInitializer;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.PullRequestPanel;
import org.openqa.selenium.NoSuchElementException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = TestGroup.GITHUB)
public class DirectUrlFactoryWithSpecificBranchTest {
  @Inject(optional = true)
  @Named("github.username")
  private String gitHubUsername;

  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private TestUser testUser;
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
    testFactoryWithSpecificBranch =
        testFactoryInitializer.fromUrl(
            "https://github.com/" + gitHubUsername + "/gitPullTest/tree/contrib-12092015");
  }

  @AfterClass
  public void tearDown() throws Exception {
    if (workspaceServiceClient.exists(gitHubUsername, testUser.getName())) {
      testFactoryWithSpecificBranch.delete();
    }
  }

  @Test
  public void factoryWithDirectUrlWithSpecificBranch() throws Exception {
    try {
      testFactoryWithSpecificBranch.authenticateAndOpen();
    } catch (NoSuchElementException ex) {
      // remove try-catch block after issue has been resolved
      fail("https://github.com/eclipse/che/issues/8671");
    }

    projectExplorer.waitProjectExplorer();
    notificationsPopupPanel.waitProgressPopupPanelClose();
    events.clickEventLogBtn();

    events.waitExpectedMessage("Project gitPullTest imported", UPDATING_PROJECT_TIMEOUT_SEC);
    events.waitExpectedMessage(
        "Successfully configured and cloned source code of gitPullTest.",
        UPDATING_PROJECT_TIMEOUT_SEC);
    events.waitExpectedMessage(
        "Project: gitPullTest | cloned from: gitPullTest | remote branch: refs/remotes/origin/contrib-12092015 | local branch: contrib-12092015",
        UPDATING_PROJECT_TIMEOUT_SEC);

    projectExplorer.waitAndSelectItem("gitPullTest");
    pullRequestPanel.waitOpenPanel();

    projectExplorer.expandPathInProjectExplorer("gitPullTest/my-lib");
    projectExplorer.waitItem("gitPullTest/my-lib/pom.xml");

    String wsId =
        workspaceServiceClient
            .getByName(seleniumWebDriver.getWorkspaceNameFromBrowserUrl(), testUser.getName())
            .getId();

    List<String> visibleItems = projectExplorer.getNamesOfAllOpenItems();
    assertTrue(
        visibleItems.containsAll(
            ImmutableList.of("gitPullTest", "my-lib", "my-webapp", "src", "pom.xml")));

    String projectType = testProjectServiceClient.getFirstProject(wsId).getType();
    assertTrue(projectType.equals("blank"));
  }
}
