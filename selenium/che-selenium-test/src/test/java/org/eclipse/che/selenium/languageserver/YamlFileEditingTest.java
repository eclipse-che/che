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

import static java.lang.String.format;
import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Profile.PREFERENCES;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Profile.PROFILE_MENU;
import static org.eclipse.che.selenium.core.project.ProjectTemplates.NODE_JS;
import static org.eclipse.che.selenium.core.workspace.WorkspaceTemplate.ECLIPSE_NODEJS_YAML;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.ERROR;
import static org.eclipse.che.selenium.pageobject.Preferences.DropDownLanguageServerSettings.YAML;
import static org.openqa.selenium.Keys.DELETE;
import static org.openqa.selenium.Keys.ENTER;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project.New;
import org.eclipse.che.selenium.core.workspace.InjectTestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.Preferences;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class YamlFileEditingTest {

  private static final String PROJECT_NAME = generate("project", 4);
  private static final String YAML_FILE_NAME = "openshift.yaml";
  private static final String PATH_TO_YAML_FILE = PROJECT_NAME + "/" + YAML_FILE_NAME;
  private static final String LS_INIT_MESSAGE =
      format("Finished language servers initialization, file path '/%s'", PATH_TO_YAML_FILE);

  private static final String COMMENTED_CODE =
      "#  generation: 4\n"
          + "  labels:\n"
          + "#    app: che\n"
          + "    template: che\n"
          + "#  name: che";
  private static final String UNCOMMENTED_CODE =
      "  generation: 4\n"
          + "  labels:\n"
          + "    app: che\n"
          + "    template: che\n"
          + "  name: che";

  @InjectTestWorkspace(template = ECLIPSE_NODEJS_YAML)
  private TestWorkspace workspace;

  @Inject private Ide ide;
  @Inject private Menu menu;
  @Inject private Consoles consoles;
  @Inject private CodenvyEditor editor;
  @Inject private Preferences preferences;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private AskForValueDialog askForValueDialog;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = YamlFileEditingTest.class.getResource("/projects/nodejs-with-yaml");
    testProjectServiceClient.importProject(
        workspace.getId(), Paths.get(resource.toURI()), PROJECT_NAME, NODE_JS);
    ide.open(workspace);

    ide.waitOpenedWorkspaceIsReadyToUse();

    addYamlSchema();
  }

  @Test
  public void checkLanguageServerInitialized() {
    projectExplorer.waitAndSelectItem(PROJECT_NAME);

    menu.runCommand(Project.PROJECT, New.NEW, New.FILE);
    askForValueDialog.createNotJavaFileByName(YAML_FILE_NAME);
    editor.waitTabIsPresent(YAML_FILE_NAME);

    projectExplorer.openItemByPath(PROJECT_NAME + "/deployment.yaml");
    editor.waitTabIsPresent("deployment.yaml");

    // check Yaml language server initialized
    consoles.selectProcessByTabName("dev-machine");
    consoles.waitExpectedTextIntoConsole(LS_INIT_MESSAGE);
  }

  @Test(priority = 1)
  public void checkAutocompleteFeature() {
    editor.selectTabByName(YAML_FILE_NAME);

    // launch autocomplete feature and check proposal documentation
    editor.launchAutocompleteAndWaitContainer();
    editor.waitTextIntoAutocompleteContainer("diskName");
    editor.selectAutocompleteProposal("diskName");
    editor.checkProposalDocumentation("The Name of the data disk in the blob storage");
    editor.waitTextIntoAutocompleteContainer("diskURI");
    editor.selectAutocompleteProposal("diskURI");
    editor.checkProposalDocumentation("The URI the data disk in the blob storage");

    // select proposal and check expected text in the Editor
    editor.enterAutocompleteProposal("kind");
    editor.launchAutocompleteAndWaitContainer();
    editor.waitTextIntoAutocompleteContainer("PersistentVolume");
    editor.enterAutocompleteProposal("PersistentVolume");
    editor.waitTextIntoEditor("kind: PersistentVolume");
    editor.typeTextIntoEditor(ENTER.toString());
    editor.typeTextIntoEditor(ENTER.toString());

    // launch autocomplete feature and check expected text in the Editor
    editor.typeTextIntoEditor("api");
    editor.launchAutocomplete();
    editor.waitTextIntoEditor("apiVersion: ");
    editor.launchAutocomplete();
    editor.waitTextIntoEditor("apiVersion: v1");
    editor.typeTextIntoEditor(ENTER.toString());
    editor.typeTextIntoEditor(ENTER.toString());

    // launch autocomplete feature, select proposal and check expected text in the Editor
    editor.typeTextIntoEditor("me");
    editor.launchAutocomplete();
    editor.waitTextIntoEditor("metadata:");

    editor.launchAutocompleteAndWaitContainer();
    editor.waitTextIntoAutocompleteContainer("status");
    editor.enterAutocompleteProposal("status");
    editor.waitMarkerInPosition(ERROR, 4);
    editor.moveToMarkerAndWaitAssistContent(ERROR);
    editor.waitTextIntoAnnotationAssist("Using tabs can lead to unpredictable results");

    editor.goToPosition(4, 1);
    editor.typeTextIntoEditor(DELETE.toString());
    editor.waitMarkerInvisibility(ERROR, 4);

    editor.goToPosition(5, 1);
    editor.launchAutocomplete();
    editor.waitTextIntoEditor("spec:");
  }

  @Test(priority = 1)
  public void checkHoverFeature() {
    editor.selectTabByName("deployment.yaml");

    // move cursor on text and check expected text in hover popup
    editor.moveCursorToText("namespace:");
    editor.waitTextInHoverPopup("Namespace defines the space within each name must be unique.");

    editor.moveCursorToText("kind:");
    editor.waitTextInHoverPopup(
        "Kind is a string value representing the REST resource this object represents.");

    editor.moveCursorToText("apiVersion:");
    editor.waitTextInHoverPopup(
        "APIVersion defines the versioned schema of this representation of an object.");
  }

  @Test(priority = 1)
  public void checkCodeValidation() {
    editor.selectTabByName("deployment.yaml");
    editor.waitAllMarkersInvisibility(ERROR);

    editor.goToPosition(12, 2);
    editor.typeTextIntoEditor("a");
    editor.moveCursorToText("aapiVersion");
    editor.waitTextInHoverPopup("Unexpected property aapiVersion");

    editor.goToPosition(12, 1);
    editor.typeTextIntoEditor(DELETE.toString());
    editor.waitAllMarkersInvisibility(ERROR);
    editor.moveCursorToText("apiVersion:");
    editor.waitTextInHoverPopup(
        "APIVersion defines the versioned schema of this representation of an object.");
  }

  @Test(priority = 1)
  public void checkCommentCodeFeature() {
    editor.selectTabByName("deployment.yaml");

    // comment lines by Ctrl+'/' buttons
    editor.goToPosition(18, 1);
    editor.launchCommentCodeFeature();
    editor.waitTextIntoEditor("#  generation: 4");
    editor.goToPosition(20, 1);
    editor.launchCommentCodeFeature();
    editor.goToPosition(22, 1);
    editor.launchCommentCodeFeature();
    editor.waitTextIntoEditor(COMMENTED_CODE);

    // uncomment all commented lines
    editor.goToPosition(18, 1);
    editor.launchCommentCodeFeature();
    editor.goToPosition(20, 1);
    editor.launchCommentCodeFeature();
    editor.goToPosition(22, 1);
    editor.launchCommentCodeFeature();
    editor.waitTextIntoEditor(UNCOMMENTED_CODE);
  }

  private void addYamlSchema() {
    menu.runCommand(PROFILE_MENU, PREFERENCES);
    preferences.waitPreferencesForm();

    preferences.waitMenuInCollapsedDropdown(YAML);
    preferences.selectDroppedMenuByName(YAML);

    preferences.clickOnAddSchemaUrlButton();
    preferences.addSchemaUrl("kubernetes");
    preferences.clickOnOkBtn();

    preferences.closeForm();
  }
}
