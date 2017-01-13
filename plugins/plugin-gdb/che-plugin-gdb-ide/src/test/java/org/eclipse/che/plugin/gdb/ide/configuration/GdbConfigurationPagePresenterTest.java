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
package org.eclipse.che.plugin.gdb.ide.configuration;

import com.google.gwt.user.client.ui.AcceptsOneWidget;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.debug.DebugConfiguration;
import org.eclipse.che.ide.api.debug.DebugConfigurationPage;
import org.eclipse.che.ide.api.machine.RecipeServiceClient;
import org.eclipse.che.ide.extension.machine.client.command.macros.CurrentProjectPathMacro;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** @author Artem Zatsarynnyi */
@RunWith(MockitoJUnitRunner.class)
public class GdbConfigurationPagePresenterTest {

    private static final String HOST = "localhost";
    private static final int    PORT = 8000;

    @Mock
    private GdbConfigurationPageView pageView;
    @Mock
    private DebugConfiguration       configuration;
    @Mock
    private CurrentProjectPathMacro  currentProjectPathMacro;
    @Mock
    private AppContext               appContext;
    @Mock
    private RecipeServiceClient      recipeServiceClient;

    @InjectMocks
    private GdbConfigurationPagePresenter pagePresenter;

    @Before
    public void setUp() {
        when(configuration.getHost()).thenReturn(HOST);
        when(configuration.getPort()).thenReturn(PORT);

        pagePresenter.resetFrom(configuration);
    }

    @Test
    public void testResetting() throws Exception {
        verify(configuration, atLeastOnce()).getHost();
        verify(configuration, atLeastOnce()).getPort();
        verify(configuration, atLeastOnce()).getConnectionProperties();
        verify(currentProjectPathMacro).getName();
    }

    @Test
    public void testGo() throws Exception {
        AcceptsOneWidget container = Mockito.mock(AcceptsOneWidget.class);

        pagePresenter.go(container);

        verify(appContext).getWorkspace();
        verify(container).setWidget(eq(pageView));
        verify(configuration, atLeastOnce()).getHost();
        verify(configuration, atLeastOnce()).getPort();
        verify(configuration, atLeastOnce()).getConnectionProperties();
        verify(pageView).setHost(eq(HOST));
        verify(pageView).setPort(eq(PORT));
        verify(pageView).setBinaryPath(anyString());
        verify(pageView).setDevHost(eq(false));
        verify(pageView).setPortEnableState(eq(true));
        verify(pageView).setHostEnableState(eq(true));
    }

    @Test
    public void testOnHostChanged() throws Exception {
        String host = "localhost";
        when(pageView.getHost()).thenReturn(host);

        final DebugConfigurationPage.DirtyStateListener listener = mock(DebugConfigurationPage.DirtyStateListener.class);
        pagePresenter.setDirtyStateListener(listener);

        pagePresenter.onHostChanged();

        verify(pageView).getHost();
        verify(configuration).setHost(eq(host));
        verify(listener).onDirtyStateChanged();
    }

    @Test
    public void testOnPortChanged() throws Exception {
        int port = 8000;
        when(pageView.getPort()).thenReturn(port);

        final DebugConfigurationPage.DirtyStateListener listener = mock(DebugConfigurationPage.DirtyStateListener.class);
        pagePresenter.setDirtyStateListener(listener);

        pagePresenter.onPortChanged();

        verify(pageView).getPort();
        verify(configuration).setPort(eq(port));
        verify(listener).onDirtyStateChanged();
    }

    @Test
    public void testOnBinaryPathChanged() throws Exception {
        String binPath = "/path";
        when(pageView.getBinaryPath()).thenReturn(binPath);

        final DebugConfigurationPage.DirtyStateListener listener = mock(DebugConfigurationPage.DirtyStateListener.class);
        pagePresenter.setDirtyStateListener(listener);

        pagePresenter.onBinaryPathChanged();

        verify(pageView).getBinaryPath();
        ArgumentCaptor<Map> argumentCaptor = ArgumentCaptor.forClass(Map.class);

        verify(configuration).setConnectionProperties(argumentCaptor.capture());
        Map argumentCaptorValue = argumentCaptor.getValue();
        assertEquals(binPath, argumentCaptorValue.get(GdbConfigurationPagePresenter.BIN_PATH_CONNECTION_PROPERTY));

        verify(listener).onDirtyStateChanged();
    }
}
