/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.cache.tck;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.eclipse.che.commons.lang.NameGenerator.generate;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Map;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.model.workspace.runtime.MachineStatus;
import org.eclipse.che.api.core.model.workspace.runtime.ServerStatus;
import org.eclipse.che.api.workspace.server.model.impl.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeIdentityImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesMachineImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesRuntimeState;

/** @author Sergii Leshchenko */
public class TestObjects {

  public static AccountImpl createAccount() {
    return new AccountImpl(generate("id", 8), generate("name", 6), "any");
  }

  public static WorkspaceImpl createWorkspace() {
    return new WorkspaceImpl(
        generate("wsId", 8),
        createAccount(),
        new WorkspaceConfigImpl(
            generate("wsName", 8),
            "description",
            "defEnv",
            emptyList(),
            emptyList(),
            emptyMap(),
            emptyMap()));
  }

  public static KubernetesRuntimeState createRuntimeState(WorkspaceImpl workspace) {
    return new KubernetesRuntimeState(
        new RuntimeIdentityImpl(
            workspace.getId(), "defEnv", workspace.getAccount().getId(), generate("namespace", 5)),
        WorkspaceStatus.RUNNING,
        Arrays.asList(createCommand(), createCommand()));
  }

  public static CommandImpl createCommand() {
    CommandImpl cmd =
        new CommandImpl(generate("command", 5), "echo " + generate("command", 5), "CUSTOM");
    cmd.getAttributes().put("attr1", "val1");
    cmd.getAttributes().put("attr2", "val2");
    return cmd;
  }

  public static KubernetesMachineImpl createMachine(
      String workspaceId,
      String machineName,
      MachineStatus status,
      Map<String, ServerImpl> servers) {
    return new KubernetesMachineImpl(
        workspaceId,
        machineName,
        generate("pod", 5),
        generate("container", 5),
        status,
        ImmutableMap.of("key1", "value1", generate("key", 2), generate("value", 2)),
        servers);
  }

  public static ServerImpl createServer(ServerStatus status) {
    return new ServerImpl(
        generate("http:://", 10),
        status,
        ImmutableMap.of(generate("key", 5), generate("value", 5)));
  }
}
