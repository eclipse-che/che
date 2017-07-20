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

import org.eclipse.che.selenium.core.project.ProjectTemplates;
import com.google.inject.Inject;

import org.eclipse.che.api.workspace.server.DtoConverter;
import org.eclipse.che.selenium.core.client.TestCommandServiceClient;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.workspace.InjectTestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.core.workspace.WorkspaceTemplate;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.pageobject.plugins.JavaTestRunnerPluginConsole;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URL;
import java.nio.file.Paths;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.EXPECTED_MESS_IN_CONSOLE_SEC;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.SubMenuTest.JUNIT_CLASS;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.TEST;
import static org.testng.Assert.assertTrue;

/**
 * @author Dmytro Nochevnov
 */
public class JavaTestPluginJunit3Test {

    private static final String PROJECT          = "junit3-tests";

    private static final String PATH_TO_TEST_CLASS = PROJECT + "/src/test/java/org/eclipse/che/examples/AppOneTest.java";
    private static final String PATH_TO_TEST_SUITE = PROJECT + "/src/test/java/org/eclipse/che/examples/Junit3TestSuite.java";

    public static final String APP_TEST_ONE_FAIL_OUTPUT_TEMPLATE = "junit.framework.AssertionFailedError\n" +
                                                                   " at junit.framework.Assert.fail(Assert.java:47)\n" +
                                                                   " at junit.framework.Assert.assertTrue(Assert.java:20)\n" +
                                                                   " at junit.framework.Assert.assertFalse(Assert.java:34)\n" +
                                                                   " at junit.framework.Assert.assertFalse(Assert.java:41)\n" +
                                                                   " at org.eclipse.che.examples.AppOneTest.testAppOneShouldFail(AppOneTest.java:27)";

    public static final String APP_TEST_ANOTHER_FAIL_OUTPUT_TEMPLATE = "junit.framework.AssertionFailedError\n" +
                                                                       " at junit.framework.Assert.fail(Assert.java:47)\n" +
                                                                       " at junit.framework.Assert.assertTrue(Assert.java:20)\n" +
                                                                       " at junit.framework.Assert.assertFalse(Assert.java:34)\n" +
                                                                       " at junit.framework.Assert.assertFalse(Assert.java:41)\n" +
                                                                       " at org.eclipse.che.examples.AppAnotherTest.testAppAnotherShouldFail(AppAnotherTest.java:27)\n";

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
    private CodenvyEditor               editor;
    @Inject
    private TestCommandServiceClient    testCommandServiceClient;
    @Inject
    private TestProjectServiceClient    testProjectServiceClient;

    @BeforeClass
    public void prepareTestProject() throws Exception {
        //TODO this is temporary solution for avoid problem described in the issue: https://github.com/eclipse/che/issues/4683. After fix, this can be removed
        URL resource = getClass().getResource("/projects/plugins/JavaTestRunnerPlugin/junit3-tests");
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
        projectExplorer.clickOnItemInContextMenu(TEST);
        projectExplorer.clickOnItemInContextMenu(JUNIT_CLASS);

        // then
        notifications.waitExpectedMessageOnProgressPanelAndClosed("Running Tests...");
        notifications.waitExpectedMessageOnProgressPanelAndClosed("Test runner executed successfully with test failures.\n" +
                                                                  "1 test(s) failed.", EXPECTED_MESS_IN_CONSOLE_SEC);

        pluginConsole.waitTitlePresents("Test Results (Framework: JUnit3x)");

        pluginConsole.waitTestResultsInTree("There are 1 test failures.", "AppOneTest", "testAppOneShouldFail");
        String testErrorMessage = pluginConsole.getTestErrorMessage("testAppOneShouldFail");
        assertTrue(testErrorMessage.startsWith(APP_TEST_ONE_FAIL_OUTPUT_TEMPLATE));
    }

    @Test(priority = 1)
    public void shouldExecuteTestProjectSuccessfully() throws InterruptedException {
        // given
        projectExplorer.quickRevealToItemWithJavaScript(PATH_TO_TEST_SUITE);
        projectExplorer.openItemByPath(PATH_TO_TEST_SUITE);
        editor.waitActiveEditor();
        menu.runCommand(TestMenuCommandsConstants.Run.RUN_MENU, TestMenuCommandsConstants.Run.TEST, TestMenuCommandsConstants.Run.Test.JUNIT_PROJECT);

        // then
        notifications.waitExpectedMessageOnProgressPanelAndClosed("Running Tests...");
        notifications.waitExpectedMessageOnProgressPanelAndClosed("Test runner executed successfully with test failures.\n" +
                                                                  "2 test(s) failed.", EXPECTED_MESS_IN_CONSOLE_SEC);

        pluginConsole.waitTitlePresents("Test Results (Framework: JUnit3x)");

        pluginConsole.waitTestResultsInTree("There are 2 test failures.", "AppOneTest", "testAppOneShouldFail");
        String testErrorMessage = pluginConsole.getTestErrorMessage("testAppOneShouldFail");
        assertTrue(testErrorMessage.startsWith(APP_TEST_ONE_FAIL_OUTPUT_TEMPLATE), "Actual message was: " + testErrorMessage);

        pluginConsole.waitTestResultsInTree("There are 2 test failures.", "AppOneTest", "testAppAnotherShouldFail");
        testErrorMessage = pluginConsole.getTestErrorMessage("testAppAnotherShouldFail");
        assertTrue(testErrorMessage.startsWith(APP_TEST_ANOTHER_FAIL_OUTPUT_TEMPLATE));
    }


}
