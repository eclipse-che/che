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
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.ASSISTANT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.GO_TO_SYMBOL;
import static org.eclipse.che.selenium.core.project.ProjectTemplates.CONSOLE_JAVA_SIMPLE;
import static org.eclipse.che.selenium.core.workspace.WorkspaceTemplate.APACHE_CAMEL;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.workspace.InjectTestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AssistantFindPanel;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Skoryk Serhii */
public class ApacheCamelFileEditingTest {

  private static final String PROJECT_NAME = "project-for-camel-ls";
  private static final String CAMEL_FILE_NAME = "camel.xml";
  private static final String PATH_TO_CAMEL_FILE = PROJECT_NAME + "/" + CAMEL_FILE_NAME;
  private static final String LS_INIT_MESSAGE =
      format("Finished language servers initialization, file path '/%s'", PATH_TO_CAMEL_FILE);
  private static final String[] EXPECTED_GO_TO_SYMBOL_ALTERNATIVES = {
    "<no id>symbols (2)", "<no id>"
  };

  @InjectTestWorkspace(template = APACHE_CAMEL)
  private TestWorkspace workspace;

  @Inject private Ide ide;
  @Inject private Menu menu;
  @Inject private Consoles consoles;
  @Inject private CodenvyEditor editor;
  @Inject private AssistantFindPanel assistantFindPanel;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = ApacheCamelFileEditingTest.class.getResource("/projects/project-for-camel-ls");
    testProjectServiceClient.importProject(
        workspace.getId(), Paths.get(resource.toURI()), PROJECT_NAME, CONSOLE_JAVA_SIMPLE);
    ide.open(workspace);
  }

  @Test
  public void checkLanguageServerInitialized() {
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    projectExplorer.openItemByPath(PROJECT_NAME);
    projectExplorer.openItemByPath(PATH_TO_CAMEL_FILE);
    editor.waitTabIsPresent(CAMEL_FILE_NAME);

    // check Apache Camel language server initialized
    consoles.selectProcessByTabName("dev-machine");
    consoles.waitExpectedTextIntoConsole(LS_INIT_MESSAGE);
  }

  @Test(priority = 1)
  public void checkAutocompleteFeature() {
    editor.selectTabByName(CAMEL_FILE_NAME);

    editor.goToPosition(50, 21);

    editor.launchAutocomplete();
    editor.waitTextIntoEditor("timer:timerName");

    // launch autocomplete feature, select proposal and check expected text in the Editor
    editor.typeTextIntoEditor("?");
    editor.launchAutocompleteAndWaitContainer();
    editor.waitProposalIntoAutocompleteContainer("fixedRate ");
    editor.enterAutocompleteProposal("fixedRate ");
    editor.waitTextIntoEditor("timer:timerName?fixedRate=false");

    editor.typeTextIntoEditor("&amp;");
    editor.launchAutocompleteAndWaitContainer();
    editor.waitProposalIntoAutocompleteContainer("exchangePattern ");
    editor.enterAutocompleteProposal("exchangePattern ");
    editor.waitTextIntoEditor("timer:timerName?fixedRate=false&amp;exchangePattern=");

    editor.launchAutocompleteAndWaitContainer();
    editor.waitProposalIntoAutocompleteContainer("InOnly");
    editor.enterAutocompleteProposal("InOnly");
    editor.waitTextIntoEditor("timer:timerName?fixedRate=false&amp;exchangePattern=InOnly");
  }

  @Test(priority = 1)
  public void checkHoverFeature() {
    // move cursor on text and check expected text in hover popup
    editor.moveCursorToText("velocity");
    editor.waitTextInHoverPopup("Transforms the message using a Velocity template.");
  }

  @Test(priority = 1)
  public void checkGoToSymbolFeature() {
    editor.selectTabByName(CAMEL_FILE_NAME);
    editor.waitActive();

    // open and close 'Go To Symbol' panel by keyboard
    editor.enterCtrlF12();
    assistantFindPanel.waitForm();
    editor.cancelFormInEditorByEscape();
    assistantFindPanel.waitFormIsClosed();

    // select the item by click
    menu.runCommand(ASSISTANT, GO_TO_SYMBOL);
    assistantFindPanel.waitForm();
    assistantFindPanel.waitAllNodes(EXPECTED_GO_TO_SYMBOL_ALTERNATIVES);
    assistantFindPanel.clickOnActionNodeWithTextContains("<no id>symbols (2)");
    assistantFindPanel.waitFormIsClosed();
    editor.waitCursorPosition(49, 12);

    // select the item by keyboard
    editor.enterCtrlF12();
    assistantFindPanel.waitForm();
    editor.pressArrowDown();
    editor.pressArrowDown();
    assistantFindPanel.waitActionNodeSelection("<no id>");
    editor.pressEnter();
    assistantFindPanel.waitFormIsClosed();
    editor.waitCursorPosition(63, 12);
  }
}
