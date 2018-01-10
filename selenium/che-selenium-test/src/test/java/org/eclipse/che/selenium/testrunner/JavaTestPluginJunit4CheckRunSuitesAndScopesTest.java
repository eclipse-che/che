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

import static org.eclipse.che.selenium.pageobject.plugins.JavaTestRunnerPluginConsole.JunitMethodsState.FAILED;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import java.nio.file.Paths;
import org.eclipse.che.api.workspace.server.DtoConverter;
import org.eclipse.che.selenium.core.client.TestCommandServiceClient;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestBuildConstants;
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
import org.eclipse.che.selenium.pageobject.intelligent.CommandsPalette;
import org.eclipse.che.selenium.pageobject.plugins.JavaTestRunnerPluginConsole;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class JavaTestPluginJunit4CheckRunSuitesAndScopesTest {
  private static final String JUNIT4_PROJECT = "junit4-tests-with-separeted-suites";

  private static final String PATH_TO_JUNIT4_TEST_CLASSES =
      JUNIT4_PROJECT + "/src/test/java/org/eclipse/che/tests/AppOneTest.java";

  private static final int VALUE_OF_SHIFTING_CONSOLES_ALONG_X_AXIS = -100;

  @Inject private JavaTestRunnerPluginConsole pluginConsole;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private NotificationsPopupPanel notifications;
  @Inject private Menu menu;

  @Inject private TestWorkspace ws;

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
                .getResource("/projects/plugins/JavaTestRunnerPlugin/" + JUNIT4_PROJECT)
                .toURI()),
        JUNIT4_PROJECT,
        ProjectTemplates.CONSOLE_JAVA_SIMPLE);
    ide.open(ws);
    loader.waitOnClosed();
    projectExplorer.waitItem(JUNIT4_PROJECT);
    runCompileCommandByPallete(compileCommand);
    notifications.waitProgressPopupPanelClose();
    consoles.dragConsolesInDefinePosition(VALUE_OF_SHIFTING_CONSOLES_ALONG_X_AXIS);
  }

  @Test
  public void shouldExecuteJUnit4TestClassWithDifferentStatuses() throws InterruptedException {
    // given
    String expectedResultAfterFirstLaunch =
        "Default Suite\n"
            + "org.eclipse.che.tests.AppOneTest\n"
            + "shouldBeIgnoredOfAppOne\n"
            + "shouldSuccessOfAppOne\n"
            + "shouldFailOfAppOne\n"
            + "org.eclipse.che.suite.Junit4TestSuite\n"
            + "org.eclipse.che.tests.AppAnotherTest\n"
            + "shouldFailOfAppAnother\n"
            + "shouldSuccessOfAppAnother\n"
            + "org.eclipse.che.tests.AppAnotherTest\n"
            + "shouldFailOfAppAnother\n"
            + "shouldSuccessOfAppAnother\n"
            + "org.eclipse.che.tests.AppAnotherTest\n"
            + "shouldFailOfAppAnother\n"
            + "shouldSuccessOfAppAnother";

    String expectedExceptionForFailedTest =
        "java.lang.AssertionError\n"
            + " at org.junit.Assert.fail(Assert.java:86)\n"
            + " at org.junit.Assert.assertTrue(Assert.java:41)";

    projectExplorer.quickRevealToItemWithJavaScript(PATH_TO_JUNIT4_TEST_CLASSES);
    projectExplorer.selectItem(JUNIT4_PROJECT);
    // when
    menu.runCommand(
        TestMenuCommandsConstants.Run.RUN_MENU,
        TestMenuCommandsConstants.Run.TEST,
        TestMenuCommandsConstants.JUNIT_TEST_DROP_DAWN_ITEM);

    // then
    notifications.waitExpectedMessageOnProgressPanelAndClosed("Test runner executed successfully.");
    pluginConsole.waitFqnOfTesClassInResultTree("org.eclipse.che.tests.AppAnotherTest");
    assertTrue(pluginConsole.getTextFromResultTree().equals(expectedResultAfterFirstLaunch));
    pluginConsole.waitFqnOfTesClassInResultTree("org.eclipse.che.tests.AppOneTest");
    pluginConsole.waitMethodMarkedAsPassed("shouldSuccessOfAppOne");
    pluginConsole.waitMethodMarkedAsFailed("shouldFailOfAppOne");
    pluginConsole.waitMethodMarkedAsIgnored("shouldBeIgnoredOfAppOne");
    try {
      pluginConsole.selectMethodWithDefinedStatus(FAILED, "shouldFailOfAppAnother");
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/7338", ex);
    }
    assertTrue(pluginConsole.getTestErrorMessage().startsWith(expectedExceptionForFailedTest));
  }

  private void runCompileCommandByPallete(CompileCommand compileCommand) {
    commandsPalette.openCommandPalette();
    commandsPalette.startCommandByDoubleClick(compileCommand.getName());
    consoles.waitExpectedTextIntoConsole(TestBuildConstants.BUILD_SUCCESS);
  }
}
