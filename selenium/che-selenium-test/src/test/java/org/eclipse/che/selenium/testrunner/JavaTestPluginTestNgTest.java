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
package org.eclipse.che.selenium.testrunner;

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Run.RUN_MENU;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Run.TEST;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.TEST_NG_TEST_DROP_DAWN_ITEM;
import static org.eclipse.che.selenium.pageobject.plugins.JavaTestRunnerPluginConsole.JunitMethodsState.FAILED;
import static org.eclipse.che.selenium.pageobject.plugins.JavaTestRunnerPluginConsole.JunitMethodsState.PASSED;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.api.workspace.server.DtoConverter;
import org.eclipse.che.selenium.core.client.TestCommandServiceClient;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestBuildConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.user.TestUser;
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
import org.eclipse.che.selenium.pageobject.intelligent.CommandsPalette;
import org.eclipse.che.selenium.pageobject.plugins.JavaTestRunnerPluginConsole;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Dmytro Nochevnov */
public class JavaTestPluginTestNgTest {

  private static final String PROJECT = "testng-tests";

  private static final String PATH_TO_TEST_CLASS =
      PROJECT + "/src/test/java/org/eclipse/che/examples/AppOneTest.java";
  private static final String PATH_TO_ANOTHER_TEST_CLASS =
      PROJECT + "/src/test/java/org/eclipse/che/examples/AppAnotherTest.java";

  public static final String APP_TEST_ONE_FAIL_OUTPUT_TEMPLATE =
      "[TestNG] Running:  /home/user/che/ws-agent/temp/che-testng-suite.xmlexpected [false] but found [true]\n"
          + "java.lang.AssertionError: expected [false] but found [true]\n"
          + " at org.testng.Assert.fail(Assert.java:94)\n"
          + " at org.testng.Assert.failNotEquals(Assert.java:494)\n"
          + " at org.testng.Assert.assertFalse(Assert.java:63)\n"
          + " at org.testng.Assert.assertFalse(Assert.java:73)\n"
          + " at org.eclipse.che.examples.AppOneTest.shouldFailOfAppOne(AppOneTest.java:31)";

  public static final String APP_TEST_ANOTHER_FAIL_OUTPUT_TEMPLATE =
      "[TestNG] Running:  /home/user/che/ws-agent/temp/che-testng-suite.xmlexpected [false] but found [true]\n"
          + "java.lang.AssertionError: expected [false] but found [true]\n"
          + " at org.testng.Assert.fail(Assert.java:94)\n"
          + " at org.testng.Assert.failNotEquals(Assert.java:494)\n"
          + " at org.testng.Assert.assertFalse(Assert.java:63)\n"
          + " at org.testng.Assert.assertFalse(Assert.java:73)";

  public static final String END_OF_FAILED_TEST =
      "===============================================Default SuiteTotal tests run: 1, Failures: 1, Skips: 0===============================================";

  @InjectTestWorkspace(template = WorkspaceTemplate.UBUNTU_JDK8)
  private TestWorkspace ws;

  @Inject private Ide ide;
  @Inject private TestUser user;

  @Inject private JavaTestRunnerPluginConsole pluginConsole;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private NotificationsPopupPanel notifications;
  @Inject private Menu menu;
  @Inject private TestCommandServiceClient testCommandServiceClient;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private CommandsPalette commandsPalette;
  @Inject private Consoles consoles;
  @Inject private CodenvyEditor editor;

  @BeforeClass
  public void prepareTestProject() throws Exception {
    URL resource = getClass().getResource("/projects/plugins/JavaTestRunnerPlugin/testng-tests");

    CompileCommand compileCommand = new CompileCommand();
    testCommandServiceClient.createCommand(DtoConverter.asDto(compileCommand), ws.getId());
    testProjectServiceClient.importProject(
        ws.getId(), Paths.get(resource.toURI()), PROJECT, ProjectTemplates.CONSOLE_JAVA_SIMPLE);

    ide.open(ws);
    loader.waitOnClosed();
    projectExplorer.waitItem(PROJECT);
    notifications.waitProgressPopupPanelClose();
    runCompileCommandByPallete(compileCommand);
  }

