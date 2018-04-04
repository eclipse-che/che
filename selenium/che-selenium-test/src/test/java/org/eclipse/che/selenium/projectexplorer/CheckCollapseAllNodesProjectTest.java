/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * @author Aleksandr Shmaraev
 * @author Andrey Chizhikov
 */
public class CheckCollapseAllNodesProjectTest {

  private static final String PROJECT_NAME = "TestProjectCollapseAll";

  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private Consoles consoles;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/defaultSpringProjectWithDifferentTypeOfFiles");
    testProjectServiceClient.importProject(
        testWorkspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
    ide.open(testWorkspace);
  }

  @Test
  public void checkCollapseAllBtnProjectTree() throws Exception {
    // Preconditions
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    notificationsPopupPanel.waitProgressPopupPanelClose();
    consoles.closeProcessesArea();

    // Expand the nodes
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByVisibleNameInExplorer("AppController.java");
    loader.waitOnClosed();
    editor.waitTabIsPresent("AppController");
    projectExplorer.openItemByVisibleNameInExplorer("AppControlleTest.java");
    loader.waitOnClosed();
    editor.waitTabIsPresent("AppControlleTest");

    // Perform the 'collapse all'
    projectExplorer.collapseProjectTreeByOptionsButton();
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.waitDisappearItemByPath(PROJECT_NAME + "/src");
    projectExplorer.waitDisappearItemByPath(PROJECT_NAME + "/pom.xml");

    // Open the project and check the tree
    projectExplorer.openItemByPath(PROJECT_NAME);
    projectExplorer.waitItem(PROJECT_NAME + "/src");
    projectExplorer.openItemByPath(PROJECT_NAME + "/src");
    projectExplorer.waitItem(PROJECT_NAME + "/src/main");
    projectExplorer.waitItem(PROJECT_NAME + "/src/test");
    projectExplorer.waitDisappearItemByPath(
        PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/AppController.java");
    projectExplorer.waitDisappearItemByPath(
        PROJECT_NAME + "/src/test/java/org/eclipse/qa/examples/AppControlleTest.java");
    editor.waitTabIsPresent("AppController");
    editor.waitTabIsPresent("AppControlleTest");
  }
}
