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
package org.eclipse.che.selenium.refactor.move;

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.DEFAULT_TIMEOUT;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.ERROR;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.WARNING;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Refactor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev */
public class CodeAssistAfterMoveItemTest {

  private static final String APPLY_WORKSPACE_CHANGES = "Apply Workspace Changes\nDone";
  private static final String PROJECT_NAME = NameGenerator.generate("CodeAssistAfterMoveItem-", 4);
  private static final String pathToPackageInChePrefix = PROJECT_NAME + "/src/main/java";

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private Refactor refactor;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private Consoles consoles;
  @Inject private Events events;

  @BeforeClass
  public void prepare() throws Exception {
    URL resource = getClass().getResource("/projects/move-items-project");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
    ide.open(workspace);
    ide.waitOpenedWorkspaceIsReadyToUse();
    consoles.waitJDTLSProjectResolveFinishedMessage(PROJECT_NAME);
    projectExplorer.waitVisibleItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
    loader.waitOnClosed();
  }

  @Test
  public void checkCodeAssistAfterMoveItem() {
    projectExplorer.openItemByPath(
        pathToPackageInChePrefix + "/org/eclipse/qa/examples/AppController.java");
    loader.waitOnClosed();
    editor.waitActive();
    editor.setCursorToLine(32);
    editor.typeTextIntoEditor(Keys.TAB.toString());
    loader.waitOnClosed();
    editor.typeTextIntoEditor("A5 a = new A5();");
    loader.waitOnClosed();
    editor.waitTextIntoEditor("A5 a = new A5();");
    editor.waitMarkerInPosition(ERROR, 32);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.launchPropositionAssistPanel();
    loader.waitOnClosed();
    editor.waitTextIntoFixErrorProposition("Import 'A5' (r)");
    editor.enterTextIntoFixErrorPropByDoubleClick("Import 'A5' (r)");
    loader.waitOnClosed();
    editor.waitTextIntoEditor("import r.A5;");
    editor.waitMarkerInPosition(WARNING, 34);
    events.clickEventLogBtn();

    // move item 'A5' into package 'p1'
    projectExplorer.waitAndSelectItem(pathToPackageInChePrefix + "/r/A5.java");
    projectExplorer.launchRefactorMoveByKeyboard();
    refactor.waitMoveItemFormIsOpen();
    refactor.clickOnExpandIconTree(PROJECT_NAME);
    refactor.clickOnExpandIconTree("/src/main/java");
    refactor.chooseDestinationForItem("p1");
    refactor.clickOkButtonRefactorForm();
    refactor.waitMoveItemFormIsClosed();
    loader.waitOnClosed();
    events.waitExpectedMessage(APPLY_WORKSPACE_CHANGES, DEFAULT_TIMEOUT);
    projectExplorer.waitItem(pathToPackageInChePrefix + "/p1/A5.java");
    projectExplorer.waitDisappearItemByPath(pathToPackageInChePrefix + "/r/A5.java");
    editor.waitTextIntoEditor("import p1.A5;");
    editor.setCursorToLine(17);
    editor.deleteCurrentLine();
    loader.waitOnClosed();
    editor.waitMarkerInPosition(ERROR, 33);
    editor.goToCursorPositionVisible(33, 5);
    editor.launchPropositionAssistPanel();
    loader.waitOnClosed();

    try {
      editor.waitTextIntoFixErrorProposition("Import 'A5' (p1)");
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known permanent failure https://github.com/eclipse/che/issues/11701");
    }

    editor.enterTextIntoFixErrorPropByEnter("Import 'A5' (p1)");
    editor.waitTextIntoEditor("import p1.A5;");
  }
}
