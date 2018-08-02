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
package org.eclipse.che.selenium.editor.autocomplete;

import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.INFO;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.openqa.selenium.Keys;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class AutocompleteJSFilesTest {
  private static final String PROJECT_NAME =
      NameGenerator.generate(AutocompleteJSFilesTest.class.getSimpleName(), 4);
  private static final String EXPECTED_TEXT =
      "function a (){\n"
          + "    var b = 5;\n"
          + "    var c = b;\n"
          + "    return c;\n"
          + "}\n"
          + "a();\n"
          + "/**\n"
          + " * \n"
          + " */";

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private Menu menu;
  @Inject private AskForValueDialog askForValueDialog;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/default-spring-project");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
    ide.open(workspace);
  }

  @Test
  public void checkAutocompleteJSFilesTest() {
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.openItemByPath(PROJECT_NAME);
    notificationsPopupPanel.waitProgressPopupPanelClose();
    projectExplorer.waitProjectExplorer();
    projectExplorer.quickExpandWithJavaScript();
    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT,
        TestMenuCommandsConstants.Project.New.NEW,
        TestMenuCommandsConstants.Project.New.JAVASCRIPT_FILE);
    askForValueDialog.waitFormToOpen();
    askForValueDialog.typeAndWaitText("newJsFile");
    askForValueDialog.clickOkBtn();
    loader.waitOnClosed();
    projectExplorer.waitVisibilityByName("newJsFile.js");

    editor.waitActive();
    loader.waitOnClosed();
    editor.setCursorToLine(1);

    editor.typeTextIntoEditorWithoutDelayForSaving("function a (");
    editor.typeTextIntoEditorWithoutDelayForSaving(Keys.END.toString());
    editor.typeTextIntoEditorWithoutDelayForSaving("{");
    editor.typeTextIntoEditorWithoutDelayForSaving(Keys.ENTER.toString());
    editor.typeTextIntoEditorWithoutDelayForSaving("var b = 5;");
    editor.typeTextIntoEditorWithoutDelayForSaving(Keys.ENTER.toString());
    editor.typeTextIntoEditorWithoutDelayForSaving("var c = ");

    editor.launchPropositionAssistPanelForJSFiles();
    editor.enterTextIntoFixErrorPropByEnterForJsFiles("b", " : number");

    editor.typeTextIntoEditorWithoutDelayForSaving(";");
    editor.typeTextIntoEditorWithoutDelayForSaving(Keys.ENTER.toString());
    editor.typeTextIntoEditorWithoutDelayForSaving("return c;");
    editor.setCursorToLine(5);
    editor.typeTextIntoEditorWithoutDelayForSaving(Keys.END.toString());
    editor.typeTextIntoEditorWithoutDelayForSaving(Keys.ENTER.toString());

    editor.launchPropositionAssistPanelForJSFiles();
    editor.enterTextIntoFixErrorPropByEnterForJsFiles("a()", " : number");

    editor.typeTextIntoEditorWithoutDelayForSaving(";");
    editor.typeTextIntoEditorWithoutDelayForSaving(Keys.ENTER.toString());
    editor.typeTextIntoEditorWithoutDelayForSaving("/**");
    editor.typeTextIntoEditorWithoutDelayForSaving(Keys.ENTER.toString());

    Assert.assertEquals(editor.getVisibleTextFromEditor(), EXPECTED_TEXT);

    editor.setCursorToLine(9);
    editor.typeTextIntoEditorWithoutDelayForSaving(Keys.END.toString());
    editor.typeTextIntoEditorWithoutDelayForSaving(Keys.ENTER.toString());
    editor.typeTextIntoEditorWithoutDelayForSaving("function f (");
    editor.typeTextIntoEditorWithoutDelayForSaving(Keys.END.toString());
    editor.typeTextIntoEditorWithoutDelayForSaving("{");
    editor.typeTextIntoEditorWithoutDelayForSaving(Keys.ENTER.toString());
    editor.typeTextIntoEditorWithoutDelayForSaving("return 'test text';");
    editor.setCursorToLine(12);
    editor.typeTextIntoEditorWithoutDelayForSaving(Keys.END.toString());
    editor.typeTextIntoEditorWithoutDelayForSaving(Keys.ENTER.toString());

    editor.launchPropositionAssistPanelForJSFiles();
    editor.enterTextIntoFixErrorPropByEnterForJsFiles("f()", " : string");
    editor.typeTextIntoEditorWithoutDelayForSaving(";");

    editor.typeTextIntoEditorWithoutDelayForSaving(Keys.ENTER.toString());

    editor.setCursorToLine(11);
    editor.typeTextIntoEditorWithoutDelayForSaving(Keys.END.toString());
    editor.typeTextIntoEditorWithoutDelayForSaving(Keys.BACK_SPACE.toString());

    editor.clickOnMarker(INFO, 11);
    editor.clickOnElementByXpath("//button[text()='Add missing semicolon']");

    editor.waitAllMarkersInvisibility(INFO);
    loader.waitOnClosed();
  }
}
