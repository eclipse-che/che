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
package org.eclipse.che.plugin.docker.compose.yaml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.core.model.workspace.EnvironmentRecipe;
import org.eclipse.che.api.environment.server.TypeSpecificEnvironmentParser;
import org.eclipse.che.api.environment.server.model.CheServiceBuildContextImpl;
import org.eclipse.che.api.environment.server.model.CheServiceImpl;
import org.eclipse.che.api.environment.server.model.CheServicesEnvironmentImpl;
import org.eclipse.che.api.machine.server.util.RecipeDownloader;
import org.eclipse.che.plugin.docker.compose.ComposeEnvironment;
import org.eclipse.che.plugin.docker.compose.ComposeServiceImpl;

import java.io.IOException;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Converters compose file to {@link ComposeEnvironment} and vise versa or
 * converters compose {@link Environment} to {@link CheServicesEnvironmentImpl}.
 *
 * @author Alexander Garagatyi
 * @author Alexander Andrienko
 */
public class ComposeEnvironmentParser implements TypeSpecificEnvironmentParser {

    private static final ObjectMapper YAML_PARSER = new ObjectMapper(new YAMLFactory());

    private final RecipeDownloader recipeDownloader;

    @Inject
    public ComposeEnvironmentParser(RecipeDownloader recipeDownloader) {
        this.recipeDownloader = recipeDownloader;
    }

    /**
     * Parses compose file from {@link Environment} into {@link CheServicesEnvironmentImpl}.
     *
     * @param environment
     *         environment with {@link EnvironmentRecipe} to parse.
     *         {@link EnvironmentRecipe} contains {@link CheServicesEnvironmentImpl} definition.
     * @throws IllegalArgumentException
     *         when environment or environment recipe is invalid
     * @throws ServerException
     *         when environment recipe can not be retrieved
     */
    @Override
    public CheServicesEnvironmentImpl parse(Environment environment) throws ServerException {
        requireNonNull(environment, "Environment should not be null");
        EnvironmentRecipe recipe = environment.getRecipe();
        requireNonNull(environment.getRecipe(), "Environment recipe should not be null");

        String content = getContentOfRecipe(recipe);
        ComposeEnvironment composeEnvironment = parse(content, recipe.getContentType());
        return asCheEnvironment(composeEnvironment);
    }

    /**
     * Parses compose file into Docker Compose model.
     *
     * @param recipeContent
     *         compose file to parse
     * @throws IllegalArgumentException
     *         when environment or environment recipe is invalid
     * @throws ServerException
     *         when environment recipe can not be retrieved
     */
    public ComposeEnvironment parse(String recipeContent, String contentType) throws ServerException {
        requireNonNull(recipeContent, "Recipe content should not be null");
        requireNonNull(contentType, "Recipe content type should not be null");

        ComposeEnvironment composeEnvironment;
        switch (contentType) {
            case "application/x-yaml":
            case "text/yaml":
            case "text/x-yaml":
                try {
                    composeEnvironment = YAML_PARSER.readValue(recipeContent, ComposeEnvironment.class);
                } catch (IOException e) {
                    throw new IllegalArgumentException(
                            "Parsing of environment configuration failed. " + e.getLocalizedMessage());
                }
                break;
            default:
                throw new IllegalArgumentException("Provided environment recipe content type '" +
                                                   contentType +
                                                   "' is unsupported. Supported values are: " +
                                                   "application/x-yaml, text/yaml, text/x-yaml");
        }
        return composeEnvironment;
    }

    /**
     * Converts Docker Compose environment model into YAML file.
     *
     * @param composeEnvironment
     *         Docker Compose environment model file
     * @throws IllegalArgumentException
     *         when argument is null or conversion to YAML fails
     */
    public String toYaml(ComposeEnvironment composeEnvironment) {
        requireNonNull(composeEnvironment, "Compose environment should not be null");

        try {
            return YAML_PARSER.writeValueAsString(composeEnvironment);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e.getLocalizedMessage(), e);
        }
    }

    private String getContentOfRecipe(EnvironmentRecipe environmentRecipe) throws ServerException {
        if (environmentRecipe.getContent() != null) {
            return environmentRecipe.getContent();
        } else {
            return recipeDownloader.getRecipe(environmentRecipe.getLocation());
        }
    }

    private CheServicesEnvironmentImpl asCheEnvironment(ComposeEnvironment composeEnvironment) {
        Map<String, CheServiceImpl> services = Maps.newHashMapWithExpectedSize(composeEnvironment.getServices().size());
        for (Map.Entry<String, ComposeServiceImpl> composeServiceEntry : composeEnvironment.getServices()
                                                                                           .entrySet()) {
            ComposeServiceImpl service = composeServiceEntry.getValue();

            CheServiceImpl cheService = new CheServiceImpl().withCommand(service.getCommand())
                                                            .withContainerName(service.getContainerName())
                                                            .withDependsOn(service.getDependsOn())
                                                            .withEntrypoint(service.getEntrypoint())
                                                            .withEnvironment(service.getEnvironment())
                                                            .withExpose(service.getExpose())
                                                            .withImage(service.getImage())
                                                            .withLabels(service.getLabels())
                                                            .withLinks(service.getLinks())
                                                            .withMemLimit(service.getMemLimit())
                                                            .withNetworks(service.getNetworks())
                                                            .withPorts(service.getPorts())
                                                            .withVolumes(service.getVolumes())
                                                            .withVolumesFrom(service.getVolumesFrom());

            if (service.getBuild() != null) {
                cheService.setBuild(new CheServiceBuildContextImpl().withContext(service.getBuild().getContext())
                                                                    .withDockerfilePath(service.getBuild().getDockerfile())
                                                                    .withArgs(service.getBuild().getArgs()));
            }

            services.put(composeServiceEntry.getKey(), cheService);
        }
        return new CheServicesEnvironmentImpl(services);
    }
}
