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
package org.eclipse.che.plugin.urlfactory;

import static java.lang.Boolean.FALSE;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.eclipse.che.plugin.urlfactory.URLFactoryBuilder.DEFAULT_DOCKER_IMAGE;
import static org.eclipse.che.plugin.urlfactory.URLFactoryBuilder.DEFAULT_MEMORY_LIMIT_BYTES;
import static org.eclipse.che.plugin.urlfactory.URLFactoryBuilder.MACHINE_NAME;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.MachineConfigDto;
import org.eclipse.che.api.workspace.shared.dto.RecipeDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.dto.server.DtoFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Testing {@link URLFactoryBuilder}
 *
 * @author Florent Benoit
 */
@Listeners(MockitoTestNGListener.class)
public class URLFactoryBuilderTest {

  /** Check if URL is existing or not */
  @Mock private URLChecker urlChecker;

  /** Grab content of URLs */
  @Mock private URLFetcher urlFetcher;

  /** Tested instance. */
  @InjectMocks private URLFactoryBuilder urlFactoryBuilder;

  /** Check if not specifying a custom docker file we have the default value */
  @Test
  public void checkDefaultImage() throws Exception {

    RecipeDto recipeDto =
        newDto(RecipeDto.class).withContent(DEFAULT_DOCKER_IMAGE).withType("dockerimage");
    MachineConfigDto machine =
        newDto(MachineConfigDto.class)
            .withInstallers(singletonList("org.eclipse.che.ws-agent"))
            .withAttributes(singletonMap(MEMORY_LIMIT_ATTRIBUTE, DEFAULT_MEMORY_LIMIT_BYTES));

    // setup environment
    EnvironmentDto environmentDto =
        newDto(EnvironmentDto.class)
            .withRecipe(recipeDto)
            .withMachines(singletonMap(MACHINE_NAME, machine));
    // setup environment
    WorkspaceConfigDto expectedWsConfig =
        newDto(WorkspaceConfigDto.class)
            .withDefaultEnv("foo")
            .withEnvironments(singletonMap("foo", environmentDto))
            .withName("dumm");

    WorkspaceConfigDto actualWsConfigDto =
        urlFactoryBuilder.buildWorkspaceConfig("foo", "dumm", null);

    assertEquals(actualWsConfigDto, expectedWsConfig);
  }

  /**
   * Check that by specifying a location of custom dockerfile it's stored in the machine source if
   * URL is accessible
   */
  @Test
  public void checkWithCustomDockerfile() throws Exception {

    String myLocation = "http://foo-location";
    RecipeDto recipeDto =
        newDto(RecipeDto.class)
            .withLocation(myLocation)
            .withType("dockerfile")
            .withContentType("text/x-dockerfile");
    MachineConfigDto machine =
        newDto(MachineConfigDto.class)
            .withInstallers(singletonList("org.eclipse.che.ws-agent"))
            .withAttributes(singletonMap(MEMORY_LIMIT_ATTRIBUTE, DEFAULT_MEMORY_LIMIT_BYTES));

    // setup environment
    EnvironmentDto environmentDto =
        newDto(EnvironmentDto.class)
            .withRecipe(recipeDto)
            .withMachines(singletonMap(MACHINE_NAME, machine));

    WorkspaceConfigDto expectedWsConfig =
        newDto(WorkspaceConfigDto.class)
            .withDefaultEnv("foo")
            .withEnvironments(singletonMap("foo", environmentDto))
            .withName("dumm");

    when(urlChecker.exists(myLocation)).thenReturn(true);

    WorkspaceConfigDto actualWsConfigDto =
        urlFactoryBuilder.buildWorkspaceConfig("foo", "dumm", myLocation);

    assertEquals(actualWsConfigDto, expectedWsConfig);
  }

  /**
   * Check that by specifying a location of custom dockerfile it's stored in the machine source if
   * URL is accessible
   */
  @Test
  public void checkWithNonAccessibleCustomDockerfile() throws Exception {
    String myLocation = "http://foo-location";
    RecipeDto recipeDto =
        newDto(RecipeDto.class).withContent(DEFAULT_DOCKER_IMAGE).withType("dockerimage");
    MachineConfigDto machine =
        newDto(MachineConfigDto.class)
            .withInstallers(singletonList("org.eclipse.che.ws-agent"))
            .withAttributes(singletonMap(MEMORY_LIMIT_ATTRIBUTE, DEFAULT_MEMORY_LIMIT_BYTES));

    // setup environment
    EnvironmentDto environmentDto =
        newDto(EnvironmentDto.class)
            .withRecipe(recipeDto)
            .withMachines(singletonMap(MACHINE_NAME, machine));

    WorkspaceConfigDto expectedWsConfig =
        newDto(WorkspaceConfigDto.class)
            .withDefaultEnv("foo")
            .withEnvironments(singletonMap("foo", environmentDto))
            .withName("dumm");

    when(urlChecker.exists(myLocation)).thenReturn(false);

    WorkspaceConfigDto actualWsConfigDto =
        urlFactoryBuilder.buildWorkspaceConfig("foo", "dumm", myLocation);

    assertEquals(actualWsConfigDto, expectedWsConfig);
  }

  /** Check that with a custom factory.json we've this factory being built */
  @Test
  public void checkWithCustomFactoryJsonFile() throws Exception {

    WorkspaceConfigDto workspaceConfigDto = newDto(WorkspaceConfigDto.class);
    FactoryDto templateFactory =
        newDto(FactoryDto.class).withV("4.0").withName("florent").withWorkspace(workspaceConfigDto);
    String jsonFactory = DtoFactory.getInstance().toJson(templateFactory);

    String myLocation = "http://foo-location";
    when(urlChecker.exists(myLocation)).thenReturn(FALSE);
    when(urlFetcher.fetch(myLocation)).thenReturn(jsonFactory);

    FactoryDto factory = urlFactoryBuilder.createFactory(myLocation);

    assertEquals(templateFactory, factory);
  }

  /** Check that without specifying a custom factory.json we've default factory */
  @Test
  public void checkWithDefaultFactoryJsonFile() throws Exception {

    FactoryDto factory = urlFactoryBuilder.createFactory(null);

    assertNull(factory.getWorkspace());
    assertEquals(factory.getV(), "4.0");
  }
}
