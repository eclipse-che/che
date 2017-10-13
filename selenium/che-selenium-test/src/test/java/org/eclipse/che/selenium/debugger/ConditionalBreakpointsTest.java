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
package org.eclipse.che.selenium.debugger;

import static org.eclipse.che.selenium.core.constant.TestCommandsConstants.MAVEN;
import static org.eclipse.che.selenium.core.project.ProjectTemplates.CONSOLE_JAVA_SIMPLE;

import com.google.inject.Inject;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestCommandServiceClient;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestBuildConstants;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.debug.DebugPanel;
import org.eclipse.che.selenium.pageobject.debug.JavaDebugConfig;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsPalette;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class ConditionalBreakpointsTest {
  private static final String PROJECT = NameGenerator.generate("project", 2);

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;

  @Inject private ProjectExplorer projectExplorer;
  @Inject private CodenvyEditor editor;
  @Inject private DebugPanel debugPanel;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private TestCommandServiceClient testCommandServiceClient;
  @Inject private CommandsPalette commandsPalette;
  @Inject private Consoles consoles;
  @Inject private Menu menu;
  @Inject private NotificationsPopupPanel notifications;
  @Inject private JavaDebugConfig debugConfig;

  @BeforeClass
  public void setUp() throws Exception {
    testProjectServiceClient.importProject(
        ws.getId(),
        Paths.get(
            getClass().getResource("/projects/plugins/DebuggerPlugin/java-multimodule").toURI()),
        PROJECT,
        CONSOLE_JAVA_SIMPLE);

    testCommandServiceClient.createCommand(
        "mvn -f ${current.project.path} clean install &&"
            + " java -Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=y"
            + " -classpath ${current.project.path}/app/target/classes/:${current.project.path}/model/target/classes multimodule.App",
        "debug",
        MAVEN,
        ws.getId());

    ide.open(ws);
    projectExplorer.waitItem(PROJECT);
    projectExplorer.quickExpandWithJavaScript();
    debugPanel.openDebugPanel();

    projectExplorer.openItemByPath(PROJECT + "/app/src/main/java/multimodule/App.java");
  }

  @Test
  public void shouldAddConditionalBreakpoint() throws Exception {
    editor.setBreakpoint(20);
    editor.waitInactiveBreakpoint(20);
    debugPanel.makeBreakpointConditional("App.java", 20, "book1.getTitle().equals(\"java\")");
    editor.waitConditionalBreakpoint(20, false);

    commandsPalette.openCommandPalette();
    commandsPalette.startCommandByDoubleClick("debug");
    consoles.waitExpectedTextIntoConsole(TestBuildConstants.LISTENING_AT_ADDRESS_8000);

    menu.runCommandByXpath(
        TestMenuCommandsConstants.Run.RUN_MENU,
        TestMenuCommandsConstants.Run.DEBUG,
        debugConfig.getXpathToІRunDebugCommand(PROJECT));
    notifications.waitExpectedMessageOnProgressPanelAndClosed("Remote debugger connected");

    editor.waitConditionalBreakpoint(20, true);
  }
}
