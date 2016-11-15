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
package org.eclipse.che.ide.workspace.state;

import elemental.json.Json;
import elemental.json.JsonObject;

import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.Perspective;
import org.eclipse.che.ide.api.parts.PerspectiveManager;
import org.eclipse.che.ide.menu.MainMenuPresenter;
import org.eclipse.che.ide.menu.StatusPanelGroupPresenter;
import org.eclipse.che.ide.ui.toolbar.ToolbarPresenter;
import org.eclipse.che.ide.workspace.WorkspacePresenter;
import org.eclipse.che.ide.workspace.WorkspaceView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Evgen Vidolob
 */
@RunWith(MockitoJUnitRunner.class)
public class WorkspacePresenterPersistenceTest {


    @Mock
    private WorkspaceView             workspaceView;
    @Mock
    private Perspective               perspective1;
    @Mock
    private Perspective               perspective2;
    @Mock
    private MainMenuPresenter         mainMenuPresenter;
    @Mock
    private StatusPanelGroupPresenter statusPanelGroupPresenter;
    @Mock
    private ToolbarPresenter          toolbarPresenter;
    @Mock
    private PartPresenter             part1;

    private WorkspacePresenter presenter;
    private PerspectiveManager perspectiveManager;

    @Before
    public void setUp() throws Exception {
        Map<String, Perspective> map = new HashMap<>();
        map.put("perspective1", perspective1);
        map.put("perspective2", perspective2);
        perspectiveManager = new PerspectiveManager(map, "perspective1");
        presenter = new WorkspacePresenter(workspaceView,
                                           perspectiveManager,
                                           mainMenuPresenter,
                                           statusPanelGroupPresenter,
                                           toolbarPresenter,
                                           "perspective1");

    }

    @Test
    public void shouldStorePerspectives() throws Exception {
        when(perspective1.getState()).thenReturn(Json.createObject());
        when(perspective2.getState()).thenReturn(Json.createObject());

        JsonObject state = presenter.getState();
        JsonObject perspectives = state.getObject("perspectives");
        assertThat(perspectives).isNotNull();
        assertThat(perspectives.getObject("perspective1")).isNotNull();
    }

    @Test
    public void shouldRestoreStorePerspectives() throws Exception {
        JsonObject state = Json.createObject();
        state.put("currentPerspectiveId", "perspective2");
        JsonObject perspectives = Json.createObject();
        state.put("perspectives", perspectives);
        JsonObject perspective1State = Json.createObject();
        perspectives.put("perspective1", perspective1State);

        presenter.loadState(state);

        verify(perspective1).loadState(perspective1State);
    }
}
