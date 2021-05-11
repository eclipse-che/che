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
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_WORKSPACE_ID_LABEL;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.putLabel;

import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.client.KubernetesClientException;
import java.util.List;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesInfrastructureException;

/**
 * Defines an internal API for managing {@link Secret} instances in {@link
 * KubernetesSecrets#namespace predefined namespace}.
 *
 * @author Sergii Leshchenko
 */
public class KubernetesSecrets {
  private final String namespace;
  private final String workspaceId;
  private final KubernetesClientFactory clientFactory;

  KubernetesSecrets(String namespace, String workspaceId, KubernetesClientFactory clientFactory) {
    this.namespace = namespace;
    this.workspaceId = workspaceId;
    this.clientFactory = clientFactory;
  }

  /**
   * Creates specified secret.
   *
   * @param secret secret to create
   * @throws InfrastructureException when any exception occurs
   */
  public void create(Secret secret) throws InfrastructureException {
    putLabel(secret, CHE_WORKSPACE_ID_LABEL, workspaceId);
    try {
      clientFactory.create(workspaceId).secrets().inNamespace(namespace).create(secret);
    } catch (KubernetesClientException e) {
      throw new KubernetesInfrastructureException(e);
    }
  }

  /**
   * Finds secrets matching specified label selector.
   *
   * @param labelSelector selector to filter secrets
   * @return matched secrets list
   * @throws InfrastructureException when any exception occurs
   */
  public List<Secret> get(LabelSelector labelSelector) throws InfrastructureException {
    try {
      return clientFactory
          .create(workspaceId)
          .secrets()
          .inNamespace(namespace)
          .withLabelSelector(labelSelector)
          .list()
          .getItems();
    } catch (KubernetesClientException e) {
      throw new KubernetesInfrastructureException(e);
    }
  }

  /**
   * Deletes all existing secrets.
   *
   * @throws InfrastructureException when any exception occurs
   */
  public void delete() throws InfrastructureException {
    try {
      clientFactory
          .create(workspaceId)
          .secrets()
          .inNamespace(namespace)
          .withLabel(CHE_WORKSPACE_ID_LABEL, workspaceId)
          .withPropagationPolicy(BACKGROUND)
          .delete();
    } catch (KubernetesClientException e) {
      throw new KubernetesInfrastructureException(e);
    }
  }
}
