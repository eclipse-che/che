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

import elemental.json.Json;
import elemental.json.JsonObject;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import org.eclipse.che.ide.api.component.StateComponent;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.mvp.Presenter;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.PartStack;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.Perspective;
import org.eclipse.che.ide.api.parts.PerspectiveManager;
import org.eclipse.che.ide.api.parts.PerspectiveManager.PerspectiveTypeListener;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.menu.MainMenuPresenter;
import org.eclipse.che.ide.menu.StatusPanelGroupPresenter;
import org.eclipse.che.ide.ui.toolbar.MainToolbar;
import org.eclipse.che.ide.ui.toolbar.ToolbarPresenter;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Root Presenter that implements Workspace logic. Descendant Presenters are injected
 * via constructor and exposed to corresponding UI containers. It contains Menu,
 * Toolbar and WorkBench Presenter to expose their views into corresponding places and to
 * maintain their interactions.
 *
 * @author Nikolay Zamosenchuk
 * @author Dmitry Shnurenko
 */
@Singleton
public class WorkspacePresenter implements Presenter, WorkspaceView.ActionDelegate, WorkspaceAgent, PerspectiveTypeListener,
                                           StateComponent {

    private final WorkspaceView             view;
    private final String                    defaultPerspectiveId;
    private final MainMenuPresenter         mainMenu;
    private final StatusPanelGroupPresenter bottomMenu;
    private final ToolbarPresenter          toolbarPresenter;
    private final PerspectiveManager        perspectiveManager;

    private Perspective activePerspective;

    @Inject
    public WorkspacePresenter(WorkspaceView view,
                              PerspectiveManager perspectiveManager,
                              MainMenuPresenter mainMenu,
                              StatusPanelGroupPresenter bottomMenu,
                              @MainToolbar ToolbarPresenter toolbarPresenter,
                              @Named("defaultPerspectiveId") String defaultPerspectiveId) {
        this.view = view;
        this.defaultPerspectiveId = defaultPerspectiveId;
        this.view.setDelegate(this);

        this.toolbarPresenter = toolbarPresenter;
        this.mainMenu = mainMenu;
        this.bottomMenu = bottomMenu;

        this.perspectiveManager = perspectiveManager;
        this.perspectiveManager.addListener(this);

        onPerspectiveChanged();
    }

    /** {@inheritDoc} */
    @Override
    public void go(AcceptsOneWidget container) {
        mainMenu.go(view.getMenuPanel());
        toolbarPresenter.go(view.getToolbarPanel());
        bottomMenu.go(view.getStatusPanel());

        container.setWidget(view);
    }

    /** {@inheritDoc} */
    @Override
    public void onPerspectiveChanged() {
        activePerspective = perspectiveManager.getActivePerspective();

        if (activePerspective == null) {
            throw new IllegalStateException("Current perspective isn't defined " + perspectiveManager.getPerspectiveId());
        }

        activePerspective.go(view.getPerspectivePanel());
    }

    public void setActivePart(@NotNull PartPresenter part, @NotNull PartStackType type) {
        activePerspective.setActivePart(part, type);
    }

    /** {@inheritDoc} */
    @Override
    public void setActivePart(PartPresenter part) {
        activePerspective.setActivePart(part);
    }

    /** {@inheritDoc} */
    @Override
    public void openPart(PartPresenter part, PartStackType type) {
        openPart(part, type, null);
    }

    /** {@inheritDoc} */
    @Override
    public void openPart(PartPresenter part, PartStackType type, Constraints constraint) {
        activePerspective.addPart(part, type, constraint);
    }

    /** {@inheritDoc} */
    @Override
    public void hidePart(PartPresenter part) {
        activePerspective.hidePart(part);
    }

    /** {@inheritDoc} */
    @Override
    public void removePart(PartPresenter part) {
        activePerspective.removePart(part);
    }

    /**
     * Retrieves the instance of the {@link org.eclipse.che.ide.api.parts.PartStack} for given {@link PartStackType}
     *
     * @param type
     *         one of the enumerated type {@link org.eclipse.che.ide.api.parts.PartStackType}
     * @return the part stack found, else null
     */
    public PartStack getPartStack(PartStackType type) {
        return activePerspective.getPartStack(type);
    }

    @Override
    public JsonObject getState() {
        JsonObject state = Json.createObject();
        JsonObject perspectivesJs = Json.createObject();
        state.put("perspectives", perspectivesJs);
        Map<String, Perspective> perspectives = perspectiveManager.getPerspectives();
        for (Map.Entry<String, Perspective> entry : perspectives.entrySet()) {
            //store only default perspective
            if (entry.getKey().equals(defaultPerspectiveId)) {
                perspectivesJs.put(entry.getKey(), entry.getValue().getState());
            }
        }
        return state;
    }

    @Override
    public void loadState(JsonObject state) {
        if (state.hasKey("perspectives")) {
            JsonObject perspectives = state.getObject("perspectives");
            Map<String, Perspective> perspectiveMap = perspectiveManager.getPerspectives();
            for (String key : perspectives.keys()) {
                if (perspectiveMap.containsKey(key)) {
                    perspectiveMap.get(key).loadState(perspectives.getObject(key));
                }
            }
        }
    }
}
