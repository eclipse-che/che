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
package org.eclipse.che.selenium.refactor.parameters;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Refactor;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev on 23.11.15 */
public class FailParametersTest {
  private static final String PROJECT_NAME = NameGenerator.generate("FailParametersProject-", 4);
  private static final String pathToPackageInChePrefix = PROJECT_NAME + "/src/main/java";

  private String pathToCurrentPackage;
  private String contentFromInA;

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private Refactor refactor;
  @Inject private AskDialog askDialog;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private Consoles consoles;

  @BeforeClass
  public void prepare() throws Exception {
    URL resource = getClass().getResource("/projects/rename-parameters");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SIMPLE);
    ide.open(workspace);
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
    loader.waitOnClosed();
  }

  @Test
  public void testFail2() throws Exception {
    consoles.closeProcessesArea();
    projectExplorer.scrollToItemByPath(PROJECT_NAME + "/src/main/webapp");
    setFieldsForTest("testfail2");
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActiveEditor();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToDefinedLineAndChar(14, 23);
    editor.launchRefactorFormFromEditor();
    editor.launchRefactorFormFromEditor();
    refactor.waitRenameParametersFormIsOpen();
    refactor.waitUpdateReferencesIsSelected();
    refactor.typeAndWaitNewName("i");
    refactor.clickOkButtonRefactorForm();
    askDialog.acceptDialogWithText("Duplicate parameter i");
    refactor.waitRenameParametersFormIsClosed();
    editor.closeFileByNameWithSaving("A");
  }

  @Test(priority = 1)
  public void testFail3() throws Exception {
    setFieldsForTest("testfail3");
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActiveEditor();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToDefinedLineAndChar(14, 15);
    editor.launchRefactorFormFromEditor();
    editor.launchRefactorFormFromEditor();
    refactor.waitRenameParametersFormIsOpen();
    refactor.waitUpdateReferencesIsSelected();
    refactor.typeAndWaitNewName("9");
    refactor.waitTextInErrorMessage("'9' is not a valid Java identifier");
    refactor.clickCancelButtonRefactorForm();
    refactor.waitRenameParametersFormIsClosed();
    editor.closeFileByNameWithSaving("A");
  }

  @Test(priority = 2)
  public void testFail7() throws Exception {
    setFieldsForTest("testfail7");
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActiveEditor();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToDefinedLineAndChar(17, 16);
    editor.launchRefactorFormFromEditor();
    editor.launchRefactorFormFromEditor();
    refactor.waitRenameParametersFormIsOpen();
    refactor.waitUpdateReferencesIsSelected();
    refactor.typeAndWaitNewName("j");
    refactor.clickOkButtonRefactorForm();
    askDialog.acceptDialogWithText("Name collision with name 'j'");
    refactor.waitRenameParametersFormIsClosed();
    editor.closeFileByNameWithSaving("A");
  }

  @Test(priority = 3)
  public void testFail11() throws Exception {
    setFieldsForTest("testfail11");
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActiveEditor();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToDefinedLineAndChar(14, 16);
    editor.launchRefactorFormFromEditor();
    editor.launchRefactorFormFromEditor();
    refactor.waitRenameParametersFormIsOpen();
    refactor.waitUpdateReferencesIsSelected();
    refactor.typeAndWaitNewName("j");
    refactor.clickOkButtonRefactorForm();
    askDialog.acceptDialogWithText("Duplicate parameter j");
    refactor.waitRenameParametersFormIsClosed();
    editor.closeFileByNameWithSaving("A");
  }

  @Test(priority = 4)
  public void testFail14() throws Exception {
    setFieldsForTest("testfail14");
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActiveEditor();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToDefinedLineAndChar(18, 15);
    editor.launchRefactorFormFromEditor();
    editor.launchRefactorFormFromEditor();
    refactor.waitRenameParametersFormIsOpen();
    refactor.waitUpdateReferencesIsSelected();
    refactor.typeAndWaitNewName("j");
    refactor.clickOkButtonRefactorForm();
    askDialog.acceptDialogWithText("Name collision with name 'j'");
    refactor.waitRenameParametersFormIsClosed();
    editor.closeFileByNameWithSaving("A");
  }

  @Test(priority = 5)
  public void testFail17() throws Exception {
    setFieldsForTest("testfail17");
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActiveEditor();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToDefinedLineAndChar(14, 17);
    editor.launchRefactorFormFromEditor();
    editor.launchRefactorFormFromEditor();
    refactor.waitRenameParametersFormIsOpen();
    refactor.waitUpdateReferencesIsSelected();
    refactor.typeAndWaitNewName("j");
    refactor.clickOkButtonRefactorForm();
    askDialog.acceptDialogWithText("Duplicate parameter j");
    refactor.waitRenameParametersFormIsClosed();
    editor.closeFileByNameWithSaving("A");
  }

  @Test(priority = 6)
  public void testFail20() throws Exception {
    setFieldsForTest("testfail20");
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActiveEditor();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToDefinedLineAndChar(17, 17);
    editor.launchRefactorFormFromEditor();
    editor.launchRefactorFormFromEditor();
    refactor.waitRenameParametersFormIsOpen();
    refactor.waitUpdateReferencesIsSelected();
    refactor.typeAndWaitNewName("j");
    refactor.clickOkButtonRefactorForm();
    askDialog.acceptDialogWithText("Name collision with name 'j'");
    refactor.waitRenameParametersFormIsClosed();
    editor.closeFileByNameWithSaving("A");
  }

  private void setFieldsForTest(String nameCurrentTest) throws Exception {
    pathToCurrentPackage = pathToPackageInChePrefix + "/" + nameCurrentTest;
    URL resourcesIn =
        getClass()
            .getResource(
                "/org/eclipse/che/selenium/refactor/parameters/" + nameCurrentTest + "/in/A.java");
    contentFromInA = getTextFromFile(resourcesIn);
  }

  private String getTextFromFile(URL url) throws Exception {
    String result = "";
    List<String> listWithAllLines =
        Files.readAllLines(Paths.get(url.toURI()), Charset.forName("UTF-8"));
    for (String buffer : listWithAllLines) {
      result += buffer + '\n';
    }

    return result;
  }
}
