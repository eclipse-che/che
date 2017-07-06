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

import io.fabric8.kubernetes.api.model.ServicePort;

import org.eclipse.che.plugin.docker.client.json.ExposedPort;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.testng.Assert.assertTrue;

public class KubernetesServiceTest {

    @Test
    public void shouldReturnServicePortListFromImageExposedPortList() {
        // Given
        Map<String, ExposedPort> imageExposedPorts = new HashMap<>();
        imageExposedPorts.put("8080/TCP",new ExposedPort());

        // When
        List<ServicePort> servicePorts = KubernetesService.getServicePortsFrom(imageExposedPorts.keySet());

        // Then
        List<String> portsAndProtocols = servicePorts.stream().
                map(p -> Integer.toString(p.getPort()) +
                        "/" +
                        p.getProtocol()).collect(Collectors.toList());
        assertTrue(imageExposedPorts.keySet().stream().allMatch(portsAndProtocols::contains));
    }

    @Test
    public void shouldReturnServicePortListFromExposedPortList() {
        // Given
        Map<String, Map<String, String>> exposedPorts = new HashMap<>();
        exposedPorts.put("8080/TCP",null);
        exposedPorts.put("22/TCP",null);
        exposedPorts.put("4401/TCP",null);
        exposedPorts.put("4403/TCP",null);

        // When
        List<ServicePort> servicePorts = KubernetesService.getServicePortsFrom(exposedPorts.keySet());

        // Then
        List<String> portsAndProtocols = servicePorts.stream().
                map(p -> Integer.toString(p.getPort()) +
                        "/" +
                        p.getProtocol()).collect(Collectors.toList());
        assertTrue(exposedPorts.keySet().stream().allMatch(portsAndProtocols::contains));
    }

    @Test
    public void shouldReturnServicePortNameWhenKnownPortNumberIsProvided() {
        // Given
        Map<String, Map<String, String>> exposedPorts = new HashMap<>();
        exposedPorts.put("22/tcp",null);
        exposedPorts.put("4401/tcp",null);
        exposedPorts.put("4403/tcp",null);
        exposedPorts.put("4411/tcp",null);
        exposedPorts.put("4412/tcp",null);
        exposedPorts.put("8080/tcp",null);
        exposedPorts.put("8000/tcp",null);
        exposedPorts.put("9876/tcp",null);

        Set<String> expectedPortNames = new HashSet<>();
        expectedPortNames.add("sshd");
        expectedPortNames.add("wsagent");
        expectedPortNames.add("wsagent-jpda");
        expectedPortNames.add("terminal");
        expectedPortNames.add("exec-agent");
        expectedPortNames.add("tomcat");
        expectedPortNames.add("tomcat-jpda");
        expectedPortNames.add("codeserver");

        // When
        List<ServicePort> servicePorts = KubernetesService.getServicePortsFrom(exposedPorts.keySet());
        List<String> actualPortNames = servicePorts.stream().
                map(p -> p.getName()).collect(Collectors.toList());

        // Then
        assertTrue(actualPortNames.stream().allMatch(expectedPortNames::contains));
    }

    @Test
    public void shouldReturnServicePortNameWhenUnknownPortNumberIsProvided() {
        // Given
        Map<String, Map<String, String>> exposedPorts = new HashMap<>();
        exposedPorts.put("55/tcp",null);

        Set<String> expectedPortNames = new HashSet<>();
        expectedPortNames.add("server-55-tcp");

        // When
        List<ServicePort> servicePorts = KubernetesService.getServicePortsFrom(exposedPorts.keySet());
        List<String> actualPortNames = servicePorts.stream().
                map(p -> p.getName()).collect(Collectors.toList());

        // Then
        assertTrue(actualPortNames.stream().allMatch(expectedPortNames::contains));
    }

}
