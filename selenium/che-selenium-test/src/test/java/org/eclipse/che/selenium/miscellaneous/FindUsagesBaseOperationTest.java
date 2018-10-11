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
package org.eclipse.che.selenium.miscellaneous;

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.ASSISTANT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.FIND_USAGES;
import static org.eclipse.che.selenium.core.project.ProjectTemplates.MAVEN_SPRING;
import static org.openqa.selenium.Keys.ARROW_DOWN;
import static org.openqa.selenium.Keys.ARROW_LEFT;
import static org.openqa.selenium.Keys.ARROW_RIGHT;
import static org.openqa.selenium.Keys.ARROW_UP;
import static org.openqa.selenium.Keys.ENTER;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.FindUsages;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev */
public class FindUsagesBaseOperationTest {
  private static final String PROJECT_NAME = generate("project", 4);

  private static final String EXPECTED_TEXT =
      "Usages of numGuessByUser [3 occurrences]\n"
          + PROJECT_NAME
          + "\n"
          + "org.eclipse.qa.examples\n"
          + "- src/main/java\n"
          + "AppController\n"
          + "handleRequest(HttpServletRequest, HttpServletResponse)\n"
          + "30:    if (numGuessByUser != null && numGuessByUser.equals(secretNum)) {\n"
          + "30:    if (numGuessByUser != null && numGuessByUser.equals(secretNum)) {\n"
          + "34:    else if (numGuessByUser != null) {";

  private static final String EXPECTED_TEXT_1 =
      "org.eclipse.qa.examples\n"
          + "- src/main/java\n"
          + "AppController\n"
          + "handleRequest(HttpServletRequest, HttpServletResponse)\n"
          + "30:    if (numGuessByUser != null && numGuessByUser.equals(secretNum)) {\n"
          + "30:    if (numGuessByUser != null && numGuessByUser.equals(secretNum)) {\n"
          + "34:    else if (numGuessByUser != null) {";

  private static final String EXPECTED_TEXT_2 =
      "handleRequest(HttpServletRequest, HttpServletResponse)\n"
          + "30:    if (numGuessByUser != null && numGuessByUser.equals(secretNum)) {\n"
          + "30:    if (numGuessByUser != null && numGuessByUser.equals(secretNum)) {\n"
          + "34:    else if (numGuessByUser != null) {";

  @Inject private Ide ide;
  @Inject private Menu menu;
  @Inject private Loader loader;
  @Inject private Events events;
  @Inject private CodenvyEditor editor;
  @Inject private FindUsages findUsages;
  @Inject private TestWorkspace workspace;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/guess-project");
    testProjectServiceClient.importProject(
        workspace.getId(), Paths.get(resource.toURI()), PROJECT_NAME, MAVEN_SPRING);

