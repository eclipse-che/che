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
package org.eclipse.che.ide.ext.java.jdi.client.debug.remotedebug;

import org.eclipse.che.ide.ext.java.jdi.client.debug.DebuggerPresenter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class RemoteDebugPresenterTest {

    @Mock
    private RemoteDebugView   view;
    @Mock
    private DebuggerPresenter debuggerPresenter;

    @InjectMocks
    private RemoteDebugPresenter presenter;

    @Test
    public void dialogShouldBeShown() throws Exception {
        presenter.showDialog();

        verify(view).show();
    }

    @Test
    public void connectToRemoteDebuggerShouldBeDone() throws Exception {
        presenter.onConfirmClicked("host", 8000);

        verify(debuggerPresenter).attachDebugger("host", 8000);
    }
}