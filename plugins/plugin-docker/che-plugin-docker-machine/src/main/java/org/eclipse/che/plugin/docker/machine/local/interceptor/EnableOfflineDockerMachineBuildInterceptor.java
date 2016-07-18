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
package org.eclipse.che.plugin.docker.machine.local.interceptor;

import com.google.common.base.MoreObjects;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.Recipe;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.util.RecipeRetriever;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.DockerFileException;
import org.eclipse.che.plugin.docker.client.Dockerfile;
import org.eclipse.che.plugin.docker.client.ProgressMonitor;
import org.eclipse.che.plugin.docker.client.UserSpecificDockerRegistryCredentialsProvider;
import org.eclipse.che.plugin.docker.client.params.PullParams;
import org.eclipse.che.plugin.docker.client.parser.DockerImageIdentifier;
import org.eclipse.che.plugin.docker.client.parser.DockerImageIdentifierParser;
import org.eclipse.che.plugin.docker.machine.DockerInstanceProvider;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;

/**
 * Allow to build docker machine if base image of machine recipe cached but network is gone.
 *
 * @author Alexander Garagatyi
 *
 * @see DockerInstanceProvider#buildImage(MachineConfig, String, boolean, ProgressMonitor)
 */
public class EnableOfflineDockerMachineBuildInterceptor implements MethodInterceptor {
    @Inject
    DockerConnector                               dockerConnector;
    @Inject
    UserSpecificDockerRegistryCredentialsProvider dockerCredentials;
    @Inject
    RecipeRetriever                               recipeRetriever;
    @Inject
    @Named("machine.docker.pull_image")
    boolean                                       doForcePullOnBuild;

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        // If force pull of base image is not disabled ensure that image build won't fail if needed layers are cached
        // but update of them fails due to network outage.
        // To do that do pull here if needed and make DockerInstanceProvider to not do force pull itself.
        if (doForcePullOnBuild) {
            MachineConfig machineConfig = (MachineConfig)methodInvocation.getArguments()[0];
            ProgressMonitor progressMonitor = (ProgressMonitor)methodInvocation.getArguments()[3];

            try {
                pullImage(machineConfig, progressMonitor);
            } catch (IOException | DockerFileException ignored) {
            }
        }

        // set force pulling flag to false
        methodInvocation.getArguments()[2] = Boolean.FALSE;
        return methodInvocation.proceed();
    }

    private void pullImage(MachineConfig machineConfig, ProgressMonitor progressMonitor)
            throws DockerFileException, IOException, MachineException {

        Recipe recipe = recipeRetriever.getRecipe(machineConfig);
        Dockerfile dockerfile = DockerInstanceProvider.parseRecipe(recipe);
        DockerImageIdentifier imageIdentifier = DockerImageIdentifierParser.parse(dockerfile.getImages().get(0).getFrom());

        dockerConnector.pull(PullParams.create(imageIdentifier.getRepository())
                                       .withTag(MoreObjects.firstNonNull(imageIdentifier.getTag(), "latest"))
                                       .withRegistry(imageIdentifier.getRegistry())
                                       .withAuthConfigs(dockerCredentials.getCredentials()),
                             progressMonitor);
    }
}
