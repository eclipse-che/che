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

import static java.nio.file.Paths.get;
import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.core.constant.TestCommandsConstants.CUSTOM;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Run.EDIT_DEBUG_CONFIGURATION;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Run.RUN_MENU;
import static org.eclipse.che.selenium.core.project.ProjectTemplates.PLAIN_JAVA;
import static org.eclipse.che.selenium.pageobject.debug.DebugPanel.DebuggerActionButtons.ADD_WATCH_EXPRESSION;
import static org.eclipse.che.selenium.pageobject.debug.DebugPanel.DebuggerActionButtons.BTN_DISCONNECT;
import static org.eclipse.che.selenium.pageobject.debug.DebugPanel.DebuggerActionButtons.CHANGE_DEBUG_TREE_NODE;
import static org.eclipse.che.selenium.pageobject.debug.DebugPanel.DebuggerActionButtons.REMOVE_WATCH_EXPRESSION;
import static org.eclipse.che.selenium.pageobject.debug.DebugPanel.DebuggerActionButtons.STEP_OVER;

import com.google.inject.Inject;
import java.net.URL;
import org.eclipse.che.selenium.core.client.TestCommandServiceClient;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestBuildConstants;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.debug.DebugPanel;
import org.eclipse.che.selenium.pageobject.debug.JavaDebugConfig;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsPalette;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test to cover debugger watch expression functionality
 *
 * @author Oleksandr Andriienko
 */
public class DebuggerWatchExpressionTest {
  private static final String PROJECT =
      generate(DebuggerWatchExpressionTest.class.getSimpleName(), 2);
  private static final String PROJECT_PATH = "/projects/plugins/DebuggerPlugin/watch-expression";
  private static final String PATH_TO_CLASS = "/src/Application.java";

  private static final String START_DEBUG = "startDebug";
  private static final String DEBUG_APP =
      "cd ${current.project.path}/src/ && javac -g Application.java &&"
          + " java -Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=y Application";

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;
  @Inject private TestProjectServiceClient prjServiceClient;
  @Inject private TestCommandServiceClient cmdClient;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private DebugPanel debugPanel;
  @Inject private CodenvyEditor editor;
  @Inject private CommandsPalette cmdPalette;
  @Inject private Consoles consoles;
  @Inject private Menu menu;
  @Inject private JavaDebugConfig debugConfig;

  @BeforeClass
  public void prepare() throws Exception {
    URL resource = getClass().getResource(PROJECT_PATH);
    prjServiceClient.importProject(ws.getId(), get(resource.toURI()), PROJECT, PLAIN_JAVA);

    cmdClient.createCommand(DEBUG_APP, START_DEBUG, CUSTOM, ws.getId());

    ide.open(ws);

    projectExplorer.waitItem(PROJECT);
    projectExplorer.quickExpandWithJavaScript();
    debugPanel.openDebugPanel();

    projectExplorer.openItemByPath(PROJECT + PATH_TO_CLASS);

    editor.waitActive();
    editor.setCursorToLine(18);
    editor.setBreakpoint(18);

    menu.runCommand(RUN_MENU, EDIT_DEBUG_CONFIGURATION);
    debugConfig.createConfig(PROJECT);

    cmdPalette.openCommandPalette();
    cmdPalette.startCommandByDoubleClick(START_DEBUG);

    consoles.waitExpectedTextIntoConsole(TestBuildConstants.LISTENING_AT_ADDRESS_8000);
    menu.runCommandByXpath(
        TestMenuCommandsConstants.Run.RUN_MENU,
        TestMenuCommandsConstants.Run.DEBUG,
        debugConfig.getXpathToІRunDebugCommand(PROJECT));
  }

  @Test(priority = 1)
  public void addWatchExpression() {
    editor.waitActiveBreakpoint(18);
    debugPanel.waitDebugHighlightedText("message.setLevel(\"WARN\");");

    debugPanel.clickOnButton(ADD_WATCH_EXPRESSION);
    debugPanel.waitAppearTextAreaForm();
    debugPanel.typeAndSaveTextAreaDialog("message.getContent()");
    debugPanel.waitDisappearTextAreaForm();

    debugPanel.waitTextInVariablesPanel("message.getContent()=\"Simple test message\"");
  }

  @Test(priority = 2)
  public void editWatchExpression() {
    debugPanel.selectNodeInDebuggerTree("message.getContent()=\"Simple test message\"");

    debugPanel.clickOnButton(CHANGE_DEBUG_TREE_NODE);
    debugPanel.waitAppearTextAreaForm();
    debugPanel.typeAndSaveTextAreaDialog("message.level");
    debugPanel.waitDisappearTextAreaForm();

    debugPanel.waitTextInVariablesPanel("message.level=\"INFO\"");
  }

  @Test(priority = 3)
  public void watchExpressionShouldBeReEvaluatedOnNextDebugStep() {
    debugPanel.waitTextInVariablesPanel("message.level=\"INFO\"");

    debugPanel.clickOnButton(STEP_OVER);

    debugPanel.waitTextInVariablesPanel("message.level=\"WARN\"");
  }

  @Test(priority = 4)
  public void debuggerSupportComplexArithmeticExpression() {
    debugPanel.clickOnButton(ADD_WATCH_EXPRESSION);
    debugPanel.waitAppearTextAreaForm();
    debugPanel.typeAndSaveTextAreaDialog("100.0 + 1.0/2.0");
    debugPanel.waitDisappearTextAreaForm();

    debugPanel.waitTextInVariablesPanel("100.0 + 1.0/2.0=100.5");
  }

  @Test(priority = 5)
  public void watchExpressionShouldStayAfterStopDebug() {
    debugPanel.clickOnButton(BTN_DISCONNECT);

    debugPanel.waitTextInVariablesPanel("message.level=");
    debugPanel.waitTextInVariablesPanel("100.0 + 1.0/2.0=");
  }

  @Test(priority = 6)
  public void removeWatchExpression() {
    debugPanel.selectNodeInDebuggerTree("message.level=");
    debugPanel.clickOnButton(REMOVE_WATCH_EXPRESSION);

    debugPanel.waitTextIsNotPresentInVariablesPanel("message.level=");
  }
}
