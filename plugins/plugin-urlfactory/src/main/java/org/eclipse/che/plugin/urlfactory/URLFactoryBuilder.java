/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.urlfactory;

import com.google.common.base.Strings;
import com.google.common.io.CharStreams;

import org.eclipse.che.api.factory.server.FactoryMessageBodyAdapter;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentRecipeDto;
import org.eclipse.che.api.workspace.shared.dto.ExtendedMachineDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.dto.server.DtoFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Handle the creation of some elements used inside a {@link FactoryDto}
 *
 * @author Florent Benoit
 */
@Singleton
public class URLFactoryBuilder {

    /**
     * Default docker image (if repository has no dockerfile)
     */
    protected static final String DEFAULT_DOCKER_IMAGE = "codenvy/ubuntu_jdk8";

    /**
     * Default docker type (if repository has no dockerfile)
     */
    protected static final String MEMORY_LIMIT_BYTES  = Long.toString(2000L * 1024L * 1024L);
    protected static final String MACHINE_NAME        = "ws-machine";

    /**
     * Check if URL is existing or not
     */
    @Inject
    private URLChecker URLChecker;

    /**
     * Grab content of URLs
     */
    @Inject
    private URLFetcher URLFetcher;

    @Inject
    private FactoryMessageBodyAdapter factoryAdapter;

    /**
     * Build a default factory using the provided json file or create default one
     *
     * @param jsonFileLocation
     *         location of factory json file
     * @return a factory
     */
    public FactoryDto createFactory(String jsonFileLocation) {

        // Check if there is factory json file inside the repository
        if (jsonFileLocation != null) {
            String factoryJsonContent = URLFetcher.fetch(jsonFileLocation);
            if (!Strings.isNullOrEmpty(factoryJsonContent)) {
                // Adapt an old factory format to a new one if necessary
                try {
                    final ByteArrayInputStream contentStream = new ByteArrayInputStream(factoryJsonContent.getBytes(UTF_8));
                    final InputStream newStream = factoryAdapter.adapt(contentStream);
                    factoryJsonContent = CharStreams.toString(new InputStreamReader(newStream, UTF_8));
                } catch (IOException x) {
                    throw new IllegalStateException(x.getLocalizedMessage(), x);
                }
                return DtoFactory.getInstance().createDtoFromJson(factoryJsonContent, FactoryDto.class);
            }
        }

        // else return a default factory
        return newDto(FactoryDto.class).withV("4.0");
    }


    /**
     * Help to generate default workspace configuration
     *
     * @param environmentName
     *         the name of the environment to create
     * @param name
     *         the name of the workspace
     * @param dockerFileLocation
     *         the optional location for codenvy dockerfile to use
     * @return a workspace configuration
     */
    public WorkspaceConfigDto buildWorkspaceConfig(String environmentName,
                                                   String name,
                                                   String dockerFileLocation) {

        // if remote repository contains a codenvy docker file, use it
        // else use the default image.
        EnvironmentRecipeDto recipeDto;
        if (dockerFileLocation != null && URLChecker.exists(dockerFileLocation)) {
            recipeDto = newDto(EnvironmentRecipeDto.class).withLocation(dockerFileLocation)
                                                          .withType("dockerfile")
                                                          .withContentType("text/x-dockerfile");
        } else {
            recipeDto = newDto(EnvironmentRecipeDto.class).withLocation(DEFAULT_DOCKER_IMAGE)
                                                          .withType("dockerimage");
        }
        ExtendedMachineDto machine = newDto(ExtendedMachineDto.class).withAgents(singletonList("org.eclipse.che.ws-agent"))
                                                                     .withAttributes(singletonMap("memoryLimitBytes", MEMORY_LIMIT_BYTES));

        // setup environment
        EnvironmentDto environmentDto = newDto(EnvironmentDto.class).withRecipe(recipeDto)
                                                                    .withMachines(singletonMap(MACHINE_NAME, machine));

        // workspace configuration using the environment
        return newDto(WorkspaceConfigDto.class)
                .withDefaultEnv(environmentName)
                .withEnvironments(singletonMap(environmentName, environmentDto))
                .withName(name);
    }
}
