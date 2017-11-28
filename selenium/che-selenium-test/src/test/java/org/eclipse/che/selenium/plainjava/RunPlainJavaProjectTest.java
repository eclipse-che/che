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
package org.eclipse.che.selenium.plainjava;

import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandsDefaultNames.JAVA_NAME;
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandsGoals.RUN_GOAL;
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandsTypes.JAVA_TYPE;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project.CONFIGURE_CLASSPATH;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project.New.JAVA_CLASS;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project.New.NEW;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project.New.PACKAGE;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project.PROJECT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.IMPORT_PROJECT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.WORKSPACE;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkersType.ERROR_MARKER;

import com.google.inject.Inject;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.ConfigureClasspath;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ImportProjectFromLocation;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsEditor;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsExplorer;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev */
public class RunPlainJavaProjectTest {
  private static final String PROJECT_NAME = NameGenerator.generate("RunningPlainJavaProject", 4);
  private static final String NEW_PACKAGE = "base.test";
  private static final String CLONE_URI = "https://github.com/iedexmain1/plainJavaProject.git";
  private static final String NAME_COMMAND = "startApp";
  private static final String COMMAND =
      "cd ${current.project.path}\n"
          + "javac -classpath ${project.java.classpath} -sourcepath ${project.java.sourcepath} -d ${project.java.output.dir} src/com/company/nba/MainClass.java\n"
          + "java -classpath ${project.java.classpath}${project.java.output.dir} com.company.nba.MainClass";
  private static final String CONSOLE_MESS =
      "javac: directory not found: /projects/" + PROJECT_NAME + "/bin";

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;
  @Inject private TestUser productUser;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private CodenvyEditor codenvyEditor;
  @Inject private ConfigureClasspath configureClasspath;
  @Inject private CommandsExplorer commandsExplorer;
  @Inject private CommandsEditor commandsEditor;
  @Inject private Consoles consoles;
  @Inject private AskForValueDialog askForValueDialog;
  @Inject private ImportProjectFromLocation importFromLocation;
  @Inject private Wizard projectWizard;
  @Inject private Loader loader;
  @Inject private Menu menu;

  @BeforeClass
  public void prepare() throws Exception {
    ide.open(ws);
  }

