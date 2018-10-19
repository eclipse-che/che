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
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.IMPLEMENTATION_S;
import static org.eclipse.che.selenium.core.project.ProjectTemplates.MAVEN_SIMPLE;
import static org.openqa.selenium.Keys.ARROW_LEFT;
import static org.openqa.selenium.Keys.ENTER;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev on 12.01.16 */
public class ImplementationBaseOperationsTest {
  private static final String PROJECT_NAME = generate("project", 5);
  private static final String JAVA_FILE_NAME = "Company";
  private static final String ABSTRACT_CLASS_NAME = "Empl";
  private static final String INTERFACE_NAME = "Employee";
  private static final String GENERAL_INTERFACE_NAME = "Remote";
  private static final String NO_FOUND_TEXT = "No implementations found";
  private static final String LIST_IMPLEMENTATIONS =
      "Empl - (/"
          + PROJECT_NAME
          + "/src/main/java/com/codenvy/qa/Empl.java)\n"
          + "EmployeeFixedSalary - (/"
          + PROJECT_NAME
          + "/src/main/java/com/codenvy/qa/EmployeeFixedSalary.java)\n"
          + "Employee - (/"
          + PROJECT_NAME
          + "/src/main/java/com/codenvy/qa/Employee.java)\n"
          + "EmployeeHourlyWages - (/"
          + PROJECT_NAME
          + "/src/main/java/com/codenvy/qa/EmployeeHourlyWages.java)";

