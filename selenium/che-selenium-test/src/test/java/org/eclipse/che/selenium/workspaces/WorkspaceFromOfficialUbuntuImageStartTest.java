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

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.workspace.InjectTestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.core.workspace.WorkspaceTemplate;
import org.eclipse.che.selenium.pageobject.CheTerminal;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.ToastLoader;
import org.testng.annotations.Test;

/** @author Alexander Garagatyi */
@Test(groups = {TestGroup.DOCKER})
public class WorkspaceFromOfficialUbuntuImageStartTest {
  @InjectTestWorkspace(template = WorkspaceTemplate.UBUNTU)
  private TestWorkspace testWorkspace;

  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private ToastLoader toastLoader;
  @Inject private CheTerminal terminal;

  public void ensureWorkspaceStartsFromOfficialUbuntuImage() throws Exception {
    ide.open(testWorkspace);
    projectExplorer.waitProjectExplorer();
    terminal.waitFirstTerminalTab();
  }
}
