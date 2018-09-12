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

import static org.openqa.selenium.Keys.CONTROL;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
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

public class PhpAssistantFeaturesTest {
  private static final String PROJECT = "php-tests";
  private static final String PATH_TO_INDEX_PHP = PROJECT + "/index.php";
  private static final String TEXT_FOR_HOVERING = "sayHello";
  private static final String EXPECTED_ORIGINAL_TEXT =
      "/*\n" + " * Copyright (c) 2012-2018 Red Hat, Inc.";
  private static final String EXPECTED_COMMENTED_TEXT =
      "/*\n" + "// * Copyright (c) 2012-2018 Red Hat, Inc.";
  private static final String EXPECTED_HOVER_POPUP_TEXT =
      "php\n" + "<?php function sayHello($name) {\n" + "php\n" + "<?php function sayHello($name) {";
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
  public void checkEditor() {
    editor.waitActive();

    // check code commenting
    editor.waitTextIntoEditor(EXPECTED_ORIGINAL_TEXT);
    editor.setCursorToLine(4);
    performCommentAction();
    editor.waitTextIntoEditor(EXPECTED_COMMENTED_TEXT);
    performCommentAction();
    editor.waitTextIntoEditor(EXPECTED_ORIGINAL_TEXT);

    // check hover feature
    editor.waitActive();
    editor.moveCursorToText(TEXT_FOR_HOVERING);
    editor.waitTextInHoverPopup(EXPECTED_HOVER_POPUP_TEXT);
  }

  private void performCommentAction() {
    String comment = Keys.chord(CONTROL, "/");
    seleniumWebDriverHelper.getAction().sendKeys(comment).perform();
  }
}
