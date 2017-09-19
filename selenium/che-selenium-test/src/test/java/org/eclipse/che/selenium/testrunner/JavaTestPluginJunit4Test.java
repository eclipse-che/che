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
package org.eclipse.che.selenium.testrunner;

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Run.RUN_MENU;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Run.TEST;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Run.Test.JUNIT_TEST;
import static org.eclipse.che.selenium.pageobject.plugins.JavaTestRunnerPluginConsole.JunitMethodsState.FAILED;
import static org.eclipse.che.selenium.pageobject.plugins.JavaTestRunnerPluginConsole.JunitMethodsState.IGNORED;
import static org.eclipse.che.selenium.pageobject.plugins.JavaTestRunnerPluginConsole.JunitMethodsState.PASSED;
import static org.testng.Assert.assertTrue;

import com.google.inject.Inject;
import java.nio.file.Paths;
import org.eclipse.che.api.workspace.server.DtoConverter;
import org.eclipse.che.selenium.core.client.TestCommandServiceClient;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestBuildConstants;
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
import org.eclipse.che.selenium.pageobject.intelligent.CommandsPalette;
import org.eclipse.che.selenium.pageobject.plugins.JavaTestRunnerPluginConsole;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Dmytro Nochevnov */
public class JavaTestPluginJunit4Test {

  private static final String JUNIT4_PROJECT = "junit4-tests";
  private static final String PATH_TO_JUNIT4_TEST_CLASS =
      JUNIT4_PROJECT + "/src/test/java/org/eclipse/che/examples/AppOneTest.java";
  private static final String PATH_TO_JUNIT4_ANOTHER_TEST =
      JUNIT4_PROJECT + "/src/test/java/org/eclipse/che/examples/AppAppAnotherTest.java";
  private static final String PATH_TO_JUNIT4_TEST_SUITE =
      JUNIT4_PROJECT + "/src/test/java/org/eclipse/che/examples/Junit4TestSuite.java";

  public static final String APP_TEST_ONE_FAIL_OUTPUT_TEMPLATE =
      "java.lang.AssertionError\n"
          + " at org.junit.Assert.fail(Assert.java:86)\n"
          + " at org.junit.Assert.assertTrue(Assert.java:41)\n"
          + " at org.junit.Assert.assertFalse(Assert.java:64)\n"
          + " at org.junit.Assert.assertFalse(Assert.java:74)\n"
          + " at org.eclipse.che.examples.AppOneTest.shouldFailOfAppOne(AppOneTest.java:33)";

  public static final String APP_TEST_ANOTHER_FAIL_OUTPUT_TEMPLATE =
      "java.lang.AssertionError\n"
          + " at org.junit.Assert.fail(Assert.java:86)\n"
          + " at org.junit.Assert.assertTrue(Assert.java:41)\n"
          + " at org.junit.Assert.assertFalse(Assert.java:64)\n"
          + " at org.junit.Assert.assertFalse(Assert.java:74)\n"
          + " at org.eclipse.che.examples.AppAnotherTest.shouldFailOfAppAnother(AppAnotherTest.java:33)";
  @Inject private JavaTestRunnerPluginConsole pluginConsole;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private NotificationsPopupPanel notifications;
  @Inject private Menu menu;

  @InjectTestWorkspace(template = WorkspaceTemplate.CODENVY_UBUNTU_JDK8)
  private TestWorkspace ws;

  @Inject private Ide ide;
  @Inject private Consoles consoles;
  @Inject private CodenvyEditor editor;
  @Inject private TestCommandServiceClient testCommandServiceClient;
  @Inject private CommandsPalette commandsPalette;
  @Inject private TestProjectServiceClient projectServiceClient;

  @BeforeClass
  public void prepareTestProject() throws Exception {
    CompileCommand compileCommand = new CompileCommand();
    testCommandServiceClient.createCommand(DtoConverter.asDto(compileCommand), ws.getId());
    projectServiceClient.importProject(
        ws.getId(),
        Paths.get(
            getClass()
                .getResource("projects/plugins/JavaTestRunnerPlugin/" + JUNIT4_PROJECT)
                .toURI()),
        JUNIT4_PROJECT,
        ProjectTemplates.CONSOLE_JAVA_SIMPLE);

    ide.open(ws);
    loader.waitOnClosed();
    projectExplorer.waitItem(JUNIT4_PROJECT);
    runCompileCommandByPallete(compileCommand);
    notifications.waitProgressPopupPanelClose();
  }

