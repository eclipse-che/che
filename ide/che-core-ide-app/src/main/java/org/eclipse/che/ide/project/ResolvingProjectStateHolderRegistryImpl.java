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
package org.eclipse.che.ide.project;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.util.loging.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Registry for implementations of {@link ResolvingProjectStateHolder}.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class ResolvingProjectStateHolderRegistryImpl implements ResolvingProjectStateHolderRegistry {
    private final Map<String, ResolvingProjectStateHolder> resolvingProjectStateHolders = new HashMap<>();

    @Inject(optional = true)
    private void register(Set<ResolvingProjectStateHolder> holders) {
        for (ResolvingProjectStateHolder holder : holders) {
            final String projectType = holder.getProjectType();
            if (this.resolvingProjectStateHolders.containsKey(projectType)) {
                Log.warn(this.getClass(), "Resolving project state holder for '" + projectType + "' project type is already registered.");
            } else {
                this.resolvingProjectStateHolders.put(projectType, holder);
            }
        }
    }

    /**
     * Returns the state holder of Resolving project process for the specified project type or {@code null} if none.
     *
     * @param projectType
     *         the project type for which you need to get corresponding holder
     * @return the holder of Resolving project sate process for the specified project type or {@code null} if none
     */
    @Nullable
    public ResolvingProjectStateHolder getResolvingProjectStateHolder(String projectType) {
        return resolvingProjectStateHolders.get(projectType);
    }
}
