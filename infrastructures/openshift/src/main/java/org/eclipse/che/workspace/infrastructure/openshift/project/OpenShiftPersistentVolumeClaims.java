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

import static java.util.stream.Collectors.toSet;

import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.client.KubernetesClientException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientFactory;

/**
 * Defines an internal API for managing {@link PersistentVolumeClaim} instances in {@link
 * OpenShiftPersistentVolumeClaims#namespace predefined namespace}.
 *
 * @author Sergii Leshchenko
 */
public class OpenShiftPersistentVolumeClaims {
  private final String namespace;
  private final OpenShiftClientFactory clientFactory;

  OpenShiftPersistentVolumeClaims(String namespace, OpenShiftClientFactory clientFactory) {
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
      throw new InfrastructureException(e.getMessage(), e);
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
      throw new InfrastructureException(e.getMessage(), e);
    }
  }

  /**
   * Creates all PVCs which are not present in current OpenShift project.
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
