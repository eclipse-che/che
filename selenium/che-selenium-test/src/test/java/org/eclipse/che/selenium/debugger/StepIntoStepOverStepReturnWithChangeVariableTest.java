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

import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.COMMON_GOAL;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOADER_TIMEOUT_SEC;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestCommandServiceClient;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.constant.TestBuildConstants;
import org.eclipse.che.selenium.core.constant.TestCommandsConstants;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CheTerminal;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.debug.DebugPanel;
import org.eclipse.che.selenium.pageobject.debug.JavaDebugConfig;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsPalette;
import org.openqa.selenium.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class StepIntoStepOverStepReturnWithChangeVariableTest {
  private static final Logger LOG =
      LoggerFactory.getLogger(StepIntoStepOverStepReturnWithChangeVariableTest.class);
  private static final String PROJECT = NameGenerator.generate("project", 4);
  private static final String START_DEBUG = "startDebug";
  private static final String CLEAN_TOMCAT = "cleanTomcat";
  private static final String BUILD = "build";

  private DebuggerUtils debugUtils = new DebuggerUtils();

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;

  @Inject private ProjectExplorer projectExplorer;
  @Inject private Consoles consoles;
  @Inject private CodenvyEditor editor;
  @Inject private Menu menu;
  @Inject private DebugPanel debugPanel;
  @Inject private JavaDebugConfig debugConfig;
  @Inject private TestCommandServiceClient testCommandServiceClient;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private Loader loader;
  @Inject private CommandsPalette commandsPalette;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private CheTerminal machineTerminal;

  @BeforeClass
  public void prepare() throws Exception {
    testProjectServiceClient.importProject(
        ws.getId(),
        Paths.get(getClass().getResource("/projects/debugStepInto").toURI()),
        PROJECT,
        ProjectTemplates.MAVEN_SPRING);

    testCommandServiceClient.createCommand(
        "cp /projects/"
            + PROJECT
            + "/target/qa-spring-sample-1.0-SNAPSHOT.war /home/user/tomcat8/webapps/ROOT.war"
            + " && "
            + "/home/user/tomcat8/bin/catalina.sh jpda run",
        START_DEBUG,
        TestCommandsConstants.CUSTOM,
        ws.getId());

    testCommandServiceClient.createCommand(
        "mvn clean install -f /projects/" + PROJECT,
        BUILD,
        TestCommandsConstants.CUSTOM,
        ws.getId());

    testCommandServiceClient.createCommand(
        "/home/user/tomcat8/bin/shutdown.sh && rm -rf /home/user/tomcat8/webapps/*",
        CLEAN_TOMCAT,
        TestCommandsConstants.CUSTOM,
        ws.getId());

    ide.open(ws);
  }

  @AfterMethod
  public void shutDownTomCatAndCleanWebApp() {
    editor.closeAllTabs();
    debugPanel.stopDebuggerWithUiAndCleanUpTomcat(CLEAN_TOMCAT);
    projectExplorer.clickOnProjectExplorerTab();
  }

  @Test
  public void changeVariableTest() throws Exception {
    buildProjectAndOpenMainClass();
    commandsPalette.openCommandPalette();
    commandsPalette.startCommandByDoubleClick(START_DEBUG);
    consoles.waitExpectedTextIntoConsole(" Server startup in");
    editor.setCursorToLine(35);
    editor.setInactiveBreakpoint(35);
    menu.runCommand(
        TestMenuCommandsConstants.Run.RUN_MENU,
        TestMenuCommandsConstants.Run.EDIT_DEBUG_CONFIGURATION);
    debugConfig.createConfig(PROJECT);
    menu.runCommand(
        TestMenuCommandsConstants.Run.RUN_MENU,
        TestMenuCommandsConstants.Run.DEBUG,
        TestMenuCommandsConstants.Run.DEBUG + "/" + PROJECT);
    editor.waitActiveBreakpoint(35);
    String appUrl =
        workspaceServiceClient
                .getServerFromDevMachineBySymbolicName(ws.getId(), "tomcat8")
                .getUrl()
                .replace("tcp", "http")
            + "/spring/guess";
    String requestMess = "numGuess=6&submit=Ok";
    CompletableFuture<String> requestToApplication =
        debugUtils.gotoDebugAppAndSendRequest(
            appUrl, requestMess, APPLICATION_FORM_URLENCODED, 200);

    editor.waitActiveBreakpoint(35);
    debugPanel.waitDebugHighlightedText("result = \"Sorry, you failed. Try again later!\";");
    debugPanel.clickOnButton(DebugPanel.DebuggerActionButtons.STEP_OVER);
    debugPanel.waitDebugHighlightedText("AdditonalClass.check();");
    debugPanel.clickOnButton(DebugPanel.DebuggerActionButtons.STEP_INTO);
    editor.waitTabFileWithSavedStatus("AdditonalClass");
    debugPanel.waitDebugHighlightedText(" someStr.toLowerCase();");
    debugPanel.clickOnButton(DebugPanel.DebuggerActionButtons.STEP_OVER);
    debugPanel.waitDebugHighlightedText("Operation.valueOf(\"SUBTRACT\").toString();");
    debugPanel.waitTextInVariablesPanel("someStr=\"hello Cdenvy\"");
    debugPanel.clickOnButton(DebugPanel.DebuggerActionButtons.STEP_OUT);
    debugPanel.waitTextInVariablesPanel("secretNum=");
    debugPanel.selectNodeInDebuggerTree("numGuessByUser=\"6\"");
    debugPanel.clickOnButton(DebugPanel.DebuggerActionButtons.CHANGE_DEBUG_TREE_NODE);
    debugPanel.typeAndSaveTextAreaDialog("\"7\"");
    debugPanel.waitTextInVariablesPanel("numGuessByUser=\"7\"");
    debugPanel.clickOnButton(DebugPanel.DebuggerActionButtons.RESUME_BTN_ID);

    String applicationResponse = requestToApplication.get(LOADER_TIMEOUT_SEC, TimeUnit.SECONDS);
    // remove try-catch block after issue has been resolved
    try {
      assertTrue(
          applicationResponse.contains("Sorry, you failed. Try again later!"),
          "Actual application response content was: " + applicationResponse);
    } catch (AssertionError ex) {
      machineTerminal.logApplicationInfo(PROJECT, ws);
      if (applicationResponse != null && applicationResponse.contains("504 Gateway Time-out")) {
        fail("Known issue: https://github.com/eclipse/che/issues/9251", ex);
      } else {
        throw ex;
      }
    }
  }

  // @Test(priority = 1)
  public void shouldOpenDebuggingFile() {
    buildProjectAndOpenMainClass();
    commandsPalette.openCommandPalette();
    commandsPalette.startCommandByDoubleClick(START_DEBUG);
    consoles.waitExpectedTextIntoConsole(" Server startup in");
    editor.setInactiveBreakpoint(26);
    seleniumWebDriver
        .switchTo()
        .activeElement()
        .sendKeys(Keys.SHIFT.toString() + Keys.F9.toString());
    editor.waitActiveBreakpoint(26);
  }

  private void buildProjectAndOpenMainClass() {
    String absPathToClass = PROJECT + "/src/main/java/org/eclipse/qa/examples/AppController.java";
    projectExplorer.waitItem(PROJECT);
    loader.waitOnClosed();
    projectExplorer.waitAndSelectItem(PROJECT);
    projectExplorer.invokeCommandWithContextMenu(COMMON_GOAL, PROJECT, BUILD);
    consoles.waitExpectedTextIntoConsole(TestBuildConstants.BUILD_SUCCESS);
    projectExplorer.quickRevealToItemWithJavaScript(absPathToClass);
    projectExplorer.openItemByPath(absPathToClass);
    editor.waitActive();
  }
}
