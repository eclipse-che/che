/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.debugger;

import static org.eclipse.che.selenium.core.constant.TestCommandsConstants.CUSTOM;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Run.EDIT_DEBUG_CONFIGURATION;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Run.RUN_MENU;
import static org.eclipse.che.selenium.core.project.ProjectTemplates.PLAIN_JAVA;

import com.google.inject.Inject;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestCommandServiceClient;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestBuildConstants;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.*;
import org.eclipse.che.selenium.pageobject.debug.DebugPanel;
import org.eclipse.che.selenium.pageobject.debug.JavaDebugConfig;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsPalette;
import org.testng.annotations.*;

/** @author Igor Vinokur */
public class RunToCursorTest {
  private static final String PROJECT = NameGenerator.generate("project", 4);

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;

  @Inject private ProjectExplorer projectExplorer;
  @Inject private DebugPanel debugPanel;
  @Inject private JavaDebugConfig debugConfig;
  @Inject private Menu menu;
  @Inject private CodenvyEditor editor;
  @Inject private Consoles consoles;
  @Inject private TestCommandServiceClient testCommandServiceClient;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private CommandsPalette commandsPalette;
  @Inject private NotificationsPopupPanel notifications;

  @BeforeClass
  public void setup() throws Exception {
    testProjectServiceClient.importProject(
        ws.getId(),
        Paths.get(
            getClass().getResource("/projects/plugins/DebuggerPlugin/java-run-to-cursor").toURI()),
        PROJECT,
        PLAIN_JAVA);

    testCommandServiceClient.createCommand(
        "cd ${current.project.path}/src/ && javac -g App.java", "build", CUSTOM, ws.getId());

    testCommandServiceClient.createCommand(
        "cd ${current.project.path}/src/ &&"
            + " java -Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=y App",
        "debug",
        CUSTOM,
        ws.getId());

    ide.open(ws);
    projectExplorer.waitItem(PROJECT);
    commandsPalette.openCommandPalette();
    commandsPalette.startCommandByDoubleClick("build");
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByPath(PROJECT + "/src/App.java");
    editor.setBreakpoint(15);
    debugPanel.openDebugPanel();

    startDebug();
  }

  @Test
  public void shouldRunToLocationInsideMainClass() {
    // when
    editor.setCursorToLine(22);
    debugPanel.clickOnButton(DebugPanel.DebuggerActionButtons.RUN_TO_CURSOR);

    // then
    debugPanel.waitTextInVariablesPanel("string=\"method\"");
  }

  @Test(priority = 1)
  public void shouldRunToLocationInsideMethod() {
    // when
    editor.setCursorToLine(28);
    debugPanel.clickOnButton(DebugPanel.DebuggerActionButtons.RUN_TO_CURSOR);

    // then
    debugPanel.waitTextInVariablesPanel("string=\"inner class\"");
  }

  @Test(priority = 2)
  public void shouldNotRunToNonExistedLocation() {
    // when
    editor.setCursorToLine(29);
    debugPanel.clickOnButton(DebugPanel.DebuggerActionButtons.RUN_TO_CURSOR);

    // then
    debugPanel.waitTextInVariablesPanel("string=\"inner class\"");
  }

  @Test(priority = 3)
  public void shouldRunToLocationInsideInnerClass() {
    // when
    editor.setCursorToLine(32);
    debugPanel.clickOnButton(DebugPanel.DebuggerActionButtons.RUN_TO_CURSOR);

    // then
    debugPanel.waitTextInVariablesPanel("string=\"inner class method\"");
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
