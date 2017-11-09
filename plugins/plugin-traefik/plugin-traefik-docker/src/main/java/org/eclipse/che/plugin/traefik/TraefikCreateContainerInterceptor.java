/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.traefik;

import static java.lang.String.format;

import com.google.common.collect.ImmutableSet;
import com.google.inject.name.Named;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import javax.inject.Inject;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.json.ContainerConfig;
import org.eclipse.che.plugin.docker.client.json.ImageInfo;
import org.eclipse.che.plugin.docker.client.params.CreateContainerParams;
import org.eclipse.che.plugin.docker.client.params.InspectImageParams;
import org.eclipse.che.plugin.docker.machine.CustomServerEvaluationStrategy;
import org.eclipse.che.plugin.docker.machine.ServerEvaluationStrategy;
import org.eclipse.che.plugin.docker.machine.ServerEvaluationStrategyProvider;

/**
 * Traefik has a listener on Docker containers. Each time a container is added or removed, it checks
 * if the container has specific Traefik Labels and then create routes based upon these labels. The
 * job of this interceptor is to add Traefik labels prior the start of the container (it will be
 * done when we create the container) by adding Traefik labels. The routes are built using the
 * custom strategy template.
 *
 * @author Florent Benoit
 */
public class TraefikCreateContainerInterceptor implements MethodInterceptor {

  /** Inject the server evaluation strategy provider. */
  private ServerEvaluationStrategyProvider serverEvaluationStrategyProvider;

  /** Template. */
  private String template;

  /**
   * Grab labels of the config and from image to get all exposed ports and the labels defined if any
   *
   * @param methodInvocation intercepting data of createContainer method on {@link DockerConnector}
   * @return the result of the intercepted method
   * @throws Throwable if there is an exception
   */
  @Override
  public Object invoke(MethodInvocation methodInvocation) throws Throwable {
    ServerEvaluationStrategy serverEvaluationStrategy = serverEvaluationStrategyProvider.get();
    // Abort if custom server evaluation strategy is not enabled.
    if (!(CustomServerEvaluationStrategy.class.isInstance(serverEvaluationStrategy))) {
      return methodInvocation.proceed();
    }
    final CustomServerEvaluationStrategy customServerEvaluationStrategy =
        (CustomServerEvaluationStrategy) serverEvaluationStrategy;

    // Get the connector
    DockerConnector dockerConnector = (DockerConnector) methodInvocation.getThis();

    // only one parameter which is CreateContainerParams
    CreateContainerParams createContainerParams =
        (CreateContainerParams) methodInvocation.getArguments()[0];

    // Grab container configuration
    ContainerConfig containerConfig = createContainerParams.getContainerConfig();
    String image = containerConfig.getImage();

    // first, get labels defined in the container configuration
    Map<String, String> containerLabels = containerConfig.getLabels();

    // Also, get labels from the image itself
    final ImageInfo imageInfo = dockerConnector.inspectImage(InspectImageParams.create(image));
    Map<String, String> imageLabels = imageInfo.getConfig().getLabels();

    // Now merge all labels
    final Map<String, String> allLabels = new HashMap<>(containerLabels);
    if (imageLabels != null) {
      // If image has some labels, merge them
      allLabels.putAll(imageLabels);
    }

    // Get all ports exposed by the container and by the image
    // it is under the form "22/tcp"
    final Set<String> allExposedPorts =
        ImmutableSet.<String>builder()
            .addAll(containerConfig.getExposedPorts().keySet())
            .addAll(imageInfo.getConfig().getExposedPorts().keySet())
            .build();
    final String[] allEnv =
        Stream.concat(
                Arrays.stream(containerConfig.getEnv()),
                Arrays.stream(imageInfo.getConfig().getEnv()))
            .toArray(String[]::new);

    CustomServerEvaluationStrategy.RenderingEvaluation renderingEvaluation =
        customServerEvaluationStrategy.getOfflineRenderingEvaluation(
            allLabels, allExposedPorts, allEnv);

    // portValue is under format <port-number>/<tcp>
    allExposedPorts.forEach(
        (portValue) -> {
          final String serviceName = renderingEvaluation.render("service-<serverName>", portValue);
          final String port = portValue.split("/")[0];

          String hostnameAndPort = renderingEvaluation.render(this.template, portValue);

          // extract only host from host:port
          String[] elements = hostnameAndPort.split(":");
          String hostName = elements[0];
          final String host = format("Host:%s", hostName);
          containerLabels.put(format("traefik.%s.port", serviceName), port);
          containerLabels.put(format("traefik.%s.frontend.entryPoints", serviceName), "http");
          containerLabels.put(format("traefik.%s.frontend.rule", serviceName), host);
          containerLabels.put("traefik.frontend.rule", createContainerParams.getContainerName());
        });

    return methodInvocation.proceed();
  }

  /**
   * Sets the server evaluation provider
   *
   * @param serverEvaluationStrategyProvider
   */
  @Inject
  protected void setServerEvaluationStrategyProvider(
      ServerEvaluationStrategyProvider serverEvaluationStrategyProvider) {
    this.serverEvaluationStrategyProvider = serverEvaluationStrategyProvider;
  }

  /**
   * Sets the template of server evaluation strategy
   *
   * @param cheDockerCustomExternalTemplate
   */
  @Inject
  protected void setTemplate(
      @Nullable @Named("che.docker.server_evaluation_strategy.custom.template")
          String cheDockerCustomExternalTemplate) {
    this.template = cheDockerCustomExternalTemplate;
  }
}
