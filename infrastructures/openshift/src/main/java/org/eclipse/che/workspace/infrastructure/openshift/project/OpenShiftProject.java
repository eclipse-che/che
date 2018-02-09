/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
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
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.api.model.Project;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.client.OpenShiftClient;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesIngresses;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesPersistentVolumeClaims;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesPods;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesServices;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientFactory;

/**
 * Defines an internal API for managing subset of objects inside {@link Project} instance.
 *
 * @author Sergii Leshchenko
 */
public class OpenShiftProject extends KubernetesNamespace {

  private final OpenShiftRoutes routes;

  @VisibleForTesting
  OpenShiftProject(
      String workspaceId,
      KubernetesPods pods,
      KubernetesServices services,
      OpenShiftRoutes routes,
      KubernetesPersistentVolumeClaims pvcs,
      KubernetesIngresses ingresses) {
    super(workspaceId, pods, services, pvcs, ingresses);
    this.routes = routes;
  }

  public OpenShiftProject(OpenShiftClientFactory clientFactory, String name, String workspaceId)
      throws InfrastructureException {
    super(clientFactory, name, workspaceId, false);
    this.routes = new OpenShiftRoutes(name, workspaceId, clientFactory);
    doPrepare(name, clientFactory.create());
  }

  private void doPrepare(String name, OpenShiftClient osClient) throws InfrastructureException {
    if (get(name, osClient) == null) {
      create(name, osClient);
    }
  }

  /** Returns object for managing {@link Route} instances inside project. */
  public OpenShiftRoutes routes() {
    return routes;
  }

  /** Removes all object except persistent volume claims inside project. */
  public void cleanUp() throws InfrastructureException {
    doRemove(routes::delete, services()::delete, pods()::delete);
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
      waitDefaultServiceAccount(projectName, client);
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
