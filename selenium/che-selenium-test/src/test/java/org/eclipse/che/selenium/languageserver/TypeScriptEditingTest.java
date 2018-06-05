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

import static org.eclipse.che.selenium.core.workspace.WorkspaceTemplate.ECLIPSE_NODEJS;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.InjectTestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsPalette;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class TypeScriptEditingTest {
  private static final String PROJECT_NAME =
      NameGenerator.generate(TypeScriptEditingTest.class.getSimpleName(), 4);
  private static final String PATH_TO_GREETER_FILE = PROJECT_NAME + "/Greeter.ts";
  private static final String PATH_TO_PRINT_TEST_FILE = PROJECT_NAME + "/printTest.ts";

  @InjectTestWorkspace(template = ECLIPSE_NODEJS)
  private TestWorkspace workspace;

  @Inject private Ide ide;
  @Inject private Menu menu;
  @Inject private Wizard wizard;
  @Inject private Consoles consoles;
  @Inject private CodenvyEditor editor;
  @Inject private CommandsPalette commandsPalette;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private AskForValueDialog askForValueDialog;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/type-script-simple-project");
    testProjectServiceClient.importProject(
        workspace.getId(), Paths.get(resource.toURI()), PROJECT_NAME, ProjectTemplates.NODE_JS);
    ide.open(workspace);
    projectExplorer.waitVisibleItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByPath(PATH_TO_GREETER_FILE);
  }

  @Test
  public void checkMainFeaturesTypeScriptLS() {

    String intitTypeScriptLanguageServerMessage =
        String.format(
            "Finished language servers initialization, file path '/%s'", PATH_TO_GREETER_FILE);

    consoles.waitExpectedTextIntoConsole(intitTypeScriptLanguageServerMessage);
    editor.waitActive();
    editor.goToPosition(13, 2);
    editor.typeTextIntoEditor(Keys.SPACE.toString());
  }
}
