/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.selenium.testrunner;

import com.google.inject.Inject;

import org.eclipse.che.api.workspace.server.DtoConverter;
import org.eclipse.che.selenium.core.client.TestCommandServiceClient;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.workspace.InjectTestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.core.workspace.WorkspaceTemplate;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.plugins.JavaTestRunnerPluginConsole;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URL;
import java.nio.file.Paths;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.EXPECTED_MESS_IN_CONSOLE_SEC;
import static org.testng.Assert.assertTrue;

/**
 * @author Dmytro Nochevnov
 */
public class JavaTestPluginJunit4Test {

    private static final String JUNIT4_PROJECT           = "junit4-tests";
    private static final String PATH_TO_JUNIT4_TEST_CLASS = JUNIT4_PROJECT + "/src/test/java/org/eclipse/che/examples/AppOneTest.java";
    private static final String PATH_TO_JUNIT4_TEST_SUITE = JUNIT4_PROJECT + "/src/test/java/org/eclipse/che/examples/Junit4TestSuite.java";

    public static final String APP_TEST_ONE_FAIL_OUTPUT_TEMPLATE = "java.lang.AssertionError\n" +
                                                                   " at org.junit.Assert.fail(Assert.java:86)\n" +
                                                                   " at org.junit.Assert.assertTrue(Assert.java:41)\n" +
                                                                   " at org.junit.Assert.assertFalse(Assert.java:64)\n" +
                                                                   " at org.junit.Assert.assertFalse(Assert.java:74)\n" +
                                                                   " at org.eclipse.che.examples.AppOneTest.shouldFailOfAppOne(AppOneTest.java:31)";

    public static final String APP_TEST_ANOTHER_FAIL_OUTPUT_TEMPLATE = "java.lang.AssertionError\n" +
                                                                       " at org.junit.Assert.fail(Assert.java:86)\n" +
                                                                       " at org.junit.Assert.assertTrue(Assert.java:41)\n" +
                                                                       " at org.junit.Assert.assertFalse(Assert.java:64)\n" +
                                                                       " at org.junit.Assert.assertFalse(Assert.java:74)\n" +
                                                                       " at org.eclipse.che.examples.AppAnotherTest.shouldFailOfAppAnother(AppAnotherTest.java:31)";
    @Inject
    private JavaTestRunnerPluginConsole pluginConsole;
    @Inject
    private ProjectExplorer             projectExplorer;
    @Inject
    private Loader                      loader;
    @Inject
    private NotificationsPopupPanel     notifications;
    @Inject
    private Menu                        menu;
    @Inject
    private TestCommandServiceClient    testCommandServiceClient;

    @InjectTestWorkspace(template = WorkspaceTemplate.CODENVY_UBUNTU_JDK8)
    private TestWorkspace              ws;
    @Inject
    private Ide                        ide;
    @Inject
    private DefaultTestUser            user;
    @Inject
    private TestProjectServiceClient   testProjectServiceClient;

    @BeforeClass
    public void prepareTestProject() throws Exception {
        //TODO this is temporary solution for avoid problem described in the issue: https://github.com/eclipse/che/issues/4683. After fix, this can be removed
        URL resource = getClass().getResource("/projects/plugins/JavaTestRunnerPlugin/junit4-tests");
        testCommandServiceClient.createCommand(DtoConverter.asDto(new CompileCommand()), ws.getId(), user.getAuthToken());
        testProjectServiceClient.importProject(ws.getId(), user.getAuthToken(), Paths.get(resource.toURI()), JUNIT4_PROJECT,
                                               ProjectTemplates.CONSOLE_JAVA_SIMPLE
        );

        ide.open(ws);
        loader.waitOnClosed();
        projectExplorer.waitItem(JUNIT4_PROJECT);
        notifications.waitProgressPopupPanelClose();
    }

    @Test
    public void shouldExecuteJUnit4TestClassSuccessfully() throws InterruptedException {
        // given
        projectExplorer.quickRevealToItemWithJavaScript(PATH_TO_JUNIT4_TEST_CLASS);
        projectExplorer.openItemByPath(PATH_TO_JUNIT4_TEST_CLASS);

        // when
        menu.runCommand(TestMenuCommandsConstants.Run.RUN_MENU, TestMenuCommandsConstants.Run.TEST, TestMenuCommandsConstants.Run.Test.JUNIT_CLASS);

        // then
        notifications.waitExpectedMessageOnProgressPanelAndClosed("Running Tests...");
        notifications.waitExpectedMessageOnProgressPanelAndClosed("Test runner executed successfully with test failures.\n" +
                                                                  "1 test(s) failed.", EXPECTED_MESS_IN_CONSOLE_SEC);

        pluginConsole.waitTitlePresents("Test Results (Framework: JUnit4x)");

        pluginConsole.waitTestResultsInTree("There are 1 test failures.", "AppOneTest", "shouldFailOfAppOne");
        String testErrorMessage = pluginConsole.getTestErrorMessage("shouldFailOfAppOne");
        assertTrue(testErrorMessage.startsWith(APP_TEST_ONE_FAIL_OUTPUT_TEMPLATE), "Actual message was: " + testErrorMessage);
    }

    @Test(priority = 1)
    public void shouldExecuteJUnit4TestProjectSuccessfully() throws InterruptedException {
        // given
        projectExplorer.quickRevealToItemWithJavaScript(PATH_TO_JUNIT4_TEST_SUITE);
        projectExplorer.openItemByPath(PATH_TO_JUNIT4_TEST_SUITE);

        // when
        menu.runCommand(TestMenuCommandsConstants.Run.RUN_MENU, TestMenuCommandsConstants.Run.TEST, TestMenuCommandsConstants.Run.Test.JUNIT_PROJECT);

        // then
        notifications.waitExpectedMessageOnProgressPanelAndClosed("Running Tests...");
        notifications.waitExpectedMessageOnProgressPanelAndClosed("Test runner executed successfully with test failures.\n" +
                                                                  "2 test(s) failed.", EXPECTED_MESS_IN_CONSOLE_SEC);

        pluginConsole.waitTitlePresents("Test Results (Framework: JUnit4x)");

        pluginConsole.waitTestResultsInTree("There are 2 test failures.", "AppOneTest", "shouldFailOfAppOne");
        String testErrorMessage = pluginConsole.getTestErrorMessage("shouldFailOfAppOne");
        assertTrue(testErrorMessage.startsWith(APP_TEST_ONE_FAIL_OUTPUT_TEMPLATE), "Actual message was: " + testErrorMessage);

        pluginConsole.waitTestResultsInTree("There are 2 test failures.", "AppOneTest", "shouldFailOfAppAnother");
        testErrorMessage = pluginConsole.getTestErrorMessage("shouldFailOfAppAnother");
        assertTrue(testErrorMessage.startsWith(APP_TEST_ANOTHER_FAIL_OUTPUT_TEMPLATE), "Actual message was: " + testErrorMessage);
    }

}
