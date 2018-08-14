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
package org.eclipse.che.multiuser.integration.jpa.cascaderemoval;

import static java.util.Arrays.asList;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.factory.server.model.impl.AuthorImpl;
import org.eclipse.che.api.factory.server.model.impl.FactoryImpl;
import org.eclipse.che.api.ssh.server.model.impl.SshPairImpl;
import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.api.workspace.server.stack.image.StackIcon;
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
        null,
        null);
  }

  public static StackImpl createStack(String id, String name) {
    return StackImpl.builder()
        .setId(id)
        .setName(name)
        .setCreator("user123")
        .setDescription(id + "-description")
        .setScope(id + "-scope")
        .setWorkspaceConfig(createWorkspaceConfig(id + "test"))
        .setTags(asList(id + "-tag1", id + "-tag2"))
        .setComponents(
            asList(
                new StackComponentImpl(id + "-component1", id + "-component1-version"),
                new StackComponentImpl(id + "-component2", id + "-component2-version")))
        .setStackIcon(
            new StackIcon(id + "-icon", id + "-media-type", "0x1234567890abcdef".getBytes()))
        .build();
  }

  public static WorkerImpl createWorker(String userId, String workspaceId) {
    return new WorkerImpl(workspaceId, userId, Arrays.asList("read", "write", "run"));
  }

  public static FreeResourcesLimitImpl createFreeResourcesLimit(String accountId) {
    return new FreeResourcesLimitImpl(
        accountId,
        Arrays.asList(new ResourceImpl("test1", 123, "mb"), new ResourceImpl("test2", 234, "h")));
  }

  private TestObjectsFactory() {}
}
