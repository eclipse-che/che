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
package org.eclipse.che.selenium.dashboard;


import com.google.inject.Inject;

import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.constant.TestStacksConstants;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.dashboard.CreateWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.DashboardProject;
import org.eclipse.che.selenium.pageobject.dashboard.DashboardWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.NavigationBar;
import org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.ExecutionException;


/**
 * @author Andrey Chizhikov
 */
public class CreateAndDeleteProjectsTest {
    private final        String                    WORKSPACE            = NameGenerator.generate("CreateAndDeletePrj", 2);
    private static final DashboardProject.Template PROJECT_TEMPLATE_ONE = DashboardProject.Template.WEB_JAVA_SPRING;
    private static final DashboardProject.Template PROJECT_TEMPLATE_TWO = DashboardProject.Template.CONSOLE_JAVA_SIMPLE;

    @Inject
    private Ide                        ide;
    @Inject
    private Dashboard                  dashboard;
    @Inject
    private DashboardProject           dashboardProject;
    @Inject
    private DashboardWorkspace         dashboardWorkspace;
    @Inject
    private NavigationBar              navigationBar;
    @Inject
    private CreateWorkspace            createWorkspace;
    @Inject
    private ProjectSourcePage          projectSourcePage;
    @Inject
    private Loader                     loader;
    @Inject
    private ProjectExplorer            explorer;
    @Inject
    private SeleniumWebDriver          seleniumWebDriver;
    @Inject
    private TestWorkspaceServiceClient workspaceServiceClient;
    @Inject
    private DefaultTestUser            defaultTestUser;

    @BeforeClass
    public void setUp() {
        dashboard.open();
    }

    @AfterClass
    public void tearDown() throws Exception {
        workspaceServiceClient.delete(WORKSPACE, defaultTestUser.getName());
    }

    @Test
    public void createAndDeleteProjectTest() throws ExecutionException, InterruptedException {
        navigationBar.waitNavigationBar();
        navigationBar.clickOnMenu(NavigationBar.MenuItem.WORKSPACES);

        dashboardWorkspace.clickOnNewWorkspaceBtn();
        createWorkspace.waitToolbar();
        createWorkspace.selectStack(TestStacksConstants.JAVA.getId());
        createWorkspace.typeWorkspaceName(WORKSPACE);

        projectSourcePage.clickAddOrImportProjectButton();

        projectSourcePage.selectSample(PROJECT_TEMPLATE_ONE.value());
        projectSourcePage.selectSample(PROJECT_TEMPLATE_TWO.value());
        projectSourcePage.clickAdd();

        createWorkspace.clickCreate();

        String dashboardWindow = ide.driver().getWindowHandle();
        seleniumWebDriver.switchFromDashboardIframeToIde();
        loader.waitOnClosed();
        explorer.waitProjectExplorer();
        explorer.waitItem(PROJECT_TEMPLATE_TWO.value());
        switchToWindow(dashboardWindow);
        dashboard.selectWorkspacesItemOnDashboard();
        WaitUtils.sleepQuietly(10);

        dashboardWorkspace.selectWorkspaceItemName(WORKSPACE);
        dashboardWorkspace.selectTabInWorspaceMenu(DashboardWorkspace.TabNames.PROJECTS);
        dashboardProject.waitProjectIsPresent(PROJECT_TEMPLATE_ONE.value());
        dashboardProject.waitProjectIsPresent(PROJECT_TEMPLATE_TWO.value());
        dashboardProject.openSettingsForProjectByName(PROJECT_TEMPLATE_ONE.value());
        dashboardProject.clickOnDeleteProject();
        dashboardProject.clickOnDeleteItInDialogWindow();
        dashboardProject.waitProjectIsNotPresent(PROJECT_TEMPLATE_ONE.value());
        dashboardProject.openSettingsForProjectByName(PROJECT_TEMPLATE_TWO.value());
        dashboardProject.clickOnDeleteProject();
        dashboardProject.clickOnDeleteItInDialogWindow();
        dashboardProject.waitProjectIsNotPresent(PROJECT_TEMPLATE_TWO.value());
    }

    private void switchToWindow(String windowHandle) {
        ide.driver().switchTo().window(windowHandle);
    }
}
