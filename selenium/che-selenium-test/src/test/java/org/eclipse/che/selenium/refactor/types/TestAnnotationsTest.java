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
package org.eclipse.che.selenium.refactor.types;

import static java.lang.String.format;
import static java.nio.charset.Charset.forName;
import static java.nio.file.Files.readAllLines;
import static java.nio.file.Paths.get;

import com.google.common.base.Joiner;
import com.google.inject.Inject;
import java.net.URL;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Refactor;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class TestAnnotationsTest {
  private static final String nameOfProject =
      NameGenerator.generate(TestAnnotationsTest.class.getName(), 4);
  private static final String pathToPackageInChePrefix =
      nameOfProject + "/src/main/java/renametype";

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

  @BeforeClass
  public void setup() throws Exception {
    URL resource = TestAnnotationsTest.this.getClass().getResource("/projects/RenameType");
    testProjectServiceClient.importProject(
        workspace.getId(), get(resource.toURI()), nameOfProject, ProjectTemplates.MAVEN_SIMPLE);

    ide.open(workspace);
  }

  @Test
  public void testAnnotation1() throws Exception {
    projectExplorer.waitVisibleItem(nameOfProject);
    projectExplorer.quickExpandWithJavaScript();
    loader.waitOnClosed();
    setFieldsForTest("testAnnotation1");
    projectExplorer.openItemByPath(pathToCurrentPackage + "/A.java");
    editor.waitTextIntoEditor(contentFromInA);
    projectExplorer.selectItem(pathToCurrentPackage + "/A.java");
    menu.runCommand(
        TestMenuCommandsConstants.Assistant.ASSISTANT,
        TestMenuCommandsConstants.Assistant.Refactoring.REFACTORING,
        TestMenuCommandsConstants.Assistant.Refactoring.RENAME);

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
    editor.waitTextIntoEditor(contentFromInB);
  }

  private void setFieldsForTest(String nameCurrentTest) throws Exception {
    pathToCurrentPackage = pathToPackageInChePrefix + "/" + nameCurrentTest;

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
