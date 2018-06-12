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

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.ASSISTANT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.FIND_DEFINITION;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.ERROR_OVERVIEW;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
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
import org.eclipse.che.selenium.pageobject.debug.DebugPanel;
import org.eclipse.che.selenium.pageobject.debug.PhpDebugConfig;
import org.openqa.selenium.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class PhpFileEditingTest {
  private static final Logger LOG = LoggerFactory.getLogger(PhpFileEditingTest.class);
  private static final String PROJECT = "php-tests";
  private static final String PATH_TO_INDEX_PHP = PROJECT + "/index.php";
  private static final URL RESOURCE =
      PhpFileEditingTest.class.getResource("/projects/plugins/DebuggerPlugin/php-tests");

  @InjectTestWorkspace(template = WorkspaceTemplate.ECLIPSE_PHP)
  private TestWorkspace ws;

  @Inject private Ide ide;

  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private DebugPanel debugPanel;
  @Inject private PhpDebugConfig debugConfig;
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

    editor.goToPosition(14, 9);

    menu.runCommand(ASSISTANT, FIND_DEFINITION);
    editor.waitActiveTabFileName("lib.php");
    editor.waitCursorPosition(14, 2);
  }

  private void checkCodeAssistant() {
    editor.goToCursorPositionVisible(15, 7);
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.typeTextIntoEditor("e");
    editor.launchAutocomplete();
    editor.waitTextIntoAutocompleteContainer(
        "expm1 float\nexp float\nerror_log bool\nexplode array\nexec string");
  }

  private void checkAutocompletion() {
    editor.goToCursorPositionVisible(13, 1);
    editor.typeTextIntoEditor("$color = \"blue\";");

    editor.goToCursorPositionVisible(14, 22);
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitCursorPosition(15, 1);

    editor.typeTextIntoEditor("$");
    editor.launchAutocomplete();
    editor.waitTextIntoEditor("$color = \"blue\";\necho sayHello(\"man\");\n$color\n?>", 3);
    editor.closeAutocomplete();
  }

  private void checkCodeValidation() {
    editor.goToPosition(14, 2);
    editor.typeTextIntoEditor(" ");
    editor.clickOnMarker(ERROR_OVERVIEW, 14);
    editor.waitTextInToolTipPopup("';' expected.");

    editor.goToPosition(14, 3);
    editor.typeTextIntoEditor(Keys.BACK_SPACE.toString());
    editor.waitAllMarkersInvisibility(ERROR_OVERVIEW);
  }
}
