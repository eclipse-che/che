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
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace;

import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_WORKSPACE_ID_LABEL;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.putLabel;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.client.KubernetesClientException;
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
          .delete();
    } catch (KubernetesClientException e) {
      throw new KubernetesInfrastructureException(e);
    }
  }
}
