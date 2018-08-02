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
public class CreateNewJavaFilesTest {

  private static final String PROJECT_NAME = "FileCreation6";
  private static final String NEW_CLASS = "NewClass";
  private static final String NEW_INTERFACE = "NewInterface";
  private static final String NEW_ENUM = "NewEnum";
  private static final String DEFAULT_TEXT_FOR_NEW_CLASS =
      "package org.eclipse.qa.examples;\n" + "\n" + "public class NewClass {\n" + "}\n";
  private static final String DEFAULT_TEXT_FOR_NEW_INTERFACE =
      "package org.eclipse.qa.examples;\n" + "\n" + "public interface NewInterface {\n" + "}\n";
  private static final String DEFAULT_TEXT_FOR_NEW_ENUM =
      "package org.eclipse.qa.examples;\n" + "\n" + "public enum NewEnum {\n" + "}\n";
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
  public void createNewJavaFilesTest() throws Exception {
    projectExplorer.waitItem(PROJECT_NAME);
    notificationsPopupPanel.waitProgressPopupPanelClose();
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.waitVisibilityByName("AppController.java");
    projectExplorer.openItemByVisibleNameInExplorer("AppController.java");
    loader.waitOnClosed();

    // create new class
    createNewFileFromMenuFile(NEW_CLASS, AskForValueDialog.JavaFiles.CLASS, ".java");

    checkDefaultTextInEditorForFile(DEFAULT_TEXT_FOR_NEW_CLASS, NEW_CLASS);

    // create new interface
    createNewFileFromMenuFile(NEW_INTERFACE, AskForValueDialog.JavaFiles.INTERFACE, ".java");

    checkDefaultTextInEditorForFile(DEFAULT_TEXT_FOR_NEW_INTERFACE, NEW_INTERFACE);

    // create new enum
    createNewFileFromMenuFile(NEW_ENUM, AskForValueDialog.JavaFiles.ENUM, ".java");

    checkDefaultTextInEditorForFile(DEFAULT_TEXT_FOR_NEW_ENUM, NEW_ENUM);
  }

  private void createNewFileFromMenuFile(
      String name, AskForValueDialog.JavaFiles item, String fileExt) {

    projectExplorer.waitAndSelectItem(PATH_TO_FILES);
    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT,
        TestMenuCommandsConstants.Project.New.NEW,
        TestMenuCommandsConstants.Project.New.JAVA_CLASS);

    loader.waitOnClosed();
    askForValueDialog.createJavaFileByNameAndType(name, item);
    loader.waitOnClosed();
    projectExplorer.waitVisibilityByName(name + fileExt);
  }

  private void checkDefaultTextInEditorForFile(String defaultText, String fileName) {
    editor.waitActive();
    editor.waitTabIsPresent(fileName);
    loader.waitOnClosed();
    editor.waitTextIntoEditor(defaultText);
  }
}
