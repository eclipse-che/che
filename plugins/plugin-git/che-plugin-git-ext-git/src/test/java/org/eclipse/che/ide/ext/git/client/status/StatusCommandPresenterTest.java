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
package org.eclipse.che.ide.ext.git.client.status;

import org.eclipse.che.api.git.shared.StatusFormat;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.ext.git.client.BaseTest;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.resource.Path;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testing {@link StatusCommandPresenter} functionality.
 *
 * @author Andrey Plotnikov
 * @author Vlad Zhukovskyi
 */
public class StatusCommandPresenterTest extends BaseTest {
    @InjectMocks
    private StatusCommandPresenter presenter;

    @Mock
    private WorkspaceAgent workspaceAgent;

    @Mock
    private GitOutputConsoleFactory gitOutputConsoleFactory;

    @Mock
    private ProcessesPanelPresenter processesPanelPresenter;

    @Override
    public void disarm() {
        super.disarm();

        presenter = new StatusCommandPresenter(service,
                                               appContext,
                                               gitOutputConsoleFactory,
                                               processesPanelPresenter,
                                               constant,
                                               notificationManager);

        when(service.statusText(anyObject(), any(Path.class), any(StatusFormat.class))).thenReturn(stringPromise);
        when(stringPromise.then(any(Operation.class))).thenReturn(stringPromise);
        when(stringPromise.catchError(any(Operation.class))).thenReturn(stringPromise);
    }

    @Test
    public void testShowStatusWhenStatusTextRequestIsSuccessful() throws Exception {
        when(gitOutputConsoleFactory.create(anyString())).thenReturn(console);

        presenter.showStatus(project);

        verify(stringPromise).then(stringCaptor.capture());
        stringCaptor.getValue().apply("");

        verify(console, times(2)).print(anyString());
        verify(processesPanelPresenter).addCommandOutput(anyString(), anyObject());
    }

    @Test
    public void testShowStatusWhenStatusTextRequestIsFailed() throws Exception {
        presenter.showStatus(project);

        verify(stringPromise).catchError(promiseErrorCaptor.capture());
        promiseErrorCaptor.getValue().apply(promiseError);

        verify(notificationManager).notify(anyString(), any(StatusNotification.Status.class), anyObject());
        verify(constant).statusFailed();
    }
}
