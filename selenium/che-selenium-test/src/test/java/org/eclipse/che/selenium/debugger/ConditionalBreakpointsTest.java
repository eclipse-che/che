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
package org.eclipse.che.selenium.debugger;

import static org.eclipse.che.selenium.core.constant.TestCommandsConstants.CUSTOM;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Run.EDIT_DEBUG_CONFIGURATION;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Run.RUN_MENU;
import static org.eclipse.che.selenium.core.project.ProjectTemplates.PLAIN_JAVA;
import static org.eclipse.che.selenium.pageobject.debug.DebugPanel.DebuggerActionButtons.BTN_DISCONNECT;
import static org.eclipse.che.selenium.pageobject.debug.DebugPanel.DebuggerActionButtons.RESUME_BTN_ID;
import static org.testng.Assert.assertEquals;

import com.google.inject.Inject;
import java.nio.file.Paths;
import java.util.List;
import org.eclipse.che.api.debug.shared.model.impl.BreakpointConfigurationImpl;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestCommandServiceClient;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestBuildConstants;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.debug.DebugPanel;
import org.eclipse.che.selenium.pageobject.debug.JavaDebugConfig;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsPalette;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class ConditionalBreakpointsTest {
  private static final String PROJECT = NameGenerator.generate("project", 2);

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;

  @Inject private ProjectExplorer projectExplorer;
  @Inject private CodenvyEditor editor;
  @Inject private DebugPanel debugPanel;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private TestCommandServiceClient testCommandServiceClient;
  @Inject private CommandsPalette commandsPalette;
  @Inject private Consoles consoles;
  @Inject private Menu menu;
  @Inject private NotificationsPopupPanel notifications;
  @Inject private JavaDebugConfig debugConfig;

  @BeforeClass
  public void setUp() throws Exception {
    testProjectServiceClient.importProject(
        ws.getId(),
        Paths.get(getClass().getResource("/projects/plugins/DebuggerPlugin/hello-world").toURI()),
        PROJECT,
        PLAIN_JAVA);

    testCommandServiceClient.createCommand(
        "cd ${current.project.path}/src/ && javac -g HelloWorld.java", "build", CUSTOM, ws.getId());

    testCommandServiceClient.createCommand(
        "cd ${current.project.path}/src/ &&"
            + " java -Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=y HelloWorld",
        "debug",
        CUSTOM,
        ws.getId());

    ide.open(ws);
    projectExplorer.waitItem(PROJECT);
    commandsPalette.openCommandPalette();
    commandsPalette.startCommandByDoubleClick("build");
    projectExplorer.quickExpandWithJavaScript();
    debugPanel.openDebugPanel();
  }

  @Test
  public void shouldNavigateToBreakpoint() throws Exception {
    projectExplorer.openItemByPath(PROJECT + "/src/HelloWorld.java");
    editor.setBreakpoint(14);
    editor.waitInactiveBreakpoint(14);

    projectExplorer.openItemByVisibleNameInExplorer("External Libraries");
    projectExplorer.openItemByVisibleNameInExplorer("rt.jar");
    projectExplorer.openItemByVisibleNameInExplorer("com");
    projectExplorer.openItemByVisibleNameInExplorer("oracle");
    projectExplorer.openItemByVisibleNameInExplorer("net");
    projectExplorer.openItemByVisibleNameInExplorer("Sdp");
    editor.setBreakpoint(6);
    editor.waitInactiveBreakpoint(6);

    editor.closeAllTabs();

    debugPanel.navigateToBreakpoint("com.oracle.net.Sdp", 6);
    editor.waitActiveTabFileName("Sdp");

    debugPanel.navigateToBreakpoint("HelloWorld.java", 14);
    editor.waitActiveTabFileName("HelloWorld");

    editor.closeAllTabs();
  }

  @Test(priority = 1)
  public void shouldAddConditionalBreakpoint() throws Exception {
    projectExplorer.openItemByPath(PROJECT + "/src/HelloWorld.java");
    editor.setBreakpoint(15);
    editor.waitInactiveBreakpoint(15);

    debugPanel.configureBreakpoint(
        "HelloWorld.java", 15, new BreakpointConfigurationImpl("i == 3"));
    editor.waitConditionalBreakpoint(15, false);

    projectExplorer.selectItem(PROJECT);

    startDebug();

    editor.waitConditionalBreakpoint(15, true);

    String filePath = "/" + PROJECT + "/src/HelloWorld.java";
    debugPanel.waitBreakpointState(filePath, 15, DebugPanel.BreakpointState.ACTIVE, true);
    debugPanel.clickOnButton(RESUME_BTN_ID);
    debugPanel.waitTextInVariablesPanel("i=3");
  }

  @Test(priority = 2)
  public void shouldDisableBreakpoint() throws Exception {
    editor.setBreakpoint(18);
    editor.waitActiveBreakpoint(18);
    debugPanel.disableBreakpoint("HelloWorld.java", 18);
    editor.waitDisabledBreakpoint(18);

    editor.setBreakpoint(19);
    editor.waitActiveBreakpoint(19);

    debugPanel.clickOnButton(RESUME_BTN_ID);
    debugPanel.waitDebugHighlightedText("System.out.println(j);");

    String filePath = "/" + PROJECT + "/src/HelloWorld.java";
    debugPanel.waitBreakpointState(filePath, 14, DebugPanel.BreakpointState.ACTIVE, false);
    debugPanel.waitBreakpointState(filePath, 15, DebugPanel.BreakpointState.ACTIVE, true);
    debugPanel.waitBreakpointState(filePath, 18, DebugPanel.BreakpointState.DISABLED, false);
    debugPanel.waitBreakpointState(filePath, 19, DebugPanel.BreakpointState.ACTIVE, false);
    debugPanel.waitBreakpointState(
        "com.oracle.net.Sdp", 6, DebugPanel.BreakpointState.INACTIVE, false);
  }

  @Test(priority = 3)
  public void shouldDeleteBreakpoint() throws Exception {
    List<String> breakpoints = debugPanel.getAllBreakpoints();

    debugPanel.deleteBreakpoint("HelloWorld.java", 19);

    assertEquals(debugPanel.getAllBreakpoints().size(), breakpoints.size() - 1);
  }

  @Test(priority = 4)
  public void shouldCheckBreakpointWhenDebuggerDisconnected() throws Exception {
    debugPanel.clickOnButton(BTN_DISCONNECT);

    editor.waitInactiveBreakpoint(14);
    editor.waitConditionalBreakpoint(15, false);
    editor.waitDisabledBreakpoint(18);

    String filePath = "/" + PROJECT + "/src/HelloWorld.java";
    debugPanel.waitBreakpointState(filePath, 14, DebugPanel.BreakpointState.INACTIVE, false);
    debugPanel.waitBreakpointState(filePath, 15, DebugPanel.BreakpointState.INACTIVE, true);
    debugPanel.waitBreakpointState(filePath, 18, DebugPanel.BreakpointState.DISABLED, false);
  }

  private void startDebug() {
    menu.runCommand(RUN_MENU, EDIT_DEBUG_CONFIGURATION);
    debugConfig.createConfig(PROJECT);

    commandsPalette.openCommandPalette();
    commandsPalette.startCommandByDoubleClick("debug");
    consoles.waitExpectedTextIntoConsole(TestBuildConstants.LISTENING_AT_ADDRESS_8000);

    menu.runCommandByXpath(
        TestMenuCommandsConstants.Run.RUN_MENU,
        TestMenuCommandsConstants.Run.DEBUG,
        debugConfig.getXpathToІRunDebugCommand(PROJECT));
    notifications.waitExpectedMessageOnProgressPanelAndClosed("Remote debugger connected");
  }
}
