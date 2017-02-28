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

import org.eclipse.che.api.core.model.machine.MachineConfig;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

@Listeners(MockitoTestNGListener.class)
public class LocalDockerSinglePortServerEvaluationStrategyTest {

    private static final String CHE_DOCKER_IP_EXTERNAL   = "container-host-ext.com";
    private static final String ALL_IP_ADDRESS           = "0.0.0.0";
    private static final String CONTAINERCONFIG_HOSTNAME = "che-ws-y6jwknht0efzczit-4086112300-fm0aj";
    private static final String WORKSPACE_ID             = "79rfwhqaztq2ru2k";

    @Mock
    private ContainerInfo   containerInfo;
    @Mock
    private ContainerConfig containerConfig;
    @Mock
    private NetworkSettings networkSettings;

    private ServerEvaluationStrategy strategy;

    private Map<String, ServerConfImpl> serverConfs;

    private Map<String, List<PortBinding>> ports;

    private Map<String, String> labels;

    private String[] env;

    @BeforeMethod
    public void setUp() {

        serverConfs = new HashMap<>();
        serverConfs.put("4301/tcp", new ServerConfImpl("sysServer1-tcp", "4301/tcp", "http", "/some/path1"));
        serverConfs.put("4305/udp", new ServerConfImpl("devSysServer1-udp", "4305/udp", null, "some/path4"));

        ports = new HashMap<>();
        ports.put("4301/tcp", Collections.singletonList(new PortBinding().withHostIp(ALL_IP_ADDRESS )
                                                                .withHostPort("32100")));
        ports.put("4305/udp", Collections.singletonList(new PortBinding().withHostIp(ALL_IP_ADDRESS )
                                                                .withHostPort("32103")));

        labels = new HashMap<>();
        labels.put("che:server:4301/tcp:ref", "sysServer1-tcp");
        labels.put("che:server:4305/udp:ref", "devSysServer1-udp");

        env = new String[]{"CHE_WORKSPACE_ID="+ WORKSPACE_ID};

        when(containerInfo.getNetworkSettings()).thenReturn(networkSettings);
        when(networkSettings.getIpAddress()).thenReturn(CONTAINERCONFIG_HOSTNAME);
        when(networkSettings.getPorts()).thenReturn(ports);
        when(containerInfo.getConfig()).thenReturn(containerConfig);
        when(containerConfig.getHostname()).thenReturn(CONTAINERCONFIG_HOSTNAME);
        when(containerConfig.getEnv()).thenReturn(env);
        when(containerConfig.getLabels()).thenReturn(labels);
    }

    /**
     * Test: single port strategy should use .
     * @throws Exception
     */
    @Test
    public void shouldUseServerRefToBuildAddressWhenAvailable() throws Exception {
        // given
        strategy = new LocalDockerSinglePortServerEvaluationStrategy(null, null);

        final Map<String, ServerImpl> expectedServers = getExpectedServers(CHE_DOCKER_IP_EXTERNAL,
                                                                           CONTAINERCONFIG_HOSTNAME,
                                                                           true);

        // when
        final Map<String, ServerImpl> servers = strategy.getServers(containerInfo,
                                                                    CHE_DOCKER_IP_EXTERNAL,
                                                                    serverConfs);

        // then
        assertEquals(servers, expectedServers);
    }

    private Map<String, ServerImpl> getExpectedServers(String externalAddress,
                                                       String internalAddress,
                                                       boolean useExposedPorts) {
        String port1;
        String port2;
        if (useExposedPorts) {
            port1 = ":4301";
            port2 = ":4305";
        } else {
            port1 = ":32100";
            port2 = ":32103";
        }
        Map<String, ServerImpl> expectedServers = new HashMap<>();
        expectedServers.put("4301/tcp", new ServerImpl("sysServer1-tcp",
                "http",
                "sysServer1-tcp." + WORKSPACE_ID + "." + externalAddress,
                "http://" + "sysServer1-tcp." + WORKSPACE_ID + "." + externalAddress + "/some/path1",
                new ServerPropertiesImpl("/some/path1",
                                         internalAddress + port1,
                                         "http://" + internalAddress + port1 + "/some/path1")));
        expectedServers.put("4305/udp", new ServerImpl("devSysServer1-udp",
                null,
                "devSysServer1-udp." + WORKSPACE_ID + "." + externalAddress,
                null,
                new ServerPropertiesImpl("some/path4",
                                         internalAddress + port2,
                                         null)));
        return expectedServers;
    }

}
