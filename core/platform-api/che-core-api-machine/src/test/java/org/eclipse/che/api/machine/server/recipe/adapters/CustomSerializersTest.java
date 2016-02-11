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
package org.eclipse.che.api.machine.server.recipe.adapters;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import org.eclipse.che.api.machine.server.recipe.GroupImpl;
import org.eclipse.che.api.machine.server.recipe.PermissionsImpl;
import org.eclipse.che.api.machine.shared.Group;
import org.eclipse.che.api.machine.shared.Permissions;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * @author Anton Korneta
 */
@Test
public class CustomSerializersTest {

    @Test(expectedExceptions = JsonParseException.class)
    public void deserializeGroupsWithInvalidJson() {
        Gson gson = new GsonBuilder().registerTypeAdapter(Group.class, new GroupSerializer()).create();

        gson.fromJson("\\/*=invalid.", Group.class);
    }

    @Test
    public void deserializeGroupsWithValidJson() {
        String name = "group";
        String unit = "test";
        Gson gson = new GsonBuilder().registerTypeAdapter(Group.class, new GroupSerializer()).create();
        Group group = new GroupImpl(name, unit, Collections.emptyList());

        GroupImpl res = (GroupImpl)gson.fromJson(gson.toJson(group), Group.class);

        assertEquals(res.getName(), name);
        assertEquals(res.getUnit(), unit);
    }

    @Test(expectedExceptions = JsonParseException.class)
    public void deserializePermissionsWithInvalidJson() {
        Gson gson = new GsonBuilder().registerTypeAdapter(Permissions.class, new PermissionsSerializer()).create();

        gson.fromJson("\\/*=invalid.", Group.class);
    }

    @Test
    public void deserializePermissionsWithValidJson() {
        String name = "public";
        List<Group> groups = new ArrayList<>();
        groups.add(new GroupImpl("public", null, asList("read", "search")));
        PermissionsImpl permissions = new PermissionsImpl(null, groups);
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Group.class, new GroupSerializer())
                .registerTypeAdapter(Permissions.class, new PermissionsSerializer())
                .create();

        Permissions res = gson.fromJson(gson.toJson(permissions, Permissions.class), Permissions.class);

        Group deserializeGroup = res.getGroups().get(0);
        assertEquals(deserializeGroup.getName(), name);
        assertNull(deserializeGroup.getUnit());
    }
}
