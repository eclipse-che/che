/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.docker.machine;

import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.ServerConf;
import org.eclipse.che.api.machine.server.model.impl.ServerConfImpl;
import org.eclipse.che.api.machine.server.model.impl.ServerImpl;
import org.eclipse.che.plugin.docker.client.json.ContainerConfig;
import org.eclipse.che.plugin.docker.client.json.ContainerInfo;
import org.eclipse.che.plugin.docker.client.json.NetworkSettings;
import org.eclipse.che.plugin.docker.client.json.PortBinding;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.eclipse.che.plugin.docker.machine.DockerInstanceRuntimeInfo.SERVER_CONF_LABEL_PREFIX;
import static org.eclipse.che.plugin.docker.machine.DockerInstanceRuntimeInfo.SERVER_CONF_LABEL_PROTOCOL_SUFFIX;
import static org.eclipse.che.plugin.docker.machine.DockerInstanceRuntimeInfo.SERVER_CONF_LABEL_REF_SUFFIX;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

@Listeners(MockitoTestNGListener.class)
public class DockerInstanceRuntimeInfoTest {
    private static final String CONTAINER_HOST  = "container-host.com";
    private static final String DEFAULT_ADDRESS = "192.168.1.1";

    @Mock
    private ContainerInfo   containerInfo;
    @Mock
    private MachineConfig   machineConfig;
    @Mock
    private ContainerConfig containerConfig;
    @Mock
    private NetworkSettings networkSettings;

    private DockerInstanceRuntimeInfo runtimeInfo;

    @BeforeMethod
    public void setUp() {
        runtimeInfo = new DockerInstanceRuntimeInfo(containerInfo,
                                                    CONTAINER_HOST,
                                                    machineConfig,
                                                    Collections.emptySet(),
                                                    Collections.emptySet());

        when(containerInfo.getConfig()).thenReturn(containerConfig);
        when(containerInfo.getNetworkSettings()).thenReturn(networkSettings);
        when(machineConfig.getServers()).thenReturn(Collections.emptyList());
        when(containerConfig.getLabels()).thenReturn(Collections.emptyMap());
    }

    @Test
    public void shouldReturnEnvVars() throws Exception {
        // given
        Map<String, String> expectedVariables = new HashMap<>();
        expectedVariables.put("env_var1", "value1");
        expectedVariables.put("env_var2", "value2");
        expectedVariables.put("env_var3", "value3");

        when(containerConfig.getEnv()).thenReturn(expectedVariables.entrySet()
                                                                   .stream()
                                                                   .map(stringStringEntry -> stringStringEntry.getKey() +
                                                                                             "=" +
                                                                                             stringStringEntry.getValue())
                                                                   .collect(Collectors.toList())
                                                                   .toArray(new String[expectedVariables.size()]));

        // when
        final Map<String, String> envVariables = runtimeInfo.getEnvVariables();

        // then
        assertEquals(envVariables, expectedVariables);
    }

    @Test
    public void shouldReturnEmptyMapIfNoEnvVariablesFound() throws Exception {
        when(containerConfig.getEnv()).thenReturn(new String[0]);

        assertEquals(runtimeInfo.getEnvVariables(), Collections.emptyMap());
    }

    @Test
    public void shouldReturnProjectsRoot() throws Exception {
        final String projectsRoot = "/testProjectRoot";
        final String[] envVars = {
                "var1=value1",
                "var2=value2",
                DockerInstanceRuntimeInfo.PROJECTS_ROOT_VARIABLE + "=" + projectsRoot,
                "var3=value3"
        };
        when(containerConfig.getEnv()).thenReturn(envVars);

        assertEquals(runtimeInfo.projectsRoot(), projectsRoot);
    }

    @Test
    public void shouldReturnNullProjectsRootIfNoAppropriateEnvVarFound() throws Exception {
        final String[] envVars = {
                "var1=value1",
                "var2=value2",
                "var3=value3"
        };
        when(containerConfig.getEnv()).thenReturn(envVars);

        assertEquals(runtimeInfo.projectsRoot(), null);
    }

