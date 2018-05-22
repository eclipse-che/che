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

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project.New.FILE;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project.New.NEW;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project.PROJECT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.CREATE_PROJECT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.WORKSPACE;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.ERROR;
import static org.eclipse.che.selenium.pageobject.Wizard.SamplesName.CONSOLE_CPP_SIMPLE;

import com.google.inject.Inject;
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
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsPalette;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Skoryk Serhii */
public class CheckMainFeatureForClangLanguageTest {
  private static final String PROJECT_NAME = "console-cpp-simple";
  private static final String CPP_FILE_NAME = "hello.cc";
  private static final String H_MODULE_FILE_NAME = "iseven.h";
  private static final String OTHER_FILE_NAME = "iseven.cpp";
  private static final String LS_INIT_MESSAGE =
      "Finished language servers initialization, file path '/console-cpp-simple/hello.cc";

  @InjectTestWorkspace(template = WorkspaceTemplate.ECLIPSE_CPP_GCC)
  private TestWorkspace workspace;

  @Inject private Ide ide;
  @Inject private Menu menu;
  @Inject private Wizard wizard;
  @Inject private Consoles consoles;
  @Inject private Dashboard dashboard;
  @Inject private CodenvyEditor editor;
  @Inject private CommandsPalette commandsPalette;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private AskForValueDialog askForValueDialog;

  @BeforeClass
  public void setUp() throws Exception {
    ide.open(workspace);
  }

  @Test
  public void checkLanguageServerInitialized() {
    ide.waitOpenedWorkspaceIsReadyToUse();

    menu.runCommand(WORKSPACE, CREATE_PROJECT);
    wizard.selectProjectAndCreate(CONSOLE_CPP_SIMPLE, PROJECT_NAME);

    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.openItemByPath(PROJECT_NAME);
    projectExplorer.openItemByPath(PROJECT_NAME + "/hello.cc");
    editor.waitTabIsPresent("hello.cc");

    consoles.selectProcessByTabName("dev-machine");
    consoles.waitExpectedTextIntoConsole(LS_INIT_MESSAGE);
  }

  @Test(priority = 1)
  public void checkErrorMessages() {
    editor.selectTabByName(CPP_FILE_NAME);
    editor.goToCursorPositionVisible(1, 1);
    editor.waitMarkerInvisibility(ERROR, 1);
    editor.typeTextIntoEditor("c");
    editor.waitMarkerInPosition(ERROR, 1);
    editor.typeTextIntoEditor(Keys.DELETE.toString());
    editor.waitAllMarkersInvisibility(ERROR);
  }

  @Test(priority = 1)
  public void checkAutocompleteFeature() {
    editor.selectTabByName(CPP_FILE_NAME);
    editor.goToPosition(7, 1);

    // check contents of autocomplete container
    editor.deleteCurrentLineAndInsertNew();
    editor.typeTextIntoEditor("std::cou");
    editor.waitMarkerInPosition(ERROR, 7);
    editor.launchAutocompleteAndWaitContainer();

    System.out.println(editor.getAllVisibleTextFromAutocomplete());

    editor.waitTextIntoAutocompleteContainer("cout ostream");
    editor.waitTextIntoAutocompleteContainer("wcout wostream");
    editor.enterAutocompleteProposal("ostream");
    editor.waitTextIntoEditor("  std::cout");
    editor.typeTextIntoEditor("<<\"Hello World!\";");
    editor.waitAllMarkersInvisibility(ERROR);

    commandsPalette.openCommandPalette();
    commandsPalette.startCommandByDoubleClick(PROJECT_NAME + ":build and run");
    consoles.waitTabNameProcessIsPresent(PROJECT_NAME + ":build and run");
    consoles.waitExpectedTextIntoConsole("Hello World!");
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
