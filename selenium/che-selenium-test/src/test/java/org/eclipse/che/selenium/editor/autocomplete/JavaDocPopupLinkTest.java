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
package org.eclipse.che.selenium.editor.autocomplete;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Josh Pinkney */
public class JavaDocPopupLinkTest {
  private static final String PROJECT_NAME =
      NameGenerator.generate(JavaDocPopupTest.class.getSimpleName(), 4);

  private static final String PATH_TO_FILES =
      PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples";

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private Consoles consoles;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/default-spring-project");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
    ide.open(workspace);
    consoles.waitJDTLSProjectResolveFinishedMessage(PROJECT_NAME);
  }

  @Test
  public void javaDocPopupLinkTest() throws Exception {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    consoles.closeProcessesArea();
    projectExplorer.quickExpandWithJavaScript();

    // Reference to external class file
    projectExplorer.waitItem(PATH_TO_FILES + "/AppController.java");
    projectExplorer.openItemByVisibleNameInExplorer("AppController.java");
    loader.waitOnClosed();
    editor.goToCursorPositionVisible(26, 105);

    editor.openJavaDocPopUp();
    editor.waitJavaDocPopUpOpened();
    editor.clickOnElementByXpath("//div[contains(@class, 'textviewTooltip')]//a");
    editor.tabIsPresentOnce("RuntimeException.class");
    editor.closeAllTabsByContextMenu();
    loader.waitOnClosed();

    // Reference to link in same class
    projectExplorer.openItemByVisibleNameInExplorer("Aclass.java");
    editor.waitActive();
    editor.typeTextIntoEditor(Keys.CONTROL.toString());
    editor.goToCursorPositionVisible(14, 22);
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.typeTextIntoEditor(Keys.ENTER.toString());

    editor.setCursorToLine(15);
    editor.typeTextIntoEditor("/**");
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.typeTextIntoEditor("This {@link Aclass link} tests that the user is sent to line 13");
    editor.setCursorToLine(18);
    editor.typeTextIntoEditor("public void testLinkInClass() {}");
    editor.goToCursorPositionVisible(18, 24);
    editor.openJavaDocPopUp();
    editor.waitJavaDocPopUpOpened();
    editor.typeTextIntoEditor(Keys.CONTROL.toString());
    editor.clickOnElementByXpath("//div[contains(@class, 'textviewTooltip')]");
    editor.clickOnElementByXpath("//div[contains(@class, 'textviewTooltip')]//a");
    editor.expectedNumberOfActiveLine(14);

    // Reference to class in a different package but held locally
    editor.waitActive();
    editor.setCursorToLine(19);
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.typeTextIntoEditor("/**");
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.typeTextIntoEditor("{@link org.eclipse.qa.examples.AppController#handleRequest}");
    editor.setCursorToLine(23);
    editor.typeTextIntoEditor("public void testLinkInDifferentPackage() {}");
    editor.goToCursorPositionVisible(23, 18);
    editor.openJavaDocPopUp();
    editor.waitJavaDocPopUpOpened();
    editor.clickOnElementByXpath("//div[contains(@class, 'textviewTooltip')]//a");
    editor.tabIsPresentOnce("AppController.class");
  }
}
