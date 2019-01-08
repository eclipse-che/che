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
package org.eclipse.che.selenium.intelligencecommand;

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandsDefaultNames.JAVA_NAME;
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandsGoals.RUN_GOAL;
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandsTypes.JAVA_TYPE;
import static org.eclipse.che.selenium.pageobject.intelligent.CommandsEditor.CommandsEditorLocator.COMMAND_LINE_EDITOR;
import static org.eclipse.che.selenium.pageobject.intelligent.CommandsEditor.CommandsEditorLocator.PREVIEW_URL_EDITOR;
import static org.eclipse.che.selenium.pageobject.intelligent.CommandsEditor.CommandsMacrosLinkLocator.EDITOR_MACROS_LINK;
import static org.eclipse.che.selenium.pageobject.intelligent.CommandsEditor.CommandsMacrosLinkLocator.PREVIEW_MACROS_LINK;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
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

/** @author Aleksandr Shmaraiev */
public class MacrosCommandsEditorTest {
  private static final String PROJECT_NAME = generate("MacrosCommandsEditorTest-", 4);
  private static final String PATH_TO_FILE = PROJECT_NAME + "/src/Main.java";
  private static final String PATH_TO_ROOT_FOLDER = "/projects/" + PROJECT_NAME;

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private CommandsEditor commandsEditor;
  @Inject private CommandsExplorer commandsExplorer;
  @Inject private Loader loader;
  @Inject private Consoles consoles;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void prepare() throws Exception {
    URL resource = getClass().getResource("/projects/java-project-with-additional-source-folder");
    testProjectServiceClient.importProject(
        ws.getId(), Paths.get(resource.toURI()), PROJECT_NAME, ProjectTemplates.PLAIN_JAVA);
    ide.open(ws);
    consoles.waitJDTLSProjectResolveFinishedMessage(PROJECT_NAME);
  }

  @Test(priority = 1)
  public void checkCommandMacrosIntoCommandLine() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.waitItem(PATH_TO_FILE);
    projectExplorer.openItemByPath(PATH_TO_FILE);
    createNewJavaCommand();
    commandsEditor.selectMacroLinkInCommandsEditor(EDITOR_MACROS_LINK);
    commandsEditor.cancelFormInEditorByEscape();
    commandsEditor.waitCommandMacrosIsClosed();
    commandsEditor.setFocusIntoTypeCommandsEditor(COMMAND_LINE_EDITOR);
    commandsEditor.setCursorToLine(1);
    commandsEditor.typeTextIntoEditor(Keys.ENTER.toString());
    commandsEditor.typeTextIntoEditor(Keys.ARROW_UP.toString());
    commandsEditor.waitActive();
    commandsEditor.typeTextIntoEditor("echo ");
    commandsEditor.selectMacroLinkInCommandsEditor(EDITOR_MACROS_LINK);
    commandsEditor.typeTextIntoSearchMacroField("rel");
    commandsEditor.waitTextIntoSearchMacroField("rel");

    String[] macrosItems = {
      "${current.project.relpath}",
      "${editor.current.file.relpath}",
      "${explorer.current.file.relpath}",
      "Path relative to the /projects folder to the selected file in Editor",
      "Path relative to the /projects folder in project tree"
    };

    for (String macrosItem : macrosItems) {
      commandsEditor.waitTextIntoMacrosContainer(macrosItem);
    }
    commandsEditor.enterMacroCommandByEnter("${explorer.current.file.relpath}");
    commandsEditor.waitTextIntoEditor("echo ${explorer.current.file.relpath}");
    commandsEditor.clickOnRunButton();
    consoles.waitExpectedTextIntoConsole("/" + PROJECT_NAME + "/src/Main.java");
    commandsEditor.setCursorToLine(1);
    commandsEditor.selectLineAndDelete();
    commandsEditor.waitActive();
    commandsEditor.typeTextIntoEditor("echo ");
    commandsEditor.selectMacroLinkInCommandsEditor(EDITOR_MACROS_LINK);
    commandsEditor.selectMacroCommand("${current.class.fqn}");
    commandsEditor.typeTextIntoEditor(Keys.ARROW_DOWN.toString());
    commandsEditor.typeTextIntoEditor(Keys.SPACE.toString());
    commandsEditor.enterMacroCommandByDoubleClick("${current.project.path}");
    commandsEditor.waitTextIntoEditor("echo ${current.project.path}");
    commandsEditor.clickOnRunButton();
    consoles.waitExpectedTextIntoConsole(PATH_TO_ROOT_FOLDER);
  }

  @Test(priority = 2)
  public void checkCommandMacrosIntoPreviewUrl() {
    commandsEditor.cancelFormInEditorByEscape();
    commandsEditor.setFocusIntoTypeCommandsEditor(PREVIEW_URL_EDITOR);
    commandsEditor.setCursorToLine(1);
    commandsEditor.waitActive();
    commandsEditor.selectMacroLinkInCommandsEditor(PREVIEW_MACROS_LINK);
    commandsEditor.typeTextIntoSearchMacroField("server.");
    commandsEditor.waitTextIntoSearchMacroField("server.");

    for (String macrosItem : getArraytMacrosItems()) {
      commandsEditor.waitTextIntoMacrosContainer(macrosItem);
    }

    commandsEditor.enterMacroCommandByEnter("${server.wsagent/http}");
    commandsEditor.waitTextIntoEditor("${server.wsagent/http");
    commandsEditor.clickOnRunButton();
    consoles.waitExpectedTextIntoPreviewUrl("http");
    commandsEditor.setFocusIntoTypeCommandsEditor(PREVIEW_URL_EDITOR);
    commandsEditor.setCursorToLine(1);
    commandsEditor.selectLineAndDelete();
    commandsEditor.waitActive();
    commandsEditor.selectMacroLinkInCommandsEditor(PREVIEW_MACROS_LINK);
    commandsEditor.selectMacroCommand("${server.wsagent/http}");
    commandsEditor.typeTextIntoEditor(Keys.ARROW_UP.toString());
    commandsEditor.typeTextIntoEditor(Keys.SPACE.toString());
    commandsEditor.enterMacroCommandByDoubleClick("${server.wsagent/http}");
    commandsEditor.waitTextIntoEditor("${server.wsagent/http}");
    commandsEditor.clickOnRunButton();
    consoles.waitExpectedTextIntoPreviewUrl("http");
  }

  private void createNewJavaCommand() {
    commandsExplorer.openCommandsExplorer();
    commandsExplorer.waitCommandExplorerIsOpened();
    commandsExplorer.clickAddCommandButton(RUN_GOAL);
    loader.waitOnClosed();
    commandsExplorer.chooseCommandTypeInContextMenu(JAVA_TYPE);
    loader.waitOnClosed();
    commandsExplorer.waitCommandInExplorerByName(JAVA_NAME);
    commandsEditor.waitTabIsPresent(JAVA_NAME);
  }

  protected String[] getArraytMacrosItems() {
    String[] macrosItems = {
      "${server.codeserver}",
      "${server.exec-agent/http}",
      "${server.exec-agent/ws}",
      "${server.terminal}",
      "${server.tomcat8-debug}",
      "${server.tomcat8}",
      "${server.wsagent/http}",
      "${server.wsagent/ws}"
    };
    return macrosItems;
  }
}
