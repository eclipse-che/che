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
package org.eclipse.che.ide.ext.java.jdi.client.actions;

import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.ext.java.jdi.client.JavaRuntimeLocalizationConstant;
import org.eclipse.che.ide.ext.java.jdi.client.JavaRuntimeResources;
import org.eclipse.che.ide.ext.java.jdi.client.debug.remotedebug.RemoteDebugPresenter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.verify;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class RemoteDebugActionTest {
    @Mock
    private RemoteDebugPresenter            presenter;
    @Mock
    private JavaRuntimeResources            resources;
    @Mock
    private JavaRuntimeLocalizationConstant locale;

    //additional mocks
    @Mock(answer = RETURNS_DEEP_STUBS)
    private ActionEvent actionEvent;

    @InjectMocks
    private RemoteDebugAction action;

    @Test
    public void constructorShouldBeVerified() {
        verify(locale).connectToRemote();
        verify(locale).connectToRemoteDescription();
    }

    @Test
    public void actionShouldBePerformed() {
        action.actionPerformed(actionEvent);

        verify(presenter).showDialog();
    }
}
