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

import static java.lang.String.format;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.CREATE_PROJECT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.WORKSPACE;
import static org.eclipse.che.selenium.core.workspace.WorkspaceTemplate.NODEJS_WITH_JSON_LS;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.ERROR;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.workspace.InjectTestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
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
public class JsonFileEditingTest {

  private static final String PROJECT_NAME = "nodejs-hello-world";
  private static final String JSON_FILE_NAME = "package.json";
  private static final String PATH_TO_JSON_FILE = PROJECT_NAME + "/" + JSON_FILE_NAME;
  private static final String LS_INIT_MESSAGE =
      format("Finished language servers initialization, file path '/%s'", PATH_TO_JSON_FILE);

  @InjectTestWorkspace(template = NODEJS_WITH_JSON_LS)
  private TestWorkspace workspace;

  @Inject private Ide ide;
  @Inject private Menu menu;
  @Inject private Wizard wizard;
  @Inject private Consoles consoles;
  @Inject private CodenvyEditor editor;
  @Inject private ProjectExplorer projectExplorer;

  @BeforeClass
  public void setUp() throws Exception {
    ide.open(workspace);
    createProjectFromWizard();
  }

  @Test
  public void checkLanguageServerInitialized() {
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    projectExplorer.openItemByPath(PROJECT_NAME);
    projectExplorer.openItemByPath(PATH_TO_JSON_FILE);
    editor.waitTabIsPresent(JSON_FILE_NAME);

    // check JSON language sever initialized
    consoles.selectProcessByTabName("dev-machine");
    consoles.waitExpectedTextIntoConsole(LS_INIT_MESSAGE);
  }

  @Test(priority = 1)
  public void checkCodeValidationFeature() {
    editor.selectTabByName(JSON_FILE_NAME);

    // delete ',' and check error marker with message
    editor.goToCursorPositionVisible(8, 5);
    editor.typeTextIntoEditor(Keys.BACK_SPACE.toString());
    editor.waitMarkerInPosition(ERROR, 9);
    editor.moveToMarkerAndWaitAssistContent(ERROR);
    editor.waitTextIntoAnnotationAssist("Expected '(end)' and instead saw ':'.");

    // return ',' and check error marker invisibility
    editor.goToCursorPositionVisible(8, 4);
    editor.typeTextIntoEditor(",");
    editor.waitAllMarkersInvisibility(ERROR);

    // add new object
    editor.goToCursorPositionVisible(9, 16);
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.typeTextIntoEditor("\"newObj\":[1,2,3],");
    editor.waitAllMarkersInvisibility(ERROR);

    // add duplicated object and check error marker with message
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.typeTextIntoEditor("\"newObj\":[1,2,3],");
    editor.waitMarkerInPosition(ERROR, 11);
    editor.moveToMarkerAndWaitAssistContent(ERROR);
    editor.waitTextIntoAnnotationAssist("Duplicate key 'newObj'.");

    // delete the duplicated object and check error marker invisibility
    editor.deleteCurrentLine();
    editor.waitAllMarkersInvisibility(ERROR);

    // TODO add duplicated object in other {block} and check there is not error marker
    editor.goToCursorPositionVisible(6, 15);
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.typeTextIntoEditor("\"newObj\":[1,2,3],");
    editor.waitAllMarkersInvisibility(ERROR);
  }

  private void createProjectFromWizard() {
    projectExplorer.waitProjectExplorer();
    menu.runCommand(WORKSPACE, CREATE_PROJECT);
    wizard.selectSample(Wizard.SamplesName.NODEJS_HELLO_WORLD);
    wizard.typeProjectNameOnWizard(PROJECT_NAME);
    wizard.clickCreateButton();
    wizard.waitCloseProjectConfigForm();
  }
}
