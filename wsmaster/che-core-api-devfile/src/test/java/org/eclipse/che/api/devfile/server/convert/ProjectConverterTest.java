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
package org.eclipse.che.api.devfile.server.convert;

import static org.eclipse.che.api.core.model.workspace.config.SourceStorage.REFSPEC_PARAMETER_NAME;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import org.eclipse.che.api.devfile.model.Project;
import org.eclipse.che.api.devfile.model.Source;
import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.SourceStorageImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Sergii Leshchenko */
public class ProjectConverterTest {

  private ProjectConverter projectConverter;

  @BeforeMethod
  public void setUp() {
    projectConverter = new ProjectConverter();
  }

  @Test
  public void testConvertingDevfileProjectToProjectConfig() {
    Project devfileProject =
        new Project()
            .withName("myProject")
            .withSource(
                new Source()
                    .withLocation("https://github.com/eclipse/che.git")
                    .withType("git")
                    .withRefspec("master"));

    ProjectConfigImpl workspaceProject = projectConverter.toWorkspaceProject(devfileProject);

    assertEquals(workspaceProject.getName(), "myProject");
    assertEquals(workspaceProject.getPath(), "/myProject");
    SourceStorageImpl source = workspaceProject.getSource();
    assertEquals(source.getType(), "git");
    assertEquals(source.getLocation(), "https://github.com/eclipse/che.git");
    assertEquals(source.getParameters().get(REFSPEC_PARAMETER_NAME), "master");
  }

  @Test
  public void testConvertingProjectConfigToDevfileProject() {
    ProjectConfigImpl workspaceProject = new ProjectConfigImpl();
    workspaceProject.setName("myProject");
    workspaceProject.setPath("/ignored");
    SourceStorageImpl sourceStorage = new SourceStorageImpl();
    sourceStorage.setType("git");
    sourceStorage.setLocation("https://github.com/eclipse/che.git");
    sourceStorage.setParameters(ImmutableMap.of(REFSPEC_PARAMETER_NAME, "master"));
    workspaceProject.setSource(sourceStorage);

    Project devfileProject = projectConverter.toDevfileProject(workspaceProject);

    assertEquals(devfileProject.getName(), "myProject");
    Source source = devfileProject.getSource();
    assertEquals(source.getType(), "git");
    assertEquals(source.getLocation(), "https://github.com/eclipse/che.git");
    assertEquals(source.getRefspec(), "master");
  }
}
