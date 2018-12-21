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

import static org.eclipse.che.selenium.core.TestGroup.UNDER_REPAIR;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.ASSISTANT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.FIND_DEFINITION;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.FIND_PROJECT_SYMBOL;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.FIND_REFERENCES;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.GO_TO_SYMBOL;
import static org.eclipse.che.selenium.core.workspace.WorkspaceTemplate.UBUNTU_GO;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.ContextMenuLocator.FORMAT;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.ERROR;
import static org.openqa.selenium.Keys.ARROW_LEFT;
import static org.openqa.selenium.Keys.F4;
import static org.testng.Assert.fail;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.InjectTestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AssistantFindPanel;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.FindReferencesConsoleTab;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.internal.collections.Pair;

/** @author Skoryk Serhii */
public class GolangFileEditingTest {

  private static final String PROJECT_NAME = "desktop-go-simple";
  private static final String GO_FILE_NAME = "main.go";
  private static final String LS_INIT_MESSAGE = "Finished running tool: /usr/local/go/bin/go build";
  private static final String FORMATTED_CODE =
      "package main\n"
          + "\n"
          + "import (\n"
          + " \"fmt\"\n"
          + " \"math\"\n"
          + ")\n"
          + "\n"
          + "func print() {\n"
          + " fmt.Printf(\"Hello, world. Sqrt(2) = %v\\n\", math.Sqrt(2))\n"
          + "}";

  private static final String REFERENCES_NOTJHING_TO_SHOW_TEXT = "Nothing to show";

  private static final String[] REFERENCES_EXPECTED_TEXT = {
    "/desktop-go-simple/towers.go\nFrom:19:5 To:19:10",
    "/desktop-go-simple/towers.go\nFrom:23:3 To:23:8",
    "/desktop-go-simple/towers.go\nFrom:24:72 To:24:77",
    "/desktop-go-simple/towers.go\nFrom:29:2 To:29:7",
    "/desktop-go-simple/towers.go\nFrom:30:72 To:30:77"
  };

  private static final String[] GO_TO_SYMBOL_EXPECTED_TEXT = {
    "mainsymbols (4)", "count", "hanoi", "main"
  };

  /** It is Map[node-name, Pair[tab-name, line-number]] */
  private static final ImmutableMap<String, Pair<String, Integer>> PROJECT_SYMBOL_EXPECTED_TEXT =
      ImmutableMap.of(
          "print/desktop-go-simple/format.go", Pair.of("format.go", 23),
          "Print/desktop-go-simple/print.go", Pair.of("print.go", 24));

  private String textFirstNode;
  private String textSecondNode;

  private List<String> expectedProposals = ImmutableList.of("Print", "Println", "Printf");

  @InjectTestWorkspace(template = UBUNTU_GO)
  private TestWorkspace workspace;

  @Inject private Ide ide;
  @Inject private Menu menu;
  @Inject private Consoles consoles;
  @Inject private CodenvyEditor editor;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private FindReferencesConsoleTab findReferencesConsoleTab;
  @Inject private AssistantFindPanel assistantFindPanel;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = ApacheCamelFileEditingTest.class.getResource("/projects/go-simple");
    testProjectServiceClient.importProject(
        workspace.getId(), Paths.get(resource.toURI()), PROJECT_NAME, ProjectTemplates.GO);

