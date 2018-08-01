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

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrienko Alexander on 11.01.15. */
public class EditorMemberActiveLineForOpenedTabTest {
  private static final String PROJECT_NAME =
      NameGenerator.generate(EditorMemberActiveLineForOpenedTabTest.class.getSimpleName(), 4);
  private static final String DELETE_TEXT_FOR_SQL = "Delete file \"sqlFile.sql\"?";
  private static final String DELETE_TEXT_FOR_LESS = "Delete file \"LessFile.less\"?";
  private static final String DELETE_TEXT_FOR_TEXT_FILE = "Delete file \"another\"?";

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private Menu menu;
  @Inject private AskDialog askDialog;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void prepare() throws Exception {
    URL resource = getClass().getResource("/projects/defaultSpringProjectWithDifferentTypeOfFiles");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
    ide.open(workspace);
  }

  @Test
  public void createJavaSpringProjectAndTestEditor() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByVisibleNameInExplorer("AppController.java");
    editor.waitTabIsPresent("AppController");
    editor.waitActive();
    editor.expectedNumberOfActiveLine(1);
    projectExplorer.openItemByVisibleNameInExplorer("sqlFile.sql");
    editor.waitTabIsPresent("sqlFile.sql");
    editor.waitActive();
    editor.expectedNumberOfActiveLine(1);
    projectExplorer.openItemByVisibleNameInExplorer("another");
    editor.waitTabIsPresent("another");
    editor.expectedNumberOfActiveLine(1);
    editor.waitActive();
    editor.setCursorToLine(5);
    editor.selectTabByName("AppController");
    editor.waitActive();
    editor.setCursorToLine(8);
    editor.selectTabByName("another");
    editor.waitActive();
    editor.expectedNumberOfActiveLine(5);
    editor.selectTabByName("AppController");
    editor.waitActive();
    editor.expectedNumberOfActiveLine(8);
    editor.selectTabByName("sqlFile.sql");
    editor.waitActive();
    editor.setCursorToLine(3);
    editor.selectTabByName("another");
    editor.waitActive();
    editor.expectedNumberOfActiveLine(5);
    editor.selectTabByName("AppController");
    WaitUtils.sleepQuietly(2);
    editor.setCursorToLine(42);
    editor.selectTabByName("sqlFile.sql");
    editor.waitActive();
    editor.expectedNumberOfActiveLine(3);
    editor.selectTabByName("another");
    editor.waitActive();
    editor.expectedNumberOfActiveLine(5);
    editor.selectTabByName("sqlFile.sql");
    editor.waitActive();
    editor.expectedNumberOfActiveLine(3);
    projectExplorer.waitAndSelectItem(
        PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/sqlFile.sql");
    menu.runCommand(TestMenuCommandsConstants.Edit.EDIT, TestMenuCommandsConstants.Edit.DELETE);
    loader.waitOnClosed();
    askDialog.acceptDialogWithText(DELETE_TEXT_FOR_SQL);
    loader.waitOnClosed();
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitDisappearItemByPath(
        PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/sqlFile.sql");
    editor.selectTabByName("another");
    editor.expectedNumberOfActiveLine(5);
    projectExplorer.waitAndSelectItem(
        PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/LessFile.less");
    menu.runCommand(TestMenuCommandsConstants.Edit.EDIT, TestMenuCommandsConstants.Edit.DELETE);
    loader.waitOnClosed();
    askDialog.acceptDialogWithText(DELETE_TEXT_FOR_LESS);
    loader.waitOnClosed();
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitDisappearItemByPath(
        PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/LessFile.less");
    editor.selectTabByName("another");
    loader.waitOnClosed();
    editor.expectedNumberOfActiveLine(5);
    projectExplorer.waitAndSelectItem(
        PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/another");
    menu.runCommand(TestMenuCommandsConstants.Edit.EDIT, TestMenuCommandsConstants.Edit.DELETE);
    loader.waitOnClosed();
    askDialog.acceptDialogWithText(DELETE_TEXT_FOR_TEXT_FILE);
    loader.waitOnClosed();
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitDisappearItemByPath(
        PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/another");
    editor.selectTabByName("AppController");
    editor.expectedNumberOfActiveLine(42);
  }
}
