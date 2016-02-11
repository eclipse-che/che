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
import org.eclipse.che.api.machine.shared.Permissions;
//import org.eclipse.che.api.workspace.server.dao.Member;
//import org.eclipse.che.api.workspace.server.dao.MemberDao;

import javax.inject.Singleton;
import java.util.List;

// TODO fix after workspace memberships refactoring

/**
 * TODO add doc
 *
 * @author Eugene Voevodin
 */
@Singleton
public class PermissionsCheckerImpl implements PermissionsChecker {

//    private final MemberDao memberDao;

//    @Inject
//    private PermissionsCheckerImpl(MemberDao memberDao) {
//        this.memberDao = memberDao;
//    }

    @Override
    public boolean hasAccess(ManagedRecipe recipe, String userId, String permission) throws ServerException {
        //TODO consider logic when creator has 'read', 'write' and 'update_acl' permissions
        //if user is recipe creator he has access to it
        if (recipe.getCreator().equals(userId)) {
            return true;
        }

        //if recipe doesn't have any permissions it may be accessed only by its creator
        final Permissions permissions = recipe.getPermissions();
        if (permissions == null) {
            return false;
        }

        //check user permissions
        final List<String> userPerms = permissions.getUsers().get(userId);
        if (userPerms != null) {
            return userPerms.contains(permission);
        }

        //check group permissions
        for (Group group : permissions.getGroups()) {
            if (!group.getAcl().contains(permission)) {
                continue;
            }
            if (group.getName().equals("public")) {
                return true;
            }
//            //check user relationships for this group
//            for (Member member : relationships) {
//                if (group.getUnit().equals(member.getWorkspaceId()) && member.getRoles().contains(group.getName())) {
//                    return true;
//                }
//            }
        }
        return false;
    }
}
