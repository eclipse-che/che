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
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace;

import static java.util.stream.Collectors.toSet;

import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.client.KubernetesClientException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesInfrastructureException;

/**
 * Defines an internal API for managing {@link PersistentVolumeClaim} instances in {@link
 * KubernetesPersistentVolumeClaims#namespace predefined namespace}.
 *
 * @author Sergii Leshchenko
 */
public class KubernetesPersistentVolumeClaims {
  private final String namespace;
  private final KubernetesClientFactory clientFactory;

  KubernetesPersistentVolumeClaims(String namespace, KubernetesClientFactory clientFactory) {
    this.namespace = namespace;
    this.clientFactory = clientFactory;
  }

  /**
   * Creates specified persistent volume claim.
   *
   * @param pvc persistent volume claim to create
   * @return created persistent volume claim
   * @throws InfrastructureException when any exception occurs
   */
  public PersistentVolumeClaim create(PersistentVolumeClaim pvc) throws InfrastructureException {
    try {
      return clientFactory.create().persistentVolumeClaims().inNamespace(namespace).create(pvc);
    } catch (KubernetesClientException e) {
      throw new KubernetesInfrastructureException(e);
    }
  }

  /**
   * Returns all existing persistent volume claims.
   *
   * @throws InfrastructureException when any exception occurs
   */
  public List<PersistentVolumeClaim> get() throws InfrastructureException {
    try {
      return clientFactory
          .create()
          .persistentVolumeClaims()
          .inNamespace(namespace)
          .list()
          .getItems();
    } catch (KubernetesClientException e) {
      throw new KubernetesInfrastructureException(e);
    }
  }

  /**
   * Returns all existing persistent volume claims with given label.
   *
   * @param labelName name of provided label
   * @param labelValue value of provided label
   * @return list of matched PVCs
   * @throws InfrastructureException when any exception occurs while fetching the pvcs
   */
  public List<PersistentVolumeClaim> getByLabel(String labelName, String labelValue)
      throws InfrastructureException {
    try {
      return clientFactory
          .create()
          .persistentVolumeClaims()
          .inNamespace(namespace)
          .withLabel(labelName, labelValue)
          .list()
          .getItems();
    } catch (KubernetesClientException e) {
      throw new KubernetesInfrastructureException(e);
    }
  }

  /**
   * Creates all PVCs which are not present in current Kubernetes namespace.
   *
   * @param toCreate collection of PVCs to create
   * @throws InfrastructureException when any error occurs while creation
   */
  public void createIfNotExist(Collection<PersistentVolumeClaim> toCreate)
      throws InfrastructureException {
    final Set<String> existing =
        get().stream().map(p -> p.getMetadata().getName()).collect(toSet());
    for (PersistentVolumeClaim pvc : toCreate) {
      if (!existing.contains(pvc.getMetadata().getName())) {
        create(pvc);
      }
    }
  }
}
