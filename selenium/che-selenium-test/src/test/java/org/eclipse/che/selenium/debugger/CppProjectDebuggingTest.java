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

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.InjectTestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.core.workspace.WorkspaceTemplate;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.debug.CppDebugConfig;
import org.eclipse.che.selenium.pageobject.debug.DebugPanel;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsPalette;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Dmytro Nochevnov */
public class CppProjectDebuggingTest {

  private static final String PROJECT = "cpp-tests";
  private static final String PATH_TO_PROGRAM = PROJECT + "/hello.cc";
  private static final int DEBUG_PORT = 8001;

  private static final String MAKE_AND_DEBUG_COMMAND_NAME = "make and debug";

  @InjectTestWorkspace(template = WorkspaceTemplate.ECLIPSE_CPP_GCC)
  private TestWorkspace ws;

  @Inject private Ide ide;

  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private DebugPanel debugPanel;
  @Inject private CppDebugConfig debugConfig;
  @Inject private NotificationsPopupPanel notifications;
  @Inject private Menu menu;
  @Inject private CodenvyEditor editor;
  @Inject private Consoles consoles;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private CommandsPalette commandsPalette;

  @BeforeClass
  public void setup() throws Exception {
    URL resource =
        CppProjectDebuggingTest.this
            .getClass()
            .getResource("/projects/plugins/DebuggerPlugin/cpp-tests");
    testProjectServiceClient.importProject(
        ws.getId(), Paths.get(resource.toURI()), PROJECT, ProjectTemplates.CPP);

    // open IDE
    ide.open(ws);
    loader.waitOnClosed();
    projectExplorer.waitItem(PROJECT);
    notifications.waitProgressPopupPanelClose();

    // open project tree
    projectExplorer.quickExpandWithJavaScript();

    // add debug config
    menu.runCommand(
        TestMenuCommandsConstants.Run.RUN_MENU,
        TestMenuCommandsConstants.Run.EDIT_DEBUG_CONFIGURATION);
    debugConfig.createConfig(PROJECT, DEBUG_PORT);
  }

  @AfterMethod
  public void stopDebug() {
    menu.runCommand(
        TestMenuCommandsConstants.Run.RUN_MENU, TestMenuCommandsConstants.Run.END_DEBUG_SESSION);
    debugPanel.removeAllBreakpoints();
  }

  @AfterClass
  public void removeConfig() {
    menu.runCommand(
        TestMenuCommandsConstants.Run.RUN_MENU,
        TestMenuCommandsConstants.Run.EDIT_DEBUG_CONFIGURATION);
    debugConfig.removeConfig(PROJECT);
  }

  @Test
  public void shouldDebugCppProject() {
    // when
    projectExplorer.openItemByPath(PATH_TO_PROGRAM);
    editor.setInactiveBreakpoint(22);
    editor.closeAllTabs();
    commandsPalette.openCommandPalette();
    commandsPalette.startCommandByDoubleClick(MAKE_AND_DEBUG_COMMAND_NAME);
    consoles.waitExpectedTextIntoConsole("Listening on port 8001");

    menu.runCommandByXpath(
        TestMenuCommandsConstants.Run.RUN_MENU,
        TestMenuCommandsConstants.Run.DEBUG,
        getXpathForDebugConfigurationMenuItem());

    notifications.waitExpectedMessageOnProgressPanelAndClosed(
        String.format("Remote debugger connected\nConnected to: localhost:%s.", DEBUG_PORT));

    // then
    editor.waitTabFileWithSavedStatus("hello.cc");
    debugPanel.waitDebugHighlightedText("  return \"Hello World, \" + name + \"!\";");
    debugPanel.waitTextInVariablesPanel("name =");
    debugPanel.waitTextInVariablesPanel("\"man\"");

    // when
    debugPanel.clickOnButton(DebugPanel.DebuggerActionButtons.STEP_OUT);

    // then
    debugPanel.waitDebugHighlightedText("  std::cout << hello.sayHello(\"man\") << std::endl;");
    debugPanel.waitTextInVariablesPanel("hello={<No data fields>}");
  }

  private String getXpathForDebugConfigurationMenuItem() {
    return String.format(
        "//*[@id=\"%1$s/%2$s\" or @id=\"topmenu/Run/Debug/Debug '%2$s'\"]",
        TestMenuCommandsConstants.Run.DEBUG, PROJECT);
  }
}
