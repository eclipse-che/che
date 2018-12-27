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
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.CAMEL_SPRINGBOOT;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.dashboard.CreateWorkspaceHelper;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CreateWorkspaceFromCamelStackTest {

  private static final String WORKSPACE_NAME = generate("workspaceCamel", 4);
  private static final String LS_INIT_MESSAGE =
      "Initialized language server 'org.eclipse.che.plugin.camel.server.languageserver'";

  @Inject private Ide ide;
  @Inject private Dashboard dashboard;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private CreateWorkspaceHelper createWorkspaceHelper;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private Consoles consoles;

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
  public void createWorkspaceFromCamelStackTest() {
    // store info about created workspace to make SeleniumTestHandler.captureTestWorkspaceLogs()
    // possible to read logs in case of test failure
    testWorkspace =
        createWorkspaceHelper.createWorkspaceFromStackWithoutProject(
            CAMEL_SPRINGBOOT, WORKSPACE_NAME);

    ide.switchToIdeAndWaitWorkspaceIsReadyToUse();

    consoles.selectProcessByTabName("dev-machine");
    consoles.waitExpectedTextIntoConsole(LS_INIT_MESSAGE);
  }
}
