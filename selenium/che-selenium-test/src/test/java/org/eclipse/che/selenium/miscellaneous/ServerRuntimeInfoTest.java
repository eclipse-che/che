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

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.util.List;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CheTerminal;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Vlad Zhukovskyi */
public class ServerRuntimeInfoTest {

  private List<String> expectedReferenceList =
      Lists.newArrayList(
          "codeserver",
          "tomcat8",
          "tomcat8-debug",
          "wsagent/ws",
          "exec-agent/ws",
          "wsagent/http",
          "exec-agent/http",
          "wsagent-debug");

  @Inject private TestWorkspace testWorkspace;
  @Inject private Loader loader;
  @Inject private Consoles consoles;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private CheTerminal terminal;

  @BeforeClass
  public void setUp() throws Exception {
    ide.open(testWorkspace);
  }

  @Test
  public void testShouldCheckServerList() throws Exception {
    ide.waitOpenedWorkspaceIsReadyToUse();

    consoles.clickOnPlusMenuButton();
    consoles.clickOnServerItemInContextMenu();
    consoles.waitProcessInProcessConsoleTree("Servers");
    consoles.waitTabNameProcessIsPresent("Servers");
    consoles.waitExpectedTextIntoServerTableCation("Servers of dev-machine:");

    for (String server : expectedReferenceList) {
      consoles.checkThatServerExists(server);
    }

    consoles.clickOnHideInternalServers();
    consoles.waitReferenceIsNotPresent("gwt-debug-runtime-info-reference-3");
    consoles.waitReferenceIsNotPresent("gwt-debug-runtime-info-reference-6");
  }
}
