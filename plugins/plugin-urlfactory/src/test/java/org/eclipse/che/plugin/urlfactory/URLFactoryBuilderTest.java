/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.urlfactory;

import org.eclipse.che.api.factory.server.FactoryMessageBodyAdapter;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentRecipeDto;
import org.eclipse.che.api.workspace.shared.dto.ExtendedMachineDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.dto.server.DtoFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.eclipse.che.plugin.urlfactory.URLFactoryBuilder.DEFAULT_DOCKER_IMAGE;
import static org.eclipse.che.plugin.urlfactory.URLFactoryBuilder.MACHINE_NAME;
import static org.eclipse.che.plugin.urlfactory.URLFactoryBuilder.MEMORY_LIMIT_BYTES;
import static java.lang.Boolean.FALSE;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * Testing {@link URLFactoryBuilder}
 *
 * @author Florent Benoit
 */
@Listeners(MockitoTestNGListener.class)
public class URLFactoryBuilderTest {

    /**
     * Check if URL is existing or not
     */
    @Mock
    private URLChecker URLChecker;

    /**
     * Grab content of URLs
     */
    @Mock
    private URLFetcher URLFetcher;

    @Mock
    private FactoryMessageBodyAdapter factoryAdapter;

    /**
     * Tested instance.
     */
    @InjectMocks
    private URLFactoryBuilder urlFactoryBuilder;

    /**
     * Check if not specifying a custom docker file we have the default value
     */
    @Test
    public void checkDefaultImage() throws Exception {

        EnvironmentRecipeDto recipeDto = newDto(EnvironmentRecipeDto.class).withLocation(DEFAULT_DOCKER_IMAGE)
                                                                           .withType("dockerimage");
        ExtendedMachineDto machine = newDto(ExtendedMachineDto.class).withAgents(singletonList("org.eclipse.che.ws-agent"))
                                                                     .withAttributes(singletonMap("memoryLimitBytes", MEMORY_LIMIT_BYTES));

        // setup environment
        EnvironmentDto environmentDto = newDto(EnvironmentDto.class).withRecipe(recipeDto)
                                                                    .withMachines(singletonMap(MACHINE_NAME, machine));
        // setup environment
        WorkspaceConfigDto expectedWsConfig = newDto(WorkspaceConfigDto.class)
                .withDefaultEnv("foo")
                .withEnvironments(singletonMap("foo", environmentDto))
                .withName("dumm");

        WorkspaceConfigDto actualWsConfigDto = urlFactoryBuilder.buildWorkspaceConfig("foo", "dumm", null);

        assertEquals(actualWsConfigDto, expectedWsConfig);
    }


    /**
     * Check that by specifying a location of custom dockerfile it's stored in the machine source if URL is accessible
     */
    @Test
    public void checkWithCustomDockerfile() throws Exception {

        String myLocation = "http://foo-location";
        EnvironmentRecipeDto recipeDto = newDto(EnvironmentRecipeDto.class).withLocation(myLocation)
                                                                           .withType("dockerfile")
                                                                           .withContentType("text/x-dockerfile");
        ExtendedMachineDto machine = newDto(ExtendedMachineDto.class).withAgents(singletonList("org.eclipse.che.ws-agent"))
                                                                     .withAttributes(singletonMap("memoryLimitBytes", MEMORY_LIMIT_BYTES));

        // setup environment
        EnvironmentDto environmentDto = newDto(EnvironmentDto.class).withRecipe(recipeDto)
                                                                    .withMachines(singletonMap(MACHINE_NAME, machine));

        WorkspaceConfigDto expectedWsConfig = newDto(WorkspaceConfigDto.class)
                .withDefaultEnv("foo")
                .withEnvironments(singletonMap("foo", environmentDto))
                .withName("dumm");

        when(URLChecker.exists(myLocation)).thenReturn(true);

        WorkspaceConfigDto actualWsConfigDto = urlFactoryBuilder.buildWorkspaceConfig("foo", "dumm", myLocation);

        assertEquals(actualWsConfigDto, expectedWsConfig);
    }

    /**
     * Check that by specifying a location of custom dockerfile it's stored in the machine source if URL is accessible
     */
    @Test
    public void checkWithNonAccessibleCustomDockerfile() throws Exception {

        String myLocation = "http://foo-location";
        EnvironmentRecipeDto recipeDto = newDto(EnvironmentRecipeDto.class).withLocation(DEFAULT_DOCKER_IMAGE)
                                                                           .withType("dockerimage");
        ExtendedMachineDto machine = newDto(ExtendedMachineDto.class).withAgents(singletonList("org.eclipse.che.ws-agent"))
                                                                     .withAttributes(singletonMap("memoryLimitBytes", MEMORY_LIMIT_BYTES));

        // setup environment
        EnvironmentDto environmentDto = newDto(EnvironmentDto.class).withRecipe(recipeDto)
                                                                    .withMachines(singletonMap(MACHINE_NAME, machine));

        WorkspaceConfigDto expectedWsConfig = newDto(WorkspaceConfigDto.class)
                .withDefaultEnv("foo")
                .withEnvironments(singletonMap("foo", environmentDto))
                .withName("dumm");

        when(URLChecker.exists(myLocation)).thenReturn(false);

        WorkspaceConfigDto actualWsConfigDto = urlFactoryBuilder.buildWorkspaceConfig("foo", "dumm", myLocation);

        assertEquals(actualWsConfigDto, expectedWsConfig);
    }

    /**
     * Check that with a custom factory.json we've this factory being built
     */
    @Test
    public void checkWithCustomFactoryJsonFile() throws Exception {

        when(factoryAdapter.adapt(any())).thenAnswer(inv -> inv.getArguments()[0]);

        WorkspaceConfigDto workspaceConfigDto = newDto(WorkspaceConfigDto.class);
        FactoryDto templateFactory = newDto(FactoryDto.class).withV("4.0").withName("florent").withWorkspace(workspaceConfigDto);
        String jsonFactory = DtoFactory.getInstance().toJson(templateFactory);


        String myLocation = "http://foo-location";
        when(URLChecker.exists(myLocation)).thenReturn(FALSE);
        when(URLFetcher.fetch(myLocation)).thenReturn(jsonFactory);

        FactoryDto factory = urlFactoryBuilder.createFactory(myLocation);

        assertEquals(templateFactory, factory);

    }


    /**
     * Check that without specifying a custom factory.json we've default factory
     */
    @Test
    public void checkWithDefaultFactoryJsonFile() throws Exception {

        FactoryDto factory = urlFactoryBuilder.createFactory(null);

        assertNull(factory.getWorkspace());
        assertEquals(factory.getV(), "4.0");

    }
}
