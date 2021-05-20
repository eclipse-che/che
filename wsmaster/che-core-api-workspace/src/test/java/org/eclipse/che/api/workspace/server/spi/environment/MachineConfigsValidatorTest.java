/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.spi.environment;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_REQUEST_ATTRIBUTE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.shared.Constants;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link MachineConfigsValidator}.
 *
 * @author Alexander Garagatyi
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class MachineConfigsValidatorTest {

  private static final String MACHINE_NAME = "machine1";

  private InternalMachineConfig machineConfig;

  private MachineConfigsValidator machinesValidator;

  @BeforeMethod
  public void setUp() throws Exception {
    machinesValidator = new MachineConfigsValidator();

    machineConfig = machineMockWithServers(Constants.SERVER_WS_AGENT_HTTP_REFERENCE);
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp = "Name of machine '.*' in environment is invalid",
      dataProvider = "invalidMachineNames")
  public void shouldFailIfMachinesNameAreInvalid(String machineName) throws Exception {
    // when
    machinesValidator.validate(singletonMap(machineName, machineConfig));
  }

  @DataProvider(name = "invalidMachineNames")
  public Object[][] invalidMachineNames() {
    return new Object[][] {
      {""}, {"-123"}, {"123-"}, {"-123-"}, {"/123-"}, {"/123"}, {"123/"}, {"123_"}, {"!asdd/"},
    };
  }

  @Test(dataProvider = "validMachineNames")
  public void shouldNotFailIfMachinesNameAreValid(String machineName) throws Exception {
    // when
    machinesValidator.validate(singletonMap(machineName, machineConfig));
  }

  @DataProvider(name = "validMachineNames")
  public Object[][] validMachineNames() {
    return new Object[][] {
      {"machine"}, {"machine123"}, {"machine-123"}, {"app/db"}, {"app_db"},
    };
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "Machine '.*' in environment contains server conf '.*' with invalid port '.*'",
      dataProvider = "invalidServerPorts")
  public void shouldFailIfServerPortIsInvalid(String servicePort) throws Exception {
    // given
    ServerConfigImpl server =
        new ServerConfigImpl(servicePort, "https", "/some/path", singletonMap("key", "value"));
    when(machineConfig.getServers())
        .thenReturn(singletonMap(Constants.SERVER_WS_AGENT_HTTP_REFERENCE, server));

    // when
    machinesValidator.validate(singletonMap(MACHINE_NAME, machineConfig));
  }

  @DataProvider(name = "invalidServerPorts")
  public Object[][] invalidServerPorts() {
    return new Object[][] {
      {"aaa"}, {"123aaa"}, {"8080/tpc2"}, {"8080/TCP"}, {"123udp"}, {""}, {"/123"},
    };
  }

  @Test(dataProvider = "validServerPorts")
  public void shouldNotFailIfServerPortIsValid(String servicePort) throws Exception {
    // given
    ServerConfigImpl server =
        new ServerConfigImpl(servicePort, "https", "/some/path", singletonMap("key", "value"));
    when(machineConfig.getServers())
        .thenReturn(singletonMap(Constants.SERVER_WS_AGENT_HTTP_REFERENCE, server));

    // when
    machinesValidator.validate(singletonMap(MACHINE_NAME, machineConfig));
  }

  @DataProvider(name = "validServerPorts")
  public Object[][] validServerPorts() {
    return new Object[][] {
      {"1"}, {"12"}, {"8080"}, {"8080/tcp"}, {"8080/udp"},
    };
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "Machine '.*' in environment contains server conf '.*' with invalid protocol '.*'",
      dataProvider = "invalidServerProtocols")
  public void shouldFailIfServerProtocolIsInvalid(String serviceProtocol) throws Exception {
    // given
    ServerConfigImpl server =
        new ServerConfigImpl("8080", serviceProtocol, "/some/path", singletonMap("key", "value"));
    when(machineConfig.getServers())
        .thenReturn(singletonMap(Constants.SERVER_WS_AGENT_HTTP_REFERENCE, server));

    // when
    machinesValidator.validate(singletonMap(MACHINE_NAME, machineConfig));
  }

  @DataProvider(name = "invalidServerProtocols")
  public Object[][] invalidServerProtocols() {
    return new Object[][] {{"0"}, {"0sds"}, {"TCP"}, {"UDP"}, {"http@"}};
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "Machine '.*' in environment contains inconsistent memory attributes: Memory limit: '.*', Memory request: '.*'")
  public void shouldFailIfMemoryAttributesAreInconsistent() throws Exception {
    // given
    when(machineConfig.getAttributes())
        .thenReturn(
            ImmutableMap.of(
                MEMORY_LIMIT_ATTRIBUTE,
                String.valueOf(1024L * 1024L * 1024L),
                MEMORY_REQUEST_ATTRIBUTE,
                String.valueOf(2048L * 1024L * 1024L)));
    // when
    machinesValidator.validate(singletonMap(MACHINE_NAME, machineConfig));
  }

  @Test(dataProvider = "validMemoryAttributes")
  public void shouldSucceedIfMemoryAttributesAreConsistentOrNotPresent(
      String memoryLimit, String memoryRequest) throws Exception {
    // given
    Map<String, String> attributes = new HashMap<>();
    if (memoryLimit != null) {
      attributes.put(MEMORY_LIMIT_ATTRIBUTE, memoryLimit);
    }
    if (memoryRequest != null) {
      attributes.put(MEMORY_LIMIT_ATTRIBUTE, memoryRequest);
    }
    when(machineConfig.getAttributes()).thenReturn(attributes);
    // when
    machinesValidator.validate(singletonMap(MACHINE_NAME, machineConfig));
  }

  @DataProvider(name = "validMemoryAttributes")
  public Object[][] validMemoryAttributes() {
    return new Object[][] {
      {null, String.valueOf(2048L * 1024L * 1024L)},
      {String.valueOf(2048L * 1024L * 1024L), null},
      {null, null},
      {String.valueOf(2048L * 1024L * 1024L), String.valueOf(2048L * 1024L * 1024L)}
    };
  }

  @Test(dataProvider = "validServerProtocols")
  public void shouldNotFailIfServerProtocolIsValid(String serviceProtocol) throws Exception {
    // given
    ServerConfigImpl server =
        new ServerConfigImpl("8080", serviceProtocol, "/some/path", singletonMap("key", "value"));
    when(machineConfig.getServers())
        .thenReturn(singletonMap(Constants.SERVER_WS_AGENT_HTTP_REFERENCE, server));

    // when
    machinesValidator.validate(singletonMap(MACHINE_NAME, machineConfig));
  }

  @DataProvider(name = "validServerProtocols")
  public Object[][] validServerProtocols() {
    return new Object[][] {{"a"}, {"http"}, {"tcp"}, {"tcp2"}};
  }

  private static InternalMachineConfig machineMock() {
    InternalMachineConfig machineConfig = mock(InternalMachineConfig.class);
    when(machineConfig.getServers()).thenReturn(emptyMap());
    return machineConfig;
  }

  private static InternalMachineConfig machineMockWithServers(String... servers) {
    InternalMachineConfig machineConfig = machineMock();
    when(machineConfig.getServers()).thenReturn(createServers(servers));
    return machineConfig;
  }

  private static Map<String, ServerConfig> createServers(String... servers) {
    return Arrays.stream(servers)
        .collect(
            Collectors.toMap(
                Function.identity(),
                s -> new ServerConfigImpl("8080", "http", "/", singletonMap("key", "value"))));
  }
}
