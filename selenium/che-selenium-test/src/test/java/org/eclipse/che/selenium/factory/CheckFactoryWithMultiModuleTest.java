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

import static org.eclipse.che.selenium.pageobject.ProjectExplorer.FolderTypes.PROJECT_FOLDER;
import static org.testng.Assert.fail;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestGitHubRepository;
import org.eclipse.che.selenium.core.client.TestUserPreferencesServiceClient;
import org.eclipse.che.selenium.core.factory.FactoryTemplate;
import org.eclipse.che.selenium.core.factory.TestFactory;
import org.eclipse.che.selenium.core.factory.TestFactoryInitializer;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.PullRequestPanel;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class CheckFactoryWithMultiModuleTest {
  private static final String PROJECT_NAME = NameGenerator.generate("project", 6);

  @Inject private TestFactoryInitializer testFactoryInitializer;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private NotificationsPopupPanel notifications;
  @Inject private Dashboard dashboard;
  @Inject private PullRequestPanel pullRequestPanel;

@Inject private Consoles consoles;  @Inject

  private TestGitHubRepository testRepo;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private SeleniumWebDriverHelper seleniumWebDriverHelper;
  @Inject private TestUserPreferencesServiceClient testUserPreferencesServiceClient;

  private TestFactory testFactory;

  @BeforeClass
  public void setUp() throws Exception {
    // preconditions - add the project to the test repository
    Path entryPath = Paths.get(getClass().getResource("/projects/java-multimodule").getPath());
    testRepo.addContent(entryPath);
    String repositoryUrl = testRepo.getHttpsTransportUrl();

    TestFactoryInitializer.TestFactoryBuilder factoryBuilder =
        testFactoryInitializer.fromTemplate(FactoryTemplate.MINIMAL);
    ProjectConfigDto projectConfigDto = factoryBuilder.getWorkspace().getProjects().get(0);
    projectConfigDto.setName(PROJECT_NAME);
    projectConfigDto.setPath("/" + PROJECT_NAME);
    projectConfigDto.getSource().setParameters(ImmutableMap.of("branch", "master"));
    projectConfigDto.getSource().setLocation(repositoryUrl);
    testFactory = factoryBuilder.build();
  }

  @AfterClass
  public void deleteTestFactory() throws Exception {
    testFactory.delete();
  }

  @AfterClass
  public void restoreContributionTabPreference() throws Exception {
    testUserPreferencesServiceClient.restoreDefaultContributionTabPreference();
  }

  @Test
  public void checkFactoryProcessing() {
    dashboard.open();
    testFactory.open(seleniumWebDriver);
    seleniumWebDriverHelper.switchToIdeFrameAndWaitAvailability();
    projectExplorer.waitProjectExplorer();

    try {
      notifications.waitExpectedMessageOnProgressPanelAndClose(
          "Project " + PROJECT_NAME + " imported");
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/10728");
    }

    consoles.waitJDTLSProjectResolveFinishedMessage(PROJECT_NAME);
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    pullRequestPanel.waitOpenPanel();
    projectExplorer.openItemByPath(PROJECT_NAME);
    projectExplorer.waitDefinedTypeOfFolder(PROJECT_NAME + "/my-lib", PROJECT_FOLDER);
    projectExplorer.waitDefinedTypeOfFolder(PROJECT_NAME + "/my-webapp", PROJECT_FOLDER);
  }
}
