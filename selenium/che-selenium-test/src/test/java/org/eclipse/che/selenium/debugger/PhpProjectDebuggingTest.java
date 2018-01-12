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
package org.eclipse.che.selenium.debugger;

import com.google.inject.Inject;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.utils.WaitUtils;
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
import org.eclipse.che.selenium.pageobject.debug.DebugPanel;
import org.eclipse.che.selenium.pageobject.debug.PhpDebugConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Dmytro Nochevnov */
public class PhpProjectDebuggingTest {

  private static final Logger LOG = LoggerFactory.getLogger(PhpProjectDebuggingTest.class);
  private static final String PROJECT = "php-tests";
  private static final String PATH_TO_INDEX_PHP = PROJECT + "/index.php";
  private static final String PATH_TO_LIB_PHP = PROJECT + "/lib.php";

  private static final String DEBUG_PHP_SCRIPT_COMMAND_NAME = "debug php script";
  private static final String START_APACHE_COMMAND_NAME = "start apache";
  private static final String STOP_APACHE_COMMAND_NAME = "stop apache";
  private static final int NON_DEFAULT_DEBUG_PORT = 10140;
  private static final String START_DEBUG_PARAMETERS =
      "?start_debug=1&debug_host=localhost&debug_port=" + NON_DEFAULT_DEBUG_PORT;

  @InjectTestWorkspace(template = WorkspaceTemplate.ECLIPSE_PHP)
  private TestWorkspace ws;

  @Inject private Ide ide;

  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private DebugPanel debugPanel;
  @Inject private PhpDebugConfig debugConfig;
  @Inject private NotificationsPopupPanel notificationPopup;
  @Inject private Menu menu;
  @Inject private CodenvyEditor editor;
  @Inject private Consoles consoles;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setup() throws Exception {
    URL resource = getClass().getResource("/projects/plugins/DebuggerPlugin/php-tests");
    testProjectServiceClient.importProject(
        ws.getId(), Paths.get(resource.toURI()), PROJECT, ProjectTemplates.PHP);

    // open IDE
    ide.open(ws);
    loader.waitOnClosed();
    projectExplorer.waitItem(PROJECT);
    notificationPopup.waitProgressPopupPanelClose();

    // open project tree
    projectExplorer.quickExpandWithJavaScript();
  }

  @BeforeMethod
  public void startDebug() {
    // goto root item in the Project Explorer to have proper value of ${current.project.path} when
    // executing maven command.
    projectExplorer.selectItem(PROJECT);
  }

  @AfterMethod
  public void stopDebug() {
    debugPanel.removeAllBreakpoints();
    menu.runCommand(
        TestMenuCommandsConstants.Run.RUN_MENU, TestMenuCommandsConstants.Run.END_DEBUG_SESSION);
    projectExplorer.invokeCommandWithContextMenu(
        ProjectExplorer.CommandsGoal.COMMON, PROJECT, STOP_APACHE_COMMAND_NAME);

    // remove debug configuration
    menu.runCommand(
        TestMenuCommandsConstants.Run.RUN_MENU,
        TestMenuCommandsConstants.Run.EDIT_DEBUG_CONFIGURATION);
    debugConfig.removeConfig(PROJECT);
  }

  @Test(priority = 0)
  public void shouldDebugCliPhpScriptFromFirstLine() {
    // when
    menu.runCommand(
        TestMenuCommandsConstants.Run.RUN_MENU,
        TestMenuCommandsConstants.Run.EDIT_DEBUG_CONFIGURATION);
    debugConfig.createConfig(PROJECT);

    menu.runCommandByXpath(
        TestMenuCommandsConstants.Run.RUN_MENU,
        TestMenuCommandsConstants.Run.DEBUG,
        getXpathForDebugConfigurationMenuItem());

    notificationPopup.waitExpectedMessageOnProgressPanelAndClosed("Remote debugger connected");

    projectExplorer.openItemByPath(PATH_TO_LIB_PHP);
    editor.setBreakpoint(14);
    editor.closeAllTabs();

    projectExplorer.openItemByPath(PATH_TO_INDEX_PHP);
    projectExplorer.invokeCommandWithContextMenu(
        ProjectExplorer.CommandsGoal.COMMON, PROJECT, DEBUG_PHP_SCRIPT_COMMAND_NAME);

    debugPanel.openDebugPanel();

    // then
    debugPanel.waitDebugHighlightedText("<?php include 'lib.php';?>");
    debugPanel.waitTextInVariablesPanel("$_GET=array [0]");

    // when
    debugPanel.clickOnButton(DebugPanel.DebuggerActionButtons.RESUME_BTN_ID);

    // then
    editor.waitTabFileWithSavedStatus("lib.php");
    editor.waitActiveBreakpoint(14);
    debugPanel.waitDebugHighlightedText("return \"Hello, $name\"");
    debugPanel.waitTextInVariablesPanel("$name=\"man\"");

    // when
    debugPanel.clickOnButton(DebugPanel.DebuggerActionButtons.STEP_OUT);

    // then
    editor.waitTabFileWithSavedStatus("index.php");
    debugPanel.waitDebugHighlightedText("echo sayHello(\"man\");");
    debugPanel.waitTextInVariablesPanel("$_GET=array [0]");
  }

