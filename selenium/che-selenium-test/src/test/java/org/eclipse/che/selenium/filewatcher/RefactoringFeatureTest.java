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
package org.eclipse.che.selenium.filewatcher;

import static org.eclipse.che.selenium.core.utils.WaitUtils.sleepQuietly;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.pageobject.InjectPageObject;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Refactor;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class RefactoringFeatureTest {
  private static final String PROJECT_NAME = NameGenerator.generate("project", 6);
  private final String PATH_TO_GREETING_FILE =
      PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/AppController.java";
  private final String originClassName = "AppController.java";
  private final String renamedClassName = "AppController2.java";

  @Inject private TestWorkspace ws;

  @InjectPageObject(driverId = 1)
  private Ide ide1;

  @InjectPageObject(driverId = 1)
  private ProjectExplorer projectExplorer1;

  @InjectPageObject(driverId = 1)
  private CodenvyEditor editor1;

  @InjectPageObject(driverId = 1)
  private Events events1;

  @InjectPageObject(driverId = 1)
  private Menu menu1;

  @InjectPageObject(driverId = 2)
  private Ide ide2;

  @InjectPageObject(driverId = 2)
  private CodenvyEditor editor2;

  @InjectPageObject(driverId = 2)
  private Events events2;

  @InjectPageObject(driverId = 2)
  private ProjectExplorer projectExplorer2;

  @InjectPageObject(driverId = 2)
  private Refactor refactorPanel2;

  @InjectPageObject(driverId = 2)
  private Menu menu2;

  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/spring-project-for-file-watcher-tabs");
    testProjectServiceClient.importProject(
        ws.getId(), Paths.get(resource.toURI()), PROJECT_NAME, ProjectTemplates.MAVEN_SPRING);
    ide1.open(ws);
    ide2.open(ws);
    events1.clickEventLogBtn();
    events2.clickEventLogBtn();
  }

  @Test
  public void checkRefactorFilesFromIde() throws Exception {
    String expectedMessAfterRename = "File '" + originClassName + "' is removed";
    String expectedMessAfterMove = "File '" + renamedClassName + "' is removed";
    projectExplorer1.waitItem(PROJECT_NAME);
    prepareFiles(editor1, projectExplorer1);
    prepareFiles(editor2, projectExplorer2);
    editor1.goToCursorPositionVisible(21, 14);
    doRenameRefactor();
    checkWatching(expectedMessAfterRename);
    editor2.waitTabIsNotPresent(renamedClassName);
    doMoveRefactor();
    projectExplorer1.openItemByVisibleNameInExplorer(renamedClassName);
    events1.waitExpectedMessage(expectedMessAfterMove);
  }

  private void checkWatching(String expectedMessAfterRename) {
    events2.waitExpectedMessage(expectedMessAfterRename);
    projectExplorer2.waitItemIsNotPresentVisibleArea(originClassName);
    editor2.waitTabIsNotPresent(originClassName);
  }

  private void prepareFiles(CodenvyEditor editor, ProjectExplorer projectExplorer) {
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByPath(PATH_TO_GREETING_FILE);
    editor.waitTabIsPresent(originClassName.replace(".java", ""));
    editor.waitActive();
  }

  private void doRenameRefactor() {
    menu1.runCommand(
        TestMenuCommandsConstants.Assistant.ASSISTANT,
        TestMenuCommandsConstants.Assistant.Refactoring.REFACTORING,
        TestMenuCommandsConstants.Assistant.Refactoring.RENAME);
    editor1.typeTextIntoEditor(renamedClassName.replace(".java", ""));
    sleepQuietly(1);
    editor1.typeTextIntoEditor(Keys.ENTER.toString());
    projectExplorer1.waitItem(PATH_TO_GREETING_FILE.replace(originClassName, renamedClassName), 10);
  }

  private void doMoveRefactor() {
    String pathToRenamedItem =
        String.format(
            PROJECT_NAME + "%s%s", "/src/main/java/org/eclipse/qa/examples/", renamedClassName);
    projectExplorer2.waitAndSelectItem(pathToRenamedItem);
    menu2.runCommand(
        TestMenuCommandsConstants.Assistant.ASSISTANT,
        TestMenuCommandsConstants.Assistant.Refactoring.REFACTORING,
        TestMenuCommandsConstants.Assistant.Refactoring.MOVE);
    refactorPanel2.waitMoveItemFormIsOpen();
    refactorPanel2.clickOnExpandIconTree(PROJECT_NAME);
    refactorPanel2.clickOnExpandIconTree("/src/main/java");
    refactorPanel2.chooseDestinationForItem("com.move");
    refactorPanel2.clickOkButtonRefactorForm();
    refactorPanel2.waitMoveItemFormIsClosed();
  }
}
