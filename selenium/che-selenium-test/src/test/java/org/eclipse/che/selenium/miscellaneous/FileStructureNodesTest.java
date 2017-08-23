/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.miscellaneous;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Random;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.FileStructure;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev on 12.12.15 */
public class FileStructureNodesTest {
  private static final String PROJECT_NAME = "FileStructureNodes" + new Random().nextInt(999);
  private static final String JAVA_FILE_NAME = "Company";
  private static final String INNER_CLASS_NAME = "CompanyHelper";
  private static final String INTERFACE_NAME = "Inter";

  private static final String ITEMS_CLASS =
      "Company() : void\n"
          + "getInstance() : Company\n"
          + "doListId() : List<String>\n"
          + "doListName() : List<String>\n"
          + "doListDate() : List<String>\n"
          + "createListEmpl() : List<Employee>\n"
          + "createListEmpl(int) : List<Employee>\n"
          + "removeEmployee(String) : List<Employee>\n"
          + "getListEmployees() : List<Employee>\n"
          + "sortSalary() : List<Employee>\n"
          + "sortSurname() : List<Employee>\n"
          + "sortId() : List<Employee>\n"
          + "sortDate() : List<Employee>\n"
          + "listEmployees\n"
          + "listId\n"
          + "listName\n"
          + "listDate\n"
          + "CompanyHelper\n"
          + "INSTANCE\n"
          + "ONE\n"
          + "QWE\n"
          + "TWO\n"
          + "Inter\n"
          + "setDate() : void\n"
          + "getId() : double\n"
          + "getDate() : String\n"
          + "ASD\n"
          + "FIVE\n"
          + "TEN";

  private static final String ITEMS_CLASS_1 =
      "Company() : void\n"
          + "getInstance() : Company\n"
          + "doListId() : List<String>\n"
          + "doListName() : List<String>\n"
          + "doListDate() : List<String>\n"
          + "createListEmpl() : List<Employee>\n"
          + "createListEmpl(int) : List<Employee>\n"
          + "removeEmployee(String) : List<Employee>\n"
          + "getListEmployees() : List<Employee>\n"
          + "sortSalary() : List<Employee>\n"
          + "sortSurname() : List<Employee>\n"
          + "sortId() : List<Employee>\n"
          + "sortDate() : List<Employee>\n"
          + "listEmployees\n"
          + "listId\n"
          + "listName\n"
          + "listDate\n"
          + "CompanyHelper\n"
          + "Inter";

  private static final String ITEMS_INNER_CLASS = "INSTANCE\n" + "ONE\n" + "QWE\n" + "TWO\n";

  private static final String ITEMS_INTERFACE =
      "setDate() : void\n"
          + "getId() : double\n"
          + "getDate() : String\n"
          + "ASD\n"
          + "FIVE\n"
          + "TEN";

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private CodenvyEditor editor;
  @Inject private Menu menu;
  @Inject private FileStructure fileStructure;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/prOutline");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SIMPLE);
    ide.open(workspace);
  }

  @Test
  public void checkFileStructureNodes() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.openItemByPath(PROJECT_NAME);
    expandTReeProjectAndOpenClass(JAVA_FILE_NAME);

    // check work nodes in the 'file structure' by double click
    menu.runCommand(
        TestMenuCommandsConstants.Assistant.ASSISTANT,
        TestMenuCommandsConstants.Assistant.FILE_STRUCTURE);
    fileStructure.waitFileStructureFormIsOpen(JAVA_FILE_NAME);
    fileStructure.waitExpectedTextInFileStructure(ITEMS_CLASS);
    fileStructure.waitExpectedTextInFileStructure(ITEMS_INNER_CLASS);
    fileStructure.selectItemInFileStructureByDoubleClick(INNER_CLASS_NAME);
    fileStructure.waitExpectedTextIsNotPresentInFileStructure(ITEMS_INNER_CLASS);
    fileStructure.waitExpectedTextInFileStructure(ITEMS_INTERFACE);
    fileStructure.selectItemInFileStructureByDoubleClick(INTERFACE_NAME);
    fileStructure.waitExpectedTextIsNotPresentInFileStructure(ITEMS_INTERFACE);
    fileStructure.selectItemInFileStructureByDoubleClick(INTERFACE_NAME);
    fileStructure.waitExpectedTextInFileStructure(ITEMS_INTERFACE);
    fileStructure.selectItemInFileStructureByDoubleClick(INNER_CLASS_NAME);
    fileStructure.waitExpectedTextInFileStructure(ITEMS_INNER_CLASS);
    fileStructure.waitExpectedTextInFileStructure(ITEMS_CLASS);
    fileStructure.selectItemInFileStructureByDoubleClick(JAVA_FILE_NAME);
    fileStructure.waitExpectedTextIsNotPresentInFileStructure(ITEMS_CLASS);
    fileStructure.selectItemInFileStructureByDoubleClick(JAVA_FILE_NAME);
    fileStructure.waitExpectedTextInFileStructure(ITEMS_CLASS_1);

    // check work nodes in the 'file structure' by click on the icon
    fileStructure.clickOnIconNodeInFileStructure(INNER_CLASS_NAME);
    fileStructure.waitExpectedTextInFileStructure(ITEMS_INNER_CLASS);
    fileStructure.clickOnIconNodeInFileStructure(INNER_CLASS_NAME);
    fileStructure.waitExpectedTextIsNotPresentInFileStructure(ITEMS_INNER_CLASS);
    fileStructure.clickOnIconNodeInFileStructure(INTERFACE_NAME);
    fileStructure.waitExpectedTextInFileStructure(ITEMS_INTERFACE);
    fileStructure.clickOnIconNodeInFileStructure(INTERFACE_NAME);
    fileStructure.waitExpectedTextIsNotPresentInFileStructure(ITEMS_INTERFACE);
    fileStructure.clickOnIconNodeInFileStructure(INTERFACE_NAME);
    fileStructure.clickOnIconNodeInFileStructure(INNER_CLASS_NAME);
    fileStructure.waitExpectedTextInFileStructure(ITEMS_CLASS);
    fileStructure.clickOnIconNodeInFileStructure(JAVA_FILE_NAME);
    fileStructure.waitExpectedTextIsNotPresentInFileStructure(ITEMS_CLASS);
    fileStructure.clickOnIconNodeInFileStructure(JAVA_FILE_NAME);
    fileStructure.waitExpectedTextIsNotPresentInFileStructure(ITEMS_INNER_CLASS);
    fileStructure.waitExpectedTextIsNotPresentInFileStructure(ITEMS_INTERFACE);
    fileStructure.clickOnIconNodeInFileStructure(INNER_CLASS_NAME);
    fileStructure.clickOnIconNodeInFileStructure(INTERFACE_NAME);
    fileStructure.waitExpectedTextInFileStructure(ITEMS_CLASS);
  }

  public void expandTReeProjectAndOpenClass(String fileName) {
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
    editor.waitActiveEditor();
  }
}
