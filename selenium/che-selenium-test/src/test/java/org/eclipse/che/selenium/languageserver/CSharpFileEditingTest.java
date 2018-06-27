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

import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.ERROR;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.INFO;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestCommandServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
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
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class CSharpFileEditingTest {

  private final String PROJECT_NAME = NameGenerator.generate("AspProject", 4);
  private final String COMMAND_NAME_FOR_RESTORE_LS = PROJECT_NAME + ": update dependencies";

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
  @Inject TestWorkspaceServiceClient testWorkspaceServiceClient;
  @Inject private Consoles consoles;

  @BeforeClass
  public void setUp() throws Exception {
    ide.open(workspace);
    createDotNetAppFromWizard();
    restoreDependenciesForLanguageServerByCommand();
    projectExplorer.quickRevealToItemWithJavaScript(PROJECT_NAME + "/Program.cs");
  }

  @AfterMethod
  public void restartWorkspace() throws Exception {
    editor.closeAllTabs();
    testWorkspaceServiceClient.stop(workspace.getName(), workspace.getOwner().getName());
    ide.open(workspace);
    ide.waitOpenedWorkspaceIsReadyToUse();
    restoreDependenciesForLanguageServerByCommand();
    projectExplorer.quickRevealToItemWithJavaScript(PROJECT_NAME + "/Program.cs");
  }

  @Test
  public void checkCodeEditing() {
    projectExplorer.openItemByPath(PROJECT_NAME + "/Program.cs");
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
      editor.typeTextIntoEditor(Keys.BACK_SPACE.toString());
    }
    editor.waitMarkerInPosition(INFO, 23);
    editor.waitMarkerInPosition(ERROR, 21);
    checkAutocompletion();
  }

  private void checkAutocompletion() {
    editor.goToCursorPositionVisible(23, 49);
    editor.typeTextIntoEditor(".");
    editor.launchAutocomplete();
    editor.enterAutocompleteProposal("Build ");
    editor.typeTextIntoEditor("();");
    editor.waitAllMarkersInvisibility(INFO);
  }

  private void createDotNetAppFromWizard() {
    projectExplorer.waitProjectExplorer();
    menu.runCommand(
        TestMenuCommandsConstants.Workspace.WORKSPACE,
        TestMenuCommandsConstants.Workspace.CREATE_PROJECT);
    wizard.selectSample(Wizard.SamplesName.ASP_DOT_NET_WEB_SIMPLE);
    wizard.typeProjectNameOnWizard(PROJECT_NAME);
    wizard.clickCreateButton();
    wizard.waitCloseProjectConfigForm();
  }

  private void restoreDependenciesForLanguageServerByCommand() {
    commandsPalette.openCommandPalette();
    commandsPalette.startCommandByDoubleClick(COMMAND_NAME_FOR_RESTORE_LS);
    consoles.waitExpectedTextIntoConsole("Restore completed");
  }
}
