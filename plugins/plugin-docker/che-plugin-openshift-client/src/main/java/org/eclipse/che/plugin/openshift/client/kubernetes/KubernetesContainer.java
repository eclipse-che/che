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
package org.eclipse.che.plugin.openshift.client.kubernetes;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.che.plugin.docker.client.json.ContainerConfig;
import org.eclipse.che.plugin.docker.client.json.ImageConfig;
import org.eclipse.che.plugin.openshift.client.CheServicePorts;

import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;

/**
 * Provides API for managing Kubernetes {@link ContainerPort}
 */
public final class KubernetesContainer {

    private KubernetesContainer() {
    }

    /**
     * Retrieves list of ({@link ContainerPort} based on ports defined in
     * {@link ContainerConfig} and {@link ImageConfig}
     *
     * @param exposedPorts
     * @return list of {@link ContainerPort}
     */
    public static List<ContainerPort> getContainerPortsFrom(Set<String> exposedPorts) {
        List<ContainerPort> containerPorts = new ArrayList<>(exposedPorts.size());
        for (String exposedPort : exposedPorts) {
            String[] portAndProtocol = exposedPort.split("/", 2);
            String port = portAndProtocol[0];
            String protocol = portAndProtocol[1].toUpperCase();

            int portNumber = Integer.parseInt(port);
            String portName = CheServicePorts.get().get(portNumber);
            portName = isNullOrEmpty(portName) ? exposedPort.replace("/", "-") : portName;

            ContainerPort containerPort = new ContainerPortBuilder().withName(portName).withProtocol(protocol)
                    .withContainerPort(portNumber).build();
            containerPorts.add(containerPort);
        }
        return containerPorts;
    }

}
