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

import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * 
 * Provides the underlying system filter for virtual file paths.
 * 
 * @author Tareq Sharafy (tareq.sharafy@sap.com)
 */
@Singleton
public class SystemPathsFilter {

    /** A dummy system filter that accepts all paths */
    public static final SystemPathsFilter ANY = new SystemPathsFilter(Collections.emptySet());

    private final Set<SystemVirtualFilePathFilter> filters;

    @Inject
    public SystemPathsFilter(Set<SystemVirtualFilePathFilter> filters) {
        this.filters = filters;
    }

    public boolean accept(String workspace, Path path) {
        return filters == null || filters.stream().allMatch(filter -> filter.accept(workspace, path));
    }

}
