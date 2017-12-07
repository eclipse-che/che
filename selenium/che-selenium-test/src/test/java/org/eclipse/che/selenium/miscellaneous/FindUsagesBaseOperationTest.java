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
package org.eclipse.che.selenium.miscellaneous;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.FindUsages;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev */
public class FindUsagesBaseOperationTest {
  private static final String PROJECT_NAME = NameGenerator.generate("FindUsagesProject", 4);
  private static final String PATH_FOR_EXPAND =
      PROJECT_NAME + "/src/main/java/org.eclipse.qa.examples";

  private static final String EXPECTED_TEXT =
      "Usages of numGuessByUser [3 occurrences]\n"
          + PROJECT_NAME
          + "\n"
          + "org.eclipse.qa.examples\n"
          + "- src/main/java\n"
          + "AppController\n"
          + "handleRequest(HttpServletRequest, HttpServletResponse)\n"
          + "29:    if (numGuessByUser != null && numGuessByUser.equals(secretNum)) {\n"
          + "29:    if (numGuessByUser != null && numGuessByUser.equals(secretNum)) {\n"
          + "33:    else if (numGuessByUser != null) {";

  private static final String EXPECTED_TEXT_1 =
      "org.eclipse.qa.examples\n"
          + "- src/main/java\n"
          + "AppController\n"
          + "handleRequest(HttpServletRequest, HttpServletResponse)\n"
          + "29:    if (numGuessByUser != null && numGuessByUser.equals(secretNum)) {\n"
          + "29:    if (numGuessByUser != null && numGuessByUser.equals(secretNum)) {\n"
          + "33:    else if (numGuessByUser != null) {";

  private static final String EXPECTED_TEXT_2 =
      "handleRequest(HttpServletRequest, HttpServletResponse)\n"
          + "29:    if (numGuessByUser != null && numGuessByUser.equals(secretNum)) {\n"
          + "29:    if (numGuessByUser != null && numGuessByUser.equals(secretNum)) {\n"
          + "33:    else if (numGuessByUser != null) {";

