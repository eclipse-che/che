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
import static org.eclipse.che.selenium.core.constant.TestCommandsConstants.FINISH_LANGUAGE_SERVER_INITIALIZATION_MESSAGE;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.ASSISTANT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.FIND_DEFINITION;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.FIND_PROJECT_SYMBOL;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.FIND_REFERENCES;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.GO_TO_SYMBOL;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Edit.EDIT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Edit.FORMAT;
import static org.eclipse.che.selenium.core.project.ProjectTemplates.NODE_JS;
import static org.eclipse.che.selenium.core.workspace.WorkspaceTemplate.ECLIPSE_NODEJS;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.ERROR;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.ERROR_OVERVIEW;
import static org.openqa.selenium.Keys.DELETE;
import static org.openqa.selenium.Keys.ENTER;
import static org.openqa.selenium.Keys.SPACE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.workspace.InjectTestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
import org.eclipse.che.selenium.pageobject.AssistantFindPanel;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.FindReferencesConsoleTab;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsPalette;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;
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
  @Inject private FindReferencesConsoleTab findReferencesConsoleTab;
  @Inject private AssistantFindPanel assistantFindPanel;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/type-script-simple-project");

    testProjectServiceClient.importProject(
        workspace.getId(), Paths.get(resource.toURI()), PROJECT_NAME, NODE_JS);

    ide.open(workspace);

    projectExplorer.waitVisibleItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByPath(PATH_TO_GREETER_FILE);
    consoles.waitExpectedTextIntoConsole(FINISH_LANGUAGE_SERVER_INITIALIZATION_MESSAGE);
  }

  @Test
  public void checkGoToSymbolFeature() {
    editor.goToCursorPositionVisible(14, 7);
    List<String> expectedGoToSymbolAlternatives =
        Arrays.asList(
            "greeter",
            "Greeter",
            "greet",
            "constructor",
            "\"Greeter\"",
            "print",
            "printVar",
            "greeting");
    menu.runCommand(ASSISTANT, GO_TO_SYMBOL);
    assistantFindPanel.waitAllNodes(expectedGoToSymbolAlternatives);
    assistantFindPanel.clickOnActionNodeWithTextEqualsTo("greet");
    editor.waitCursorPosition(19, 5);
  }

  @Test(priority = 1, alwaysRun = true)
  public void checkFindProjectSymbolsFeature() {
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(ASSISTANT, FIND_PROJECT_SYMBOL);
    assistantFindPanel.typeToInputField("testPrint");
    assistantFindPanel.clickOnActionNodeWithTextContains("testPrint");
    editor.waitCursorPosition(26, 6);
  }

  @Test(priority = 2, alwaysRun = true)
  public void checkMainFeaturesTypeScriptLS() {
    String initTypeScriptLanguageServerMessage =
        format("Finished language servers initialization, file path '/%s'", PATH_TO_GREETER_FILE);

    consoles.waitExpectedTextIntoConsole(initTypeScriptLanguageServerMessage);
    checkCodeValidation();
    checkCodeAssistant();
    checkGoToDefinition();
  }

  @Test(priority = 3, alwaysRun = true)
  public void checkFindReferencesFeature() {
    String referenceInGreeterClass = format("/%s/Greeter.ts\nFrom:24:17 To:24:22", PROJECT_NAME);
    String referenceInTestPrintClass = format("/%s/testPrint.ts\nFrom:14:0 To:14:5", PROJECT_NAME);
    menu.runCommand(ASSISTANT, FIND_REFERENCES);
    findReferencesConsoleTab.waitAllReferencesWithText(
        referenceInGreeterClass, referenceInTestPrintClass);
    findReferencesConsoleTab.doubleClickOnReferenceEqualsTo(referenceInGreeterClass);
    editor.waitCursorPosition(25, 23);
  }

  @Test(priority = 4, alwaysRun = true)
  public void checkHoveringFeature() {
    editor.moveCursorToText("Greeter");
    try {
      editor.waitHoverPopupAppearance();
    } catch (TimeoutException ex) {
      fail("Known permanent failure https://github.com/eclipse/che/issues/10699");
    }
  }

  @Test(priority = 5, alwaysRun = true)
  public void checkSignatureHelpProvider() {
    editor.goToCursorPositionVisible(25, 38);
    editor.typeTextIntoEditor(ENTER.toString());
    editor.typeTextIntoEditor("printVar.print(");

    try {
      editor.waitExpTextIntoShowHintsPopUp("setValue: string");
    } catch (WebDriverException ex) {
      fail("Known permanent failure https://github.com/eclipse/che/issues/11324", ex);
    }
  }

  @Test(priority = 6, alwaysRun = true)
  public void checkCodeCommentFeature() {
    editor.goToCursorPositionVisible(26, 9);
    editor.launchCommentCodeFeature();
    editor.waitTextIntoEditor("//        printVar.print()", 2);
    editor.launchCommentCodeFeature();
    // after uncommenting we get error in the editor
    editor.waitMarkerInPosition(CodenvyEditor.MarkerLocator.ERROR, 26);
  }

  @Test(priority = 7, alwaysRun = true)
  public void checkCodeFormattingFeature() throws URISyntaxException, IOException {
    // read all lines from template
    URL resources =
        getClass()
            .getResource("/org/eclipse/che/selenium/languageServers/typeScriptAfterFormatting.txt");
    List<String> listWithAllLines =
        Files.readAllLines(Paths.get(resources.toURI()), Charset.forName("UTF-8"));

    // save it ot StringBuilder with return symbols
    StringBuilder stringBuilder = new StringBuilder();
    for (String currentLine : listWithAllLines) {
      stringBuilder.append(currentLine + "\n");
    }

    // format text and compare with text from template
    menu.runCommand(EDIT, FORMAT);
    editor.waitTextIntoEditor(stringBuilder.toString());
  }

  private void checkCodeValidation() {

    final int expectedAmountOfErrorMarkers = 9;

    String tooltipWithErrorMessage = "Cannot find name 'c'";

    editor.waitActive();
    editor.goToPosition(14, 2);
    editor.typeTextIntoEditor(SPACE.toString());
    final int actualValueErrorMarkers = editor.getMarkersQuantity(ERROR);
    assertEquals(
        actualValueErrorMarkers,
        expectedAmountOfErrorMarkers,
        format(
            "The expected value of errors marker should be %d but actual %d",
            expectedAmountOfErrorMarkers, actualValueErrorMarkers));
    editor.moveToMarker(ERROR_OVERVIEW, 14);
    editor.waitTextInToolTipPopup(tooltipWithErrorMessage);
    editor.goToPosition(14, 2);
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
    editor.goToPosition(29, 36);
    editor.launchAutocomplete();
    editor.waitProposalIntoAutocompleteContainer(textFromWholeCodeAssistantScope);

    editor.closeAutocomplete();

    // Check autocomplete of method 'Greeter.greet' by double clicking in autocomplete form
    editor.typeTextIntoEditor(ENTER.toString());
    editor.typeTextIntoEditor(nameOfGreeterClassRef);

    editor.launchAutocomplete();
    editor.waitProposalIntoAutocompleteContainer(textFromGreeterObject);
    editor.selectItemIntoAutocompleteAndPerformDoubleClick(methodToComplete);
    editor.waitAllMarkersInvisibility(ERROR);
  }

  private void checkGoToDefinition() {
    // set cursor to printVar.print place
    editor.goToPosition(25, 20);
    menu.runCommand(ASSISTANT, FIND_DEFINITION);
    editor.waitActiveTabFileName("testPrint.ts");
    editor.waitCursorPosition(15, 6);
  }
}
