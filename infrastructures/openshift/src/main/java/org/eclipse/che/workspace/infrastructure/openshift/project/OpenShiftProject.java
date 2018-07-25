/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.openshift.project;

import com.google.common.annotations.VisibleForTesting;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.api.model.Project;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.client.OpenShiftClient;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesInfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesConfigsMaps;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesDeployments;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesIngresses;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesPersistentVolumeClaims;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesSecrets;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesServices;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientFactory;

/**
 * Defines an internal API for managing subset of objects inside {@link Project} instance.
 *
 * @author Sergii Leshchenko
 */
public class OpenShiftProject extends KubernetesNamespace {

  private final OpenShiftRoutes routes;
  private final OpenShiftClientFactory clientFactory;

  @VisibleForTesting
  OpenShiftProject(
      OpenShiftClientFactory clientFactory,
      String workspaceId,
      String name,
      KubernetesDeployments deployments,
      KubernetesServices services,
      OpenShiftRoutes routes,
      KubernetesPersistentVolumeClaims pvcs,
      KubernetesIngresses ingresses,
      KubernetesSecrets secrets,
      KubernetesConfigsMaps configMaps) {
    super(
        clientFactory,
        workspaceId,
        name,
        deployments,
        services,
        pvcs,
        ingresses,
        secrets,
        configMaps);
    this.clientFactory = clientFactory;
    this.routes = routes;
  }

  public OpenShiftProject(OpenShiftClientFactory clientFactory, String name, String workspaceId) {
    super(clientFactory, name, workspaceId);
    this.clientFactory = clientFactory;
    this.routes = new OpenShiftRoutes(name, workspaceId, clientFactory);
  }

  /**
   * Prepare project for using.
   *
   * <p>Preparing includes creating if needed and waiting for default service account.
   *
   * @throws InfrastructureException if any exception occurs during namespace preparing
   */
  void prepare() throws InfrastructureException {
    String workspaceId = getWorkspaceId();
    String projectName = getName();

    KubernetesClient kubeClient = clientFactory.create(workspaceId);
    OpenShiftClient osClient = clientFactory.createOC(workspaceId);

    if (get(projectName, osClient) == null) {
      create(projectName, kubeClient, osClient);
    }
  }

  /** Returns object for managing {@link Route} instances inside project. */
  public OpenShiftRoutes routes() {
    return routes;
  }

  /** Removes all object except persistent volume claims inside project. */
  public void cleanUp() throws InfrastructureException {
    doRemove(
        routes::delete,
        services()::delete,
        deployments()::delete,
        secrets()::delete,
        configMaps()::delete);
  }

  private void create(String projectName, KubernetesClient kubeClient, OpenShiftClient ocClient)
      throws InfrastructureException {
    try {
      ocClient
          .projectrequests()
          .createNew()
          .withNewMetadata()
          .withName(projectName)
          .endMetadata()
          .done();
      waitDefaultServiceAccount(projectName, kubeClient);
    } catch (KubernetesClientException e) {
      throw new KubernetesInfrastructureException(e);
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
        throw new KubernetesInfrastructureException(e);
      }
    }
  }
}
