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
package org.eclipse.che.selenium.factory;

import com.google.inject.Inject;

import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.LoadingBehaviorPage;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.DashboardFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.CREATE_PROJECT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.WORKSPACE;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.Wizard.SamplesName.WEB_JAVA_SPRING;
import static org.eclipse.che.selenium.pageobject.dashboard.DashboardFactory.AddAction.OPEN_FILE;

/**
 * @author Andrey Chizhikov
 */
public class CheckOpenFileFeatureTest {
    private static final String PROJECT_NAME  = CheckOpenFileFeatureTest.class.getSimpleName();
    private static final String OPEN_FILE_URL = "/CheckOpenFileFeatureTest/pom.xml";

    private String factoryWsName;

    @Inject
    private ProjectExplorer     projectExplorer;
    @Inject
    private Dashboard           dashboard;
    @Inject
    private DashboardFactory    dashboardFactory;
    @Inject
    private Ide                        ide;
    @Inject
    private LoadingBehaviorPage        loadingBehaviorPage;
    @Inject
    private CodenvyEditor              editor;
    @Inject
    private Loader                     loader;
    @Inject
    private Wizard                     wizard;
    @Inject
    private Menu                       menu;
    @Inject
    private TestWorkspace              ws;
    @Inject
    private DefaultTestUser            user;
    @Inject
    private SeleniumWebDriver          seleniumWebDriver;
    @Inject
    private TestWorkspaceServiceClient workspaceServiceClient;

    @BeforeClass
    public void setUp() throws Exception {
        ide.open(ws);
    }

    @AfterClass
    public void tearDown() throws Exception {
        if (factoryWsName != null) {
            workspaceServiceClient.delete(factoryWsName, user.getName());
        }
    }

    @Test
    public void checkOpenFileFeatureTest() throws Exception {
        createProject(PROJECT_NAME);
        projectExplorer.waitItem(PROJECT_NAME);
        dashboard.open();
        dashboard.selectFactoriesOnDashbord();
        dashboardFactory.clickOnAddFactoryBtn();
        dashboardFactory.clickWorkspacesTabOnSelectSource();
        dashboardFactory.selectWorkspaceForCreation(ws.getName());
        dashboardFactory.clickOnCreateFactoryBtn();
        dashboardFactory.selectAction(OPEN_FILE);
        dashboardFactory.enterParamValue(OPEN_FILE_URL);
        dashboardFactory.clickAddOnAddAction();
        dashboard.waitNotificationIsOpen();
        dashboard.waitNotificationIsClosed();
        dashboardFactory.clickOnOpenFactory();
        String currentWin = ide.driver().getWindowHandle();
        seleniumWebDriver.switchToNoneCurrentWindow(currentWin);
        loadingBehaviorPage.waitWhileLoadPageIsClosed();
        seleniumWebDriver.switchFromDashboardIframeToIde();
        factoryWsName = seleniumWebDriver.getWorkspaceNameFromBrowserUrl();
        projectExplorer.waitItem(PROJECT_NAME);
        editor.waitTabIsPresent("web-java-spring", ELEMENT_TIMEOUT_SEC);
    }

    private void createProject(String projectName) {
        projectExplorer.waitProjectExplorer();
        loader.waitOnClosed();
        menu.runCommand(WORKSPACE, CREATE_PROJECT);
        wizard.waitCreateProjectWizardForm();
        wizard.typeProjectNameOnWizard(projectName);
        wizard.selectSample(WEB_JAVA_SPRING);
        wizard.clickCreateButton();
        loader.waitOnClosed();
        wizard.waitCloseProjectConfigForm();
        loader.waitOnClosed();
        projectExplorer.waitProjectExplorer();
        projectExplorer.waitItem(projectName);
        loader.waitOnClosed();
    }
}
