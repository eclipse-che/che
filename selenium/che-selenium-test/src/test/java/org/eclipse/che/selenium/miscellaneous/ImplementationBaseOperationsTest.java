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

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Random;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev on 12.01.16 */
public class ImplementationBaseOperationsTest {
  private static final String PROJECT_NAME =
      "OpenImplementationProject" + new Random().nextInt(999);
  private static final String JAVA_FILE_NAME = "Company";
  private static final String ABSTRACT_CLASS_NAME = "Empl";
  private static final String INTERFACE_NAME = "Employee";
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

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private CodenvyEditor editor;
  @Inject private Menu menu;
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
  public void checkImplementationInEditor() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.openItemByPath(PROJECT_NAME);
    expandTReeProjectAndOpenClass(JAVA_FILE_NAME);

    // check that the 'Implementation' container is not present
    editor.setCursorToLine(12);
    menu.runCommand(
        TestMenuCommandsConstants.Assistant.ASSISTANT,
        TestMenuCommandsConstants.Assistant.IMPLEMENTATION_S);
    editor.waitImplementationFormIsClosed(JAVA_FILE_NAME);

    // check the 'implementation' for simple java class
    editor.setCursorToLine(20);
    editor.clickOnSelectedElementInEditor(JAVA_FILE_NAME);
    menu.runCommand(
        TestMenuCommandsConstants.Assistant.ASSISTANT,
        TestMenuCommandsConstants.Assistant.IMPLEMENTATION_S);
    editor.waitImplementationFormIsOpen(JAVA_FILE_NAME);
    editor.cancelFormInEditorByEscape();
    editor.waitImplementationFormIsClosed(JAVA_FILE_NAME);
    editor.setCursorToLine(20);
    editor.clickOnSelectedElementInEditor(JAVA_FILE_NAME);
    editor.launchImplementationFormByKeyboard();
    editor.waitImplementationFormIsOpen(JAVA_FILE_NAME);
    editor.waitTextInImplementationForm(NO_FOUND_TEXT);

    // check the 'implementation' for abstract class
    projectExplorer.openItemByVisibleNameInExplorer(ABSTRACT_CLASS_NAME + ".java");
    editor.goToCursorPositionVisible(15, 25);
    editor.waitActive();
    editor.waitTextElementsActiveLine("Empl");
    editor.launchImplementationFormByKeyboard();
    editor.waitActiveTabFileName("EmployeeFixedSalary");
    editor.expectedNumberOfActiveLine(13);
    editor.waitTextElementsActiveLine("EmployeeFixedSalary extends Empl");
    editor.clickOnCloseFileIcon("EmployeeFixedSalary");
    editor.waitActiveTabFileName(ABSTRACT_CLASS_NAME);
    editor.selectTabByName(ABSTRACT_CLASS_NAME);
    editor.setCursorToLine(22);
    editor.clickOnSelectedElementInEditor("toString");
    menu.runCommand(
        TestMenuCommandsConstants.Assistant.ASSISTANT,
        TestMenuCommandsConstants.Assistant.IMPLEMENTATION_S);
    editor.waitActiveTabFileName("EmployeeFixedSalary");
    editor.expectedNumberOfActiveLine(38);
    editor.waitTextElementsActiveLine("toString");

    // check the 'implementations' for interface
    projectExplorer.openItemByVisibleNameInExplorer(INTERFACE_NAME + ".java");
    editor.goToCursorPositionVisible(15, 20);
    editor.waitTextElementsActiveLine("Employee");
    editor.launchImplementationFormByKeyboard();
    editor.waitActiveTabFileName("EmployeeHourlyWages");
    editor.expectedNumberOfActiveLine(14);
    editor.waitTextElementsActiveLine("EmployeeHourlyWages implements Employee");
    editor.clickOnCloseFileIcon("EmployeeHourlyWages");
    editor.waitActiveTabFileName(INTERFACE_NAME);
    editor.selectTabByName(INTERFACE_NAME);
    editor.setCursorToLine(18);
    editor.clickOnSelectedElementInEditor("toString");
    menu.runCommand(
        TestMenuCommandsConstants.Assistant.ASSISTANT,
        TestMenuCommandsConstants.Assistant.IMPLEMENTATION_S);
    editor.waitActiveTabFileName("EmployeeHourlyWages");
    editor.expectedNumberOfActiveLine(58);
    editor.waitTextElementsActiveLine("toString");
    editor.selectTabByName(INTERFACE_NAME);
    editor.setCursorToLine(15);
    editor.waitTextElementsActiveLine("interface Employee extends Serializable");
    editor.clickOnSelectedElementInEditor("Serializable");
    menu.runCommand(
        TestMenuCommandsConstants.Assistant.ASSISTANT,
        TestMenuCommandsConstants.Assistant.IMPLEMENTATION_S);
    editor.waitImplementationFormIsOpen("Serializable");
    editor.waitTextInImplementationForm(LIST_IMPLEMENTATIONS);
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitImplementationFormIsClosed("Serializable");
    editor.waitActiveTabFileName(ABSTRACT_CLASS_NAME);
    editor.setCursorToLine(15);
    editor.waitTextElementsActiveLine("class Empl implements Serializable");
    editor.selectTabByName(INTERFACE_NAME);
    editor.launchImplementationFormByKeyboard();
    editor.waitImplementationFormIsOpen("Serializable");
    editor.waitTextInImplementationForm(LIST_IMPLEMENTATIONS);
    editor.selectImplementationByClick("EmployeeHourlyWages");
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitImplementationFormIsClosed("Serializable");
    editor.waitActiveTabFileName("EmployeeHourlyWages");
    editor.selectTabByName(INTERFACE_NAME);
    menu.runCommand(
        TestMenuCommandsConstants.Assistant.ASSISTANT,
        TestMenuCommandsConstants.Assistant.IMPLEMENTATION_S);
    editor.waitImplementationFormIsOpen("Serializable");
    editor.waitTextInImplementationForm(LIST_IMPLEMENTATIONS);
    editor.chooseImplementationByDoubleClick("EmployeeFixedSalary");
    editor.waitImplementationFormIsClosed("Serializable");
    editor.waitActiveTabFileName("EmployeeFixedSalary");
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
    editor.waitActive();
  }
}
