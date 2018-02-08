/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.core.db.jpa;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.ssh.server.model.impl.SshPairImpl;
import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.model.impl.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.RecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.SourceStorageImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.api.workspace.server.stack.image.StackIcon;

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
        asList(
            new CommandImpl(id + "cmd1", "mvn clean install", "maven"),
            new CommandImpl(id + "cmd2", "mvn clean install", "maven")),
        asList(createProjectConfig(id + "-project1"), createProjectConfig(id + "-project2")),
        ImmutableMap.of(
            id + "env1", createEnv(),
            id + "env2", createEnv()));
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
    newMachine.setInstallers(ImmutableList.of("agent5", "agent4"));
    newMachine.setAttributes(singletonMap("att1", "val"));
    newMachine.setEnv(singletonMap("CHE_ENV", "value"));

    final EnvironmentImpl newEnv = new EnvironmentImpl();
    newEnv.setMachines(ImmutableMap.of("new-machine", newMachine));
    newEnv.setRecipe(newRecipe);
    return newEnv;
  }

  public static WorkspaceImpl createWorkspace(String id, Account account) {
    return new WorkspaceImpl(id, account, createWorkspaceConfig(id));
  }

  public static SshPairImpl createSshPair(String owner, String service, String name) {
    return new SshPairImpl(owner, service, name, "public-key", "private-key");
  }

  public static StackImpl createStack(String id, String name) {
    return StackImpl.builder()
        .setId(id)
        .setName(name)
        .setCreator("user123")
        .setDescription(id + "-description")
        .setScope(id + "-scope")
        .setWorkspaceConfig(createWorkspaceConfig("test"))
        .setTags(asList(id + "-tag1", id + "-tag2"))
        .setComponents(
            asList(
                new StackComponentImpl(id + "-component1", id + "-component1-version"),
                new StackComponentImpl(id + "-component2", id + "-component2-version")))
        .setStackIcon(
            new StackIcon(id + "-icon", id + "-media-type", "0x1234567890abcdef".getBytes()))
        .build();
  }

  private TestObjectsFactory() {}
}
