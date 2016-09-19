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
package org.eclipse.che.ide.ui.loaders;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages loaders for loading phases.
 *
 * @author Vitaliy Guliy
 */
@Singleton
public class LoaderPresenter {

    public enum Phase {
        STARTING_WORKSPACE_RUNTIME, STARTING_WORKSPACE_AGENT, CREATING_PROJECT
    }

    public enum Status {
        LOADING, SUCCESS, ERROR;
    }

    private PopupLoaderFactory      popupLoaderFactory;
    private PopupLoaderMessages     locale;

    private Map<Phase, PopupLoader> popups = new HashMap<>();

    @Inject
    public LoaderPresenter(PopupLoaderFactory popupLoaderFactory,
                           PopupLoaderMessages locale) {
        this.popupLoaderFactory = popupLoaderFactory;
        this.locale = locale;
    }

    /**
     * Sets phase and status.
     *
     * @param phase
     *          phase
     * @param status
     *          status
     */
    public void setProgress(Phase phase, Status status) {
        PopupLoader popup = popups.get(phase);

        if (popup == null) {
            // Create and show a loader
            switch (phase) {
                case STARTING_WORKSPACE_RUNTIME:
                    popup = popupLoaderFactory.getPopup(locale.startingWorkspaceRuntime(), locale.startingWorkspaceRuntimeDescription());
                    break;
                case STARTING_WORKSPACE_AGENT:
                    popup = popupLoaderFactory.getPopup(locale.startingWorkspaceAgent(), locale.startingWorkspaceAgentDescription());
                    break;
                case CREATING_PROJECT:
                    popup = popupLoaderFactory.getPopup(locale.creatingProject(), locale.creatingProjectDescription());
                    break;
            }
            popups.put(phase, popup);

        } else if (Status.SUCCESS == status) {
            // Hide the loader if status is SUCCESS
            popups.remove(phase);
            popup.setSuccess();

        } else if (Status.ERROR == status) {
            // Don't hide the loader with status ERROR
            popups.remove(phase);
            popup.setError();
        }
    }

}
