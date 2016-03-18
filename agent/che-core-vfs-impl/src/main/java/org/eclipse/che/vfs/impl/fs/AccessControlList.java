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
package org.eclipse.che.vfs.impl.fs;

import org.eclipse.che.api.vfs.shared.dto.AccessControlEntry;
import org.eclipse.che.api.vfs.shared.dto.Principal;
import org.eclipse.che.dto.server.DtoFactory;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Access Control List (ACL) contains set of permissionMap assigned to each user.
 * <p/>
 * NOTE: Implementation is not thread-safe and required external synchronization.
 *
 * @author andrew00x
 */
public class AccessControlList {
    private final Map<Principal, Set<String>> permissionMap;

    public AccessControlList() {
        permissionMap = new HashMap<>(4);
    }

    public AccessControlList(AccessControlList accessControlList) {
        this(accessControlList.permissionMap);
    }

    public AccessControlList(Map<Principal, Set<String>> permissions) {
        this.permissionMap = copy(permissions);
    }

    private static Map<Principal, Set<String>> copy(Map<Principal, Set<String>> source) {
        Map<Principal, Set<String>> copy = new HashMap<>(source.size());
        for (Map.Entry<Principal, Set<String>> e : source.entrySet()) {
            if (!(e.getValue() == null || e.getValue().isEmpty())) {
                copy.put(DtoFactory.getInstance().clone(e.getKey()), new HashSet<>(e.getValue()));
            }
        }
        return copy;
    }

    public boolean isEmpty() {
        return permissionMap.isEmpty();
    }

    public List<AccessControlEntry> getEntries() {
        if (isEmpty()) {
            return Collections.emptyList();
        }
        List<AccessControlEntry> acl = new ArrayList<>(permissionMap.size());
        for (Map.Entry<Principal, Set<String>> e : permissionMap.entrySet()) {
            Set<String> basicPermissions = e.getValue();
            List<String> plainPermissions = new ArrayList<>(basicPermissions.size());
            for (String permission : e.getValue()) {
                plainPermissions.add(permission);
            }
            acl.add(DtoFactory.getInstance().createDto(AccessControlEntry.class)
                              .withPrincipal(DtoFactory.getInstance().clone(e.getKey()))
                              .withPermissions(plainPermissions)
                   );
        }
        return acl;
    }

    Map<Principal, Set<String>> getPermissionMap() {
        return copy(permissionMap);
    }

    public Set<String> getPermissions(Principal principal) {
        if (permissionMap.isEmpty()) {
            return null;
        }
        Set<String> userPermissions = permissionMap.get(principal);
        if (userPermissions == null) {
            return null;
        }
        return new HashSet<>(userPermissions);
    }

    public void update(List<AccessControlEntry> acl, boolean override) {
        if (acl.isEmpty() && !override) {
            // Nothing to do if there is no updates and override flag is not set.
            return;
        }

        if (override) {
            // remove all existed permissions
            permissionMap.clear();
        }

        for (AccessControlEntry ace : acl) {
            final Principal principal = DtoFactory.getInstance().clone(ace.getPrincipal());
            List<String> acePermissions = ace.getPermissions();
            if (acePermissions == null || acePermissions.isEmpty()) {
                permissionMap.remove(principal);
            } else {
                Set<String> permissions = permissionMap.get(principal);
                if (permissions == null) {
                    permissionMap.put(principal, permissions = new HashSet<>(4));
                } else {
                    permissions.clear();
                }
                permissions.addAll(acePermissions);
            }
        }
    }

    void write(DataOutput output) throws IOException {
        output.writeInt(permissionMap.size());
        for (Map.Entry<Principal, Set<String>> entry : permissionMap.entrySet()) {
            Principal principal = entry.getKey();
            Set<String> permissions = entry.getValue();
            output.writeUTF(principal.getName());
            output.writeUTF(principal.getType().toString());
            output.writeInt(permissions.size());
            for (String permission : permissions) {
                output.writeUTF(permission);
            }
        }
    }

    static AccessControlList read(DataInput input) throws IOException {
        int recordsNum = input.readInt();
        HashMap<Principal, Set<String>> permissionsMap = new HashMap<>(recordsNum);
        int readRecords = 0;
        while (readRecords < recordsNum) {
            String principalName = input.readUTF();
            String principalType = input.readUTF();
            int permissionsNum = input.readInt();
            if (permissionsNum > 0) {
                Set<String> permissions = new HashSet<>(4);
                int readPermissions = 0;
                while (readPermissions < permissionsNum) {
                    permissions.add(input.readUTF());
                    ++readPermissions;
                }
                final Principal principal = DtoFactory.getInstance().createDto(Principal.class)
                                                      .withName(principalName)
                                                      .withType(Principal.Type.valueOf(principalType));
                permissionsMap.put(principal, permissions);
            }
            ++readRecords;
        }
        return new AccessControlList(permissionsMap);
    }
}
