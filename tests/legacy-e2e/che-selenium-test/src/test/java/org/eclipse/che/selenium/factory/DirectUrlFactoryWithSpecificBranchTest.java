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
import static org.eclipse.che.selenium.core.TestGroup.OPENSHIFT;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.UPDATING_PROJECT_TIMEOUT_SEC;
import static org.testng.AssertJUnit.assertEquals;

import com.google.inject.Inject;
import java.util.Arrays;
import java.util.List;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.factory.TestFactory;
import org.eclipse.che.selenium.core.factory.TestFactoryInitializer;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.theia.TheiaIde;
import org.eclipse.che.selenium.pageobject.theia.TheiaProjectTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = {GITHUB, OPENSHIFT})
public class DirectUrlFactoryWithSpecificBranchTest {
  private static final Logger LOG =
      LoggerFactory.getLogger(DirectUrlFactoryWithSpecificBranchTest.class);

  private static final String SECOND_BRANCH_NAME = "java1.11";
  private final String REPOSITORY_URL = "https://github.com/che-samples/console-java-simple";

  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private TestFactoryInitializer testFactoryInitializer;
  @Inject private TheiaIde theiaIde;
  @Inject private TheiaProjectTree theiaProjectTree;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private Dashboard dashboard;

  private TestFactory testFactoryWithSpecificBranch;

  @BeforeClass
  public void setUp() throws Exception {
    testFactoryWithSpecificBranch =
        testFactoryInitializer.fromUrl(REPOSITORY_URL + "/tree/" + SECOND_BRANCH_NAME);

    dashboard.open();
  }

  @AfterClass
  public void deleteTestBranch() throws Exception {
    try {
      workspaceServiceClient.delete(getWorkspaceName(), defaultTestUser.getName());
    } catch (Exception e) {
      LOG.warn("It was impossible to remove factory.", e);
    }
  }

  @Test
  public void factoryWithDirectUrlWithSpecificBranch() {
    String repositoryName = "console-java-simple";
    List<String> expectedItemsAfterCloning =
        Arrays.asList("pom.xml", "build.gradle", "LICENSE", "README.md");

    testFactoryWithSpecificBranch.authenticateAndOpen();

    theiaIde.switchToIdeFrame();
    theiaIde.waitTheiaIde();
    theiaIde.waitLoaderInvisibility();
    theiaIde.waitTheiaIdeTopPanel();
    theiaIde.waitAllNotificationsClosed();

    theiaProjectTree.waitFilesTab();
    theiaProjectTree.waitItem(repositoryName);
    theiaIde.waitNotificationDisappearance(
        "Che Workspace: Finished importing projects.", UPDATING_PROJECT_TIMEOUT_SEC);
    theiaIde.waitAllNotificationsClosed();
    theiaProjectTree.expandItemWithIgnoreExceptions(repositoryName);

    expectedItemsAfterCloning.forEach(
        name -> {
          theiaProjectTree.waitItem(repositoryName + "/" + name);
        });

    // check specific branch
    assertEquals(theiaIde.getBranchName(), SECOND_BRANCH_NAME);
  }

  private String getWorkspaceName() {
    String workspaceUrl = seleniumWebDriver.getCurrentUrl();

    return workspaceUrl.substring(workspaceUrl.lastIndexOf('/') + 1);
  }
}
