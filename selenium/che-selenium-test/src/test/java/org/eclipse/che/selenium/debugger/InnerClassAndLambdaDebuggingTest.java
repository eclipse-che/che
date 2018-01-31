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
package org.eclipse.che.selenium.debugger;

import static org.eclipse.che.selenium.pageobject.debug.DebugPanel.DebuggerActionButtons.BTN_DISCONNECT;
import static org.eclipse.che.selenium.pageobject.debug.DebugPanel.DebuggerActionButtons.RESUME_BTN_ID;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.client.TestCommandServiceClient;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestBuildConstants;
import org.eclipse.che.selenium.core.constant.TestCommandsConstants;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.debug.DebugPanel;
import org.eclipse.che.selenium.pageobject.debug.JavaDebugConfig;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsPalette;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Dmytro Nochevnov */
public class InnerClassAndLambdaDebuggingTest {
  private static final String PROJECT = "java-inner-lambda";
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
            getClass().getResource("/projects/plugins/DebuggerPlugin/java-inner-lambda").toURI()),
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
    consoles.waitJDTLSProjectResolveFinishedMessage(PROJECT);
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

  @BeforeMethod
  public void startDebug() {
    // start application in debug mode
    commandsPalette.openCommandPalette();
    commandsPalette.startCommandByDoubleClick(BUILD_AND_DEBUG_COMMAND_NAME);
    consoles.waitExpectedTextIntoConsole(TestBuildConstants.LISTENING_AT_ADDRESS_8000);
    // set breakpoints
    editor.waitActive();
    editor.setCursorToLine(42);
    editor.setInactiveBreakpoint(42);
    menu.runCommandByXpath(
        TestMenuCommandsConstants.Run.RUN_MENU,
        TestMenuCommandsConstants.Run.DEBUG,
        String.format(
            "//*[@id=\"%1$s/%2$s\" or @id=\"topmenu/Run/Debug/Debug '%2$s'\"]",
            TestMenuCommandsConstants.Run.DEBUG, PROJECT));

    try {
      notificationPopup.waitExpectedMessageOnProgressPanelAndClose("Remote debugger connected");
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/10728");
    }

    editor.waitActiveBreakpoint(42);
  }

  @AfterMethod
  public void stopDebug() {
    debugPanel.removeAllBreakpoints();
    debugPanel.clickOnButton(BTN_DISCONNECT);
  }

  @Test
  public void shouldDebugAnonymousClass() {
    // when
    editor.setCursorToLine(38);
    editor.setBreakpoint(38);
    debugPanel.clickOnButton(RESUME_BTN_ID);

    // then
    editor.waitActiveBreakpoint(38);
    debugPanel.waitTextInVariablesPanel("anonym=\"App anonym\"");
  }

  @Test(priority = 1)
  public void shouldDebugMethodLocalInnerClass() {
    // when
    editor.setCursorToLine(54);
    editor.setBreakpoint(54);
    debugPanel.clickOnButton(RESUME_BTN_ID);

    // then
    editor.waitActiveBreakpoint(54);
  }

  @Test(priority = 2)
  public void shouldDebugInnerClass() {
    // when
    editor.setCursorToLine(65);
    editor.setBreakpoint(65);
    debugPanel.clickOnButton(RESUME_BTN_ID);

    // then
    editor.waitActiveBreakpoint(65);
    debugPanel.waitTextInVariablesPanel("innerValue=\"App inner value\"");
  }

  @Test(priority = 3)
  public void shouldDebugStaticInnerClass() {
    // when
    editor.setCursorToLine(73);
    editor.setBreakpoint(73);
    debugPanel.clickOnButton(RESUME_BTN_ID);

    // then
    editor.waitActiveBreakpoint(73);
    debugPanel.waitTextInVariablesPanel("staticInnerValue=\"App static inner value\"");
  }

  @Test(priority = 4)
  public void shouldDebugLambdaExpressions() {
    // when
    editor.setCursorToLine(80);
    editor.setBreakPointAndWaitActiveState(80);
    editor.setBreakPointAndWaitActiveState(88);
    debugPanel.clickOnButton(RESUME_BTN_ID);

    // then
    editor.waitActiveBreakpoint(80);
    debugPanel.waitTextInVariablesPanel("j=1");

    // when
    debugPanel.clickOnButton(RESUME_BTN_ID);

    // then
    editor.waitActiveBreakpoint(80);
    debugPanel.waitTextInVariablesPanel("j=2");

    // when
    debugPanel.clickOnButton(RESUME_BTN_ID);

    // then
    editor.waitActiveBreakpoint(88);
    debugPanel.waitTextInVariablesPanel("j=2");
  }
}
