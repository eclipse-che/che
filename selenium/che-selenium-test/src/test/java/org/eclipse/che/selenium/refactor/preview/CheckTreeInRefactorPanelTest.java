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
package org.eclipse.che.selenium.refactor.preview;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Refactor;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class CheckTreeInRefactorPanelTest {

  private static final String PROJECT_NAME = CheckTreeInRefactorPanelTest.class.getSimpleName();

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private Refactor refactorPanel;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource =
        CheckTreeInRefactorPanelTest.this.getClass().getResource("/projects/guess-project");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
    ide.open(workspace);
  }

  @Test
  public void previewChangeTest() {
    projectExplorer.waitProjectExplorer();
    loader.waitOnClosed();
    projectExplorer.waitItem(PROJECT_NAME);
    loader.waitOnClosed();
    projectExplorer.quickExpandWithJavaScript();
    loader.waitOnClosed();
    projectExplorer.openItemByPath(
        PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/AppController.java");
    editor.waitActive();
    editor.goToCursorPositionVisible(26, 17);
    editor.launchRefactorFormFromEditor();
    editor.launchRefactorFormFromEditor();
    refactorPanel.waitRenameParametersFormIsOpen();
    refactorPanel.typeAndWaitNewName("a3");
    refactorPanel.clickPreviewButtonRefactorForm();
    refactorPanel.clickOnItemByNameAndPosition("AppController.java", 0);
    loader.waitOnClosed();
    refactorPanel.clickOnExpandItemByNameAndPosition("AppController", 0);
    refactorPanel.clickOnExpandItemByNameAndPosition("AppController", 1);
    refactorPanel.clickOnExpandItemByNameAndPosition("handleRequest", 0);
    refactorPanel.setFlagItemByNameAndPosition("handleRequest", 0);
    Assert.assertFalse(
        refactorPanel.itemIsSelectedByNameAndPosition("Update local variable reference", 0),
        "This item in tree mustn't be selected.");
    Assert.assertFalse(
        refactorPanel.itemIsSelectedByNameAndPosition("Update local variable reference", 1),
        "This item in tree mustn't be selected.");
    Assert.assertFalse(
        refactorPanel.itemIsSelectedByNameAndPosition("Update local variable reference", 2),
        "This item in tree mustn't be selected.");
    Assert.assertFalse(
        refactorPanel.itemIsSelectedByNameAndPosition("Update local variable reference", 3),
        "This item in tree mustn't be selected.");
  }
}
