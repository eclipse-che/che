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
package org.eclipse.che.ide.workspace;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.workspace.event.WorkspaceRunningEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStartingEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppingEvent;
import org.eclipse.che.ide.bootstrap.BasicIDEInitializedEvent;
import org.eclipse.che.ide.ui.loaders.DownloadWorkspaceOutputEvent;
import org.eclipse.che.ide.ui.loaders.PopupLoader;
import org.eclipse.che.ide.ui.loaders.PopupLoaderFactory;
import org.eclipse.che.ide.ui.loaders.PopupLoaderMessages;

import java.util.HashMap;
import java.util.Map;

import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STARTING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPING;
import static org.eclipse.che.ide.workspace.WorkspaceStatusNotification.Phase.STARTING_WORKSPACE_RUNTIME;
import static org.eclipse.che.ide.workspace.WorkspaceStatusNotification.Phase.STOPPING_WORKSPACE;
import static org.eclipse.che.ide.workspace.WorkspaceStatusNotification.Phase.WORKSPACE_STOPPED;

/**
 * Manages loaders for loading phases.
 *
 * @author Vitaliy Guliy
 */
@Singleton
public class WorkspaceStatusNotification implements PopupLoader.ActionDelegate {

    private PopupLoaderFactory  popupLoaderFactory;
    private PopupLoaderMessages locale;
    private EventBus            eventBus;

    private Map<Phase, PopupLoader> popups = new HashMap<>();

    @Inject
    public WorkspaceStatusNotification(PopupLoaderFactory popupLoaderFactory,
                                       PopupLoaderMessages locale,
                                       EventBus eventBus,
                                       AppContext appContext) {
        this.popupLoaderFactory = popupLoaderFactory;
        this.locale = locale;
        this.eventBus = eventBus;

        eventBus.addHandler(BasicIDEInitializedEvent.TYPE, e -> {
            WorkspaceStatus status = appContext.getWorkspace().getStatus();

            if (status == STARTING) {
                show(STARTING_WORKSPACE_RUNTIME);
            } else if (status == STOPPING) {
                show(STOPPING_WORKSPACE);
            }
        });

        eventBus.addHandler(WorkspaceStartingEvent.TYPE, e -> {
            setSuccess(WORKSPACE_STOPPED);
            show(STARTING_WORKSPACE_RUNTIME);
        });

        eventBus.addHandler(WorkspaceRunningEvent.TYPE, e -> setSuccess(STARTING_WORKSPACE_RUNTIME));

        eventBus.addHandler(WorkspaceStoppingEvent.TYPE, e -> {
            setSuccess(STARTING_WORKSPACE_RUNTIME);
            show(STOPPING_WORKSPACE);
        });

        eventBus.addHandler(WorkspaceStoppedEvent.TYPE, e -> {
            setSuccess(STOPPING_WORKSPACE);
            setSuccess(STARTING_WORKSPACE_RUNTIME);
        });
    }

    /**
     * Displays a loader with a message.
     *
     * @param phase
     *         corresponding phase
     * @return loader instance
     */
    public PopupLoader show(Phase phase) {
        return show(phase, null);
    }

    /**
     * Displays a loader with a message and a widget.
     *
     * @param phase
     *         corresponding phase
     * @param widget
     *         additional widget to display
     * @return loader instance
     */
    public PopupLoader show(Phase phase, Widget widget) {
        PopupLoader popup = popups.get(phase);
        if (popup != null) {
            return popup;
        }

        // Create and show a popup
        switch (phase) {
            case STARTING_WORKSPACE_RUNTIME:
                popup = popupLoaderFactory.getPopup(locale.startingWorkspaceRuntime(), locale.startingWorkspaceRuntimeDescription());
                popup.showDownloadButton();
                break;
            case STARTING_WORKSPACE_AGENT:
                popup = popupLoaderFactory.getPopup(locale.startingWorkspaceAgent(), locale.startingWorkspaceAgentDescription());
                break;
            case CREATING_PROJECT:
                popup = popupLoaderFactory.getPopup(locale.creatingProject(), locale.creatingProjectDescription());
                break;
            case CREATING_WORKSPACE_SNAPSHOT:
                popup = popupLoaderFactory.getPopup(locale.snapshottingWorkspace(), locale.snapshottingWorkspaceDescription());
                break;
            case STOPPING_WORKSPACE:
                popup = popupLoaderFactory.getPopup(locale.stoppingWorkspace(), locale.stoppingWorkspaceDescription());
                break;
            case WORKSPACE_STOPPED:
                popup = popupLoaderFactory.getPopup(locale.workspaceStopped(), locale.workspaceStoppedDescription(), widget);
                break;
        }

        popup.setDelegate(this);
        popups.put(phase, popup);
        return popup;
    }

    /**
     * Sets phase succeeded and hides corresponding loader.
     *
     * @param phase
     *         corresponding phase
     */
    public void setSuccess(Phase phase) {
        PopupLoader popup = popups.get(phase);
        if (popup != null) {
            // Hide the loader if status is SUCCESS
            popups.remove(phase);
            popup.setSuccess();
        }
    }

    /**
     * Sets phase filed.
     *
     * @param phase
     *         corresponding phase
     */
    public void setError(Phase phase) {
        PopupLoader popup = popups.get(phase);
        if (popup != null) {
            // Don't hide the loader with status ERROR
            popups.remove(phase);
            popup.setError();
        }
    }

    @Override
    public void onDownloadLogs() {
        eventBus.fireEvent(new DownloadWorkspaceOutputEvent());
    }

    public enum Phase {
        STARTING_WORKSPACE_RUNTIME,
        STARTING_WORKSPACE_AGENT,
        CREATING_PROJECT,
        CREATING_WORKSPACE_SNAPSHOT,
        STOPPING_WORKSPACE,
        WORKSPACE_STOPPED
    }

}
