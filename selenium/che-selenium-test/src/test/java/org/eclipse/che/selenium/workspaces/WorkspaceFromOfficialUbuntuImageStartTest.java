/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.workspaces;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.constant.TestWorkspaceConstants;
import org.eclipse.che.selenium.core.workspace.InjectTestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.core.workspace.WorkspaceTemplate;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.testng.annotations.Test;

/** @author Alexander Garagatyi */
public class WorkspaceFromOfficialUbuntuImageStartTest {
  @InjectTestWorkspace(template = WorkspaceTemplate.UBUNTU)
  private TestWorkspace testWorkspace;

  @Inject private Ide ide;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;

  @Test
  public void ensureWorkspaceStartsFromOfficialUbuntuImage() throws Exception {
    ide.open(testWorkspace);
    notificationsPopupPanel.waitExpectedMessageOnProgressPanelAndClosed(
        TestWorkspaceConstants.RUNNING_WORKSPACE_MESS, 300);
  }
}
