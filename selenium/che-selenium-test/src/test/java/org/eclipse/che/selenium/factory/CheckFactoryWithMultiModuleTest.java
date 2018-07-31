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

import static org.eclipse.che.selenium.pageobject.ProjectExplorer.FolderTypes.PROJECT_FOLDER;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.factory.FactoryTemplate;
import org.eclipse.che.selenium.core.factory.TestFactory;
import org.eclipse.che.selenium.core.factory.TestFactoryInitializer;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.PullRequestPanel;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
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

  @Inject
  @Named("github.username")
  private String gitHubUsername;

  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private SeleniumWebDriverHelper seleniumWebDriverHelper;

  private TestFactory testFactory;

  @BeforeClass
  public void setUp() throws Exception {
    TestFactoryInitializer.TestFactoryBuilder factoryBuilder =
        testFactoryInitializer.fromTemplate(FactoryTemplate.MINIMAL);
    ProjectConfigDto projectConfigDto = factoryBuilder.getWorkspace().getProjects().get(0);
    projectConfigDto.setName(PROJECT_NAME);
    projectConfigDto.setPath("/" + PROJECT_NAME);
    projectConfigDto.getSource().setParameters(ImmutableMap.of("branch", "master"));
    projectConfigDto
        .getSource()
        .setLocation("https://github.com/" + gitHubUsername + "/gitPullTest.git");
    testFactory = factoryBuilder.build();
  }

  @AfterClass
  public void tearDown() throws Exception {
    testFactory.delete();
  }

  @Test
  public void checkFactoryProcessing() throws Exception {
    dashboard.open();
    testFactory.open(seleniumWebDriver);
    seleniumWebDriverHelper.switchToIdeFrameAndWaitAvailability();
    projectExplorer.waitProjectExplorer();
    notifications.waitExpectedMessageOnProgressPanelAndClosed(
        "Project " + PROJECT_NAME + " imported");
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    pullRequestPanel.waitOpenPanel();
    projectExplorer.openItemByPath(PROJECT_NAME);
    projectExplorer.waitDefinedTypeOfFolder(PROJECT_NAME + "/my-lib", PROJECT_FOLDER);
    projectExplorer.waitDefinedTypeOfFolder(PROJECT_NAME + "/my-webapp", PROJECT_FOLDER);
  }
}
