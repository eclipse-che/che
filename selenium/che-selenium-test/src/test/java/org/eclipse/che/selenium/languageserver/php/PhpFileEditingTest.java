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
package org.eclipse.che.selenium.languageserver.php;

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.ASSISTANT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.FIND_DEFINITION;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.ERROR_OVERVIEW;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.InjectTestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.core.workspace.WorkspaceTemplate;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class PhpFileEditingTest {
  private static final String PROJECT = "php-tests";
  private static final String PATH_TO_INDEX_PHP = PROJECT + "/index.php";
  private static final URL RESOURCE =
      PhpFileEditingTest.class.getResource("/projects/plugins/DebuggerPlugin/php-tests");

  private List<String> expectedProposals =
      ImmutableList.of(
          "expm1 float", "exp float", "error_log bool", "explode array", "exec string");

  @InjectTestWorkspace(template = WorkspaceTemplate.ECLIPSE_PHP)
  private TestWorkspace ws;

  @Inject private Ide ide;

  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private NotificationsPopupPanel notificationPopup;
  @Inject private Menu menu;
  @Inject private CodenvyEditor editor;
  @Inject private Consoles consoles;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setup() throws Exception {
    testProjectServiceClient.importProject(
        ws.getId(), Paths.get(RESOURCE.toURI()), PROJECT, ProjectTemplates.PHP);

    // open IDE
    ide.open(ws);
    loader.waitOnClosed();
    projectExplorer.waitItem(PROJECT);
    notificationPopup.waitProgressPopupPanelClose();

    // open project tree
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByPath(PATH_TO_INDEX_PHP);
    editor.waitActive();
  }

  @Test
  public void checkMainFeaturesPhpLS() {
    String intitPhpLanguageServerMessage =
        String.format(
            "Finished language servers initialization, file path '/%s'", PATH_TO_INDEX_PHP);

    consoles.waitExpectedTextIntoConsole(intitPhpLanguageServerMessage);

    checkCodeValidation();
    checkAutocompletion();
    checkCodeAssistant();
    checkGoToDefinition();
  }

  private void checkGoToDefinition() {
    editor.deleteCurrentLine();

    editor.goToPosition(15, 9);

    menu.runCommand(ASSISTANT, FIND_DEFINITION);
    editor.waitActiveTabFileName("lib.php");
    editor.waitCursorPosition(15, 2);
  }

  private void checkCodeAssistant() {
    editor.goToCursorPositionVisible(16, 7);
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.typeTextIntoEditor("e");
    editor.launchAutocomplete();

    // check expected proposals in Autocomplete container
    expectedProposals.forEach(
        proposal -> {
          editor.waitProposalIntoAutocompleteContainer(proposal);
        });
  }

  private void checkAutocompletion() {
    editor.goToCursorPositionVisible(14, 1);
    editor.typeTextIntoEditor("$color = \"blue\";");

    editor.goToCursorPositionVisible(15, 22);
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitCursorPosition(16, 1);

    editor.typeTextIntoEditor("$");
    editor.launchAutocomplete();
    editor.waitTextIntoEditor("$color = \"blue\";\necho sayHello(\"man\");\n$color\n?>", 3);
    editor.closeAutocomplete();
  }

  private void checkCodeValidation() {
    editor.goToPosition(15, 2);
    editor.typeTextIntoEditor(" ");
    editor.clickOnMarker(ERROR_OVERVIEW, 15);
    editor.waitTextInToolTipPopup("';' expected.");

    editor.goToPosition(15, 3);
    editor.typeTextIntoEditor(Keys.BACK_SPACE.toString());
    editor.waitAllMarkersInvisibility(ERROR_OVERVIEW);
  }
}
