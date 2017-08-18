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
package org.eclipse.che.workspace.infrastructure.docker.environment;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.RecipeDownloader;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.workspace.infrastructure.docker.container.ContainerNameGenerator;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;

/**
 * @author Alexander Garagatyi
 */
public class EnvironmentNormalizer {
    private final RecipeDownloader       recipeDownloader;
    private final Pattern                recipeApiPattern;
    private final ContainerNameGenerator containerNameGenerator;

    @Inject
    public EnvironmentNormalizer(RecipeDownloader recipeDownloader,
                                 @Named("che.api") String apiEndpoint,
                                 ContainerNameGenerator containerNameGenerator) {
        this.recipeDownloader = recipeDownloader;
        this.recipeApiPattern = Pattern.compile("(^https?" +
                                                apiEndpoint.substring(apiEndpoint.indexOf(":")) +
                                                "/recipe/.*$)|(^/recipe/.*$)");
        this.containerNameGenerator = containerNameGenerator;
    }

    public void normalize(Environment environment, DockerEnvironment dockerEnvironment, RuntimeIdentity identity)
            throws InfrastructureException {
        String networkId = identity.getWorkspaceId() + "_" + identity.getEnvName();
        dockerEnvironment.setNetwork(networkId);

        Map<String, DockerContainerConfig> containers = dockerEnvironment.getContainers();
        for (Map.Entry<String, DockerContainerConfig> containerEntry : containers.entrySet()) {
            normalize(identity.getOwner(), identity.getWorkspaceId(), containerEntry.getKey(),
                      containerEntry.getValue());
        }
        normalizeNames(dockerEnvironment);
    }

    private void normalizeNames(DockerEnvironment dockerEnvironment) throws InfrastructureException {
        Map<String, DockerContainerConfig> containers = dockerEnvironment.getContainers();
        for (Map.Entry<String, DockerContainerConfig> containerEntry : containers.entrySet()) {
            DockerContainerConfig container = containerEntry.getValue();
            normalizeVolumesFrom(container, containers);
            normalizeLinks(container, containers);
        }
    }

    // replace machines names in volumes_from with containers IDs
    private void normalizeVolumesFrom(DockerContainerConfig container, Map<String, DockerContainerConfig> containers) {
        if (container.getVolumesFrom() != null) {
            container.setVolumesFrom(container.getVolumesFrom()
                                              .stream()
                                              .map(containerName -> containers.get(containerName).getContainerName())
                                              .collect(toList()));
        }
    }

    /**
     * Replaces linked to this container's name with container name which represents the container in links section.
     * The problem is that a user writes names of other services in links section in compose file.
     * But actually links are constraints and their values should be names of containers (not services) to be linked.
     * <br/>
     * For example: serviceDB:serviceDbAlias -> container_1234:serviceDbAlias <br/>
     * If alias is omitted then service name will be used.
     *
     * @param containerToNormalizeLinks
     *         container which links will be normalized
     * @param containers
     *         all containers in environment
     */
    private void normalizeLinks(DockerContainerConfig containerToNormalizeLinks,
                                Map<String, DockerContainerConfig> containers)
            throws InfrastructureException {
        List<String> normalizedLinks = new ArrayList<>();
        for (String link : containerToNormalizeLinks.getLinks()) {
            // a link has format: 'name:alias' or 'name'
            String containerNameAndAliasToLink[] = link.split(":", 2);
            String containerName = containerNameAndAliasToLink[0];
            String containerAlias = (containerNameAndAliasToLink.length > 1) ?
                                    containerNameAndAliasToLink[1] : null;
            DockerContainerConfig containerLinkTo = containers.get(containerName);
            if (containerLinkTo != null) {
                String containerNameLinkTo = containerLinkTo.getContainerName();
                normalizedLinks.add((containerAlias == null) ?
                                    containerNameLinkTo :
                                    containerNameLinkTo + ':' + containerAlias);
            } else {
                // should never happens. Errors like this should be filtered by CheEnvironmentValidator
                throw new InfrastructureException("Attempt to link non existing container " + containerName +
                                                  " to " + containerToNormalizeLinks + " container.");
            }
        }
        containerToNormalizeLinks.setLinks(normalizedLinks);
    }

    private void normalize(String namespace,
                           String workspaceId,
                           String machineName,
                           DockerContainerConfig container) throws InfrastructureException {

        // download dockerfile if it is hosted by API to avoid problems with unauthorized requests from docker daemon
        if (container.getBuild() != null &&
            container.getBuild().getContext() != null &&
            recipeApiPattern.matcher(container.getBuild().getContext()).matches()) {

            String recipeContent;
            try {
                recipeContent = recipeDownloader.getRecipe(container.getBuild().getContext());
            } catch (ServerException e) {
                throw new InfrastructureException(e.getLocalizedMessage(), e);
            }
            container.getBuild().setDockerfileContent(recipeContent);
            container.getBuild().setContext(null);
            container.getBuild().setDockerfilePath(null);
        }
        if (container.getId() == null) {
            container.setId(generateMachineId());
        }

        container.setContainerName(containerNameGenerator.generateContainerName(workspaceId,
                                                                                container.getId(),
                                                                                namespace,
                                                                                machineName));
    }

    private String generateMachineId() {
        return NameGenerator.generate("machine", 16);
    }
}
