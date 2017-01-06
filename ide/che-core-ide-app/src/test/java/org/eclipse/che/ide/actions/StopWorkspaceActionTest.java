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
package org.eclipse.che.ide.actions;

import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.DevMachine;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class StopWorkspaceActionTest {

    @Mock
    private CoreLocalizationConstant locale;
    @Mock
    private AppContext               appContext;
    @Mock
    private WorkspaceServiceClient   workspaceService;
    @Mock
    private NotificationManager      notificationManager;
    @Mock
    private Workspace                workspace;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ActionEvent   actionEvent;
    @Mock
    private Promise<Void> voidPromise;

    @Captor
    private ArgumentCaptor<Operation<Void>> operationCaptor;

    @InjectMocks
    private StopWorkspaceAction action;

    @Test
    public void titleAndDescriptionShouldBeSet() {
        verify(locale).stopWsTitle();
        verify(locale).stopWsDescription();
    }

    @Test
    public void actionShouldBeUpdated() {
        action.updateInPerspective(actionEvent);

        verify(actionEvent, times(2)).getPresentation();
    }

    @Test
    public void actionShouldBePerformed() throws Exception {
        when(workspaceService.stop(anyString())).thenReturn(voidPromise);
        DevMachine devMachine = mock(DevMachine.class);
        when(devMachine.getId()).thenReturn("id");
        when(appContext.getWorkspace()).thenReturn(workspace);
        when(workspace.getId()).thenReturn("id");

        action.actionPerformed(actionEvent);

        verify(workspaceService).stop("id");
    }
}
