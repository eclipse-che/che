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

import com.google.inject.Inject;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.client.TestCommandServiceClient;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestBuildConstants;
import org.eclipse.che.selenium.core.constant.TestCommandsConstants;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.*;
import org.eclipse.che.selenium.pageobject.debug.DebugPanel;
import org.eclipse.che.selenium.pageobject.debug.JavaDebugConfig;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsPalette;
import org.testng.annotations.*;

/** @author Igor Vinokur */
public class RunDebugToCursorTest {
  private static final String PROJECT = "java-run-to-cursor";
  private static final String PATH_TO_CLASS = PROJECT + "/src/main/java/test/App.java";

  private static final String BUILD_AND_DEBUG_COMMAND_NAME = "build-and-debug";

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;

  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private DebugPanel debugPanel;
  @Inject private JavaDebugConfig debugConfig;
  @Inject private NotificationsPopupPanel notificationPopup;
  @Inject private Menu menu;
  @Inject private CodenvyEditor editor;
  @Inject private Consoles consoles;
  @Inject private TestCommandServiceClient testCommandServiceClient;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private CommandsPalette commandsPalette;

  @BeforeClass
  public void setup() throws Exception {
    testProjectServiceClient.importProject(
        ws.getId(),
        Paths.get(
            getClass().getResource("/projects/plugins/DebuggerPlugin/java-run-to-cursor").toURI()),
        PROJECT,
        ProjectTemplates.CONSOLE_JAVA_SIMPLE);

    testCommandServiceClient.createCommand(
        "mvn -f ${current.project.path} clean install && "
            + "java -jar -Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=y "
            + "${current.project.path}/target/*.jar",
        BUILD_AND_DEBUG_COMMAND_NAME,
        TestCommandsConstants.MAVEN,
        ws.getId());

    // open IDE
    ide.open(ws);
    loader.waitOnClosed();
    projectExplorer.waitItem(PROJECT);
    notificationPopup.waitProgressPopupPanelClose();

    // add java debug configuration
    menu.runCommand(
        TestMenuCommandsConstants.Run.RUN_MENU,
        TestMenuCommandsConstants.Run.EDIT_DEBUG_CONFIGURATION);
    debugConfig.createConfig(PROJECT);

    // open test class
    projectExplorer.quickRevealToItemWithJavaScript(PATH_TO_CLASS);
    projectExplorer.openItemByPath(PATH_TO_CLASS);
  }

  @BeforeClass
  public void startDebug() {
    // start application in debug mode
    commandsPalette.openCommandPalette();
    commandsPalette.startCommandByDoubleClick(BUILD_AND_DEBUG_COMMAND_NAME);
    consoles.waitExpectedTextIntoConsole(TestBuildConstants.LISTENING_AT_ADDRESS_8000);
    // set breakpoints
    editor.waitActiveEditor();
    editor.setCursorToLine(16);
    editor.setInactiveBreakpoint(16);
    menu.runCommandByXpath(
        TestMenuCommandsConstants.Run.RUN_MENU,
        TestMenuCommandsConstants.Run.DEBUG,
        String.format(
            "//*[@id=\"%1$s/%2$s\" or @id=\"topmenu/Run/Debug/Debug '%2$s'\"]",
            TestMenuCommandsConstants.Run.DEBUG, PROJECT));
    notificationPopup.waitExpectedMessageOnProgressPanelAndClosed("Remote debugger connected");
    editor.waitActiveBreakpoint(16);
  }

  @AfterClass
  public void stopDebug() {
    debugPanel.removeAllBreakpoints();
    menu.runCommand(
        TestMenuCommandsConstants.Run.RUN_MENU, TestMenuCommandsConstants.Run.END_DEBUG_SESSION);
  }

  @Test
  public void shouldRunToLocationInsideMainClass() {
    // when
    editor.setCursorToLine(17);
    debugPanel.clickOnButton(DebugPanel.DebuggerButtonsPanel.RUN_TO_CURSOR);

    // then
    debugPanel.waitTextInVariablesPanel("string=\"line\"");
  }

  @Test(priority = 1)
  public void shouldRunToLocationInsideMethod() {
    // when
    editor.setCursorToLine(23);
    debugPanel.clickOnButton(DebugPanel.DebuggerButtonsPanel.RUN_TO_CURSOR);

    // then
    debugPanel.waitTextInVariablesPanel("string=\"method\"");
  }

  @Test(priority = 2)
  public void shouldRunToLocationInsideInnerClass() {
    // when
    editor.setCursorToLine(29);
    debugPanel.clickOnButton(DebugPanel.DebuggerButtonsPanel.RUN_TO_CURSOR);

    // then
    debugPanel.waitTextInVariablesPanel("string=\"inner class\"");
  }

  @Test(priority = 3)
  public void shouldRunToLocationInsideInnerClassMethod() {
    // when
    editor.setCursorToLine(33);
    debugPanel.clickOnButton(DebugPanel.DebuggerButtonsPanel.RUN_TO_CURSOR);

    // then
    debugPanel.waitTextInVariablesPanel("string=\"inner class method\"");
  }
}
