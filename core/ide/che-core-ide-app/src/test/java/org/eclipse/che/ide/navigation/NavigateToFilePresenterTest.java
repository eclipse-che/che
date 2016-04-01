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
package org.eclipse.che.ide.navigation;

import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.machine.gwt.client.events.WsAgentStateEvent;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.project.node.Node;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test for {@link NavigateToFilePresenter}.
 *
 * @author Ann Shumilova
 * @author Artem Zatsarynnyi
 */
@RunWith(MockitoJUnitRunner.class)
public class NavigateToFilePresenterTest {

    public static final String PROJECT_NAME      = "test";
    public static final String FILE_IN_ROOT_NAME = "pom.xml";

    @Mock
    private NavigateToFileView       view;
    @Mock
    private AppContext               appContext;
    @Mock
    private EventBus                 eventBus;
    @Mock
    private MessageBusProvider       messageBusProvider;
    @Mock
    private CurrentProject           project;
    @Mock
    private ProjectExplorerPresenter explorerPresenter;
    @Mock
    private DtoFactory               dtoFactory;

    @Mock
    private MessageBus             messageBus;
    @Mock
    private DtoUnmarshallerFactory dtoUnmarshallerFactory;
    @Mock
    private NotificationManager    notificationManager;
    @Mock
    private WsAgentStateEvent      wsAgentStateEvent;
    @Mock
    private WorkspaceDto           workspace;
    @Mock
    private Promise<Node>          nodePromise;

    private NavigateToFilePresenter presenter;

    @Before
    public void setUp() {
        when(appContext.getCurrentProject()).thenReturn(project);
        when(appContext.getWorkspace()).thenReturn(workspace);
        when(messageBusProvider.getMachineMessageBus()).thenReturn(messageBus);

        presenter = new NavigateToFilePresenter(view,
                                                appContext,
                                                eventBus,
                                                dtoUnmarshallerFactory,
                                                explorerPresenter,
                                                messageBusProvider,
                                                dtoFactory);

        presenter.onWsAgentStarted(wsAgentStateEvent);
    }

    @Test
    public void testShowDialog() throws Exception {
        presenter.showDialog();

        verify(view).showDialog();
        verify(view).clearInput();
    }

    @Ignore
    @Test
    public void testOnFileSelected() throws Exception {
        String displayName = FILE_IN_ROOT_NAME + " (" + PROJECT_NAME + ")";
        when(view.getItemPath()).thenReturn(displayName);

        presenter.showDialog();
        presenter.onFileSelected();

        verify(view).close();
        verify(view).getItemPath();
    }
}
