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
package org.eclipse.che.ide.projecttree;

import com.google.inject.Inject;

import org.eclipse.che.ide.api.project.tree.TreeStructureProvider;
import org.eclipse.che.ide.api.project.tree.TreeStructureProviderRegistry;
import org.eclipse.che.ide.api.project.tree.generic.GenericTreeStructureProvider;
import org.eclipse.che.ide.util.loging.Log;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Implementation for {@link TreeStructureProviderRegistry}.
 *
 * @author Artem Zatsarynnyi
 */
public class TreeStructureProviderRegistryImpl implements TreeStructureProviderRegistry {
    private final Map<String, TreeStructureProvider> treeProviders;
    private final Map<String, String>                projectType2TreeProvider;
    private final GenericTreeStructureProvider       defaultTreeStructureProvider;

    @Inject
    public TreeStructureProviderRegistryImpl(GenericTreeStructureProvider defaultTreeStructureProvider) {
        treeProviders = new HashMap();
        projectType2TreeProvider = new HashMap();
        this.defaultTreeStructureProvider = defaultTreeStructureProvider;
    }

    @Inject(optional = true)
    private void register(Set<TreeStructureProvider> providers) {
        for (TreeStructureProvider provider : providers) {
            final String id = provider.getId();
            if (treeProviders.get(id) == null) {
                treeProviders.put(id, provider);
            } else {
                Log.warn(TreeStructureProviderRegistryImpl.class, "Tree structure provider with ID " + id + " already registered.");
            }
        }
    }

    @Override
    public void associateProjectTypeToTreeProvider(@NotNull String projectTypeId, @NotNull String treeStructureProviderId) {
        projectType2TreeProvider.put(projectTypeId, treeStructureProviderId);
    }

    @NotNull
    @Override
    public TreeStructureProvider getTreeStructureProvider(@NotNull String projectTypeId) {
        final String providerId = projectType2TreeProvider.get(projectTypeId);
        if (providerId != null) {
            TreeStructureProvider provider = treeProviders.get(providerId);
            if (provider != null) {
                return provider;
            }
        }
        return getDefaultTreeStructureProvider();
    }

    private TreeStructureProvider getDefaultTreeStructureProvider() {
        return defaultTreeStructureProvider;
    }
}
