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
package org.eclipse.che.selenium.languageserver;

import static java.lang.String.format;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.ASSISTANT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.GO_TO_SYMBOL;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.CREATE_PROJECT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.WORKSPACE;
import static org.eclipse.che.selenium.core.workspace.WorkspaceTemplate.NODEJS_WITH_JSON_LS;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.ERROR;
import static org.eclipse.che.selenium.pageobject.Wizard.SamplesName.NODEJS_HELLO_WORLD;
import static org.openqa.selenium.Keys.BACK_SPACE;
import static org.openqa.selenium.Keys.ENTER;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project.New;
import org.eclipse.che.selenium.core.workspace.InjectTestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
import org.eclipse.che.selenium.pageobject.AssistantFindPanel;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Skoryk Serhii */
public class JsonFileEditingTest {

  private static final String PROJECT_NAME = "nodejs-hello-world";
  private static final String JSON_FILE_NAME = "package.json";
  private static final String PATH_TO_JSON_FILE = PROJECT_NAME + "/" + JSON_FILE_NAME;
  private static final String NEW_OBJECT = "\"newObj\":[1,2,3],";
  private static final String LS_INIT_MESSAGE =
      format("Finished language servers initialization, file path '/%s'", PATH_TO_JSON_FILE);

  private String[] symbols = {
    "namesymbols (12)",
    "version",
    "description",
    "main",
    "scripts",
    "test",
    "author",
    "license",
    "dependencies",
    "express"
  };

  @InjectTestWorkspace(template = NODEJS_WITH_JSON_LS)
  private TestWorkspace workspace;

  @Inject private Ide ide;
  @Inject private Menu menu;
  @Inject private Wizard wizard;
  @Inject private Consoles consoles;
  @Inject private CodenvyEditor editor;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private AskForValueDialog askForValueDialog;
  @Inject private AssistantFindPanel assistantFindPanel;

  @BeforeClass
  public void setUp() throws Exception {
    ide.open(workspace);
    ide.waitOpenedWorkspaceIsReadyToUse();
    createProjectFromWizard();
  }

  @Test
  public void checkLanguageServerInitialized() {
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    projectExplorer.openItemByPath(PROJECT_NAME);
    projectExplorer.openItemByPath(PATH_TO_JSON_FILE);
    editor.waitTabIsPresent(JSON_FILE_NAME);

    // check JSON language server initialized
    consoles.selectProcessByTabName("dev-machine");
    consoles.waitExpectedTextIntoConsole(LS_INIT_MESSAGE);
  }

  @Test(priority = 1)
  public void checkCodeValidationFeature() {
    editor.selectTabByName(JSON_FILE_NAME);

    // delete comma after bracket and check error marker with message
    editor.goToCursorPositionVisible(8, 5);
    editor.typeTextIntoEditor(BACK_SPACE.toString());
    editor.waitMarkerInPosition(ERROR, 9);
    editor.moveToMarkerAndWaitAssistContent(ERROR);
    editor.waitTextIntoAnnotationAssist("Expected '(end)' and instead saw ':'.");

    // move cursor on text and check expected text in hover popup
    editor.moveCursorToText("author");
    editor.waitTextInHoverPopup("Expected comma or closing brace");

    // return comma and check error marker invisibility
    editor.goToCursorPositionVisible(8, 4);
    editor.typeTextIntoEditor(",");
    editor.waitAllMarkersInvisibility(ERROR);

    // add new object
    editor.goToCursorPositionVisible(9, 16);
    editor.typeTextIntoEditor(ENTER.toString());
    editor.typeTextIntoEditor(NEW_OBJECT);
    editor.waitAllMarkersInvisibility(ERROR);

    // add duplicated object and check error marker with 'duplicate' message
    editor.typeTextIntoEditor(ENTER.toString());
    editor.typeTextIntoEditor(NEW_OBJECT);
    editor.waitMarkerInPosition(ERROR, 11);
    editor.moveToMarkerAndWaitAssistContent(ERROR);
    editor.waitTextIntoAnnotationAssist("Duplicate key 'newObj'.");

    // delete the duplicated object and check error marker invisibility
    editor.deleteCurrentLine();
    editor.waitAllMarkersInvisibility(ERROR);

    // add duplicated object in other {} block
    editor.goToCursorPositionVisible(6, 15);
    editor.typeTextIntoEditor(ENTER.toString());
    editor.typeTextIntoEditor(NEW_OBJECT);
    editor.waitAllMarkersInvisibility(ERROR);
  }

