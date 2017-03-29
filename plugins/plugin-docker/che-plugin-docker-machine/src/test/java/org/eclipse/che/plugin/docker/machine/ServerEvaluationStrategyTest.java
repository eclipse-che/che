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
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.plugin.docker.machine.ServerEvaluationStrategy.SERVER_CONF_LABEL_PATH_KEY;
import static org.eclipse.che.plugin.docker.machine.ServerEvaluationStrategy.SERVER_CONF_LABEL_PROTOCOL_KEY;
import static org.eclipse.che.plugin.docker.machine.ServerEvaluationStrategy.SERVER_CONF_LABEL_REF_KEY;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * @author Angel Misevski <amisevsk@redhat.com>
 * @author Alexander Garagatyi
 */
@Listeners(MockitoTestNGListener.class)
public class ServerEvaluationStrategyTest {

    private static final String ALL_IP_ADDRESS   = "0.0.0.0";
    private static final String DEFAULT_HOSTNAME = "localhost";

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
        strategy = spy(new TestServerEvaluationStrategyImpl());
        serverConfs = new HashMap<>();
        labels = new HashMap<>();

        when(containerInfo.getConfig()).thenReturn(containerConfig);
        when(containerInfo.getNetworkSettings()).thenReturn(networkSettings);
        when(containerConfig.getLabels()).thenReturn(labels);
    }

    @Test
    public void shouldConvertAddressAndExposedPortsInMapOfExposedPortToAddressPort() throws Exception {
        // given
        Map<String, List<PortBinding>> ports = new HashMap<>();
        ports.put("8080/tcp", Collections.singletonList(new PortBinding().withHostIp(ALL_IP_ADDRESS)
                                                                         .withHostPort("32100")));
        ports.put("9090/udp", Collections.singletonList(new PortBinding().withHostIp(ALL_IP_ADDRESS)
                                                                         .withHostPort("32101")));
        Map<String, String> expected = new HashMap<>();
        expected.put("8080/tcp", DEFAULT_HOSTNAME + ":" + "32100");
        expected.put("9090/udp", DEFAULT_HOSTNAME + ":" + "32101");

        // when
        Map<String, String> actual = strategy.getExposedPortsToAddressPorts(DEFAULT_HOSTNAME, ports);

        // then
        assertEquals(actual, expected);
    }

    @Test
    public void shouldIgnoreMultiplePortBindingEntries() throws Exception {
        // given
        Map<String, List<PortBinding>> ports = new HashMap<>();
        ports.put("8080/tcp", Arrays.asList(new PortBinding().withHostIp(ALL_IP_ADDRESS)
                                                             .withHostPort("32100"),
                                            new PortBinding().withHostIp(DEFAULT_HOSTNAME)
                                                             .withHostPort("32102")));
        ports.put("9090/udp", Arrays.asList(new PortBinding().withHostIp(ALL_IP_ADDRESS)
                                                             .withHostPort("32101"),
                                            new PortBinding().withHostIp(ALL_IP_ADDRESS)
                                                             .withHostPort("32103"),
                                            new PortBinding().withHostIp(DEFAULT_HOSTNAME)
                                                             .withHostPort("32104")));
        Map<String, String> expected = new HashMap<>();
        expected.put("8080/tcp", DEFAULT_HOSTNAME + ":" + "32100");
        expected.put("9090/udp", DEFAULT_HOSTNAME + ":" + "32101");

        // when
        Map<String, String> actual = strategy.getExposedPortsToAddressPorts(DEFAULT_HOSTNAME, ports);

        // then
        assertEquals(actual, expected);
    }

    @Test
    public void shouldReturnServerForEveryExposedPort() throws Exception {
        // given
        Map<String, List<PortBinding>> ports = prepareStrategyAndContainerInfoMocks();
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
        prepareStrategyAndContainerInfoMocks();

        final HashMap<String, ServerImpl> expectedServers = new HashMap<>();
        expectedServers.put("8080/tcp", new ServerImpl("Server-8080-tcp",
                                                       null,
                                                       DEFAULT_HOSTNAME + ":32100",
                                                       null,
                                                       new ServerPropertiesImpl(null, DEFAULT_HOSTNAME + ":32100",
                                                                                null)));
        expectedServers.put("9090/udp", new ServerImpl("Server-9090-udp",
                                                       null,
                                                       DEFAULT_HOSTNAME + ":32101",
                                                       null,
                                                       new ServerPropertiesImpl(null, DEFAULT_HOSTNAME + ":32101",
                                                                                null)));
        // when
        final Map<String, ServerImpl> servers = strategy.getServers(containerInfo, DEFAULT_HOSTNAME, serverConfs);

        // then
        assertEquals(servers, expectedServers);
    }

    @Test
    public void shouldAddRefUrlProtocolPathToServerFromMachineConfig() throws Exception {
        // given
        prepareStrategyAndContainerInfoMocks();

        serverConfs.put("8080/tcp", new ServerConfImpl("myserv1", "8080/tcp", "http", null));
        serverConfs.put("9090/udp", new ServerConfImpl("myserv2", "9090/udp", "dhcp", "/some/path"));

        final HashMap<String, ServerImpl> expectedServers = new HashMap<>();
        expectedServers.put("8080/tcp", new ServerImpl("myserv1",
                                                       "http",
                                                       DEFAULT_HOSTNAME + ":32100",
                                                       "http://" + DEFAULT_HOSTNAME + ":32100",
                                                       new ServerPropertiesImpl(null,
                                                                                DEFAULT_HOSTNAME + ":32100",
                                                                                "http://" + DEFAULT_HOSTNAME +
                                                                                ":32100")));
        expectedServers.put("9090/udp", new ServerImpl("myserv2",
                                                       "dhcp",
                                                       DEFAULT_HOSTNAME + ":32101",
                                                       "dhcp://" + DEFAULT_HOSTNAME + ":32101/some/path",
                                                       new ServerPropertiesImpl("/some/path",
                                                                                DEFAULT_HOSTNAME + ":32101",
                                                                                "dhcp://" + DEFAULT_HOSTNAME +
                                                                                ":32101/some/path")));

        // when
        final Map<String, ServerImpl> servers = strategy.getServers(containerInfo, DEFAULT_HOSTNAME, serverConfs);

        // then
        assertEquals(servers, expectedServers);
    }

    @Test
    public void shouldAllowToUsePortFromMachineConfigWithoutTransportProtocol() throws Exception {
        // given
        prepareStrategyAndContainerInfoMocks();

        serverConfs.put("8080", new ServerConfImpl("myserv1", "8080", "http", "/some"));
        serverConfs.put("9090/udp", new ServerConfImpl("myserv1-tftp", "9090/udp", "tftp", "/path"));

        final HashMap<String, ServerImpl> expectedServers = new HashMap<>();
        expectedServers.put("8080/tcp", new ServerImpl("myserv1",
                                                       "http",
                                                       DEFAULT_HOSTNAME + ":32100",
                                                       "http://" + DEFAULT_HOSTNAME + ":32100/some",
                                                       new ServerPropertiesImpl("/some",
                                                                                DEFAULT_HOSTNAME + ":32100",
                                                                                "http://" + DEFAULT_HOSTNAME +
                                                                                ":32100/some")));
        expectedServers.put("9090/udp", new ServerImpl("myserv1-tftp",
                                                       "tftp",
                                                       DEFAULT_HOSTNAME + ":32101",
                                                       "tftp://" + DEFAULT_HOSTNAME + ":32101/path",
                                                       new ServerPropertiesImpl("/path",
                                                                                DEFAULT_HOSTNAME + ":32101",
                                                                                "tftp://" + DEFAULT_HOSTNAME +
                                                                                ":32101/path")));

        // when
        final Map<String, ServerImpl> servers = strategy.getServers(containerInfo, DEFAULT_HOSTNAME, serverConfs);

        // then
        assertEquals(servers, expectedServers);
    }

    @Test
    public void shouldAddRefUrlPathToServerFromLabels() throws Exception {
        // given
        Map<String, List<PortBinding>> ports = prepareStrategyAndContainerInfoMocks();
        Map<String, String> labels = new HashMap<>();
        when(containerConfig.getLabels()).thenReturn(labels);
        ports.put("8080/tcp", Collections.singletonList(new PortBinding().withHostIp(ALL_IP_ADDRESS)
                                                                         .withHostPort("32100")));
        ports.put("9090/udp", Collections.singletonList(new PortBinding().withHostIp(ALL_IP_ADDRESS)
                                                                         .withHostPort("32101")));
        labels.put(String.format(SERVER_CONF_LABEL_REF_KEY, "8080/tcp"), "myserv1");
        labels.put(String.format(SERVER_CONF_LABEL_PROTOCOL_KEY, "8080/tcp"), "http");
        labels.put(String.format(SERVER_CONF_LABEL_PATH_KEY, "8080/tcp"), "/some/path");

        labels.put(String.format(SERVER_CONF_LABEL_PROTOCOL_KEY, "9090/udp"), "dhcp");
        labels.put(String.format(SERVER_CONF_LABEL_PATH_KEY, "9090/udp"), "some/path");

        final HashMap<String, ServerImpl> expectedServers = new HashMap<>();
        expectedServers.put("8080/tcp", new ServerImpl("myserv1",
                                                       "http",
                                                       DEFAULT_HOSTNAME + ":32100",
                                                       "http://" + DEFAULT_HOSTNAME + ":32100/some/path",
                                                       new ServerPropertiesImpl("/some/path",
                                                                                DEFAULT_HOSTNAME + ":32100",
                                                                                "http://" + DEFAULT_HOSTNAME +
                                                                                ":32100/some/path")));
        expectedServers.put("9090/udp", new ServerImpl("Server-9090-udp",
                                                       "dhcp",
                                                       DEFAULT_HOSTNAME + ":32101",
                                                       "dhcp://" + DEFAULT_HOSTNAME + ":32101/some/path",
                                                       new ServerPropertiesImpl("some/path",
                                                                                DEFAULT_HOSTNAME + ":32101",
                                                                                "dhcp://" + DEFAULT_HOSTNAME +
                                                                                ":32101/some/path")));

        // when
        final Map<String, ServerImpl> servers = strategy.getServers(containerInfo, DEFAULT_HOSTNAME, serverConfs);

        // then
        assertEquals(servers, expectedServers);
    }

    @Test
    public void shouldAllowToUsePortFromDockerLabelsWithoutTransportProtocol() throws Exception {
        // given
        prepareStrategyAndContainerInfoMocks();

        labels.put(String.format(SERVER_CONF_LABEL_REF_KEY, "8080"), "myserv1");
        labels.put(String.format(SERVER_CONF_LABEL_PROTOCOL_KEY, "8080"), "http");

        labels.put(String.format(SERVER_CONF_LABEL_REF_KEY, "9090/udp"), "myserv1-tftp");
        labels.put(String.format(SERVER_CONF_LABEL_PROTOCOL_KEY, "9090/udp"), "tftp");

        final HashMap<String, ServerImpl> expectedServers = new HashMap<>();
        expectedServers.put("8080/tcp", new ServerImpl("myserv1",
                                                       "http",
                                                       DEFAULT_HOSTNAME + ":32100",
                                                       "http://" + DEFAULT_HOSTNAME + ":32100",
                                                       new ServerPropertiesImpl(null,
                                                                                DEFAULT_HOSTNAME + ":32100",
                                                                                "http://" + DEFAULT_HOSTNAME +
                                                                                ":32100")));
        expectedServers.put("9090/udp", new ServerImpl("myserv1-tftp",
                                                       "tftp",
                                                       DEFAULT_HOSTNAME + ":32101",
                                                       "tftp://" + DEFAULT_HOSTNAME + ":32101",
                                                       new ServerPropertiesImpl(null,
                                                                                DEFAULT_HOSTNAME + ":32101",
                                                                                "tftp://" + DEFAULT_HOSTNAME +
                                                                                ":32101")));

        // when
        final Map<String, ServerImpl> servers = strategy.getServers(containerInfo, DEFAULT_HOSTNAME, serverConfs);

        // then
        assertEquals(servers, expectedServers);
    }

    @Test
    public void shouldPreferMachineConfOverDockerLabels() throws Exception {
        // given
        prepareStrategyAndContainerInfoMocks();

        labels.put(String.format(SERVER_CONF_LABEL_REF_KEY, "8080/tcp"), "myserv1label");
        labels.put(String.format(SERVER_CONF_LABEL_PROTOCOL_KEY, "8080/tcp"), "https");

        labels.put(String.format(SERVER_CONF_LABEL_REF_KEY, "9090/udp"), "myserv2label");
        labels.put(String.format(SERVER_CONF_LABEL_PROTOCOL_KEY, "9090/udp"), "dhcp");
        labels.put(String.format(SERVER_CONF_LABEL_PATH_KEY, "9090/udp"), "/path");

        serverConfs.put("8080/tcp", new ServerConfImpl("myserv1conf", "8080/tcp", "http", null));

        final HashMap<String, ServerImpl> expectedServers = new HashMap<>();
        expectedServers.put("8080/tcp", new ServerImpl("myserv1conf",
                                                       "http",
                                                       DEFAULT_HOSTNAME + ":32100",
                                                       "http://" + DEFAULT_HOSTNAME + ":32100",
                                                       new ServerPropertiesImpl(null,
                                                                                DEFAULT_HOSTNAME + ":32100",
                                                                                "http://" + DEFAULT_HOSTNAME +
                                                                                ":32100")));
        expectedServers.put("9090/udp", new ServerImpl("myserv2label",
                                                       "dhcp",
                                                       DEFAULT_HOSTNAME + ":32101",
                                                       "dhcp://" + DEFAULT_HOSTNAME + ":32101/path",
                                                       new ServerPropertiesImpl("/path",
                                                                                DEFAULT_HOSTNAME + ":32101",
                                                                                "dhcp://" + DEFAULT_HOSTNAME +
                                                                                ":32101/path")));

        // when
        final Map<String, ServerImpl> servers = strategy.getServers(containerInfo, DEFAULT_HOSTNAME, serverConfs);

        // then
        assertEquals(servers, expectedServers);
    }

    @Test
    public void shouldAddPathCorrectlyWithoutLeadingSlash() throws Exception {
        // given
        prepareStrategyAndContainerInfoMocks();

        serverConfs.put("8080", new ServerConfImpl("myserv1", "8080", "http", "some"));
        serverConfs.put("9090/udp", new ServerConfImpl("myserv1-tftp", "9090/udp", "tftp", "some/path"));

        final HashMap<String, ServerImpl> expectedServers = new HashMap<>();
        expectedServers.put("8080/tcp", new ServerImpl("myserv1",
                                                       "http",
                                                       DEFAULT_HOSTNAME + ":32100",
                                                       "http://" + DEFAULT_HOSTNAME + ":32100/some",
                                                       new ServerPropertiesImpl("some",
                                                                                DEFAULT_HOSTNAME + ":32100",
                                                                                "http://" + DEFAULT_HOSTNAME +
                                                                                ":32100/some")));
        expectedServers.put("9090/udp", new ServerImpl("myserv1-tftp",
                                                       "tftp",
                                                       DEFAULT_HOSTNAME + ":32101",
                                                       "tftp://" + DEFAULT_HOSTNAME + ":32101/some/path",
                                                       new ServerPropertiesImpl("some/path",
                                                                                DEFAULT_HOSTNAME + ":32101",
                                                                                "tftp://" + DEFAULT_HOSTNAME +
                                                                                ":32101/some/path")));

        // when
        final Map<String, ServerImpl> servers = strategy.getServers(containerInfo, DEFAULT_HOSTNAME, serverConfs);

        // then
        assertEquals(servers, expectedServers);
    }

    private Map<String, List<PortBinding>> prepareStrategyAndContainerInfoMocks() {
        Map<String, List<PortBinding>> ports = new HashMap<>();
        ports.put("8080/tcp", Collections.singletonList(new PortBinding().withHostIp(ALL_IP_ADDRESS)
                                                                         .withHostPort("32100")));
        ports.put("9090/udp", Collections.singletonList(new PortBinding().withHostIp(ALL_IP_ADDRESS)
                                                                         .withHostPort("32101")));
        when(networkSettings.getPorts()).thenReturn(ports);
        Map<String, String> exposedPortsToAddressPorts =
                strategy.getExposedPortsToAddressPorts(DEFAULT_HOSTNAME, ports);
        when(strategy.getExternalAddressesAndPorts(containerInfo, DEFAULT_HOSTNAME))
                .thenReturn(exposedPortsToAddressPorts);
        when(strategy.getInternalAddressesAndPorts(containerInfo, DEFAULT_HOSTNAME))
                .thenReturn(exposedPortsToAddressPorts);

        return ports;
    }

    private static class TestServerEvaluationStrategyImpl extends ServerEvaluationStrategy {
        @Override
        protected Map<String, String> getInternalAddressesAndPorts(ContainerInfo containerInfo,
                                                                   String internalAddress) {
            return null;
        }

        @Override
        protected Map<String, String> getExternalAddressesAndPorts(ContainerInfo containerInfo,
                                                                   String internalAddress) {
            return null;
        }

        @Override
        protected boolean useHttpsForExternalUrls() {
            return false;
        }
    }
}
