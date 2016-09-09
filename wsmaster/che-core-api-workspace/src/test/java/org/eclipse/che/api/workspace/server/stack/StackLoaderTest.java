/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.workspace.server.stack;

import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.api.workspace.server.spi.StackDao;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentRecipeDto;
import org.eclipse.che.api.workspace.shared.dto.ExtendedMachineDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectProblemDto;
import org.eclipse.che.api.workspace.shared.dto.ServerConf2Dto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.api.workspace.shared.dto.stack.StackComponentDto;
import org.eclipse.che.api.workspace.shared.dto.stack.StackDto;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link StackLoader}
 *
 * @author Alexander Andrienko
 */
@Listeners(MockitoTestNGListener.class)
public class StackLoaderTest {

    @Mock
    private StackDao stackDao;

    private StackLoader stackLoader;

    @Test
    public void predefinedStackWithValidJsonShouldBeUpdated() throws ServerException, NotFoundException, ConflictException {
        URL url = Resources.getResource("stacks.json");
        URL urlFolder = Thread.currentThread().getContextClassLoader().getResource("stack_img");

        stackLoader = new StackLoader(url.getPath(), urlFolder.getPath(), stackDao);

        stackLoader.start();
        verify(stackDao, times(5)).update(any());
        verify(stackDao, never()).create(any());
    }

    @Test
    public void predefinedStackWithValidJsonShouldBeCreated() throws ServerException, NotFoundException, ConflictException {
        URL url = Resources.getResource("stacks.json");
        URL urlFolder = Thread.currentThread().getContextClassLoader().getResource("stack_img");

        doThrow(new NotFoundException("Stack is already exist")).when(stackDao).update(any());

        stackLoader = new StackLoader(url.getPath(), urlFolder.getPath(), stackDao);

        stackLoader.start();
        verify(stackDao, times(5)).update(any());
        verify(stackDao, times(5)).create(any());
    }

    @Test
    public void predefinedStackWithValidJsonShouldBeCreated2() throws ServerException, NotFoundException, ConflictException {
        URL url = Resources.getResource("stacks.json");
        URL urlFolder = Thread.currentThread().getContextClassLoader().getResource("stack_img");

        doThrow(new ServerException("Internal server error")).when(stackDao).update(any());

        stackLoader = new StackLoader(url.getPath(), urlFolder.getPath(), stackDao);

        stackLoader.start();
        verify(stackDao, times(5)).update(any());
        verify(stackDao, times(5)).create(any());
    }

    @Test
    public void dtoShouldBeSerialized() {
        StackDto stackDtoDescriptor = newDto(StackDto.class).withName("nameWorkspaceConfig");
        StackComponentDto stackComponentDto = newDto(StackComponentDto.class)
                .withName("java")
                .withVersion("1.8");
        stackDtoDescriptor.setComponents(Collections.singletonList(stackComponentDto));
        stackDtoDescriptor.setTags(Arrays.asList("some teg1", "some teg2"));
        stackDtoDescriptor.setDescription("description");
        stackDtoDescriptor.setId("someId");
        stackDtoDescriptor.setScope("scope");
        stackDtoDescriptor.setCreator("Created in Codenvy");

        Map<String, String> attributes = new HashMap<>();
        attributes.put("attribute1", "valute attribute1");
        Link link = newDto(Link.class).withHref("some url")
                                      .withMethod("get")
                                      .withRel("someRel")
                                      .withConsumes("consumes")
                                      .withProduces("produces");


        HashMap<String, List<String>> projectMap = new HashMap<>();
        projectMap.put("test", Arrays.asList("test", "test2"));

        ProjectProblemDto projectProblem = newDto(ProjectProblemDto.class).withCode(100).withMessage("message");
        SourceStorageDto sourceStorageDto = newDto(SourceStorageDto.class).withType("some type")
                                                                          .withParameters(attributes)
                                                                          .withLocation("location");

        ProjectConfigDto projectConfigDto = newDto(ProjectConfigDto.class).withName("project")
                                                                          .withPath("somePath")
                                                                          .withAttributes(projectMap)
                                                                          .withType("maven type")
                                                                          .withDescription("some project description")
                                                                          .withLinks(Collections.singletonList(link))
                                                                          .withMixins(Collections.singletonList("mixin time"))
                                                                          .withProblems(Collections.singletonList(projectProblem))
                                                                          .withSource(sourceStorageDto);


        EnvironmentRecipeDto environmentRecipe = newDto(EnvironmentRecipeDto.class).withContent("some content")
                                                                                   .withContentType("some content type")
                                                                                   .withType("someType");

        Map<String, ServerConf2Dto> servers = new HashMap<>();
        servers.put("server1Ref", newDto(ServerConf2Dto.class).withPort("8080/tcp")
                                                              .withProtocol("http")
                                                              .withProperties(singletonMap("key", "value")));
        Map<String, ExtendedMachineDto> machines = new HashMap<>();
        machines.put("someMachineName", newDto(ExtendedMachineDto.class).withAgents(Arrays.asList("agent1", "agent2"))
                                                                        .withServers(servers)
                                                                        .withAttributes(singletonMap("memoryLimitBytes", "" + 512L * 1024L * 1024L)));

        EnvironmentDto environmentDto = newDto(EnvironmentDto.class).withRecipe(environmentRecipe)
                                                                    .withMachines(machines);

        CommandDto commandDto = newDto(CommandDto.class).withType("command type")
                                                        .withName("command name")
                                                        .withCommandLine("command line");

        WorkspaceConfigDto workspaceConfigDto = newDto(WorkspaceConfigDto.class).withName("SomeWorkspaceConfig")
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
