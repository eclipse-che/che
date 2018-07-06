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
package org.eclipse.che.selenium.languageserver;

import static java.lang.String.format;
import static org.eclipse.che.selenium.core.workspace.WorkspaceTemplate.UBUNTU_CAMEL;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.InjectTestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CamelFileEditingTest {
  private static final String PROJECT_NAME = "spring-project-for-camel-ls";
  private static final String CAMEL_XML_FILE = "camel.xml";
  private static final String PATH_TO_CAMEL_FILE = PROJECT_NAME + "/" + CAMEL_XML_FILE;
  private static final String LS_INIT_MESSAGE =
      format("Finished language servers initialization, file path '/%s'", PATH_TO_CAMEL_FILE);

  @InjectTestWorkspace(template = UBUNTU_CAMEL)
  private TestWorkspace workspace;

  @Inject private Ide ide;
  @Inject private Consoles consoles;
  @Inject private CodenvyEditor editor;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = CamelFileEditingTest.class.getResource("/projects/spring-project-for-camel-ls");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.CONSOLE_JAVA_SIMPLE);
    ide.open(workspace);
  }

  @Test
  public void checkLanguageServerInitialized() {
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    projectExplorer.openItemByPath(PROJECT_NAME);
    projectExplorer.openItemByPath(PATH_TO_CAMEL_FILE);
    editor.waitTabIsPresent(CAMEL_XML_FILE);

    // check camel language sever initialized
    consoles.selectProcessByTabName("dev-machine");
    consoles.waitExpectedTextIntoConsole(LS_INIT_MESSAGE);
  }

  @Test(priority = 1)
  public void checkAutocompleteFeature() {
    editor.selectTabByName(CAMEL_XML_FILE);

    editor.goToPosition(49, 21);

    editor.launchAutocomplete();
    editor.waitTextIntoEditor("timer:timerName");

    editor.typeTextIntoEditor("?");
    editor.launchAutocompleteAndWaitContainer();
    editor.waitTextIntoAutocompleteContainer("fixedRate ");
    editor.enterAutocompleteProposal("fixedRate ");
    editor.waitTextIntoEditor("timer:timerName?fixedRate=false");

    editor.typeTextIntoEditor("&amp;");
    editor.launchAutocompleteAndWaitContainer();
    editor.waitTextIntoAutocompleteContainer("exchangePattern ");
    editor.enterAutocompleteProposal("exchangePattern ");
    editor.waitTextIntoEditor("timer:timerName?fixedRate=false&amp;exchangePattern=");

    editor.launchAutocompleteAndWaitContainer();
    editor.waitTextIntoAutocompleteContainer("InOnly");
    editor.enterAutocompleteProposal("InOnly");
    editor.waitTextIntoEditor("timer:timerName?fixedRate=false&amp;exchangePattern=InOnly");
  }

  @Test(priority = 2)
  public void checkHoverFeature() {
    editor.moveCursorToText("timer");

    editor.waitTextInHoverPopup(
        "The timer component is used for generating message exchanges when a timer fires.");
  }
}
