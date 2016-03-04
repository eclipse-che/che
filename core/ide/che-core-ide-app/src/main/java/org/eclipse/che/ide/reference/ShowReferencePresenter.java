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
package org.eclipse.che.ide.reference;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.util.loging.Log;

import java.util.Map;

/**
 * @author Dmitry Shnurenko
 */
@Singleton
public class ShowReferencePresenter implements ShowReferenceView.ActionDelegate {

    private final ShowReferenceView        view;
    private final Map<String, FqnProvider> providers;
    private final AppContext               appContext;

    @Inject
    public ShowReferencePresenter(ShowReferenceView view,
                                  Map<String, FqnProvider> providers,
                                  AppContext appContext) {
        this.view = view;
        this.view.setDelegate(this);

        this.providers = providers;
        this.appContext = appContext;
    }

    /**
     * Shows dialog which contains information about file fqn and path calculated from passed element.
     *
     * @param selectedElement
     *         element for which fqn and path will be calculated
     */
    public void show(HasStorablePath selectedElement) {
        String projectType = appContext.getCurrentProject().getProjectConfig().getType();

        FqnProvider provider = providers.get(projectType);

        String fqn = "";

        if (provider != null) {
            fqn = provider.getFqn(selectedElement);
        } else {
            Log.error(ShowReferencePresenter.class, "Fqn provider does not defined for " + projectType + " project type.");
        }

        view.show(fqn, selectedElement.getStorablePath());
    }
}
