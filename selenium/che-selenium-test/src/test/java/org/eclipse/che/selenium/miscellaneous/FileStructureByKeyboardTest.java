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
package org.eclipse.che.selenium.miscellaneous;

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.core.project.ProjectTemplates.MAVEN_SIMPLE;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.FileStructure;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class FileStructureByKeyboardTest {
  private static final String PROJECT_NAME = generate("project", 4);
  private static final String JAVA_FILE_NAME = "Company";
  private static final String INNER_CLASS_NAME = "CompanyHelper";
  private static final String INTERFACE_NAME = "Inter";

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private CodenvyEditor editor;
  @Inject private FileStructure fileStructure;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private Consoles consoles;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/prOutline");
    testProjectServiceClient.importProject(
        workspace.getId(), Paths.get(resource.toURI()), PROJECT_NAME, MAVEN_SIMPLE);

    ide.open(workspace);
    consoles.waitJDTLSProjectResolveFinishedMessage(PROJECT_NAME);
  }

  @Test
  public void checkFileStructureByKeyboard() {
    ide.waitOpenedWorkspaceIsReadyToUse();
    projectExplorer.openItemByPath(PROJECT_NAME);
    expandTReeProjectAndOpenClass(JAVA_FILE_NAME);

    // check work nodes in the 'file structure' by keyboard
    fileStructure.launchFileStructureFormByKeyboard();
    fileStructure.waitFileStructureFormIsOpen(JAVA_FILE_NAME);
    fileStructure.waitExpectedTextInFileStructure(FileStructureNodesTest.ITEMS_CLASS);
    fileStructure.sendCommandByKeyboardInFileStructure(Keys.ARROW_LEFT.toString());
    fileStructure.waitExpectedTextIsNotPresentInFileStructure(FileStructureNodesTest.ITEMS_CLASS);
    fileStructure.sendCommandByKeyboardInFileStructure(Keys.ARROW_RIGHT.toString());
    fileStructure.waitExpectedTextInFileStructure(FileStructureNodesTest.ITEMS_CLASS_1);
    fileStructure.selectItemInFileStructure(INNER_CLASS_NAME);
    fileStructure.waitExpectedTextIsNotPresentInFileStructure(
        FileStructureNodesTest.ITEMS_INNER_CLASS);
    fileStructure.sendCommandByKeyboardInFileStructure(Keys.ARROW_RIGHT.toString());
    fileStructure.waitExpectedTextInFileStructure(FileStructureNodesTest.ITEMS_INNER_CLASS);
    fileStructure.sendCommandByKeyboardInFileStructure(Keys.ARROW_LEFT.toString());
    fileStructure.waitExpectedTextIsNotPresentInFileStructure(
        FileStructureNodesTest.ITEMS_INNER_CLASS);
    fileStructure.sendCommandByKeyboardInFileStructure(Keys.ARROW_RIGHT.toString());
    fileStructure.waitExpectedTextInFileStructure(FileStructureNodesTest.ITEMS_INNER_CLASS);
    fileStructure.selectItemInFileStructure(INTERFACE_NAME);
    fileStructure.waitExpectedTextIsNotPresentInFileStructure(
        FileStructureNodesTest.ITEMS_INTERFACE);
    fileStructure.sendCommandByKeyboardInFileStructure(Keys.ARROW_RIGHT.toString());
    fileStructure.waitExpectedTextInFileStructure(FileStructureNodesTest.ITEMS_INTERFACE);
    fileStructure.sendCommandByKeyboardInFileStructure(Keys.ARROW_LEFT.toString());
    fileStructure.waitExpectedTextIsNotPresentInFileStructure(
        FileStructureNodesTest.ITEMS_INTERFACE);
    fileStructure.sendCommandByKeyboardInFileStructure(Keys.ARROW_RIGHT.toString());
    fileStructure.waitExpectedTextInFileStructure(FileStructureNodesTest.ITEMS_INTERFACE);

    // check go on the root node after 'double click arrow left'
    fileStructure.sendCommandByKeyboardInFileStructure(Keys.ARROW_LEFT.toString());
    fileStructure.sendCommandByKeyboardInFileStructure(Keys.ARROW_LEFT.toString());
    fileStructure.sendCommandByKeyboardInFileStructure(Keys.ARROW_LEFT.toString());
    fileStructure.waitExpectedTextIsNotPresentInFileStructure(FileStructureNodesTest.ITEMS_CLASS);
    fileStructure.sendCommandByKeyboardInFileStructure(Keys.ARROW_RIGHT.toString());
    fileStructure.clickOnIconNodeInFileStructure(INTERFACE_NAME);
    fileStructure.clickOnIconNodeInFileStructure(INNER_CLASS_NAME);
    fileStructure.waitExpectedTextInFileStructure(FileStructureNodesTest.ITEMS_CLASS);

    // check scroll by keyboard
    fileStructure.selectItemInFileStructure(JAVA_FILE_NAME);
    fileStructure.waitExpectedTextInFileStructure(FileStructureNodesTest.ITEMS_CLASS);
    fileStructure.moveDownToItemInFileStructure("TEN");
    // TODO add code scroll up later
  }

  private void expandTReeProjectAndOpenClass(String fileName) {
    projectExplorer.openItemByPath(PROJECT_NAME + "/src");
    projectExplorer.waitItem(PROJECT_NAME + "/src" + "/main");
    projectExplorer.openItemByPath(PROJECT_NAME + "/src" + "/main");
    projectExplorer.waitItem(PROJECT_NAME + "/src" + "/main" + "/java");
    projectExplorer.openItemByPath(PROJECT_NAME + "/src" + "/main" + "/java");
    projectExplorer.waitItem(PROJECT_NAME + "/src" + "/main" + "/java" + "/com/codenvy/qa");
    projectExplorer.openItemByPath(PROJECT_NAME + "/src" + "/main" + "/java" + "/com/codenvy/qa");
    projectExplorer.waitItem(
        PROJECT_NAME + "/src" + "/main" + "/java" + "/com/codenvy/qa/" + fileName + ".java");
    projectExplorer.openItemByPath(
        PROJECT_NAME + "/src" + "/main" + "/java" + "/com/codenvy/qa/" + fileName + ".java");
    editor.waitActive();
  }
}