  @Test(priority = 1)
  public void checkAutocompleteFeature() {
    editor.selectTabByName(JSON_FILE_NAME);

    editor.goToCursorPositionVisible(13, 4);
    editor.typeTextIntoEditor(Keys.ENTER.toString());

    editor.typeTextIntoEditor("obj");
    editor.launchAutocomplete();
    editor.waitTextIntoEditor("\"obj\"");

    editor.typeTextIntoEditor(":");
    editor.launchAutocompleteAndWaitContainer();
    editor.waitProposalIntoAutocompleteContainer("Empty object");
    editor.waitProposalIntoAutocompleteContainer("Empty array");
    editor.closeAutocomplete();

    editor.deleteCurrentLine();
  }

  @Test(priority = 1)
  public void checkHoverFeature() {
    menu.runCommand(Project.PROJECT, New.NEW, New.FILE);
    askForValueDialog.createNotJavaFileByName("schema.json");
    editor.waitTabIsPresent("schema.json");

    editor.goToPosition(1, 1);
    editor.typeTextIntoEditor("{\n");
    editor.typeTextIntoEditor(
        "   \"$schema\": \"http://json-schema.org/draft-04/schema#\",\n"
            + "\"title\": \"Items\",\n"
            + "\"type\": \"string\"");

    editor.moveCursorToText("title");
    editor.waitTextInHoverPopup("A descriptive title of the element");

    editor.moveCursorToText("$schema");
    editor.waitTextInHoverPopup("The schema to verify this document against");
  }

  @Test(priority = 2)
  public void checkGoToSymbolFeature() {
    editor.selectTabByName(JSON_FILE_NAME);

    // check list for expected items
    menu.runCommand(ASSISTANT, GO_TO_SYMBOL);
    assistantFindPanel.waitForm();
    assistantFindPanel.waitAllNodes(symbols);

    // open item by mouse click
    assistantFindPanel.clickOnActionNodeWithText("version");
    editor.waitCursorPosition(3, 3);

    // find and open item from Go To Symbol panel
    menu.runCommand(ASSISTANT, GO_TO_SYMBOL);
    assistantFindPanel.waitForm();
    assistantFindPanel.typeToInputField("nam");
    assistantFindPanel.waitNode("name");
    assistantFindPanel.clickOnActionNodeWithText("name");
    editor.waitCursorPosition(2, 3);

    // select items by DOWN and UP buttons
    menu.runCommand(ASSISTANT, GO_TO_SYMBOL);
    assistantFindPanel.waitForm();
    editor.typeTextIntoEditor(Keys.DOWN.toString());
    editor.waitCursorPosition(2, 26);
    editor.typeTextIntoEditor(Keys.DOWN.toString());
    editor.waitCursorPosition(3, 21);
    editor.typeTextIntoEditor(Keys.DOWN.toString());
    editor.waitCursorPosition(4, 20);
    editor.typeTextIntoEditor(Keys.UP.toString());
    editor.waitCursorPosition(3, 21);

    // open item by pressing ENTER key
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitCursorPosition(3, 3);
  }

  private void createProjectFromWizard() {
    projectExplorer.waitProjectExplorer();

    menu.runCommand(WORKSPACE, CREATE_PROJECT);
    wizard.selectSample(NODEJS_HELLO_WORLD);
    wizard.typeProjectNameOnWizard(PROJECT_NAME);
    wizard.clickCreateButton();
    wizard.waitCloseProjectConfigForm();
  }
}
