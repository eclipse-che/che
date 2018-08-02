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
package org.eclipse.che.selenium.assistant;

import static org.testng.Assert.assertTrue;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CheTerminal;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.KeyBindings;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NavigateToFile;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class KeyBindingsTest {
  private static final String PROJECT_NAME = NameGenerator.generate("project", 6);

  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private CodenvyEditor editor;
  @Inject private Menu menu;
  @Inject private Loader loader;
  @Inject private KeyBindings keyBindings;
  @Inject private NavigateToFile navigateToFile;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private CheTerminal terminal;
  @Inject private Consoles consoles;
  @Inject private TestProjectServiceClient projectServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = KeyBindings.class.getResource("/projects/default-spring-project");
    projectServiceClient.importProject(
        testWorkspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);

    ide.open(testWorkspace);
  }

  @Test
  public void enterKeyCombinationTest() throws Exception {
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.openItemByPath(PROJECT_NAME);
    notificationsPopupPanel.waitProgressPopupPanelClose();
    loader.waitOnClosed();
    keyBindings.enterKeyCombination(Keys.CONTROL, Keys.ALT, Keys.getKeyFromUnicode('n'));
    navigateToFile.waitFormToOpen();
    navigateToFile.closeNavigateToFileForm();
    keyBindings.enterKeyCombination(Keys.ALT, Keys.F12);
    terminal.waitTerminalTab();
    WaitUtils.sleepQuietly(1);
    consoles.closeTerminalIntoConsoles();
    consoles.closeProcessesArea();
  }

  @Test(priority = 1)
  public void searchKeyBindingsTest() throws Exception {
    projectExplorer.waitProjectExplorer();
    menu.runCommand(
        TestMenuCommandsConstants.Assistant.ASSISTANT,
        TestMenuCommandsConstants.Assistant.KEY_BINDINGS);
    keyBindings.checkSearchResultKeyBinding("open", 5);
    keyBindings.clickOkButton();
  }

  @Test(priority = 2)
  public void dialogAboutKeyBindingTest() throws Exception {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);

    projectExplorer.quickExpandWithJavaScript();

    projectExplorer.openItemByVisibleNameInExplorer("AppController.java");
    editor.waitActive();
    menu.runCommand(
        TestMenuCommandsConstants.Assistant.ASSISTANT,
        TestMenuCommandsConstants.Assistant.KEY_BINDINGS);
    loader.waitOnClosed();

    URL resource = KeyBindingsTest.class.getResource("key-bindings.txt");
    List<String> expectedBindings =
        Files.readAllLines(Paths.get(resource.toURI()), Charset.forName("UTF-8"));

    assertTrue(keyBindings.checkAvailabilityAllKeyBindings(expectedBindings));
    keyBindings.clickOkButton();
  }
}
