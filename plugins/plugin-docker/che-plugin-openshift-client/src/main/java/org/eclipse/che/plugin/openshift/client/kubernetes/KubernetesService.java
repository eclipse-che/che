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

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.ServicePort;

/**
 * Provides API for managing Kubernetes {@link ServicePort}
 */
public final class KubernetesService {

    private KubernetesService() {
    }

    /**
     * Retrieves list of {@link ServicePort} based on ports defined in
     * {@link ContainerConfig} and {@link ImageConfig}
     *
     * @param exposedPorts
     * @return list of {@link ServicePort}
     */
    public static List<ServicePort> getServicePortsFrom(Set<String> exposedPorts) {
        List<ServicePort> servicePorts = new ArrayList<>(exposedPorts.size());
        for (String exposedPort : exposedPorts) {
            String[] portAndProtocol = exposedPort.split("/", 2);
            String port = portAndProtocol[0];
            String protocol = portAndProtocol[1];

            int portNumber = Integer.parseInt(port);
            String portName = CheServicePorts.get().get(portNumber);
            portName = isNullOrEmpty(portName) ? "server-" + exposedPort.replace("/", "-") : portName;

            int targetPortNumber = portNumber;
            ServicePort servicePort = new ServicePort();
            servicePort.setName(portName);
            servicePort.setProtocol(protocol.toUpperCase());
            servicePort.setPort(portNumber);
            servicePort.setTargetPort(new IntOrString(targetPortNumber));
            servicePorts.add(servicePort);
        }
        return servicePorts;
    }

}
