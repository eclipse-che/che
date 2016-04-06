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
package org.eclipse.che.ide.ext.debugger.client.actions;

import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.ide.ext.debugger.client.DebuggerLocalizationConstant;
import org.eclipse.che.ide.ext.debugger.client.DebuggerResources;
import org.eclipse.che.ide.ext.debugger.client.actions.RemoteDebugAction;
import org.eclipse.che.ide.ext.debugger.client.debug.remotedebug.RemoteDebugPresenter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class RemoteDebugActionTest {
    @Mock
    private RemoteDebugPresenter         presenter;
    @Mock
    private DebuggerResources            resources;
    @Mock
    private DebuggerLocalizationConstant locale;

    @InjectMocks
    private RemoteDebugAction action;

    @Test
    public void constructorShouldBeVerified() {
        verify(locale).connectToRemote();
        verify(locale).connectToRemoteDescription();
    }

    @Test
    public void actionShouldBePerformed() {
        action.actionPerformed(null);
        verify(presenter).showDialog();
    }
}
