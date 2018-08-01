/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.editor.autocomplete;

import static org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkerLocator.ERROR;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.MavenPluginStatusBar;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrienko Alexander on 12.01.15. */
public class InheritClassTest {
  private static final String PROJECT_NAME =
      NameGenerator.generate(InheritClassTest.class.getSimpleName(), 4);
  private static final String COMMON_PACKAGE =
      PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples";

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private AskDialog askDialog;
  @Inject private MavenPluginStatusBar mavenPluginStatusBar;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private Menu menu;
  @Inject private AskForValueDialog askForValueDialog;
  @Inject private TestProjectServiceClient testProjectServiceClient;

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
    // create java class in the same package with GreetingController.java
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    notificationsPopupPanel.waitProgressPopupPanelClose();
    loader.waitOnClosed();
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
    loader.waitOnClosed();
    projectExplorer.openItemByPath(COMMON_PACKAGE + "/AppController.java");

    // create java class in the different package with GreetingController.java
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitAndSelectItem(COMMON_PACKAGE);
    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT,
        TestMenuCommandsConstants.Project.New.NEW,
        TestMenuCommandsConstants.Project.New.JAVA_CLASS);
    askForValueDialog.createJavaFileByNameAndType("CodenvyTest", AskForValueDialog.JavaFiles.CLASS);
    editor.waitTabIsPresent("CodenvyTest");
    loader.waitOnClosed();

    projectExplorer.waitAndSelectItem(COMMON_PACKAGE);
    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT,
        TestMenuCommandsConstants.Project.New.NEW,
        TestMenuCommandsConstants.Project.New.JAVA_CLASS);
    askForValueDialog.createJavaFileByNameAndType(
        "CodenvyTestInherite", AskForValueDialog.JavaFiles.CLASS);
    editor.waitTabIsPresent("CodenvyTestInherite");
    loader.waitOnClosed();

    editor.waitActive();
    editor.setCursorToLine(3);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ARROW_LEFT.toString());
    editor.typeTextIntoEditor("ex");
    loader.waitOnClosed();
    editor.launchAutocomplete();
    editor.waitAutocompleteContainerIsClosed();
    editor.waitTextIntoEditor("extends");
    editor.typeTextIntoEditor(" Code");
    loader.waitOnClosed();
    editor.launchAutocompleteAndWaitContainer();
    editor.waitTextIntoAutocompleteContainer("CodenvyTest - org.eclipse.qa.examples");
    editor.enterAutocompleteProposal("CodenvyTest");
    editor.waitAutocompleteContainerIsClosed();
    editor.waitTextIntoEditor("CodenvyTestInherite extends CodenvyTest");
    loader.waitOnClosed();
    editor.waitActive();

    editor.selectTabByName("AppController");
    editor.waitActive();
    editor.setCursorToLine(33);
    editor.typeTextIntoEditor("Code");
    editor.launchAutocompleteAndWaitContainer();
    editor.waitTextIntoAutocompleteContainer("CodenvyTest - org.eclipse.qa.examples");
    editor.waitTextIntoAutocompleteContainer("CodenvyTestInherite - org.eclipse.qa.examples");
    editor.enterAutocompleteProposal("CodenvyTest");
    editor.waitAutocompleteContainerIsClosed();

    editor.waitTextIntoEditor("CodenvyTest");
    editor.typeTextIntoEditor(" codenvyTest = n");
    editor.launchAutocompleteAndWaitContainer();
    editor.waitAutocompleteContainer();
    editor.waitTextIntoAutocompleteContainer("new");
    editor.typeTextIntoEditor(Keys.ENTER.toString());

    editor.typeTextIntoEditor(" Code");
    editor.waitCodeAssistMarkers(ERROR);
    editor.launchAutocompleteAndWaitContainer();
    editor.waitTextIntoAutocompleteContainer("CodenvyTest - org.eclipse.qa.examples");
    editor.waitTextIntoAutocompleteContainer("CodenvyTestInherite - org.eclipse.qa.examples");
    editor.enterAutocompleteProposal("CodenvyTestInherite");
    editor.waitAutocompleteContainerIsClosed();
    editor.typeTextIntoEditor(";");
    editor.waitTextIntoEditor(
        "CodenvyTest codenvyTest = numGuessByUser CodenvyTestInherite;        \n");
  }
}
