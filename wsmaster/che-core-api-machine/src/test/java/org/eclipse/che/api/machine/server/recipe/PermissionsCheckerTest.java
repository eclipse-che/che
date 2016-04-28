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
package org.eclipse.che.api.machine.server.recipe;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.machine.shared.Group;
import org.eclipse.che.api.machine.shared.ManagedRecipe;
//import org.eclipse.che.api.workspace.server.dao.Member;
//import org.eclipse.che.api.workspace.server.dao.MemberDao;
import org.mockito.InjectMocks;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

// TODO fix after workspace memberships refactoring

/**
 * @author Eugene Voevodin
 */
@Listeners(value = {MockitoTestNGListener.class})
public class PermissionsCheckerTest {

//    @Mock
//    MemberDao              memberDao;
    @InjectMocks
    PermissionsCheckerImpl permissionsChecker;

    @Test
    public void userShouldHaveAccessToRecipeWhenHeIsListedInRecipeUsersPermissions() throws ServerException {
        final Map<String, List<String>> users = singletonMap("user-id", asList("read", "write"));
        final ManagedRecipe recipe = new RecipeImpl().withCreator("someone")
                                              .withPermissions(new PermissionsImpl(users, null));

        assertTrue(permissionsChecker.hasAccess(recipe, "user-id", "read"), "should have read permission");
        assertTrue(permissionsChecker.hasAccess(recipe, "user-id", "write"), "should have write permission");
        assertFalse(permissionsChecker.hasAccess(recipe, "user-id", "update_acl"), "should not have update_acl permission");
    }

//    @Test
//    public void userShouldHaveAccessToRecipeWhenHeIsInTheGroupWhichIsListedInRecipePermissions() throws ServerException {
//        final Group group = new GroupImpl("workspace/admin", "workspace123", asList("read", "write"));
//        final ManagedRecipe recipe = new RecipeImpl().withCreator("someone")
//                                              .withPermissions(new PermissionsImpl(null, asList(group)));
//        when(memberDao.getUserRelationships("user-id")).thenReturn(asList(new Member().withUserId("user-id")
//                                                                                      .withWorkspaceId("workspace123")
//                                                                                      .withRoles(asList("workspace/admin"))));
//
//        assertTrue(permissionsChecker.hasAccess(recipe, "user-id", "read"), "should have read permission");
//        assertTrue(permissionsChecker.hasAccess(recipe, "user-id", "write"), "should have write permission");
//        assertFalse(permissionsChecker.hasAccess(recipe, "user-id", "update_acl"), "should not have update_acl permission");
//    }

//    @Test
//    public void userShouldNotHaveAccessToRecipeWhenHeIsNotInTheGroupWhichIsListedInRecipePermissions() throws ServerException {
//        final Group group = new GroupImpl("workspace/admin", "workspace123", asList("read", "write"));
//        final ManagedRecipe recipe = new RecipeImpl().withCreator("someone")
//                                              .withPermissions(new PermissionsImpl(null, asList(group)));
//        when(memberDao.getUserRelationships("user-id")).thenReturn(asList(new Member().withUserId("user-id")
//                                                                                      .withWorkspaceId("workspace123")
//                                                                                      .withRoles(asList("workspace/developer"))));
//
//        assertFalse(permissionsChecker.hasAccess(recipe, "user-id", "read"), "should not have read permission");
//        assertFalse(permissionsChecker.hasAccess(recipe, "user-id", "write"), "should not have write permission");
//        assertFalse(permissionsChecker.hasAccess(recipe, "user-id", "update_acl"), "should not have update_acl permission");
//    }

    @Test
    public void userShouldHaveAccessToRecipeWhenRecipePermissionsContainsPublicGroup() throws ServerException {
        final Group group = new GroupImpl("public", null, asList("read"));
        final ManagedRecipe recipe = new RecipeImpl().withCreator("someone")
                                              .withPermissions(new PermissionsImpl(null, asList(group)));

        assertTrue(permissionsChecker.hasAccess(recipe, "user-id", "read"), "should have read permission");
        assertFalse(permissionsChecker.hasAccess(recipe, "user-id", "write"), "should not have write permission");
        assertFalse(permissionsChecker.hasAccess(recipe, "user-id", "update_acl"), "should not have update_acl permission");
    }
//
//    @Test
//    public void groupPermissionsShouldHaveLessPriorityThenUserPermissions() throws ServerException {
//        final Group group = new GroupImpl("workspace/developer", "workspace123", asList("read", "write", "update_acl"));
//        final Map<String, List<String>> users = singletonMap("user-id", asList("read"));
//        final ManagedRecipe recipe = new RecipeImpl().withCreator("someone")
//                                              .withPermissions(new PermissionsImpl(users, asList(group)));
//        when(memberDao.getUserRelationships("user-id")).thenReturn(asList(new Member().withUserId("user-id")
//                                                                                      .withWorkspaceId("workspace123")
//                                                                                      .withRoles(asList("workspace/developer"))));
//
//        assertTrue(permissionsChecker.hasAccess(recipe, "user-id", "read"), "should have read permission");
//        assertFalse(permissionsChecker.hasAccess(recipe, "user-id", "write"), "should not have write permission");
//        assertFalse(permissionsChecker.hasAccess(recipe, "user-id", "update_acl"), "should not have update_acl permission");
//    }

    @Test(enabled = false)
    public void shouldReturnFalseIfRecipeDoesNotHavePermissions() throws ServerException {
        final ManagedRecipe recipe = new RecipeImpl().withCreator("user-id");

        assertFalse(permissionsChecker.hasAccess(recipe, "someone", "read"));
    }
}
