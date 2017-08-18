/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.workspace.infrastructure.openshift.project;

import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.client.OpenShiftClient;

import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientFactory;

import java.util.List;

/**
 * Defines an internal API for managing {@link PersistentVolumeClaim} instances in
 * {@link OpenShiftPersistentVolumeClaims#namespace predefined namespace}.
 *
 * @author Sergii Leshchenko
 */
public class OpenShiftPersistentVolumeClaims {
    private final String                 namespace;
    private final OpenShiftClientFactory clientFactory;

    OpenShiftPersistentVolumeClaims(String namespace, OpenShiftClientFactory clientFactory) {
        this.namespace = namespace;
        this.clientFactory = clientFactory;
    }

    /**
     * Creates specified persistent volume claim.
     *
     * @param pvc
     *         persistent volume claim to create
     * @return created persistent volume claim
     * @throws InfrastructureException
     *         when any exception occurs
     */
    public PersistentVolumeClaim create(PersistentVolumeClaim pvc) throws InfrastructureException {
        try (OpenShiftClient client = clientFactory.create()) {
            return client.persistentVolumeClaims()
                         .inNamespace(namespace)
                         .create(pvc);
        } catch (KubernetesClientException e) {
            throw new InfrastructureException(e.getMessage(), e);
        }
    }

    /**
     * Returns all existing persistent volume claims.
     *
     * @throws InfrastructureException
     *         when any exception occurs
     */
    public List<PersistentVolumeClaim> get() throws InfrastructureException {
        try (OpenShiftClient client = clientFactory.create()) {
            return client.persistentVolumeClaims()
                         .inNamespace(namespace)
                         .list()
                         .getItems();
        } catch (KubernetesClientException e) {
            throw new InfrastructureException(e.getMessage(), e);
        }
    }
}
