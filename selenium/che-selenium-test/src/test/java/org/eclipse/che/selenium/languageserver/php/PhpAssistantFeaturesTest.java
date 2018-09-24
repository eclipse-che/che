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
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.FIND_PROJECT_SYMBOL;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.FIND_REFERENCES;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.GO_TO_SYMBOL;
import static org.openqa.selenium.Keys.CONTROL;
import static org.openqa.selenium.Keys.ENTER;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.core.workspace.InjectTestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.core.workspace.WorkspaceTemplate;
import org.eclipse.che.selenium.pageobject.AssistantFindPanel;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.FindReferencesConsoleTab;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class PhpAssistantFeaturesTest {
  private static final String PROJECT = "php-tests";
  private static final String PATH_TO_INDEX_PHP = PROJECT + "/index.php";
  private static final String TEXT_FOR_HOVERING = "sayHello";
  private static final String EXPECTED_ORIGINAL_TEXT =
      "/*\n" + " * Copyright (c) 2012-2018 Red Hat, Inc.";
  private static final String EXPECTED_COMMENTED_TEXT =
      "/*\n" + "// * Copyright (c) 2012-2018 Red Hat, Inc.";
  private static final String EXPECTED_HOVER_POPUP_TEXT =
      "php\n" + "<?php function sayHello($name) {\n";
  private static final String EXPECTED_REFERENCE_TEXT =
      PROJECT + "/index.php\n" + "From:15:6 To:15:14";
  private static final String EXPECTED_TEXT_AFTER_TYPING =
      "echo sayHello(\"man\");\n" + "sayHello(";
  private static final String EXPECTED_HINT_TEXT = "mixed $name";
  private static final String EXPECTED_GO_TO_SYMBOL_TEXT = "sayHellosymbols (1)";
  private static final String EXPECTED_FIND_PROJECT_TEXT = "sayHello/php-tests/lib.php";
  private static final String INDEX_TAB_NAME = "index.php";
  private static final String LIB_TAB_NAME = "lib.php";
  private static final String FIND_PROJECT_SEARCHING_TEXT = "say";
  private static final URL RESOURCE =
      PhpFileEditingTest.class.getResource("/projects/plugins/DebuggerPlugin/php-tests");

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
  @Inject private SeleniumWebDriverHelper seleniumWebDriverHelper;
  @Inject private FindReferencesConsoleTab findReferencesConsoleTab;
  @Inject private AssistantFindPanel assistantFindPanel;

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
  public void codeShouldBeCommented() {
    projectExplorer.openItemByPath(PATH_TO_INDEX_PHP);
    editor.waitActive();
    editor.waitTextIntoEditor(EXPECTED_ORIGINAL_TEXT);

    editor.setCursorToLine(4);
    performCommentAction();
    editor.waitTextIntoEditor(EXPECTED_COMMENTED_TEXT);

    performCommentAction();
    editor.waitTextIntoEditor(EXPECTED_ORIGINAL_TEXT);
  }

  @Test
  public void hoverShouldBeDisplayedWithExpectedText() {
    projectExplorer.openItemByPath(PATH_TO_INDEX_PHP);
    editor.waitActive();

    editor.moveCursorToText(TEXT_FOR_HOVERING);
    editor.waitTextInHoverPopUpEqualsTo(EXPECTED_HOVER_POPUP_TEXT);
  }

  @Test
  public void findReferenceNodeShouldBeDisplayed() {
    projectExplorer.openItemByPath(PATH_TO_INDEX_PHP);
    editor.waitActive();

    editor.goToCursorPositionVisible(15, 8);
    menu.runCommand(ASSISTANT, FIND_REFERENCES);
    try {
      findReferencesConsoleTab.waitReferenceWithText(EXPECTED_REFERENCE_TEXT);
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known permanent failure https://github.com/eclipse/che/issues/10698", ex);
    }
  }

  @Test
  public void signatureShouldBeDisplayed() {
    projectExplorer.openItemByPath(PATH_TO_INDEX_PHP);
    editor.waitActive();

    editor.goToCursorPositionVisible(15, 22);
    editor.typeTextIntoEditor(ENTER.toString());
    editor.waitCursorPosition(16, 1);
    editor.typeTextIntoEditor(TEXT_FOR_HOVERING + "(");
    editor.waitTextIntoEditor(EXPECTED_TEXT_AFTER_TYPING);
    waitExpectedTextIntoShowHintsPopup(EXPECTED_HINT_TEXT);
    editor.deleteCurrentLine();
  }

  @Test
  public void goToSymbolFeatureShouldWork() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.openItemByPath(PROJECT + "/" + LIB_TAB_NAME);
    editor.waitActive();

    editor.waitTabIsPresent(LIB_TAB_NAME);
    menu.runCommand(ASSISTANT, GO_TO_SYMBOL);
    waitActionNodeContainsText(EXPECTED_GO_TO_SYMBOL_TEXT);
  }

  @Test
  public void checkFindProjectFeature() {
    projectExplorer.openItemByPath(PATH_TO_INDEX_PHP);
    editor.waitActive();
    editor.selectTabByName(INDEX_TAB_NAME);
    editor.waitActiveTabFileName(INDEX_TAB_NAME);
    editor.waitActive();

    menu.runCommand(ASSISTANT, FIND_PROJECT_SYMBOL);
    assistantFindPanel.waitForm();
    assistantFindPanel.typeToInputField(FIND_PROJECT_SEARCHING_TEXT);
    assistantFindPanel.waitNode(EXPECTED_FIND_PROJECT_TEXT);
  }

  private void performCommentAction() {
    String comment = Keys.chord(CONTROL, "/");
    seleniumWebDriverHelper.sendKeys(comment);
  }

  private void waitExpectedTextIntoShowHintsPopup(String expectedText) {
    try {
      editor.waitExpTextIntoShowHintsPopUp(expectedText);
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known permanent failure https://github.com/eclipse/che/issues/10699", ex);
    }
  }

  private void waitActionNodeContainsText(String expectedText) {
    try {
      assistantFindPanel.waitNode(expectedText);
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known permanent failure https://github.com/eclipse/che/issues/10698", ex);
    }
  }
}
