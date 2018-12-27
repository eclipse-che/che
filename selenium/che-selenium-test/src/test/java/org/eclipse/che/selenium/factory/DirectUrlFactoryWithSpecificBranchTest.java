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

import static org.eclipse.che.selenium.core.CheSeleniumSuiteModule.AUXILIARY;
import static org.eclipse.che.selenium.core.TestGroup.GITHUB;
import static org.eclipse.che.selenium.core.TestGroup.OPENSHIFT;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.UPDATING_PROJECT_TIMEOUT_SEC;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestGitHubRepository;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestUserPreferencesServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.factory.TestFactory;
import org.eclipse.che.selenium.core.factory.TestFactoryInitializer;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.PullRequestPanel;
import org.eclipse.che.selenium.pageobject.theia.TheiaEditor;
import org.eclipse.che.selenium.pageobject.theia.TheiaIde;
import org.eclipse.che.selenium.pageobject.theia.TheiaProjectTree;
import org.openqa.selenium.NoSuchElementException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = {GITHUB, OPENSHIFT})
public class DirectUrlFactoryWithSpecificBranchTest {
  private static final String SECOND_BRANCH_NAME = "contrib";

  @Inject
  @Named(AUXILIARY)
  private TestGitHubRepository testAuxiliaryRepo;

  @Inject
  @Named("github.auxiliary.username")
  private String gitHubAuxiliaryUserName;

  @Inject private ProjectExplorer projectExplorer;
  @Inject private DefaultTestUser testUser;
  @Inject private TestFactoryInitializer testFactoryInitializer;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private Events events;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private TheiaProjectTree theiaProjectTree;
  @Inject private TheiaEditor theiaEditor;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private TheiaIde theiaIde;
  @Inject private PullRequestPanel pullRequestPanel;
  @Inject private TestUserPreferencesServiceClient testUserPreferencesServiceClient;

  private TestFactory testFactoryWithSpecificBranch;

  @BeforeClass
  public void setUp() throws Exception {
    // preconditions - add the project to the test repository
    Path entryPath = Paths.get(getClass().getResource("/projects/java-multimodule").getPath());
    testAuxiliaryRepo.addContent(entryPath);
    String repositoryUrl = testAuxiliaryRepo.getHtmlUrl();

    // create another branch in the test repo
    testAuxiliaryRepo.createBranch(SECOND_BRANCH_NAME);

    testFactoryWithSpecificBranch =
        testFactoryInitializer.fromUrl(repositoryUrl + "/tree/" + SECOND_BRANCH_NAME);
  }

  @AfterClass
  public void deleteTestBranch() throws Exception {
    if (workspaceServiceClient.exists(gitHubAuxiliaryUserName, testUser.getName())) {
      testFactoryWithSpecificBranch.delete();
    }
  }

  @AfterClass
  public void restoreContributionTabPreference() throws Exception {
    testUserPreferencesServiceClient.restoreDefaultContributionTabPreference();
  }

  @Test
  public void factoryWithDirectUrlWithSpecificBranch() throws Exception {
    String repositoryName = testAuxiliaryRepo.getName();

    try {
      testFactoryWithSpecificBranch.authenticateAndOpen();
    } catch (NoSuchElementException ex) {
      // remove try-catch block after issue has been resolved
      fail("https://github.com/eclipse/che/issues/8671");
    }

    theiaIde.switchToIdeFrame();
    theiaIde.waitTheiaIde();
    theiaIde.waitLoaderInvisibility();

    theiaProjectTree.waitFilesTab();
    theiaProjectTree.clickOnFilesTab();
    theiaProjectTree.waitProjectsRootItem();
    theiaIde.waitNotificationDisappearance(
        "Che Workspace: Finished cloning projects.", UPDATING_PROJECT_TIMEOUT_SEC);

    theiaProjectTree.waitItem(repositoryName);
    theiaProjectTree.clickOnItem(repositoryName);
    theiaProjectTree.waitItemSelected(repositoryName);
    theiaProjectTree.openItem(repositoryName);
    theiaProjectTree.expandPathAndOpenFile(repositoryName + "/my-lib", "pom.xml");
    // TODO check visible items
    theiaEditor.waitEditorTab("pom.xml");
  }
}
