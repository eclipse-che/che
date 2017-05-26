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
package org.eclipse.che.workspace.infrastructure.docker.environment;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.RecipeDownloader;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.workspace.infrastructure.docker.ContainerNameGenerator;
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
    private final long                   defaultMachineMemorySizeBytes;

    @Inject
    public EnvironmentNormalizer(RecipeDownloader recipeDownloader,
                                 @Named("che.api") String apiEndpoint,
                                 ContainerNameGenerator containerNameGenerator,
                                 @Named("che.workspace.default_memory_mb") long defaultMachineMemorySizeMB) {
        this.recipeDownloader = recipeDownloader;
        this.recipeApiPattern = Pattern.compile("(^https?" +
                                                apiEndpoint.substring(apiEndpoint.indexOf(":")) +
                                                "/recipe/.*$)|(^/recipe/.*$)");
        this.containerNameGenerator = containerNameGenerator;
        this.defaultMachineMemorySizeBytes = defaultMachineMemorySizeMB * 1_024 * 1_024;
    }

    public void normalize(Environment environment, DockerEnvironment dockerEnvironment, RuntimeIdentity identity)
            throws InfrastructureException {
        String networkId = NameGenerator.generate(identity.getWorkspaceId() + "_", 16);
        dockerEnvironment.setNetwork(networkId);

        Map<String, DockerContainerConfig> services = dockerEnvironment.getServices();
        for (Map.Entry<String, DockerContainerConfig> serviceEntry : services.entrySet()) {
            normalize(identity.getOwner(), identity.getWorkspaceId(), serviceEntry.getKey(), serviceEntry.getValue());
        }
        normalizeNames(dockerEnvironment);
    }

    private void normalizeNames(DockerEnvironment dockerEnvironment) throws InfrastructureException {
        Map<String, DockerContainerConfig> services = dockerEnvironment.getServices();
        for (Map.Entry<String, DockerContainerConfig> serviceEntry : services.entrySet()) {
            DockerContainerConfig service = serviceEntry.getValue();
            normalizeVolumesFrom(service, services);
            normalizeLinks(service, services);
        }
    }

    // replace machines names in volumes_from with containers IDs
    private void normalizeVolumesFrom(DockerContainerConfig service, Map<String, DockerContainerConfig> services) {
        if (service.getVolumesFrom() != null) {
            service.setVolumesFrom(service.getVolumesFrom()
                                          .stream()
                                          .map(serviceName -> services.get(serviceName).getContainerName())
                                          .collect(toList()));
        }
    }

    /**
     * Replaces linked to this service's name with container name which represents the service in links section.
     * The problem is that a user writes names of other services in links section in compose file.
     * But actually links are constraints and their values should be names of containers (not services) to be linked.
     * <br/>
     * For example: serviceDB:serviceDbAlias -> container_1234:serviceDbAlias <br/>
     * If alias is omitted then service name will be used.
     *
     * @param serviceToNormalizeLinks
     *         service which links will be normalized
     * @param services
     *         all services in environment
     */
    private void normalizeLinks(DockerContainerConfig serviceToNormalizeLinks, Map<String, DockerContainerConfig> services)
            throws InfrastructureException {
        List<String> normalizedLinks = new ArrayList<>();
        for (String link : serviceToNormalizeLinks.getLinks()) {
            // a link has format: 'name:alias' or 'name'
            String serviceNameAndAliasToLink[] = link.split(":", 2);
            String serviceName = serviceNameAndAliasToLink[0];
            String serviceAlias = (serviceNameAndAliasToLink.length > 1) ?
                                  serviceNameAndAliasToLink[1] : null;
            DockerContainerConfig serviceLinkTo = services.get(serviceName);
            if (serviceLinkTo != null) {
                String containerNameLinkTo = serviceLinkTo.getContainerName();
                normalizedLinks.add((serviceAlias == null) ?
                                    containerNameLinkTo :
                                    containerNameLinkTo + ':' + serviceAlias);
            } else {
                // should never happens. Errors like this should be filtered by CheEnvironmentValidator
                throw new InfrastructureException("Attempt to link non existing service " + serviceName +
                                                  " to " + serviceToNormalizeLinks + " service.");
            }
        }
        serviceToNormalizeLinks.setLinks(normalizedLinks);
    }

    private void normalize(String namespace,
                           String workspaceId,
                           String machineName,
                           DockerContainerConfig service) throws InfrastructureException {
        // set default mem limit for service if it is not set
        if (service.getMemLimit() == null || service.getMemLimit() == 0) {
            service.setMemLimit(defaultMachineMemorySizeBytes);
        }
        // download dockerfile if it is hosted by API to avoid problems with unauthorized requests from docker daemon
        if (service.getBuild() != null &&
            service.getBuild().getContext() != null &&
            recipeApiPattern.matcher(service.getBuild().getContext()).matches()) {

            String recipeContent;
            try {
                recipeContent = recipeDownloader.getRecipe(service.getBuild().getContext());
            } catch (ServerException e) {
                throw new InfrastructureException(e.getLocalizedMessage(), e);
            }
            service.getBuild().setDockerfileContent(recipeContent);
            service.getBuild().setContext(null);
            service.getBuild().setDockerfilePath(null);
        }
        if (service.getId() == null) {
            service.setId(generateMachineId());
        }

        service.setContainerName(containerNameGenerator.generateContainerName(workspaceId,
                                                                              service.getId(),
                                                                              namespace,
                                                                              machineName));
    }

    private String generateMachineId() {
        return NameGenerator.generate("machine", 16);
    }
}