    @Test
    public void shouldReturnServerForEveryExposedPort() throws Exception {
        // given
        Map<String, List<PortBinding>> ports = new HashMap<>();
        when(networkSettings.getPorts()).thenReturn(ports);
        ports.put("8080/tcp", Collections.singletonList(new PortBinding().withHostIp(DEFAULT_ADDRESS)
                                                                         .withHostPort("32100")));
        ports.put("100100/udp", Collections.singletonList(new PortBinding().withHostIp(DEFAULT_ADDRESS)
                                                                           .withHostPort("32101")));
        ports.put("8080/udp", Collections.singletonList(new PortBinding().withHostIp(DEFAULT_ADDRESS)
                                                                         .withHostPort("32102")));

        // when
        final Map<String, ServerImpl> servers = runtimeInfo.getServers();

        // then
        assertEquals(servers.keySet(), ports.keySet());
    }

    @Test
    public void shouldAddDefaultReferenceIfReferenceIsNotSet() throws Exception {
        // given
        Map<String, List<PortBinding>> ports = new HashMap<>();
        when(networkSettings.getPorts()).thenReturn(ports);
        ports.put("8080/tcp", Collections.singletonList(new PortBinding().withHostIp(DEFAULT_ADDRESS)
                                                                         .withHostPort("32100")));
        ports.put("100100/udp", Collections.singletonList(new PortBinding().withHostIp(DEFAULT_ADDRESS)
                                                                           .withHostPort("32101")));
        ports.put("8080/udp", Collections.singletonList(new PortBinding().withHostIp(DEFAULT_ADDRESS)
                                                                         .withHostPort("32102")));

        // when
        final Map<String, ServerImpl> servers = runtimeInfo.getServers();

        // then
        assertEquals(servers.get("8080/tcp"), new ServerImpl("Server-8080-tcp",
                                                             CONTAINER_HOST + ":32100",
                                                             null));
        assertEquals(servers.get("100100/udp"), new ServerImpl("Server-100100-udp",
                                                               CONTAINER_HOST + ":32101",
                                                               null));
        assertEquals(servers.get("8080/udp"), new ServerImpl("Server-8080-udp",
                                                             CONTAINER_HOST + ":32102",
                                                             null));
    }

    @Test
    public void shouldAddRefAndUrlToServerFromMachineConfig() throws Exception {
        // given
        Map<String, List<PortBinding>> ports = new HashMap<>();
        when(networkSettings.getPorts()).thenReturn(ports);
        List<ServerConfImpl> serversConfigs = new ArrayList<>();
        doReturn(serversConfigs).when(machineConfig).getServers();
        ports.put("8080/tcp", Collections.singletonList(new PortBinding().withHostIp(DEFAULT_ADDRESS)
                                                                         .withHostPort("32100")));
        ports.put("100100/udp", Collections.singletonList(new PortBinding().withHostIp(DEFAULT_ADDRESS)
                                                                           .withHostPort("32101")));
        ports.put("8080/udp", Collections.singletonList(new PortBinding().withHostIp(DEFAULT_ADDRESS)
                                                                         .withHostPort("32102")));
        ports.put("8000/tcp", Collections.singletonList(new PortBinding().withHostIp(DEFAULT_ADDRESS)
                                                                         .withHostPort("32103")));
        serversConfigs.add(new ServerConfImpl("myserv1", "8080/tcp", "http"));
        serversConfigs.add(new ServerConfImpl("myserv1-tftp", "8080/udp", "tftp"));
        serversConfigs.add(new ServerConfImpl("myserv2", "100100/udp", "dhcp"));
        serversConfigs.add(new ServerConfImpl(null, "8000/tcp", "tcp"));
        runtimeInfo = new DockerInstanceRuntimeInfo(containerInfo,
                                                    CONTAINER_HOST,
                                                    machineConfig,
                                                    Collections.emptySet(),
                                                    Collections.emptySet());

        // when
        final Map<String, ServerImpl> servers = runtimeInfo.getServers();

        // then
        assertEquals(servers.get("8080/tcp"), new ServerImpl("myserv1",
                                                             CONTAINER_HOST + ":32100",
                                                             "http://" + CONTAINER_HOST + ":32100"));
        assertEquals(servers.get("100100/udp"), new ServerImpl("myserv2",
                                                               CONTAINER_HOST + ":32101",
                                                               "dhcp://" + CONTAINER_HOST + ":32101"));
        assertEquals(servers.get("8080/udp"), new ServerImpl("myserv1-tftp",
                                                             CONTAINER_HOST + ":32102",
                                                             "tftp://" + CONTAINER_HOST + ":32102"));
        assertEquals(servers.get("8000/tcp"), new ServerImpl("Server-8000-tcp",
                                                             CONTAINER_HOST + ":32103",
                                                             "tcp://" + CONTAINER_HOST + ":32103"));
    }

