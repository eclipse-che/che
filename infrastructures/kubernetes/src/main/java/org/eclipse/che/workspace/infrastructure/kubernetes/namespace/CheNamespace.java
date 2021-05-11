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
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace;

import static io.fabric8.kubernetes.api.model.DeletionPropagation.BACKGROUND;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Annotations.CREATE_IN_CHE_INSTALLATION_NAMESPACE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_WORKSPACE_ID_LABEL;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.putLabel;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.shouldCreateInCheNamespace;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.client.KubernetesClientException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.WorkspaceRuntimes;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalRuntime;
import org.eclipse.che.workspace.infrastructure.kubernetes.Annotations;
import org.eclipse.che.workspace.infrastructure.kubernetes.CheServerKubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesInfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.CheInstallationLocation;

/**
 * This singleton bean can be used to create K8S object in Che installation namespaces. These
 * objects are related to particular workspace, but for some reason has to be created in Che
 * namespace.
 */
@Singleton
public class CheNamespace {

  private final String cheNamespaceName;
  private final CheServerKubernetesClientFactory clientFactory;
  private final WorkspaceRuntimes workspaceRuntimes;

  @Inject
  public CheNamespace(
      CheInstallationLocation installationLocation,
      CheServerKubernetesClientFactory clientFactory,
      WorkspaceRuntimes workspaceRuntimes)
      throws InfrastructureException {
    this.cheNamespaceName = installationLocation.getInstallationLocationNamespace();
    this.clientFactory = clientFactory;
    this.workspaceRuntimes = workspaceRuntimes;
  }

  /**
   * Creates given {@link ConfigMap}s in Che installation namespace labeled with `workspaceId` from
   * given `identity`.
   *
   * <p>`workspaceId` from given `identity` must be valid workspace ID, that is in {@link
   * WorkspaceStatus#STARTING} state. Otherwise, {@link InfrastructureException} is thrown.
   *
   * @param configMap to create
   * @param identity to validate and label configmaps
   * @return created {@link ConfigMap}s
   * @throws InfrastructureException when something goes wrong
   */
  private ConfigMap createConfigMap(ConfigMap configMap, RuntimeIdentity identity)
      throws InfrastructureException {
    putLabel(configMap, CHE_WORKSPACE_ID_LABEL, identity.getWorkspaceId());
    // check that ConfigMap is properly annotated to be created in Che installation namespace
    if (!shouldCreateInCheNamespace(configMap)) {
      throw new InfrastructureException(
          String.format(
              "ConfigMap '%s' to be created in Che installation namespace is not properly annotated with '%s=true'. This is a bug, please report.",
              configMap.getMetadata().getName(), CREATE_IN_CHE_INSTALLATION_NAMESPACE));
    }
    // remove this annotation, so it's not exposed in actual k8s object
    configMap.getMetadata().getAnnotations().remove(CREATE_IN_CHE_INSTALLATION_NAMESPACE);
    return clientFactory.create().configMaps().inNamespace(cheNamespaceName).create(configMap);
  }

  /**
   * Creates given {@link ConfigMap}s in Che installation namespace labeled with `workspaceId` from
   * given `identity`.
   *
   * <p>`workspaceId` from given `identity` must be valid workspace ID, that is in {@link
   * WorkspaceStatus#STARTING} state. Otherwise, {@link InfrastructureException} is thrown.
   *
   * <p>all given {@code configMaps} must be annotated with {@link
   * Annotations#CREATE_IN_CHE_INSTALLATION_NAMESPACE} set to 'true'.
   *
   * @param configMaps to create
   * @param identity to validate and label configmaps
   * @return created {@link ConfigMap}s
   * @throws InfrastructureException when something goes wrong
   */
  public List<ConfigMap> createConfigMaps(List<ConfigMap> configMaps, RuntimeIdentity identity)
      throws InfrastructureException {
    if (configMaps.isEmpty()) {
      return Collections.emptyList();
    }
    validate(identity, WorkspaceStatus.STARTING);

    List<ConfigMap> createdConfigMaps = new ArrayList<>();
    for (ConfigMap cm : configMaps) {
      createdConfigMaps.add(createConfigMap(cm, identity));
    }
    return createdConfigMaps;
  }

  /**
   * Cleanup all objects related to given `workspaceId` in Che installation namespace.
   *
   * @param workspaceId to delete objects
   * @throws InfrastructureException when workspaceId is null or something bad happen during
   *     removing the objects
   */
  public void cleanUp(String workspaceId) throws InfrastructureException {
    if (workspaceId == null) {
      throw new InfrastructureException("workspaceId to cleanup can't be null");
    }
    cleanUpConfigMaps(workspaceId);
  }

  /**
   * Checks whether we have valid `workspaceId` and `owner` of existing workspace.
   *
   * @param identity to get `workspaceId` and `owner` to check
   * @throws InfrastructureException when `workspaceId` is not valid workspace, is not in {@link
   *     WorkspaceStatus#STARTING} state, is `null`, or owner does not match.
   */
  private void validate(RuntimeIdentity identity, WorkspaceStatus expectedStatus)
      throws InfrastructureException {
    try {
      InternalRuntime<?> runtime = workspaceRuntimes.getInternalRuntime(identity.getWorkspaceId());
      if (!identity.getOwnerId().equals(runtime.getOwner())) {
        throw new InfrastructureException("Given owner does not match workspace's actual owner.");
      }

      if (runtime.getStatus() != expectedStatus) {
        throw new InfrastructureException("Can create objects only for starting workspaces.");
      }
    } catch (ServerException e) {
      throw new InfrastructureException(e);
    }
  }

  private void cleanUpConfigMaps(String workspaceId) throws InfrastructureException {
    try {
      clientFactory
          .create()
          .configMaps()
          .inNamespace(cheNamespaceName)
          .withLabel(CHE_WORKSPACE_ID_LABEL, workspaceId)
          .withPropagationPolicy(BACKGROUND)
          .delete();
    } catch (KubernetesClientException e) {
      throw new KubernetesInfrastructureException(e);
    }
  }
}
