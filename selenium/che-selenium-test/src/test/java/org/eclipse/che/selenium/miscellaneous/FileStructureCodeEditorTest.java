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
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.ASSISTANT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.FILE_STRUCTURE;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.FileStructure;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev on 15.12.15 */
public class FileStructureCodeEditorTest {
  private static final String PROJECT_NAME = generate("project", 5);
  private static final String JAVA_FILE_NAME = "Company";

  private static final String NEW_CONTENT =
      "private int a;\n" + "private long b;\n" + "private String s;";

  private static final String EXPECTED_TEXT =
      "private int a;\n" + "private long b;\n" + "private String s;";

  private static final String NEW_ITEMS = "a\n" + "b\n" + "s";

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private CodenvyEditor editor;
  @Inject private Menu menu;
  @Inject private FileStructure fileStructure;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private Consoles consoles;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/prOutline");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
    ide.open(workspace);
    consoles.waitJDTLSProjectResolveFinishedMessage(PROJECT_NAME);
  }

  @Test
  public void checkFileStructureCodeEditor() {
    ide.waitOpenedWorkspaceIsReadyToUse();
    projectExplorer.openItemByPath(PROJECT_NAME);
    expandTReeProjectAndOpenClass(JAVA_FILE_NAME);

    // check the highlighted item in editor
    menu.runCommand(ASSISTANT, FILE_STRUCTURE);
    fileStructure.waitFileStructureFormIsOpen(JAVA_FILE_NAME);
    fileStructure.selectItemInFileStructureByDoubleClick("getInstance():Company");
    fileStructure.waitFileStructureFormIsClosed();
    editor.waitSpecifiedValueForLineAndChar(41, 38);

    menu.runCommand(ASSISTANT, FILE_STRUCTURE);
    fileStructure.waitFileStructureFormIsOpen(JAVA_FILE_NAME);
    fileStructure.selectItemInFileStructure("INSTANCE");
    fileStructure.selectItemInFileStructureByEnter("INSTANCE");
    fileStructure.waitFileStructureFormIsClosed();
    editor.waitSpecifiedValueForLineAndChar(25, 46);

    menu.runCommand(ASSISTANT, FILE_STRUCTURE);
    fileStructure.waitFileStructureFormIsOpen(JAVA_FILE_NAME);
    fileStructure.selectItemInFileStructure("getId():double");
    fileStructure.selectItemInFileStructureByEnter("getId():double");
    fileStructure.waitFileStructureFormIsClosed();
    editor.waitSpecifiedValueForLineAndChar(37, 28);

    // check new elements in the 'file structure' form
    editor.setCursorToLine(20);
    editor.waitActive();
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.typeTextIntoEditor(NEW_CONTENT);
    editor.waitTextIntoEditor(EXPECTED_TEXT);
    menu.runCommand(ASSISTANT, FILE_STRUCTURE);
    fileStructure.waitFileStructureFormIsOpen(JAVA_FILE_NAME);
    fileStructure.waitExpectedTextInFileStructure(NEW_ITEMS);
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
