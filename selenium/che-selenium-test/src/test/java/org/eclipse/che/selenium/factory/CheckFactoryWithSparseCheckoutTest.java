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

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.client.TestGitHubRepository;
import org.eclipse.che.selenium.core.client.TestUserPreferencesServiceClient;
import org.eclipse.che.selenium.core.factory.FactoryTemplate;
import org.eclipse.che.selenium.core.factory.TestFactory;
import org.eclipse.che.selenium.core.factory.TestFactoryInitializer;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.PullRequestPanel;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Mihail Kuznyetsov */
@Test(groups = TestGroup.GITHUB)
public class CheckFactoryWithSparseCheckoutTest {
  private String projectName;

  @Inject private ProjectExplorer projectExplorer;
  @Inject private Events events;
  @Inject private PullRequestPanel pullRequestPanel;
  @Inject private TestGitHubRepository testRepo;
  @Inject private TestFactoryInitializer testFactoryInitializer;
  @Inject private TestUserPreferencesServiceClient testUserPreferencesServiceClient;

  private TestFactory testFactory;

  @BeforeClass
  public void setUp() throws Exception {
    // preconditions - add the project to the test repository
    Path entryPath = Paths.get(getClass().getResource("/projects/java-multimodule").getPath());
    testRepo.addContent(entryPath);
    String repositoryUrl = testRepo.getHtmlUrl();
    projectName = testRepo.getName();

    TestFactoryInitializer.TestFactoryBuilder testFactoryBuilder =
        testFactoryInitializer.fromTemplate(FactoryTemplate.MINIMAL);
    ProjectConfigDto projectConfig = testFactoryBuilder.getWorkspace().getProjects().get(0);
    projectConfig.getSource().setParameters(ImmutableMap.of("keepDir", "my-lib"));
    projectConfig.getSource().setLocation(repositoryUrl);
    projectConfig.setName(projectName);
    projectConfig.setPath("/" + projectName);
    testFactory = testFactoryBuilder.build();
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
  public void acceptFactoryWithSparseCheckout() {
    testFactory.authenticateAndOpen();
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(projectName);

    events.clickEventLogBtn();
    events.waitOpened();
    events.waitExpectedMessage("Project " + projectName + " imported");
    projectExplorer.waitAndSelectItem(projectName);
    pullRequestPanel.waitOpenPanel();
    projectExplorer.openItemByPath(projectName);
    projectExplorer.waitItem(projectName + "/my-lib");
    projectExplorer.waitItemIsNotPresentVisibleArea(projectName + "/my-webapp");
  }
}