  @Test
  public void shouldExecuteTestClassSuccessfully() {
    // given
    projectExplorer.quickRevealToItemWithJavaScript(PATH_TO_ANOTHER_TEST_CLASS);
    projectExplorer.openItemByPath(PATH_TO_TEST_CLASS);
    // when

    editor.waitActive();
    menu.runCommand(RUN_MENU, TEST, TEST_NG_TEST_DROP_DAWN_ITEM);

    // then
    notifications.waitExpectedMessageOnProgressPanelAndClosed("Test runner executed successfully.");
    pluginConsole.waitMethodMarkedAsPassed("shouldSuccessOfAppOne");
    pluginConsole.waitMethodMarkedAsFailed("shouldFailOfAppOne");
    assertTrue(pluginConsole.getAllNamesOfMethodsMarkedDefinedStatus(PASSED).size() == 1);
    assertTrue(pluginConsole.getAllNamesOfMethodsMarkedDefinedStatus(FAILED).size() == 1);
    String testErrorMessage = pluginConsole.getTestErrorMessage();
    assertTrue(
        testErrorMessage.startsWith(APP_TEST_ONE_FAIL_OUTPUT_TEMPLATE),
        "Actual message was: " + testErrorMessage);
  }

  @Test(priority = 1)
  public void shouldExecuteTestMethodsSuccessfully() {
    // given
    projectExplorer.openItemByPath(PATH_TO_ANOTHER_TEST_CLASS);

    // then
    editor.waitActive();
    editor.goToCursorPositionVisible(25, 17);
    menu.runCommand(RUN_MENU, TEST, TEST_NG_TEST_DROP_DAWN_ITEM);
    notifications.waitExpectedMessageOnProgressPanelAndClosed("Test runner executed successfully.");
    pluginConsole.waitMethodMarkedAsPassed("shouldSuccessOfAppAnother");
    assertTrue(pluginConsole.getAllNamesOfMethodsMarkedDefinedStatus(PASSED).size() == 1);
    editor.goToCursorPositionVisible(30, 17);
    menu.runCommand(RUN_MENU, TEST, TEST_NG_TEST_DROP_DAWN_ITEM);
    try {
      pluginConsole.waitMethodMarkedAsFailed("shouldFailOfAppAnother");
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/7338", ex);
    }
    assertTrue(pluginConsole.getAllNamesOfMethodsMarkedDefinedStatus(FAILED).size() == 1);
    String testErrorMessage = pluginConsole.getTestErrorMessage();
    assertTrue(
        testErrorMessage.startsWith(APP_TEST_ANOTHER_FAIL_OUTPUT_TEMPLATE),
        "Actual message was: " + testErrorMessage);
    assertTrue(
        testErrorMessage.endsWith(END_OF_FAILED_TEST), "Actual message was: " + testErrorMessage);
  }

  @Test(priority = 2)
  public void shouldExecuteAlltests() {
    // given
    projectExplorer.openItemByPath(PATH_TO_ANOTHER_TEST_CLASS);

    // then
    editor.waitActive();
    editor.goToCursorPositionVisible(25, 17);
    menu.runCommand(RUN_MENU, TEST, TEST_NG_TEST_DROP_DAWN_ITEM);
    notifications.waitExpectedMessageOnProgressPanelAndClosed("Test runner executed successfully.");
    try {
      pluginConsole.waitMethodMarkedAsPassed("shouldSuccessOfAppAnother");
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/7338", ex);
    }
    assertTrue(pluginConsole.getAllNamesOfMethodsMarkedDefinedStatus(PASSED).size() == 1);
    editor.goToCursorPositionVisible(30, 17);
    menu.runCommand(RUN_MENU, TEST, TEST_NG_TEST_DROP_DAWN_ITEM);
    pluginConsole.waitMethodMarkedAsFailed("shouldFailOfAppAnother");
    assertTrue(pluginConsole.getAllNamesOfMethodsMarkedDefinedStatus(FAILED).size() == 1);
    String testErrorMessage = pluginConsole.getTestErrorMessage();
    assertTrue(
        testErrorMessage.startsWith(APP_TEST_ANOTHER_FAIL_OUTPUT_TEMPLATE),
        "Actual message was: " + testErrorMessage);
    assertTrue(
        testErrorMessage.endsWith(END_OF_FAILED_TEST), "Actual message was: " + testErrorMessage);
  }

  private void runCompileCommandByPallete(CompileCommand compileCommand) {
    commandsPalette.openCommandPalette();
    commandsPalette.startCommandByDoubleClick(compileCommand.getName());
    consoles.waitExpectedTextIntoConsole(TestBuildConstants.BUILD_SUCCESS);
  }
}
