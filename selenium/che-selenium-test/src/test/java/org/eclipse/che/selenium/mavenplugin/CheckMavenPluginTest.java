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
package org.eclipse.che.selenium.mavenplugin;

import static java.nio.file.Paths.get;
import static org.eclipse.che.selenium.core.project.ProjectTemplates.MAVEN_SPRING;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.ERROR;
import static org.eclipse.che.selenium.pageobject.ProjectExplorer.FolderTypes.PROJECT_FOLDER;

import com.google.inject.Inject;
import java.net.URL;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class CheckMavenPluginTest {
  private static final String PROJECT_NAME = NameGenerator.generate("project", 6);
  private static final String PATH_TO_EXTERNAL_LIBRARIES_IN_MODULE_1 =
      PROJECT_NAME + "/my-lib/External Libraries";
  private static final String PATH_TO_EXTERNAL_LIBRARIES_IN_MODULE_2 =
      PROJECT_NAME + "/my-webapp/External Libraries";

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private CodenvyEditor editor;
  @Inject private Menu menu;
  @Inject private Loader loader;
  @Inject private AskForValueDialog askDialog;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private Consoles consoles;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/check-maven-plugin-test");
    testProjectServiceClient.importProject(
        workspace.getId(), get(resource.toURI()), PROJECT_NAME, MAVEN_SPRING);
    ide.open(workspace);
    ide.waitOpenedWorkspaceIsReadyToUse();
    consoles.waitJDTLSProjectResolveFinishedMessage(PROJECT_NAME);
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
  }

  @Test
  public void shouldAccessClassCreatedInAnotherModule() {
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.waitAndSelectItem(PROJECT_NAME + "/my-lib/src/main/java/hello");
    createNewFileFromMenuFile("TestClass", AskForValueDialog.JavaFiles.CLASS, ".java");
    projectExplorer.openItemByPath(
        PROJECT_NAME + "/my-webapp/src/main/java/che/eclipse/sample/Aclass.java");
    editor.waitActive();
    editor.setCursorToLine(15);
    enterClassNameViaAutocomplete();
    editor.waitTextIntoEditor("import hello.TestClass;");
    editor.typeTextIntoEditor(" testClass = new TestClass();");
    editor.waitAllMarkersInvisibility(ERROR);
  }

  @Test(priority = 1)
  public void shouldExcludeModules() {
    projectExplorer.openItemByPath(PROJECT_NAME + "/pom.xml");
    editor.waitActive();
    editor.goToCursorPositionVisible(26, 8);
    editor.typeTextIntoEditor("!--");
    editor.goToCursorPositionVisible(27, 32);
    editor.typeTextIntoEditor("--");

    projectExplorer.waitItemInvisibility(PATH_TO_EXTERNAL_LIBRARIES_IN_MODULE_1);
    projectExplorer.waitItemInvisibility(PATH_TO_EXTERNAL_LIBRARIES_IN_MODULE_2);
  }

  @Test(priority = 2)
  public void shouldAccessClassCreatedInAnotherModuleAfterIncludingModule() {
    includeModulesInTheParentPom();
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByPath(
        PROJECT_NAME + "/my-webapp/src/main/java/che/eclipse/sample/Aclass.java");
    editor.waitActive();
    editor.goToCursorPositionVisible(18, 1);
    enterClassNameViaAutocomplete();
    editor.typeTextIntoEditor(" testClass2 = new TestClass();");
    editor.waitAllMarkersInvisibility(ERROR);
  }

  private void includeModulesInTheParentPom() {
    editor.goToCursorPositionVisible(27, 32);
    editor.typeTextIntoEditor(Keys.DELETE.toString());
    editor.typeTextIntoEditor(Keys.DELETE.toString());
    editor.goToCursorPositionVisible(26, 8);
    editor.typeTextIntoEditor(Keys.DELETE.toString());
    editor.typeTextIntoEditor(Keys.DELETE.toString());
    editor.typeTextIntoEditor(Keys.DELETE.toString());
    projectExplorer.waitDefinedTypeOfFolder(PROJECT_NAME + "/my-lib", PROJECT_FOLDER);
    projectExplorer.waitDefinedTypeOfFolder(PROJECT_NAME + "/my-webapp", PROJECT_FOLDER);
    editor.closeAllTabs();
  }

  /** check ability just created class in autocomplete container */
  private void enterClassNameViaAutocomplete() {
    editor.typeTextIntoEditor("Test");
    editor.launchAutocomplete();
  }

  /**
   * create file with UI
   *
   * @param name name of the file
   * @param item the type of item in the create file widget
   * @param fileExt the extension of file for checking in the Project explorer tree
   */
  private void createNewFileFromMenuFile(
      String name, AskForValueDialog.JavaFiles item, String fileExt) {
    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT,
        TestMenuCommandsConstants.Project.New.NEW,
        TestMenuCommandsConstants.Project.New.JAVA_CLASS);
    loader.waitOnClosed();
    askDialog.createJavaFileByNameAndType(name, item);
    loader.waitOnClosed();
    projectExplorer.waitVisibilityByName(name + fileExt);
  }
}
