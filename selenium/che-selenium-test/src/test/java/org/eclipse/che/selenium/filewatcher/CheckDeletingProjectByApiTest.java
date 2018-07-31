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
package org.eclipse.che.selenium.filewatcher;

import com.google.inject.Inject;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CheckDeletingProjectByApiTest {

  private String projectName = NameGenerator.generate("project1", 6);
  private String projectName2 = NameGenerator.generate("project2", 6);

  @Inject private TestWorkspace testWorkspace;
  @Inject private TestProjectServiceClient projectServiceClient;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Events events;

  @BeforeClass
  public void setUp() throws Exception {
    createProject(projectName);
    createProject(projectName2);
    ide.open(testWorkspace);
  }

  @Test
  public void shouldDeleteProjectsAndCheckDeleting() throws Exception {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(projectName);
    projectExplorer.waitItem(projectName2);
    events.clickEventLogBtn();
    deleteAndWaitProjectNotExistByApi(projectName);
    deleteAndWaitProjectNotExistByApi(projectName2);
  }

  private void createProject(String projectName) throws Exception {
    projectServiceClient.importProject(
        testWorkspace.getId(),
        Paths.get(getClass().getResource("/projects/default-spring-project").toURI()),
        projectName,
        ProjectTemplates.MAVEN_SPRING);
  }

  private void deleteAndWaitProjectNotExistByApi(String projectName) throws Exception {
    projectServiceClient.deleteResource(testWorkspace.getId(), projectName);
    projectExplorer.waitItemIsNotPresentVisibleArea(projectName);
    events.waitExpectedMessage(String.format("Project %s removed", projectName));
  }
}
