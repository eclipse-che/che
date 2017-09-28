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
package org.eclipse.che.multiuser.resource.api.workspace;

import static java.util.Collections.singletonMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
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
  private static final ObjectMapper YAML_PARSER = new ObjectMapper(new YAMLFactory());

  public static EnvironmentImpl createEnvironment(String devMachineRam, String... machineRams)
      throws Exception {
    return null; /*
                 Map<String, ExtendedMachineImpl> machines = new HashMap<>();
                 machines.put(
                     "dev-machine",
                     new ExtendedMachineImpl(
                         singletonList("org.eclipse.che.ws-agent"),
                         emptyMap(),
                         singletonMap("memoryLimitBytes", Long.toString(Size.parseSize(devMachineRam)))));
                 HashMap<String, ComposeServiceImpl> services = new HashMap<>(1 + machineRams.length);
                 services.put("dev-machine", createService());
                 for (int i = 0; i < machineRams.length; i++) {
                   services.put("machine" + i, createService());
                   // null is allowed to reproduce situation with default RAM size
                   if (machineRams[i] != null) {
                     machines.put(
                         "machine" + i,
                         new ExtendedMachineImpl(
                             null,
                             null,
                             singletonMap("memoryLimitBytes", Long.toString(Size.parseSize(machineRams[i])))));
                   }
                 }
                 ComposeEnvironment composeEnvironment = new ComposeEnvironment();
                 composeEnvironment.setServices(services);
                 String yaml = YAML_PARSER.writeValueAsString(composeEnvironment);
                 EnvironmentRecipeImpl recipe =
                     new EnvironmentRecipeImpl("compose", "application/x-yaml", yaml, null);

                 return new EnvironmentImpl(recipe, machines);*/
  }

  /** Creates users workspace object based on the owner and machines RAM. */
  public static WorkspaceImpl createWorkspace(
      String owner, String devMachineRam, String... machineRams) throws Exception {

    return WorkspaceImpl.builder()
        .generateId()
        .setConfig(
            WorkspaceConfigImpl.builder()
                .setName(NameGenerator.generate("workspace", 2))
                .setEnvironments(
                    singletonMap("dev-env", createEnvironment(devMachineRam, machineRams)))
                .setDefaultEnv("dev-env")
                .build())
        .setAccount(new AccountImpl("accountId", owner, "test"))
        .setTemporary(false)
        .setStatus(WorkspaceStatus.STOPPED)
        .build();
  }

  /** Creates workspace config object based on the machines RAM. */
  public static WorkspaceConfig createConfig(String devMachineRam, String... machineRams)
      throws Exception {
    return createWorkspace(DEFAULT_USER_NAME, devMachineRam, machineRams).getConfig();
  }

  /** Creates runtime workspace object based on the machines RAM. */
  public static WorkspaceImpl createRuntime(String devMachineRam, String... machineRams)
      throws Exception {
    /*
    final WorkspaceImpl workspace = createWorkspace(DEFAULT_USER_NAME, devMachineRam, machineRams);
    final String envName = workspace.getConfig().getDefaultEnv();
    EnvironmentImpl env = workspace.getConfig().getEnvironments().get(envName);
    String devMachineName = getDevMachineName(env);
    if (devMachineName == null) {
      throw new Exception("ws-machine is not found");
    }
    ExtendedMachineImpl devMachine = env.getMachines().get(devMachineName);
    final WorkspaceRuntimeImpl runtime =
        new WorkspaceRuntimeImpl(
            workspace.getConfig().getDefaultEnv(),
            null,
            env.getMachines()
                .entrySet()
                .stream()
                .map(
                    entry ->
                        createMachine(
                            workspace.getId(),
                            envName,
                            entry.getKey(),
                            devMachineName.equals(entry.getKey()),
                            entry.getValue().getAttributes().get("memoryLimitBytes")))
                .collect(toList()),
            createMachine(
                workspace.getId(),
                envName,
                devMachineName,
                true,
                devMachine.getAttributes().get("memoryLimitBytes")));
    workspace.setStatus(RUNNING);
    workspace.setRuntime(runtime);
    return workspace;
    */
    return null;
  }
  /*
  private static MachineImpl createMachine(
      String workspaceId, String envName, String machineName, boolean isDev, String memoryBytes) {

    return MachineImpl.builder()
        .setConfig(
            MachineConfigImpl.builder()
                .setDev(isDev)
                .setName(machineName)
                .setSource(new MachineSourceImpl("some-type").setContent("some-content"))
                .setLimits(
                    new MachineLimitsImpl((int) Size.parseSizeToMegabytes(memoryBytes + "b")))
                .setType("someType")
                .build())
        .setId(NameGenerator.generate("machine", 10))
        .setOwner(DEFAULT_USER_NAME)
        .setStatus(MachineStatus.RUNNING)
        .setWorkspaceId(workspaceId)
        .setEnvName(envName)
        .setRuntime(new MachineRuntimeInfoImpl(emptyMap(), emptyMap(), emptyMap()))
        .build();
  }

  private static ComposeServiceImpl createService() {
    ComposeServiceImpl service = new ComposeServiceImpl();
    service.setImage("image");
    return service;
  }
  */

  private TestObjects() {}
}
