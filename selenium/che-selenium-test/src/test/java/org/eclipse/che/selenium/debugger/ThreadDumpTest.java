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

import static org.eclipse.che.selenium.core.constant.TestCommandsConstants.MAVEN;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Run.DEBUG;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Run.EDIT_DEBUG_CONFIGURATION;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Run.RUN_MENU;
import static org.eclipse.che.selenium.core.project.ProjectTemplates.CONSOLE_JAVA_SIMPLE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.inject.Inject;
import java.nio.file.Paths;
import org.eclipse.che.api.debug.shared.model.SuspendPolicy;
import org.eclipse.che.api.debug.shared.model.impl.BreakpointConfigurationImpl;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestCommandServiceClient;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestBuildConstants;
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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Anatolii Bazko */
public class ThreadDumpTest {
  private static final String PROJECT = NameGenerator.generate("project", 2);
  private static final String DEBUG_COMMAND = "debug";

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
  public void setUp() throws Exception {
    testProjectServiceClient.importProject(
        ws.getId(),
        Paths.get(
            getClass().getResource("/projects/plugins/DebuggerPlugin/java-multimodule").toURI()),
        PROJECT,
        CONSOLE_JAVA_SIMPLE);

    testCommandServiceClient.createCommand(
        "mvn -f ${current.project.path} clean install &&"
            + " java -Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=y"
            + " -classpath ${current.project.path}/app/target/classes/:${current.project.path}/model/target/classes multimodule.App",
        DEBUG_COMMAND,
        MAVEN,
        ws.getId());

    // open IDE
    ide.open(ws);
    loader.waitOnClosed();
    projectExplorer.waitItem(PROJECT);
    notificationPopup.waitProgressPopupPanelClose();

    // open project tree
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.waitVisibleItem(PROJECT);
    projectExplorer.waitAndSelectItem(PROJECT);

    startDebuggingApp();
  }

  @Test
  public void shouldShowAndNavigateByAllThread() {
    assertTrue(debugPanel.getSelectedThread().contains("\"main\"@"));

    String[] frames = debugPanel.getFrames();
    assertEquals(frames.length, 1);
    assertTrue(frames[0].contains("main(String[]):20, multimodule.App"));

    debugPanel.clickOnButton(DebugPanel.DebuggerActionButtons.RESUME_BTN_ID);
    debugPanel.waitDebugHighlightedText("this.title = title;");

    frames = debugPanel.getFrames();
    assertEquals(frames.length, 2);
    assertTrue(frames[0].contains("<init>(String, String):19, multimodule.model.BookImpl"));
    assertTrue(frames[1].contains("main(String[]):20, multimodule.App"));

    editor.closeAllTabs();

    debugPanel.selectFrame(0);
    editor.waitActiveTabFileName("BookImpl");
    assertTrue(debugPanel.getVariables().contains("title=\"java\""));

    debugPanel.selectFrame(1);
    editor.waitActiveTabFileName("App");
    assertTrue(debugPanel.getVariables().contains("args=instance of java.lang.String[0]"));

    debugPanel.selectThread("Finalizer");
    assertTrue(debugPanel.getVariables().isEmpty());

    debugPanel.selectThread("main");
    assertTrue(debugPanel.getVariables().contains("title=\"java\""));
  }

  @Test(priority = 1)
  public void shouldShowAndNavigateBySuspendedThread() {
    editor.setBreakpoint(21);
    debugPanel.configureBreakpoint(
        "App.java", 21, new BreakpointConfigurationImpl(SuspendPolicy.THREAD));
    debugPanel.clickOnButton(DebugPanel.DebuggerActionButtons.RESUME_BTN_ID);

    debugPanel.selectThread("Finalizer");

    debugPanel.waitThreadNotSuspendedHolderVisible();
    assertTrue(debugPanel.getVariables().isEmpty());

    debugPanel.selectThread("main");
    assertTrue(debugPanel.getVariables().contains("args=instance of java.lang.String[0]"));
    debugPanel.waitThreadNotSuspendedHolderHidden();
  }

  private void startDebuggingApp() {
    menu.runCommand(RUN_MENU, EDIT_DEBUG_CONFIGURATION);
    debugConfig.createConfig(PROJECT);

    // starts application in debug mode
    commandsPalette.openCommandPalette();
    commandsPalette.startCommandByDoubleClick(DEBUG_COMMAND);
    consoles.waitExpectedTextIntoConsole(TestBuildConstants.LISTENING_AT_ADDRESS_8000);

    // stops at breakpoint
    projectExplorer.openItemByPath(PROJECT + "/app/src/main/java/multimodule/App.java");
    editor.setInactiveBreakpoint(20);

    projectExplorer.openItemByPath(
        PROJECT + "/model/src/main/java/multimodule/model/BookImpl.java");
    editor.setInactiveBreakpoint(19);

    menu.runCommand(RUN_MENU, DEBUG, DEBUG + "/" + PROJECT);
    notificationPopup.waitExpectedMessageOnProgressPanelAndClosed("Remote debugger connected");
    editor.waitActiveBreakpoint(20);
  }
}
