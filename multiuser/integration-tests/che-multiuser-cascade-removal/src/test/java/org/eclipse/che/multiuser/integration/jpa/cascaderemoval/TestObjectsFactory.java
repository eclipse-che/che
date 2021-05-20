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
package org.eclipse.che.multiuser.integration.jpa.cascaderemoval;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;

import com.google.common.collect.ImmutableMap;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.devfile.server.model.impl.UserDevfileImpl;
import org.eclipse.che.api.factory.server.model.impl.AuthorImpl;
import org.eclipse.che.api.factory.server.model.impl.FactoryImpl;
import org.eclipse.che.api.ssh.server.model.impl.SshPairImpl;
import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ActionImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.DevfileImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.EndpointImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.EntrypointImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.EnvImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.MetadataImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ProjectImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.SourceImpl;
import org.eclipse.che.multiuser.machine.authentication.server.signature.model.impl.SignatureKeyPairImpl;
import org.eclipse.che.multiuser.permission.workspace.server.model.impl.WorkerImpl;
import org.eclipse.che.multiuser.resource.spi.impl.FreeResourcesLimitImpl;
import org.eclipse.che.multiuser.resource.spi.impl.ResourceImpl;

/**
 * Defines method for creating tests object instances.
 *
 * @author Yevhenii Voevodin
 */
public final class TestObjectsFactory {

  public static AccountImpl createAccount(String id) {
    return new AccountImpl(id, id + "_name", "test");
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
        id + "_name", id + "description", "default-env", null, null, null, null);
  }

  public static WorkspaceImpl createWorkspace(String id, Account account) {
    return new WorkspaceImpl(id, account, createWorkspaceConfig(id));
  }

  public static SshPairImpl createSshPair(String owner, String service, String name) {
    return new SshPairImpl(owner, service, name, "public-key", "private-key");
  }

  public static FactoryImpl createFactory(String id, String creator) {
    return new FactoryImpl(
        id,
        id + "-name",
        "4.0",
        createWorkspaceConfig(id),
        new AuthorImpl(creator, System.currentTimeMillis()),
        null,
        null);
  }

  public static WorkerImpl createWorker(String userId, String workspaceId) {
    return new WorkerImpl(workspaceId, userId, Arrays.asList("read", "write", "run"));
  }

  public static FreeResourcesLimitImpl createFreeResourcesLimit(String accountId) {
    return new FreeResourcesLimitImpl(
        accountId,
        Arrays.asList(new ResourceImpl("test1", 123, "mb"), new ResourceImpl("test2", 234, "h")));
  }

  public static SignatureKeyPairImpl createSignatureKeyPair(String workspaceId)
      throws NoSuchAlgorithmException {
    final KeyPairGenerator kpg;
    kpg = KeyPairGenerator.getInstance("RSA");
    kpg.initialize(512);
    final KeyPair pair = kpg.generateKeyPair();
    return new SignatureKeyPairImpl(workspaceId, pair.getPublic(), pair.getPrivate());
  }

  public static UserDevfileImpl createUserDevfile(String id, String name, Account account) {
    return new UserDevfileImpl(id, account, name, "descr", createDevfile(name));
  }

  public static DevfileImpl createDevfile(String name) {

    SourceImpl source1 =
        new SourceImpl(
            "type1",
            "http://location",
            "branch1",
            "point1",
            "tag1",
            "commit1",
            "sparseCheckoutDir1");
    ProjectImpl project1 = new ProjectImpl("project1", source1, "path1");

    SourceImpl source2 =
        new SourceImpl(
            "type2",
            "http://location",
            "branch2",
            "point2",
            "tag2",
            "commit2",
            "sparseCheckoutDir2");
    ProjectImpl project2 = new ProjectImpl("project2", source2, "path2");

    ActionImpl action1 =
        new ActionImpl("exec1", "component1", "run.sh", "/home/user/1", null, null);
    ActionImpl action2 =
        new ActionImpl("exec2", "component2", "run.sh", "/home/user/2", null, null);

    CommandImpl command1 =
        new CommandImpl(name + "-1", singletonList(action1), singletonMap("attr1", "value1"), null);
    CommandImpl command2 =
        new CommandImpl(name + "-2", singletonList(action2), singletonMap("attr2", "value2"), null);

    EntrypointImpl entrypoint1 =
        new EntrypointImpl(
            "parentName1",
            singletonMap("parent1", "selector1"),
            "containerName1",
            asList("command1", "command2"),
            asList("arg1", "arg2"));

    EntrypointImpl entrypoint2 =
        new EntrypointImpl(
            "parentName2",
            singletonMap("parent2", "selector2"),
            "containerName2",
            asList("command3", "command4"),
            asList("arg3", "arg4"));

    org.eclipse.che.api.workspace.server.model.impl.devfile.VolumeImpl volume1 =
        new org.eclipse.che.api.workspace.server.model.impl.devfile.VolumeImpl("name1", "path1");

    org.eclipse.che.api.workspace.server.model.impl.devfile.VolumeImpl volume2 =
        new org.eclipse.che.api.workspace.server.model.impl.devfile.VolumeImpl("name2", "path2");

    EnvImpl env1 = new EnvImpl("name1", "value1");
    EnvImpl env2 = new EnvImpl("name2", "value2");

    EndpointImpl endpoint1 = new EndpointImpl("name1", 1111, singletonMap("key1", "value1"));
    EndpointImpl endpoint2 = new EndpointImpl("name2", 2222, singletonMap("key2", "value2"));

    ComponentImpl component1 =
        new ComponentImpl(
            "kubernetes",
            "component1",
            "eclipse/che-theia/0.0.1",
            ImmutableMap.of("java.home", "/home/user/jdk11"),
            "https://mysite.com/registry/somepath1",
            "/dev.yaml",
            "refcontent1",
            ImmutableMap.of("app.kubernetes.io/component", "db"),
            asList(entrypoint1, entrypoint2),
            "image",
            "256G",
            "128M",
            "2",
            "130m",
            false,
            false,
            singletonList("command"),
            singletonList("arg"),
            asList(volume1, volume2),
            asList(env1, env2),
            asList(endpoint1, endpoint2));
    component1.setSelector(singletonMap("key1", "value1"));

    ComponentImpl component2 =
        new ComponentImpl(
            "kubernetes",
            "component2",
            "eclipse/che-theia/0.0.1",
            ImmutableMap.of(
                "java.home",
                "/home/user/jdk11aertwertert",
                "java.boolean",
                true,
                "java.long",
                123444L),
            "https://mysite.com/registry/somepath2",
            "/dev.yaml",
            "refcontent2",
            ImmutableMap.of("app.kubernetes.io/component", "webapp"),
            asList(entrypoint1, entrypoint2),
            "image",
            "256G",
            "256M",
            "3",
            "180m",
            false,
            false,
            singletonList("command"),
            singletonList("arg"),
            asList(volume1, volume2),
            asList(env1, env2),
            asList(endpoint1, endpoint2));
    component2.setSelector(singletonMap("key2", "value2"));

    DevfileImpl devfile =
        new DevfileImpl(
            "0.0.1",
            asList(project1, project2),
            asList(component1, component2),
            asList(command1, command2),
            singletonMap("attribute1", "value1"),
            new MetadataImpl(name));

    return devfile;
  }

  private TestObjectsFactory() {}
}