    @Test
    public void shouldAllowToUsePortFromMachineConfigWithoutTransportProtocol() throws Exception {
        // given
        Map<String, List<PortBinding>> ports = new HashMap<>();
        when(networkSettings.getPorts()).thenReturn(ports);
        List<ServerConfImpl> serversConfigs = new ArrayList<>();
        doReturn(serversConfigs).when(machineConfig).getServers();
        ports.put("8080/tcp", Collections.singletonList(new PortBinding().withHostIp(DEFAULT_ADDRESS)
                                                                         .withHostPort("32100")));
        ports.put("8080/udp", Collections.singletonList(new PortBinding().withHostIp(DEFAULT_ADDRESS)
                                                                         .withHostPort("32102")));
        serversConfigs.add(new ServerConfImpl("myserv1", "8080", "http"));
        serversConfigs.add(new ServerConfImpl("myserv1-tftp", "8080/udp", "tftp"));
        runtimeInfo = new DockerInstanceRuntimeInfo(containerInfo,
                                                    CONTAINER_HOST,
                                                    machineConfig,
                                                    Collections.emptySet(),
                                                    Collections.emptySet());

        // when
        final Map<String, ServerImpl> servers = runtimeInfo.getServers();

        // then
        assertEquals(servers.get("8080/tcp"), new ServerImpl("myserv1",
                                                             CONTAINER_HOST + ":32100",
                                                             "http://" + CONTAINER_HOST + ":32100"));
        assertEquals(servers.get("8080/udp"), new ServerImpl("myserv1-tftp",
                                                             CONTAINER_HOST + ":32102",
                                                             "tftp://" + CONTAINER_HOST + ":32102"));
    }

