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
package org.eclipse.che.selenium.consoles;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.util.List;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Vlad Zhukovskyi */
public class ServerRuntimeInfoTest {

  @Inject private TestWorkspace testWorkspace;
  @Inject private Loader loader;
  @Inject private Consoles consoles;
  @Inject private Ide ide;

  @BeforeClass
  public void setUp() throws Exception {
    ide.open(testWorkspace);
  }

  @Test
  public void testShouldCheckServerList() throws Exception {
    loader.waitOnClosed();
    consoles.clickOnPlusMenuButton();
    consoles.clickOnServerItemInContextMenu();
    consoles.waitProcessInProcessConsoleTree("Servers");
    consoles.waitTabNameProcessIsPresent("Servers");
    consoles.waitExpectedTextIntoServerTableCation("Servers of dev-machine:");

    List<String> expectedReferenceList =
        Lists.newArrayList(
            "codeserver",
            "tomcat8",
            "tomcat8-debug",
            "wsagent/ws",
            "exec-agent/ws",
            "wsagent/http",
            "exec-agent/http",
            "wsagent-debug",
            "terminal");

    for (int i = 0; i < expectedReferenceList.size(); i++) {
      String referenceId = "gwt-debug-runtime-info-reference-" + i;
      consoles.checkReferenceList(referenceId, expectedReferenceList.get(i));
    }

    consoles.clickOnHideInternalServers();
    consoles.waitReferenceIsNotPresent("gwt-debug-runtime-info-reference-3");
    consoles.waitReferenceIsNotPresent("gwt-debug-runtime-info-reference-6");
  }
}
