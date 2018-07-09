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
package org.eclipse.che.selenium.editor;

import com.google.inject.Inject;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class UnexpectedWsAgentStopTest {
  private static final String PROJECT_NAME = NameGenerator.generate("WsAgentTest", 4);
  private static final String PATH_TO_CHECKING_FILE = PROJECT_NAME + "/src/main/java/che.eclipse.sample";

  @Inject private ProjectExplorer projectExplorer;
  @Inject private CodenvyEditor codenvyEditor;
  @Inject private Ide ide;
  @Inject private TestWorkspace testWorkspace;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private DefaultTestUser defaultTestUser;

  @BeforeClass
  public void prepare() throws Exception {
    Path projectPath =
        Paths.get(getClass().getResource("/projects/default-spring-project").toURI());
    testProjectServiceClient.importProject(
        testWorkspace.getId(), projectPath, PROJECT_NAME, ProjectTemplates.MAVEN_SPRING);

    ide.open(testWorkspace);
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
  }

  @Test
  public void editorShouldBeInReadonlyModeAfterUnexpectedWsAgentStopping() {
    projectExplorer.expandPathInProjectExplorerAndOpenFile(PATH_TO_CHECKING_FILE, "Aclass.java");
  }
}
