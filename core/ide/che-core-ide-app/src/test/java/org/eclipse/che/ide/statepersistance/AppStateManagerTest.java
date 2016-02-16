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
package org.eclipse.che.ide.statepersistance;

import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.machine.gwt.client.events.WsAgentStateEvent;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.workspace.shared.dto.UsersWorkspaceDto;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.event.WindowActionEvent;
import org.eclipse.che.ide.api.parts.PerspectiveManager;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.project.event.ProjectExplorerLoadedEvent;
import org.eclipse.che.ide.statepersistance.dto.ActionDescriptor;
import org.eclipse.che.ide.statepersistance.dto.AppState;
import org.eclipse.che.ide.statepersistance.dto.WorkspaceState;
import org.eclipse.che.ide.ui.toolbar.PresentationFactory;
import org.eclipse.che.ide.util.Pair;
import org.eclipse.che.ide.workspace.start.StopWorkspaceEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.eclipse.che.ide.statepersistance.AppStateManager.PREFERENCE_PROPERTY_NAME;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test covers {@link AppStateManager} functionality.
 *
 * @author Artem Zatsarynnyi
 * @author Dmitry Shnurenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class AppStateManagerTest {

    private static final String JSON      = "json";
    private static final String WS_ID     = "ws_id";
    private static final String ACTION_ID = "action_id";

    @Mock
    private PreferencesManager           preferencesManager;
    @Mock
    private AppContext                   appContext;
    @Mock
    private DtoFactory                   dtoFactory;
    @Mock
    private ActionManager                actionManager;
    @Mock
    private PresentationFactory          presentationFactory;
    @Mock
    private Provider<PerspectiveManager> perspectiveManagerProvider;
    @Mock
    private EventBus                     eventBus;

    //additional mocks
    @Mock
    private AppState             appState;
    @Mock
    private WorkspaceState       workspaceState;
    @Mock
    private Promise<Void>        voidPromise;
    @Mock
    private PersistenceComponent persistenceComponent;

    private AppStateManager appStateManager;

    @Before
    public void setUp() {
        UsersWorkspaceDto usersWorkspaceDto = mock(UsersWorkspaceDto.class);
        when(appContext.getWorkspace()).thenReturn(usersWorkspaceDto);
        when(usersWorkspaceDto.getId()).thenReturn(WS_ID);
        when(preferencesManager.getValue(PREFERENCE_PROPERTY_NAME)).thenReturn(JSON);
        when(dtoFactory.createDtoFromJson(JSON, AppState.class)).thenReturn(appState);

        Map<String, WorkspaceState> workspaceStates = new HashMap<>();
        workspaceStates.put(WS_ID, workspaceState);

        when(appState.getWorkspaces()).thenReturn(workspaceStates);

        Set<PersistenceComponent> persistenceComponents = new HashSet<>(Collections.singletonList(persistenceComponent));

        appStateManager = new AppStateManager(persistenceComponents,
                                              preferencesManager,
                                              appContext,
                                              dtoFactory,
                                              actionManager,
                                              presentationFactory,
                                              perspectiveManagerProvider,
                                              eventBus);
    }

    @Test
    public void shouldSubscribeOnEventBus() {
        verify(eventBus).addHandler(StopWorkspaceEvent.TYPE, appStateManager);
        verify(eventBus).addHandler(WindowActionEvent.TYPE, appStateManager);
        verify(eventBus).addHandler(WsAgentStateEvent.TYPE, appStateManager);
        verify(eventBus).addHandler(ProjectExplorerLoadedEvent.getType(), appStateManager);
    }

    @Test
    public void shouldReadStateFromPreferences() {
        verify(preferencesManager).getValue(PREFERENCE_PROPERTY_NAME);
        verify(dtoFactory).createDtoFromJson(JSON, AppState.class);
    }

    @Test
    public void shouldRestoreWorkspaceState() {
        ActionDescriptor actionDescriptor = mock(ActionDescriptor.class);
        when(actionDescriptor.getId()).thenReturn(ACTION_ID);
        when(workspaceState.getActions()).thenReturn(Collections.singletonList(actionDescriptor));
        Action action = mock(Action.class);
        when(actionManager.getAction(eq(ACTION_ID))).thenReturn(action);
        when(actionManager.performActions(Matchers.<List<Pair<Action, ActionEvent>>>anyObject(), eq(false))).thenReturn(voidPromise);

        appStateManager.onProjectsLoaded(mock(ProjectExplorerLoadedEvent.class));

        verify(appState).getWorkspaces();
        verify(workspaceState).getActions();
        verify(actionManager).getAction(ACTION_ID);
        verify(presentationFactory).getPresentation(action);
        verify(perspectiveManagerProvider).get();
        verify(actionManager).performActions(Matchers.<List<Pair<Action, ActionEvent>>>anyObject(), eq(false));
    }

    @Test
    public void shouldNotRestoreWorkspaceStateWhenNoSavedStates() {
        when(appState.getWorkspaces()).thenReturn(new HashMap<String, WorkspaceState>());

        appStateManager.onProjectsLoaded(mock(ProjectExplorerLoadedEvent.class));

        verify(actionManager, never()).performActions(Matchers.<List<Pair<Action, ActionEvent>>>anyObject(), anyBoolean());
    }

    @Test
    public void shouldNotRestoreWorkspaceStateWhenNoActions() {
        when(workspaceState.getActions()).thenReturn(Collections.<ActionDescriptor>emptyList());

        appStateManager.onProjectsLoaded(mock(ProjectExplorerLoadedEvent.class));

        verify(actionManager, never()).performActions(Matchers.<List<Pair<Action, ActionEvent>>>anyObject(), anyBoolean());
    }
}
