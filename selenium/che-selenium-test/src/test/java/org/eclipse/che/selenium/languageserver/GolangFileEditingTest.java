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

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.ASSISTANT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.FIND_DEFINITION;
import static org.eclipse.che.selenium.core.workspace.WorkspaceTemplate.UBUNTU_GO;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.ContextMenuLocator.FORMAT;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.ERROR;
import static org.openqa.selenium.Keys.F4;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
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
  private List<String> expectedProposals = ImmutableList.of("Print", "Println", "Printf");

  @InjectTestWorkspace(template = UBUNTU_GO)
  private TestWorkspace workspace;

  @Inject private Ide ide;
  @Inject private Menu menu;
  @Inject private Consoles consoles;
  @Inject private CodenvyEditor editor;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private TestProjectServiceClient testProjectServiceClient;

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

    editor.checkProposalDocumentation("No documentation found.");
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

    editor.moveCursorToText("COLOR_YELLOW");
    editor.waitTextInHoverPopup("const COLOR_YELLOW string = \"\\x1b[33;1m \"");

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
}