  @Test(priority = 1)
  public void shouldDebugWebPhpScriptFromNonDefaultPortAndNotFromFirstLine() throws IOException {
    // when
    menu.runCommand(
        TestMenuCommandsConstants.Run.RUN_MENU,
        TestMenuCommandsConstants.Run.EDIT_DEBUG_CONFIGURATION);
    debugConfig.createConfig(PROJECT, false, NON_DEFAULT_DEBUG_PORT);

    menu.runCommandByXpath(
        TestMenuCommandsConstants.Run.RUN_MENU,
        TestMenuCommandsConstants.Run.DEBUG,
        getXpathForDebugConfigurationMenuItem());

    notificationPopup.waitExpectedMessageOnProgressPanelAndClosed(
        String.format(
            "Remote debugger connected\nConnected to: Zend Debugger, port: %s.",
            NON_DEFAULT_DEBUG_PORT));

    projectExplorer.openItemByPath(PATH_TO_LIB_PHP);
    editor.setBreakpoint(14);
    editor.closeAllTabs();

    projectExplorer.openItemByPath(PATH_TO_INDEX_PHP);
    projectExplorer.invokeCommandWithContextMenu(
        ProjectExplorer.CommandsGoal.COMMON, PROJECT, START_APACHE_COMMAND_NAME);

    startWebPhpScriptInDebugMode();

    debugPanel.openDebugPanel();

    // then
    editor.waitTabFileWithSavedStatus("lib.php");
    editor.waitActiveBreakpoint(14);
    debugPanel.waitDebugHighlightedText("return \"Hello, $name\"");
    debugPanel.waitTextInVariablesPanel("$name=\"man\"");

    // when
    debugPanel.clickOnButton(DebugPanel.DebuggerActionButtons.STEP_OUT);

    // then
    editor.waitTabFileWithSavedStatus("index.php");
    debugPanel.waitDebugHighlightedText("echo sayHello(\"man\");");
    debugPanel.waitTextInVariablesPanel("$_GET=array [3]");
  }

  /**
   * Start Web PHP Application in debug mode by making HTTP GET request to this application
   * asynchronously on preview url displayed in console + start debug parameters
   */
  private void startWebPhpScriptInDebugMode() {
    final String previewUrl = consoles.getPreviewUrl() + START_DEBUG_PARAMETERS;

    // it needs when the test is running on the che6-ocp platform
    if (previewUrl.contains("route")) {
      WaitUtils.sleepQuietly(10);
    }

    new Thread(
            () -> {
              try {
                URL connectionUrl = new URL(previewUrl);
                HttpURLConnection connection = (HttpURLConnection) connectionUrl.openConnection();
                connection.setRequestMethod("GET");
                connection.getResponseCode();
              } catch (IOException e) {
                LOG.error(
                    String.format(
                        "There was a problem with connecting to PHP-application for debug on URL '%s'",
                        previewUrl),
                    e);
              }
            })
        .start();
  }

  private String getXpathForDebugConfigurationMenuItem() {
    return String.format(
        "//*[@id=\"%1$s/%2$s\" or @id=\"topmenu/Run/Debug/Debug '%2$s'\"]",
        TestMenuCommandsConstants.Run.DEBUG, PROJECT);
  }
}
