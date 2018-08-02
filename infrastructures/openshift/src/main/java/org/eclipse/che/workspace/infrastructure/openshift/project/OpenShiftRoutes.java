/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.openshift.project;

import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_WORKSPACE_ID_LABEL;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.putLabel;

import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.api.model.Route;
import java.util.List;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesInfrastructureException;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientFactory;

/**
 * Defines an internal API for managing {@link Route} instances in {@link OpenShiftRoutes#namespace
 * predefined namespace}.
 *
 * @author Sergii Leshchenko
 */
public class OpenShiftRoutes {
  private final String namespace;
  private final String workspaceId;
  private final OpenShiftClientFactory clientFactory;

  OpenShiftRoutes(String namespace, String workspaceId, OpenShiftClientFactory clientFactory) {
    this.namespace = namespace;
    this.workspaceId = workspaceId;
    this.clientFactory = clientFactory;
  }

  /**
   * Creates specified route.
   *
   * @param route route to create
   * @return created route
   * @throws InfrastructureException when any exception occurs
   */
  public Route create(Route route) throws InfrastructureException {
    putLabel(route, CHE_WORKSPACE_ID_LABEL, workspaceId);
    try {
      return clientFactory.createOC(workspaceId).routes().inNamespace(namespace).create(route);
    } catch (KubernetesClientException e) {
      throw new KubernetesInfrastructureException(e);
    }
  }

  /**
   * Returns all existing routes.
   *
   * @throws InfrastructureException when any exception occurs
   */
  public List<Route> get() throws InfrastructureException {
    try {
      return clientFactory
          .createOC(workspaceId)
          .routes()
          .inNamespace(namespace)
          .withLabel(CHE_WORKSPACE_ID_LABEL, workspaceId)
          .list()
          .getItems();
    } catch (KubernetesClientException e) {
      throw new KubernetesInfrastructureException(e);
    }
  }

  /**
   * Deletes all existing routes.
   *
   * @throws InfrastructureException when any exception occurs
   */
  public void delete() throws InfrastructureException {
    try {
      clientFactory
          .createOC(workspaceId)
          .routes()
          .inNamespace(namespace)
          .withLabel(CHE_WORKSPACE_ID_LABEL, workspaceId)
          .delete();
    } catch (KubernetesClientException e) {
      throw new KubernetesInfrastructureException(e);
    }
  }
}
