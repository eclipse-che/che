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
package org.eclipse.che.plugin.urlfactory;

import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.testng.Assert.assertEquals;

import java.util.Collections;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.mockito.InjectMocks;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Testing {@link ProjectConfigDtoMerger}
 *
 * @author Florent Benoit
 */
@Listeners(MockitoTestNGListener.class)
public class ProjectConfigDtoMergerTest {

  /** Location */
  private static final String DUMMY_LOCATION = "dummy-location";

  @InjectMocks private ProjectConfigDtoMerger projectConfigDtoMerger;

  private ProjectConfigDto computedProjectConfig;

  private FactoryDto factory;

  @BeforeClass
  public void setup() {
    WorkspaceConfigDto workspaceConfigDto = newDto(WorkspaceConfigDto.class);
    this.factory = newDto(FactoryDto.class).withWorkspace(workspaceConfigDto);

    SourceStorageDto sourceStorageDto = newDto(SourceStorageDto.class).withLocation(DUMMY_LOCATION);
    computedProjectConfig = newDto(ProjectConfigDto.class).withSource(sourceStorageDto);
  }

  /** Check project is added when we have no project */
  @Test
  public void mergeWithoutAnyProject() {

    // no project
    Assert.assertTrue(factory.getWorkspace().getProjects().isEmpty());

    // merge
    projectConfigDtoMerger.merge(factory, computedProjectConfig);

    // project
    assertEquals(factory.getWorkspace().getProjects().size(), 1);

    assertEquals(factory.getWorkspace().getProjects().get(0), computedProjectConfig);
  }

  /** Check source are added if there is only one project without source */
  @Test
  public void mergeWithoutOneProjectWithoutSource() {

    // add existing project
    ProjectConfigDto projectConfigDto = newDto(ProjectConfigDto.class);
    factory.getWorkspace().setProjects(Collections.singletonList(projectConfigDto));
    // no source storage
    Assert.assertNull(projectConfigDto.getSource());

    // merge
    projectConfigDtoMerger.merge(factory, computedProjectConfig);

    // project still 1
    assertEquals(factory.getWorkspace().getProjects().size(), 1);

    SourceStorageDto sourceStorageDto = factory.getWorkspace().getProjects().get(0).getSource();

    assertEquals(sourceStorageDto.getLocation(), DUMMY_LOCATION);
  }
}
