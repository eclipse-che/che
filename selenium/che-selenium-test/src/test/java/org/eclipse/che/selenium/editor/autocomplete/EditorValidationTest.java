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

import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.ERROR;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.WARNING;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.MavenPluginStatusBar;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrienko Alexander on 12.01.15. */
public class EditorValidationTest {

  private static final String PROJECT_NAME =
      NameGenerator.generate(EditorValidationTest.class.getSimpleName(), 4);
  private static final String TEXT_FOR_WARNING = "String l, n;";
  private static final String TEXT_FOR_ERROR = "Integer f = 7.8; String key = Keys.ALT;";

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private AskDialog askDialog;
  @Inject private MavenPluginStatusBar mavenPluginStatusBar;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private Consoles consoles;

  @BeforeClass
  public void prepare() throws Exception {
    URL resource = getClass().getResource("/projects/default-spring-project");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
    ide.open(workspace);
  }

  @Test
  public void autoCompleteClassInTheSamePackage() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    consoles.closeProcessesArea();
    projectExplorer.quickExpandWithJavaScript();
    mavenPluginStatusBar.waitClosingInfoPanel();
    projectExplorer.openItemByPath(
        PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/AppController.java");
    editor.waitActive();
    // validation warnings
    editor.waitAllMarkersInvisibility(ERROR);
    editor.setCursorToLine(28);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor("\n");
    editor.waitAllMarkersInvisibility(ERROR);
    editor.typeTextIntoEditor(TEXT_FOR_WARNING);
    editor.waitMarkerInPosition(WARNING, 29);
    editor.waitAnnotationCodeAssistIsClosed();
    editor.moveToMarkerAndWaitAssistContent(WARNING);
    loader.waitOnClosed();
    editor.waitTextIntoAnnotationAssist("The value of the local variable l is not used");
    editor.waitTextIntoAnnotationAssist("The value of the local variable n is not used");

    // TODO the proposition code assist does not still work by click
    // ide.getEditor().clickOnWarningMarkerInPosition(21);
    editor.launchPropositionAssistPanel();
    editor.waitTextIntoFixErrorProposition("Remove 'l', keep assignments with side effects");
    editor.waitTextIntoFixErrorProposition("Remove 'l' and all assignments");
    editor.typeTextIntoEditor(Keys.ESCAPE.toString());
    editor.waitErrorPropositionPanelClosed();

    // validation errors
    editor.setCursorToLine(30);
    editor.waitActive();
    editor.typeTextIntoEditor(TEXT_FOR_ERROR);
    editor.waitMarkerInPosition(ERROR, 30);
    editor.waitActive();
    editor.waitAnnotationCodeAssistIsClosed();
    editor.moveToMarkerAndWaitAssistContent(ERROR);
    editor.waitTextIntoAnnotationAssist("Type mismatch: cannot convert from double to Integer");
    editor.waitTextIntoAnnotationAssist("Keys cannot be resolved to a variable");
    loader.waitOnClosed();

    // TODO the proposition code assist does not still work by click
    // ide.getEditor().clickOnErrorMarkerInPosition(22);
    editor.typeTextIntoEditor(Keys.HOME.toString());
    editor.launchPropositionAssistPanel();
    editor.waitTextIntoFixErrorProposition("Add cast to 'int'");
    editor.waitTextIntoFixErrorProposition("Change type of 'f' to 'double'");
  }
}
