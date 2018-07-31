/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.languageserver;

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.ASSISTANT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.FIND_DEFINITION;
import static org.eclipse.che.selenium.core.project.ProjectTemplates.CPP;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.ContextMenuLocator.FORMAT;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.ERROR;
import static org.openqa.selenium.Keys.F4;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.workspace.InjectTestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.core.workspace.WorkspaceTemplate;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Skoryk Serhii */
public class ClangFileEditingTest {
  private static final String PROJECT_NAME = "console-cpp-simple";
  private static final String CPP_FILE_NAME = "hello.cc";
  private static final String H_FILE_NAME = "iseven.h";
  private static final String LS_INIT_MESSAGE =
      "Finished language servers initialization, file path '/console-cpp-simple/hello.cc";

  @InjectTestWorkspace(template = WorkspaceTemplate.ECLIPSE_CPP_GCC)
  private TestWorkspace workspace;

  @Inject private Ide ide;
  @Inject private Menu menu;
  @Inject private Consoles consoles;
  @Inject private CodenvyEditor editor;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = ClangFileEditingTest.this.getClass().getResource("/projects/console-cpp-simple");
    testProjectServiceClient.importProject(
        workspace.getId(), Paths.get(resource.toURI()), PROJECT_NAME, CPP);

    ide.open(workspace);
  }

  @Test
  public void checkMainFeaturesClangdLS() {
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    projectExplorer.openItemByPath(PROJECT_NAME);
    projectExplorer.openItemByPath(PROJECT_NAME + "/hello.cc");
    editor.waitTabIsPresent("hello.cc");

    // check clangd language sever initialized
    consoles.selectProcessByTabName("dev-machine");
    consoles.waitExpectedTextIntoConsole(LS_INIT_MESSAGE);

    checkCodeValidation();
    checkAutocompleteFeature();
    checkCodeFormatting();
    checkFindDefinitionFeature();
  }

  private void checkCodeValidation() {
    editor.selectTabByName(CPP_FILE_NAME);

    // check error marker message
    editor.goToCursorPositionVisible(13, 1);
    editor.waitMarkerInvisibility(ERROR, 13);
    editor.typeTextIntoEditor("c");
    editor.waitMarkerInPosition(ERROR, 13);

    editor.typeTextIntoEditor(Keys.DELETE.toString());
    editor.waitAllMarkersInvisibility(ERROR);
  }

  private void checkAutocompleteFeature() {
    editor.selectTabByName(CPP_FILE_NAME);

    // check contents of autocomplete container
    editor.goToPosition(15, 1);
    editor.deleteCurrentLineAndInsertNew();
    editor.typeTextIntoEditor("std::cou");
    editor.launchAutocompleteAndWaitContainer();
    editor.waitTextIntoAutocompleteContainer("cout ostream");
    editor.waitTextIntoAutocompleteContainer("wcout wostream");
    editor.closeAutocomplete();
  }

  private void checkFindDefinitionFeature() {
    projectExplorer.openItemByPath(PROJECT_NAME + "/hello.cpp");
    editor.waitActive();

    // check Find Definition feature from Assistant menu
    editor.goToPosition(20, 20);
    menu.runCommand(ASSISTANT, FIND_DEFINITION);
    editor.waitTabIsPresent(H_FILE_NAME);
    editor.clickOnCloseFileIcon(H_FILE_NAME);

    // check Find Definition feature by pressing F4
    editor.goToPosition(20, 20);
    editor.typeTextIntoEditor(F4.toString());
    editor.waitTabIsPresent(H_FILE_NAME);
  }

  private void checkCodeFormatting() {
    projectExplorer.openItemByPath(PROJECT_NAME + "/iseven.cpp");
    editor.waitActive();

    editor.openContextMenuInEditor();
    editor.clickOnItemInContextMenu(FORMAT);
    editor.waitTextIntoEditor("int isEven(int x) { return x % 2 == 0; }");
  }
}
