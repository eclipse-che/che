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

import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.api.model.Project;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.client.OpenShiftClient;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientFactory;

/**
 * Defines an internal API for managing subset of objects inside {@link Project} instance.
 *
 * @author Sergii Leshchenko
 */
public class OpenShiftProject {

  private final OpenShiftPods pods;
  private final OpenShiftServices services;
  private final OpenShiftRoutes routes;
  private final OpenShiftPersistentVolumeClaims pvcs;

  public OpenShiftProject(OpenShiftClientFactory clientFactory, String name, String workspaceId)
      throws InfrastructureException {
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

  /** Removes all object except persistent volume claim inside project. */
  public void cleanUp() throws InfrastructureException {
    pods.delete();
    services.delete();
    routes.delete();
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
}
