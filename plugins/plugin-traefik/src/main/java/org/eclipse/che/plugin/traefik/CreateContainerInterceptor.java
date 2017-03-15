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
package org.eclipse.che.plugin.traefik;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.json.ContainerConfig;
import org.eclipse.che.plugin.docker.client.json.ImageInfo;
import org.eclipse.che.plugin.docker.client.params.CreateContainerParams;
import org.eclipse.che.plugin.docker.client.params.InspectImageParams;
import org.eclipse.che.plugin.docker.machine.CustomServerEvaluationStrategy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Intercept the calls on createContainer on docker Connector.
 * @author Florent Benoit
 */
public class CreateContainerInterceptor implements MethodInterceptor {

    /**
     * Inject the custom docker strategy
     */
    private CustomServerEvaluationStrategy customServerEvaluationStrategy;

    /**
     * Regexp to extract port (under the form 22/tcp or 4401/tcp, etc.) from references
     */
    public static final String LABEL_CHE_SERVER_REF_KEY = "^che:server:(.*):ref$";

    /**
     * Grab labels of the config and from image to get all exposed ports and the labels defined if any
     * @param methodInvocation intercepting data of createContainer method on {@link DockerConnector}
     * @return the result of the intercepted method
     * @throws Throwable if there is an exception
     */
    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        // Get the connector
        DockerConnector dockerConnector =  (DockerConnector)methodInvocation.getThis();

        // only one parameter which is CreateContainerParams
        CreateContainerParams createContainerParams = (CreateContainerParams)methodInvocation.getArguments()[0];

        // Grab container configuration
        ContainerConfig containerConfig = createContainerParams.getContainerConfig();
        String image = containerConfig.getImage();

        // first, get labels defined in the container configuration
        Map<String, String> containerLabels = containerConfig.getLabels();

        // Also, get labels from the image itself
        final ImageInfo imageInfo = dockerConnector.inspectImage(InspectImageParams.create(image));
        Map<String, String> imageLabels = imageInfo.getConfig().getLabels();

        // Now merge all labels
        final Map<String,String> allLabels = new HashMap<>(containerLabels);
        allLabels.putAll(imageLabels);

        // Get all ports exposed by the container and by the image
        // it is under the form "22/tcp"
        final Set<String> allExposedPorts = ImmutableSet.<String>builder().addAll(containerConfig.getExposedPorts().keySet())
                                                                 .addAll(imageInfo.getConfig().getExposedPorts().keySet())
                                                                 .build();
        final String[] allEnv = Stream.concat(Arrays.stream(containerConfig.getEnv()), Arrays.stream(imageInfo.getConfig().getEnv()))
                                      .toArray(String[]::new);


        CustomServerEvaluationStrategy.RenderingEvaluation renderingEvaluation = customServerEvaluationStrategy.getOfflineRenderingEvaluation(allLabels, allExposedPorts, allEnv);

        // portValue is under format <port-number>/<tcp>
        allExposedPorts.forEach((portValue) -> {

            final String serviceName = renderingEvaluation.render("service-<serverName>", portValue);
            final String port = portValue.split("/")[0];

            final String host = renderingEvaluation.render("Host:<serverName>.<machineName>.<workspaceId>.<wildcardNipDomain>", portValue);
            containerLabels.put(String.format("traefik.%s.port", serviceName), port);
            containerLabels.put(String.format("traefik.%s.frontend.entryPoints", serviceName), "http");
            containerLabels.put(String.format("traefik.%s.frontend.rule", serviceName), host);
        });

        return methodInvocation.proceed();
    }


    @Inject
    protected void setCustomServerEvaluationStrategy(CustomServerEvaluationStrategy customServerEvaluationStrategy) {
        this.customServerEvaluationStrategy = customServerEvaluationStrategy;
    }


}
