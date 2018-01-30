/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.editor.autocomplete;

import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkersType.ERROR_MARKER;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkersType.TASK_MARKER_OVERVIEW;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkersType;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.MavenPluginStatusBar;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class AutocompleteWithInheritTest {
  private static final String PROJECT_NAME =
      NameGenerator.generate(AutocompleteWithInheritTest.class.getSimpleName(), 4);
  private static final String BASE_CLASS = "AppController";
  private static final String EXTENDED_CLASS = "InheritClass";

  private static final String contentAfterFix =
      "public class InheritClass extends AppController {\n"
          + "\n"
          + "    public InheritClass(int testInt, String testString) {\n"
          + "        super(testInt, testString);\n"
          + "        // TODO Auto-generated constructor stub\n"
          + "    }\n"
          + "\n"
          + "}\n";

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private MavenPluginStatusBar mavenPluginStatusBar;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void prepare() throws Exception {
    URL resource = getClass().getResource("/projects/default-dependency-test");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);

    ide.open(workspace);
  }

  @Test
  public void updateDependencyWithInheritTest() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    mavenPluginStatusBar.waitClosingInfoPanel();
    projectExplorer.selectItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();

    projectExplorer.openItemByVisibleNameInExplorer("AppController.java");
    editor.waitAllMarkersDisappear(ERROR_MARKER);

    projectExplorer.openItemByVisibleNameInExplorer(EXTENDED_CLASS + ".java");
    editor.returnFocusInCurrentLine();
    waitErrorMarkerInPosition();
    editor.setCursorToLine(13);
    editor.launchPropositionAssistPanel();
    editor.waitTextIntoFixErrorProposition("Add constructor 'InheritClass(int,String)'");
    editor.selectFirstItemIntoFixErrorPropByEnter();
    editor.waitTextIntoEditor(contentAfterFix);
    editor.waitMarkerDisappears(ERROR_MARKER, 13);
    editor.waitMarkerInPosition(TASK_MARKER_OVERVIEW, 18);
    editor.waitTabFileWithSavedStatus(EXTENDED_CLASS);
    editor.selectTabByName(BASE_CLASS);
    loader.waitOnClosed();
    editor.selectLineAndDelete(24);
    editor.typeTextIntoEditor("int testString;");
    editor.typeTextIntoEditor(Keys.ARROW_DOWN.toString());
    editor.typeTextIntoEditor(Keys.ARROW_DOWN.toString());
    editor.selectLineAndDelete();
    editor.typeTextIntoEditor("public AppController(int testInt, int testString) {");
    editor.typeTextIntoEditor(Keys.DELETE.toString());
    editor.waitTabFileWithSavedStatus(BASE_CLASS);
    editor.selectTabByName(EXTENDED_CLASS);
    loader.waitOnClosed();
    editor.setCursorToLine(16);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.HOME.toString());
    editor.typeTextIntoEditor(Keys.DELETE.toString());
    editor.typeTextIntoEditor("s");
    editor.launchPropositionAssistPanel();
    editor.waitTextIntoFixErrorProposition("Change type of 'testString' to 'int'");
    editor.selectFirstItemIntoFixErrorPropByDoubleClick();
    editor.waitAllMarkersDisappear(ERROR_MARKER);
  }

  private void waitErrorMarkerInPosition() {
    try {
      editor.waitMarkerInPosition(MarkersType.ERROR_MARKER, 13);
    } catch (TimeoutException ex) {
      editor.setCursorToLine(13);
      editor.waitCursorPosition(13, 1);
      editor.typeTextIntoEditor(Keys.ENTER.toString());
      editor.waitCursorPosition(14, 1);
      editor.typeTextIntoEditor(Keys.ENTER.toString());
      editor.waitCursorPosition(15, 1);
      editor.typeTextIntoEditor(Keys.ENTER.toString());
      editor.waitCursorPosition(16, 1);
      editor.typeTextIntoEditor(Keys.BACK_SPACE.toString());
      editor.waitCursorPosition(15, 1);
      editor.typeTextIntoEditor(Keys.BACK_SPACE.toString());
      editor.waitCursorPosition(14, 1);
      editor.typeTextIntoEditor(Keys.BACK_SPACE.toString());
      editor.waitCursorPosition(13, 1);
      editor.waitMarkerInPosition(MarkersType.ERROR_MARKER, 13);
    }
  }
}
