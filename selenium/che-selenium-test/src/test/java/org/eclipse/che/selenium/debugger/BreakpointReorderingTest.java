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
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.action.ActionsFactory;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.debug.DebugPanel;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class BreakpointReorderingTest {
  private static final String PROJECT_NAME = NameGenerator.generate("project", 2);

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;

  @Inject private ProjectExplorer projectExplorer;
  @Inject private CodenvyEditor editor;
  @Inject private DebugPanel debugPanel;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private ActionsFactory actionsFactory;

  @BeforeClass
  public void setUp() throws Exception {
    testProjectServiceClient.importProject(
        ws.getId(),
        Paths.get(getClass().getResource("/projects/debug-spring-project").toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);

    ide.open(ws);
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
    debugPanel.openDebugPanel();

    projectExplorer.openItemByPath(
        PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/AppController.java");

    editor.setCursorToLine(26);
    editor.setInactiveBreakpoint(26);
    editor.setInactiveBreakpoint(29);
    editor.setInactiveBreakpoint(31);
    editor.setCursorToLine(38);
    editor.setInactiveBreakpoint(38);
  }

  @Test
  public void shouldNotRemoveBreakpointWhenFirstCharacterRemoved() throws Exception {
    editor.goToCursorPositionVisible(26, 1);
    actionsFactory.createAction(seleniumWebDriver).sendKeys(Keys.DELETE).build().perform();

    editor.waitInactiveBreakpoint(26);
  }

  @Test(priority = 1)
  public void shouldNotRemoveBreakpointWhenLastCharacterRemoved() throws Exception {
    editor.goToCursorPositionVisible(26, 65);
    actionsFactory.createAction(seleniumWebDriver).sendKeys(Keys.BACK_SPACE).build().perform();

    editor.waitInactiveBreakpoint(26);
  }

  @Test(priority = 2)
  public void shouldReorderBreakpointsWhenLineRemoved() throws Exception {
    editor.deleteCurrentLine();

    editor.waitInactiveBreakpoint(28);
    editor.waitInactiveBreakpoint(30);
    editor.waitInactiveBreakpoint(37);

    actionsFactory
        .createAction(seleniumWebDriver)
        .keyDown(Keys.CONTROL)
        .sendKeys("z")
        .keyUp(Keys.CONTROL)
        .perform();

    editor.waitInactiveBreakpoint(29);
    editor.waitInactiveBreakpoint(31);
    editor.waitInactiveBreakpoint(38);
  }

  @Test(priority = 3)
  public void shouldReorderBreakpointsWhenLineAdded() throws Exception {
    editor.goToCursorPositionVisible(26, 1);
    actionsFactory.createAction(seleniumWebDriver).sendKeys(Keys.ENTER).build().perform();

    editor.waitInactiveBreakpoint(30);
    editor.waitInactiveBreakpoint(32);
    editor.waitInactiveBreakpoint(39);

    actionsFactory
        .createAction(seleniumWebDriver)
        .keyDown(Keys.CONTROL)
        .sendKeys("z")
        .keyUp(Keys.CONTROL)
        .perform();

    editor.waitInactiveBreakpoint(29);
    editor.waitInactiveBreakpoint(31);
    editor.waitInactiveBreakpoint(38);
  }
}
