/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
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

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.List;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.factory.TestFactory;
import org.eclipse.che.selenium.core.factory.TestFactoryInitializer;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DirectUrlFactoryWithSpecificBranch {
  @Inject
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

  private TestFactory testFactoryWithSpecificBranch;
  private static final Logger LOG =
      LoggerFactory.getLogger(DirectUrlFactoryWithSpecificBranch.class);

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
    testFactoryWithSpecificBranch.authenticateAndOpen(seleniumWebDriver);
    seleniumWebDriver.switchFromDashboardIframeToIde();
    projectExplorer.waitProjectExplorer();
    notificationsPopupPanel.waitProgressPopupPanelClose();
    events.clickEventLogBtn();

    Workspace testWorkspace = workspaceServiceClient.getByName(gitHubUsername, testUser.getName());

    logWorkspaceStatus(testWorkspace);

    events.waitExpectedMessage("Project gitPullTest imported", UPDATING_PROJECT_TIMEOUT_SEC);

    logWorkspaceStatus(testWorkspace);

    events.waitExpectedMessage(
        "Successfully configured and cloned source code of gitPullTest.",
        UPDATING_PROJECT_TIMEOUT_SEC);

    logWorkspaceStatus(testWorkspace);

    events.waitExpectedMessage(
        "Project: gitPullTest | cloned from: gitPullTest | remote branch: refs/remotes/origin/contrib-12092015 | local branch: contrib-12092015",
        UPDATING_PROJECT_TIMEOUT_SEC);

    logWorkspaceStatus(testWorkspace);

    projectExplorer.expandPathInProjectExplorer("gitPullTest/my-lib");

    logWorkspaceStatus(testWorkspace);

    projectExplorer.waitItem("gitPullTest/my-lib/pom.xml");

    logWorkspaceStatus(testWorkspace);

    String wsId =
        workspaceServiceClient
            .getByName(seleniumWebDriver.getWorkspaceNameFromBrowserUrl(), testUser.getName())
            .getId();

    logWorkspaceStatus(testWorkspace);

    List<String> visibleItems = projectExplorer.getNamesOfAllOpenItems();
    assertTrue(
        visibleItems.containsAll(
            ImmutableList.of("gitPullTest", "my-lib", "my-webapp", "src", "pom.xml")));

    logWorkspaceStatus(testWorkspace);

    String projectType = testProjectServiceClient.getFirstProject(wsId).getType();

    logWorkspaceStatus(testWorkspace);

    assertTrue(projectType.equals("blank"));

    logWorkspaceStatus(testWorkspace);
  }

  private void logWorkspaceStatus(Workspace workspace) {
    try {
      LOG.debug("============>>>>>   " + workspace.getStatus().toString());
    } catch (Exception ex) {
      LOG.debug(
          "************************    "
              + "unhandled exeption in the \"logWorkspaceStatus method\"");
    }
  }
}