  @Inject private FindUsages findUsages;
  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private CodenvyEditor editor;
  @Inject private Menu menu;
  @Inject private Loader loader;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private Events events;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/guess-project");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
    ide.open(workspace);
  }

  @Test
  public void checkFindUsagesBaseOperation() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByVisibleNameInExplorer("AppController.java");

    // Check basic operations of the 'find usages' panel
    editor.selectTabByName("AppController");
    editor.goToCursorPositionVisible(26, 17);
    menu.runCommand(
        TestMenuCommandsConstants.Assistant.ASSISTANT,
        TestMenuCommandsConstants.Assistant.FIND_USAGES);
    loader.waitOnClosed();
    findUsages.waitFindUsagesPanelIsOpen();
    events.clickEventLogBtn();
    findUsages.waitFindUsegesPanelIsClosed();
    findUsages.clickFindUsagesIcon();
    findUsages.waitFindUsagesPanelIsOpen();
    findUsages.waitExpectedTextInFindUsagesPanel(EXPECTED_TEXT);
    findUsages.waitSelectedElementInFindUsagesPanel("numGuessByUser");
    // Check basic operations of the 'find usages' panel
    editor.selectTabByName("AppController");
    editor.goToCursorPositionVisible(26, 17);
    menu.runCommand(
        TestMenuCommandsConstants.Assistant.ASSISTANT,
        TestMenuCommandsConstants.Assistant.FIND_USAGES);
    loader.waitOnClosed();
    findUsages.waitFindUsagesPanelIsOpen();
    findUsages.waitExpectedTextInFindUsagesPanel(EXPECTED_TEXT);
    findUsages.waitSelectedElementInFindUsagesPanel("numGuessByUser");

    // Check nodes in the 'find usages' panel by 'double click' and click on the icon node
    findUsages.clickOnIconNodeInFindUsagesPanel(PROJECT_NAME);
    findUsages.waitExpectedTextIsNotPresentInFindUsagesPanel(EXPECTED_TEXT_1);
    findUsages.selectNodeInFindUsagesByDoubleClick(PROJECT_NAME);
    findUsages.waitExpectedTextIsNotPresentInFindUsagesPanel(EXPECTED_TEXT_1);
    findUsages.selectNodeInFindUsagesByDoubleClick("org.eclipse.qa.examples");
    findUsages.selectNodeInFindUsagesByDoubleClick("AppController");
    findUsages.selectNodeInFindUsagesByDoubleClick(
        "handleRequest(HttpServletRequest, HttpServletResponse)");
    findUsages.waitExpectedTextInFindUsagesPanel(EXPECTED_TEXT_1);
    findUsages.clickOnIconNodeInFindUsagesPanel("AppController");
    findUsages.waitExpectedTextIsNotPresentInFindUsagesPanel(EXPECTED_TEXT_2);
    findUsages.selectNodeInFindUsagesByDoubleClick("AppController");
    findUsages.clickOnIconNodeInFindUsagesPanel("AppController");
    findUsages.waitExpectedTextIsNotPresentInFindUsagesPanel(EXPECTED_TEXT_2);

    // try-catch was added because test fails while trying to open node by click action
    // issue: https://github.com/eclipse/che/issues/6499
    try {
      findUsages.clickOnIconNodeInFindUsagesPanel(
          "handleRequest(HttpServletRequest, HttpServletResponse)");
    } catch (org.openqa.selenium.TimeoutException ex) {
      findUsages.clickOnIconNodeInFindUsagesPanel("AppController");
      findUsages.waitExpectedTextIsNotPresentInFindUsagesPanel(EXPECTED_TEXT_2);
      findUsages.clickOnIconNodeInFindUsagesPanel(
          "handleRequest(HttpServletRequest, HttpServletResponse)");
    }
    findUsages.waitExpectedTextInFindUsagesPanel(EXPECTED_TEXT_2);

    // Check nodes in the 'find usages' panel by 'Enter'
    findUsages.selectNodeInFindUsagesPanel(PROJECT_NAME);
    findUsages.sendCommandByKeyboardInFindUsagespanel(Keys.ENTER.toString());
    findUsages.waitExpectedTextIsNotPresentInFindUsagesPanel(EXPECTED_TEXT_1);
    findUsages.sendCommandByKeyboardInFindUsagespanel(Keys.ENTER.toString());
    findUsages.waitExpectedTextIsNotPresentInFindUsagesPanel(EXPECTED_TEXT_1);
    findUsages.selectNodeInFindUsagesPanel("org.eclipse.qa.examples");
    findUsages.sendCommandByKeyboardInFindUsagespanel(Keys.ENTER.toString());
    findUsages.selectNodeInFindUsagesPanel("AppController");
    findUsages.sendCommandByKeyboardInFindUsagespanel(Keys.ENTER.toString());
    findUsages.selectNodeInFindUsagesPanel(
        "handleRequest(HttpServletRequest, HttpServletResponse)");
    findUsages.sendCommandByKeyboardInFindUsagespanel(Keys.ENTER.toString());
    findUsages.waitExpectedTextInFindUsagesPanel(EXPECTED_TEXT_1);
    findUsages.selectNodeInFindUsagesPanel("AppController");
    findUsages.sendCommandByKeyboardInFindUsagespanel(Keys.ENTER.toString());
    findUsages.waitExpectedTextIsNotPresentInFindUsagesPanel(EXPECTED_TEXT_2);
    findUsages.sendCommandByKeyboardInFindUsagespanel(Keys.ENTER.toString());
    findUsages.waitExpectedTextIsNotPresentInFindUsagesPanel(EXPECTED_TEXT_2);
    findUsages.selectNodeInFindUsagesPanel(
        "handleRequest(HttpServletRequest, HttpServletResponse)");
    findUsages.sendCommandByKeyboardInFindUsagespanel(Keys.ENTER.toString());
    findUsages.waitExpectedTextInFindUsagesPanel(EXPECTED_TEXT_2);

    // Check nodes in the 'find usages' panel by keyboard
    findUsages.selectNodeInFindUsagesPanel(
        "handleRequest(HttpServletRequest, HttpServletResponse)");
    findUsages.sendCommandByKeyboardInFindUsagespanel(Keys.ARROW_LEFT.toString());
    findUsages.waitExpectedTextIsNotPresentInFindUsagesPanel(EXPECTED_TEXT_2);
    findUsages.sendCommandByKeyboardInFindUsagespanel(Keys.ARROW_RIGHT.toString());
    findUsages.waitExpectedTextInFindUsagesPanel(EXPECTED_TEXT_2);
    findUsages.sendCommandByKeyboardInFindUsagespanel(Keys.ARROW_UP.toString());
    findUsages.sendCommandByKeyboardInFindUsagespanel(Keys.ARROW_UP.toString());
    findUsages.sendCommandByKeyboardInFindUsagespanel(Keys.ARROW_UP.toString());
    findUsages.sendCommandByKeyboardInFindUsagespanel(Keys.ARROW_LEFT.toString());
    findUsages.waitExpectedTextIsNotPresentInFindUsagesPanel(EXPECTED_TEXT_1);
    findUsages.sendCommandByKeyboardInFindUsagespanel(Keys.ARROW_RIGHT.toString());
    findUsages.waitExpectedTextIsNotPresentInFindUsagesPanel(EXPECTED_TEXT_1);
    findUsages.sendCommandByKeyboardInFindUsagespanel(Keys.ARROW_DOWN.toString());
    findUsages.sendCommandByKeyboardInFindUsagespanel(Keys.ARROW_RIGHT.toString());
    findUsages.sendCommandByKeyboardInFindUsagespanel(Keys.ARROW_DOWN.toString());
    findUsages.sendCommandByKeyboardInFindUsagespanel(Keys.ARROW_RIGHT.toString());
    findUsages.sendCommandByKeyboardInFindUsagespanel(Keys.ARROW_DOWN.toString());
    findUsages.sendCommandByKeyboardInFindUsagespanel(Keys.ARROW_RIGHT.toString());
    findUsages.waitExpectedTextInFindUsagesPanel(EXPECTED_TEXT_2);
    findUsages.waitExpectedTextInFindUsagesPanel(EXPECTED_TEXT_1);

    // Check the found items in the editor
    findUsages.selectHighlightedItemInFindUsagesByDoubleClick(29);
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.expectedNumberOfActiveLine(29);
    editor.waitTextElementsActiveLine("numGuessByUser");
    findUsages.selectNodeInFindUsagesPanel(
        "handleRequest(HttpServletRequest, HttpServletResponse)");
    findUsages.sendCommandByKeyboardInFindUsagespanel(Keys.ARROW_DOWN.toString());
    findUsages.sendCommandByKeyboardInFindUsagespanel(Keys.ARROW_DOWN.toString());
    findUsages.sendCommandByKeyboardInFindUsagespanel(Keys.ARROW_DOWN.toString());
    findUsages.sendCommandByKeyboardInFindUsagespanel(Keys.ENTER.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.expectedNumberOfActiveLine(33);
    editor.waitTextElementsActiveLine("numGuessByUser");
  }
}
