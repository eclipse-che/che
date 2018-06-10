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

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.ASSISTANT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.FIND_DEFINITION;
import static org.eclipse.che.selenium.core.project.ProjectTemplates.NODE_JS;
import static org.eclipse.che.selenium.core.workspace.WorkspaceTemplate.ECLIPSE_NODEJS;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.ERROR;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.ERROR_OVERVIEW;
import static org.openqa.selenium.Keys.DELETE;
import static org.openqa.selenium.Keys.ENTER;
import static org.openqa.selenium.Keys.SPACE;
import static org.testng.Assert.assertEquals;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class TypeScriptEditingTest {
  private static final String PROJECT_NAME =
      NameGenerator.generate(TypeScriptEditingTest.class.getSimpleName(), 4);
  private static final String PATH_TO_GREETER_FILE = PROJECT_NAME + "/Greeter.ts";
  private static final String PATH_TO_PRINT_TEST_FILE = PROJECT_NAME + "/printTest.ts";

  @InjectTestWorkspace(template = ECLIPSE_NODEJS)
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
    URL resource = getClass().getResource("/projects/type-script-simple-project");

    testProjectServiceClient.importProject(
        workspace.getId(), Paths.get(resource.toURI()), PROJECT_NAME, NODE_JS);

    ide.open(workspace);

    projectExplorer.waitVisibleItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByPath(PATH_TO_GREETER_FILE);
  }

  @Test
  public void checkMainFeaturesTypeScriptLS() {
    String intitTypeScriptLanguageServerMessage =
        String.format(
            "Finished language servers initialization, file path '/%s'", PATH_TO_GREETER_FILE);

    consoles.waitExpectedTextIntoConsole(intitTypeScriptLanguageServerMessage);
    checkCodeValidation();
    checkCodeAssistant();
    checkGoToDefinition();
  }

  private void checkCodeValidation() {

    final int expectedAmountOfErrorMarkers = 9;

    String tooltipWithErrorMessage = "Cannot find name 'c'";

    editor.waitActive();
    editor.goToPosition(13, 2);
    editor.typeTextIntoEditor(SPACE.toString());
    final int actualValueErrorMarkers = editor.getMarkersQuantity(ERROR);
    assertEquals(
        actualValueErrorMarkers,
        expectedAmountOfErrorMarkers,
        String.format(
            "The expected value of errors marker should be %d but actual %d",
            expectedAmountOfErrorMarkers, actualValueErrorMarkers));
    editor.moveToMarker(ERROR_OVERVIEW, 13);
    editor.waitTextInToolTipPopup(tooltipWithErrorMessage);
    editor.goToPosition(13, 2);
    editor.typeTextIntoEditor(DELETE.toString());
    editor.waitAllMarkersInvisibility(ERROR);
  }

  private void checkCodeAssistant() {
    String textFromWholeCodeAssistantScope =
        "AbortController\nAbortSignal\nabstract\nActiveXObject\naddEventListener\nalert";

    String textFromGreeterObject = "greet\ngreeting\ntestPrint";

    String nameOfGreeterClassRef = "greeter.";
    String methodToComplete = "greet";

    // check autocomplete form content after the ";"
    editor.goToPosition(28, 36);
    editor.launchAutocomplete();
    editor.waitTextIntoAutocompleteContainer(textFromWholeCodeAssistantScope);

    editor.closeAutocomplete();

    // Check autocomplete of method 'Greeter.greet' by double clicking in autocomplete form
    editor.typeTextIntoEditor(ENTER.toString());
    editor.typeTextIntoEditor(nameOfGreeterClassRef);

    editor.launchAutocomplete();
    editor.waitTextIntoAutocompleteContainer(textFromGreeterObject);
    editor.selectItemIntoAutocompleteAndPerformDoubleClick(methodToComplete);
    editor.waitAllMarkersInvisibility(ERROR);
  }

  private void checkGoToDefinition() {
    // set cursor to printVar.print place
    editor.goToPosition(24, 20);
    menu.runCommand(ASSISTANT, FIND_DEFINITION);
    editor.waitActiveTabFileName("testPrint.ts");
    editor.waitCursorPosition(14, 6);
  }
}
