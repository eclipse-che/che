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
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
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
public class CreateNewNotJavaFilesTest {

  private static final String PROJECT_NAME = "FileCreation4";
  private static final String NEW_FILE_NAME = "file.txt";
  private static final String TEST_FILE_NAME = "fileTest";
  private static final String NEW_XML_FILE = "NewXml";
  private static final String NEW_LESS_FILE = "NewLess";
  private static final String NEW_CSS_FILE = "NewCSS";
  private static final String NEW_HTML_FILE = "NewHTML";
  private static final String NEW_JS_FILE = "NewJS";
  private static final String DEFAULT_TEXT_FOR_NEW_FILE_NAME = "";
  private static final String DEFAULT_TEXT_FOR_NEW_XML_FILE =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
  private static final String DEFAULT_TEXT_FOR_NEW_LESS_FILE = "@CHARSET \"UTF-8\"\n;";
  private static final String DEFAULT_TEXT_FOR_NEW_CSS_FILE = "@CHARSET \"UTF-8\";";
  private static final String DEFAULT_TEXT_FOR_NEW_JS_FILE = "";
  private static final String DEFAULT_TEXT_FOR_NEW_HTML_FILE =
      "<!DOCTYPE html>\n"
          + "<html>\n"
          + "<head>\n"
          + "    <title></title>\n"
          + "</head>\n"
          + "<body>\n"
          + "\n"
          + "</body>\n"
          + "</html>";
  private static final String PATH_TO_FILES =
      PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples";

  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private Menu menu;
  @Inject private AskForValueDialog askForValueDialog;
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
  public void createNewFileFromContextMenuTest() throws Exception {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    notificationsPopupPanel.waitProgressPopupPanelClose();

    // go to folder for creation files
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByVisibleNameInExplorer("AppController.java");
    editor.closeFileByNameWithSaving("AppController");
    loader.waitOnClosed();

    // create new file
    createNewFile(PATH_TO_FILES, NEW_FILE_NAME, TestMenuCommandsConstants.Project.New.FILE, "");
    checkDefaultTextInCodeMirrorEditorForFile(DEFAULT_TEXT_FOR_NEW_FILE_NAME, NEW_FILE_NAME);
    editor.closeFileByNameWithSaving(NEW_FILE_NAME);

    // set the focus on the file and create some file
    String pathToFile = PATH_TO_FILES + "/" + NEW_FILE_NAME;
    createNewFile(pathToFile, TEST_FILE_NAME, TestMenuCommandsConstants.Project.New.FILE, "");
    editor.setCursorToLine(1);
    editor.typeTextIntoEditor("Hello, World!");
    checkDefaultTextInCodeMirrorEditorForFile("Hello, World!", TEST_FILE_NAME);

    // create new xml file
    createNewFile(
        PATH_TO_FILES, NEW_XML_FILE, TestMenuCommandsConstants.Project.New.XML_FILE, ".xml");
    checkDefaultTextInCodeMirrorEditorForFile(DEFAULT_TEXT_FOR_NEW_XML_FILE, NEW_XML_FILE + ".xml");
    editor.closeFileByNameWithSaving(NEW_XML_FILE + ".xml");

    // create new less file
    createNewFile(
        PATH_TO_FILES, NEW_LESS_FILE, TestMenuCommandsConstants.Project.New.LESS_FILE, ".less");
    checkDefaultTextInCodeMirrorEditorForFile(
        DEFAULT_TEXT_FOR_NEW_LESS_FILE, NEW_LESS_FILE + ".less");
    editor.closeFileByNameWithSaving(NEW_LESS_FILE + ".less");

    // create new css file
    createNewFile(
        PATH_TO_FILES, NEW_CSS_FILE, TestMenuCommandsConstants.Project.New.CSS_FILE, ".css");
    checkDefaultTextInCodeMirrorEditorForFile(DEFAULT_TEXT_FOR_NEW_CSS_FILE, NEW_CSS_FILE + ".css");
    editor.closeFileByNameWithSaving(NEW_CSS_FILE + ".css");

    // create new html file
    createNewFile(
        PATH_TO_FILES, NEW_HTML_FILE, TestMenuCommandsConstants.Project.New.HTML_FILE, ".html");
    checkDefaultTextInCodeMirrorEditorForFile(
        DEFAULT_TEXT_FOR_NEW_HTML_FILE, NEW_HTML_FILE + ".html");
    editor.closeFileByNameWithSaving(NEW_HTML_FILE + ".html");

    // create new js file
    createNewFile(
        PATH_TO_FILES, NEW_JS_FILE, TestMenuCommandsConstants.Project.New.JAVASCRIPT_FILE, ".js");
    checkDefaultTextInCodeMirrorEditorForFile(DEFAULT_TEXT_FOR_NEW_JS_FILE, NEW_JS_FILE + ".js");
    editor.closeFileByNameWithSaving(NEW_JS_FILE + ".js");
  }

  private void createNewFile(String pathFromItem, String name, String type, String fileExt)
      throws Exception {

    projectExplorer.waitAndSelectItem(pathFromItem);
    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT, TestMenuCommandsConstants.Project.New.NEW, type);
    askForValueDialog.waitFormToOpen();
    askForValueDialog.typeAndWaitText(name);
    askForValueDialog.clickOkBtn();
    loader.waitOnClosed();
    projectExplorer.waitVisibilityByName(name + fileExt);
  }

  private void checkDefaultTextInCodeMirrorEditorForFile(String defaultText, String fileName)
      throws InterruptedException {
    editor.waitActive();
    editor.waitTabIsPresent(fileName);
    editor.waitTextIntoEditor(defaultText);
  }
}
