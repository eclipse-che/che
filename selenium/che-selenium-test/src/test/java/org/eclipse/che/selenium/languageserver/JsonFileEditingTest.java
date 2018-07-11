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
import static org.eclipse.che.selenium.core.workspace.WorkspaceTemplate.NODEJS_WITH_JSON_LS;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.workspace.InjectTestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Skoryk Serhii */
public class JsonFileEditingTest {

  private static final String PROJECT_NAME = "nodejs-hello-world";
  private static final String JSON_FILE_NAME = "package.json";
  private static final String PATH_TO_JSON_FILE = PROJECT_NAME + "/" + JSON_FILE_NAME;
  private static final String LS_INIT_MESSAGE =
      format("Finished language servers initialization, file path '/%s'", PATH_TO_JSON_FILE);

  @InjectTestWorkspace(template = NODEJS_WITH_JSON_LS)
  private TestWorkspace workspace;

  @Inject private Ide ide;
  @Inject private Consoles consoles;
  @Inject private CodenvyEditor editor;
  @Inject private Wizard wizard;
  @Inject private ProjectExplorer projectExplorer;

  @BeforeClass
  public void setUp() throws Exception {
    ide.open(workspace);
  }

  @Test
  public void checkLanguageServerInitialized() {
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    projectExplorer.openItemByPath(PROJECT_NAME);
    projectExplorer.openItemByPath(PATH_TO_JSON_FILE);
    editor.waitTabIsPresent(JSON_FILE_NAME);

    // check JSON language sever initialized
    consoles.selectProcessByTabName("dev-machine");
    consoles.waitExpectedTextIntoConsole(LS_INIT_MESSAGE);
  }

  @Test(priority = 1)
  public void checkCodeValidationFeature() {
    editor.selectTabByName(JSON_FILE_NAME);

    // TODO Remove , symbol after last brace and check error marker.
    // Make sure that error marker appears.
    // Click on the marker and check message like Expected '(end)' and instead saw ':'.in
    // the proposal window. Return the just deleted coma and wait disappearance the marker.
  }

  @Test(priority = 1)
  public void checkFormatCodeFeature() {
    // TODO  Go to the line 9 and add fragment like: "newObj":[1,2,3],. Make sure that JSON does not
    // have any errors.
  }
}
