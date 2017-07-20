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
import org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants;
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
public class JavaTestPluginTestNgTest {

    private static final String PROJECT           = "testng-tests";

    private static final String PATH_TO_TEST_CLASS   = PROJECT + "/src/test/java/org/eclipse/che/examples/AppOneTest.java";
    private static final String PATH_TO_MAIN_CLASS   = PROJECT + "/src/main/java/org/eclipse/che/examples/HelloWorld.java";
    private static final String PATH_TO_TEST_PACKAGE = PROJECT + "/src/test/java/org/eclipse/che/examples";

    public static final String APP_TEST_ONE_FAIL_OUTPUT_TEMPLATE = "java.lang.AssertionError: expected [false] but found [true]\n" +
                                                                   " at org.testng.Assert.fail(Assert.java:94)\n" +
                                                                   " at org.testng.Assert.failNotEquals(Assert.java:494)\n" +
                                                                   " at org.testng.Assert.assertFalse(Assert.java:63)\n" +
                                                                   " at org.testng.Assert.assertFalse(Assert.java:73)\n" +
                                                                   " at org.eclipse.che.examples.AppOneTest.shouldFailOfAppOne(AppOneTest.java:31)";

    public static final String APP_TEST_ANOTHER_FAIL_OUTPUT_TEMPLATE = "java.lang.AssertionError: expected [false] but found [true]\n" +
                                                                       " at org.testng.Assert.fail(Assert.java:94)\n" +
                                                                       " at org.testng.Assert.failNotEquals(Assert.java:494)\n" +
                                                                       " at org.testng.Assert.assertFalse(Assert.java:63)\n" +
                                                                       " at org.testng.Assert.assertFalse(Assert.java:73)\n" +
                                                                       " at org.eclipse.che.examples.AppAnotherTest.shouldFailOfAppAnother(AppAnotherTest.java:31)";

    @InjectTestWorkspace(template = WorkspaceTemplate.CODENVY_UBUNTU_JDK8)
    private TestWorkspace   ws;
    @Inject
    private Ide             ide;
    @Inject
    private DefaultTestUser user;

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
    @Inject
    private TestProjectServiceClient    testProjectServiceClient;

    @BeforeClass
    public void prepareTestProject() throws Exception {
        URL resource = getClass().getResource("/projects/plugins/JavaTestRunnerPlugin/testng-tests");
        //TODO this is temporary solution for avoid problem described in the issue: https://github.com/eclipse/che/issues/4683. After fix, this can be removed
        testCommandServiceClient.createCommand(DtoConverter.asDto(new CompileCommand()), ws.getId(), user.getAuthToken());
        testProjectServiceClient.importProject(ws.getId(), user.getAuthToken(), Paths.get(resource.toURI()), PROJECT,
                                               ProjectTemplates.CONSOLE_JAVA_SIMPLE);

        ide.open(ws);
        loader.waitOnClosed();
        projectExplorer.waitItem(PROJECT);
        notifications.waitProgressPopupPanelClose();
    }

    @Test
    public void shouldExecuteTestClassSuccessfully() throws InterruptedException {
        // given
        projectExplorer.quickRevealToItemWithJavaScript(PATH_TO_TEST_CLASS);
        projectExplorer.openContextMenuByPathSelectedItem(PATH_TO_TEST_CLASS);

        // when
        projectExplorer.clickOnItemInContextMenu(TestProjectExplorerContextMenuConstants.TEST);
        projectExplorer.clickOnItemInContextMenu(TestProjectExplorerContextMenuConstants.SubMenuTest.TEST_NG_CLASS);

        // then
        notifications.waitExpectedMessageOnProgressPanelAndClosed("Running Tests...");
        notifications.waitExpectedMessageOnProgressPanelAndClosed("Test runner executed successfully with test failures.\n" +
                                                                  "1 test(s) failed.", EXPECTED_MESS_IN_CONSOLE_SEC);

        pluginConsole.waitTitlePresents("Test Results (Framework: TestNG)");
        pluginConsole.waitTestResultsInTree("There are 1 test failures.", "AppOneTest", "shouldFailOfAppOne");

        String testErrorMessage = pluginConsole.getTestErrorMessage("shouldFailOfAppOne");
        assertTrue(testErrorMessage.startsWith(APP_TEST_ONE_FAIL_OUTPUT_TEMPLATE), "Actual message was: " + testErrorMessage);
    }

