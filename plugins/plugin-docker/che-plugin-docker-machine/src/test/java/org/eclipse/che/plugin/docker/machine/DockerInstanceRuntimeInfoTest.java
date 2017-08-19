/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.docker.machine;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toMap;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.ServerConf;
import org.eclipse.che.api.machine.server.model.impl.ServerConfImpl;
import org.eclipse.che.api.machine.server.model.impl.ServerImpl;
import org.eclipse.che.plugin.docker.client.json.ContainerConfig;
import org.eclipse.che.plugin.docker.client.json.ContainerInfo;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * @author Angel Misevski <amisevsk@redhat.com>
 * @author Alexander Garagatyi
 */
@Listeners(MockitoTestNGListener.class)
public class DockerInstanceRuntimeInfoTest {
  private static final String DEFAULT_HOSTNAME = "localhost";

  @Mock private ContainerInfo containerInfo;
  @Mock private MachineConfig machineConfig;
  @Mock private ContainerConfig containerConfig;
  @Mock private ServerEvaluationStrategy serverEvaluationStrategy;
  @Mock private ServerEvaluationStrategyProvider provider;
  @Mock private Map<String, ServerImpl> serversMap;
  @Captor private ArgumentCaptor<Map<String, ServerConfImpl>> serversCaptor;

  private DockerInstanceRuntimeInfo runtimeInfo;

  @BeforeMethod
  public void setUp() {

    runtimeInfo =
        new DockerInstanceRuntimeInfo(
            containerInfo,
            machineConfig,
            DEFAULT_HOSTNAME,
            provider,
            Collections.emptySet(),
            Collections.emptySet());

    when(containerInfo.getConfig()).thenReturn(containerConfig);
    when(machineConfig.getServers()).thenReturn(Collections.emptyList());
    when(provider.get()).thenReturn(serverEvaluationStrategy);
    when(serverEvaluationStrategy.getServers(any(), anyString(), any())).thenReturn(serversMap);
  }

  @Test
  public void shouldReturnEnvVars() throws Exception {
    // given
    Map<String, String> expectedVariables = new HashMap<>();
    expectedVariables.put("env_var1", "value1");
    expectedVariables.put("env_var2", "value2");
    expectedVariables.put("env_var3", "value3");

    when(containerConfig.getEnv())
        .thenReturn(
            expectedVariables
                .entrySet()
                .stream()
                .map(
                    stringStringEntry ->
                        stringStringEntry.getKey() + "=" + stringStringEntry.getValue())
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
    final String[] envVars = {"var1=value1", "var2=value2", "var3=value3"};
    when(containerConfig.getEnv()).thenReturn(envVars);

    assertEquals(runtimeInfo.projectsRoot(), null);
  }

  @Test
  public void shouldBeAbleToGetServers() throws Exception {
    Map<String, ServerImpl> servers = runtimeInfo.getServers();

    assertEquals(servers, serversMap);
    verify(serverEvaluationStrategy).getServers(eq(containerInfo), eq(DEFAULT_HOSTNAME), any());
  }

  @Test
  public void shouldPassCommonServerConfigsOnGetServersForNonDevMachine() throws Exception {
    // given
    Set<ServerConf> commonSystemServersConfigs = new HashSet<>();
    commonSystemServersConfigs.add(
        new ServerConfImpl("sysServer1-tcp", "4301/tcp", "http", "/some/path"));
    commonSystemServersConfigs.add(new ServerConfImpl("sysServer2-udp", "4302/udp", "dhcp", null));
    commonSystemServersConfigs.add(
        new ServerConfImpl("sysServer1-udp", "4301/udp", null, "some/path"));
    Set<ServerConf> devSystemServersConfigs =
        singleton(new ServerConfImpl("devSysServer1-tcp", "4305/tcp", "http", null));
    List<ServerConf> serversConfFromMachineConf =
        singletonList(new ServerConfImpl("machineConfServer1-tcp", "4306/tcp", "http", null));
    when(machineConfig.getServers()).thenAnswer(invocation -> serversConfFromMachineConf);
    when(machineConfig.isDev()).thenReturn(false);
    runtimeInfo =
        new DockerInstanceRuntimeInfo(
            containerInfo,
            machineConfig,
            DEFAULT_HOSTNAME,
            provider,
            devSystemServersConfigs,
            commonSystemServersConfigs);

    // when
    Map<String, ServerImpl> servers = runtimeInfo.getServers();

    // then
    assertEquals(servers, serversMap);
    verify(serverEvaluationStrategy)
        .getServers(eq(containerInfo), eq(DEFAULT_HOSTNAME), serversCaptor.capture());
    assertEquals(
        serversCaptor.getValue(),
        serversToMap(commonSystemServersConfigs, serversConfFromMachineConf));
  }

  @Test
  public void shouldPassCommonAndDevServerConfigsOnGetServersForNonDevMachine() throws Exception {
    Set<ServerConf> commonSystemServersConfigs = new HashSet<>();
    commonSystemServersConfigs.add(
        new ServerConfImpl("sysServer1-tcp", "4301/tcp", "http", "/some/path1"));
    commonSystemServersConfigs.add(
        new ServerConfImpl("sysServer2-udp", "4302/udp", "dhcp", "some/path2"));
    Set<ServerConf> devSystemServersConfigs = new HashSet<>();
    devSystemServersConfigs.add(
        new ServerConfImpl("devSysServer1-tcp", "4305/tcp", "http", "/some/path3"));
    devSystemServersConfigs.add(
        new ServerConfImpl("devSysServer1-udp", "4305/udp", null, "some/path4"));
    List<ServerConf> serversConfFromMachineConf =
        singletonList(new ServerConfImpl("machineConfServer1-tcp", "4306/tcp", "http", null));
    when(machineConfig.getServers()).thenAnswer(invocation -> serversConfFromMachineConf);
    when(machineConfig.isDev()).thenReturn(true);
    runtimeInfo =
        new DockerInstanceRuntimeInfo(
            containerInfo,
            machineConfig,
            DEFAULT_HOSTNAME,
            provider,
            devSystemServersConfigs,
            commonSystemServersConfigs);

    Map<String, ServerImpl> servers = runtimeInfo.getServers();

    assertEquals(servers, serversMap);
    verify(serverEvaluationStrategy)
        .getServers(eq(containerInfo), eq(DEFAULT_HOSTNAME), serversCaptor.capture());
    assertEquals(
        serversCaptor.getValue(),
        serversToMap(
            commonSystemServersConfigs, devSystemServersConfigs, serversConfFromMachineConf));
  }

  private Map<String, ServerConfImpl> serversToMap(
      Collection<ServerConf> serverConfigs, Collection<ServerConf> serverConfigs2) {
    return serversToMap(Stream.concat(serverConfigs.stream(), serverConfigs2.stream()));
  }

  private Map<String, ServerConfImpl> serversToMap(
      Collection<ServerConf> serverConfigs,
      Collection<ServerConf> serverConfigs2,
      Collection<ServerConf> serverConfigs3) {
    return serversToMap(
        Stream.concat(
            Stream.concat(serverConfigs.stream(), serverConfigs2.stream()),
            serverConfigs3.stream()));
  }

  private Map<String, ServerConfImpl> serversToMap(Stream<ServerConf> serverConfigs) {
    return serverConfigs.collect(
        toMap(
            srvConf ->
                srvConf.getPort().contains("/") ? srvConf.getPort() : srvConf.getPort() + "/tcp",
            ServerConfImpl::new));
  }
}
