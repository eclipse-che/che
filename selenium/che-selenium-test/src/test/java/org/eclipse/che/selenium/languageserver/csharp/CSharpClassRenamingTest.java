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
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.Refactoring.LS_RENAME;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.Refactoring.REFACTORING;
import static org.eclipse.che.selenium.core.project.ProjectTemplates.DOT_NET;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.ERROR;
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
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsPalette;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CSharpClassRenamingTest {
  private static final String PROJECT_NAME =
      NameGenerator.generate(CSharpClassRenamingTest.class.getSimpleName(), 4);

  private static final String PATH_TO_DOTNET_FILE = PROJECT_NAME + "/Hello.cs";

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

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/CSharpHelloWorld");
    testProjectServiceClient.importProject(
        workspace.getId(), Paths.get(resource.toURI()), PROJECT_NAME, DOT_NET);
    ide.open(workspace);
    projectExplorer.openItemByPath(PROJECT_NAME);
    projectExplorer.openItemByPath(PATH_TO_DOTNET_FILE);
    consoles.waitExpectedTextIntoConsole(FINISH_LANGUAGE_SERVER_INITIALIZATION_MESSAGE);
  }

  @Test
  public void checkRenaming() {
    String newClassName = "HelloWorld";
    String textFragmentAfterRenaming = "public class HelloWorld";
    consoles.selectProcessByTabName("dev-machine");
    editor.goToCursorPositionVisible(18, 18);
    menu.runCommand(ASSISTANT, REFACTORING, LS_RENAME);
    editor.doRenamingByLanguageServerField(newClassName);

    try {
      editor.waitTextIntoEditor(textFragmentAfterRenaming);
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known permanent failure https://github.com/eclipse/che/issues/10180", ex);
    }

    editor.waitAllMarkersInvisibility(ERROR);
  }
}
