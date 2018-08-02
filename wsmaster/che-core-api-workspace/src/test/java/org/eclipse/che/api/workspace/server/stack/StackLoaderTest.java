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
package org.eclipse.che.api.workspace.server.stack;

import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.api.workspace.server.spi.StackDao;
import org.eclipse.che.api.workspace.shared.dto.CommandDto;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.MachineConfigDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectProblemDto;
import org.eclipse.che.api.workspace.shared.dto.RecipeDto;
import org.eclipse.che.api.workspace.shared.dto.ServerConfigDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.api.workspace.shared.dto.stack.StackComponentDto;
import org.eclipse.che.api.workspace.shared.dto.stack.StackDto;
import org.eclipse.che.core.db.DBInitializer;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link StackLoader}
 *
 * @author Alexander Andrienko
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class StackLoaderTest {

  @Mock private StackDao stackDao;

  @Mock private DBInitializer dbInitializer;

  private StackLoader stackLoader;

  @BeforeMethod
  public void startup() throws Exception {
    when(dbInitializer.isBareInit()).thenReturn(true);
    stackLoader =
        new StackLoader(
            false, ImmutableMap.of("stacks.json", "stack_img"), stackDao, dbInitializer);
  }

  @Test
  public void predefinedStackWithValidJsonShouldBeUpdated() throws Exception {
    stackLoader.start();

    verify(stackDao, times(5)).update(any());
    verify(stackDao, never()).create(any());
  }

  @Test
  public void predefinedStackWithValidJsonShouldBeCreated() throws Exception {
    doThrow(new NotFoundException("Stack is already exist")).when(stackDao).update(any());

    stackLoader.start();

    verify(stackDao, times(5)).update(any());
    verify(stackDao, times(5)).create(any());
  }

  @Test
  public void doNotThrowExceptionWhenUpdateFailed() throws Exception {
    doThrow(new ServerException("Internal server error")).when(stackDao).update(any());

    stackLoader.start();

    verify(stackDao, times(5)).update(any());
    verify(stackDao, never()).create(any());
  }

  @Test
  public void doNotThrowExceptionWhenCreationFailed() throws Exception {
    doThrow(new NotFoundException("Not found")).when(stackDao).update(any());
    doThrow(new ServerException("Internal server error")).when(stackDao).create(any());

    stackLoader.start();

    verify(stackDao, times(5)).update(any());
    verify(stackDao, times(5)).create(any());
  }

  @Test
  public void testOverrideStacksWithoutImages() throws Exception {
    final Map<String, String> map = new HashMap<>();
    map.put("stacks.json", null);
    stackLoader = new StackLoader(true, map, stackDao, dbInitializer);

    stackLoader.start();

    verify(stackDao, times(5)).update(any());
    verify(stackDao, never()).create(any());
  }

  @Test
  public void shouldNotLoadStackWhenDBAlreadyInitialized() throws Exception {
    when(dbInitializer.isBareInit()).thenReturn(false);

    stackLoader.start();

    verify(stackDao, never()).update(any());
    verify(stackDao, never()).create(any());
  }

  @Test
  public void dtoShouldBeSerialized() throws Exception {
    StackDto stackDtoDescriptor = newDto(StackDto.class).withName("nameWorkspaceConfig");
    StackComponentDto stackComponentDto =
        newDto(StackComponentDto.class).withName("java").withVersion("1.8");
    stackDtoDescriptor.setComponents(Collections.singletonList(stackComponentDto));
    stackDtoDescriptor.setTags(Arrays.asList("some teg1", "some teg2"));
    stackDtoDescriptor.setDescription("description");
    stackDtoDescriptor.setId("someId");
    stackDtoDescriptor.setScope("scope");
    stackDtoDescriptor.setCreator("Created in Codenvy");

    Map<String, String> attributes = new HashMap<>();
    attributes.put("attribute1", "valute attribute1");
    Link link =
        newDto(Link.class)
            .withHref("some url")
            .withMethod("get")
            .withRel("someRel")
            .withConsumes("consumes")
            .withProduces("produces");

    HashMap<String, List<String>> projectMap = new HashMap<>();
    projectMap.put("test", Arrays.asList("test", "test2"));

    ProjectProblemDto projectProblem =
        newDto(ProjectProblemDto.class).withCode(100).withMessage("message");
    SourceStorageDto sourceStorageDto =
        newDto(SourceStorageDto.class)
            .withType("some type")
            .withParameters(attributes)
            .withLocation("location");

    ProjectConfigDto projectConfigDto =
        newDto(ProjectConfigDto.class)
            .withName("project")
            .withPath("somePath")
            .withAttributes(projectMap)
            .withType("maven type")
            .withDescription("some project description")
            .withLinks(Collections.singletonList(link))
            .withMixins(Collections.singletonList("mixin time"))
            .withProblems(Collections.singletonList(projectProblem))
            .withSource(sourceStorageDto);

    RecipeDto environmentRecipe =
        newDto(RecipeDto.class)
            .withContent("some content")
            .withContentType("some content type")
            .withType("someType");

    Map<String, ServerConfigDto> servers = new HashMap<>();
    servers.put(
        "server1Ref",
        newDto(ServerConfigDto.class)
            .withPort("8080/tcp")
            .withProtocol("http")
            .withAttributes(singletonMap("key", "value")));
    Map<String, MachineConfigDto> machines = new HashMap<>();
    machines.put(
        "someMachineName",
        newDto(MachineConfigDto.class)
            .withInstallers(Arrays.asList("agent1", "agent2"))
            .withServers(servers)
            .withAttributes(singletonMap(MEMORY_LIMIT_ATTRIBUTE, "" + 512L * 1024L * 1024L)));

    EnvironmentDto environmentDto =
        newDto(EnvironmentDto.class).withRecipe(environmentRecipe).withMachines(machines);

    CommandDto commandDto =
        newDto(CommandDto.class)
            .withType("command type")
            .withName("command name")
            .withCommandLine("command line");

    WorkspaceConfigDto workspaceConfigDto =
        newDto(WorkspaceConfigDto.class)
            .withName("SomeWorkspaceConfig")
            .withDescription("some workspace")
            .withLinks(Collections.singletonList(link))
            .withDefaultEnv("some Default Env name")
            .withProjects(Collections.singletonList(projectConfigDto))
            .withEnvironments(singletonMap("name", environmentDto))
            .withCommands(Collections.singletonList(commandDto));

    stackDtoDescriptor.setWorkspaceConfig(workspaceConfigDto);
    Gson GSON = new GsonBuilder().create();

    GSON.fromJson(stackDtoDescriptor.toString(), StackImpl.class);
  }
}
