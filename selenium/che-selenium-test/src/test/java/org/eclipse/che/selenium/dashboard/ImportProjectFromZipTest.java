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
package org.eclipse.che.selenium.dashboard;

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.core.TestGroup.UNDER_REPAIR;
import static org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage.Sources.ZIP;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.client.TestGitHubRepository;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspaceProvider;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack;
import org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.eclipse.che.selenium.pageobject.theia.TheiaIde;
import org.eclipse.che.selenium.pageobject.theia.TheiaProjectTree;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
@Test(groups = UNDER_REPAIR)
public class ImportProjectFromZipTest {

  private final String WORKSPACE = generate("ImptPrjFromZip", 4);
  private static final String PROJECT_NAME = "master";

  @Inject private Dashboard dashboard;
  @Inject private NewWorkspace newWorkspace;
  @Inject private ProjectSourcePage projectSourcePage;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private Workspaces workspaces;
  @Inject private TestGitHubRepository testRepo;
  @Inject private TestWorkspaceProvider testWorkspaceProvider;
  @Inject private TheiaIde theiaIde;
  @Inject private TheiaProjectTree theiaProjectTree;

  // it is used to read workspace logs on test failure
  private TestWorkspace testWorkspace;

  @BeforeClass
  public void setUp() throws IOException {
    Path entryPath = Paths.get(getClass().getResource("/projects/java-multimodule").getPath());
    testRepo.addContent(entryPath);

    dashboard.open();
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE, defaultTestUser.getName());
  }

  @Test
  public void importProjectFromZipTest() {
    String testRepoFullName = testRepo.getFullName();
    String zipUrl =
        String.format("%s/%s/%s", "https://github.com", testRepoFullName, "archive/master.zip");

    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();

    workspaces.clickOnAddWorkspaceBtn();
    newWorkspace.waitToolbar();

    // we are selecting 'Java' stack from the 'All Stack' tab for compatibility with OSIO
    newWorkspace.selectStack(Stack.JAVA_MAVEN);
    newWorkspace.typeWorkspaceName(WORKSPACE);

    projectSourcePage.clickOnAddOrImportProjectButton();
    projectSourcePage.selectSourceTab(ZIP);
    projectSourcePage.typeZipLocation(zipUrl);
    projectSourcePage.skipRootFolder();
    projectSourcePage.clickOnAddProjectButton();

    newWorkspace.clickOnCreateButtonAndOpenInIDE();
    // store info about created workspace to make SeleniumTestHandler.captureTestWorkspaceLogs()
    // possible to read logs in case of test failure
    testWorkspace = testWorkspaceProvider.getWorkspace(WORKSPACE, defaultTestUser);

    theiaIde.switchToIdeFrame();
    theiaIde.waitTheiaIde();
    theiaIde.waitLoaderInvisibility();
    theiaIde.waitTheiaIdeTopPanel();
    theiaProjectTree.waitFilesTab();

    // wait the project in the tree
    theiaProjectTree.clickOnFilesTab();
    theiaProjectTree.waitProjectsRootItem();
    theiaProjectTree.waitProjectAreaOpened();
    theiaIde.waitTheiaIdeTopPanel();
    theiaProjectTree.waitFilesTab();

    try {
      theiaProjectTree.waitItem(PROJECT_NAME);
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known permanent failure https://github.com/eclipse/che/issues/13579");
    }
  }
}
