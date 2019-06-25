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
package org.eclipse.che.selenium.workspaces;

import static org.eclipse.che.commons.lang.NameGenerator.generate;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspaceProvider;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.eclipse.che.selenium.pageobject.theia.TheiaIde;
import org.eclipse.che.selenium.pageobject.theia.TheiaProjectTree;
import org.eclipse.che.selenium.pageobject.theia.TheiaProposalForm;
import org.openqa.selenium.Keys;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class CreateWorkspaceOnDashboardTest {

  private static final String WS_NAME = generate("workspace", 4);
  private static final String PROJECT_NAME = "web-java-spring";
  private static final String WEB_JAVA_SPRING_SAMPLE_URL =
      "https://github.com/che-samples/web-java-spring.git";

  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private SeleniumWebDriverHelper seleniumWebDriverHelper;
  @Inject private NewWorkspace newWorkspace;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private Workspaces workspaces;
  @Inject private Dashboard dashboard;
  @Inject private TestWorkspaceProvider testWorkspaceProvider;
  @Inject private TheiaIde theiaIde;
  @Inject private TheiaProjectTree theiaProjectTree;
  @Inject private TheiaProposalForm theiaProposalForm;

  // it is used to read workspace logs on test failure
  private TestWorkspace testWorkspace;

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WS_NAME, defaultTestUser.getName());
  }

  @Test
  public void createWorkspaceOnDashboardTest() {
    dashboard.open();
    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();
    dashboard.waitToolbarTitleName("Workspaces");

    // create and start a new workspace
    workspaces.clickOnAddWorkspaceBtn();
    newWorkspace.waitToolbar();
    newWorkspace.typeWorkspaceName(WS_NAME);
    newWorkspace.selectStack(Stack.JAVA_MAVEN);
    newWorkspace.clickOnCreateButtonAndOpenInIDE();

    // store info about created workspace to make SeleniumTestHandler.captureTestWorkspaceLogs()
    // possible to read logs in case of test failure
    testWorkspace = testWorkspaceProvider.getWorkspace(WS_NAME, defaultTestUser);

    // switch to the IDE and wait for workspace is ready to use
    theiaIde.switchToIdeFrame();
    theiaIde.waitTheiaIde();
    theiaIde.waitLoaderInvisibility();
    theiaIde.waitTheiaIdeTopPanel();
    theiaProjectTree.waitFilesTab();

    // wait the project explorer
    theiaProjectTree.clickOnFilesTab();
    theiaProjectTree.waitProjectsRootItem();
    theiaProjectTree.waitProjectAreaOpened();
    theiaIde.waitNotificationMessageContains("Do you want to exclude");
    theiaIde.clickOnNotificationCloseButton();
    theiaIde.waitNotificationPanelClosed();

    // Import the sample "web-java-spring" project
    theiaIde.pressKeyCombination(Keys.LEFT_CONTROL, Keys.LEFT_SHIFT, "p");
    theiaProposalForm.waitForm();
    theiaProposalForm.enterTextToSearchField(">clo");
    theiaProposalForm.waitProposal("Git: Clone...");
    theiaProposalForm.clickOnProposal("Git: Clone...");
    theiaProposalForm.enterTextToSearchField(WEB_JAVA_SPRING_SAMPLE_URL);
    seleniumWebDriverHelper.pressEnter();
    theiaProjectTree.waitItem(PROJECT_NAME);
  }
}
