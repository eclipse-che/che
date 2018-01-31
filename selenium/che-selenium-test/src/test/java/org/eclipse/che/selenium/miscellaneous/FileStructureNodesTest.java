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
package org.eclipse.che.selenium.miscellaneous;

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.ASSISTANT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.FILE_STRUCTURE;
import static org.eclipse.che.selenium.core.project.ProjectTemplates.MAVEN_SIMPLE;
import static org.openqa.selenium.Keys.ESCAPE;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.FileStructure;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.openqa.selenium.WebDriverException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Aleksandr Shmaraev
 * @author Serhii Skoryk
 */
public class FileStructureNodesTest {
  private static final String PROJECT_NAME = NameGenerator.generate("project", 4);
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

  private static final String ITEMS_FILTERED_GET =
      "Company\n"
          + "getInstance() : Company\n"
          + "getListEmployees() : List<Employee>\n"
          + "Inter\n"
          + "getId() : double\n"
          + "getDate() : String\n";

  private static final String ITEMS_FILTERED_I =
      "Company\n"
          + "getInstance() : Company\n"
          + "doListId() : List<String>\n"
          + "doListName() : List<String>\n"
          + "doListDate() : List<String>\n"
          + "createListEmpl() : List<Employee>\n"
          + "createListEmpl(int) : List<Employee>\n"
          + "getListEmployees() : List<Employee>\n"
          + "sortId() : List<Employee>\n"
          + "listEmployees\n"
          + "listId\n"
          + "listName\n"
          + "listDate\n"
          + "CompanyHelper\n"
          + "INSTANCE\n"
          + "Inter\n"
          + "getId() : double\n"
          + "FIVE\n";

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Menu menu;
  @Inject private FileStructure fileStructure;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/prOutline");
    testProjectServiceClient.importProject(
        workspace.getId(), Paths.get(resource.toURI()), PROJECT_NAME, MAVEN_SIMPLE);
    ide.open(workspace);
  }

  @Test
  void checkFileStructureFilter() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByVisibleNameInExplorer("Company.java");

    menu.runCommand(ASSISTANT, FILE_STRUCTURE);
    fileStructure.waitFileStructureFormIsOpen(JAVA_FILE_NAME);

    try {
      fileStructure.waitExpectedTextInFileStructure(ITEMS_CLASS);
    } catch (WebDriverException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/8300");
    }

    fileStructure.type("get");
    fileStructure.waitExpectedTextInFileStructure(ITEMS_FILTERED_GET);
    fileStructure.type(ESCAPE.toString());
    fileStructure.waitExpectedTextInFileStructure(ITEMS_CLASS);
    fileStructure.type("i");
    fileStructure.waitExpectedTextInFileStructure(ITEMS_FILTERED_I);
    fileStructure.type(ESCAPE.toString());
  }

  @Test(priority = 1)
  public void checkFileStructureNodes() {
    // check work nodes in the 'file structure' by double click
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
    fileStructure.clickOnIconNodeInFileStructure(JAVA_FILE_NAME);
    fileStructure.waitExpectedTextIsNotPresentInFileStructure(ITEMS_INNER_CLASS);
    fileStructure.waitExpectedTextIsNotPresentInFileStructure(ITEMS_INTERFACE);
    fileStructure.clickOnIconNodeInFileStructure(INNER_CLASS_NAME);
    fileStructure.clickOnIconNodeInFileStructure(INTERFACE_NAME);
    fileStructure.waitExpectedTextInFileStructure(ITEMS_CLASS);

    fileStructure.closeFileStructureFormByEscape();
    fileStructure.waitFileStructureFormIsClosed();
  }
}