    @Test
    public void shouldAddRefAndUrlToServerFromLabels() throws Exception {
        // given
        runtimeInfo = new DockerInstanceRuntimeInfo(containerInfo,
                                                    CONTAINER_HOST,
                                                    machineConfig,
                                                    Collections.emptySet(),
                                                    Collections.emptySet());
        Map<String, List<PortBinding>> ports = new HashMap<>();
        when(networkSettings.getPorts()).thenReturn(ports);
        Map<String, String> labels = new HashMap<>();
        when(containerConfig.getLabels()).thenReturn(labels);
        ports.put("8080/tcp", Collections.singletonList(new PortBinding().withHostIp(DEFAULT_ADDRESS)
                                                                         .withHostPort("32100")));
        ports.put("100100/udp", Collections.singletonList(new PortBinding().withHostIp(DEFAULT_ADDRESS)
                                                                           .withHostPort("32101")));
        ports.put("8080/udp", Collections.singletonList(new PortBinding().withHostIp(DEFAULT_ADDRESS)
                                                                         .withHostPort("32102")));
        ports.put("8000/tcp", Collections.singletonList(new PortBinding().withHostIp(DEFAULT_ADDRESS)
                                                                         .withHostPort("32103")));
        labels.put(SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_REF_SUFFIX, "myserv1");
        labels.put(SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "http");
        labels.put(SERVER_CONF_LABEL_PREFIX + "8080/udp" + SERVER_CONF_LABEL_REF_SUFFIX, "myserv1-tftp");
        labels.put(SERVER_CONF_LABEL_PREFIX + "8080/udp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "tftp");
        labels.put(SERVER_CONF_LABEL_PREFIX + "100100/udp" + SERVER_CONF_LABEL_REF_SUFFIX, "myserv2");
        labels.put(SERVER_CONF_LABEL_PREFIX + "100100/udp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "dhcp");
        labels.put(SERVER_CONF_LABEL_PREFIX + "8000/tcp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "tcp");

        // when
        final Map<String, ServerImpl> servers = runtimeInfo.getServers();

        // then
        assertEquals(servers.get("8080/tcp"), new ServerImpl("myserv1",
                                                             CONTAINER_HOST + ":32100",
                                                             "http://" + CONTAINER_HOST + ":32100"));
        assertEquals(servers.get("100100/udp"), new ServerImpl("myserv2",
                                                               CONTAINER_HOST + ":32101",
                                                               "dhcp://" + CONTAINER_HOST + ":32101"));
        assertEquals(servers.get("8080/udp"), new ServerImpl("myserv1-tftp",
                                                             CONTAINER_HOST + ":32102",
                                                             "tftp://" + CONTAINER_HOST + ":32102"));
        assertEquals(servers.get("8000/tcp"), new ServerImpl("Server-8000-tcp",
                                                             CONTAINER_HOST + ":32103",
                                                             "tcp://" + CONTAINER_HOST + ":32103"));
    }

    @Test
    public void shouldAllowToUsePortFromDockerLabelsWithoutTransportProtocol() throws Exception {
        // given
        runtimeInfo = new DockerInstanceRuntimeInfo(containerInfo,
                                                    CONTAINER_HOST,
                                                    machineConfig,
                                                    Collections.emptySet(),
                                                    Collections.emptySet());
        Map<String, List<PortBinding>> ports = new HashMap<>();
        when(networkSettings.getPorts()).thenReturn(ports);
        Map<String, String> labels = new HashMap<>();
        when(containerConfig.getLabels()).thenReturn(labels);
        ports.put("8080/tcp", Collections.singletonList(new PortBinding().withHostIp(DEFAULT_ADDRESS)
                                                                         .withHostPort("32100")));
        ports.put("8080/udp", Collections.singletonList(new PortBinding().withHostIp(DEFAULT_ADDRESS)
                                                                         .withHostPort("32102")));
        ports.put("8000/tcp", Collections.singletonList(new PortBinding().withHostIp(DEFAULT_ADDRESS)
                                                                         .withHostPort("32103")));
        labels.put(SERVER_CONF_LABEL_PREFIX + "8080" + SERVER_CONF_LABEL_REF_SUFFIX, "myserv1");
        labels.put(SERVER_CONF_LABEL_PREFIX + "8080" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "http");
        labels.put(SERVER_CONF_LABEL_PREFIX + "8080/udp" + SERVER_CONF_LABEL_REF_SUFFIX, "myserv1-tftp");
        labels.put(SERVER_CONF_LABEL_PREFIX + "8080/udp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "tftp");
        labels.put(SERVER_CONF_LABEL_PREFIX + "8000" + SERVER_CONF_LABEL_REF_SUFFIX, "myserv2");
        labels.put(SERVER_CONF_LABEL_PREFIX + "8000/tcp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "tcp");

        // when
        final Map<String, ServerImpl> servers = runtimeInfo.getServers();

        // then
        assertEquals(servers.get("8080/tcp"), new ServerImpl("myserv1",
                                                             CONTAINER_HOST + ":32100",
                                                             "http://" + CONTAINER_HOST + ":32100"));
        assertEquals(servers.get("8080/udp"), new ServerImpl("myserv1-tftp",
                                                             CONTAINER_HOST + ":32102",
                                                             "tftp://" + CONTAINER_HOST + ":32102"));
        assertEquals(servers.get("8000/tcp"), new ServerImpl("myserv2",
                                                             CONTAINER_HOST + ":32103",
                                                             "tcp://" + CONTAINER_HOST + ":32103"));
    }

    @Test
    public void shouldPreferMachineConfOverDockerLabels() throws Exception {
        // given
        Map<String, List<PortBinding>> ports = new HashMap<>();
        when(networkSettings.getPorts()).thenReturn(ports);
        Map<String, String> labels = new HashMap<>();
        when(containerConfig.getLabels()).thenReturn(labels);
        List<ServerConfImpl> serversConfigs = new ArrayList<>();
        doReturn(serversConfigs).when(machineConfig).getServers();
        ports.put("8080/tcp", Collections.singletonList(new PortBinding().withHostIp(DEFAULT_ADDRESS)
                                                                         .withHostPort("32100")));
        ports.put("100100/udp", Collections.singletonList(new PortBinding().withHostIp(DEFAULT_ADDRESS)
                                                                           .withHostPort("32101")));
        ports.put("8080/udp", Collections.singletonList(new PortBinding().withHostIp(DEFAULT_ADDRESS)
                                                                         .withHostPort("32102")));
        ports.put("8000/tcp", Collections.singletonList(new PortBinding().withHostIp(DEFAULT_ADDRESS)
                                                                         .withHostPort("32103")));
        labels.put(SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_REF_SUFFIX, "myserv1label");
        labels.put(SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "https");
        labels.put(SERVER_CONF_LABEL_PREFIX + "8080/udp" + SERVER_CONF_LABEL_REF_SUFFIX, "myserv1-tftp");
        labels.put(SERVER_CONF_LABEL_PREFIX + "8080/udp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "tftp");
        labels.put(SERVER_CONF_LABEL_PREFIX + "100100/udp" + SERVER_CONF_LABEL_REF_SUFFIX, "myserv2label");
        labels.put(SERVER_CONF_LABEL_PREFIX + "100100/udp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "dhcp");
        serversConfigs.add(new ServerConfImpl("myserv1conf", "8080/tcp", "http"));
        serversConfigs.add(new ServerConfImpl(null, "8080/udp", null));
        runtimeInfo = new DockerInstanceRuntimeInfo(containerInfo,
                                                    CONTAINER_HOST,
                                                    machineConfig,
                                                    Collections.emptySet(),
                                                    Collections.emptySet());

        // when
        final Map<String, ServerImpl> servers = runtimeInfo.getServers();

        // then
        assertEquals(servers.get("8080/tcp"), new ServerImpl("myserv1conf",
                                                             CONTAINER_HOST + ":32100",
                                                             "http://" + CONTAINER_HOST + ":32100"));
        assertEquals(servers.get("100100/udp"), new ServerImpl("myserv2label",
                                                               CONTAINER_HOST + ":32101",
                                                               "dhcp://" + CONTAINER_HOST + ":32101"));
        assertEquals(servers.get("8080/udp"), new ServerImpl("Server-8080-udp",
                                                             CONTAINER_HOST + ":32102",
                                                             null));
    }

    @Test
    public void shouldAddOnlyCommonSystemServersConfigToNonDevMachine() throws Exception {
        // given
        Map<String, List<PortBinding>> ports = new HashMap<>();
        when(networkSettings.getPorts()).thenReturn(ports);
        ports.put("4301/tcp", Collections.singletonList(new PortBinding().withHostIp(DEFAULT_ADDRESS)
                                                                         .withHostPort("32100")));
        ports.put("4302/udp", Collections.singletonList(new PortBinding().withHostIp(DEFAULT_ADDRESS)
                                                                           .withHostPort("32101")));
        ports.put("4301/udp", Collections.singletonList(new PortBinding().withHostIp(DEFAULT_ADDRESS)
                                                                         .withHostPort("32102")));
        // add user defined server
        ports.put("4305/tcp", Collections.singletonList(new PortBinding().withHostIp(DEFAULT_ADDRESS)
                                                                         .withHostPort("32103")));
        Set<ServerConf> commonSystemServersConfigs = new HashSet<>();
        commonSystemServersConfigs.add(new ServerConfImpl("sysServer1-tcp", "4301/tcp", "http"));
        commonSystemServersConfigs.add(new ServerConfImpl("sysServer2-udp", "4302/udp", "dhcp"));
        commonSystemServersConfigs.add(new ServerConfImpl("sysServer1-udp", "4301/udp", null));
        Set<ServerConf> devSystemServersConfigs = new HashSet<>();
        devSystemServersConfigs.add(new ServerConfImpl("devSysServer1-tcp", "4305/tcp", "http"));
        when(machineConfig.isDev()).thenReturn(false);
        runtimeInfo = new DockerInstanceRuntimeInfo(containerInfo,
                                                    CONTAINER_HOST,
                                                    machineConfig,
                                                    devSystemServersConfigs,
                                                    commonSystemServersConfigs);

        // when
        final Map<String, ServerImpl> servers = runtimeInfo.getServers();

        // then
        assertEquals(servers.get("4301/tcp"), new ServerImpl("sysServer1-tcp",
                                                             CONTAINER_HOST + ":32100",
                                                             "http://" + CONTAINER_HOST + ":32100"));
        assertEquals(servers.get("4302/udp"), new ServerImpl("sysServer2-udp",
                                                               CONTAINER_HOST + ":32101",
                                                               "dhcp://" + CONTAINER_HOST + ":32101"));
        assertEquals(servers.get("4301/udp"), new ServerImpl("sysServer1-udp",
                                                             CONTAINER_HOST + ":32102",
                                                             null));
        assertEquals(servers.get("4305/tcp"), new ServerImpl("Server-4305-tcp",
                                                             CONTAINER_HOST + ":32103",
                                                             null));
    }

    @Test
    public void shouldAddCommonAndDevSystemServersConfigToDevMachine() throws Exception {
        // given
        Map<String, List<PortBinding>> ports = new HashMap<>();
        when(networkSettings.getPorts()).thenReturn(ports);
        ports.put("4301/tcp", Collections.singletonList(new PortBinding().withHostIp(DEFAULT_ADDRESS)
                                                                         .withHostPort("32100")));
        ports.put("4302/udp", Collections.singletonList(new PortBinding().withHostIp(DEFAULT_ADDRESS)
                                                                         .withHostPort("32101")));
        ports.put("4305/tcp", Collections.singletonList(new PortBinding().withHostIp(DEFAULT_ADDRESS)
                                                                         .withHostPort("32102")));
        ports.put("4305/udp", Collections.singletonList(new PortBinding().withHostIp(DEFAULT_ADDRESS)
                                                                         .withHostPort("32103")));
        Set<ServerConf> commonSystemServersConfigs = new HashSet<>();
        commonSystemServersConfigs.add(new ServerConfImpl("sysServer1-tcp", "4301/tcp", "http"));
        commonSystemServersConfigs.add(new ServerConfImpl("sysServer2-udp", "4302/udp", "dhcp"));
        Set<ServerConf> devSystemServersConfigs = new HashSet<>();
        devSystemServersConfigs.add(new ServerConfImpl("devSysServer1-tcp", "4305/tcp", "http"));
        devSystemServersConfigs.add(new ServerConfImpl("devSysServer1-udp", "4305/udp", null));
        when(machineConfig.isDev()).thenReturn(true);
        runtimeInfo = new DockerInstanceRuntimeInfo(containerInfo,
                                                    CONTAINER_HOST,
                                                    machineConfig,
                                                    devSystemServersConfigs,
                                                    commonSystemServersConfigs);

        // when
        final Map<String, ServerImpl> servers = runtimeInfo.getServers();

        // then
        assertEquals(servers.get("4301/tcp"), new ServerImpl("sysServer1-tcp",
                                                             CONTAINER_HOST + ":32100",
                                                             "http://" + CONTAINER_HOST + ":32100"));
        assertEquals(servers.get("4302/udp"), new ServerImpl("sysServer2-udp",
                                                             CONTAINER_HOST + ":32101",
                                                             "dhcp://" + CONTAINER_HOST + ":32101"));
        assertEquals(servers.get("4305/tcp"), new ServerImpl("devSysServer1-tcp",
                                                             CONTAINER_HOST + ":32102",
                                                             "http://" + CONTAINER_HOST + ":32102"));
        assertEquals(servers.get("4305/udp"), new ServerImpl("devSysServer1-udp",
                                                             CONTAINER_HOST + ":32103",
                                                             null));
    }


}
