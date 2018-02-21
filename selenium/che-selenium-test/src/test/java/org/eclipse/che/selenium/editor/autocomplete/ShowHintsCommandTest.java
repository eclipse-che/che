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
package org.eclipse.che.selenium.editor.autocomplete;

import static org.testng.Assert.fail;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.utils.BrowserLogsUtil;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.CodenvyEditor.MarkersType;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev */
public class ShowHintsCommandTest {
  private final Logger LOG = LoggerFactory.getLogger(ShowHintsCommandTest.class);
  private static final String PROJECT_NAME =
      NameGenerator.generate(ShowHintsCommandTest.class.getSimpleName(), 4);

  private static final String TEXT_IN_POP_UP_1 =
      "<no parameters>\n"
          + "int arg\n"
          + "boolean arg\n"
          + "String arg\n"
          + "int arg, String arg2\n"
          + "int arg, String arg2, boolean arg3";

  private static final String CONSTRUCTOR = "HintTestClass hintTestClass = new HintTestClass(11);";

  private static final String TEXT_IN_POP_UP_2 =
      "<no parameters>\n"
          + "int arg\n"
          + "int arg, String arg2\n"
          + "int arg, String arg2, boolean arg3";

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private Consoles console;
  @Inject private BrowserLogsUtil browserLogsUtil;

  @BeforeClass
  public void prepare() throws Exception {
    URL resource = getClass().getResource("/projects/spring-for-hint-test");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
    ide.open(workspace);
  }

  @Test
  public void checkShowHintsCommand() throws Exception {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    console.closeProcessesArea();
    projectExplorer.expandPathInProjectExplorerAndOpenFile(
        PROJECT_NAME + "/src/main/java/org.eclipse.qa.examples", "AppController.java");
    loader.waitOnClosed();
    projectExplorer.openItemByVisibleNameInExplorer("HintTestClass.java");
    loader.waitOnClosed();

    // check the 'show hints' to all parameters on the overloaded method
    editor.selectTabByName("AppController");
    editor.waitActive();
    editor.setCursorToLine(32);
    editor.typeTextIntoEditor(Keys.TAB.toString());
    editor.typeTextIntoEditor("runCommand();");
    editor.waitTextIntoEditor("runCommand();");
    waitErrorMarkerInPosition();
    editor.goToCursorPositionVisible(32, 5);
    editor.callShowHintsPopUp();
    editor.waitShowHintsPopUpOpened();
    editor.waitExpTextIntoShowHintsPopUp(TEXT_IN_POP_UP_1);
    editor.typeTextIntoEditor(Keys.ESCAPE.toString());
    editor.waitShowHintsPopUpClosed();

    // check the 'show hints' to all parameters on the overloaded constructor
    editor.waitActive();
    editor.setCursorToLine(27);
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.typeTextIntoEditor(Keys.TAB.toString());
    editor.typeTextIntoEditor(CONSTRUCTOR);
    editor.waitTextIntoEditor(CONSTRUCTOR);
    editor.goToCursorPositionVisible(28, 41);
    editor.callShowHintsPopUp();
    editor.waitShowHintsPopUpOpened();
    editor.waitExpTextIntoShowHintsPopUp(TEXT_IN_POP_UP_2);
    editor.typeTextIntoEditor(Keys.ESCAPE.toString());
    editor.waitShowHintsPopUpClosed();
  }

  private void waitErrorMarkerInPosition() throws Exception {
    try {
      editor.waitMarkerInPosition(MarkersType.ERROR_MARKER, 33);
    } catch (TimeoutException ex) {
      logExternalLibraries();
      logProjectTypeChecking();
      logProjectLanguageChecking();
      browserLogsUtil.storeLogs();

      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/7161", ex);
    }
  }

  private void logExternalLibraries() throws Exception {
    testProjectServiceClient
        .getExternalLibraries(workspace.getId(), PROJECT_NAME)
        .forEach(library -> LOG.info("project external library:  {}", library));
  }

  private void logProjectTypeChecking() throws Exception {
    LOG.info(
        "Project type of the {} project is \"maven\" - {}",
        PROJECT_NAME,
        testProjectServiceClient.checkProjectType(workspace.getId(), PROJECT_NAME, "maven"));
  }

  private void logProjectLanguageChecking() throws Exception {
    LOG.info(
        "Project language of the {} project is \"java\" - {}",
        PROJECT_NAME,
        testProjectServiceClient.checkProjectLanguage(workspace.getId(), PROJECT_NAME, "java"));
  }
}
