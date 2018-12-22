/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.projectexplorer;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Andrienko Alexander
 * @author Andrey Chizhikov
 */
public class CheckTreeStructureAfterCloseTest {

  private static final String PROJECT_NAME = "TestTree";
  private static final String PATH_FOR_EXPAND =
      PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples";
  private static final String PATH_TO_WEB_APP = PROJECT_NAME + "/src/main/webapp";
  private static final String PATH_TO_SRC_FOLDER = PROJECT_NAME + "/src";
  private static final String PATH_TO_POM_XML_FILE = PROJECT_NAME + "/pom.xml";

  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private Consoles consoles;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/default-spring-project");
    testProjectServiceClient.importProject(
        testWorkspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
    ide.open(testWorkspace);
    ide.waitOpenedWorkspaceIsReadyToUse();
    consoles.waitJDTLSProjectResolveFinishedMessage(PROJECT_NAME);
  }

  @Test
  public void openProjectWizard() throws Exception {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    notificationsPopupPanel.waitProgressPopupPanelClose();

    projectExplorer.quickExpandWithJavaScript();
    loader.waitOnClosed();
    projectExplorer.openItemByPath(PATH_FOR_EXPAND + "/AppController.java");
    editor.waitActive();
    projectExplorer.openItemByPath(PATH_TO_WEB_APP + "/index.jsp");
    editor.waitActive();
    projectExplorer.openItemByPath(PATH_TO_POM_XML_FILE);
    loader.waitOnClosed();
    editor.waitActive();

    // close project explorer
    projectExplorer.openItemByPath(PROJECT_NAME);
    // open project explorer
    projectExplorer.openItemByPath(PROJECT_NAME);

    // check tree structure project explorer
    projectExplorer.waitItem(PATH_TO_POM_XML_FILE);
    projectExplorer.waitItem(PATH_TO_SRC_FOLDER);
  }
}
