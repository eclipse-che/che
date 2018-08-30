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

import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.ERROR;
import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.WARNING;
import static org.testng.Assert.fail;

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
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrienko Alexander on 08.01.15. */
public class QuickFixAndCodeAssistantFeaturesTest {
  private static final String PROJECT_NAME =
      NameGenerator.generate(QuickFixAndCodeAssistantFeaturesTest.class.getSimpleName(), 4);
  private static final String TEXT_FOR_WARNING = "String l = \"test\";";
  private static final String TEXT_FOR_ERROR = "Class1 test = n";

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
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
    consoles.waitJDTLSProjectResolveFinishedMessage(PROJECT_NAME);
  }

  @Test
  public void quickFixAndCodeAssistantTest() {
    projectExplorer.waitItem(PROJECT_NAME);
    notificationsPopupPanel.waitProgressPopupPanelClose();
    consoles.closeProcessesArea();
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByVisibleNameInExplorer("AppController.java");
    editor.waitActive();

    editor.setCursorToLine(28);
    editor.typeTextIntoEditor(TEXT_FOR_WARNING);
    editor.waitMarkerInPosition(WARNING, 28);
    editor.launchPropositionAssistPanel();
    editor.waitTextIntoFixErrorProposition("Remove 'l', keep assignments with side effects");
    editor.waitTextIntoFixErrorProposition("Remove 'l' and all assignments");
    editor.typeTextIntoEditor(Keys.ESCAPE.toString());
    editor.waitErrorPropositionPanelClosed();

    editor.launchPropositionAssistPanel();
    try {
      editor.waitTextIntoFixErrorProposition("Convert local variable to field");
      editor.waitTextIntoFixErrorProposition("Inline local variable");
    } catch (TimeoutException e) {
      fail(
          "Known issues: https://github.com/eclipse/eclipse.jdt.ls/issues/772, "
              + "https://github.com/eclipse/eclipse.jdt.ls/issues/771");
    }
    editor.typeTextIntoEditor(Keys.ESCAPE.toString());
    editor.waitErrorPropositionPanelClosed();
    loader.waitOnClosed();
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor("\n");

    editor.typeTextIntoEditor(TEXT_FOR_ERROR);
    editor.launchAutocompleteAndWaitContainer();
    editor.enterAutocompleteProposal("null");
    projectExplorer.waitAndSelectItemByName(
        "AppController.java"); // TODO because the cursor disappears after click in the autocomplete
    // panel
    projectExplorer.openItemByVisibleNameInExplorer("AppController.java");
    editor.typeTextIntoEditor(";");
    editor.waitMarkerInPosition(ERROR, 29);
    editor.launchPropositionAssistPanel();
    editor.waitTextIntoFixErrorProposition("Change to 'ClassDesc' (javax.rmi.CORBA)");
    editor.waitTextIntoFixErrorProposition("Change to 'ClassUtils' (org.springframework.util)");
    editor.waitTextIntoFixErrorProposition("Change to 'ClassValue' (java.lang)");
    editor.waitTextIntoFixErrorProposition("Change to 'Class' (java.lang)");
    editor.typeTextIntoEditor(Keys.ESCAPE.toString());
    editor.waitErrorPropositionPanelClosed();
    editor.launchPropositionAssistPanel();
    editor.waitTextIntoFixErrorProposition("Change to 'ClassDesc' (javax.rmi.CORBA)");
    editor.waitTextIntoFixErrorProposition("Change to 'ClassUtils' (org.springframework.util)");
    editor.waitTextIntoFixErrorProposition("Change to 'ClassValue' (java.lang)");
    editor.waitTextIntoFixErrorProposition("Change to 'Class' (java.lang)");
  }
}
