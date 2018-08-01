/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.resource.api.workspace;

import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.eclipse.che.api.core.model.workspace.runtime.MachineStatus;
import org.eclipse.che.api.core.model.workspace.runtime.Server;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.MachineImpl;
import org.eclipse.che.api.workspace.server.model.impl.RecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.commons.lang.NameGenerator;

/**
 * Test util class, helps to create test objects.
 *
 * @author Yevhenii Voevodin
 * @author Alexander Garagatyi
 */
public final class TestObjects {

  private static final String DEFAULT_USER_NAME = "user123456";

  public static EnvironmentImpl createEnvironment(String... machineRams) throws Exception {
    final Map<String, MachineConfig> machines = new HashMap<>();
    for (String machineRam : machineRams) {
      final MachineConfigImpl machineConfig = new MachineConfigImpl();
      machineConfig.setAttributes(
          ImmutableMap.of(MachineConfig.MEMORY_LIMIT_ATTRIBUTE, machineRam));
      machines.put("dev-machine", machineConfig);
    }
    final RecipeImpl recipe = new RecipeImpl("compose", "application/x-yaml", "", null);
    return new EnvironmentImpl(recipe, machines);
  }

  /** Creates users workspace object based on the owner and machines RAM. */
  public static WorkspaceImpl createWorkspace(String owner, String... machineRams)
      throws Exception {

    return WorkspaceImpl.builder()
        .generateId()
        .setConfig(
            WorkspaceConfigImpl.builder()
                .setName(NameGenerator.generate("workspace", 2))
                .setEnvironments(singletonMap("dev-env", createEnvironment(machineRams)))
                .setDefaultEnv("dev-env")
                .build())
        .setAccount(new AccountImpl("accountId", owner, "test"))
        .setTemporary(false)
        .setStatus(WorkspaceStatus.STOPPED)
        .build();
  }

  /** Creates workspace config object based on the machines RAM. */
  public static WorkspaceConfig createConfig(String... machineRams) throws Exception {
    return createWorkspace(DEFAULT_USER_NAME, machineRams).getConfig();
  }

  /** Creates runtime workspace object based on the machines RAM. */
  public static WorkspaceImpl createRuntime(String... machineRams) throws Exception {
    final WorkspaceImpl workspace = createWorkspace(DEFAULT_USER_NAME, machineRams);
    final String envName = workspace.getConfig().getDefaultEnv();
    final Map<String, MachineImpl> machines = new HashMap<>();
    int i = 0;
    for (String machineRam : machineRams) {
      machines.put(
          "machine" + i++,
          createMachine(machineRam, Collections.emptyMap(), MachineStatus.RUNNING));
    }
    RuntimeImpl runtime = new RuntimeImpl(envName, machines, DEFAULT_USER_NAME);

    workspace.setStatus(RUNNING);
    workspace.setRuntime(runtime);
    return workspace;
  }

  private static MachineImpl createMachine(
      String memoryBytes, Map<String, ? extends Server> servers, MachineStatus status) {
    return new MachineImpl(
        ImmutableMap.of(MachineConfig.MEMORY_LIMIT_ATTRIBUTE, memoryBytes), servers, status);
  }

  private TestObjects() {}
}
