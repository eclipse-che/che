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
package org.eclipse.che.workspace.infrastructure.docker.environment.compose;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.config.Recipe;
import org.eclipse.che.api.machine.server.util.RecipeDownloader;
import org.eclipse.che.workspace.infrastructure.docker.TypeSpecificEnvironmentParser;
import org.eclipse.che.workspace.infrastructure.docker.environment.compose.model.ComposeEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.environment.compose.model.ComposeService;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerBuildContext;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerService;

import java.io.IOException;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Converters compose file to {@link ComposeEnvironment} and vise versa or
 * converters compose {@link Environment} to {@link DockerEnvironment}.
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
     * Parses compose file from {@link Environment} into {@link DockerEnvironment}.
     *
     * @param environment
     *         environment with {@link Recipe} to parse.
     *         {@link Recipe} contains {@link DockerEnvironment} definition.
     * @throws IllegalArgumentException
     *         when environment or environment recipe is invalid
     * @throws ServerException
     *         when environment recipe can not be retrieved
     */
    @Override
    public DockerEnvironment parse(Environment environment) throws ServerException {
        requireNonNull(environment, "Environment should not be null");
        Recipe recipe = environment.getRecipe();
        requireNonNull(environment.getRecipe(), "Environment recipe should not be null");

        String content = getContentOfRecipe(recipe);
        ComposeEnvironment composeEnvironment = parse(content, recipe.getContentType());
        return asDockerEnvironment(composeEnvironment);
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

    private String getContentOfRecipe(Recipe environmentRecipe) throws ServerException {
        if (environmentRecipe.getContent() != null) {
            return environmentRecipe.getContent();
        } else {
            return recipeDownloader.getRecipe(environmentRecipe.getLocation());
        }
    }

    private DockerEnvironment asDockerEnvironment(ComposeEnvironment composeEnvironment) {
        Map<String, DockerService> services = Maps.newHashMapWithExpectedSize(composeEnvironment.getServices().size());
        for (Map.Entry<String, ComposeService> composeServiceEntry : composeEnvironment.getServices()
                                                                                       .entrySet()) {
            ComposeService service = composeServiceEntry.getValue();

            DockerService cheService = new DockerService().withCommand(service.getCommand())
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
                cheService.setBuild(new DockerBuildContext().withContext(service.getBuild().getContext())
                                                            .withDockerfilePath(service.getBuild().getDockerfile())
                                                            .withArgs(service.getBuild().getArgs()));
            }

            services.put(composeServiceEntry.getKey(), cheService);
        }
        return new DockerEnvironment(services);
    }
}
