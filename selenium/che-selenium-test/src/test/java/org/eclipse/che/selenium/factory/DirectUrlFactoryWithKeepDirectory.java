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

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.factory.TestFactory;
import org.eclipse.che.selenium.core.factory.TestFactoryInitializer;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.UPDATING_PROJECT_TIMEOUT_SEC;
import static org.testng.Assert.assertTrue;

public class DirectUrlFactoryWithKeepDirectory {

    @Inject
    @Named("github.username")
    private String                     gitHubUsername;
    @Inject
    private ProjectExplorer            projectExplorer;
    @Inject
    private TestFactoryInitializer     testFactoryInitializer;
    @Inject
    private NotificationsPopupPanel    notificationsPopupPanel;
    @Inject
    private Events                     events;
    @Inject
    private SeleniumWebDriver          seleniumWebDriver;
    @Inject
    private TestWorkspaceServiceClient workspaceServiceClient;
    @Inject
    private TestProjectServiceClient   projectServiceClient;
    @Inject
    private DefaultTestUser            testUser;

    private TestFactory testFactoryWithKeepDir;

    @BeforeClass
    public void setUp() throws Exception {
        testFactoryWithKeepDir = testFactoryInitializer.fromUrl("https://github.com/" + gitHubUsername + "/gitPullTest/tree/master/my-lib");
    }

    @AfterClass
    public void tearDown() throws Exception {
        testFactoryWithKeepDir.delete();
    }


    @Test
    public void factoryWithDirectUrlWithKeepDirectory() throws Exception {
        testFactoryWithKeepDir.authenticateAndOpen(seleniumWebDriver);
        seleniumWebDriver.switchFromDashboardIframeToIde();
        projectExplorer.waitProjectExplorer();
        notificationsPopupPanel.waitProgressPopupPanelClose();
        events.clickProjectEventsTab();
        events.waitExpectedMessage("Project gitPullTest imported", UPDATING_PROJECT_TIMEOUT_SEC);
        projectExplorer.expandPathInProjectExplorer("gitPullTest/my-lib");
        projectExplorer.waitItem("gitPullTest/my-lib/pom.xml");


        String wsId = workspaceServiceClient.getByName(seleniumWebDriver.getWorkspaceNameFromBrowserUrl(),
                                                       testUser.getName()
        )
                                            .getId();

        List<String> visibleItems = projectExplorer.getNamesOfAllOpenItems();
        assertTrue(visibleItems.containsAll(ImmutableList.of("gitPullTest", "my-lib", "src", "pom.xml")));

        String projectType = projectServiceClient.getFirstProject(wsId).getType();
        assertTrue(projectType.equals("blank"));
    }
}
