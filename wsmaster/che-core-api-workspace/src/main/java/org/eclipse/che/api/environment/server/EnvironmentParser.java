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
package org.eclipse.che.api.environment.server;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.core.model.workspace.EnvironmentRecipe;
import org.eclipse.che.api.core.model.workspace.ExtendedMachine;
import org.eclipse.che.api.environment.server.compose.ComposeEnvironmentImpl;
import org.eclipse.che.api.environment.server.compose.ComposeFileParser;
import org.eclipse.che.api.environment.server.compose.ComposeServiceImpl;
import org.eclipse.che.api.environment.server.model.CheServiceBuildContextImpl;
import org.eclipse.che.api.environment.server.model.CheServiceImpl;
import org.eclipse.che.api.environment.server.model.CheServicesEnvironmentImpl;
import org.eclipse.che.api.machine.server.util.RecipeDownloader;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

/**
 * Parses {@link Environment} into {@link CheServicesEnvironmentImpl}.
 *
 * @author Alexander Garagatyi
 */
public class EnvironmentParser {
    private static final List<String> types = Arrays.asList("compose", "dockerimage", "dockerfile");

    // TODO move to container related code
    protected static final String SERVER_CONF_LABEL_PREFIX          = "che:server:";
    protected static final String SERVER_CONF_LABEL_REF_SUFFIX      = ":ref";
    protected static final String SERVER_CONF_LABEL_PROTOCOL_SUFFIX = ":protocol";
    protected static final String SERVER_CONF_LABEL_PATH_SUFFIX     = ":path";

    private final ComposeFileParser composeFileParser;
    private final RecipeDownloader  recipeDownloader;

    @Inject
    public EnvironmentParser(ComposeFileParser composeFileParser,
                             RecipeDownloader recipeDownloader) {
        this.composeFileParser = composeFileParser;
        this.recipeDownloader = recipeDownloader;
    }

    /**
     * Returns list of supported types of environments.
     */
    public List<String> getEnvironmentTypes() {
        return types;
    }

    /**
     * Parses {@link Environment} into {@link CheServicesEnvironmentImpl}.
     *
     * @param environment
     *         environment to parse
     * @return environment representation as compose environment
     * @throws IllegalArgumentException
     *         if provided environment is illegal
     * @throws ServerException
     *         if fetching of environment recipe content fails
     */
    public CheServicesEnvironmentImpl parse(Environment environment) throws IllegalArgumentException,
                                                                            ServerException {

        checkNotNull(environment, "Environment should not be null");
        checkNotNull(environment.getRecipe(), "Environment recipe should not be null");
        checkNotNull(environment.getRecipe().getType(), "Environment recipe type should not be null");
        checkArgument(environment.getRecipe().getContent() != null || environment.getRecipe().getLocation() != null,
                      "Recipe of environment must contain location or content");

        CheServicesEnvironmentImpl cheServicesEnvironment;
        String envType = environment.getRecipe().getType();
        switch (envType) {
            case "compose":
                cheServicesEnvironment = parseCompose(environment.getRecipe());
                break;
            case "dockerimage":
            case "dockerfile":
                cheServicesEnvironment = parseDocker(environment);
                break;
            default:
                throw new IllegalArgumentException(format("Environment type '%s' is not supported. " +
                                                          "Supported environment types: %s",
                                                          envType,
                                                          Joiner.on(", ").join(types)));
        }

        cheServicesEnvironment.getServices().forEach((name, service) -> {
            ExtendedMachine extendedMachine = environment.getMachines().get(name);
            if (extendedMachine != null) {
                normalizeMachine(name, service, extendedMachine);
            }
        });

        return cheServicesEnvironment;
    }

