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
package org.eclipse.che.ide.ext.debugger.client.debug.remotedebug;

import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.ext.debugger.client.DebuggerLocalizationConstant;
import org.eclipse.che.ide.ext.debugger.client.DebuggerResources;
import org.eclipse.che.ide.ext.debugger.client.debug.remotedebug.RemoteDebugView.ActionDelegate;
import org.eclipse.che.ide.ui.dialogs.CancelCallback;
import org.eclipse.che.ide.ui.dialogs.ConfirmCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.dialogs.confirm.ConfirmDialog;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class RemoteDebugViewImplTest {

    private static final String SOME_TEXT = "someText";

    @Mock
    private DebuggerLocalizationConstant locale;
    @Mock
    private DebuggerResources            resources;
    @Mock
    private ActionDelegate               delegate;
    @Mock
    private ConfirmDialog                dialog;
    @Mock
    private DialogFactory                dialogFactory;
    @Mock
    private NotificationManager          notificationManager;

    @Captor
    private ArgumentCaptor<ConfirmCallback> confirmCallbackCaptor;
    @Captor
    private ArgumentCaptor<CancelCallback>  failureCallbackCaptor;

    private RemoteDebugViewImpl view;

    @Before
    public void setUp() {
        when(locale.connectToRemote()).thenReturn(SOME_TEXT);

        when(dialogFactory.createConfirmDialog(anyString(),
                                               Matchers.<RemoteDebugViewImpl>anyObject(),
                                               confirmCallbackCaptor.capture(),
                                               failureCallbackCaptor.capture())).thenReturn(dialog);

        view = new RemoteDebugViewImpl(locale, resources, dialogFactory, notificationManager);
        view.setDelegate(delegate);
    }

    @Test
    public void confirmAcceptedShouldBeCalled() throws Exception {
        when(view.host.getValue()).thenReturn(SOME_TEXT);
        when(view.port.getValue()).thenReturn("8000");
        verify(dialogFactory).createConfirmDialog(eq(SOME_TEXT),
                                                  eq(view),
                                                  confirmCallbackCaptor.capture(),
                                                  failureCallbackCaptor.capture());

        confirmCallbackCaptor.getValue().accepted();

        verify(delegate).onConfirmClicked(SOME_TEXT, 8000);
        verify(view.host).getValue();
        verify(view.port).getValue();
        verify(locale).connectToRemote();
    }

    @Test
    public void cancelCallBackShouldBeCalled() throws Exception {
        verify(dialogFactory).createConfirmDialog(eq(SOME_TEXT),
                                                  eq(view),
                                                  confirmCallbackCaptor.capture(),
                                                  failureCallbackCaptor.capture());

        failureCallbackCaptor.getValue().cancelled();

        verify(locale).connectToRemote();
    }

    @Test
    public void dialogShouldBeShown() throws Exception {
        view.show();

        verify(dialog).show();
    }

}