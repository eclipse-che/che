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
package org.eclipse.che.core.db.jpa;

import com.google.common.collect.ImmutableMap;

import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.api.machine.server.model.impl.SnapshotImpl;
import org.eclipse.che.api.machine.server.recipe.RecipeImpl;
import org.eclipse.che.api.ssh.server.model.impl.SshPairImpl;
import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackSourceImpl;
import org.eclipse.che.api.workspace.server.stack.image.StackIcon;

import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;

/**
 * Defines method for creating tests object instances.
 *
 * @author Yevhenii Voevodin
 */
public final class TestObjectsFactory {

    public static UserImpl createUser(String id) {
        return new UserImpl(id,
                            id + "@eclipse.org",
                            id + "_name",
                            "password",
                            asList(id + "_alias1", id + "_alias2"));
    }

    public static ProfileImpl createProfile(String userId) {
        return new ProfileImpl(userId, new HashMap<>(ImmutableMap.of("attribute1", "value1",
                                                                     "attribute2", "value2",
                                                                     "attribute3", "value3")));
    }

    public static Map<String, String> createPreferences() {
        return new HashMap<>(ImmutableMap.of("preference1", "value1",
                                             "preference2", "value2",
                                             "preference3", "value3"));
    }

    public static WorkspaceConfigImpl createWorkspaceConfig(String id) {
        return new WorkspaceConfigImpl(id + "_name",
                                       id + "description",
                                       "default-env",
                                       null,
                                       null,
                                       null);
    }

    public static WorkspaceImpl createWorkspace(String id, Account account) {
        return new WorkspaceImpl(id, account, createWorkspaceConfig(id));
    }

    public static SshPairImpl createSshPair(String owner, String service, String name) {
        return new SshPairImpl(owner, service, name, "public-key", "private-key");
    }

    public static SnapshotImpl createSnapshot(String snapshotId, String workspaceId) {
        return new SnapshotImpl(snapshotId,
                                "type",
                                null,
                                System.currentTimeMillis(),
                                workspaceId,
                                snapshotId + "_description",
                                true,
                                "dev-machine",
                                snapshotId + "env-name");
    }

    public static RecipeImpl createRecipe(String id) {
        return new RecipeImpl(id,
                              "recipe-name-" + id,
                              "recipe-creator",
                              "recipe-type",
                              "recipe-script",
                              asList("recipe-tag1", "recipe-tag2"),
                              "recipe-description");
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
                        .setComponents(asList(new StackComponentImpl(id + "-component1", id + "-component1-version"),
                                              new StackComponentImpl(id + "-component2", id + "-component2-version")))
                        .setSource(new StackSourceImpl(id + "-type", id + "-origin"))
                        .setStackIcon(new StackIcon(id + "-icon",
                                                    id + "-media-type",
                                                    "0x1234567890abcdef".getBytes()))
                        .build();
    }

    private TestObjectsFactory() {}
}

