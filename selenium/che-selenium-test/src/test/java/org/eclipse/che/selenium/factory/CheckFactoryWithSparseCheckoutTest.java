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

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.selenium.core.factory.FactoryTemplate;
import org.eclipse.che.selenium.core.factory.TestFactory;
import org.eclipse.che.selenium.core.factory.TestFactoryInitializer;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Mihail Kuznyetsov */
public class CheckFactoryWithSparseCheckoutTest {
  private static final String PROJECT_NAME = "java-multimodule2";

  @Inject private ProjectExplorer projectExplorer;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private Events events;
  @Inject private Dashboard dashboard;

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

    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.openItemByPath(PROJECT_NAME);
    projectExplorer.waitItem(PROJECT_NAME + "/my-lib");
    projectExplorer.waitItemIsNotPresentVisibleArea(PROJECT_NAME + "/my-webapp");
  }
}
