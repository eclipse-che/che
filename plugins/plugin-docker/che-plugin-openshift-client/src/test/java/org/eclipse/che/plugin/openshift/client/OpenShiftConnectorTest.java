/*******************************************************************************
 * Copyright (c) 2012-2016 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.openshift.client;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.che.plugin.docker.client.DockerApiVersionPathPrefixProvider;
import org.eclipse.che.plugin.docker.client.DockerConnectorConfiguration;
import org.eclipse.che.plugin.docker.client.DockerRegistryAuthResolver;
import org.eclipse.che.plugin.docker.client.connection.DockerConnectionFactory;
import org.eclipse.che.plugin.docker.client.json.ContainerConfig;
import org.eclipse.che.plugin.docker.client.json.ExposedPort;
import org.eclipse.che.plugin.docker.client.params.CreateContainerParams;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.ServicePort;

@Listeners(MockitoTestNGListener.class)
public class OpenShiftConnectorTest {

    private static final String[] CONTAINER_ENV_VARIABLES = {"CHE_WORKSPACE_ID=abcd1234"};
    private static final String   CHE_DEFAULT_OPENSHIFT_PROJECT_NAME = "eclipse-che";
    private static final String   CHE_DEFAULT_OPENSHIFT_SERVICEACCOUNT = "cheserviceaccount";
    private static final String   OPENSHIFT_API_ENDPOINT_MINISHIFT = "https://192.168.64.2:8443/";
    private static final String   OPENSHIFT_DEFAULT_USER_NAME = "openshift-dev";
    private static final String   OPENSHIFT_DEFAULT_USER_PASSWORD = "devel";

    @Mock
    private DockerConnectorConfiguration       dockerConnectorConfiguration;
    @Mock
    private DockerConnectionFactory            dockerConnectionFactory;
    @Mock
    private DockerRegistryAuthResolver         authManager;
    @Mock
    private DockerApiVersionPathPrefixProvider dockerApiVersionPathPrefixProvider;

    @Mock
    private CreateContainerParams createContainerParams;

    private OpenShiftConnector openShiftConnector;
    
    private KubernetesLabelConverter kubernetesLabelConverter;

    @BeforeMethod
    public void setup() {
        kubernetesLabelConverter = new KubernetesLabelConverter();
        openShiftConnector = spy(new OpenShiftConnector(dockerConnectorConfiguration,
                                                        dockerConnectionFactory,
                                                        authManager,
                                                        dockerApiVersionPathPrefixProvider,
                                                        OPENSHIFT_API_ENDPOINT_MINISHIFT,
                                                        OPENSHIFT_DEFAULT_USER_NAME,
                                                        OPENSHIFT_DEFAULT_USER_PASSWORD,
                                                        CHE_DEFAULT_OPENSHIFT_PROJECT_NAME,
                                                        CHE_DEFAULT_OPENSHIFT_SERVICEACCOUNT));
    }


    @Test
    public void shouldGetWorkspaceIDWhenAValidOneIsProvidedInCreateContainerParams() throws IOException {
        //Given
        String expectedWorkspaceID="abcd1234";
        ContainerConfig containerConfig = mock(ContainerConfig.class);
        CreateContainerParams createContainerParams = CreateContainerParams.create(containerConfig);

        when(containerConfig.getEnv()).thenReturn(CONTAINER_ENV_VARIABLES);

        //When
        String workspaceID = openShiftConnector.getCheWorkspaceId(createContainerParams);

        //Then
        assertEquals(expectedWorkspaceID, workspaceID);
    }

    @Test
    public void shouldReturnServicePortListFromExposedPortList() {
        // Given
        Map<String, Map<String, String>> exposedPorts = new HashMap<>();
        exposedPorts.put("8080/tcp",null);
        exposedPorts.put("22/tcp",null);
        exposedPorts.put("4401/tcp",null);
        exposedPorts.put("4403/tcp",null);

        // When
        List<ServicePort> servicePorts = openShiftConnector.getServicePortsFrom(exposedPorts.keySet());

        // Then
        List<String> portsAndProtocols = servicePorts.stream().
                map(p -> Integer.toString(p.getPort()) +
                        OpenShiftConnector.DOCKER_PROTOCOL_PORT_DELIMITER +
                        p.getProtocol()).collect(Collectors.toList());
        assertTrue(exposedPorts.keySet().stream().anyMatch(portsAndProtocols::contains));
    }

    @Test
    public void shouldReturnServicePortNameWhenKnownPortNumberIsProvided() {
        // Given
        Map<String, Map<String, String>> exposedPorts = new HashMap<>();
        exposedPorts.put("22/tcp",null);
        exposedPorts.put("4401/tcp",null);
        exposedPorts.put("4403/tcp",null);
        exposedPorts.put("4411/tcp",null);
        exposedPorts.put("8080/tcp",null);
        exposedPorts.put("8888/tcp",null);
        exposedPorts.put("9876/tcp",null);

        Set<String> expectedPortNames = new HashSet<>();
        expectedPortNames.add("sshd");
        expectedPortNames.add("wsagent");
        expectedPortNames.add("wsagent-pda");
        expectedPortNames.add("terminal");
        expectedPortNames.add("tomcat");
        expectedPortNames.add("tomcat-jpda");
        expectedPortNames.add("codeserver");

        // When
        List<ServicePort> servicePorts = openShiftConnector.getServicePortsFrom(exposedPorts.keySet());
        List<String> actualPortNames = servicePorts.stream().
                map(p -> p.getName()).collect(Collectors.toList());

        // Then
        assertTrue(actualPortNames.stream().anyMatch(expectedPortNames::contains));
    }

    @Test
    public void shouldReturnServicePortNameWhenUnknownPortNumberIsProvided() {
        // Given
        Map<String, Map<String, String>> exposedPorts = new HashMap<>();
        exposedPorts.put("55/tcp",null);

        Set<String> expectedPortNames = new HashSet<>();
        expectedPortNames.add("55-tcp");

        // When
        List<ServicePort> servicePorts = openShiftConnector.getServicePortsFrom(exposedPorts.keySet());
        List<String> actualPortNames = servicePorts.stream().
                map(p -> p.getName()).collect(Collectors.toList());

        // Then
        assertTrue(actualPortNames.stream().anyMatch(expectedPortNames::contains));
    }

    @Test
    public void shouldReturnContainerPortFromExposedPortList() {
        // Given
        Set<String> exposedPorts = new HashSet<>();
        exposedPorts.add("8080/tcp");
        exposedPorts.add("22/tcp");
        exposedPorts.add("4401/tcp");
        exposedPorts.add("4403/tcp");

        // When
        List<ContainerPort> containerPorts = openShiftConnector.getContainerPortsFrom(exposedPorts);

        // Then
        List<String> portsAndProtocols = containerPorts.stream().
                map(p -> Integer.toString(p.getContainerPort()) +
                        OpenShiftConnector.DOCKER_PROTOCOL_PORT_DELIMITER +
                        p.getProtocol().toLowerCase()).collect(Collectors.toList());
        assertTrue(exposedPorts.stream().anyMatch(portsAndProtocols::contains));
    }

    @Test
    public void shouldReturnContainerEnvFromEnvVariableArray() {
        // Given
        String[] envVariables = {"CHE_LOCAL_CONF_DIR=/mnt/che/conf",
                "USER_TOKEN=dummy_token",
                "CHE_API_ENDPOINT=http://172.17.0.4:8080/wsmaster/api",
                "JAVA_OPTS=-Xms256m -Xmx2048m -Djava.security.egd=file:/dev/./urandom",
                "CHE_WORKSPACE_ID=workspaceID",
                "CHE_PROJECTS_ROOT=/projects",
//                "PATH=/opt/jdk1.8.0_45/bin:/home/user/apache-maven-3.3.9/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
//                "MAVEN_VERSION=3.3.9",
//                "JAVA_VERSION=8u45",
//                "JAVA_VERSION_PREFIX=1.8.0_45",
                "TOMCAT_HOME=/home/user/tomcat8",
//                "JAVA_HOME=/opt/jdk1.8.0_45",
                "M2_HOME=/home/user/apache-maven-3.3.9",
                "TERM=xterm",
                "LANG=en_US.UTF-8"
        };

        // When
        List<EnvVar> env = openShiftConnector.getContainerEnvFrom(envVariables);

        // Then
        List<String> keysAndValues = env.stream().map(k -> k.getName() + "=" + k.getValue()).collect(Collectors.toList());
        assertTrue(Arrays.stream(envVariables).anyMatch(keysAndValues::contains));
    }

    @Test
    public void shouldReturnContainerPortListFromImageExposedPortList() {
        // Given
        Map<String, ExposedPort> imageExposedPorts = new HashMap<>();
        imageExposedPorts.put("8080/tcp",new ExposedPort());

        // When
        List<ContainerPort> containerPorts = openShiftConnector.getContainerPortsFrom(imageExposedPorts.keySet());

        // Then
        List<String> portsAndProtocols = containerPorts.stream().
                map(p -> Integer.toString(p.getContainerPort()) +
                        OpenShiftConnector.DOCKER_PROTOCOL_PORT_DELIMITER +
                        p.getProtocol().toLowerCase()).collect(Collectors.toList());
        assertTrue(imageExposedPorts.keySet().stream().anyMatch(portsAndProtocols::contains));
    }

    @Test
    public void shouldReturnServicePortListFromImageExposedPortList() {
        // Given
        Map<String, ExposedPort> imageExposedPorts = new HashMap<>();
        imageExposedPorts.put("8080/tcp",new ExposedPort());

        // When
        List<ServicePort> servicePorts = openShiftConnector.getServicePortsFrom(imageExposedPorts.keySet());

        // Then
        List<String> portsAndProtocols = servicePorts.stream().
                map(p -> Integer.toString(p.getPort()) +
                        OpenShiftConnector.DOCKER_PROTOCOL_PORT_DELIMITER +
                        p.getProtocol()).collect(Collectors.toList());
        assertTrue(imageExposedPorts.keySet().stream().anyMatch(portsAndProtocols::contains));
    }

    @Test
    public void shouldConvertLabelsToValidKubernetesLabelNames() {
        String validLabelRegex   = "([A-Za-z0-9][-A-Za-z0-9_\\.]*)?[A-Za-z0-9]";
        String prefix = kubernetesLabelConverter.getCheServerLabelPrefix();

        // Given
        Map<String, String> labels = new HashMap<>();
        labels.put(prefix + "4401/tcp:path:", "/api");
        labels.put(prefix + "8000/tcp:ref:", "tomcat-debug");

        // When
        Map<String, String> converted = kubernetesLabelConverter.labelsToNames(labels);

        // Then
        for (Map.Entry<String, String> entry : converted.entrySet()) {
            assertTrue(entry.getKey().matches(validLabelRegex),
                    String.format("Converted Key %s should be valid Kubernetes label name", entry.getKey()));
            assertTrue(entry.getValue().matches(validLabelRegex),
                    String.format("Converted Value %s should be valid Kubernetes label name", entry.getValue()));
        }
    }

    @Test
    public void shouldBeAbleToRecoverOriginalLabelsAfterConversion() {
        // Given
        String prefix = kubernetesLabelConverter.getCheServerLabelPrefix();
        Map<String, String> originalLabels = new HashMap<>();
        originalLabels.put(prefix + "4401/tcp:path:", "/api");
        originalLabels.put(prefix + "8000/tcp:ref:", "tomcat-debug");

        // When
        Map<String, String> converted   = kubernetesLabelConverter.labelsToNames(originalLabels);
        Map<String, String> unconverted = kubernetesLabelConverter.namesToLabels(converted);

        // Then
        assertEquals(originalLabels, unconverted);
    }
}
