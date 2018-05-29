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
package org.eclipse.che.selenium.languageserver;

import static java.lang.String.format;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.ASSISTANT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.FIND_DEFINITION;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project.New.FILE;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project.New.NEW;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project.PROJECT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.CREATE_PROJECT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.WORKSPACE;
import static org.eclipse.che.selenium.core.workspace.WorkspaceTemplate.PYTHON;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.ERROR;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.WARNING;
import static org.eclipse.che.selenium.pageobject.Wizard.SamplesName.CONSOLE_PYTHON3_SIMPLE;
import static org.openqa.selenium.Keys.F4;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.workspace.InjectTestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsPalette;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Skoryk Serhii */
public class PythonFileEditingTest {
  private static final String PROJECT_NAME = "console-python3-simple";
  private static final String PYTHON_FILE_NAME = "main.py";
  private static final String PYTHON_MODULE_FILE_NAME = "math.py";
  private static final String PATH_TO_PYTHON_FILE = PROJECT_NAME + "/" + PYTHON_FILE_NAME;
  private static final String LS_INIT_MESSAGE =
      format("Finished language servers initialization, file path '/%s'", PATH_TO_PYTHON_FILE);
  private static final String PYTHON_CLASS =
      "class MyClass:\n"
          + "    var = 1\n"
          + "    variable = \"variable\"\n"
          + "\n"
          + "    def function(self):\n"
          + "        print(\"This is a message inside the class.\")";

  @InjectTestWorkspace(template = PYTHON)
  private TestWorkspace workspace;

  @Inject private Ide ide;
  @Inject private Menu menu;
  @Inject private Wizard wizard;
  @Inject private Consoles consoles;
  @Inject private CodenvyEditor editor;
  @Inject private CommandsPalette commandsPalette;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private AskForValueDialog askForValueDialog;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    ide.open(workspace);
  }

  @Test
  public void checkLanguageServerInitialized() {
    ide.waitOpenedWorkspaceIsReadyToUse();

    menu.runCommand(WORKSPACE, CREATE_PROJECT);
    wizard.selectProjectAndCreate(CONSOLE_PYTHON3_SIMPLE, PROJECT_NAME);

    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    projectExplorer.openItemByPath(PROJECT_NAME);
    projectExplorer.openItemByPath(PATH_TO_PYTHON_FILE);
    editor.waitTabIsPresent(PYTHON_FILE_NAME);

    // check python language sever initialized
    consoles.selectProcessByTabName("dev-machine");
    consoles.waitExpectedTextIntoConsole(LS_INIT_MESSAGE);
  }

  @Test(priority = 1)
  public void checkMarkers() throws Exception {
    editor.selectTabByName(PYTHON_FILE_NAME);
    editor.deleteAllContent();
    testProjectServiceClient.updateFile(workspace.getId(), PATH_TO_PYTHON_FILE, PYTHON_CLASS);

    // check warning marker message
    editor.goToPosition(6, 53);
    editor.typeTextIntoEditor("\n");
    editor.waitMarkerInPosition(WARNING, editor.getPositionVisible());
    editor.moveToMarkerAndWaitAssistContent(WARNING);
    editor.waitTextIntoAnnotationAssist("W293 blank line contains whitespace");

    // check error marker message
    editor.goToCursorPositionVisible(1, 1);
    editor.waitAllMarkersInvisibility(ERROR);
    editor.typeTextIntoEditor("c");
    editor.waitMarkerInPosition(ERROR, 1);
    editor.typeTextIntoEditor(Keys.DELETE.toString());
    editor.waitAllMarkersInvisibility(ERROR);
  }

  @Test(priority = 1)
  public void checkAutocompleteFeature() throws Exception {
    editor.selectTabByName(PYTHON_FILE_NAME);
    editor.deleteAllContent();
    testProjectServiceClient.updateFile(workspace.getId(), PATH_TO_PYTHON_FILE, PYTHON_CLASS);

    // check contents of autocomplete container
    editor.goToPosition(6, 53);
    editor.typeTextIntoEditor("\n");
    editor.goToPosition(7, 1);
    editor.typeTextIntoEditor("object = MyClass()\nprint(object.");
    editor.launchAutocompleteAndWaitContainer();
    editor.waitTextIntoAutocompleteContainer("function");
    editor.waitTextIntoAutocompleteContainer("var");
    editor.waitTextIntoAutocompleteContainer("variable");

    editor.enterAutocompleteProposal("function() ");
    editor.waitTextIntoEditor("print(object.function");
    editor.typeTextIntoEditor("())");
    commandsPalette.openCommandPalette();
    commandsPalette.startCommandByDoubleClick(PROJECT_NAME + ": run");
    consoles.waitTabNameProcessIsPresent(PROJECT_NAME + ": run");
    consoles.waitExpectedTextIntoConsole("This is a message inside the class.");
  }

  @Test(priority = 1)
  public void checkFindDefinitionFeature() {
    createFile(PYTHON_MODULE_FILE_NAME);
    editor.selectTabByName(PYTHON_MODULE_FILE_NAME);
    editor.typeTextIntoEditor("def add(a, b):\n return a + b");

    editor.selectTabByName(PYTHON_FILE_NAME);
    editor.deleteAllContent();
    editor.typeTextIntoEditor("import math\n");
    editor.typeTextIntoEditor("\nvar2 = math.add(100, 200)");

    // check Find Definition feature from Assistant menu
    editor.goToPosition(editor.getPositionVisible(), 15);
    menu.runCommand(ASSISTANT, FIND_DEFINITION);
    editor.waitTabIsPresent(PYTHON_MODULE_FILE_NAME);
    editor.closeFileByNameWithSaving(PYTHON_MODULE_FILE_NAME);

    // check Find Definition feature by pressing F4
    editor.goToPosition(editor.getPositionVisible(), 15);
    editor.typeTextIntoEditor(F4.toString());
    editor.waitTabIsPresent(PYTHON_MODULE_FILE_NAME);
  }

  private void createFile(String fileName) {
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(PROJECT, NEW, FILE);
    askForValueDialog.waitFormToOpen();
    askForValueDialog.typeAndWaitText(fileName);
    askForValueDialog.clickOkBtn();
    askForValueDialog.waitFormToClose();
    editor.waitTabIsPresent(fileName);
  }
}
