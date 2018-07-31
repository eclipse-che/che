/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.projectexplorer;

import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuFirstLevelItems.SHOW_REFERENCES;

import com.google.inject.Inject;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Random;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.ShowReference;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Dmitry Shnurenko */
public class ShowFileReferenceTest {
  private static final String PROJECT_NAME =
      NavigationByKeyboardTest.class.getSimpleName() + new Random().nextInt(9999);
  private static final String FILE_FQN = "org.eclipse.qa.examples.AppController";
  private static final String FILE_PATH =
      PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/AppController.java";
  private static final String PATH_FOR_EXPAND =
      PROJECT_NAME + "/src/main/java/org.eclipse.qa.examples";

  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private ShowReference showReference;
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

    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.expandPathInProjectExplorerAndOpenFile(PATH_FOR_EXPAND, "AppController.java");
    projectExplorer.openContextMenuByPathSelectedItem(FILE_PATH);

    projectExplorer.clickOnItemInContextMenu(SHOW_REFERENCES);
  }

  @Test
  public void dialogWithPathAndFqnShouldBeDisplayed() throws IOException {
    showReference.checkFqnFieldValue(FILE_FQN);
    showReference.checkPathFieldValue(FILE_PATH);
  }
}
