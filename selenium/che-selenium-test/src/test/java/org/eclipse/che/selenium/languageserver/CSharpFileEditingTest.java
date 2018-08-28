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

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.CREATE_PROJECT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.WORKSPACE;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.ERROR;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.INFO;
import static org.eclipse.che.selenium.pageobject.Wizard.SamplesName.ASP_DOT_NET_WEB_SIMPLE;
import static org.openqa.selenium.Keys.BACK_SPACE;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestCommandServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.workspace.InjectTestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.core.workspace.WorkspaceTemplate;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsPalette;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class CSharpFileEditingTest {

  private final String PROJECT_NAME = NameGenerator.generate("AspProject", 4);
  private final String LANGUAGE_SERVER_INIT_MESSAGE =
      "Finished language servers initialization, file path";
  private final String NAME_OF_EDITING_FILE = "Program.cs";

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
  @Inject private TestWorkspaceServiceClient testWorkspaceServiceClient;
  @Inject private Consoles consoles;

  @BeforeClass
  public void setUp() throws Exception {
    ide.open(workspace);
    createDotNetAppFromWizard();
    initLanguageServer();
  }

  @AfterMethod
  public void restartWorkspace() throws Exception {
    editor.closeAllTabs();
    testWorkspaceServiceClient.stop(workspace.getName(), workspace.getOwner().getName());
    ide.open(workspace);
    ide.waitOpenedWorkspaceIsReadyToUse();
    initLanguageServer();
  }

  @Test
  public void checkCodeEditing() {
    checkCodeValidation();
  }

  @Test(priority = 1)
  public void checkInitializingAfterFirstStarting() {
    projectExplorer.openItemByPath(PROJECT_NAME + "/Program.cs");

    try {
      editor.waitMarkerInPosition(INFO, 1);
      editor.waitMarkerInPosition(INFO, 2);
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known issue: https://github.com/eclipse/che/issues/10151", ex);
    }
  }

  public void checkCodeValidation() {
    editor.goToCursorPositionVisible(24, 12);
    for (int i = 0; i < 9; i++) {
      editor.typeTextIntoEditor(BACK_SPACE.toString());
    }

    try {
      editor.waitMarkerInPosition(INFO, 23);
    } catch (TimeoutException ex) {
      fail("Known issue: https://github.com/eclipse/che/issues/10789", ex);
    }

    editor.waitMarkerInPosition(ERROR, 21);
    checkAutocompletion();
  }

  private void initLanguageServer() {
    projectExplorer.quickRevealToItemWithJavaScript(PROJECT_NAME + "/" + NAME_OF_EDITING_FILE);
    projectExplorer.openItemByPath(PROJECT_NAME + "/" + NAME_OF_EDITING_FILE);
    consoles.selectProcessByTabName("dev-machine");
    consoles.waitExpectedTextIntoConsole(LANGUAGE_SERVER_INIT_MESSAGE);
    editor.selectTabByName(NAME_OF_EDITING_FILE);
  }

  private void checkAutocompletion() {
    editor.goToCursorPositionVisible(23, 49);
    editor.typeTextIntoEditor(".");
    editor.launchAutocomplete();
    editor.enterAutocompleteProposal("Build ");
    editor.typeTextIntoEditor("();");
    editor.waitAllMarkersInvisibility(ERROR);
  }

  private void createDotNetAppFromWizard() {
    projectExplorer.waitProjectExplorer();
    menu.runCommand(WORKSPACE, CREATE_PROJECT);
    wizard.selectSample(ASP_DOT_NET_WEB_SIMPLE);
    wizard.typeProjectNameOnWizard(PROJECT_NAME);
    wizard.clickCreateButton();
    wizard.waitCloseProjectConfigForm();

    projectExplorer.waitProjectInitialization(PROJECT_NAME);
  }
}
