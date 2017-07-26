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
package org.eclipse.che.selenium.intelligencecommand;

import com.google.inject.Inject;

import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsToolbar;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.MULTIPLE;


/**
 * @author Musienko Maxim
 */

public class CheckIntelligenceCommandFromToolbarTest {

    private static final String PROJECT_NAME =
            NameGenerator.generate(CheckIntelligenceCommandFromToolbarTest.class.getSimpleName(), 2);

    @Inject
    private TestWorkspace     testWorkspace;
    @Inject
    private DefaultTestUser   defaultTestUser;
    @Inject
    private Ide               ide;
    @Inject
    private ProjectExplorer   projectExplorer;
    @Inject
    private Consoles          consoles;
    @Inject
    private Menu              menu;
    @Inject
    private Wizard            wizard;
    @Inject
    private CommandsToolbar   commandsToolbar;
    @Inject
    private SeleniumWebDriver seleniumWebDriver;

    @BeforeClass
    public void setUp() throws Exception {
        ide.open(testWorkspace);
    }

    @Test
    public void launchClonedWepAppTest() throws Exception {
        String currentWindow = ide.driver().getWindowHandle();
        projectExplorer.waitProjectExplorer();
        menu.runCommand(TestMenuCommandsConstants.Workspace.WORKSPACE, TestMenuCommandsConstants.Workspace.CREATE_PROJECT);
        wizard.selectProjectAndCreate(Wizard.SamplesName.WEB_JAVA_SPRING, PROJECT_NAME);
        projectExplorer.waitItem(PROJECT_NAME);
        commandsToolbar.clickWithHoldAndLaunchCommandFromList(PROJECT_NAME + ": build and run");
        consoles.waitExpectedTextIntoConsole(" Server startup in");
        commandsToolbar.clickExecStopBtn();
        consoles.clickOnPreviewUrl();
        checkTestAppAndReturnToIde(currentWindow, "This site can’t be reached");
        commandsToolbar.clickExecRerunBtn();
        consoles.waitExpectedTextIntoConsole(" Server startup in");
        consoles.clickOnPreviewUrl();
        checkTestAppAndReturnToIde(currentWindow, "Enter your name:");
        Assert.assertTrue(commandsToolbar.getTimerValue().matches("\\d\\d:\\d\\d"));
        Assert.assertTrue(commandsToolbar.getNumOfProcessCounter().equals("#2"));
        commandsToolbar.clickOnPreviewCommandBtnAndSelectUrl("dev-machine:8080");
        checkTestAppAndReturnToIde(currentWindow, "Enter your name:");
        commandsToolbar.clickExecStopBtn();
        commandsToolbar.clickWithHoldAndLaunchDebuCmdFromList(PROJECT_NAME + ": debug");
        consoles.waitExpectedTextIntoConsole("Listening for transport dt_socket at address: 8000", 60);
        consoles.waitExpectedTextIntoConsole(" Server startup in", 30);
    }

    private void checkTestAppAndReturnToIde(String currentWindow, String expectedTextOnTestAppPage) {
        seleniumWebDriver.switchToNoneCurrentWindow(currentWindow);
        new WebDriverWait(seleniumWebDriver, MULTIPLE)
                .until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), expectedTextOnTestAppPage));
        seleniumWebDriver.close();
        seleniumWebDriver.switchTo().window(currentWindow);
    }
}
