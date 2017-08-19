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

import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkersType.ERROR_MARKER;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestCommandServiceClient;
import org.eclipse.che.selenium.core.constant.TestCommandsConstants;
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
import org.eclipse.che.selenium.pageobject.Wizard;
import org.eclipse.che.selenium.pageobject.git.Git;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class CheckMavenPluginTest {
  private static final String PROJECT_NAME = NameGenerator.generate("project", 6);
  private static final String CHECKOUT_COMMAND = "checkout";

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

  @Inject
  @Named("github.username")
  private String gitHubUsername;

  @Inject private TestCommandServiceClient commandServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    commandServiceClient.createCommand(
        "cd /projects/" + PROJECT_NAME + " && git checkout contrib-12042015",
        CHECKOUT_COMMAND,
        TestCommandsConstants.CUSTOM,
        workspace.getId());

    ide.open(workspace);
    projectExplorer.waitProjectExplorer();
  }

  @Test
  public void mavenStatusBarShouldDisplayResolvingProjectMessage() {
    git.importJavaAppAndCheckMavenPluginBar(
        "https://github.com/" + gitHubUsername + "/pushChangesTest.git",
        PROJECT_NAME,
        Wizard.TypeProject.MAVEN,
        "Resolving project: SpringDemo");
    mavenPluginStatusBar.waitClosingInfoPanel(100);
    projectExplorer.waitItem(PROJECT_NAME);
  }

  @Test(priority = 1)
  public void shouldExecuteCommandAndWaitTextInConsole() throws Exception {
    projectExplorer.invokeCommandWithContextMenu(
        ProjectExplorer.CommandsGoal.COMMON, PROJECT_NAME, CHECKOUT_COMMAND);

    console.waitExpectedTextIntoConsole("Switched to a new branch 'contrib-12042015'");
  }

  @Test(priority = 2)
  public void shouldAccessClassCreatedInAnotherModule() {
    projectExplorer.expandPathInProjectExplorer(PROJECT_NAME + "/my-lib/src/main/java/hello");
    createNewFileFromMenuFile("TestClass", AskForValueDialog.JavaFiles.CLASS, ".java");

    projectExplorer.clickCollapseAllButton();
    projectExplorer.expandPathInProjectExplorerAndOpenFile(
        PROJECT_NAME + "/my-webapp/src/main/java/helloworld", "GreetingController.java");
    editor.waitActiveEditor();
    editor.setCursorToLine(24);
    enterClassNameViaAutocomplete();
    editor.typeTextIntoEditor(" testClass = new TestClass();");
    editor.waitAllMarkersDisappear(ERROR_MARKER);
  }

  @Test(priority = 3)
  public void excludeIncludeModules() {
    projectExplorer.clickCollapseAllButton();
    projectExplorer.expandPathInProjectExplorerAndOpenFile(PROJECT_NAME, "pom.xml");
    editor.waitActiveEditor();
    editor.setCursorToDefinedLineAndChar(13, 8);
    editor.typeTextIntoEditor("!--");
    editor.setCursorToDefinedLineAndChar(13, 32);
    editor.typeTextIntoEditor("--");

    projectExplorer.waitFolderDefinedTypeOfFolderByPath(
        PROJECT_NAME + "/my-lib", ProjectExplorer.FolderTypes.SIMPLE_FOLDER);

    editor.setCursorToDefinedLineAndChar(13, 32);
    editor.typeTextIntoEditor(Keys.DELETE.toString());
    editor.typeTextIntoEditor(Keys.DELETE.toString());
    editor.setCursorToDefinedLineAndChar(13, 8);
    editor.typeTextIntoEditor(Keys.DELETE.toString());
    editor.typeTextIntoEditor(Keys.DELETE.toString());
    editor.typeTextIntoEditor(Keys.DELETE.toString());

    projectExplorer.waitFolderDefinedTypeOfFolderByPath(
        PROJECT_NAME + "/my-lib", ProjectExplorer.FolderTypes.PROJECT_FOLDER);

    editor.closeAllTabs();
  }

  @Test(priority = 4)
  public void shouldAccessClassCreatedInAnotherModuleAfterIncludingModule() {
    projectExplorer.clickCollapseAllButton();
    projectExplorer.expandPathInProjectExplorerAndOpenFile(
        PROJECT_NAME + "/my-webapp/src/main/java/helloworld", "GreetingController.java");
    editor.waitActiveEditor();
    editor.setCursorToDefinedLineAndChar(27, 1);
    enterClassNameViaAutocomplete();
    editor.typeTextIntoEditor(" testClass2 = new TestClass();");
    editor.waitAllMarkersDisappear(ERROR_MARKER);
  }

  /** check ability just created class in autocomplete container */
  private void enterClassNameViaAutocomplete() {
    editor.typeTextIntoEditor("Test");
    editor.launchAutocomplete();
    editor.enterAutocompleteProposal("TestClass");
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
