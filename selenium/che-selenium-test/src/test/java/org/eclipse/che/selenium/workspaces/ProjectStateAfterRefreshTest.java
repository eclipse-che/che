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
package org.eclipse.che.selenium.workspaces;

import org.eclipse.che.selenium.core.project.ProjectTemplates;
import com.google.inject.Inject;

import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestWorkspaceConstants;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.ToastLoader;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URL;
import java.nio.file.Paths;

/**
 * @author Andrey chizhikov
 */
public class ProjectStateAfterRefreshTest {
    private static final String PROJECT_NAME = ProjectStateAfterRefreshTest.class.getSimpleName();

    @Inject
    private TestWorkspace            workspace;
    @Inject
    private DefaultTestUser          defaultTestUser;
    @Inject
    private Ide                      ide;
    @Inject
    private ProjectExplorer          projectExplorer;
    @Inject
    private Consoles                 consoles;
    @Inject
    private ToastLoader              toastLoader;
    @Inject
    private Menu                     menu;
    @Inject
    private NotificationsPopupPanel  notificationsPanel;
    @Inject
    private CodenvyEditor            editor;
    @Inject
    private TestProjectServiceClient testProjectServiceClient;

    @BeforeClass
    public void setUp() throws Exception {
        URL resource = ProjectStateAfterRefreshTest.this.getClass().getResource("/projects/guess-project");
        testProjectServiceClient.importProject(workspace.getId(), defaultTestUser.getAuthToken(), Paths.get(resource.toURI()),
                                               PROJECT_NAME,
                                               ProjectTemplates.MAVEN_SPRING
        );
        ide.open(workspace);
    }

    @Test
    public void checkRestoreStateOfProjectTest() throws Exception {
        projectExplorer.waitProjectExplorer();
        projectExplorer.waitItem(PROJECT_NAME);
        notificationsPanel.waitProgressPopupPanelClose();

        projectExplorer.quickExpandWithJavaScript();
        projectExplorer.waitItem(PROJECT_NAME + "/pom.xml");
        projectExplorer.waitItem(PROJECT_NAME + "/src/main/webapp/WEB-INF");
        projectExplorer.waitItem(PROJECT_NAME + "/src/main/webapp/WEB-INF/jsp");
        projectExplorer.openItemByPath(PROJECT_NAME + "/pom.xml");
        editor.waitActiveEditor();
        ide.driver().navigate().refresh();
        projectExplorer.waitItem(PROJECT_NAME);
        projectExplorer.waitItem(PROJECT_NAME + "/pom.xml");
        projectExplorer.waitItem(PROJECT_NAME + "/src/main/webapp/WEB-INF");
        projectExplorer.waitItem(PROJECT_NAME + "/src/main/webapp/index.jsp");

        projectExplorer.quickExpandWithJavaScript();
        consoles.closeProcessesArea();
        projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/webapp/index.jsp");
        editor.waitActiveEditor();
        projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/AppController.java");
        editor.waitActiveEditor();
        projectExplorer.openItemByPath(PROJECT_NAME + "/pom.xml");
        editor.waitActiveEditor();
        projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/webapp/WEB-INF/jsp" + "/guess_num.jsp");
        editor.waitActiveEditor();
        projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/webapp/WEB-INF" + "/web.xml");
        editor.waitActiveEditor();
        checkFilesAreOpened();
        ide.driver().navigate().refresh();
        checkFilesAreOpened();


        editor.closeAllTabs();
        projectExplorer.quickExpandWithJavaScript();
        projectExplorer.waitItem(PROJECT_NAME + "/pom.xml");
        projectExplorer.waitItem(PROJECT_NAME + "/src/main/webapp/WEB-INF");
        projectExplorer.waitItem(PROJECT_NAME + "/src/main/webapp/WEB-INF/jsp");
        projectExplorer.openItemByPath(PROJECT_NAME + "/pom.xml");
        editor.waitActiveEditor();
        ide.driver().navigate().refresh();
        projectExplorer.waitItem(PROJECT_NAME);
        editor.waitTabIsPresent("qa-spring-sample");
        projectExplorer.waitItem(PROJECT_NAME + "/pom.xml");
        projectExplorer.waitItem(PROJECT_NAME + "/my-lib");
        projectExplorer.waitItem(PROJECT_NAME + "/my-webapp");
    }

    private void checkFilesAreOpened() {
        projectExplorer.waitItem(PROJECT_NAME + "/src/main/webapp/WEB-INF");
        projectExplorer.waitItem(PROJECT_NAME + "/src/main/webapp/WEB-INF/jsp");
        projectExplorer.waitItem(PROJECT_NAME + "/src/main/webapp/index.jsp");
        projectExplorer.waitItem(PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/AppController.java");
        editor.waitTabIsPresent("index.jsp");
        editor.waitTabIsPresent("AppController");
        editor.waitTabIsPresent("guess_num.jsp");
        editor.waitTabIsPresent("web.xml");
        editor.waitTabIsPresent("qa-spring-sample");
    }
}
