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

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.client.OpenShiftClient;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.config.Recipe;
import org.eclipse.che.api.workspace.server.RecipeDownloader;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.openshift.OpenshiftClientFactory;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;

/**
 * @author Sergii Leshchenko
 */
public class OpenshiftEnvironmentParser {
    private final OpenshiftClientFactory clientFactory;
    private final RecipeDownloader       recipeDownloader;

    @Inject
    public OpenshiftEnvironmentParser(OpenshiftClientFactory clientFactory,
                                      RecipeDownloader recipeDownloader) {
        this.clientFactory = clientFactory;
        this.recipeDownloader = recipeDownloader;
    }

    public OpenshiftEnvironment parse(Environment environment) throws ValidationException,
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

        //TODO Implement own validation for openshift recipes, because it is OK for openshift client to load  list with services only, but in our case there should be at least one pod with containers
        KubernetesList list;
        try (OpenShiftClient client = clientFactory.create()) {
            list = client.lists().load(new ByteArrayInputStream(content.getBytes())).get();
        }

        OpenshiftEnvironment openshiftEnvironment = new OpenshiftEnvironment();
        for (HasMetadata object : list.getItems()) {
            if (object instanceof DeploymentConfig) {
//                environment.addDeploymentConfig((DeploymentConfig)object);
                throw new ValidationException("Supporting of deployment configs is not implemented yet.");
            } else if (object instanceof Pod) {
                openshiftEnvironment.addPod((Pod)object);
            } else if (object instanceof Service) {
                openshiftEnvironment.addService((Service)object);
            } else if (object instanceof Route) {
                openshiftEnvironment.addRoute((Route)object);
            } else {
                throw new ValidationException(String.format("Found unknown object type '%s'", object.getMetadata()));
            }
        }

        return openshiftEnvironment;
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
