/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
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
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.IdeMainDockPanel;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Andrienko Alexander
 * @author Andrey Chizhikov
 */
public class DeleteFilesPackagesAndProjectWithHelpDeleteIconTest {

  private static final String PROJECT_NAME = "DeletionPrj8";
  private static final String DELETE_TEXT1 = "Delete folder \"WEB-INF\"?";
  private static final String DELETE_TEXT2 = "Delete folder \"webapp\"?";
  private static final String DELETE_TEXT3 = "Delete folder \"examples\"?";
  private static final String DELETE_PROJECT = "Delete project \"DeletionPrj8\"?";
  private static final String PATH_TO_WEB_INF = PROJECT_NAME + "/src/main/webapp/WEB-INF";
  private static final String PATH_TO_WEB_APP = PROJECT_NAME + "/src/main/webapp";
  private static final String PATH_TO_PACKAGE3 =
      PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples";
  private static final String DELETE_TEXT_FOR_JSP = "Delete file \"index.jsp\"?";
  private static final String DELETE_TEXT_FOR_LESS = "Delete file \"LessFile.less\"?";
  private static final String DELETE_TEXT_FOR_CSS = "Delete file \"cssFile.css\"?";
  private static final String DELETE_TEXT_FOR_XML = "Delete file \"web.xml\"?";
  private static final String DELETE_TEXT_FOR_HTML = "Delete file \"htmlFile.html\"?";
  private static final String DELETE_TEXT_FOR_JS = "Delete file \"jsFile.js\"?";
  private static final String DELETE_TEXT_FOR_JAVA_CLASS = "Delete file \"AppController.java\"?";
  private static final String DELETE_TEXT_FOR_SIMPLE_FILE = "Delete file \"another\"?";
  private static final String DELETE_TEXT_FOR_SQL = "Delete file \"sqlFile.sql\"?";
  private static final String PATH_TO_JSP = PROJECT_NAME + "/src/main/webapp/index.jsp";
  private static final String PATH_TO_LESS =
      PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/LessFile.less";
  private static final String PATH_TO_CSS = PROJECT_NAME + "/src/main/webapp/WEB-INF/cssFile.css";
  private static final String PATH_TO_XML = PROJECT_NAME + "/src/main/webapp/WEB-INF/web.xml";
  private static final String PATH_TO_HTML =
      PROJECT_NAME + "/src/main/webapp/WEB-INF/htmlFile.html";
  private static final String PATH_TO_JAVASCRIPT =
      PROJECT_NAME + "/src/main/webapp/WEB-INF/jsFile.js";
  private static final String PATH_TO_JAVA_CLASS =
      PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/AppController.java";
  private static final String PATH_FOR_EXPAND =
      PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples";
  private static final String PATH_TO_SIMPLE_FILE =
      PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/another";
  private static final String PATH_TO_SQL =
      PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/sqlFile.sql";

  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private AskDialog askDialog;
  @Inject private IdeMainDockPanel ideMainDockPanel;
  @Inject private Menu menu;
  @Inject private TestProjectServiceClient testProjectServiceClient;

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
  public void deleteFileTest() throws Exception {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    notificationsPopupPanel.waitProgressPopupPanelClose();
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByPath(PATH_FOR_EXPAND + "/AppController.java");
    editor.waitActiveEditor();
    projectExplorer.openItemByPath(PATH_TO_WEB_APP + "/index.jsp");
    editor.waitActiveEditor();

    //delete jsp file
    deleteFromDeleteIcon(DELETE_TEXT_FOR_JSP, PATH_TO_JSP);

    //delete css file
    deleteFromDeleteIcon(DELETE_TEXT_FOR_CSS, PATH_TO_CSS);

    //delete xml file
    deleteFromDeleteIcon(DELETE_TEXT_FOR_XML, PATH_TO_XML);

    //delete less file
    deleteFromDeleteIcon(DELETE_TEXT_FOR_LESS, PATH_TO_LESS);

    //delete html file
    deleteFromDeleteIcon(DELETE_TEXT_FOR_HTML, PATH_TO_HTML);
    checkDeletion(PATH_TO_HTML);

    //delete js file
    deleteFromDeleteIcon(DELETE_TEXT_FOR_JS, PATH_TO_JAVASCRIPT);

    //delete java class file
    deleteFromDeleteIcon(DELETE_TEXT_FOR_JAVA_CLASS, PATH_TO_JAVA_CLASS);

    //delete simple file
    deleteFromDeleteIcon(DELETE_TEXT_FOR_SIMPLE_FILE, PATH_TO_SIMPLE_FILE);

    //delete sql file
    deleteFromDeleteIcon(DELETE_TEXT_FOR_SQL, PATH_TO_SQL);

    //select package1 for deletion
    projectExplorer.selectItem(PATH_TO_WEB_INF);
    deletePackage(DELETE_TEXT1);
    loader.waitOnClosed();
    //check that package disappeared
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitDisappearItemByPath(PATH_TO_WEB_INF);

    //select package2 for deletion
    projectExplorer.selectItem(PATH_TO_WEB_APP);
    deletePackage(DELETE_TEXT2);
    //check that package disappeared
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitDisappearItemByPath(PATH_TO_WEB_APP);

    //select package3 for deletion
    projectExplorer.selectItem(PATH_TO_PACKAGE3);
    deletePackage(DELETE_TEXT3);
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitDisappearItemByPath(PATH_TO_PACKAGE3);

    //delete project
    projectExplorer.selectItem(PROJECT_NAME);
    deletePackage(DELETE_PROJECT);
    //check that project doesn't exist
    loader.waitOnClosed();
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitDisappearItemByPath(PROJECT_NAME);
  }

  private void deleteFromDeleteIcon(String deleteText, String pathToFile) {
    loader.waitOnClosed();
    projectExplorer.selectItem(pathToFile);
    ideMainDockPanel.clickDeleteIcon();
    askDialog.acceptDialogWithText(deleteText);
    checkDeletion(pathToFile);
  }

  private void checkDeletion(String pathToFile) {
    loader.waitOnClosed();
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitDisappearItemByPath(pathToFile);
  }

  /**
   * delete package for menu File
   *
   * @param expectedMessage warning message about delete package
   */
  private void deletePackage(String expectedMessage) {
    menu.runAndWaitCommand(
        TestMenuCommandsConstants.Edit.EDIT, TestMenuCommandsConstants.Edit.DELETE);
    askDialog.waitFormToOpen();
    askDialog.containsText(expectedMessage);
    askDialog.clickOkBtn();
    askDialog.waitFormToClose();
  }
}