    ide.open(workspace);
    ide.waitOpenedWorkspaceIsReadyToUse();
  }

  @Test
  public void checkFindUsagesBaseOperation() {
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByVisibleNameInExplorer("AppController.java");

    // Check basic operations of the Find Usages panel
    editor.selectTabByName("AppController");
    editor.goToCursorPositionVisible(27, 17);
    menu.runCommand(ASSISTANT, FIND_USAGES);
    loader.waitOnClosed();
    findUsages.waitFindUsagesPanelIsOpen();
    findUsages.waitExpectedTextInFindUsagesPanel(EXPECTED_TEXT);
    findUsages.waitSelectedElementInFindUsagesPanel("numGuessByUser");

    // Switch to the Events panel and check that the Find Usages panel is not visible
    findUsages.waitFindUsagesPanelIsOpen();
    events.clickEventLogBtn();
    events.waitOpened();
    findUsages.waitFindUsagesPanelIsClosed();

    // Switch to the Find Usages panel and check its expected content
    findUsages.clickFindUsagesIcon();
    findUsages.waitFindUsagesPanelIsOpen();
    findUsages.waitExpectedTextInFindUsagesPanel(EXPECTED_TEXT);
    findUsages.waitSelectedElementInFindUsagesPanel("numGuessByUser");

    // Check nodes in the Find Usages panel by 'double click'
    findUsages.selectNodeInFindUsagesByDoubleClick(PROJECT_NAME);
    findUsages.waitExpectedTextIsNotPresentInFindUsagesPanel(EXPECTED_TEXT_1);
    findUsages.selectNodeInFindUsagesByDoubleClick(PROJECT_NAME);
    findUsages.waitExpectedTextIsNotPresentInFindUsagesPanel(EXPECTED_TEXT_1);
    findUsages.selectNodeInFindUsagesByDoubleClick("org.eclipse.qa.examples");
    findUsages.waitExpectedTextInFindUsagesPanel("AppController");
    findUsages.selectNodeInFindUsagesByDoubleClick("AppController");
    findUsages.waitExpectedTextInFindUsagesPanel(
        "handleRequest(HttpServletRequest, HttpServletResponse)");
    findUsages.selectNodeInFindUsagesByDoubleClick(
        "handleRequest(HttpServletRequest, HttpServletResponse)");
    findUsages.waitExpectedTextInFindUsagesPanel(EXPECTED_TEXT);

    // Check nodes in the Find Usages panel by click on node icon
    findUsages.clickOnIconNodeInFindUsagesPanel(
        "handleRequest(HttpServletRequest, HttpServletResponse)");
    findUsages.waitExpectedTextIsNotPresentInFindUsagesPanel(EXPECTED_TEXT_2);
    findUsages.clickOnIconNodeInFindUsagesPanel("AppController");
    findUsages.waitExpectedTextIsNotPresentInFindUsagesPanel(EXPECTED_TEXT_1);
    findUsages.clickOnIconNodeInFindUsagesPanel("org.eclipse.qa.examples");
    findUsages.waitExpectedTextIsNotPresentInFindUsagesPanel(
        "handleRequest(HttpServletRequest, HttpServletResponse)");

    // Check nodes in the Find Usages panel by 'Enter' button
    // Close node by ENTER button and check it closed
    findUsages.selectNodeInFindUsagesPanel(PROJECT_NAME);
    findUsages.sendCommandByKeyboardInFindUsagesPanel(ENTER.toString());
    findUsages.waitExpectedTextIsNotPresentInFindUsagesPanel("org.eclipse.qa.examples");

    // Open node and check that only "org.eclipse.qa.examples" node is opened
    findUsages.selectNodeInFindUsagesPanel(PROJECT_NAME);
    findUsages.sendCommandByKeyboardInFindUsagesPanel(ENTER.toString());
    findUsages.waitExpectedTextInFindUsagesPanel("org.eclipse.qa.examples");
    findUsages.selectNodeInFindUsagesPanel("org.eclipse.qa.examples");
    findUsages.sendCommandByKeyboardInFindUsagesPanel(ENTER.toString());
    findUsages.selectNodeInFindUsagesPanel("AppController");
    findUsages.sendCommandByKeyboardInFindUsagesPanel(ENTER.toString());
    findUsages.selectNodeInFindUsagesPanel(
        "handleRequest(HttpServletRequest, HttpServletResponse)");
    findUsages.sendCommandByKeyboardInFindUsagesPanel(ENTER.toString());
    findUsages.waitExpectedTextInFindUsagesPanel(EXPECTED_TEXT_1);

    // Close "AppController" node by ENTER button and check it closed
    findUsages.selectNodeInFindUsagesPanel("AppController");
    findUsages.sendCommandByKeyboardInFindUsagesPanel(ENTER.toString());
    findUsages.waitExpectedTextIsNotPresentInFindUsagesPanel(
        "handleRequest(HttpServletRequest, HttpServletResponse)");

    // Open "AppController" node and check that only "handleRequest(HttpServletRequest,
    // HttpServletResponse)" node is opened
    findUsages.selectNodeInFindUsagesPanel("AppController");
    findUsages.sendCommandByKeyboardInFindUsagesPanel(ENTER.toString());
    findUsages.waitExpectedTextInFindUsagesPanel(
        "handleRequest(HttpServletRequest, HttpServletResponse)");
    findUsages.selectNodeInFindUsagesPanel(
        "handleRequest(HttpServletRequest, HttpServletResponse)");
    findUsages.sendCommandByKeyboardInFindUsagesPanel(ENTER.toString());
    findUsages.waitExpectedTextInFindUsagesPanel(EXPECTED_TEXT_2);

    // Check nodes in the Find Usages panel by keyboard
    findUsages.selectNodeInFindUsagesPanel(
        "handleRequest(HttpServletRequest, HttpServletResponse)");
    findUsages.sendCommandByKeyboardInFindUsagesPanel(ARROW_LEFT.toString());
    findUsages.waitExpectedTextIsNotPresentInFindUsagesPanel(EXPECTED_TEXT_2);
    findUsages.sendCommandByKeyboardInFindUsagesPanel(ARROW_RIGHT.toString());
    findUsages.waitExpectedTextInFindUsagesPanel(EXPECTED_TEXT_2);
    findUsages.sendCommandByKeyboardInFindUsagesPanel(ARROW_UP.toString());
    findUsages.sendCommandByKeyboardInFindUsagesPanel(ARROW_UP.toString());
    findUsages.sendCommandByKeyboardInFindUsagesPanel(ARROW_UP.toString());
    findUsages.sendCommandByKeyboardInFindUsagesPanel(ARROW_LEFT.toString());
    findUsages.waitExpectedTextIsNotPresentInFindUsagesPanel(EXPECTED_TEXT_1);
    findUsages.sendCommandByKeyboardInFindUsagesPanel(ARROW_RIGHT.toString());
    findUsages.waitExpectedTextIsNotPresentInFindUsagesPanel(EXPECTED_TEXT_1);
    findUsages.sendCommandByKeyboardInFindUsagesPanel(ARROW_DOWN.toString());
    findUsages.sendCommandByKeyboardInFindUsagesPanel(ARROW_RIGHT.toString());
    findUsages.sendCommandByKeyboardInFindUsagesPanel(ARROW_DOWN.toString());
    findUsages.sendCommandByKeyboardInFindUsagesPanel(ARROW_RIGHT.toString());
    findUsages.sendCommandByKeyboardInFindUsagesPanel(ARROW_DOWN.toString());
    findUsages.sendCommandByKeyboardInFindUsagesPanel(ARROW_RIGHT.toString());
    findUsages.waitExpectedTextInFindUsagesPanel(EXPECTED_TEXT_2);
    findUsages.waitExpectedTextInFindUsagesPanel(EXPECTED_TEXT_1);

    // Check the found items in the editor
    findUsages.selectHighlightedItemInFindUsagesByDoubleClick(30);
    editor.typeTextIntoEditor(ARROW_LEFT.toString());
    editor.expectedNumberOfActiveLine(30);
    editor.waitTextElementsActiveLine("numGuessByUser");

    findUsages.selectNodeInFindUsagesPanel(
        "handleRequest(HttpServletRequest, HttpServletResponse)");
    findUsages.sendCommandByKeyboardInFindUsagesPanel(ARROW_DOWN.toString());
    findUsages.sendCommandByKeyboardInFindUsagesPanel(ARROW_DOWN.toString());
    findUsages.sendCommandByKeyboardInFindUsagesPanel(ARROW_DOWN.toString());
    findUsages.sendCommandByKeyboardInFindUsagesPanel(ENTER.toString());
    editor.typeTextIntoEditor(ARROW_LEFT.toString());
    editor.expectedNumberOfActiveLine(34);
    editor.waitTextElementsActiveLine("numGuessByUser");
  }
}
