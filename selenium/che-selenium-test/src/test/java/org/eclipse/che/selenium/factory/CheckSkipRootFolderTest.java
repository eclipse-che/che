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
import com.google.inject.name.Named;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestGitHubRepository;
import org.eclipse.che.selenium.core.client.TestUserPreferencesServiceClient;
import org.eclipse.che.selenium.core.factory.FactoryTemplate;
import org.eclipse.che.selenium.core.factory.TestFactory;
import org.eclipse.che.selenium.core.factory.TestFactoryInitializer;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CheckSkipRootFolderTest {

  @Inject private TestFactoryInitializer testFactoryInitializer;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private TestGitHubRepository testRepo;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private SeleniumWebDriverHelper seleniumWebDriverHelper;
  @Inject private TestUserPreferencesServiceClient testUserPreferencesServiceClient;
  @Inject private DefaultTestUser productUser;
  @Inject private Dashboard dashboard;

  @Inject
  @Named("github.username")
  private String gitHubUsername;

  @Inject private Ide ide;

  private TestFactory testFactory;
  private String projectName;

  @BeforeClass
  public void setUp() throws Exception {
    // add test repository
    Path entryPath =
        Paths.get(getClass().getResource("/projects/default-spring-project").getPath());
    testRepo.addContent(entryPath);

    // create download URL for current repo (this will be used in the factory as resource URL)
    projectName = testRepo.getName();
    String urlToDownloadZippedRepoFromGitHub =
        String.format("%s%s", testRepo.getHtmlUrl(), "/archive/master.zip");

    // create test factory based on minimal template, set necessary path and name to imported
    // project, type, skipFirstLevel value
    TestFactoryInitializer.TestFactoryBuilder factoryBuilder =
        testFactoryInitializer.fromTemplate(FactoryTemplate.MINIMAL);
    ProjectConfigDto projectConfigDto = factoryBuilder.getWorkspace().getProjects().get(0);
    projectConfigDto.setName(projectName);
    projectConfigDto.setPath("/" + projectName);
    projectConfigDto.getSource().setParameters(ImmutableMap.of("skipFirstLevel", "true"));
    projectConfigDto.getSource().setType("zip");
    projectConfigDto.getSource().setLocation(urlToDownloadZippedRepoFromGitHub);
    testFactory = factoryBuilder.build();
  }

  @AfterClass
  public void tearDown() throws Exception {
    testFactory.delete();
  }

  @Test
  public void checkSkipRootDirectory() {
    dashboard.open();
    testFactory.open(seleniumWebDriver);
    seleniumWebDriverHelper.switchToIdeFrameAndWaitAvailability();
    checkProjectStructure();
  }

  private void checkProjectStructure() {
    projectExplorer.openItemByPath(projectName);
    projectExplorer.waitItem(projectName + "/pom.xml");
    projectExplorer.waitItem(projectName + "/README.md");
  }
}
