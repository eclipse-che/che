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
package org.eclipse.che.selenium.languageserver.python;

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.ASSISTANT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.FIND_REFERENCES;
import static org.eclipse.che.selenium.core.workspace.WorkspaceTemplate.PYTHON;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.InjectTestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.FindReferencesConsoleTab;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class PythonAssistantFeatureTest {
  private static final String PROJECT_NAME = "console-python3-simple";
  private static final String CALC_TAB_NAME = "calc.py";
  private static final String MAIN_TAB_NAME = "main.py";
  private static final String RENAMED_VARIABLE_NAME = "renamedVar";
  private static final String EXPECTED_CODE_BEFORE_COMMENTING = "var2 = module.add(100, 200)";
  private static final String EXPECTED_COMMENTED_CODE = "#var2 = module.add(100, 200)";
  private static final String EXPECTED_HOVER_TEXT = "function(self)";
  private static final String EXPECTED_FIND_REFERENCE_NODE_TEXT =
      "/console-python3-simple/calc.py\n" + "From:16:1 To:16:5";
  private static final String EXPECTED_TEXT_AFTER_RENAME =
      "class MyClass:\n" + "    renamedVar = 1\n" + "    variable = \"variable\"";

  @InjectTestWorkspace(template = PYTHON)
  private TestWorkspace workspace;

  @Inject private Ide ide;
  @Inject private Menu menu;
  @Inject private Consoles consoles;
  @Inject private CodenvyEditor editor;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private FindReferencesConsoleTab findReferencesConsoleTab;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = PythonFileEditingTest.class.getResource("/projects/console-python3-simple");
    testProjectServiceClient.importProject(
        workspace.getId(), Paths.get(resource.toURI()), PROJECT_NAME, ProjectTemplates.PYTHON);
    ide.open(workspace);
    projectExplorer.waitProjectExplorer();
    projectExplorer.openItemByPath(PROJECT_NAME);
  }

  @Test
  public void checkCommenting() {
    projectExplorer.waitProjectExplorer();

    projectExplorer.openItemByPath(PROJECT_NAME + "/" + CALC_TAB_NAME);
    editor.waitTabIsPresent(CALC_TAB_NAME);
    editor.waitActive();
    editor.waitTextIntoEditor(EXPECTED_CODE_BEFORE_COMMENTING);

    editor.setCursorToLine(16);
    editor.launchCommentCodeFeature();
    editor.waitTextIntoEditor(EXPECTED_COMMENTED_CODE);
    editor.launchCommentCodeFeature();
    editor.waitTextIntoEditor(EXPECTED_CODE_BEFORE_COMMENTING);
  }

  @Test
  public void checkRenaming() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.openItemByPath(PROJECT_NAME + "/" + MAIN_TAB_NAME);
    editor.waitTabIsPresent(MAIN_TAB_NAME);
    editor.waitActive();

    editor.goToCursorPositionVisible(15, 6);
    editor.launchLocalRefactor();
    editor.doRenamingByLanguageServerField(RENAMED_VARIABLE_NAME);
    editor.waitTextIntoEditor(EXPECTED_TEXT_AFTER_RENAME);
  }

  @Test
  public void checkHoverFeature() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.openItemByPath(PROJECT_NAME + "/" + MAIN_TAB_NAME);
    editor.waitTabIsPresent(MAIN_TAB_NAME);
    editor.waitActive();

    editor.goToCursorPositionVisible(18, 11);
    // <<<< ---- hoverPopupEqualsTo(EXPECTED_HOVER_TEXT);
  }

  @Test
  public void checkFindReferenceFeature() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.openItemByPath(PROJECT_NAME + "/" + CALC_TAB_NAME);
    editor.waitTabIsPresent(CALC_TAB_NAME);
    editor.waitActive();

    editor.goToCursorPositionVisible(16, 2);
    menu.runCommand(ASSISTANT, FIND_REFERENCES);
    waitReferenceWithText(EXPECTED_FIND_REFERENCE_NODE_TEXT);
  }

  @Test
  public void checkSignatureHelpFeature() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.openItemByPath(PROJECT_NAME + "/" + CALC_TAB_NAME);
    editor.waitTabIsPresent(CALC_TAB_NAME);
    editor.waitActive();
  }

  private void waitReferenceWithText(String expectedText) {
    try {
      findReferencesConsoleTab.waitReferenceWithText(expectedText);
    } catch (org.openqa.selenium.TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known permanent failure https://github.com/eclipse/che/issues/10698", ex);
    }
  }
}
