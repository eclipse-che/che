/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
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
    consoles.closeProcessesArea();
    projectExplorer.scrollToItemByPath(PROJECT_NAME + "/src/main/webapp");
  }

  @AfterMethod
  public void closeCurrentTab() {
    if (refactor.isWidgetOpened()) {
      refactor.clickCancelButtonRefactorForm();
    }
    editor.closeAllTabs();
  }

  @Test(dataProvider = "checkRefactoringDataWthConfirmBtnClick")
  public void testFail2(TestParams params) throws Exception {
    checkRefactoring(params);
  }

  private void checkRefactoring(TestParams testParamObj) throws Exception {
    setFieldsForTest(testParamObj.getNameTest());
    projectExplorer.openItemByPath(pathToCurrentPackage);
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.goToCursorPositionVisible(
        testParamObj.getStrCursorPosition(), testParamObj.getLineCursorPosition());
    editor.launchRefactorForm();
    refactor.waitRenameParametersFormIsOpen();
    refactor.waitUpdateReferencesIsSelected();
    refactor.typeAndWaitNewName(testParamObj.getRefactorValue());
    if (testParamObj.isHandleRefactorWithConfirming()) {
      refactor.clickOkButtonRefactorForm();
      askDialog.acceptDialogWithText(testParamObj.getExpectedDialogTextInRefactorWidget());
    } else {
      refactor.waitTextInErrorMessage(testParamObj.getExpectedDialogTextInRefactorWidget());
      refactor.clickCancelButtonRefactorForm();
    }
    refactor.waitRenameParametersFormIsClosed();
  }

  private void setFieldsForTest(String nameCurrentTest) throws Exception {
    pathToCurrentPackage = pathToPackageInChePrefix + "/" + nameCurrentTest + "/A.java";
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

  private static class TestParams {
    private String nameTest;
    private int strCursorPosition;
    private int lineCursorPosition;

    private boolean handleRefactorWithConfirming;

    private String refactorValue;
    private String expectedDialogTextInRefactorWidget;

    public TestParams(
        String nameTest,
        int strCursorPosition,
        int lineCursorPosition,
        String refactorValue,
        String expectedDialogTextInRefactorWidget,
        boolean handleRefactorWithConfirming) {
      this.nameTest = nameTest;
      this.strCursorPosition = strCursorPosition;
      this.lineCursorPosition = lineCursorPosition;
      this.refactorValue = refactorValue;
      this.expectedDialogTextInRefactorWidget = expectedDialogTextInRefactorWidget;
      this.handleRefactorWithConfirming = handleRefactorWithConfirming;
    }

    public String getNameTest() {
      return nameTest;
    }

    public boolean isHandleRefactorWithConfirming() {
      return handleRefactorWithConfirming;
    }

    public int getStrCursorPosition() {
      return strCursorPosition;
    }

    public int getLineCursorPosition() {
      return lineCursorPosition;
    }

    public String getRefactorValue() {
      return refactorValue;
    }

    public String getExpectedDialogTextInRefactorWidget() {
      return expectedDialogTextInRefactorWidget;
    }
  }

  @DataProvider(name = "checkRefactoringDataWthConfirmBtnClick")
  private Object[][] refactorParameters() {
    return new Object[][] {
      {new TestParams("testfail2", 15, 23, "i", "Duplicate parameter i", true)},
      {new TestParams("testfail3", 15, 15, "9", "'9' is not a valid Java identifier", false)},
      {new TestParams("testfail7", 18, 16, "j", "Name collision with name 'j'", true)},
      {new TestParams("testfail11", 15, 16, "j", "Duplicate parameter j", true)},
      {new TestParams("testfail14", 19, 15, "j", "Name collision with name 'j'", true)},
      {new TestParams("testfail17", 15, 17, "j", "Duplicate parameter j", true)},
      {new TestParams("testfail20", 18, 17, "j", "Name collision with name 'j'", true)}
    };
  }
}