    ide.open(workspace);
  }

  @Test
  public void checkLanguageServerInitialized() {
    projectExplorer.expandPathInProjectExplorerAndOpenFile(PROJECT_NAME, GO_FILE_NAME);
    editor.waitTabIsPresent(GO_FILE_NAME);

    // check Golang language sever initialized
    consoles.selectProcessByTabName("dev-machine");
    consoles.waitExpectedTextIntoConsole(LS_INIT_MESSAGE);
  }

  @Test(priority = 1)
  public void checkAutocompleteFeature() {
    editor.selectTabByName(GO_FILE_NAME);

    // launch autocomplete feature and check proposals list
    editor.goToPosition(21, 58);
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.typeTextIntoEditor("fmt.");
    editor.launchAutocompleteAndWaitContainer();

    editor.waitProposalDocumentationHTML("<p>No documentation found.</p>\n");
    editor.waitProposalsIntoAutocompleteContainer(expectedProposals);

    editor.deleteCurrentLine();
    editor.waitAllMarkersInvisibility(ERROR);
  }

  @Test(priority = 1)
  public void checkCodeValidationFeature() {
    editor.selectTabByName(GO_FILE_NAME);

    // make error in code and check error marker with message
    editor.waitAllMarkersInvisibility(ERROR);
    editor.goToCursorPositionVisible(13, 1);
    editor.typeTextIntoEditor("p");
    editor.waitMarkerInPosition(ERROR, 13);
    editor.moveToMarkerAndWaitAssistContent(ERROR);
    editor.waitTextIntoAnnotationAssist("expected 'package', found 'IDENT' ppackage");

    // restore content and check error marker invisibility
    editor.goToCursorPositionVisible(13, 1);
    editor.typeTextIntoEditor(Keys.DELETE.toString());
    editor.waitAllMarkersInvisibility(ERROR);

    // check code line commenting
    editor.goToCursorPositionVisible(13, 1);
    editor.launchCommentCodeFeature();
    editor.waitTextIntoEditor("//package main");

    // check code line uncommenting
    editor.launchCommentCodeFeature();
    editor.waitTextNotPresentIntoEditor("//package main");
  }

  @Test(priority = 1)
  public void checkFormatCodeFeature() {
    projectExplorer.openItemByPath(PROJECT_NAME + "/format.go");
    editor.waitTabIsPresent("format.go");

    // format code by Format feature from context menu
    editor.openContextMenuInEditor();
    editor.clickOnItemInContextMenu(FORMAT);
    editor.waitTextIntoEditor(FORMATTED_CODE);
  }

  @Test(priority = 1)
  public void checkFindDefinitionFeature() {
    projectExplorer.openItemByPath(PROJECT_NAME + "/towers.go");
    editor.waitTabIsPresent("towers.go");

    // check Find Definition feature from Assistant menu
    editor.goToPosition(24, 8);
    menu.runCommand(ASSISTANT, FIND_DEFINITION);
    editor.waitTabIsPresent("print.go");
    editor.waitCursorPosition(24, 6);
    editor.clickOnCloseFileIcon("print.go");

    // check Find Definition feature by pressing F4
    editor.selectTabByName("towers.go");
    editor.goToPosition(24, 8);
    editor.typeTextIntoEditor(F4.toString());
    editor.waitTabIsPresent("print.go");
    editor.waitCursorPosition(24, 6);
    editor.clickOnCloseFileIcon("print.go");
  }

  @Test(priority = 1)
  public void checkFindReferencesFeature() {
    projectExplorer.openItemByPath(PROJECT_NAME + "/towers.go");
    editor.waitActive();

    // check the LS info panel when is 'Nothing to show'
    editor.goToPosition(12, 1);
    menu.runCommand(ASSISTANT, FIND_REFERENCES);
    findReferencesConsoleTab.waitExpectedTextInLsPanel(REFERENCES_NOTJHING_TO_SHOW_TEXT);

    // check element in the editor
    editor.goToPosition(19, 5);
    menu.runCommand(ASSISTANT, FIND_REFERENCES);
    findReferencesConsoleTab.waitAllReferencesWithText(
        "/desktop-go-simple/towers.go\nFrom:24:72 To:24:77");
    findReferencesConsoleTab.doubleClickOnReference("From:24:72 To:24:77");
    editor.waitSpecifiedValueForLineAndChar(24, 77);
    editor.typeTextIntoEditor(ARROW_LEFT.toString());
    editor.waitSpecifiedValueForLineAndChar(24, 72);
    editor.waitTextElementsActiveLine("count");

    // check the references expected text
    editor.goToPosition(19, 5);
    menu.runCommand(ASSISTANT, FIND_REFERENCES);
    findReferencesConsoleTab.waitAllReferencesWithText(REFERENCES_EXPECTED_TEXT);
  }

  @Test(priority = 1)
  public void checkSignatureHelpFeature() {
    projectExplorer.openItemByPath(PROJECT_NAME + "/towers.go");
    editor.goToPosition(27, 1);
    editor.typeTextIntoEditor("    hanoi(");

    editor.waitSignaturesContainer();
    editor.waitProposalIntoSignaturesContainer("hanoi(n int, a, b, c string)");
    editor.closeSignaturesContainer();
    editor.waitSignaturesContainerIsClosed();

    editor.deleteCurrentLineAndInsertNew();
  }

  @Test(priority = 1)
  public void checkGoToSymbolFeature() {
    projectExplorer.openItemByPath(PROJECT_NAME + "/towers.go");
    editor.waitActive();

    // open and close 'Go To Symbol' panel by keyboard
    editor.enterCtrlF12();
    assistantFindPanel.waitForm();
    editor.cancelFormInEditorByEscape();
    assistantFindPanel.waitFormIsClosed();

    // select item from 'Go To Symbol' panel
    menu.runCommand(ASSISTANT, GO_TO_SYMBOL);
    assistantFindPanel.waitForm();
    assistantFindPanel.waitAllNodes(GO_TO_SYMBOL_EXPECTED_TEXT);
    assistantFindPanel.clickOnActionNodeWithTextContains("count");
    assistantFindPanel.waitFormIsClosed();
    editor.waitCursorPosition(19, 5);

    // navigation to nodes by keyboard
    editor.enterCtrlF12();
    assistantFindPanel.waitForm();
    editor.pressArrowDown();
    editor.pressArrowDown();
    assistantFindPanel.waitActionNodeSelection("count");
    editor.pressArrowUp();
    assistantFindPanel.waitActionNodeSelection("main" + "symbols (4)");
    editor.pressEnter();
    assistantFindPanel.waitFormIsClosed();
    editor.waitCursorPosition(13, 1);

    // find and select item from 'Go To Symbol' panel
    menu.runCommand(ASSISTANT, GO_TO_SYMBOL);
    assistantFindPanel.waitForm();
    assistantFindPanel.typeToInputField("ha");
    assistantFindPanel.waitNode("hanoi");
    assistantFindPanel.clickOnActionNodeWithTextContains("hanoi");
    assistantFindPanel.waitFormIsClosed();
    editor.waitCursorPosition(21, 1);
  }

  @Test(priority = 1)
  public void checkFindProjectSymbolFeature() {
    projectExplorer.openItemByPath(PROJECT_NAME + "/towers.go");
    editor.waitActive();

    // check item in the find panel
    menu.runCommand(ASSISTANT, FIND_PROJECT_SYMBOL);
    assistantFindPanel.waitForm();
    assistantFindPanel.clickOnInputField();
    assistantFindPanel.typeToInputField("hanoi");
    assistantFindPanel.waitAllNodes("hanoi/desktop-go-simple/towers.go");
    assistantFindPanel.typeToInputField("print");
    assistantFindPanel.waitAllNodes(PROJECT_SYMBOL_EXPECTED_TEXT.keySet());

    // select item in the find panel by clicking on node
    assistantFindPanel.clickOnActionNodeWithTextContains("print/desktop-go-simple/format.go");
    assistantFindPanel.waitFormIsClosed();
    editor.waitTabVisibilityAndCheckFocus("format.go");
    editor.waitCursorPosition(23, 1);

    // select item in the find panel by keyboard
    openFindPanelAndPrintInputTetx("print");

    // go to instantly
    assistantFindPanel.waitActionNodeSelection(textFirstNode);
    editor.pressEnter();
    assistantFindPanel.waitFormIsClosed();
    editor.waitTabVisibilityAndCheckFocus(PROJECT_SYMBOL_EXPECTED_TEXT.get(textFirstNode).first());
    editor.waitCursorPosition(PROJECT_SYMBOL_EXPECTED_TEXT.get(textFirstNode).second(), 1);

    // go to from second node
    openFindPanelAndPrintInputTetx("print");
    editor.pressArrowDown();
    assistantFindPanel.waitActionNodeSelection(textSecondNode);
    editor.pressEnter();
    assistantFindPanel.waitFormIsClosed();
    editor.waitTabVisibilityAndCheckFocus(PROJECT_SYMBOL_EXPECTED_TEXT.get(textSecondNode).first());
    editor.waitCursorPosition(PROJECT_SYMBOL_EXPECTED_TEXT.get(textSecondNode).second(), 1);
  }

  @Test(priority = 1, groups = UNDER_REPAIR)
  public void checkHoverFeature() {
    projectExplorer.openItemByPath(PROJECT_NAME + "/towers.go");
    editor.waitTabIsPresent("towers.go");

    editor.moveCursorToText("COLOR_YELLOW");

    try {
      editor.waitTextInHoverPopup("const COLOR_YELLOW string = \"\\x1b[33;1m \"");
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known permanent failure https://github.com/eclipse/che/issues/10674", ex);
    }
  }

  private void openFindPanelAndPrintInputTetx(String inputText) {
    editor.selectTabByName("towers.go");
    menu.runCommand(ASSISTANT, FIND_PROJECT_SYMBOL);
    assistantFindPanel.waitForm();
    assistantFindPanel.typeToInputField(inputText);
    assistantFindPanel.waitAllNodes(PROJECT_SYMBOL_EXPECTED_TEXT.keySet());

    textFirstNode = assistantFindPanel.getActionNodeText(0);
    textSecondNode = assistantFindPanel.getActionNodeText(1);
  }
}
