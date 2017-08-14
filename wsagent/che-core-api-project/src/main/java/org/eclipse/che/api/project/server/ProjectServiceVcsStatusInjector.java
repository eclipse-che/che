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
package org.eclipse.che.api.project.server;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.shared.dto.ItemReference;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Injects VCS status to attributes of {@link ItemReference} dto.
 *
 * @author Igor Vinokur
 */
@Singleton
public class ProjectServiceVcsStatusInjector {

    private final ProjectManager         projectManager;
    private final Set<VcsStatusProvider> vcsStatusProviders;

    @Inject
    public ProjectServiceVcsStatusInjector(ProjectManager projectManager, Set<VcsStatusProvider> vcsStatusProviders) {
        this.projectManager = projectManager;
        this.vcsStatusProviders = vcsStatusProviders;
    }

    /**
     * Find related VCS provider and set VCS status of {@link ItemReference} file
     * to it's attributes if VCS provider is present.
     *
     * @param itemReference
     *         file to update
     */
    public ItemReference injectVcsStatus(ItemReference itemReference) throws ServerException, NotFoundException {
        List<String> vcsAttributes = projectManager.getProject(itemReference.getProject()).getAttributes().get("vcs.provider.name");
        Optional<VcsStatusProvider> optional =
                vcsStatusProviders.stream()
                                  .filter(vcsStatusProvider -> vcsStatusProvider.getVcsName()
                                                                                .equals(vcsAttributes != null ? vcsAttributes.get(0)
                                                                                                              : null))
                                  .findAny();
        if (optional.isPresent()) {
            Map<String, String> attributes = new HashMap<>(itemReference.getAttributes());
            attributes.put("vcs.status", optional.get().getStatus(itemReference.getPath()).toString());
            itemReference.setAttributes(attributes);
        }
        return itemReference;
    }
}
