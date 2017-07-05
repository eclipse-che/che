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

import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.che.plugin.docker.client.json.ExposedPort;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.fabric8.kubernetes.api.model.ContainerPort;

public class KubernetesContainerTest {

    @Test
    public void shouldReturnContainerPortFromExposedPortList() {
        // Given
        Set<String> exposedPorts = new HashSet<>();
        exposedPorts.add("8080/tcp");
        exposedPorts.add("22/tcp");
        exposedPorts.add("4401/tcp");
        exposedPorts.add("4403/tcp");

        // When
        List<ContainerPort> containerPorts = KubernetesContainer.getContainerPortsFrom(exposedPorts);

        // Then
        List<String> portsAndProtocols = containerPorts.stream().
                map(p -> Integer.toString(p.getContainerPort()) +
                         "/" +
                         p.getProtocol().toLowerCase()).collect(Collectors.toList());
        assertTrue(exposedPorts.stream().allMatch(portsAndProtocols::contains));
    }

    @Test
    public void shouldReturnContainerPortListFromImageExposedPortList() {
        // Given
        Map<String, ExposedPort> imageExposedPorts = new HashMap<>();
        imageExposedPorts.put("8080/tcp",new ExposedPort());

        // When
        List<ContainerPort> containerPorts = KubernetesContainer.getContainerPortsFrom(imageExposedPorts.keySet());

        // Then
        List<String> portsAndProtocols = containerPorts.stream().
                map(p -> Integer.toString(p.getContainerPort()) +
                        "/" +
                        p.getProtocol().toLowerCase()).collect(Collectors.toList());
        assertTrue(imageExposedPorts.keySet().stream().allMatch(portsAndProtocols::contains));
    }

}
