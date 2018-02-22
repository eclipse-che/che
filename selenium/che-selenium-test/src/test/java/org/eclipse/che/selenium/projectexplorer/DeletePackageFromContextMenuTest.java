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

import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuFirstLevelItems.DELETE;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
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
public class DeletePackageFromContextMenuTest {

  private static final String PROJECT_NAME = "DeletionPrj3";
  private static final String PATH_TO_WEB_INF = PROJECT_NAME + "/src/main/webapp/WEB-INF";
  private static final String PATH_TO_WEB_APP = PROJECT_NAME + "/src/main/webapp";
  private static final String DELETE_TEXT1 = "Delete folder \"WEB-INF\"?";
  private static final String DELETE_TEXT2 = "Delete folder \"webapp\"?";
  private static final String DELETE_TEXT3 = "Delete folder \"examples\"?";
  private static final String PATH_TO_PACKAGE1 = PROJECT_NAME + "/src/main/webapp/WEB-INF";
  private static final String PATH_TO_PACKAGE2 = PROJECT_NAME + "/src/main/webapp";
  private static final String PATH_TO_PACKAGE3 =
      PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples";
  private static final String PATH_FOR_EXPAND =
      PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples";

  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private AskDialog askDialog;
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
  }

  @Test
  public void deletePackageFromContextMenuTest() throws Exception {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
    notificationsPopupPanel.waitProgressPopupPanelClose();
    openItemByPath(PATH_FOR_EXPAND + "/AppController.java");
    openItemByPath(PATH_TO_WEB_APP + "/index.jsp");
    openItemByPath(PATH_TO_WEB_INF + "/spring-servlet.xml");
    openItemByPath(PATH_TO_WEB_INF + "/web.xml");

    // select package1 for deletion
    projectExplorer.waitAndSelectItem(PATH_TO_PACKAGE1);
    deletePackage(PATH_TO_PACKAGE1, DELETE_TEXT1);
    loader.waitOnClosed();
    // check that files from deleted package was closed in editor

    editor.waitTabIsNotPresent("spring-servlet.xml");
    editor.waitTabIsNotPresent("web.xml");

    // check that package disappeared in editor
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitDisappearItemByPath(PATH_TO_PACKAGE1);

    // select package2 for deletion
    projectExplorer.waitAndSelectItem(PATH_TO_PACKAGE2);
    deletePackage(PATH_TO_PACKAGE2, DELETE_TEXT2);
    loader.waitOnClosed();
    // check that files from deleted package was closed in editor
    editor.waitTabIsNotPresent("index.jsp");

    // check that package disappeared in editor
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitDisappearItemByPath(PATH_TO_PACKAGE2);

    // select package3 for deletion
    projectExplorer.waitItem(PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples");
    projectExplorer.waitAndSelectItem(PATH_TO_PACKAGE3);
    deletePackage(PATH_TO_PACKAGE3, DELETE_TEXT3);
    loader.waitOnClosed();
    // check that files from deleted package was closed in editor
    editor.waitTabIsNotPresent("AppController");

    // check that package disappeared in editor
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitDisappearItemByPath(PATH_TO_PACKAGE3);
  }

  /**
   * delete package for menu File
   *
   * @param expectedMessage warning message about delete package
   */
  private void deletePackage(String pathToPackage, String expectedMessage) {
    projectExplorer.openContextMenuByPathSelectedItem(pathToPackage);
    projectExplorer.clickOnNewContextMenuItem(DELETE);

    askDialog.waitFormToOpen();
    askDialog.containsText(expectedMessage);
    askDialog.clickOkBtn();
    askDialog.waitFormToClose();
  }

  private void openItemByPath(String path) {
    projectExplorer.openItemByPath(path);
    editor.waitActive();
    loader.waitOnClosed();
  }
}