  @Inject private Ide ide;
  @Inject private Menu menu;
  @Inject private Consoles consoles;
  @Inject private CodenvyEditor editor;
  @Inject private TestWorkspace workspace;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/prOutline");
    testProjectServiceClient.importProject(
        workspace.getId(), Paths.get(resource.toURI()), PROJECT_NAME, MAVEN_SIMPLE);
    ide.open(workspace);
    ide.waitOpenedWorkspaceIsReadyToUse();
    consoles.waitJDTLSProjectResolveFinishedMessage(PROJECT_NAME);
  }

  @Test
  public void checkImplementationInEditor() {
    projectExplorer.openItemByPath(PROJECT_NAME);
    expandTReeProjectAndOpenClass(JAVA_FILE_NAME);

    // check that the 'Implementation' container is not present
    editor.setCursorToLine(13);
    menu.runCommand(ASSISTANT, IMPLEMENTATION_S);
    editor.waitImplementationFormIsClosed(JAVA_FILE_NAME);

    // check the 'implementation' for simple java class
    editor.setCursorToLine(21);
    editor.clickOnSelectedElementInEditor(JAVA_FILE_NAME);
    menu.runCommand(ASSISTANT, IMPLEMENTATION_S);
    editor.waitImplementationFormIsOpen(JAVA_FILE_NAME);
    editor.cancelFormInEditorByEscape();
    editor.waitImplementationFormIsClosed(JAVA_FILE_NAME);
    editor.setCursorToLine(21);
    editor.clickOnSelectedElementInEditor(JAVA_FILE_NAME);
    editor.launchImplementationFormByKeyboard();
    editor.waitImplementationFormIsOpen(JAVA_FILE_NAME);
    editor.waitTextInImplementationForm(NO_FOUND_TEXT);

    // check the 'implementation' for abstract class
    projectExplorer.openItemByVisibleNameInExplorer(ABSTRACT_CLASS_NAME + ".java");
    editor.goToCursorPositionVisible(16, 25);
    editor.waitActive();
    editor.waitTextElementsActiveLine(ABSTRACT_CLASS_NAME);
    editor.launchImplementationFormByKeyboard();
    editor.waitActiveTabFileName("EmployeeFixedSalary");
    editor.typeTextIntoEditor(ARROW_LEFT.toString());
    editor.waitSpecifiedValueForLineAndChar(14, 14);
    editor.waitTextElementsActiveLine("EmployeeFixedSalary extends Empl");
    editor.clickOnCloseFileIcon("EmployeeFixedSalary");
    editor.waitActiveTabFileName(ABSTRACT_CLASS_NAME);
    editor.selectTabByName(ABSTRACT_CLASS_NAME);
    editor.setCursorToLine(23);
    editor.clickOnSelectedElementInEditor("toString");
    menu.runCommand(ASSISTANT, IMPLEMENTATION_S);
    editor.waitActiveTabFileName("EmployeeFixedSalary");
    editor.typeTextIntoEditor(ARROW_LEFT.toString());
    editor.waitSpecifiedValueForLineAndChar(39, 19);
    editor.waitTextElementsActiveLine("toString");

    // check the 'implementations' for interface
    projectExplorer.openItemByVisibleNameInExplorer(INTERFACE_NAME + ".java");
    editor.goToCursorPositionVisible(16, 20);
    editor.waitTextElementsActiveLine(INTERFACE_NAME);
    editor.launchImplementationFormByKeyboard();
    editor.waitActiveTabFileName("EmployeeHourlyWages");
    editor.expectedNumberOfActiveLine(15);
    editor.typeTextIntoEditor(ARROW_LEFT.toString());
    editor.waitSpecifiedValueForLineAndChar(15, 14);
    editor.waitTextElementsActiveLine("EmployeeHourlyWages implements Employee");
    editor.clickOnCloseFileIcon("EmployeeHourlyWages");
    editor.waitActiveTabFileName(INTERFACE_NAME);
    editor.selectTabByName(INTERFACE_NAME);
    editor.setCursorToLine(19);
    editor.clickOnSelectedElementInEditor("toString");
    menu.runCommand(ASSISTANT, IMPLEMENTATION_S);
    editor.waitActiveTabFileName("EmployeeHourlyWages");
    editor.typeTextIntoEditor(ARROW_LEFT.toString());
    editor.waitSpecifiedValueForLineAndChar(59, 19);
    editor.waitTextElementsActiveLine("toString");
    editor.selectTabByName(INTERFACE_NAME);
    editor.goToCursorPositionVisible(16, 38);
    editor.waitTextElementsActiveLine("interface Employee extends Remote");
    menu.runCommand(ASSISTANT, IMPLEMENTATION_S);
    editor.waitImplementationFormIsOpen(GENERAL_INTERFACE_NAME);

    editor.waitTextInImplementationForm(LIST_IMPLEMENTATIONS);
    editor.typeTextIntoEditor(ENTER.toString());
    editor.waitImplementationFormIsClosed(GENERAL_INTERFACE_NAME);
    editor.waitActiveTabFileName(ABSTRACT_CLASS_NAME);
    editor.setCursorToLine(16);
    editor.waitTextElementsActiveLine("class Empl implements Remote");
    editor.selectTabByName(INTERFACE_NAME);
    editor.launchImplementationFormByKeyboard();
    editor.waitImplementationFormIsOpen(GENERAL_INTERFACE_NAME);
    editor.waitTextInImplementationForm(LIST_IMPLEMENTATIONS);
    editor.selectImplementationByClick("EmployeeHourlyWages");
    editor.typeTextIntoEditor(ENTER.toString());
    editor.waitImplementationFormIsClosed(GENERAL_INTERFACE_NAME);
    editor.waitActiveTabFileName("EmployeeHourlyWages");
    editor.selectTabByName(INTERFACE_NAME);
    menu.runCommand(ASSISTANT, IMPLEMENTATION_S);
    editor.waitImplementationFormIsOpen(GENERAL_INTERFACE_NAME);
    editor.waitTextInImplementationForm(LIST_IMPLEMENTATIONS);
    editor.chooseImplementationByDoubleClick("EmployeeFixedSalary");
    editor.waitImplementationFormIsClosed(GENERAL_INTERFACE_NAME);
    editor.waitActiveTabFileName("EmployeeFixedSalary");
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
