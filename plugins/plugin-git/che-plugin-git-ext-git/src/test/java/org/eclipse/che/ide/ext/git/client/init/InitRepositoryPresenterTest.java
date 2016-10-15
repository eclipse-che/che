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
package org.eclipse.che.ide.ext.git.client.init;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.ext.git.client.BaseTest;
import org.eclipse.che.ide.resource.Path;
import org.junit.Test;

import static org.eclipse.che.ide.ext.git.client.init.InitRepositoryPresenter.INIT_COMMAND_NAME;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testing {@link InitRepositoryPresenter} functionality.
 *
 * @author Andrey Plotnikov
 * @author Roman Nikitenko
 * @author Vlad Zhukovskyi
 */
public class InitRepositoryPresenterTest extends BaseTest {

    private InitRepositoryPresenter presenter;

    @Override
    public void disarm() {
        super.disarm();

        presenter = new InitRepositoryPresenter(constant,
                                                notificationManager,
                                                gitOutputConsoleFactory,
                                                processesPanelPresenter,
                                                service,
                                                appContext);

        when(service.init(anyObject(), any(Path.class), anyBoolean())).thenReturn(voidPromise);
        when(voidPromise.then(any(Operation.class))).thenReturn(voidPromise);
        when(voidPromise.catchError(any(Operation.class))).thenReturn(voidPromise);
    }

    @Test
    public void testOnOkClickedInitWSRequestAndGetProjectIsSuccessful() throws Exception {
        presenter.initRepository(project);

        verify(voidPromise).then(voidPromiseCaptor.capture());
        voidPromiseCaptor.getValue().apply(null);

        verify(gitOutputConsoleFactory).create(eq(INIT_COMMAND_NAME));
        verify(console).print(eq(constant.initSuccess()));
        verify(processesPanelPresenter).addCommandOutput(anyString(), eq(console));
        verify(notificationManager).notify(anyString());

        verify(project).synchronize();
    }

    @Test
    public void testOnOkClickedInitWSRequestIsFailed() throws Exception {
        presenter.initRepository(project);

        verify(voidPromise).catchError(promiseErrorCaptor.capture());
        promiseErrorCaptor.getValue().apply(promiseError);

        verify(constant).initFailed();
        verify(gitOutputConsoleFactory).create(INIT_COMMAND_NAME);
        verify(console).printError(anyObject());
        verify(processesPanelPresenter).addCommandOutput(anyString(), eq(console));
        verify(notificationManager).notify(anyString(), any(StatusNotification.Status.class), anyObject());
    }
}
