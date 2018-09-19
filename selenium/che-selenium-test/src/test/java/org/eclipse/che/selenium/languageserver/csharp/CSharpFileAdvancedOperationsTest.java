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
package org.eclipse.che.selenium.languageserver.csharp;

import static org.eclipse.che.selenium.core.constant.TestCommandsConstants.FINISH_LANGUAGE_SERVER_INITIALIZATION_MESSAGE;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.ASSISTANT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.FIND_DEFINITION;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.GO_TO_SYMBOL;
import static org.eclipse.che.selenium.core.project.ProjectTemplates.DOT_NET;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestCommandServiceClient;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.workspace.InjectTestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.core.workspace.WorkspaceTemplate;
import org.eclipse.che.selenium.pageobject.AssistantFindPanel;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsPalette;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CSharpFileAdvancedOperationsTest {
  private static final String PROJECT_NAME =
      NameGenerator.generate(CSharpClassRenamingTest.class.getSimpleName(), 4);

  private static final String PATH_TO_DOT_NET_FILE = PROJECT_NAME + "/Hello.cs";

  @InjectTestWorkspace(template = WorkspaceTemplate.UBUNTU_LSP)
  private TestWorkspace workspace;

  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private Menu menu;
  @Inject private Wizard wizard;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private TestCommandServiceClient testCommandServiceClient;
  @Inject private CommandsPalette commandsPalette;
  @Inject private Consoles consoles;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private AssistantFindPanel assistantFindPanel;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/CSharpFileAdvancedOperations");
    testProjectServiceClient.importProject(
        workspace.getId(), Paths.get(resource.toURI()), PROJECT_NAME, DOT_NET);
    ide.open(workspace);
    projectExplorer.openItemByPath(PROJECT_NAME);
    projectExplorer.openItemByPath(PATH_TO_DOT_NET_FILE);

    // after opening the file we are checking initializing message from LS and than check, that
    // dependencies have been added properly in this case
    // folders obj and bin should appear in the Project tree
    consoles.waitExpectedTextIntoConsole(FINISH_LANGUAGE_SERVER_INITIALIZATION_MESSAGE);
    projectExplorer.waitItem(PROJECT_NAME + "/obj");
    projectExplorer.waitItem(PROJECT_NAME + "/bin");
  }

  @Test(alwaysRun = true)
  public void checkHoveringFeature() {
    String expectedTextInHoverPopUp =
        "System.Console\nRepresents the standard input, output, and error streams for console applications. This class cannot be inherited.";
    editor.moveCursorToText("Console");
    try {

      editor.waitTextInHoverPopUpEqualsTo(expectedTextInHoverPopUp);
    } catch (TimeoutException ex) {
      fail("Known permanent failure: https://github.com/eclipse/che/issues/10117", ex);
    }
  }

  @Test(priority = 1, alwaysRun = true)
  public void checkFindDefinition() {
    // check Find definition from Test.getStr()
    editor.goToCursorPositionVisible(21, 18);
    menu.runCommand(ASSISTANT, FIND_DEFINITION);
    editor.waitTabIsPresent("Test.cs");
    editor.waitCursorPosition(18, 22);
  }

  @Test(priority = 2, alwaysRun = true)
  public void checkCodeCommentFeature() {
    editor.goToPosition(17, 1);
    editor.launchCommentCodeFeature();
    editor.waitTextIntoEditor("//private counter = 5;");
    editor.typeTextIntoEditor(Keys.END.toString());
  }

  @Test(priority = 3, alwaysRun = true)
  public void checkGoToSymbolFeature() {
    menu.runCommand(ASSISTANT, GO_TO_SYMBOL);
    try {
      assistantFindPanel.waitActionNodeContainsText("Main(string[] args)");
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known permanent failure: https://github.com/eclipse/che/issues/11258", ex);
    }
  }
}
