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
package org.eclipse.che.selenium.refactor.methods;

import com.google.inject.Inject;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class RenamePrivateMethodTest {
  private static final Logger LOG = LoggerFactory.getLogger(RenamePrivateMethodTest.class);
  private static final String nameOfProject =
      RenamePrivateMethodTest.class.getSimpleName() + new Random().nextInt(9999);
  private static final String pathToPackageInChePrefix =
      nameOfProject + "/src" + "/main" + "/java" + "/renamePrivateMethods";

  private String pathToCurrentPackage;
  private String contentFromInA;
  private String contentFromOutB;

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private Refactor refactor;
  @Inject private AskDialog askDialog;
  @Inject private Consoles consoles;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setup() throws Exception {
    URL resource = getClass().getResource("/projects/RenameMethods");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        nameOfProject,
        ProjectTemplates.MAVEN_SIMPLE);
    ide.open(workspace);
    ide.waitOpenedWorkspaceIsReadyToUse();
    consoles.waitJDTLSProjectResolveFinishedMessage(nameOfProject);
    projectExplorer.waitVisibleItem(nameOfProject);
    consoles.closeProcessesArea();
    projectExplorer.quickExpandWithJavaScript();
    loader.waitOnClosed();
  }

  @BeforeMethod
  public void expandTreeOfProject(Method testName) {
    try {
      setFieldsForTest(testName.getName());
    } catch (Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
  }

  @AfterMethod
  public void closeForm() {
    try {
      if (refactor.isWidgetOpened()) {
        refactor.clickCancelButtonRefactorForm();
      }
      editor.closeAllTabs();

    } catch (Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
  }

  @Test
  public void test0() {
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitTextIntoEditor(contentFromInA);
    editor.goToCursorPositionVisible(15, 18);
    editor.launchLocalRefactor();
    editor.typeTextIntoEditor("k");
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitTextIntoEditor(contentFromOutB);
  }

  @Test
  public void test2() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitTextIntoEditor(contentFromInA);
    editor.goToCursorPositionVisible(15, 18);
    editor.launchLocalRefactor();
    editor.typeTextIntoEditor("fred");
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitTextIntoEditor(contentFromOutB);
  }

  @Test
  public void test10() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitTextIntoEditor(contentFromInA);
    editor.goToCursorPositionVisible(14, 18);
    editor.launchLocalRefactor();
    editor.typeTextIntoEditor("k");
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitTextIntoEditor(contentFromOutB);
  }

  @Test
  public void test11() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitTextIntoEditor(contentFromInA);
    editor.goToCursorPositionVisible(14, 18);
    editor.launchLocalRefactor();
    editor.typeTextIntoEditor("k");
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitTextIntoEditor(contentFromOutB);
  }

  @Test
  public void test12() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitTextIntoEditor(contentFromInA);
    editor.goToCursorPositionVisible(14, 18);
    editor.launchLocalRefactor();
    editor.typeTextIntoEditor("k");
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitTextIntoEditor(contentFromOutB);
  }

  @Test
  public void test23() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitTextIntoEditor(contentFromInA);
    editor.goToCursorPositionVisible(14, 18);
    editor.launchLocalRefactor();
    editor.typeTextIntoEditor("k");
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitTextIntoEditor(contentFromOutB);
  }

  @Test
  public void testAnon0() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitTextIntoEditor(contentFromInA);
    editor.goToCursorPositionVisible(14, 18);
    editor.launchLocalRefactor();
    editor.typeTextIntoEditor("k");
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitTextIntoEditor(contentFromOutB);
  }

  @Test
  public void testFail5() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitTextIntoEditor(contentFromInA);
    editor.goToCursorPositionVisible(14, 18);
    editor.launchRefactorForm();
    refactor.waitRenameMethodFormIsOpen();
    refactor.typeAndWaitNewName("k");
    refactor.clickOkButtonRefactorForm();
    askDialog.waitFormToOpen();
    askDialog.containsText(
        "Problem in 'A.java'. Another name will shadow access to the renamed element");
    askDialog.clickCancelBtn();
    askDialog.waitFormToClose();
    refactor.clickCancelButtonRefactorForm();
  }

  private void setFieldsForTest(String nameCurrentTest) throws Exception {
    pathToCurrentPackage = pathToPackageInChePrefix + "/" + nameCurrentTest;

    URL resourcesIn =
        getClass()
            .getResource(
                "/org/eclipse/che/selenium/refactor/methods/private/"
                    + nameCurrentTest
                    + "/in/A.java");
    URL resourcesOut =
        getClass()
            .getResource(
                "/org/eclipse/che/selenium/refactor/methods/private/"
                    + nameCurrentTest
                    + "/out/B.java");

    contentFromInA = getTextFromFile(resourcesIn);
    contentFromOutB = getTextFromFile(resourcesOut);
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
