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
package org.eclipse.che.core.db.jpa;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.commons.lang.NameGenerator.generate;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.model.workspace.devfile.Metadata;
import org.eclipse.che.api.core.model.workspace.runtime.MachineStatus;
import org.eclipse.che.api.core.model.workspace.runtime.ServerStatus;
import org.eclipse.che.api.ssh.server.model.impl.SshPairImpl;
import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.model.impl.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.RecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeIdentityImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerImpl;
import org.eclipse.che.api.workspace.server.model.impl.SourceStorageImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ActionImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.DevfileImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.EntrypointImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.MetadataImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ProjectImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.SourceImpl;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesMachineImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.model.KubernetesRuntimeState;

/**
 * Defines method for creating tests object instances.
 *
 * @author Yevhenii Voevodin
 */
public final class TestObjectsFactory {

  public static AccountImpl createAccount(String id) {
    return new AccountImpl(id, id + "_name", "personal");
  }

  public static UserImpl createUser(String id) {
    return new UserImpl(
        id, id + "@eclipse.org", id + "_name", "password", asList(id + "_alias1", id + "_alias2"));
  }

  public static ProfileImpl createProfile(String userId) {
    return new ProfileImpl(
        userId,
        new HashMap<>(
            ImmutableMap.of(
                "attribute1", "value1",
                "attribute2", "value2",
                "attribute3", "value3")));
  }

  public static Map<String, String> createPreferences() {
    return new HashMap<>(
        ImmutableMap.of(
            "preference1", "value1",
            "preference2", "value2",
            "preference3", "value3"));
  }

  public static WorkspaceConfigImpl createWorkspaceConfig(String id) {
    return new WorkspaceConfigImpl(
        id + "_name",
        id + "description",
        "default-env",
        asList(createCommand(), createCommand()),
        asList(createProjectConfig(id + "-project1"), createProjectConfig(id + "-project2")),
        ImmutableMap.of(
            id + "env1", createEnv(),
            id + "env2", createEnv()),
        ImmutableMap.of("attr1", "value1", "attr2", "value2"));
  }

  public static DevfileImpl createDevfile(String id) {
    return new DevfileImpl(
        "0.0.1",
        asList(createDevfileProject(id + "-project1"), createDevfileProject(id + "-project2")),
        asList(
            createDevfileComponent(id + "-component1"), createDevfileComponent(id + "-component2")),
        asList(createDevfileCommand(id + "-command1"), createDevfileCommand(id + "-command2")),
        singletonMap("attribute1", "value1"),
        createMetadata(id + "name"));
  }

  private static ComponentImpl createDevfileComponent(String name) {
    return new ComponentImpl(
        "kubernetes",
        name,
        "eclipse/che-theia/0.0.1",
        ImmutableMap.of("java.home", "/home/user/jdk11"),
        "https://mysite.com/registry/somepath",
        "/dev.yaml",
        "refContent",
        ImmutableMap.of("app.kubernetes.io/component", "webapp"),
        singletonList(createEntrypoint()),
        "image",
        "256G",
        "128M",
        "200m",
        "100m",
        false,
        false,
        singletonList("command"),
        singletonList("arg"),
        null,
        null,
        null);
  }

  private static EntrypointImpl createEntrypoint() {
    return new EntrypointImpl(
        "parentName",
        singletonMap("parent", "selector"),
        "containerName",
        asList("command1", "command2"),
        asList("arg1", "arg2"));
  }

  private static org.eclipse.che.api.workspace.server.model.impl.devfile.CommandImpl
      createDevfileCommand(String name) {
    return new org.eclipse.che.api.workspace.server.model.impl.devfile.CommandImpl(
        name, singletonList(createAction()), singletonMap("attr1", "value1"), null);
  }

  private static ActionImpl createAction() {
    return new ActionImpl("exec", "component", "run.sh", "/home/user", null, null);
  }

