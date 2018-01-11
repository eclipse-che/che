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
package org.eclipse.che.workspace.infrastructure.openshift.project;

import com.google.common.annotations.VisibleForTesting;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.api.model.Project;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.client.OpenShiftClient;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines an internal API for managing subset of objects inside {@link Project} instance.
 *
 * @author Sergii Leshchenko
 */
public class OpenShiftProject {

  private static final Logger LOG = LoggerFactory.getLogger(OpenShiftProject.class);

  private final String workspaceId;

  private final OpenShiftPods pods;
  private final OpenShiftServices services;
  private final OpenShiftRoutes routes;
  private final OpenShiftPersistentVolumeClaims pvcs;

  @VisibleForTesting
  OpenShiftProject(
      String workspaceId,
      OpenShiftPods pods,
      OpenShiftServices services,
      OpenShiftRoutes routes,
      OpenShiftPersistentVolumeClaims pvcs) {
    this.workspaceId = workspaceId;
    this.pods = pods;
    this.services = services;
    this.routes = routes;
    this.pvcs = pvcs;
  }

  public OpenShiftProject(OpenShiftClientFactory clientFactory, String name, String workspaceId)
      throws InfrastructureException {
    this.workspaceId = workspaceId;
    this.pods = new OpenShiftPods(name, workspaceId, clientFactory);
    this.services = new OpenShiftServices(name, workspaceId, clientFactory);
    this.routes = new OpenShiftRoutes(name, workspaceId, clientFactory);
    this.pvcs = new OpenShiftPersistentVolumeClaims(name, clientFactory);
    final OpenShiftClient client = clientFactory.create();
    if (get(name, client) == null) {
      create(name, client);
    }
  }

  /** Returns object for managing {@link Pod} instances inside project. */
  public OpenShiftPods pods() {
    return pods;
  }

  /** Returns object for managing {@link Service} instances inside project. */
  public OpenShiftServices services() {
    return services;
  }

  /** Returns object for managing {@link Route} instances inside project. */
  public OpenShiftRoutes routes() {
    return routes;
  }

  /** Returns object for managing {@link PersistentVolumeClaim} instances inside project. */
  public OpenShiftPersistentVolumeClaims persistentVolumeClaims() {
    return pvcs;
  }

  /** Removes all object except persistent volume claims inside project. */
  public void cleanUp() throws InfrastructureException {
    doRemove(pods::delete, services::delete, routes::delete);
  }

  /**
   * Performs all the specified operations and throw exception with composite message if errors
   * occurred while any operation execution
   */
  private void doRemove(RemoveOperation... operations) throws InfrastructureException {
    StringBuilder errors = new StringBuilder();
    for (RemoveOperation operation : operations) {
      try {
        operation.perform();
      } catch (InternalInfrastructureException e) {
        LOG.warn(
            "Internal infra error occurred while cleaning project up for workspace with id "
                + workspaceId,
            e);
        errors.append(" ").append(e.getMessage());
      } catch (InfrastructureException e) {
        errors.append(" ").append(e.getMessage());
      }
    }

    if (errors.length() > 0) {
      throw new InfrastructureException(
          "Error(s) occurs while cleaning project up." + errors.toString());
    }
  }

  private void create(String projectName, OpenShiftClient client) throws InfrastructureException {
    try {
      client
          .projectrequests()
          .createNew()
          .withNewMetadata()
          .withName(projectName)
          .endMetadata()
          .done();
    } catch (KubernetesClientException e) {
      throw new InfrastructureException(e.getMessage(), e);
    }
  }

  private Project get(String projectName, OpenShiftClient client) throws InfrastructureException {
    try {
      return client.projects().withName(projectName).get();
    } catch (KubernetesClientException e) {
      if (e.getCode() == 403) {
        // project is foreign or doesn't exist
        return null;
      } else {
        throw new InfrastructureException(e.getMessage(), e);
      }
    }
  }

  interface RemoveOperation {
    void perform() throws InfrastructureException;
  }
}
