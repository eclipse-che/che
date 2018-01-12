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
import org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
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
public class CreateNewJavaFilesFromContextMenuTest {

  private static final String PROJECT_NAME = "FileCreation5";
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
    projectExplorer.waitItem(PROJECT_NAME);
    notificationsPopupPanel.waitProgressPopupPanelClose();

    // go to folder for creation files
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByVisibleNameInExplorer("AppController.java");
    loader.waitOnClosed();

    // create new class
    createNewFile(NEW_CLASS, AskForValueDialog.JavaFiles.CLASS);
    checkDefaultTextInEditorForFile(DEFAULT_TEXT_FOR_NEW_CLASS);

    // create new interface
    createNewFile(NEW_INTERFACE, AskForValueDialog.JavaFiles.INTERFACE);
    checkDefaultTextInEditorForFile(DEFAULT_TEXT_FOR_NEW_INTERFACE);

    // create new enum
    createNewFile(NEW_ENUM, AskForValueDialog.JavaFiles.ENUM);
    checkDefaultTextInEditorForFile(DEFAULT_TEXT_FOR_NEW_ENUM);
  }

  private void createNewFile(String name, AskForValueDialog.JavaFiles item) {
    projectExplorer.selectItem(PATH_TO_FILES);

    // create new File from context menu
    projectExplorer.openContextMenuByPathSelectedItem(PATH_TO_FILES);
    projectExplorer.clickOnItemInContextMenu(TestProjectExplorerContextMenuConstants.NEW);
    projectExplorer.clickOnNewContextMenuItem(
        TestProjectExplorerContextMenuConstants.SubMenuNew.JAVA_CLASS);

    askForValueDialog.createJavaFileByNameAndType(name, item);

    projectExplorer.waitItemInVisibleArea(name + ".java");

    editor.waitActive();
    loader.waitOnClosed();
    editor.waitTabIsPresent(name);
  }

  private void checkDefaultTextInEditorForFile(String defaultText) {
    editor.waitActive();
    editor.waitTextIntoEditor(defaultText);
  }
}
