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
package org.eclipse.che.workspace.infrastructure.openshift.environment;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.client.OpenShiftClient;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.eclipse.che.api.core.model.workspace.config.Recipe;
import org.eclipse.che.api.workspace.server.RecipeDownloader;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientFactory;
import org.eclipse.che.workspace.infrastructure.openshift.ServerExposer;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import static org.eclipse.che.workspace.infrastructure.openshift.Constants.CHE_POD_NAME_LABEL;

/**
 * Parses {@link Environment} into {@link OpenShiftEnvironment}.
 *
 * <p>It is done in following way:
 * <ul>
 * <li>parses OpenShift objects that are specified in recipe;</li>
 * <li>edits original recipe objects for exposing servers that are configured for machines.</li>
 * </ul>
 *
 * @author Sergii Leshchenko
 */
public class OpenShiftEnvironmentParser {
    private final OpenShiftClientFactory clientFactory;
    private final RecipeDownloader       recipeDownloader;

    @Inject
    public OpenShiftEnvironmentParser(OpenShiftClientFactory clientFactory,
                                      RecipeDownloader recipeDownloader) {
        this.clientFactory = clientFactory;
        this.recipeDownloader = recipeDownloader;
    }

    public OpenShiftEnvironment parse(Environment environment) throws ValidationException,
                                                                      InfrastructureException {
        checkNotNull(environment, "Environment should not be null");
        Recipe recipe = environment.getRecipe();
        checkNotNull(environment.getRecipe(), "Environment recipe should not be null");

        String content = getContentOfRecipe(recipe);
        String contentType = recipe.getContentType();

        checkNotNull(contentType, "Recipe content type should not be null");
        checkNotNull(content, "Recipe content should not be null");

        switch (contentType) {
            case "application/x-yaml":
            case "text/yaml":
            case "text/x-yaml":
                break;
            default:
                throw new ValidationException("Provided environment recipe content type '" + contentType +
                                              "' is unsupported. Supported values are: " +
                                              "application/x-yaml, text/yaml, text/x-yaml");
        }

        //TODO Implement own validation for OpenShift recipes, because it is OK for OpenShift client to load  list with services only, but in our case there should be at least one pod with containers
        KubernetesList list;
        try (OpenShiftClient client = clientFactory.create()) {
            list = client.lists().load(new ByteArrayInputStream(content.getBytes())).get();
        }

        Map<String, Pod> pods = new HashMap<>();
        Map<String, Service> services = new HashMap<>();
        Map<String, Route> routes = new HashMap<>();
        for (HasMetadata object : list.getItems()) {
            if (object instanceof DeploymentConfig) {
                throw new ValidationException("Supporting of deployment configs is not implemented yet.");
            } else if (object instanceof Pod) {
                Pod pod = (Pod)object;
                pods.put(pod.getMetadata().getName(), pod);
            } else if (object instanceof Service) {
                Service service = (Service)object;
                services.put(service.getMetadata().getName(), service);
            } else if (object instanceof Route) {
                Route route = (Route)object;
                routes.put(route.getMetadata().getName(), route);
            } else {
                throw new ValidationException(String.format("Found unknown object type '%s'", object.getMetadata()));
            }
        }

        OpenShiftEnvironment openShiftEnvironment = new OpenShiftEnvironment().withPods(pods)
                                                                              .withServices(services)
                                                                              .withRoutes(routes);
        normalizeEnvironment(openShiftEnvironment, environment);

        return openShiftEnvironment;
    }

    private void normalizeEnvironment(OpenShiftEnvironment openShiftEnvironment,
                                      Environment environment) throws ValidationException {
        for (Pod podConfig : openShiftEnvironment.getPods().values()) {
            String podName = podConfig.getMetadata().getName();
            getLabels(podConfig).put(CHE_POD_NAME_LABEL, podName);

            for (Container containerConfig : podConfig.getSpec().getContainers()) {
                String machineName = podName + "/" + containerConfig.getName();
                MachineConfig machineConfig = environment.getMachines().get(machineName);
                if (machineConfig != null && !machineConfig.getServers().isEmpty()) {
                    ServerExposer serverExposer = new ServerExposer(machineName, containerConfig, openShiftEnvironment);
                    serverExposer.expose("servers", machineConfig.getServers());
                }
            }
        }
    }

    private Map<String, String> getLabels(Pod pod) {
        ObjectMeta metadata = pod.getMetadata();
        if (metadata == null) {
            metadata = new ObjectMeta();
            pod.setMetadata(metadata);
        }

        Map<String, String> labels = metadata.getLabels();
        if (labels == null) {
            labels = new HashMap<>();
            metadata.setLabels(labels);
        }
        return labels;
    }

    private String getContentOfRecipe(Recipe environmentRecipe) throws InfrastructureException {
        if (environmentRecipe.getContent() != null) {
            return environmentRecipe.getContent();
        } else {
            try {
                return recipeDownloader.getRecipe(environmentRecipe.getLocation());
            } catch (ServerException e) {
                throw new InfrastructureException(e.getLocalizedMessage(), e);
            }
        }
    }

    private void checkNotNull(Object object, String errorMessage) throws ValidationException {
        if (object == null) {
            throw new ValidationException(errorMessage);
        }
    }
}
