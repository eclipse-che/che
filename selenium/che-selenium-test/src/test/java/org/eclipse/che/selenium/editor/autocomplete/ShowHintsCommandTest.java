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
import org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.openqa.selenium.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev */
public class ShowHintsCommandTest {
  private final Logger LOG = LoggerFactory.getLogger(ShowHintsCommandTest.class);
  private static final String PROJECT_NAME =
      NameGenerator.generate(ShowHintsCommandTest.class.getSimpleName(), 4);

  private static final String TEXT_IN_POP_UP_1 =
      "runCommand() : void\n"
          + "runCommand(String arg) : void\n"
          + "runCommand(boolean arg) : String\n"
          + "runCommand(int arg) : void\n"
          + "runCommand(int arg, String arg2) : void\n"
          + "runCommand(int arg, String arg2, boolean arg3) : void";

  private static final String CONSTRUCTOR = "HintTestClass hintTestClass = new HintTestClass(11);";

  private static final String TEXT_IN_POP_UP_2 =
      "HintTestClass()\n"
          + "HintTestClass(int arg)\n"
          + "HintTestClass(int arg, String arg2)\n"
          + "HintTestClass(int arg, String arg2, boolean arg3)";

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private Consoles console;

  @BeforeClass
  public void prepare() throws Exception {
    URL resource = getClass().getResource("/projects/spring-for-hint-test");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
    ide.open(workspace);
    console.waitJDTLSProjectResolveFinishedMessage(PROJECT_NAME);
  }

  @Test
  public void checkShowHintsCommand() throws Exception {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    console.closeProcessesArea();
    projectExplorer.expandPathInProjectExplorerAndOpenFile(
        PROJECT_NAME + "/src/main/java/org.eclipse.qa.examples", "AppController.java");
    loader.waitOnClosed();
    projectExplorer.openItemByVisibleNameInExplorer("HintTestClass.java");
    loader.waitOnClosed();

    // check the 'show hints' to all parameters on the overloaded method
    editor.selectTabByName("AppController");
    editor.waitActive();
    editor.setCursorToLine(33);
    editor.typeTextIntoEditor(Keys.TAB.toString());
    editor.typeTextIntoEditor("runCommand();");
    editor.waitTextIntoEditor("runCommand();");
    editor.waitMarkerInPosition(MarkerLocator.ERROR, 34);
    editor.goToCursorPositionVisible(33, 16);
    editor.callShowHintsPopUp();
    editor.waitShowHintsPopUpOpened();
    editor.waitExpTextIntoShowHintsPopUp(TEXT_IN_POP_UP_1);
    editor.typeTextIntoEditor(Keys.ESCAPE.toString());
    editor.waitShowHintsPopUpClosed();

    // check the 'show hints' to all parameters on the overloaded constructor
    editor.waitActive();
    editor.setCursorToLine(28);
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.typeTextIntoEditor(Keys.TAB.toString());
    editor.typeTextIntoEditor(CONSTRUCTOR);
    editor.waitTextIntoEditor(CONSTRUCTOR);
    editor.goToCursorPositionVisible(29, 53);
    editor.callShowHintsPopUp();
    editor.waitShowHintsPopUpOpened();
    editor.waitExpTextIntoShowHintsPopUp(TEXT_IN_POP_UP_2);
    editor.typeTextIntoEditor(Keys.ESCAPE.toString());
    editor.waitShowHintsPopUpClosed();
  }
}
