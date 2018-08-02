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
import static org.testng.Assert.assertEquals;

import com.google.inject.Inject;
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
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Refactor;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class TestEnumerationsTest {
  private static final String PROJECT_NAME = generate("project", 4);
  private static final String PATH_TO_PACKAGE_IN_CHE_PREFIX =
      PROJECT_NAME + "/src/main/java/renametype";

  private String pathToCurrentPackage;
  private String contentFromInA;
  private String contentFromOutB;

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private CodenvyEditor editor;
  @Inject private Refactor refactorPanel;
  @Inject private Consoles consoles;
  @Inject private Menu menu;
  @Inject private AskDialog askDialog;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setup() throws Exception {
    URL resource = TestEnumerationsTest.this.getClass().getResource("/projects/RenameType");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SIMPLE);
    ide.open(workspace);
  }

  @Test
  public void testEnum1() throws Exception {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    consoles.closeProcessesArea();
    projectExplorer.quickExpandWithJavaScript();

    setFieldsForTest("testEnum1");
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitTextIntoEditor(contentFromInA);
    projectExplorer.waitAndSelectItem(pathToCurrentPackage + "/A.java");
    menu.runCommand(ASSISTANT, REFACTORING, RENAME);

    refactorPanel.typeAndWaitNewName("B.java");
    refactorPanel.sendKeysIntoField(Keys.ARROW_LEFT.toString());
    refactorPanel.clickOkButtonRefactorForm();
    askDialog.waitFormToOpen();
    askDialog.clickOkBtn();
    askDialog.waitFormToClose();
    projectExplorer.waitItem(pathToCurrentPackage + "/B.java");
    assertEquals(editor.getVisibleTextFromEditor(), contentFromOutB);
    editor.waitTextIntoEditor(contentFromOutB);
  }

  private void setFieldsForTest(String nameCurrentTest) throws Exception {
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
