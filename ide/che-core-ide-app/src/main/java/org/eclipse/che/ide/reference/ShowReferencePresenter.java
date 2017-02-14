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
package org.eclipse.che.ide.reference;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.reference.FqnProvider;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;

import java.util.Map;

/**
 * @author Dmitry Shnurenko
 * @author Vlad Zhukovskyi
 */
@Singleton
public class ShowReferencePresenter implements ShowReferenceView.ActionDelegate {

    private final ShowReferenceView        view;
    private final Map<String, FqnProvider> providers;

    @Inject
    public ShowReferencePresenter(ShowReferenceView view,
                                  Map<String, FqnProvider> providers) {
        this.view = view;
        this.view.setDelegate(this);

        this.providers = providers;
    }

    /**
     * Shows dialog which contains information about file fqn and path calculated from passed element.
     *
     * @param resource
     *         element for which fqn and path will be calculated
     */
    public void show(Resource resource) {
        final Optional<Project> project = resource.getRelatedProject();

        if (project.isPresent()) {
            final FqnProvider provider = providers.get(project.get().getType());

            try {
                view.show(provider.getFqn(resource), resource.getLocation());
            } catch (RuntimeException e) {
                view.show("", resource.getLocation());
            }

        }

    }
}
