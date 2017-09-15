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
package org.eclipse.che.workspace.infrastructure.openshift.environment;

import static java.lang.String.format;
import static org.eclipse.che.workspace.infrastructure.openshift.Constants.CHE_POD_NAME_LABEL;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.client.OpenShiftClient;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.workspace.server.model.impl.WarningImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.InternalEnvironment.InternalRecipe;
import org.eclipse.che.api.workspace.server.spi.InternalMachineConfig;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientFactory;
import org.eclipse.che.workspace.infrastructure.openshift.ServerExposer;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment.Builder;

/**
 * Parses {@link InternalEnvironment} into {@link OpenShiftEnvironment}.
 *
 * <p>It is done in following way:
 *
 * <ul>
 *   <li>parses OpenShift objects that are specified in recipe;
 *   <li>edits original recipe objects for exposing servers that are configured for machines.
 * </ul>
 *
 * @author Sergii Leshchenko
 */
public class OpenShiftEnvironmentParser {

  static final int ROUTE_IGNORED_WARNING_CODE = 4100;
  static final String ROUTES_IGNORED_WARNING_MESSAGE =
      "Routes specified in OpenShift recipe are ignored. "
          + "To expose ports please define servers in machine configuration.";

  static final String DEFAULT_RESTART_POLICY = "Never";

  private final OpenShiftClientFactory clientFactory;

  @Inject
  public OpenShiftEnvironmentParser(OpenShiftClientFactory clientFactory) {
    this.clientFactory = clientFactory;
  }

  public OpenShiftEnvironment parse(InternalEnvironment environment)
      throws ValidationException, InfrastructureException {
    checkNotNull(environment, "Environment should not be null");
    InternalRecipe recipe = environment.getRecipe();
    checkNotNull(environment.getRecipe(), "Environment recipe should not be null");
    String content = recipe.getContent();
    checkNotNull(content, "Recipe content should not be null");
    String contentType = recipe.getContentType();
    checkNotNull(contentType, "Recipe content type should not be null");

    switch (contentType) {
      case "application/x-yaml":
      case "text/yaml":
      case "text/x-yaml":
        break;
      default:
        throw new ValidationException(
            "Provided environment recipe content type '"
                + contentType
                + "' is unsupported. Supported values are: "
                + "application/x-yaml, text/yaml, text/x-yaml");
    }

    // TODO Implement own validation for OpenShift recipes, because it is OK for OpenShift client to
    // load  list with services only, but in our case there should be at least one pod with
    // containers
    KubernetesList list;
    try (OpenShiftClient client = clientFactory.create()) {
      list = client.lists().load(new ByteArrayInputStream(content.getBytes())).get();
    }

    Map<String, Pod> pods = new HashMap<>();
    Map<String, Service> services = new HashMap<>();
    Map<String, PersistentVolumeClaim> pvcs = new HashMap<>();
    boolean isAnyRoutePresent = false;
    for (HasMetadata object : list.getItems()) {
      if (object instanceof DeploymentConfig) {
        throw new ValidationException("Supporting of deployment configs is not implemented yet.");
      } else if (object instanceof Pod) {
        Pod pod = (Pod) object;
        pods.put(pod.getMetadata().getName(), pod);
      } else if (object instanceof Service) {
        Service service = (Service) object;
        services.put(service.getMetadata().getName(), service);
      } else if (object instanceof Route) {
        isAnyRoutePresent = true;
      } else if (object instanceof PersistentVolumeClaim) {
        PersistentVolumeClaim pvc = (PersistentVolumeClaim) object;
        pvcs.put(pvc.getMetadata().getName(), pvc);
      } else {
        throw new ValidationException(
            format("Found unknown object type '%s'", object.getMetadata()));
      }
    }

    Builder openShiftEnvBuilder =
        OpenShiftEnvironment.builder()
            .setPods(pods)
            .setServices(services)
            .setPersistentVolumeClaims(pvcs);

    if (isAnyRoutePresent) {
      environment.addWarning(
          new WarningImpl(ROUTE_IGNORED_WARNING_CODE, ROUTES_IGNORED_WARNING_MESSAGE));
    }

    OpenShiftEnvironment openShiftEnv = openShiftEnvBuilder.build();

    normalizeEnvironment(openShiftEnv, environment);

    return openShiftEnv;
  }

  private void normalizeEnvironment(
      OpenShiftEnvironment openShiftEnvironment, InternalEnvironment environment)
      throws ValidationException {
    for (Pod podConfig : openShiftEnvironment.getPods().values()) {
      final String podName = podConfig.getMetadata().getName();
      getLabels(podConfig).put(CHE_POD_NAME_LABEL, podName);
      final PodSpec podSpec = podConfig.getSpec();
      rewriteRestartPolicy(podSpec, podName, environment);
      for (Container containerConfig : podSpec.getContainers()) {
        String machineName = podName + '/' + containerConfig.getName();
        InternalMachineConfig machineConfig = environment.getMachines().get(machineName);
        if (machineConfig != null && !machineConfig.getServers().isEmpty()) {
          ServerExposer serverExposer =
              new ServerExposer(machineName, containerConfig, openShiftEnvironment);
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

  private void rewriteRestartPolicy(PodSpec podSpec, String podName, InternalEnvironment env) {
    final String restartPolicy = podSpec.getRestartPolicy();

    if (restartPolicy != null && !DEFAULT_RESTART_POLICY.equalsIgnoreCase(restartPolicy)) {
      final String warnMsg =
          format(
              "Restart policy '%s' for pod '%s' is rewritten with %s",
              restartPolicy, podName, DEFAULT_RESTART_POLICY);
      env.addWarning(new WarningImpl(101, warnMsg));
    }
    podSpec.setRestartPolicy(DEFAULT_RESTART_POLICY);
  }

  private void checkNotNull(Object object, String errorMessage) throws ValidationException {
    if (object == null) {
      throw new ValidationException(errorMessage);
    }
  }
}
