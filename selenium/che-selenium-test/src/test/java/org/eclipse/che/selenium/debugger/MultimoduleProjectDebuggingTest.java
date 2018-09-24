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
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Dmytro Nochevnov */
public class MultimoduleProjectDebuggingTest {

  private static final String PROJECT = "java-multimodule";
  private static final String PATH_TO_APP_CLASS =
      PROJECT + "/app/src/main/java/multimodule/App.java";
  private static final String PATH_TO_BOOK_IMPL_CLASS =
      PROJECT + "/model/src/main/java/multimodule/model/BookImpl.java";
  private static final String PATH_TO_BOOK_INTERFACE =
      PROJECT + "/model/src/main/java/multimodule/model/Book.java";

  private static final String BUILD_AND_DEBUG_CONSOLE_APPLICATION_COMMAND =
      "mvn -f ${current.project.path} clean install && java -Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=y -classpath ${current.project.path}/app/target/classes/:${current.project.path}/model/target/classes multimodule.App";

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
    URL resource = getClass().getResource("/projects/plugins/DebuggerPlugin/java-multimodule");
    testProjectServiceClient.importProject(
        ws.getId(), Paths.get(resource.toURI()), PROJECT, ProjectTemplates.CONSOLE_JAVA_SIMPLE);

    testCommandServiceClient.createCommand(
        BUILD_AND_DEBUG_CONSOLE_APPLICATION_COMMAND,
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

    // open project tree
    projectExplorer.quickExpandWithJavaScript();
  }

  @BeforeMethod
  public void startDebug() {
    // goto root item in the Project Explorer to have proper value of ${current.project.path} when
    // executing maven command.
    projectExplorer.waitVisibleItem(PROJECT);
    projectExplorer.waitAndSelectItem(PROJECT);

    // start application in debug mode
    commandsPalette.openCommandPalette();
    commandsPalette.startCommandByDoubleClick(BUILD_AND_DEBUG_COMMAND_NAME);
    consoles.waitExpectedTextIntoConsole(TestBuildConstants.LISTENING_AT_ADDRESS_8000);
  }

  @AfterMethod
  public void stopDebug() {
    debugPanel.removeAllBreakpoints();
    menu.runCommand(
        TestMenuCommandsConstants.Run.RUN_MENU, TestMenuCommandsConstants.Run.END_DEBUG_SESSION);
  }

  @Test
  public void shouldGoIntoConstructor() {
    // when
    projectExplorer.openItemByPath(PATH_TO_APP_CLASS);
    editor.setInactiveBreakpoint(20);
    menu.runCommandByXpath(
        TestMenuCommandsConstants.Run.RUN_MENU,
        TestMenuCommandsConstants.Run.DEBUG,
        getXpathForDebugConfigurationMenuItem());
    notificationPopup.waitExpectedMessageOnProgressPanelAndClose("Remote debugger connected");
    editor.waitActiveBreakpoint(20);
    debugPanel.clickOnButton(DebugPanel.DebuggerActionButtons.STEP_INTO);

    // then
    try {
      editor.waitTabFileWithSavedStatus("ClassLoader");
      debugPanel.clickOnButton(DebugPanel.DebuggerActionButtons.STEP_OUT);
      debugPanel.clickOnButton(DebugPanel.DebuggerActionButtons.STEP_OUT);
      debugPanel.clickOnButton(DebugPanel.DebuggerActionButtons.STEP_INTO);
    } catch (Exception e) {
    }

    editor.waitTabFileWithSavedStatus("BookImpl");
    debugPanel.clickOnButton(DebugPanel.DebuggerActionButtons.STEP_OVER);
    debugPanel.waitDebugHighlightedText("this.title = title;");
    debugPanel.waitTextInVariablesPanel("title=\"java\"");
    debugPanel.waitTextInVariablesPanel("author=\"oracle\"");
  }

