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
package org.eclipse.che.selenium.projectexplorer;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Andrienko Alexander
 * @author Andrey Chizhikov
 */
public class RenamedAlreadyCreatedNotJavaFileTest {

  private static final String PROJECT_NAME = "RenameProject1";
  private static final String PATH_TO_WEB_APP = PROJECT_NAME + "/src/main/webapp";
  private static final String PATH_TO_FILE = PROJECT_NAME + "/src/main/webapp/index.jsp";

  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private CodenvyEditor editor;
  @Inject private AskForValueDialog askForValueDialog;
  @Inject private Menu menu;
  @Inject private Loader loader;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/default-spring-project");
    testProjectServiceClient.importProject(
        testWorkspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
    ide.open(testWorkspace);
  }

  @Test
  public void renameWhenFileIsOpenedIntoEditor() throws Exception {
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByPath(PATH_TO_WEB_APP + "/index.jsp");
    editor.waitActive();
    projectExplorer.waitAndSelectItem(PATH_TO_FILE);
    loader.waitOnClosed();
    editor.waitTabIsPresent("index.jsp");
    loader.waitOnClosed();
    renameFile(PATH_TO_FILE);
    checkFileIsRenamedIntoProjectExplorerAndEditor("Renamed.jsp");
  }

  private void renameFile(String pathToFile) {
    projectExplorer.waitAndSelectItem(pathToFile);
    menu.runCommand(TestMenuCommandsConstants.Edit.EDIT, TestMenuCommandsConstants.Edit.RENAME);
    askForValueDialog.waitFormToOpen();
    askForValueDialog.clearInput();
    askForValueDialog.typeAndWaitText("Renamed.jsp");
    askForValueDialog.clickOkBtn();
    askForValueDialog.waitFormToClose();
  }

  private void checkFileIsRenamedIntoProjectExplorerAndEditor(String filename) {
    boolean isItemPresent = false;
    List<String> listOfItems = projectExplorer.getNamesOfAllOpenItems();
    for (String item : listOfItems) {
      if (item.equals(filename)) {
        isItemPresent = true;
      }
    }
    Assert.assertEquals(isItemPresent, true);
    loader.waitOnClosed();
    editor.waitTabIsPresent(filename);
  }
}
