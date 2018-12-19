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
package org.eclipse.che.selenium.stack;

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.core.TestGroup.UNDER_REPAIR;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.PREPARING_WS_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.JAVA_THEIA_DOCKER;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.CreateWorkspaceHelper;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.theia.TheiaIde;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Skoryk Serhii */
@Test(groups = {TestGroup.DOCKER, UNDER_REPAIR})
public class CreateWorkspaceFromJavaTheiaDockerStackTest {
  private static final String WORKSPACE_NAME = generate("workspace", 4);

  @Inject private TheiaIde theiaIde;
  @Inject private Dashboard dashboard;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private CreateWorkspaceHelper createWorkspaceHelper;
  @Inject private SeleniumWebDriverHelper seleniumWebDriverHelper;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;

  // it is used to read workspace logs on test failure
  private TestWorkspace testWorkspace;

  @BeforeClass
  public void setUp() {
    dashboard.open();
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE_NAME, defaultTestUser.getName());
  }

  @Test
  public void createWorkspaceFromJavaTheiaDockerStack() {
    // store info about created workspace to make SeleniumTestHandler.captureTestWorkspaceLogs()
    // possible to read logs in case of test failure
    testWorkspace =
        createWorkspaceHelper.createWorkspaceFromStackWithoutProject(
            JAVA_THEIA_DOCKER, WORKSPACE_NAME);

    seleniumWebDriverHelper.waitAndSwitchToFrame(
        By.id("ide-application-iframe"), PREPARING_WS_TIMEOUT_SEC);

    // wait Theia is ready to use
    try {
      theiaIde.waitTheiaIde();
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known permanent failure https://github.com/eclipse/che/issues/12218");
    }

    theiaIde.waitTheiaIdeTopPanel();
    theiaIde.waitLoaderInvisibility();
    theiaIde.waitNotificationPanelClosed();

    // run 'About' command from 'Help' menu
    theiaIde.runMenuCommand("Help", "About");

    // wait 'About' dialog, check its content and close
    theiaIde.waitAboutDialogIsOpen();
    theiaIde.waitAboutDialogContains("List of extensions");
    theiaIde.closeAboutDialog();
    theiaIde.waitAboutDialogIsClosed();
  }
}
