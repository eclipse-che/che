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
import org.eclipse.che.commons.lang.NameGenerator;
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

/** @author mmusienko */
public class ClosingSeveralOpenFilesTest {
  private static final String PROJECT_NAME =
      NameGenerator.generate(ClosingSeveralOpenFilesTest.class.getSimpleName(), 4);
  private static final String PATH_FOR_EXPAND_FIRST_MODULE =
      PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples";
  private static final String PATH_FOR_EXPAND_SECOND_MODULE =
      PROJECT_NAME + "/src/main/webapp/WEB-INF";

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
  public void closingSeveralOpenFilesTest() throws Exception {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    notificationsPopupPanel.waitProgressPopupPanelClose();
    consoles.closeProcessesArea();
    projectExplorer.quickExpandWithJavaScript();
    // step 1 expand and build first module

    projectExplorer.openItemByPath(PATH_FOR_EXPAND_FIRST_MODULE + "/AppController.java");
    loader.waitOnClosed();
    loader.waitOnClosed();

    // step 2 expand and build second module
    projectExplorer.openItemByPath(PATH_FOR_EXPAND_SECOND_MODULE + "/cssFile.css");
    projectExplorer.openItemByPath(PROJECT_NAME + "/pom.xml");
    projectExplorer.openItemByPath(PROJECT_NAME + "/README.md");
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/webapp" + "/index.jsp");
    loader.waitOnClosed();
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    editor.closeAllTabsByContextMenu();
    editor.waitWhileAllFilesWillClosed();
  }
}
