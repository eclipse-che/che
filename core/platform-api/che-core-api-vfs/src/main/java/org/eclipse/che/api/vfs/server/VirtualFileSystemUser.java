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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a> */
public class VirtualFileSystemUser {
    private final String             userId;
    private final Collection<String> groups;

    public VirtualFileSystemUser(String userId, Set<String> groups) {
        this.userId = userId;
        this.groups = Collections.unmodifiableSet(new HashSet<>(groups));
    }

    public String getUserId() {
        return userId;
    }

    public Collection<String> getGroups() {
        return groups;
    }

    @Override
    public String toString() {
        return "VirtualFileSystemUser{" +
               "userId='" + userId + '\'' +
               ", groups=" + groups +
               '}';
    }
}
