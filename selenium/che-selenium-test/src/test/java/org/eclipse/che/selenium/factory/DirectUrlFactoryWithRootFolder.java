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

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.Arrays;
import java.util.List;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.factory.TestFactory;
import org.eclipse.che.selenium.core.factory.TestFactoryInitializer;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class DirectUrlFactoryWithRootFolder {
  private static final String EXPECTED_PROJECT = "quickstart";

  @Inject
  @Named("github.username")
  private String gitHubUsername;

  @Inject private ProjectExplorer projectExplorer;
  @Inject private TestUser testUser;
  @Inject private TestFactoryInitializer testFactoryInitializer;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private Events events;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private TestProjectServiceClient projectServiceClient;
  @Inject private Dashboard dashboard;

  private TestFactory testFactoryWithRootFolder;

  @BeforeClass
  public void setUp() throws Exception {
    testFactoryWithRootFolder =
        testFactoryInitializer.fromUrl("https://github.com/" + gitHubUsername + "/quickstart");
  }

  @AfterClass
  public void tearDown() throws Exception {
    try {
      testFactoryWithRootFolder.delete();
    } catch (org.eclipse.che.api.core.NotFoundException ex) {
      // remove try-catch block after issue has been resolved
      fail("https://github.com/eclipse/che/issues/8667");
    }
  }

  @Test
  public void factoryWithDirectUrlWithRootFolder() throws Exception {
    String expectedMessInTheEventsPanel = "Project " + EXPECTED_PROJECT + " imported";
    List<String> expectedItemsAfterClonning =
        Arrays.asList(
            "CHANGELOG.md",
            "Dockerfile",
            "LICENSE",
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
    projectExplorer.waitItem(EXPECTED_PROJECT);
    notificationsPopupPanel.waitProgressPopupPanelClose();
    events.clickEventLogBtn();

    try {
      events.waitExpectedMessage(expectedMessInTheEventsPanel);
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/6440");
    }
    projectExplorer.openItemByPath(EXPECTED_PROJECT);

    String currentWsId =
        workspaceServiceClient
            .getByName(seleniumWebDriver.getWorkspaceNameFromBrowserUrl(), testUser.getName())
            .getId();
    List<String> visibleItems = projectExplorer.getNamesOfAllOpenItems();
    assertTrue(visibleItems.containsAll(expectedItemsAfterClonning));
    String currentTypeOfProject = projectServiceClient.getFirstProject(currentWsId).getType();
    assertTrue(currentTypeOfProject.equals("blank"));
  }
}
