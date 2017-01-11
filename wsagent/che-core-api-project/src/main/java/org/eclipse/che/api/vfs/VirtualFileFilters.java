/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.vfs;

import java.nio.file.PathMatcher;
import java.util.List;

/**
 * Provides factory methods to create AND, OR filters based on set of VirtualFileFilter.
 *
 * @author andrew00x
 */
public class VirtualFileFilters {

    public static VirtualFileFilter createAndFilter(VirtualFileFilter filterOne, VirtualFileFilter filterTwo, VirtualFileFilter... filters) {
        if (filters == null || filters.length == 0) {
            return new AndFilter(new VirtualFileFilter[]{filterOne, filterTwo});
        }
        VirtualFileFilter[] copy = new VirtualFileFilter[filters.length + 2];
        System.arraycopy(filters, 0, copy, 2, filters.length);
        return new AndFilter(copy);
    }

    public static VirtualFileFilter createAndFilter(List<VirtualFileFilter> filters) {
        if (filters == null || filters.size() < 2) {
            throw new IllegalArgumentException("At least two filters required. ");
        }
        return new AndFilter(filters.toArray(new VirtualFileFilter[filters.size()]));
    }

    private static class AndFilter implements VirtualFileFilter {
        final VirtualFileFilter[] filters;

        AndFilter(VirtualFileFilter[] filters) {
            this.filters = filters;
        }

        @Override
        public boolean accept(VirtualFile file) {
            for (VirtualFileFilter filter : filters) {
                if (!filter.accept(file)) {
                    return false;
                }
            }
            return true;
        }
    }

    public static VirtualFileFilter createOrFilter(VirtualFileFilter filterOne, VirtualFileFilter filterTwo, VirtualFileFilter... filters) {
        if (filters == null || filters.length == 0) {
            return new AndFilter(new VirtualFileFilter[]{filterOne, filterTwo});
        }
        VirtualFileFilter[] copy = new VirtualFileFilter[filters.length + 2];
        System.arraycopy(filters, 0, copy, 2, filters.length);
        return new OrFilter(copy);
    }

    public static VirtualFileFilter createOrFilter(List<VirtualFileFilter> filters) {
        if (filters == null || filters.size() < 2) {
            throw new IllegalArgumentException("At least two filters required. ");
        }
        return new OrFilter(filters.toArray(new VirtualFileFilter[filters.size()]));
    }

    private static class OrFilter implements VirtualFileFilter {
        final VirtualFileFilter[] filters;

        OrFilter(VirtualFileFilter[] filters) {
            this.filters = filters;
        }

        @Override
        public boolean accept(VirtualFile file) {
            for (VirtualFileFilter filter : filters) {
                if (filter.accept(file)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static VirtualFileFilter dotGitFilter() {
        return DOT_GIT_FILTER;
    }

    private static final VirtualFileFilter DOT_GIT_FILTER = file -> !(".git".equals(file.getName()));

    public static VirtualFileFilter wrap(PathMatcher pathMatcher) {
        return file -> pathMatcher.matches(file.toIoFile().toPath());
    }

    private VirtualFileFilters() {
    }
}
