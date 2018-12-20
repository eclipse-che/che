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
package org.eclipse.che.selenium.projectexplorer;

import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuFirstLevelItems.NEW;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.SubMenuNew.C_FILE;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.SubMenuNew.C_PLUS_PLUS_FILE;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.SubMenuNew.H_FILE;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.SubMenuNew.PYTHON_FILE;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuItems;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class CreateProjectsForArtikPluginTest {
  private static final String NAME_C_PROJECT = "C-project";
  private static final String NAME_CPP_PROJECT = "C-plus-plus";
  private static final String NAME_PYTHON_PROJECT = "Python-project";

  private static final String TYPE_OF_PROJECT_C = "C";
  private static final String TYPE_OF_PROJECT_CPP = "C++";
  private static final String TYPE_OF_PROJECT_PYTHON = "Python";

  private static final String NAME_C_FILE = "newC";
  private static final String NAME_CPP_FILE = "newCPlusPlus";
  private static final String NAME_H_FILE = "newH";
  private static final String NAME_PYTHON_FILE = "newPython";

  private static final String TEXT_IN_CPP_FILE =
      "#include <iostream>\n"
          + "\n"
          + "int main()\n"
          + "{\n"
          + "    std::cout << \"Hello, world!\\n\";\n"
          + "    return 0;\n"
          + "}";
  private static final String TEXT_IN_C_FILE =
      "#include <stdio.h>\n"
          + "\n"
          + "int main(void)\n"
          + "{\n"
          + "    printf(\"hello, world\\n\");\n"
          + "}";
  private static final String TEXT_IN_H_FILE =
      "#ifndef VARIABLE\n" + "#define VARIABLE\n" + "// Write your header file here.\n" + "#endif";
  private static final String TEXT_IN_PYTHON_FILE = "";

  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private AskForValueDialog askForValueDialog;
  @Inject private Wizard projectWizard;
  @Inject private Menu menu;

  @BeforeClass
  public void setUp() throws Exception {
    ide.open(testWorkspace);
    ide.waitOpenedWorkspaceIsReadyToUse();
  }

  @Test
  public void createCProjectTest() throws Exception {
    createProject(NAME_C_PROJECT, TYPE_OF_PROJECT_C);
    createAndCheckNewFile(NAME_C_PROJECT, NAME_C_FILE, C_FILE, ".c");
    checkTextInEditorForFile(TEXT_IN_C_FILE, NAME_C_FILE + ".c");
    createAndCheckNewFile(NAME_C_PROJECT, NAME_H_FILE, H_FILE, ".h");
    checkTextInEditorForFile(TEXT_IN_H_FILE, NAME_H_FILE + ".h");
    createAndCheckNewFile(NAME_C_PROJECT, NAME_CPP_FILE, C_PLUS_PLUS_FILE, ".cpp");
    checkTextInEditorForFile(TEXT_IN_CPP_FILE, NAME_CPP_FILE + ".cpp");
  }

  @Test
  public void createCPlusPlusProjectTest() throws Exception {
    createProject(NAME_CPP_PROJECT, TYPE_OF_PROJECT_CPP);
    createAndCheckNewFile(NAME_CPP_PROJECT, NAME_C_FILE, C_FILE, ".c");
    checkTextInEditorForFile(TEXT_IN_C_FILE, NAME_C_FILE + ".c");
    createAndCheckNewFile(NAME_CPP_PROJECT, NAME_H_FILE, H_FILE, ".h");
    checkTextInEditorForFile(TEXT_IN_H_FILE, NAME_H_FILE + ".h");
    createAndCheckNewFile(NAME_CPP_PROJECT, NAME_CPP_FILE, C_PLUS_PLUS_FILE, ".cpp");
    checkTextInEditorForFile(TEXT_IN_CPP_FILE, NAME_CPP_FILE + ".cpp");
  }

  @Test
  public void createPythonProjectTest() throws Exception {
    createProject(NAME_PYTHON_PROJECT, TYPE_OF_PROJECT_PYTHON);
    createAndCheckNewFile(NAME_PYTHON_PROJECT, NAME_PYTHON_FILE, PYTHON_FILE, ".py");
    checkTextInEditorForFile(TEXT_IN_PYTHON_FILE, NAME_PYTHON_FILE + ".py");
  }

  private void createProject(String projectName, String projectType) {
    loader.waitOnClosed();
    projectExplorer.waitProjectExplorer();
    menu.runCommand(
        TestMenuCommandsConstants.Workspace.WORKSPACE,
        TestMenuCommandsConstants.Workspace.CREATE_PROJECT);
    projectWizard.selectSample(projectType);
    projectWizard.typeProjectNameOnWizard(projectName);
    projectWizard.clickCreateButton();
    loader.waitOnClosed();
  }

  private void createAndCheckNewFile(
      String projectName, String fileName, ContextMenuItems type, String fileExt) {

    projectExplorer.waitAndSelectItem(projectName);
    projectExplorer.openContextMenuByPathSelectedItem(projectName);
    projectExplorer.clickOnItemInContextMenu(NEW);
    projectExplorer.clickOnItemInContextMenu(type);
    askForValueDialog.waitFormToOpen();
    askForValueDialog.typeAndWaitText(fileName);
    askForValueDialog.clickOkBtn();
    loader.waitOnClosed();
    projectExplorer.waitVisibilityByName(fileName + fileExt);
    projectExplorer.openItemByPath(projectName + "/" + fileName + fileExt);
  }

  private void checkTextInEditorForFile(String defaultText, String fileName)
      throws InterruptedException {
    editor.waitActive();
    editor.waitTabIsPresent(fileName);
    editor.waitTextIntoEditor(defaultText);
  }
}
