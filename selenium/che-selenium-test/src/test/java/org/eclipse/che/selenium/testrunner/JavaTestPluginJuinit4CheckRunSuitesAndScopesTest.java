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

import com.google.inject.Inject;

import org.eclipse.che.api.workspace.server.DtoConverter;
import org.eclipse.che.selenium.core.client.TestCommandServiceClient;
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
import org.eclipse.che.selenium.pageobject.intelligent.CommandsPalette;
import org.eclipse.che.selenium.pageobject.plugins.JavaTestRunnerPluginConsole;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Paths;




import static org.testng.Assert.assertTrue;

public class JavaTestPluginJuinit4CheckRunSuitesAndScopesTest {
  private static final String JUNIT4_PROJECT = "junit4-tests-with-separeted-suites";

  private static final String PATH_TO_JUNIT4_TEST_CLASSES =
      JUNIT4_PROJECT + "/src/test/java/org/eclipse/che/tests/AppOneTest.java";
  private static final String PATH_TO_JUNIT4_TEST_SUITE =
      JUNIT4_PROJECT + "/src/tests/java/org/examples/suite/Junit4TestSuite.java";

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
    testCommandServiceClient.createCommand(DtoConverter.asDto(compileCommand), ws.getName());
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
    consoles.drugConsolesInDefinePosition(-100);
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

    String expectedResultAfterSecondLaunch =
        "Default Suite\n"
            + "org.eclipse.che.tests.AppOneTest\n"
            + "shouldBeIgnoredOfAppOne\n"
            + "shouldSuccessOfAppOne\n"
            + "shouldFailOfAppOne\n"
            + "org.eclipse.che.tests.AppAnotherTest\n"
            + "shouldFailOfAppAnother\n"
            + "shouldSuccessOfAppAnother";

    projectExplorer.quickRevealToItemWithJavaScript(PATH_TO_JUNIT4_TEST_CLASSES);
    projectExplorer.openItemByPath(PATH_TO_JUNIT4_TEST_CLASSES);
    projectExplorer.selectItem(JUNIT4_PROJECT);
    // when
    menu.runCommand(TestMenuCommandsConstants.Run.RUN_MENU, (TestMenuCommandsConstants.Run.TEST, (TestMenuCommandsConstants.Run.R);

    // then
    notifications.waitExpectedMessageOnProgressPanelAndClosed("Test runner executed successfully.");
    pluginConsole.waitFqnOfTesClassInResultTree("org.eclipse.che.tests.AppAnotherTest");
    assertTrue(pluginConsole.getTextFromResultTree().equals(expectedResultAfterFirstLaunch));
    pluginConsole.waitFqnOfTesClassInResultTree("org.eclipse.che.tests.AppOneTest");
    pluginConsole.waitMethodMarkedAsPassed("shouldSuccessOfAppOne");
    pluginConsole.waitMethodMarkedAsFailed("shouldFailOfAppOne");
    pluginConsole.waitMethodMarkedAsIgnored("shouldBeIgnoredOfAppOne");
    pluginConsole.selectItemInResultTree("shouldFailOfAppAnother");
    String testOutput = pluginConsole.getVisibleTextFromCommandConsole();
  }

  private void runCompileCommandByPallete(CompileCommand compileCommand) {
    commandsPalette.openCommandPalette();
    commandsPalette.startCommandByDoubleClick(compileCommand.getName());
    consoles.waitExpectedTextIntoConsole(TestConstants.BuildMessages.BUILD_SUCCESS);
  }
}
