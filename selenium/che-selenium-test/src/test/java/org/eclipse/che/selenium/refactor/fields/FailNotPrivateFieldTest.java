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
package org.eclipse.che.selenium.refactor.fields;

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
import org.openqa.selenium.Keys;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev on 16.11.15 */
public class FailNotPrivateFieldTest {
  private static final String PROJECT_NAME =
      NameGenerator.generate("FailNotPrivateFieldProject", 4);
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
    URL resource = getClass().getResource("/projects/rename-non-private-field");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SIMPLE);
    ide.open(workspace);
    consoles.waitJDTLSProjectResolveFinishedMessage(PROJECT_NAME);
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
    loader.waitOnClosed();
  }

  @AfterMethod
  public void closeForm() {
    if (refactor.isWidgetOpened()) {
      refactor.clickCancelButtonRefactorForm();
    }
    if (editor.isAnyTabsOpened()) {
      editor.closeAllTabs();
    }
  }

  @Test
  public void testFail0() throws Exception {
    setFieldsForTest("testfail0");
    projectExplorer.scrollToItemByPath(PROJECT_NAME + "/src/main/webapp");
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToLine(14);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchRefactorForm();
    refactor.waitRenameFieldFormIsOpen();
    refactor.waitUpdateReferencesIsSelected();
    refactor.typeAndWaitNewName("g");
    refactor.waitTextInErrorMessage("A field with this name is already defined.");
    refactor.clickCancelButtonRefactorForm();
    refactor.waitRenameFieldFormIsClosed();
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void testFail3() throws Exception {
    setFieldsForTest("testfail3");
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToLine(14);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchRefactorForm();
    refactor.waitRenameFieldFormIsOpen();
    refactor.waitUpdateReferencesIsSelected();
    refactor.typeAndWaitNewName("g");
    refactor.waitTextInErrorMessage("A field with this name is already defined.");
    refactor.clickCancelButtonRefactorForm();
    refactor.waitRenameFieldFormIsClosed();
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void testFail7() throws Exception {
    setFieldsForTest("testfail7");
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToLine(14);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchRefactorForm();
    refactor.waitRenameFieldFormIsOpen();
    refactor.waitUpdateReferencesIsSelected();
    refactor.typeAndWaitNewName("g");
    refactor.clickOkButtonRefactorForm();
    askDialog.acceptDialogWithText(
        "Problem in 'A.java'. Another name will shadow access to the renamed element");
    refactor.waitRenameFieldFormIsClosed();
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void testFail10() throws Exception {
    setFieldsForTest("testfail10");
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToLine(14);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchRefactorForm();
    refactor.waitRenameFieldFormIsOpen();
    refactor.waitUpdateReferencesIsSelected();
    refactor.typeAndWaitNewName("g");
    refactor.clickOkButtonRefactorForm();
    askDialog.acceptDialogWithText(
        "Problem in 'A.java'. Another name will shadow access to the renamed element");
    refactor.waitRenameFieldFormIsClosed();
    editor.closeFileByNameWithSaving("A");
  }

  @Test
  public void testFail14() throws Exception {
    setFieldsForTest("testfail14");
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitActive();
    editor.waitTextIntoEditor(contentFromInA);
    editor.setCursorToLine(14);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.launchRefactorForm();
    refactor.waitRenameFieldFormIsOpen();
    refactor.waitUpdateReferencesIsSelected();
    refactor.typeAndWaitNewName("g");
    refactor.clickOkButtonRefactorForm();
    askDialog.acceptDialogWithText(
        "Problem in 'A.java'. Another name will shadow access to the renamed element");
    refactor.waitRenameFieldFormIsClosed();
    editor.closeFileByNameWithSaving("A");
  }

  private void setFieldsForTest(String nameCurrentTest) throws Exception {
    pathToCurrentPackage = pathToPackageInChePrefix + "/" + nameCurrentTest;

    URL resources =
        getClass()
            .getResource(
                "/org/eclipse/che/selenium/refactor/fields/none-private/"
                    + nameCurrentTest
                    + "/in/A.java");
    List<String> listWithAllLines =
        Files.readAllLines(Paths.get(resources.toURI()), Charset.forName("UTF-8"));
    contentFromInA = "";
    for (String buffer : listWithAllLines) {
      contentFromInA += buffer + '\n';
    }
  }
}
