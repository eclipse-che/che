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
package org.eclipse.che.selenium.factory;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.factory.FactoryTemplate;
import org.eclipse.che.selenium.core.factory.TestFactory;
import org.eclipse.che.selenium.core.factory.TestFactoryInitializer;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.PullRequestPanel;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Mihail Kuznyetsov */
@Test(groups = TestGroup.GITHUB)
public class CheckFactoryWithSparseCheckoutTest {
  private static final String PROJECT_NAME = "java-multimodule2";

  @Inject private ProjectExplorer projectExplorer;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private Events events;
  @Inject private Dashboard dashboard;
  @Inject private PullRequestPanel pullRequestPanel;

  @Inject
  @Named("github.username")
  private String gitHubUsername;

  @Inject private TestFactoryInitializer testFactoryInitializer;

  private TestFactory testFactory;

  @BeforeClass
  public void setUp() throws Exception {
    TestFactoryInitializer.TestFactoryBuilder testFactoryBuilder =
        testFactoryInitializer.fromTemplate(FactoryTemplate.MINIMAL);
    ProjectConfigDto projectConfig = testFactoryBuilder.getWorkspace().getProjects().get(0);
    projectConfig.getSource().setParameters(ImmutableMap.of("keepDir", "my-lib"));
    projectConfig
        .getSource()
        .setLocation("https://github.com/" + gitHubUsername + "/" + PROJECT_NAME);
    projectConfig.setName(PROJECT_NAME);
    projectConfig.setPath("/" + PROJECT_NAME);
    testFactory = testFactoryBuilder.build();
  }

  @AfterClass
  public void tearDown() throws Exception {
    testFactory.delete();
  }

  @Test
  public void acceptFactoryWithSparseCheckout() throws Exception {
    testFactory.authenticateAndOpen();
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);

    events.clickEventLogBtn();
    events.waitOpened();
    events.waitExpectedMessage("Project " + PROJECT_NAME + " imported");
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    pullRequestPanel.waitOpenPanel();
    projectExplorer.openItemByPath(PROJECT_NAME);
    projectExplorer.waitItem(PROJECT_NAME + "/my-lib");
    projectExplorer.waitItemIsNotPresentVisibleArea(PROJECT_NAME + "/my-webapp");
  }
}
