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

import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.ERROR;

import com.google.common.base.Joiner;
import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CheTerminal;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Refactor;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev on 05.01.16 */
public class FailMoveItemTest {
  private static final String PROJECT_NAME = NameGenerator.generate("FailMoveItemProject-", 4);
  private static final String pathToPackageInChePrefix = PROJECT_NAME + "/src" + "/main" + "/java";

  private String contentFromInA;
  private String contentFromOutA;

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private CheTerminal terminal;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private Refactor refactor;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void prepare() throws Exception {
    URL resource = getClass().getResource("/projects/move-items-project");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
    ide.open(workspace);
    projectExplorer.waitProjectExplorer();
    terminal.waitFirstTerminalTab();
    projectExplorer.waitVisibleItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
    loader.waitOnClosed();
  }

  @AfterMethod(alwaysRun = true)
  public void closeForm() {
    if (refactor.isWidgetOpened()) {
      refactor.clickCancelButtonRefactorForm();
      editor.closeAllTabs();
    }
  }

  @Test
  public void checkFailMoveItem17() throws Exception {
    setFieldsForTest("testfail17");
    projectExplorer.openItemByPath(pathToPackageInChePrefix + "/r/A17.java");
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    projectExplorer.waitAndSelectItem(pathToPackageInChePrefix + "/r/A17.java");
    projectExplorer.launchRefactorMoveByKeyboard();
    refactor.waitMoveItemFormIsOpen();
    refactor.clickOnExpandIconTree(PROJECT_NAME);
    refactor.clickOnExpandIconTree("/src/main/java");
    refactor.chooseDestinationForItem("p1");
    refactor.setAndWaitStateUpdateReferencesCheckbox(false);
    refactor.clickOkButtonRefactorForm();
    refactor.waitMoveItemFormIsClosed();
    editor.waitTextIntoEditor(contentFromOutA);
    editor.clickOnSelectedElementInEditor("r.A17");
    editor.waitMarkerInPosition(ERROR, 14);
    projectExplorer.waitDisappearItemByPath(pathToPackageInChePrefix + "/r/A17.java");
    editor.closeFileByNameWithSaving("A17");
  }

  private void setFieldsForTest(String nameCurrentTest) throws Exception {
    URL resource = getClass().getResource(nameCurrentTest + "/" + "in/A.java");
    List<String> content = Files.readAllLines(Paths.get(resource.toURI()));
    contentFromInA = Joiner.on('\n').join(content);

    resource = getClass().getResource(nameCurrentTest + "/" + "out/A.java");
    content = Files.readAllLines(Paths.get(resource.toURI()));
    contentFromOutA = Joiner.on('\n').join(content);
  }
}
