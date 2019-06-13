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
package org.eclipse.che.selenium.theia;

import static org.eclipse.che.selenium.core.TestGroup.OPENSHIFT;

import com.google.inject.Inject;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.pageobject.dashboard.CreateWorkspaceHelper;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack;
import org.eclipse.che.selenium.pageobject.theia.TheiaIde;
import org.eclipse.che.selenium.pageobject.theia.TheiaProjectTree;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

public class Che7PreviewStackStartTest {
  private static final String WORKSPACE_NAME = NameGenerator.generate("wksp-", 5);

  @Inject private Dashboard dashboard;
  @Inject private TheiaIde theiaIde;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private CreateWorkspaceHelper createWorkspaceHelper;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private TheiaProjectTree theiaProjectTree;

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE_NAME, defaultTestUser.getName());
  }

  @Test(groups = {OPENSHIFT})
  public void workspaceShouldBeStarted() {
    dashboard.open();
    createWorkspaceHelper.createWorkspaceFromStackWithoutProject(Stack.GO, WORKSPACE_NAME);

    theiaIde.switchToIdeFrame();
    theiaIde.waitTheiaIde();
    theiaIde.waitLoaderInvisibility();
    theiaIde.waitTheiaIde();
    theiaIde.waitTheiaIdeTopPanel();
    theiaProjectTree.waitFilesTab();
  }
}
