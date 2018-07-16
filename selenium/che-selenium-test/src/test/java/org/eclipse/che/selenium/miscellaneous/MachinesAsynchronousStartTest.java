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
package org.eclipse.che.selenium.miscellaneous;

import com.google.inject.Inject;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspaceProvider;
import org.eclipse.che.selenium.core.workspace.WorkspaceTemplate;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

public class MachinesAsynchronousStartTest {
  private static final String BROKEN_WOKSPACE_NAME = NameGenerator.generate("brokenWorkspace", 6);

  /*@InjectTestWorkspace(template = BROKEN)
  private TestWorkspace brokenWorkspace;*/

  @Inject private TestWorkspace testWorkspace;
  @Inject private Dashboard dashboard;
  @Inject private Workspaces workspaces;
  @Inject private TestWorkspaceProvider testWorkspaceProvider;
  @Inject private TestWorkspaceServiceClient testWorkspaceServiceClient;
  @Inject private DefaultTestUser defaultTestUser;

  private TestWorkspace brokenWorkspace;

  @AfterClass
  public void cleanUp() throws Exception {
    testWorkspaceServiceClient.delete(brokenWorkspace.getName(), defaultTestUser.getName());
  }

  @Test
  public void checkWorkspaces() throws Exception {
    dashboard.open();
    brokenWorkspace = createBrokenWorkspace();
    testWorkspaceServiceClient.start(
        brokenWorkspace.getId(), brokenWorkspace.getName(), defaultTestUser);

    int i = 0;
  }

  private TestWorkspace createBrokenWorkspace() throws Exception {
    return testWorkspaceProvider.createWorkspace(
        defaultTestUser, 2, WorkspaceTemplate.BROKEN, true);
  }
}
