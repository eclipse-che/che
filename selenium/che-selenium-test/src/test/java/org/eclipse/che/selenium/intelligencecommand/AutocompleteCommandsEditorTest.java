/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.intelligencecommand;

import static org.eclipse.che.selenium.core.constant.TestBuildConstants.BUILD_SUCCESS;
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandsDefaultNames.MAVEN_NAME;
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandsGoals.BUILD_GOAL;
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandsTypes.MAVEN_TYPE;
import static org.eclipse.che.selenium.pageobject.intelligent.CommandsEditor.CommandsEditorType.COMMAND_LINE_EDITOR;
import static org.eclipse.che.selenium.pageobject.intelligent.CommandsEditor.CommandsEditorType.PREVIEW_URL_EDITOR;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsEditor;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsExplorer;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev */
public class AutocompleteCommandsEditorTest {
  private static final String PROJ_NAME = NameGenerator.generate("AutocompleteCommandsEditor", 4);
  private static final String PATH_TO_JAVA_FILE =
      PROJ_NAME + "/src/main/java/org/eclipse/qa/examples/AppController.java";
  private static final String MAVEN_COMMAND = "mvn compile -f ${current.project.path}";

  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private Consoles consoles;
  @Inject private CommandsEditor commandsEditor;
  @Inject private CommandsExplorer commandsExplorer;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void prepare() throws Exception {
    URL resource = getClass().getResource("/projects/default-spring-project");
    testProjectServiceClient.importProject(
        testWorkspace.getId(), Paths.get(resource.toURI()), PROJ_NAME, ProjectTemplates.PLAIN_JAVA);
    ide.open(testWorkspace);
  }

  @Test(priority = 1)
  public void checkAutocompleteCommandLine() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJ_NAME);
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.waitItem(PATH_TO_JAVA_FILE);
    projectExplorer.openItemByPath(PATH_TO_JAVA_FILE);
    createMavenCommand();
    commandsEditor.setFocusIntoTypeCommandsEditor(COMMAND_LINE_EDITOR);
    commandsEditor.setCursorToLine(1);
    commandsEditor.deleteAllContent();
    commandsEditor.typeTextIntoEditor(MAVEN_COMMAND);
    commandsEditor.waitTextIntoEditor(MAVEN_COMMAND);
    commandsEditor.typeTextIntoEditor(Keys.ENTER.toString());
    commandsEditor.waitActiveEditor();
    commandsEditor.typeTextIntoEditor("echo ");
    commandsEditor.typeTextIntoEditor("type");
    commandsEditor.launchAutocompleteAndWaitContainer();
    String[] autocompleteItems = {"${editor.current.project.", "${explorer.current.project."};
    for (String autocompleteItem : autocompleteItems) {
      commandsEditor.waitTextIntoAutocompleteContainer(autocompleteItem);
    }
    commandsEditor.selectAutocompleteProposal("${explorer.current.project.");
    commandsEditor.waitTextIntoDescriptionMacrosForm(
        "Project type of the file currently selected in explorer");
    commandsEditor.enterAutocompleteProposal("${editor.current.project.");
    commandsEditor.waitAutocompleteContainerIsClosed();
    commandsEditor.waitTextIntoEditor("${editor.current.project.type");
    commandsEditor.typeTextIntoEditor(Keys.ENTER.toString());
    commandsEditor.waitActiveEditor();
    commandsEditor.typeTextIntoEditor("echo ");
    commandsEditor.typeTextIntoEditor("na");
    commandsEditor.launchAutocompleteAndWaitContainer();
    commandsEditor.selectItemIntoAutocompleteAndPasteByDoubleClick("${explorer.current.project.");
    commandsEditor.waitAutocompleteContainerIsClosed();
    commandsEditor.waitTextIntoEditor("${explorer.current.project.name");
    commandsEditor.clickOnRunButton();
    consoles.waitExpectedTextIntoConsole(BUILD_SUCCESS);
    consoles.waitExpectedTextIntoConsole("maven");
    consoles.waitExpectedTextIntoConsole(PROJ_NAME);
  }

  @Test(priority = 2)
  public void checkAutocompletePreviewUrl() {
    commandsEditor.typeTextIntoEditor(Keys.ESCAPE.toString());
    commandsEditor.setFocusIntoTypeCommandsEditor(PREVIEW_URL_EDITOR);
    commandsEditor.setCursorToLine(1);
    commandsEditor.deleteAllContent();
    commandsEditor.typeTextIntoEditor("server.80");
    commandsEditor.launchAutocompleteAndWaitContainer();
    String[] autocompleteItems = {
      "${server.8000/tcp}", "${server.8080/tcp}",
    };
    for (String autocompleteItem : autocompleteItems) {
      commandsEditor.waitTextIntoAutocompleteContainer(autocompleteItem);
    }
    commandsEditor.selectAutocompleteProposal("80/tcp}");
    commandsEditor.waitTextIntoDescriptionMacrosForm("Returns address of the 8080/tcp server");
    commandsEditor.closeAutocomplete();
    commandsEditor.waitActiveEditor();
    commandsEditor.deleteAllContent();
    commandsEditor.typeTextIntoEditor("80/tcp");
    commandsEditor.launchAutocompleteAndWaitContainer();
    commandsEditor.selectItemIntoAutocompleteAndPasteByDoubleClick("${server.8080/");
    commandsEditor.waitTextIntoEditor("80/${server.8080/tcp}");
    commandsEditor.typeTextIntoEditor(Keys.ENTER.toString());
    commandsEditor.waitActiveEditor();
    commandsEditor.typeTextIntoEditor("8080");
    commandsEditor.launchAutocomplete();
    commandsEditor.waitTextIntoEditor("${server.8080/tcp}");
    commandsEditor.clickOnRunButton();
    consoles.waitExpectedTextIntoConsole(BUILD_SUCCESS);
    consoles.waitExpectedTextIntoPreviewUrl("80/tcp://172.17.0.1:");
  }

  @Test(priority = 3)
  public void checkAutocompleteAfterSave() {
    commandsEditor.waitTabCommandWithUnsavedStatus(MAVEN_NAME);
    commandsEditor.clickOnSaveButtonInTheEditCommand();
    commandsEditor.waitTabFileWithSavedStatus(MAVEN_NAME);
    commandsEditor.setFocusIntoTypeCommandsEditor(COMMAND_LINE_EDITOR);
    commandsEditor.setCursorToLine(2);
    commandsEditor.selectLineAndDelete();
    commandsEditor.waitActiveEditor();
    commandsEditor.launchAutocompleteAndWaitContainer();
    commandsEditor.selectItemIntoAutocompleteAndPasteByDoubleClick("${current.class.fqn}");
    commandsEditor.waitAutocompleteContainerIsClosed();
    commandsEditor.waitTextIntoEditor("${current.class.fqn}");
  }

  private void createMavenCommand() {
    commandsExplorer.openCommandsExplorer();
    commandsExplorer.waitCommandExplorerIsOpened();
    commandsExplorer.clickAddCommandButton(BUILD_GOAL);
    loader.waitOnClosed();
    commandsExplorer.chooseCommandTypeInContextMenu(MAVEN_TYPE);
    loader.waitOnClosed();
    commandsExplorer.waitCommandInExplorerByName(MAVEN_NAME);
    commandsEditor.waitTabIsPresent(MAVEN_NAME);
  }
}
