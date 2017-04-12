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
package org.eclipse.che.workspace.infrastructure.docker.old;

import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;

/**
 * @author Angel Misevski <amisevsk@redhat.com>
 * @author Alexander Garagatyi
 */
@Listeners(MockitoTestNGListener.class)
public class DefaultServerEvaluationStrategyTest {
/*
    private static final String CHE_DOCKER_IP            = "container-host.com";
    private static final String CHE_DOCKER_IP_EXTERNAL   = "container-host-ext.com";
    private static final String ALL_IP_ADDRESS           = "0.0.0.0";
    private static final String DEFAULT_HOSTNAME         = "localhost";

    @Mock
    private ContainerInfo   containerInfo;
    @Mock
    private ContainerConfig containerConfig;
    @Mock
    private NetworkSettings networkSettings;

    private ServerEvaluationStrategy strategy;

    private Map<String, ServerConfImpl> serverConfs;

    @BeforeMethod
    public void setUp() {

        serverConfs = new HashMap<>();
        serverConfs.put("4301/tcp", new ServerConfImpl("sysServer1-tcp", "4301/tcp", "http", "/some/path1"));
        serverConfs.put("4305/udp", new ServerConfImpl("devSysServer1-udp", "4305/udp", null, "some/path4"));

        Map<String, List<PortBinding>> ports = new HashMap<>();
        ports.put("4301/tcp", Collections.singletonList(new PortBinding().withHostIp(ALL_IP_ADDRESS )
                                                                         .withHostPort("32100")));
        ports.put("4305/udp", Collections.singletonList(new PortBinding().withHostIp(ALL_IP_ADDRESS )
                                                                         .withHostPort("32103")));

        when(containerInfo.getNetworkSettings()).thenReturn(networkSettings);
        when(networkSettings.getPorts()).thenReturn(ports);
        when(containerInfo.getConfig()).thenReturn(containerConfig);
        when(containerConfig.getLabels()).thenReturn(Collections.emptyMap());
    }

    /**
     * Test: If che.docker.ip is null, and che.docker.ip.external is null or empty, should use provided
     *       internalHostname value as internal and external addresses in this case.
     * @throws Exception
     */
/*
    @Test
    public void defaultStrategyShouldUseInternalHostWhenBothIpPropertyAreNull() throws Exception {
        // given
        strategy = new DefaultServerEvaluationStrategy(null, null);

        final Map<String, ServerImpl> expectedServers = getExpectedServers(DEFAULT_HOSTNAME,
                                                                           DEFAULT_HOSTNAME
        );

        // when
        final Map<String, ServerImpl> servers = strategy.getServers(containerInfo,
                                                                    DEFAULT_HOSTNAME,
                                                                    serverConfs);

        // then
        assertEquals(servers, expectedServers);
    }

    /**
     * Test: If che.docker.ip.external and che.docker.ip are not null, these values should take precedence for external and internal addresses.
     * @throws Exception
     */
/*
    @Test
    public void defaultStrategyShouldUseIpPropertiesWhenAvailable() throws Exception {
        // given
        strategy = new DefaultServerEvaluationStrategy(CHE_DOCKER_IP, CHE_DOCKER_IP_EXTERNAL);

        final Map<String, ServerImpl> expectedServers = getExpectedServers(CHE_DOCKER_IP_EXTERNAL,
                                                                           CHE_DOCKER_IP
        );

        // when
        final Map<String, ServerImpl> servers = strategy.getServers(containerInfo,
                                                                    DEFAULT_HOSTNAME,
                                                                    serverConfs);

        // then
        assertEquals(servers, expectedServers);
    }

    /**
     * Test: If che.docker.ip.external is null, che.docker.ip is used as external address if it is not null.
     * @throws Exception
     */
/*
    @Test
    public void defaultStrategyShouldUseInternalIpPropertyAsExternalIfExternalIpPropertyIsNull() throws Exception {
        // given
        strategy = new DefaultServerEvaluationStrategy(CHE_DOCKER_IP, null);

        final Map<String, ServerImpl> expectedServers = getExpectedServers(CHE_DOCKER_IP,
                                                                           CHE_DOCKER_IP);

        // when
        final Map<String, ServerImpl> servers = strategy.getServers(containerInfo,
                                                                    DEFAULT_HOSTNAME,
                                                                    serverConfs);

        // then
        assertEquals(servers, expectedServers);
    }

    /**
     * Test: If che.docker.ip.external is not null and che.docker.ip is null,
     * internal address is taken from provided internalAddress and external address is taken from property.
     * @throws Exception
     */
/*
    @Test
    public void defaultStrategyShouldUseProvidedInternalIpIfInternalIpPropertyIsNull() throws Exception {
        // given
        strategy = new DefaultServerEvaluationStrategy(null, CHE_DOCKER_IP_EXTERNAL);

        final Map<String, ServerImpl> expectedServers = getExpectedServers(CHE_DOCKER_IP_EXTERNAL,
                                                                           DEFAULT_HOSTNAME);

        // when
        final Map<String, ServerImpl> servers = strategy.getServers(containerInfo,
                                                                    DEFAULT_HOSTNAME,
                                                                    serverConfs);

        // then
        assertEquals(servers, expectedServers);
    }

    private Map<String, ServerImpl> getExpectedServers(String externalAddress,
                                                       String internalAddress) {
        String port1;
        String port2;
        port1 = ":32100";
        port2 = ":32103";
        Map<String, ServerImpl> expectedServers = new HashMap<>();
        expectedServers.put("4301/tcp", new ServerImpl("sysServer1-tcp",
                "http",
                externalAddress + ":32100",
                "http://" + externalAddress + ":32100/some/path1",
                new ServerPropertiesImpl("/some/path1",
                                         internalAddress + port1,
                                         "http://" + internalAddress + port1 + "/some/path1")));
        expectedServers.put("4305/udp", new ServerImpl("devSysServer1-udp",
                null,
                externalAddress + ":32103",
                null,
                new ServerPropertiesImpl("some/path4",
                                         internalAddress + port2,
                                         null)));
        return expectedServers;
    }
    */
}