  private static ProjectImpl createDevfileProject(String name) {
    return new ProjectImpl(name, createDevfileSource(), "path");
  }

  private static SourceImpl createDevfileSource() {
    return new SourceImpl(
        "type", "http://location", "branch1", "point1", "tag1", "commit1", "sparseCheckoutDir1");
  }

  public static CommandImpl createCommand() {
    CommandImpl cmd =
        new CommandImpl(generate("command", 5), "echo " + generate("command", 5), "CUSTOM");
    cmd.getAttributes().put("attr1", "val1");
    cmd.getAttributes().put("attr2", "val2");
    return cmd;
  }

  public static ProjectConfigImpl createProjectConfig(String name) {
    final ProjectConfigImpl project = new ProjectConfigImpl();
    project.setDescription(name + "-description");
    project.setName(name);
    project.setPath("/" + name);
    project.setType(name + "type");
    project.setSource(
        new SourceStorageImpl(
            "source-type",
            "source-location",
            ImmutableMap.of(
                "param1", "value",
                "param2", "value")));
    project.setMixins(asList("mixin1", "mixin2"));
    project.getAttributes().put("attribute1", singletonList("value1"));
    project.getAttributes().put("attribute2", singletonList("value2"));
    project.getAttributes().put("attribute3", singletonList("value3"));
    return project;
  }

  public static EnvironmentImpl createEnv() {
    final RecipeImpl newRecipe = new RecipeImpl();
    newRecipe.setLocation("new-location");
    newRecipe.setType("new-type");
    newRecipe.setContentType("new-content-type");
    newRecipe.setContent("new-content");

    final MachineConfigImpl newMachine = new MachineConfigImpl();
    final ServerConfigImpl serverConf1 =
        new ServerConfigImpl("2265", "http", "/path1", singletonMap("key", "value"));
    final ServerConfigImpl serverConf2 =
        new ServerConfigImpl("2266", "ftp", "/path2", singletonMap("key", "value"));
    newMachine.setServers(ImmutableMap.of("ref1", serverConf1, "ref2", serverConf2));
    newMachine.setAttributes(singletonMap("att1", "val"));
    newMachine.setEnv(singletonMap("CHE_ENV", "value"));

    final EnvironmentImpl newEnv = new EnvironmentImpl();
    newEnv.setMachines(ImmutableMap.of("new-machine", newMachine));
    newEnv.setRecipe(newRecipe);
    return newEnv;
  }

  public static WorkspaceImpl createWorkspaceWithConfig(String id, Account account) {
    return new WorkspaceImpl(id, account, createWorkspaceConfig(id));
  }

  public static WorkspaceImpl createWorkspaceWithDevfile(String id, Account account) {
    return new WorkspaceImpl(id, account, createDevfile(id));
  }

  public static SshPairImpl createSshPair(String owner, String service, String name) {
    return new SshPairImpl(owner, service, name, "public-key", "private-key");
  }

  public static KubernetesRuntimeState createK8sRuntimeState(String workspaceId) {
    return new KubernetesRuntimeState(
        new RuntimeIdentityImpl(workspaceId, "envName", "ownerId", "test-namespace"),
        WorkspaceStatus.RUNNING,
        asList(createCommand(), createCommand()));
  }

  public static KubernetesMachineImpl createK8sMachine(KubernetesRuntimeState k8sRuntimeState) {
    return new KubernetesMachineImpl(
        k8sRuntimeState.getRuntimeId().getWorkspaceId(),
        NameGenerator.generate("machine-", 5),
        NameGenerator.generate("pod-", 5),
        NameGenerator.generate("container-", 5),
        MachineStatus.RUNNING,
        ImmutableMap.of("test", "true"),
        ImmutableMap.of(
            "server",
            new ServerImpl(
                "http://localhost:8080/api",
                ServerStatus.RUNNING,
                ImmutableMap.of("key", "value"))));
  }

  public static Metadata createMetadata(String name) {
    return new MetadataImpl(name);
  }

  private TestObjectsFactory() {}
}
