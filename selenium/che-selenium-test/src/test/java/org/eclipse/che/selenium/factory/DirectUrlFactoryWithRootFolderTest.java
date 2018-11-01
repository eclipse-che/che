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

import static org.eclipse.che.selenium.core.TestGroup.GITHUB;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestGitHubRepository;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.factory.TestFactory;
import org.eclipse.che.selenium.core.factory.TestFactoryInitializer;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
@Test(groups = {GITHUB})
public class DirectUrlFactoryWithRootFolderTest {
  @Inject private ProjectExplorer projectExplorer;
  @Inject private DefaultTestUser testUser;
  @Inject private TestFactoryInitializer testFactoryInitializer;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private Events events;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private TestProjectServiceClient projectServiceClient;
  @Inject private TestGitHubRepository testRepo;

  private TestFactory testFactoryWithRootFolder;

  @BeforeClass
  public void setUp() throws Exception {
    // preconditions - add the project to the test repository
    Path entryPath = Paths.get(getClass().getResource("/projects/quickstart").getPath());
    testRepo.addContent(entryPath);
    String repositoryUrl = testRepo.getHtmlUrl();

    testFactoryWithRootFolder = testFactoryInitializer.fromUrl(repositoryUrl);
  }

  @AfterClass
  public void tearDown() throws Exception {
    testFactoryWithRootFolder.delete();
  }

  @Test
  public void factoryWithDirectUrlWithRootFolder() throws Exception {
    String projectName = testRepo.getName();
    String expectedMessInTheEventsPanel = "Project " + projectName + " imported";
    List<String> expectedItemsAfterCloning =
        Arrays.asList(
            "CHANGELOG.md",
            "Dockerfile",
            "LICENSE.txt",
            "README.md",
            "favicon.ico",
            "index.html",
            "karma-test-shim.js",
            "karma.conf.js",
            "package.json",
            "protractor.config.js",
            "styles.css",
            "systemjs.config.js",
            "tsconfig.json",
            "tslint.json",
            "typings.json",
            "wallaby.js");

    testFactoryWithRootFolder.authenticateAndOpen();

    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(projectName);
    notificationsPopupPanel.waitProgressPopupPanelClose();
    events.clickEventLogBtn();

    try {
      events.waitExpectedMessage(expectedMessInTheEventsPanel);
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known random failure https://github.com/eclipse/che/issues/6440");
    }
    projectExplorer.openItemByPath(projectName);

    String currentWsId =
        workspaceServiceClient
            .getByName(seleniumWebDriver.getWorkspaceNameFromBrowserUrl(), testUser.getName())
            .getId();
    List<String> visibleItems = projectExplorer.getNamesOfAllOpenItems();
    assertTrue(visibleItems.containsAll(expectedItemsAfterCloning));
    String currentTypeOfProject = projectServiceClient.getFirstProject(currentWsId).getType();
    assertTrue(currentTypeOfProject.equals("blank"));
  }
}