    private void normalizeMachine(String name, CheServiceImpl service, ExtendedMachine extendedMachine) {
        if (extendedMachine.getAttributes().containsKey("memoryLimitBytes")) {

            try {
                service.setMemLimit(Long.parseLong(extendedMachine.getAttributes().get("memoryLimitBytes")));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        format("Value of attribute 'memoryLimitBytes' of machine '%s' is illegal", name));
            }
        }
        service.setExpose(service.getExpose()
                                 .stream()
                                 .map(expose -> expose.contains("/") ?
                                                expose :
                                                expose + "/tcp")
                                 .collect(toList()));
        extendedMachine.getServers().forEach((serverRef, serverConf) -> {
            String normalizedPort = serverConf.getPort().contains("/") ?
                                    serverConf.getPort() :
                                    serverConf.getPort() + "/tcp";

            service.getExpose().add(normalizedPort);

            String portLabelPrefix = SERVER_CONF_LABEL_PREFIX + normalizedPort;

            service.getLabels().put(portLabelPrefix +
                                    SERVER_CONF_LABEL_REF_SUFFIX,
                                    serverRef);
            if (serverConf.getProperties() != null && serverConf.getProperties().get("path") != null) {

                service.getLabels().put(portLabelPrefix +
                                        SERVER_CONF_LABEL_PATH_SUFFIX,
                                        serverConf.getProperties().get("path"));
            }
            if (serverConf.getProtocol() != null) {
                service.getLabels().put(portLabelPrefix +
                                        SERVER_CONF_LABEL_PROTOCOL_SUFFIX,
                                        serverConf.getProtocol());
            }
        });
    }

    private CheServicesEnvironmentImpl parseCompose(EnvironmentRecipe recipe) throws ServerException {
        String recipeContent = getContentOfRecipe(recipe);

        ComposeEnvironmentImpl composeEnvironment = composeFileParser.parse(recipeContent, recipe.getContentType());

        return asCheEnvironment(composeEnvironment);
    }

    private CheServicesEnvironmentImpl asCheEnvironment(ComposeEnvironmentImpl composeEnvironment) {
        Map<String, CheServiceImpl> services = Maps.newHashMapWithExpectedSize(composeEnvironment.getServices().size());
        for (Map.Entry<String, ComposeServiceImpl> composeServiceEntry : composeEnvironment.getServices()
                                                                                           .entrySet()) {
            ComposeServiceImpl service = composeServiceEntry.getValue();

            CheServiceBuildContextImpl buildContext = null;
            if (service.getBuild() != null) {
                buildContext = new CheServiceBuildContextImpl().withContext(service.getBuild().getContext())
                                                               .withDockerfilePath(service.getBuild().getDockerfile());
            }

            CheServiceImpl cheService = new CheServiceImpl().withBuild(buildContext)
                                                            .withCommand(service.getCommand())
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

            services.put(composeServiceEntry.getKey(), cheService);
        }
        return new CheServicesEnvironmentImpl().withServices(services);
    }

    private CheServicesEnvironmentImpl parseDocker(Environment environment) {
        checkArgument(environment.getMachines().size() == 1,
                      "Environment of type '%s' doesn't support multiple machines, but contains machines: %s",
                      environment.getRecipe().getType(),
                      Joiner.on(", ").join(environment.getMachines().keySet()));

        CheServicesEnvironmentImpl composeEnvironment = new CheServicesEnvironmentImpl();
        CheServiceImpl service = new CheServiceImpl();

        composeEnvironment.getServices().put(environment.getMachines()
                                                        .keySet()
                                                        .iterator()
                                                        .next(), service);

        EnvironmentRecipe recipe = environment.getRecipe();

        if ("dockerimage".equals(environment.getRecipe().getType())) {
            service.setImage(recipe.getLocation());
        } else {
            if (!"text/x-dockerfile".equals(recipe.getContentType())) {
                throw new IllegalArgumentException(format("Content type '%s' of recipe of environment is unsupported." +
                                                          " Supported values are: text/x-dockerfile",
                                                          recipe.getContentType()));
            }

            if (recipe.getLocation() != null) {
                service.setBuild(new CheServiceBuildContextImpl().withContext(recipe.getLocation()));
            } else {
                service.setBuild(new CheServiceBuildContextImpl().withDockerfileContent(recipe.getContent()));
            }
        }

        return composeEnvironment;
    }

    private String getContentOfRecipe(EnvironmentRecipe environmentRecipe) throws ServerException {
        if (environmentRecipe.getContent() != null) {
            return environmentRecipe.getContent();
        } else {
            return recipeDownloader.getRecipe(environmentRecipe.getLocation());
        }
    }

    /**
     * Checks that object reference is not null, throws {@link IllegalArgumentException} otherwise.
     *
     * <p>Exception uses error message built from error message template and error message parameters.
     */
    private static void checkNotNull(Object object, String errorMessageTemplate, Object... errorMessageParams) {
        if (object == null) {
            throw new IllegalArgumentException(format(errorMessageTemplate, errorMessageParams));
        }
    }
}
