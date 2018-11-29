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

import static java.lang.String.format;
import static java.nio.charset.Charset.forName;
import static java.nio.file.Files.readAllLines;
import static java.nio.file.Paths.get;
import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.core.TestGroup.UNDER_REPAIR;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.ASSISTANT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.Refactoring.REFACTORING;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.Refactoring.RENAME;
import static org.testng.Assert.fail;

import com.google.common.base.Joiner;
import com.google.inject.Inject;
import java.net.URL;
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
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
@Test(groups = UNDER_REPAIR)
public class TestAnnotationsTest {
  private static final String PROJECT_NAME = generate("project", 4);
  private static final String PATH_TO_PACKAGE_IN_CHE_PREFIX =
      PROJECT_NAME + "/src/main/java/renametype";

  private String pathToCurrentPackage;
  private String contentFromInA;
  private String contentFromInB;

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private Refactor refactorPanel;
  @Inject private Menu menu;
  @Inject private AskDialog askDialog;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private Consoles consoles;

  @BeforeClass
  public void setup() throws Exception {
    URL resource = TestAnnotationsTest.this.getClass().getResource("/projects/RenameType");
    testProjectServiceClient.importProject(
        workspace.getId(), get(resource.toURI()), PROJECT_NAME, ProjectTemplates.MAVEN_SIMPLE);

    ide.open(workspace);
    ide.waitOpenedWorkspaceIsReadyToUse();
    consoles.waitJDTLSProjectResolveFinishedMessage(PROJECT_NAME);
  }

  @Test
  public void testAnnotation1() throws Exception {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();

    setFieldsForTest("testAnnotation1");
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitTextIntoEditor(contentFromInA);
    projectExplorer.waitAndSelectItem(pathToCurrentPackage + "/A.java");
    menu.runCommand(ASSISTANT, REFACTORING, RENAME);

    refactorPanel.typeAndWaitNewName("B.java");
    try {
      refactorPanel.clickOkButtonRefactorForm();
    } catch (org.openqa.selenium.TimeoutException ex) {
      refactorPanel.typeAndWaitNewName("B.java");
      refactorPanel.sendKeysIntoField(Keys.ARROW_LEFT.toString());
      refactorPanel.sendKeysIntoField(Keys.ARROW_LEFT.toString());
      refactorPanel.clickOkButtonRefactorForm();
    }
    askDialog.waitFormToOpen();
    askDialog.clickOkBtn();
    askDialog.waitFormToClose();
    projectExplorer.waitItem(pathToCurrentPackage + "/B.java");

    try {
      editor.waitTextIntoEditor(contentFromInB);
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known permanent failure https://github.com/eclipse/che/issues/11779");
    }
  }

  private void setFieldsForTest(String nameCurrentTest) throws Exception {
    pathToCurrentPackage = PATH_TO_PACKAGE_IN_CHE_PREFIX + "/" + nameCurrentTest;

    URL resourcesInA =
        getClass()
            .getResource(
                format("/org/eclipse/che/selenium/refactor/types/%s/in/A.java", nameCurrentTest));
    URL resourcesOutA =
        getClass()
            .getResource(
                format("/org/eclipse/che/selenium/refactor/types/%s/out/B.java", nameCurrentTest));

    contentFromInA = getTextFromFile(resourcesInA);
    contentFromInB = getTextFromFile(resourcesOutA);
  }

  private String getTextFromFile(URL url) throws Exception {
    return Joiner.on("\n").join(readAllLines(get(url.toURI()), forName("UTF-8")));
  }
}