  private void runCompileCommandByPallete(CompileCommand compileCommand) {
    commandsPalette.openCommandPalette();
    commandsPalette.startCommandByDoubleClick(compileCommand.getName());
    consoles.waitExpectedTextIntoConsole(TestBuildConstants.BUILD_SUCCESS);
  }

  @Test
  public void shouldExecuteJUnit4TestClassWithDifferentStatuses() throws InterruptedException {
    // given
    projectExplorer.quickRevealToItemWithJavaScript(PATH_TO_JUNIT4_TEST_CLASS);
    projectExplorer.openItemByPath(PATH_TO_JUNIT4_TEST_CLASS);

    // when
    menu.runCommand(RUN_MENU, TEST, JUNIT_TEST);

    // then
    notifications.waitExpectedMessageOnProgressPanelAndClosed("Test runner executed successfully.");

    pluginConsole.waitFqnOfTesClassInResultTree("org.eclipse.che.examples.AppOneTest");
    pluginConsole.waitMethodMarkedAsPassed("shouldSuccessOfAppOne");
    pluginConsole.waitMethodMarkedAsFailed("shouldFailOfAppOne");
    pluginConsole.waitMethodMarkedAsIgnored("shouldBeIgnoredOfAppOne");
    Assert.assertTrue(pluginConsole.getAllMethodsMarkedDefinedStatus(PASSED).size() == 1);
    Assert.assertTrue(pluginConsole.getAllMethodsMarkedDefinedStatus(FAILED).size() == 1);
    Assert.assertTrue(pluginConsole.getAllMethodsMarkedDefinedStatus(IGNORED).size() == 1);
    String testErrorMessage = pluginConsole.getTestErrorMessage();
    assertTrue(
        testErrorMessage.startsWith(APP_TEST_ONE_FAIL_OUTPUT_TEMPLATE),
        "Actual message was: " + testErrorMessage);
  }

  @Test(priority = 1)
  public void shouldExecuteJUnit4MethodWithDifferentStatuses() throws InterruptedException {
    // given
    projectExplorer.quickRevealToItemWithJavaScript(PATH_TO_JUNIT4_ANOTHER_TEST);
    projectExplorer.openItemByPath(PATH_TO_JUNIT4_ANOTHER_TEST);
    editor.waitActiveEditor();

    //TODO should be removed after fix issue https://github.com/eclipse/che/issues/5875
    editor.selectTabByName("AppAnotherTest");

    editor.setCursorToDefinedLineAndChar(27, 17);

    // when
    menu.runCommand(RUN_MENU, TEST, JUNIT_TEST);
    notifications.waitExpectedMessageOnProgressPanelAndClosed("Test runner executed successfully.");

    // then
    pluginConsole.waitFqnOfTesClassInResultTree("org.eclipse.che.examples.AppAnotherTest");
    pluginConsole.waitMethodMarkedAsPassed("shouldSuccessOfAppAnother");
    Assert.assertTrue(pluginConsole.getAllMethodsMarkedDefinedStatus(PASSED).size() == 1);
    // then

    //TODO should be removed after fix issue https://github.com/eclipse/che/issues/5875
    editor.selectTabByName("AppAnotherTest");

    editor.setCursorToDefinedLineAndChar(32, 17);
    menu.runCommand(RUN_MENU, TEST, JUNIT_TEST);
    notifications.waitExpectedMessageOnProgressPanelAndClosed("Test runner executed successfully.");
    pluginConsole.waitMethodMarkedAsFailed("shouldFailOfAppAnother");
    Assert.assertTrue(pluginConsole.getAllMethodsMarkedDefinedStatus(FAILED).size() == 1);
    String testErrorMessage = pluginConsole.getTestErrorMessage();
    assertTrue(
        testErrorMessage.startsWith(APP_TEST_ANOTHER_FAIL_OUTPUT_TEMPLATE),
        "Actual message was: " + testErrorMessage);

    //TODO should be removed after fix issue https://github.com/eclipse/che/issues/5875
    editor.selectTabByName("AppAnotherTest");

    editor.setCursorToDefinedLineAndChar(38, 17);
    menu.runCommand(RUN_MENU, TEST, JUNIT_TEST);
    notifications.waitExpectedMessageOnProgressPanelAndClosed("Test runner executed successfully.");
    pluginConsole.waitMethodMarkedAsIgnored("shouldBeIgnoredOfAppAnother");
    Assert.assertTrue(pluginConsole.getAllMethodsMarkedDefinedStatus(IGNORED).size() == 1);
  }
}
