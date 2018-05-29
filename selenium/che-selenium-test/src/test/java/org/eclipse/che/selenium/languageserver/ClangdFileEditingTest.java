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
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project.New.FILE;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project.New.NEW;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project.PROJECT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.CREATE_PROJECT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.WORKSPACE;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.ERROR;
import static org.eclipse.che.selenium.pageobject.Wizard.SamplesName.CONSOLE_CPP_SIMPLE;
import static org.openqa.selenium.Keys.F4;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.workspace.InjectTestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.core.workspace.WorkspaceTemplate;
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
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
public class ClangdFileEditingTest {
  private static final String PROJECT_NAME = "console-cpp-simple";
  private static final String HELLO_CC = "hello.cc";
  private static final String ISEVEN_H = "iseven.h";
  private static final String ISEVEN_CPP = "iseven.cpp";
  private static final String HELLO_CPP = "hello.cpp";

  private static final String LS_INIT_MESSAGE =
      "Finished language servers initialization, file path '/console-cpp-simple/hello.cc";
  private static final String HELLO_CC_CONTENT =
      "// Simple Hello World\n"
          + " \n"
          + "#include <iostream>\n"
          + " \n"
          + "int main()\n"
          + "{\n"
          + "  std::cout << \"Hello World!\" << std::endl;\n"
          + "  return 0;\n"
          + "}";
  private static final String ISEVEN_H_CONTENT =
      "#ifndef VARIABLE\n"
          + "#define VARIABLE\n"
          + "\n"
          + "int isEven(int arg);\n"
          + "\n"
          + "#endif";
  private static final String ISEVEN_CPP_CONTENT =
      "int isEven(int x) {\n" + "    return x % 2 == 0;\n" + "}";
  private static final String HELLO_CPP_CONTENT =
      "#include <iostream>\n"
          + "#include \"iseven.h\"\n"
          + "\n"
          + "void test(int);\n"
          + "\n"
          + "int main()\n"
          + "{\n"
          + "  int x = 4;\n"
          + "  std::cout << \"Hello World!\" << std::endl;\n"
          + "  std::cout << isEven(x) << std::endl;\n"
          + "  return 0;\n"
          + "}";

  @InjectTestWorkspace(template = WorkspaceTemplate.ECLIPSE_CPP_GCC)
  private TestWorkspace workspace;

  @Inject private Ide ide;
  @Inject private Menu menu;
  @Inject private Wizard wizard;
  @Inject private Consoles consoles;
  @Inject private CodenvyEditor editor;
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
    wizard.selectProjectAndCreate(CONSOLE_CPP_SIMPLE, PROJECT_NAME);
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    projectExplorer.openItemByPath(PROJECT_NAME);
    projectExplorer.openItemByPath(PROJECT_NAME + "/hello.cc");
    editor.waitTabIsPresent("hello.cc");

    // check clangd language sever initialized
    consoles.selectProcessByTabName("dev-machine");
    consoles.waitExpectedTextIntoConsole(LS_INIT_MESSAGE);
  }

  @Test(priority = 1)
  public void checkMarkers() throws Exception {
    editor.selectTabByName(HELLO_CC);
    testProjectServiceClient.updateFile(
        workspace.getId(), PROJECT_NAME + "/" + HELLO_CC, HELLO_CC_CONTENT);

    // check error marker message
    editor.goToCursorPositionVisible(5, 1);
    editor.waitMarkerInvisibility(ERROR, 5);
    editor.typeTextIntoEditor("c");
    editor.waitMarkerInPosition(ERROR, 5);
    editor.typeTextIntoEditor(Keys.DELETE.toString());
    editor.waitAllMarkersInvisibility(ERROR);
  }

  @Test(priority = 1)
  public void checkAutocompleteFeature() throws Exception {
    editor.selectTabByName(HELLO_CC);
    testProjectServiceClient.updateFile(
        workspace.getId(), PROJECT_NAME + "/" + HELLO_CC, HELLO_CC_CONTENT);

    // check contents of autocomplete container
    editor.goToPosition(7, 1);
    editor.deleteCurrentLineAndInsertNew();
    editor.typeTextIntoEditor("std::cou");
    editor.launchAutocompleteAndWaitContainer();
    editor.waitTextIntoAutocompleteContainer("cout ostream");
    editor.waitTextIntoAutocompleteContainer("wcout wostream");
    editor.closeAutocomplete();
  }

  @Test(priority = 1)
  public void checkFindDefinitionFeature() throws Exception {
    prepareFile(ISEVEN_H, ISEVEN_H_CONTENT);
    editor.closeFileByNameWithSaving(ISEVEN_H);
    prepareFile(ISEVEN_CPP, ISEVEN_CPP_CONTENT);
    editor.closeFileByNameWithSaving(ISEVEN_CPP);
    prepareFile(HELLO_CPP, HELLO_CPP_CONTENT);

    editor.selectTabByName(HELLO_CPP);
    editor.waitActive();

    // check Find Definition feature from Assistant menu
    editor.goToPosition(10, 17);
    menu.runCommand(ASSISTANT, FIND_DEFINITION);
    editor.waitTabIsPresent(ISEVEN_H);
    editor.clickOnCloseFileIcon(ISEVEN_H);

    // check Find Definition feature by pressing F4
    editor.goToPosition(10, 17);
    editor.typeTextIntoEditor(F4.toString());
    editor.waitTabIsPresent(ISEVEN_H);
  }

  private void prepareFile(String fileName, String text) throws Exception {
    createFile(fileName);
    editor.selectTabByName(fileName);
    editor.deleteAllContent();
    testProjectServiceClient.updateFile(workspace.getId(), PROJECT_NAME + "/" + fileName, text);
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
