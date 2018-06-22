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
import static org.eclipse.che.selenium.core.workspace.WorkspaceTemplate.PYTHON;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.ContextMenuLocator.FORMAT;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.ERROR;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.WARNING;
import static org.openqa.selenium.Keys.F4;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.InjectTestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Skoryk Serhii */
public class PythonFileEditingTest {
  private static final String PROJECT_NAME = "console-python3-simple";
  private static final String PYTHON_FILE_NAME = "main.py";
  private static final String PYTHON_MODULE_FILE_NAME = "module.py";
  private static final String PATH_TO_PYTHON_FILE = PROJECT_NAME + "/" + PYTHON_FILE_NAME;
  private static final String LS_INIT_MESSAGE =
      format("Finished language servers initialization, file path '/%s'", PATH_TO_PYTHON_FILE);

  @InjectTestWorkspace(template = PYTHON)
  private TestWorkspace workspace;

  @Inject private Ide ide;
  @Inject private Menu menu;
  @Inject private Consoles consoles;
  @Inject private CodenvyEditor editor;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = PythonFileEditingTest.class.getResource("/projects/console-python3-simple");
    testProjectServiceClient.importProject(
        workspace.getId(), Paths.get(resource.toURI()), PROJECT_NAME, ProjectTemplates.PYTHON);
    ide.open(workspace);
  }

  @Test
  public void checkLanguageServerInitialized() {
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    projectExplorer.openItemByPath(PROJECT_NAME);
    projectExplorer.openItemByPath(PATH_TO_PYTHON_FILE);
    editor.waitTabIsPresent(PYTHON_FILE_NAME);

    // check python language sever initialized
    consoles.selectProcessByTabName("dev-machine");
    consoles.waitExpectedTextIntoConsole(LS_INIT_MESSAGE);
  }

  @Test(priority = 1)
  public void checkMarkers() {
    editor.selectTabByName(PYTHON_FILE_NAME);

    // check warning marker message
    editor.goToPosition(18, 53);
    editor.typeTextIntoEditor("\n");
    editor.waitMarkerInPosition(WARNING, editor.getPositionVisible());
    editor.moveToMarkerAndWaitAssistContent(WARNING);
    editor.waitTextIntoAnnotationAssist("W293 blank line contains whitespace");

    // check error marker message
    editor.goToCursorPositionVisible(13, 1);
    editor.waitAllMarkersInvisibility(ERROR);
    editor.typeTextIntoEditor("c");
    editor.waitMarkerInPosition(ERROR, 13);
    editor.typeTextIntoEditor(Keys.DELETE.toString());
    editor.waitAllMarkersInvisibility(ERROR);
  }

  @Test(priority = 1)
  public void checkAutocompleteFeature() {
    editor.selectTabByName(PYTHON_FILE_NAME);

    // check contents of autocomplete container
    editor.goToPosition(19, 1);
    editor.typeTextIntoEditor("\n\nobject = MyClass()\nprint(object.");

    editor.launchAutocompleteAndWaitContainer();
    editor.waitTextIntoAutocompleteContainer("function");
    editor.waitTextIntoAutocompleteContainer("var");
    editor.waitTextIntoAutocompleteContainer("variable");

    editor.enterAutocompleteProposal("function() ");
    editor.waitTextIntoEditor("print(object.function");
  }

  @Test(priority = 1)
  public void checkFindDefinitionFeature() {
    // check Find Definition feature from Assistant menu
    projectExplorer.openItemByPath(PROJECT_NAME + "/calc.py");
    editor.selectTabByName("calc.py");

    editor.goToPosition(15, 17);
    menu.runCommand(ASSISTANT, FIND_DEFINITION);
    editor.waitTabIsPresent(PYTHON_MODULE_FILE_NAME);
    editor.closeFileByNameWithSaving(PYTHON_MODULE_FILE_NAME);

    // check Find Definition feature by pressing F4
    editor.goToPosition(15, 17);
    editor.typeTextIntoEditor(F4.toString());
    editor.waitTabIsPresent(PYTHON_MODULE_FILE_NAME);
  }

  @Test(priority = 1)
  public void checkFormatCodeFeature() {
    projectExplorer.openItemByPath(PROJECT_NAME + "/towers.py");
    editor.selectTabByName("towers.py");

    // select Format feature from context menu
    editor.openContextMenuInEditor();
    editor.clickOnItemInContextMenu(FORMAT);
    editor.waitTextIntoEditor(
        "        towers(i-1, middle, finish, start)\n\n\ntowers(5, 'X', 'Z', 'Y')\n");
  }
}
