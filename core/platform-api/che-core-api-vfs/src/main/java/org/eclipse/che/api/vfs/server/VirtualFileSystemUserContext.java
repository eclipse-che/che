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
package org.eclipse.che.api.vfs.server;

import org.eclipse.che.api.vfs.shared.dto.VirtualFileSystemInfo;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.user.User;

import java.util.*;

/**
 * Gives access to the current user context, e.g. uses HttpServletRequest to get info about Principal.
 *
 * @author andrew00x
 */
public abstract class VirtualFileSystemUserContext {

    protected VirtualFileSystemUserContext() {
    }

    /** Get current user. */
    public abstract VirtualFileSystemUser getVirtualFileSystemUser();

    // TODO: Temporary solution, need improve it.
    public static VirtualFileSystemUserContext newInstance() {
        return new DefaultVirtualFileSystemUserContext();
    }

    private static class DefaultVirtualFileSystemUserContext extends VirtualFileSystemUserContext {
        public VirtualFileSystemUser getVirtualFileSystemUser() {
            final EnvironmentContext context = EnvironmentContext.getCurrent();

            final User user = context.getUser();
            if (user == null) {
                return new VirtualFileSystemUser(VirtualFileSystemInfo.ANONYMOUS_PRINCIPAL, Collections.<String>emptySet());
            }
            final Set<String> groups = new HashSet<>(2);
            if (user.isMemberOf("workspace/developer")) {
                groups.add("workspace/developer");
            }
            if (user.isMemberOf("system/admin")) {
                groups.add("workspace/developer");
            }
            if (user.isMemberOf("workspace/admin")) {
                groups.add("workspace/admin");
            }
            return new VirtualFileSystemUser(user.getId(), groups);
        }
    }
}