    @Test(priority = 1)
    public void shouldExecuteTestProjectSuccessfully() throws InterruptedException {
        // given
        projectExplorer.quickRevealToItemWithJavaScript(PATH_TO_TEST_CLASS);
        projectExplorer.openItemByPath(PATH_TO_TEST_CLASS);

        projectExplorer.quickRevealToItemWithJavaScript(PATH_TO_TEST_PACKAGE);

        // when
        menu.runCommand(TestMenuCommandsConstants.Run.RUN_MENU, TestMenuCommandsConstants.Run.TEST, TestMenuCommandsConstants.Run.Test.TEST_NG_PROJECT);

        // then
        notifications.waitExpectedMessageOnProgressPanelAndClosed("Running Tests...");
        notifications.waitExpectedMessageOnProgressPanelAndClosed("Test runner executed successfully with test failures.\n" +
                                                                  "2 test(s) failed.", EXPECTED_MESS_IN_CONSOLE_SEC);

        pluginConsole.waitTitlePresents("Test Results (Framework: TestNG)");

        pluginConsole.waitTestResultsInTree("There are 2 test failures.", "AppOneTest", "shouldFailOfAppOne");
        String testErrorMessage = pluginConsole.getTestErrorMessage("shouldFailOfAppOne");
        assertTrue(testErrorMessage.startsWith(APP_TEST_ONE_FAIL_OUTPUT_TEMPLATE), "Actual message was: " + testErrorMessage);

        pluginConsole.waitTestResultsInTree("There are 2 test failures.", "AppOneTest", "shouldFailOfAppAnother");
        testErrorMessage = pluginConsole.getTestErrorMessage("shouldFailOfAppAnother");
        assertTrue(testErrorMessage.startsWith(APP_TEST_ANOTHER_FAIL_OUTPUT_TEMPLATE), "Actual message was: " + testErrorMessage);
    }

    @Test(priority = 2)
    public void shouldExecuteTestXmlSuiteSuccessfully() throws InterruptedException {
        // given
        projectExplorer.quickRevealToItemWithJavaScript(PATH_TO_TEST_CLASS);
        projectExplorer.openItemByPath(PATH_TO_TEST_CLASS);

        projectExplorer.quickRevealToItemWithJavaScript(PATH_TO_MAIN_CLASS);
        projectExplorer.selectItem(PATH_TO_TEST_PACKAGE);

        // when
        menu.runCommand(TestMenuCommandsConstants.Run.RUN_MENU, TestMenuCommandsConstants.Run.TEST, TestMenuCommandsConstants.Run.Test.TEST_NG_XML_SUITE);

        // then
        notifications.waitExpectedMessageOnProgressPanelAndClosed("Running Tests...");
        notifications.waitExpectedMessageOnProgressPanelAndClosed("Test runner executed successfully with test failures.\n" +
                                                                  "2 test(s) failed.", EXPECTED_MESS_IN_CONSOLE_SEC);

        pluginConsole.waitTitlePresents("Test Results (Framework: TestNG)");
        pluginConsole.waitTestResultsInTree("There are 2 test failures.", "AppOneTest", "shouldFailOfAppOne");
        String testErrorMessage = pluginConsole.getTestErrorMessage("shouldFailOfAppOne");
        assertTrue(testErrorMessage.startsWith(APP_TEST_ONE_FAIL_OUTPUT_TEMPLATE), "Actual message was: " + testErrorMessage);

        pluginConsole.waitTestResultsInTree("There are 2 test failures.", "AppOneTest", "shouldFailOfAppAnother");
        testErrorMessage = pluginConsole.getTestErrorMessage("shouldFailOfAppAnother");
        assertTrue(testErrorMessage.startsWith(APP_TEST_ANOTHER_FAIL_OUTPUT_TEMPLATE), "Actual message was: " + testErrorMessage);
    }

}
