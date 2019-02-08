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

import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuFirstLevelItems.DELETE;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskDialog;
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
public class DeleteFilesFromContextMenuTest {

  private static final String PROJECT_NAME = DeleteFilesFromContextMenuTest.class.getSimpleName();
  private static final String ASK_PREFIX = "Delete file ";
  private static final String DELETE_TEXT_FOR_JSP = ASK_PREFIX + "\"index.jsp\"?";
  private static final String DELETE_TEXT_FOR_LESS = ASK_PREFIX + "\"LessFile.less\"?";
  private static final String DELETE_TEXT_FOR_CSS = ASK_PREFIX + "\"cssFile.css\"?";
  private static final String DELETE_TEXT_FOR_XML = ASK_PREFIX + "\"web.xml\"?";
  private static final String DELETE_TEXT_FOR_HTML = ASK_PREFIX + "\"htmlFile.html\"?";
  private static final String DELETE_TEXT_FOR_JS = ASK_PREFIX + "\"jsFile.js\"?";
  private static final String DELETE_TEXT_FOR_JAVA_CLASS = ASK_PREFIX + "\"AppController.java\"?";
  private static final String DELETE_TEXT_FOR_SIMPLE_FILE = ASK_PREFIX + "\"another\"?";
  private static final String DELETE_TEXT_FOR_SQL = ASK_PREFIX + "\"sqlFile.sql\"?";
  private static final String PATH_TO_WEB_APP = PROJECT_NAME + "/src/main/webapp";
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
  @Inject private Consoles consoles;
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
    consoles.waitJDTLSProjectResolveFinishedMessage(PROJECT_NAME);
  }

  @Test
  public void deleteFileTest() throws Exception {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    notificationsPopupPanel.waitProgressPopupPanelClose();
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByPath(PATH_FOR_EXPAND + "/AppController.java");
    editor.waitActive();
    projectExplorer.openItemByPath(PATH_TO_WEB_APP + "/index.jsp");
    editor.waitActive();
    loader.waitOnClosed();

    // delete java class file
    deleteFromContextMenu(DELETE_TEXT_FOR_JAVA_CLASS, PATH_TO_JAVA_CLASS);

    // delete jsp file
    deleteFromContextMenu(DELETE_TEXT_FOR_JSP, PATH_TO_JSP);

    // delete css file
    deleteFromContextMenu(DELETE_TEXT_FOR_CSS, PATH_TO_CSS);

    // delete xml file
    deleteFromContextMenu(DELETE_TEXT_FOR_XML, PATH_TO_XML);

    // delete less file
    deleteFromContextMenu(DELETE_TEXT_FOR_LESS, PATH_TO_LESS);

    // delete html file
    deleteFromContextMenu(DELETE_TEXT_FOR_HTML, PATH_TO_HTML);
    checkDeletion(PATH_TO_HTML);

    // delete js file
    deleteFromContextMenu(DELETE_TEXT_FOR_JS, PATH_TO_JAVASCRIPT);

    // delete simple file
    deleteFromContextMenu(DELETE_TEXT_FOR_SIMPLE_FILE, PATH_TO_SIMPLE_FILE);

    // delete sql file
    deleteFromContextMenu(DELETE_TEXT_FOR_SQL, PATH_TO_SQL);
  }

  private void deleteFromContextMenu(String deleteText, String pathToFile) {
    loader.waitOnClosed();
    projectExplorer.waitItem(pathToFile);
    projectExplorer.openContextMenuByPathSelectedItem(pathToFile);
    projectExplorer.clickOnNewContextMenuItem(DELETE);
    loader.waitOnClosed();
    askDialog.acceptDialogWithText(deleteText);
    checkDeletion(pathToFile);
  }

  private void checkDeletion(String pathToFile) {
    loader.waitOnClosed();
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitDisappearItemByPath(pathToFile);
  }
}