  @Test
  public void checkRunPlainJavaProject() {
    // import the project and configure
    projectExplorer.waitProjectExplorer();
    importPlainJavaApp(CLONE_URI, PROJECT_NAME, Wizard.TypeProject.PLAIN_JAVA);
    loader.waitOnClosed();

    // check library into configure classpath form
    projectExplorer.quickExpandWithJavaScript();
    loader.waitOnClosed();
    projectExplorer.selectItem(PROJECT_NAME);
    menu.runCommand(PROJECT, CONFIGURE_CLASSPATH);
    configureClasspath.waitConfigureClasspathFormIsOpen();
    configureClasspath.waitExpectedTextJarsAndFolderArea(
        "mockito-all-1.10.19.jar - /projects/" + PROJECT_NAME + "/store");
    configureClasspath.closeConfigureClasspathFormByIcon();

    // expand the project and use library
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/com/company/nba/MainClass.java");
    codenvyEditor.waitActiveEditor();
    codenvyEditor.setCursorToLine(9);
    codenvyEditor.typeTextIntoEditor(Keys.TAB.toString());
    codenvyEditor.typeTextIntoEditor("Mockito mockito = new Mockito();");
    codenvyEditor.waitTextIntoEditor("Mockito mockito = new Mockito();");
    codenvyEditor.waitMarkerInPosition(ERROR_MARKER, 9);
    codenvyEditor.launchPropositionAssistPanel();
    codenvyEditor.enterTextIntoFixErrorPropByDoubleClick("Import 'Mockito' (org.mockito)");
    codenvyEditor.waitTextIntoEditor("import org.mockito.Mockito;");

    // Create new java class into new package
    projectExplorer.selectItem(PROJECT_NAME + "/src");
    menu.runCommand(PROJECT, NEW, PACKAGE);
    askForValueDialog.waitFormToOpen();
    askForValueDialog.typeAndWaitText(NEW_PACKAGE);
    askForValueDialog.clickOkBtn();
    askForValueDialog.waitFormToClose();
    projectExplorer.waitItemInVisibleArea(NEW_PACKAGE);
    projectExplorer.selectItem(PROJECT_NAME + "/src/base/test");
    menu.runCommand(TestMenuCommandsConstants.Project.PROJECT, NEW, JAVA_CLASS);
    loader.waitOnClosed();
    askForValueDialog.waitNewJavaClassOpen();
    askForValueDialog.typeTextInFieldName("A");
    askForValueDialog.clickOkBtnNewJavaClass();
    askForValueDialog.waitNewJavaClassClose();
    projectExplorer.waitItem(PROJECT_NAME + "/src/base/test/A.java");
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/com/company/nba/MainClass.java");
    codenvyEditor.waitActiveEditor();
    codenvyEditor.setCursorToLine(12);
    codenvyEditor.typeTextIntoEditor(Keys.TAB.toString());
    codenvyEditor.typeTextIntoEditor("A a = new A();");
    codenvyEditor.waitTextIntoEditor("A a = new A();");
    codenvyEditor.waitMarkerInPosition(ERROR_MARKER, 12);
    codenvyEditor.launchPropositionAssistPanel();
    codenvyEditor.enterTextIntoFixErrorPropByDoubleClick("Import 'A' (base.test)");
    codenvyEditor.waitErrorPropositionPanelClosed();
    codenvyEditor.waitTextIntoEditor("import base.test.A;");

    // open the 'Commands Explorer' and choose java command
    projectExplorer.selectItem(PROJECT_NAME);
    loader.waitOnClosed();
    commandsExplorer.openCommandsExplorer();
    commandsExplorer.waitCommandExplorerIsOpened();
    commandsExplorer.clickAddCommandButton(RUN_GOAL);
    loader.waitOnClosed();
    commandsExplorer.chooseCommandTypeInContextMenu(JAVA_TYPE);
    loader.waitOnClosed();
    commandsExplorer.waitCommandInExplorerByName(JAVA_NAME);
    commandsEditor.waitActiveEditor();
    commandsEditor.waitTabFileWithSavedStatus(JAVA_NAME);

    // edit the name and the content of the java command into editor
    commandsEditor.typeTextIntoNameCommandField(NAME_COMMAND);
    commandsEditor.waitTextIntoNameCommandField(NAME_COMMAND);
    commandsEditor.waitTabCommandWithUnsavedStatus(JAVA_NAME);
    commandsEditor.setCursorToLine(1);
    commandsEditor.deleteAllContent();
    commandsEditor.typeTextIntoEditor(COMMAND);
    commandsEditor.waitTextIntoEditor(COMMAND);
    commandsEditor.clickOnSaveButtonInTheEditCommand();
    commandsEditor.waitTabFileWithSavedStatus(NAME_COMMAND);

    // check not starting application
    commandsEditor.clickOnRunButton();
    loader.waitOnClosed();
    consoles.waitExpectedTextIntoConsole(CONSOLE_MESS);

    // add the folder 'bin'
    projectExplorer.clickOnProjectExplorerTabInTheLeftPanel();
    commandsExplorer.waitCommandExplorerIsClosed();
    projectExplorer.waitProjectExplorer();
    projectExplorer.selectItem(PROJECT_NAME);
    projectExplorer.openContextMenuByPathSelectedItem(PROJECT_NAME);
    projectExplorer.clickOnItemInContextMenu(TestProjectExplorerContextMenuConstants.NEW);
    projectExplorer.clickOnNewContextMenuItem(
        TestProjectExplorerContextMenuConstants.SubMenuNew.FOLDER);
    askForValueDialog.waitFormToOpen();
    askForValueDialog.typeAndWaitText("bin");
    askForValueDialog.clickOkBtn();
    loader.waitOnClosed();
    projectExplorer.waitItem(PROJECT_NAME + "/bin");

    // check starting application
    commandsEditor.selectTabByName(NAME_COMMAND);
    commandsEditor.waitActiveTabFileName(NAME_COMMAND);
    commandsEditor.clickOnRunButton();
    loader.waitOnClosed();
    consoles.waitExpectedTextIntoConsole("I love this game!");

    // check the 'bin' folder that contains compiling classes
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.waitItem(PROJECT_NAME + "/bin/com/company/nba/MainClass.class");
    projectExplorer.waitItem(PROJECT_NAME + "/bin/base/test/A.class");
  }

  private void importPlainJavaApp(String url, String nameApp, String typeProject) {
    loader.waitOnClosed();
    menu.runCommand(WORKSPACE, IMPORT_PROJECT);
    importFromLocation.waitAndTypeImporterAsGitInfo(url, nameApp);
    projectWizard.waitCreateProjectWizardForm();
    projectWizard.selectTypeProject(typeProject);
    loader.waitOnClosed();
    projectWizard.clickNextButton();

    // set source folder
    projectWizard.waitOpenProjectConfigForm();
    projectWizard.clickBrowseButton(Wizard.TypeFolder.SOURCE_FOLDER);
    configureClasspath.waitSelectPathFormIsOpen();
    configureClasspath.openItemInSelectPathForm(nameApp);
    configureClasspath.waitItemInSelectPathForm("src");
    configureClasspath.selectItemInSelectPathForm("src");
    configureClasspath.clickOkBtnSelectPathForm();
    projectWizard.waitExpTextInSourceFolder("src", Wizard.TypeFolder.SOURCE_FOLDER);

    // set library folder
    projectWizard.clickBrowseButton(Wizard.TypeFolder.LIBRARY_FOLDER);
    configureClasspath.openItemInSelectPathForm(nameApp);
    configureClasspath.waitItemInSelectPathForm("store");
    configureClasspath.selectItemInSelectPathForm("store");
    configureClasspath.clickOkBtnSelectPathForm();
    projectWizard.waitExpTextInSourceFolder("store", Wizard.TypeFolder.LIBRARY_FOLDER);
    projectWizard.clickSaveButton();
    loader.waitOnClosed();
    projectWizard.waitCloseProjectConfigForm();
    projectExplorer.waitItemInVisibleArea(nameApp);
    loader.waitOnClosed();
  }
}
