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
package org.eclipse.che.selenium.refactor.types;

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.ASSISTANT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.Refactoring.REFACTORING;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.Refactoring.RENAME;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Refactor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class RenameTypeTest {
  private static final Logger LOG = LoggerFactory.getLogger(RenameTypeTest.class);
  private static final String PROJECT_NAME = generate("project", 4);
  private static final String PATH_TO_PACKAGE_IN_CHE_PREFIX =
      PROJECT_NAME + "/src/main/java/renametype";

  private String pathToCurrentPackage;
  private String contentFromInA;
  private String contentFromOutB;

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private Refactor refactorPanel;
  @Inject private Consoles consoles;
  @Inject private Menu menu;
  @Inject private AskDialog askDialog;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setup() throws Exception {
    URL resource = RenameTypeTest.this.getClass().getResource("/projects/RenameType");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SIMPLE);

    ide.open(workspace);
    consoles.waitJDTLSProjectResolveFinishedMessage(PROJECT_NAME);
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    consoles.closeProcessesArea();
    projectExplorer.quickExpandWithJavaScript();
    loader.waitOnClosed();
  }

  @BeforeMethod
  public void setCurrentFieldForTest(Method method) throws IOException, URISyntaxException {
    setFieldsForTest(method.getName());
  }

  @AfterMethod
  public void closeForm() {
    try {
      if (refactorPanel.isWidgetOpened()) {
        loader.waitOnClosed();
        refactorPanel.clickCancelButtonRefactorForm();
      }
      editor.closeAllTabs();
    } catch (WebDriverException e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
  }

  @Test
  public void test0() {
    testCase();
  }

  @Test
  public void test1() {
    testCase();
  }

  @Test
  public void test2() {
    testCase();
  }

  @Test
  public void test3() {
    testCase();
  }

  @Test
  public void test4() {
    testCase();
  }

  @Test
  public void test5() {
    testCase();
  }

  @Test
  public void test6() {
    testCase();
  }

  @Test
  public void test7() {
    testCase();
  }

  @Test
  public void test8() {
    testCase();
  }

  @Test
  public void test9() {
    testCase();
  }

  private void setFieldsForTest(String nameCurrentTest) throws URISyntaxException, IOException {
    pathToCurrentPackage = PATH_TO_PACKAGE_IN_CHE_PREFIX + "/" + nameCurrentTest;

    URL resourcesInA =
        getClass()
            .getResource(
                "/org/eclipse/che/selenium/refactor/types/" + nameCurrentTest + "/in/A.java");
    URL resourcesOutA =
        getClass()
            .getResource(
                "/org/eclipse/che/selenium/refactor/types/" + nameCurrentTest + "/out/B.java");

    contentFromInA = getTextFromFile(resourcesInA);
    contentFromOutB = getTextFromFile(resourcesOutA);
  }

  private String getTextFromFile(URL url) throws URISyntaxException, IOException {
    String result = "";
    List<String> listWithAllLines =
        Files.readAllLines(Paths.get(url.toURI()), Charset.forName("UTF-8"));
    for (String buffer : listWithAllLines) {
      result += buffer + '\n';
    }

    return result;
  }

  private void testCase() {
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitTextIntoEditor(contentFromInA);
    projectExplorer.waitAndSelectItem(pathToCurrentPackage + "/A.java");
    menu.runCommand(ASSISTANT, REFACTORING, RENAME);

    refactorPanel.typeAndWaitNewName("B.java");
    refactorPanel.clickOkButtonRefactorForm();

    askDialog.waitFormToOpen();
    askDialog.clickOkBtn();
    askDialog.waitFormToClose();
    refactorPanel.waitRefactorPreviewFormIsClosed();
    projectExplorer.waitItem(pathToCurrentPackage + "/B.java", 6);

    try {
      editor.waitTextIntoEditor(contentFromOutB);
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known random failure https://github.com/eclipse/che/issues/11779");
    }
  }
}
