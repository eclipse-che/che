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
package org.eclipse.che.selenium.git;

import static org.eclipse.che.selenium.core.constant.TestGitConstants.GIT_INITIALIZED_SUCCESS;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.*;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.*;
import org.eclipse.che.selenium.pageobject.git.Git;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Igor Vnokur */
public class GitChangeMarkersTest {
  private static final String PROJECT_NAME = NameGenerator.generate("GitColors_", 4);

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;

  @Inject private ProjectExplorer projectExplorer;
  @Inject private Menu menu;
  @Inject private AskDialog askDialog;
  @Inject private Git git;
  @Inject private Events events;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void prepare() throws Exception {
    URL resource = getClass().getResource("/projects/simple-java-project");
    testProjectServiceClient.importProject(
        ws.getId(), Paths.get(resource.toURI()), PROJECT_NAME, ProjectTemplates.MAVEN_SPRING);
    ide.open(ws);
  }

  @Test
  public void testGitChangeMarkers() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.openItemByPath(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
    menu.runCommand(GIT, INITIALIZE_REPOSITORY);
    askDialog.waitFormToOpen();
    askDialog.clickOkBtn();
    askDialog.waitFormToClose();
    git.waitGitStatusBarWithMess(GIT_INITIALIZED_SUCCESS);
    events.clickProjectEventsTab();
    events.waitExpectedMessage(GIT_INITIALIZED_SUCCESS);

    // perform init commit
    projectExplorer.selectItem(PROJECT_NAME);
    menu.runCommand(GIT, TestMenuCommandsConstants.Git.COMMIT);
    git.waitAndRunCommit("init");
    loader.waitOnClosed();

    projectExplorer.openItemByPath(PROJECT_NAME + "/src/com/company/Main.java");
    editor.waitNoGitChangeMarkers();
    editor.typeTextIntoEditor("//", 11);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitGitModificationMarkerInPosition(11, 13);

    editor.setCursorToLine(16);
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitGitInsertionMarkerInPosition(17, 18);

    editor.setCursorToLine(20);
    editor.deleteCurrentLine();
    editor.waitGitDeletionMarkerInPosition(19);
  }
}
