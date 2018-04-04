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
package org.eclipse.che.selenium.debugger;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
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
import org.eclipse.che.selenium.pageobject.debug.DebugPanel;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class CheckBreakPointStateTest {
  private static final String PROJECT_NAME = NameGenerator.generate("project", 3);
  private static final String PROJECT_NAME_2 = NameGenerator.generate("project", 3);
  private static final String PATH_PREFFIX = "/src/main/java/org/eclipse/qa/examples/";
  private static final String PATH_TO_PROJECT_WITH_ONE_CLASS = PROJECT_NAME + PATH_PREFFIX;
  private static final String PATH_TO_PROJECT_WITH_TWO_CLASSES = PROJECT_NAME_2 + PATH_PREFFIX;

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;

  @Inject private ProjectExplorer projectExplorer;
  @Inject private CodenvyEditor editor;
  @Inject private DebugPanel debugPanel;
  @Inject private Menu menu;
  @Inject private Loader loader;
  @Inject private AskDialog askDialog;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private SeleniumWebDriver seleniumWebDriver;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource =
        CheckBreakPointStateTest.this.getClass().getResource("/projects/debug-spring-project");
    testProjectServiceClient.importProject(
        ws.getId(), Paths.get(resource.toURI()), PROJECT_NAME, ProjectTemplates.MAVEN_SPRING);

    resource = CheckBreakPointStateTest.this.getClass().getResource("/projects/debugStepInto");
    testProjectServiceClient.importProject(
        ws.getId(), Paths.get(resource.toURI()), PROJECT_NAME_2, ProjectTemplates.MAVEN_SPRING);

    ide.open(ws);
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.waitItem(PROJECT_NAME_2);
    projectExplorer.quickExpandWithJavaScript();
  }

  @Test
  public void checkStateAfterDeletionFileAndFolder() throws Exception {
    String expectedBreakpointsForAdditionalClass =
        "AdditonalClass.java:7\n" + "AdditonalClass.java:9";

    String expectedBreakpointsForGreetingClass =
        "AppController.java:29\n" + "AppController.java:30\n" + "AppController.java:31";

    projectExplorer.openItemByPath(PATH_TO_PROJECT_WITH_TWO_CLASSES + "AdditonalClass.java");
    editor.waitActive();
    editor.setInactiveBreakpoint(7);
    editor.setInactiveBreakpoint(9);
    debugPanel.openDebugPanel();
    debugPanel.waitContentInBreakPointPanel(expectedBreakpointsForAdditionalClass);
    projectExplorer.waitAndSelectItem(PATH_TO_PROJECT_WITH_TWO_CLASSES + "AdditonalClass.java");
    menu.runCommand(TestMenuCommandsConstants.Edit.EDIT, TestMenuCommandsConstants.Edit.DELETE);
    loader.waitOnClosed();
    askDialog.confirmAndWaitClosed();
    projectExplorer.waitDisappearItemByPath(
        PATH_TO_PROJECT_WITH_TWO_CLASSES + "AdditonalClass.java");
    debugPanel.waitBreakPointsPanelIsEmpty();
    projectExplorer.openItemByPath(PATH_TO_PROJECT_WITH_TWO_CLASSES + "AppController.java");
    editor.setInactiveBreakpoint(29);
    editor.setInactiveBreakpoint(30);
    editor.setInactiveBreakpoint(31);
    debugPanel.openDebugPanel();
    debugPanel.waitContentInBreakPointPanel(expectedBreakpointsForGreetingClass);
    projectExplorer.waitAndSelectItem(PROJECT_NAME_2 + "/src/main/java");
    menu.runCommand(TestMenuCommandsConstants.Edit.EDIT, TestMenuCommandsConstants.Edit.DELETE);
    loader.waitOnClosed();
    askDialog.confirmAndWaitClosed();
    projectExplorer.waitDisappearItemByPath(PROJECT_NAME_2 + "/src/main/java");
    debugPanel.waitBreakPointsPanelIsEmpty();
    testProjectServiceClient.deleteResource(ws.getId(), PROJECT_NAME_2);
    projectExplorer.waitDisappearItemByPath(PROJECT_NAME_2);
  }

  @Test(priority = 1)
  public void checkStateAfterDeletionProject() {
    String expectedBreakpointsForGreetingClass =
        "AppController.java:29\n" + "AppController.java:31\n" + "AppController.java:34";
    projectExplorer.openItemByPath(PATH_TO_PROJECT_WITH_ONE_CLASS + "AppController.java");
    editor.setCursorToLine(35);
    editor.setInactiveBreakpoint(29);
    editor.setInactiveBreakpoint(31);
    editor.setInactiveBreakpoint(34);
    debugPanel.openDebugPanel();
    debugPanel.waitContentInBreakPointPanel(expectedBreakpointsForGreetingClass);
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(TestMenuCommandsConstants.Edit.EDIT, TestMenuCommandsConstants.Edit.DELETE);
    loader.waitOnClosed();
    askDialog.confirmAndWaitClosed();
    projectExplorer.waitDisappearItemByPath(PROJECT_NAME);
    debugPanel.waitBreakPointsPanelIsEmpty();
    seleniumWebDriver.navigate().refresh();
  }
}