  @Test
  public void shouldStopInsideConstructor() {
    // when
    projectExplorer.openItemByPath(PATH_TO_BOOK_IMPL_CLASS);
    editor.setInactiveBreakpoint(19);

    menu.runCommandByXpath(
        TestMenuCommandsConstants.Run.RUN_MENU,
        TestMenuCommandsConstants.Run.DEBUG,
        getXpathForDebugConfigurationMenuItem());
    // then
    notificationPopup.waitExpectedMessageOnProgressPanelAndClose("Remote debugger connected");
    editor.waitActiveBreakpoint(19);
    debugPanel.waitTextInVariablesPanel("title=\"java\"");
    debugPanel.waitTextInVariablesPanel("author=\"oracle\"");
  }

  @Test
  public void shouldDebugInstanceMethod() {
    // when
    projectExplorer.openItemByPath(PATH_TO_BOOK_IMPL_CLASS);
    editor.setInactiveBreakpoint(24);

    menu.runCommandByXpath(
        TestMenuCommandsConstants.Run.RUN_MENU,
        TestMenuCommandsConstants.Run.DEBUG,
        getXpathForDebugConfigurationMenuItem());
    // then
    notificationPopup.waitExpectedMessageOnProgressPanelAndClose("Remote debugger connected");
    editor.waitActiveBreakpoint(24);
    debugPanel.waitTextInVariablesPanel("author=\"google\"");
    debugPanel.waitTextInVariablesPanel("title=\"go\"");
  }

  @Test
  public void shouldDebugStaticMethod() {
    // when
    projectExplorer.openItemByPath(PATH_TO_BOOK_IMPL_CLASS);
    editor.setCursorToLine(42);
    editor.setInactiveBreakpoint(42);

    menu.runCommandByXpath(
        TestMenuCommandsConstants.Run.RUN_MENU,
        TestMenuCommandsConstants.Run.DEBUG,
        getXpathForDebugConfigurationMenuItem());
    // then
    notificationPopup.waitExpectedMessageOnProgressPanelAndClose("Remote debugger connected");
    editor.waitActiveBreakpoint(42);
    debugPanel.waitTextInVariablesPanel("author=\"google\"");
    debugPanel.waitTextInVariablesPanel("title=\"go\"");
  }

  @Test
  public void shouldDebugDefaultMethod() {
    // when
    projectExplorer.openItemByPath(PATH_TO_BOOK_INTERFACE);
    editor.setInactiveBreakpoint(31);
    menu.runCommandByXpath(
        TestMenuCommandsConstants.Run.RUN_MENU,
        TestMenuCommandsConstants.Run.DEBUG,
        getXpathForDebugConfigurationMenuItem());
    // then
    notificationPopup.waitExpectedMessageOnProgressPanelAndClose("Remote debugger connected");
    editor.waitActiveBreakpoint(31);
    debugPanel.waitTextInVariablesPanel("o=instance of multimodule.model.BookImpl");
  }

  @Test
  public void shouldDebugStaticDefaultMethod() {
    // when
    projectExplorer.openItemByPath(PATH_TO_BOOK_INTERFACE);
    editor.setCursorToLine(44);
    editor.setInactiveBreakpoint(44);
    menu.runCommandByXpath(
        TestMenuCommandsConstants.Run.RUN_MENU,
        TestMenuCommandsConstants.Run.DEBUG,
        getXpathForDebugConfigurationMenuItem());
    // then
    notificationPopup.waitExpectedMessageOnProgressPanelAndClose("Remote debugger connected");
    editor.waitActiveBreakpoint(44);
    debugPanel.waitTextInVariablesPanel("book=instance of multimodule.model.BookImpl");
  }

  private String getXpathForDebugConfigurationMenuItem() {
    return String.format(
        "//*[@id=\"%1$s/%2$s\" or @id=\"topmenu/Run/Debug/Debug '%2$s'\"]",
        TestMenuCommandsConstants.Run.DEBUG, PROJECT);
  }
}
