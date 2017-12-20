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
package org.eclipse.che.selenium.mavenplugin;

import static java.nio.file.Paths.get;
import static org.eclipse.che.selenium.core.project.ProjectTemplates.MAVEN_SPRING;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkersType.ERROR_MARKER;
import static org.eclipse.che.selenium.pageobject.ProjectExplorer.FolderTypes.PROJECT_FOLDER;
import static org.eclipse.che.selenium.pageobject.ProjectExplorer.FolderTypes.SIMPLE_FOLDER;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import java.net.URL;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestCommandServiceClient;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.MavenPluginStatusBar;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.git.Git;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class CheckMavenPluginTest {
  private static final String PROJECT_NAME = NameGenerator.generate("project", 6);

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private CodenvyEditor editor;
  @Inject private MavenPluginStatusBar mavenPluginStatusBar;
  @Inject private Consoles console;
  @Inject private Menu menu;
  @Inject private Loader loader;
  @Inject private AskForValueDialog askDialog;
  @Inject private Git git;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @Inject private TestCommandServiceClient commandServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/check-maven-plugin-test");
    testProjectServiceClient.importProject(
        workspace.getId(), get(resource.toURI()), PROJECT_NAME, MAVEN_SPRING);
    ide.open(workspace);
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
  }

  @Test
  public void shouldAccessClassCreatedInAnotherModule() {
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.selectItem(PROJECT_NAME + "/my-lib/src/main/java/hello");
    createNewFileFromMenuFile("TestClass", AskForValueDialog.JavaFiles.CLASS, ".java");
    projectExplorer.openItemByPath(
        PROJECT_NAME + "/my-webapp/src/main/java/che/eclipse/sample/Aclass.java");
    editor.waitActive();
    editor.setCursorToLine(14);
    enterClassNameViaAutocomplete();
    editor.typeTextIntoEditor(" testClass = new TestClass();");
    editor.waitAllMarkersDisappear(ERROR_MARKER);
  }

  @Test(priority = 1)
  public void shouldExcludeModules() {
    projectExplorer.openItemByPath(PROJECT_NAME + "/pom.xml");
    editor.waitActive();
    editor.goToCursorPositionVisible(25, 8);
    editor.typeTextIntoEditor("!--");
    editor.goToCursorPositionVisible(26, 32);
    editor.typeTextIntoEditor("--");
    try {
      projectExplorer.waitFolderDefinedTypeOfFolderByPath(PROJECT_NAME + "/my-lib", SIMPLE_FOLDER);
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/7109");
    }

    projectExplorer.waitFolderDefinedTypeOfFolderByPath(PROJECT_NAME + "/my-webapp", SIMPLE_FOLDER);
  }

  @Test(priority = 2)
  public void shouldAccessClassCreatedInAnotherModuleAfterIncludingModule() {
    includeModulesInTheParentPom();
    projectExplorer.openItemByPath(
        PROJECT_NAME + "/my-webapp/src/main/java/che/eclipse/sample/Aclass.java");
    editor.waitActive();
    editor.goToCursorPositionVisible(17, 1);
    enterClassNameViaAutocomplete();
    editor.typeTextIntoEditor(" testClass2 = new TestClass();");
    editor.waitAllMarkersDisappear(ERROR_MARKER);
  }

  private void includeModulesInTheParentPom() {
    editor.goToCursorPositionVisible(26, 32);
    editor.typeTextIntoEditor(Keys.DELETE.toString());
    editor.typeTextIntoEditor(Keys.DELETE.toString());
    editor.goToCursorPositionVisible(25, 8);
    editor.typeTextIntoEditor(Keys.DELETE.toString());
    editor.typeTextIntoEditor(Keys.DELETE.toString());
    editor.typeTextIntoEditor(Keys.DELETE.toString());
    projectExplorer.waitFolderDefinedTypeOfFolderByPath(PROJECT_NAME + "/my-lib", PROJECT_FOLDER);
    projectExplorer.waitFolderDefinedTypeOfFolderByPath(
        PROJECT_NAME + "/my-webapp", PROJECT_FOLDER);
    editor.closeAllTabs();
  }

  /** check ability just created class in autocomplete container */
  private void enterClassNameViaAutocomplete() {
    editor.typeTextIntoEditor("Test");
    editor.launchAutocomplete();
    try {
      editor.enterAutocompleteProposal("TestClass");
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/7109");
    }
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
    projectExplorer.waitItemInVisibleArea(name + fileExt);
  }
}
