/*******************************************************************************
 * Copyright (c) 2016-2017 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.che.plugin.docker.machine;

import org.testng.annotations.Test;

import static org.eclipse.che.plugin.docker.machine.ServerEvaluationStrategy.SERVER_CONF_LABEL_PATH_KEY;
import static org.eclipse.che.plugin.docker.machine.ServerEvaluationStrategy.SERVER_CONF_LABEL_PROTOCOL_KEY;
import static org.eclipse.che.plugin.docker.machine.ServerEvaluationStrategy.SERVER_CONF_LABEL_REF_KEY;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.che.api.machine.server.model.impl.ServerConfImpl;
import org.eclipse.che.api.machine.server.model.impl.ServerImpl;
import org.eclipse.che.api.machine.server.model.impl.ServerPropertiesImpl;
import org.eclipse.che.plugin.docker.client.json.ContainerConfig;
import org.eclipse.che.plugin.docker.client.json.ContainerInfo;
import org.eclipse.che.plugin.docker.client.json.NetworkSettings;
import org.eclipse.che.plugin.docker.client.json.PortBinding;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

@Listeners(MockitoTestNGListener.class)
public class ServerEvaluationStrategyTest {

    private static final String ALL_IP_ADDRESS           = "0.0.0.0";
    private static final String CONTAINERINFO_GATEWAY    = "172.17.0.1";
    private static final String DEFAULT_HOSTNAME         = "localhost";

    @Mock
    private ContainerInfo   containerInfo;
    @Mock
    private ContainerConfig containerConfig;
    @Mock
    private NetworkSettings networkSettings;

    private Map<String, ServerConfImpl> serverConfs;

    private Map<String, String> labels;

    private ServerEvaluationStrategy strategy;

    @BeforeMethod
    public void setUp() {
        strategy = new DefaultServerEvaluationStrategy(null, null);
        serverConfs = new HashMap<>();
        labels = new HashMap<>();

        when(containerInfo.getConfig()).thenReturn(containerConfig);
        when(containerInfo.getNetworkSettings()).thenReturn(networkSettings);
        when(networkSettings.getGateway()).thenReturn(CONTAINERINFO_GATEWAY);
        when(containerConfig.getLabels()).thenReturn(labels);
    }

    private Map<String, List<PortBinding>> getPorts() {
        Map<String, List<PortBinding>> ports = new HashMap<>();
        ports.put("8080/tcp", Collections.singletonList(new PortBinding().withHostIp(ALL_IP_ADDRESS)
                                                                         .withHostPort("32100")));
        ports.put("9090/udp", Collections.singletonList(new PortBinding().withHostIp(ALL_IP_ADDRESS)
                                                                         .withHostPort("32101")));
        return ports;
    }

    @Test
    public void shouldReturnServerForEveryExposedPort() throws Exception {
        // given
        Map<String, List<PortBinding>> ports = getPorts();
        when(networkSettings.getPorts()).thenReturn(ports);
        // when
        final Map<String, ServerImpl> servers = strategy.getServers(containerInfo,
                                                                    DEFAULT_HOSTNAME,
                                                                    serverConfs);
        // then
        assertEquals(servers.keySet(), ports.keySet());
    }

    @Test
    public void shouldAddDefaultReferenceIfReferenceIsNotSet() throws Exception {
        // given
        Map<String, List<PortBinding>> ports = getPorts();
        when(networkSettings.getPorts()).thenReturn(ports);

        final HashMap<String, ServerImpl> expectedServers = new HashMap<>();
        expectedServers.put("8080/tcp", new ServerImpl("Server-8080-tcp",
                                                       null,
                                                       CONTAINERINFO_GATEWAY + ":32100",
                                                       null,
                                                       new ServerPropertiesImpl(null, CONTAINERINFO_GATEWAY + ":32100", null)));
        expectedServers.put("9090/udp", new ServerImpl("Server-9090-udp",
                                                       null,
                                                       CONTAINERINFO_GATEWAY + ":32101",
                                                       null,
                                                       new ServerPropertiesImpl(null, CONTAINERINFO_GATEWAY + ":32101", null)));
        // when
        final Map<String, ServerImpl> servers = strategy.getServers(containerInfo, DEFAULT_HOSTNAME, serverConfs);

        // then
        assertEquals(servers, expectedServers);
    }

    @Test
    public void shouldAddRefUrlProtocolPathToServerFromMachineConfig() throws Exception {
        // given
        Map<String, List<PortBinding>> ports = getPorts();
        when(networkSettings.getPorts()).thenReturn(ports);

        serverConfs.put("8080/tcp", new ServerConfImpl("myserv1", "8080/tcp", "http", null));
        serverConfs.put("9090/udp", new ServerConfImpl("myserv2", "9090/udp", "dhcp", "/some/path"));

        final HashMap<String, ServerImpl> expectedServers = new HashMap<>();
        expectedServers.put("8080/tcp", new ServerImpl("myserv1",
                                                       "http",
                                                       CONTAINERINFO_GATEWAY  + ":32100",
                                                       "http://" + CONTAINERINFO_GATEWAY  + ":32100",
                                                        new ServerPropertiesImpl(null,
                                                                                 CONTAINERINFO_GATEWAY  + ":32100",
                                                                                 "http://" + CONTAINERINFO_GATEWAY  + ":32100")));
        expectedServers.put("9090/udp", new ServerImpl("myserv2",
                                                       "dhcp",
                                                       CONTAINERINFO_GATEWAY  + ":32101",
                                                       "dhcp://" + CONTAINERINFO_GATEWAY  + ":32101/some/path",
                                                       new ServerPropertiesImpl("/some/path",
                                                                                CONTAINERINFO_GATEWAY  + ":32101",
                                                                                "dhcp://" + CONTAINERINFO_GATEWAY  + ":32101/some/path")));

        // when
        final Map<String, ServerImpl> servers = strategy.getServers(containerInfo, DEFAULT_HOSTNAME, serverConfs);

        // then
        assertEquals(servers, expectedServers);
    }

    @Test
    public void shouldAllowToUsePortFromMachineConfigWithoutTransportProtocol() throws Exception {
        // given
        Map<String, List<PortBinding>> ports = getPorts();
        when(networkSettings.getPorts()).thenReturn(ports);

        serverConfs.put("8080",     new ServerConfImpl("myserv1", "8080", "http", "/some"));
        serverConfs.put("9090/udp", new ServerConfImpl("myserv1-tftp", "9090/udp", "tftp", "/path"));

        final HashMap<String, ServerImpl> expectedServers = new HashMap<>();
        expectedServers.put("8080/tcp", new ServerImpl("myserv1",
                                                       "http",
                                                       CONTAINERINFO_GATEWAY + ":32100",
                                                       "http://" + CONTAINERINFO_GATEWAY + ":32100/some",
                                                       new ServerPropertiesImpl("/some",
                                                                                CONTAINERINFO_GATEWAY + ":32100",
                                                                                "http://" + CONTAINERINFO_GATEWAY + ":32100/some")));
        expectedServers.put("9090/udp", new ServerImpl("myserv1-tftp",
                                                       "tftp",
                                                       CONTAINERINFO_GATEWAY  + ":32101",
                                                       "tftp://" + CONTAINERINFO_GATEWAY  + ":32101/path",
                                                       new ServerPropertiesImpl("/path",
                                                                                CONTAINERINFO_GATEWAY  + ":32101",
                                                                                "tftp://" + CONTAINERINFO_GATEWAY  + ":32101/path")));

        // when
        final Map<String, ServerImpl> servers = strategy.getServers(containerInfo, DEFAULT_HOSTNAME, serverConfs);

        // then
        assertEquals(servers, expectedServers);
    }

    @Test
    public void shouldAddRefUrlPathToServerFromLabels() throws Exception {
        // given
        Map<String, List<PortBinding>> ports = getPorts();
        when(networkSettings.getPorts()).thenReturn(ports);
        Map<String, String> labels = new HashMap<>();
        when(containerConfig.getLabels()).thenReturn(labels);
        ports.put("8080/tcp", Collections.singletonList(new PortBinding().withHostIp(ALL_IP_ADDRESS )
                                                                         .withHostPort("32100")));
        ports.put("9090/udp", Collections.singletonList(new PortBinding().withHostIp(ALL_IP_ADDRESS )
                                                                           .withHostPort("32101")));
        labels.put(String.format(SERVER_CONF_LABEL_REF_KEY,      "8080/tcp"), "myserv1");
        labels.put(String.format(SERVER_CONF_LABEL_PROTOCOL_KEY, "8080/tcp"), "http");
        labels.put(String.format(SERVER_CONF_LABEL_PATH_KEY,     "8080/tcp"), "/some/path");

        labels.put(String.format(SERVER_CONF_LABEL_PROTOCOL_KEY, "9090/udp"), "dhcp");
        labels.put(String.format(SERVER_CONF_LABEL_PATH_KEY,     "9090/udp"), "some/path");

        final HashMap<String, ServerImpl> expectedServers = new HashMap<>();
        expectedServers.put("8080/tcp", new ServerImpl("myserv1",
                                                       "http",
                                                       CONTAINERINFO_GATEWAY  + ":32100",
                                                       "http://" + CONTAINERINFO_GATEWAY  + ":32100/some/path",
                                                       new ServerPropertiesImpl("/some/path",
                                                                                CONTAINERINFO_GATEWAY  + ":32100",
                                                                                "http://" + CONTAINERINFO_GATEWAY  + ":32100/some/path")));
        expectedServers.put("9090/udp", new ServerImpl("Server-9090-udp",
                                                       "dhcp",
                                                       CONTAINERINFO_GATEWAY  + ":32101",
                                                       "dhcp://" + CONTAINERINFO_GATEWAY  + ":32101/some/path",
                                                       new ServerPropertiesImpl("some/path",
                                                                                CONTAINERINFO_GATEWAY  + ":32101",
                                                                                "dhcp://" + CONTAINERINFO_GATEWAY  + ":32101/some/path")));

        // when
        final Map<String, ServerImpl> servers = strategy.getServers(containerInfo, DEFAULT_HOSTNAME, serverConfs);

        // then
        assertEquals(servers, expectedServers);
    }

    @Test
    public void shouldAllowToUsePortFromDockerLabelsWithoutTransportProtocol() throws Exception {
        // given
        Map<String, List<PortBinding>> ports = getPorts();
        when(networkSettings.getPorts()).thenReturn(ports);

        labels.put(String.format(SERVER_CONF_LABEL_REF_KEY,      "8080"), "myserv1");
        labels.put(String.format(SERVER_CONF_LABEL_PROTOCOL_KEY, "8080"), "http");

        labels.put(String.format(SERVER_CONF_LABEL_REF_KEY,      "9090/udp"), "myserv1-tftp");
        labels.put(String.format(SERVER_CONF_LABEL_PROTOCOL_KEY, "9090/udp"), "tftp");

        final HashMap<String, ServerImpl> expectedServers = new HashMap<>();
        expectedServers.put("8080/tcp", new ServerImpl("myserv1",
                                                       "http",
                                                       CONTAINERINFO_GATEWAY  + ":32100",
                                                       "http://" + CONTAINERINFO_GATEWAY  + ":32100",
                                                       new ServerPropertiesImpl(null,
                                                                                CONTAINERINFO_GATEWAY  + ":32100",
                                                                                "http://" + CONTAINERINFO_GATEWAY + ":32100")));
        expectedServers.put("9090/udp", new ServerImpl("myserv1-tftp",
                                                       "tftp",
                                                       CONTAINERINFO_GATEWAY  + ":32101",
                                                       "tftp://" + CONTAINERINFO_GATEWAY  + ":32101",
                                                       new ServerPropertiesImpl(null,
                                                                                CONTAINERINFO_GATEWAY  + ":32101",
                                                                                "tftp://" + CONTAINERINFO_GATEWAY + ":32101")));

        // when
        final Map<String, ServerImpl> servers = strategy.getServers(containerInfo, DEFAULT_HOSTNAME, serverConfs);

        // then
        assertEquals(servers, expectedServers);
    }

    @Test
    public void shouldPreferMachineConfOverDockerLabels() throws Exception {
        // given
        Map<String, List<PortBinding>> ports = getPorts();
        when(networkSettings.getPorts()).thenReturn(ports);

        labels.put(String.format(SERVER_CONF_LABEL_REF_KEY,      "8080/tcp"), "myserv1label");
        labels.put(String.format(SERVER_CONF_LABEL_PROTOCOL_KEY, "8080/tcp"), "https");

        labels.put(String.format(SERVER_CONF_LABEL_REF_KEY,      "9090/udp"), "myserv2label");
        labels.put(String.format(SERVER_CONF_LABEL_PROTOCOL_KEY, "9090/udp"), "dhcp");
        labels.put(String.format(SERVER_CONF_LABEL_PATH_KEY,     "9090/udp"), "/path");

        serverConfs.put("8080/tcp", new ServerConfImpl("myserv1conf", "8080/tcp", "http", null));

        final HashMap<String, ServerImpl> expectedServers = new HashMap<>();
        expectedServers.put("8080/tcp", new ServerImpl("myserv1conf",
                                                       "http",
                                                       CONTAINERINFO_GATEWAY  + ":32100",
                                                       "http://" + CONTAINERINFO_GATEWAY  + ":32100",
                                                       new ServerPropertiesImpl(null,
                                                                                CONTAINERINFO_GATEWAY  + ":32100",
                                                                                "http://" + CONTAINERINFO_GATEWAY + ":32100")));
        expectedServers.put("9090/udp", new ServerImpl("myserv2label",
                                                       "dhcp",
                                                       CONTAINERINFO_GATEWAY  + ":32101",
                                                       "dhcp://" + CONTAINERINFO_GATEWAY  + ":32101/path",
                                                       new ServerPropertiesImpl("/path",
                                                                                CONTAINERINFO_GATEWAY  + ":32101",
                                                                                "dhcp://" + CONTAINERINFO_GATEWAY + ":32101/path")));

        // when
        final Map<String, ServerImpl> servers = strategy.getServers(containerInfo, DEFAULT_HOSTNAME, serverConfs);

        // then
        assertEquals(servers, expectedServers);
    }

    @Test
    public void shouldAddPathCorrectlyWithoutLeadingSlash() throws Exception {
        // given
        Map<String, List<PortBinding>> ports = getPorts();
        when(networkSettings.getPorts()).thenReturn(ports);

        serverConfs.put("8080",     new ServerConfImpl("myserv1", "8080", "http", "some"));
        serverConfs.put("9090/udp", new ServerConfImpl("myserv1-tftp", "9090/udp", "tftp", "some/path"));

        final HashMap<String, ServerImpl> expectedServers = new HashMap<>();
        expectedServers.put("8080/tcp", new ServerImpl("myserv1",
                                                       "http",
                                                       CONTAINERINFO_GATEWAY + ":32100",
                                                       "http://" + CONTAINERINFO_GATEWAY + ":32100/some",
                                                       new ServerPropertiesImpl("some",
                                                                                CONTAINERINFO_GATEWAY + ":32100",
                                                                                "http://" + CONTAINERINFO_GATEWAY + ":32100/some")));
        expectedServers.put("9090/udp", new ServerImpl("myserv1-tftp",
                                                       "tftp",
                                                       CONTAINERINFO_GATEWAY  + ":32101",
                                                       "tftp://" + CONTAINERINFO_GATEWAY  + ":32101/some/path",
                                                       new ServerPropertiesImpl("some/path",
                                                                                CONTAINERINFO_GATEWAY  + ":32101",
                                                                                "tftp://" + CONTAINERINFO_GATEWAY  + ":32101/some/path")));

        // when
        final Map<String, ServerImpl> servers = strategy.getServers(containerInfo, DEFAULT_HOSTNAME, serverConfs);

        // then
        assertEquals(servers, expectedServers);
    }
}
