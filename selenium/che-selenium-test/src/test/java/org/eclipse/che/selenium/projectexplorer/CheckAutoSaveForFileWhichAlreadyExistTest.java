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
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Andrienko Alexander
 * @author Andrey Chizhikov
 */
public class CheckAutoSaveForFileWhichAlreadyExistTest {

  private static final String PROJECT_NAME =
      CheckAutoSaveForFileWhichAlreadyExistTest.class.getSimpleName();
  private static final String PATH_TO_JAVA =
      PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/AppController.java";
  private static final String PATH_TO_LESS =
      PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/LessFile.less";
  private static final String PATH_TO_SIMPLE_FILE =
      PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/another";
  private static final String PATH_TO_SQL =
      PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/sqlFile.sql";
  private static final String PATH_TO_CSS = PROJECT_NAME + "/src/main/webapp/WEB-INF/cssFile.css";
  private static final String PATH_TO_XML = PROJECT_NAME + "/src/main/webapp/WEB-INF/web.xml";
  private static final String PATH_TO_HTML =
      PROJECT_NAME + "/src/main/webapp/WEB-INF/htmlFile.html";
  private static final String PATH_TO_JSP = PROJECT_NAME + "/src/main/webapp/index.jsp";

  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
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
  public void saveDialogForChangedFiles() throws Exception {
    projectExplorer.waitProjectExplorer();
    // open .html file, get text from there and compare with expected text
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByPath(PATH_TO_JAVA);
    // change java file
    addChangingToFile("AppController", 36, "String ll = null;");
    loader.waitOnClosed();

    // change less file
    projectExplorer.openItemByPath(PATH_TO_LESS);
    addChangingToFile("LessFile.less", 12, "p {color: red}");
    loader.waitOnClosed();

    // change simple file
    projectExplorer.openItemByPath(PATH_TO_SIMPLE_FILE);
    addChangingToFile("another", 16, "simple text for simple file");
    loader.waitOnClosed();

    // change css file
    projectExplorer.openItemByPath(PATH_TO_CSS);
    addChangingToFile("cssFile.css", 16, "a {color: red}");
    loader.waitOnClosed();

    // change xml file
    projectExplorer.openItemByPath(PATH_TO_XML);
    addChangingToFile("web.xml", 29, "<myElement />");
    loader.waitOnClosed();

    // change html file
    projectExplorer.openItemByPath(PATH_TO_HTML);
    addChangingToFile("htmlFile.html", 20, "<input value='some text'>");
    loader.waitOnClosed();

    // change jsp
    projectExplorer.openItemByPath(PATH_TO_JSP);
    addChangingToFile("index.jsp", 15, "<p>some text</p>");
    loader.waitOnClosed();

    // change sql file
    projectExplorer.openItemByPath(PATH_TO_SQL);
    addChangingToFile("sqlFile.sql", 12, "use cardb;");
    loader.waitOnClosed();
  }

  private void addChangingToFile(String fileName, int line, String text) {
    editor.waitActive();
    editor.setCursorToLine(line);
    editor.typeTextIntoEditor("\n");
    editor.setCursorToLine(line);
    editor.typeTextIntoEditor(text);
    editor.waitTabFileWithSavedStatus(fileName);
    editor.clickOnCloseFileIcon(fileName);
  }
}
