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
package org.eclipse.che.plugin.nodejsdbg.ide.configuration;

import com.google.gwt.user.client.ui.AcceptsOneWidget;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.debug.DebugConfiguration;
import org.eclipse.che.ide.api.debug.DebugConfigurationPage;
import org.eclipse.che.ide.api.machine.MachineServiceClient;
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

import static org.eclipse.che.plugin.nodejsdbg.ide.NodeJsDebugger.ConnectionProperties.SCRIPT;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** @author Artem Zatsarynnyi */
@RunWith(MockitoJUnitRunner.class)
public class NodeJsDebuggerConfigurationPagePresenterTest {

    private static final String HOST = "localhost";
    private static final int    PORT = 8000;

    @Mock
    private NodeJsDebuggerConfigurationPageView pageView;
    @Mock
    private DebugConfiguration                  configuration;
    @Mock
    private CurrentProjectPathMacro             currentProjectPathMacro;
    @Mock
    private AppContext                          appContext;
    @Mock
    private RecipeServiceClient                 recipeServiceClient;
    @Mock
    private MachineServiceClient                machineServiceClient;

    @InjectMocks
    private NodeJsDebuggerConfigurationPagePresenter pagePresenter;

    @Before
    public void setUp() {
        when(configuration.getHost()).thenReturn(HOST);
        when(configuration.getPort()).thenReturn(PORT);

        pagePresenter.resetFrom(configuration);
    }

    @Test
    public void testResetting() throws Exception {
        verify(configuration, atLeastOnce()).getConnectionProperties();
        verify(currentProjectPathMacro).getName();
    }

    @Test
    public void testGo() throws Exception {
        AcceptsOneWidget container = Mockito.mock(AcceptsOneWidget.class);

        pagePresenter.go(container);

        verify(container).setWidget(eq(pageView));
        verify(configuration, atLeastOnce()).getConnectionProperties();
        verify(pageView).setScriptPath(anyString());
    }

    @Test
    public void testOnBinaryPathChanged() throws Exception {
        String binPath = "/path";
        when(pageView.getScriptPath()).thenReturn(binPath);

        final DebugConfigurationPage.DirtyStateListener listener = mock(DebugConfigurationPage.DirtyStateListener.class);
        pagePresenter.setDirtyStateListener(listener);

        pagePresenter.onScriptPathChanged();

        verify(pageView).getScriptPath();
        ArgumentCaptor<Map> argumentCaptor = ArgumentCaptor.forClass(Map.class);

        verify(configuration).setConnectionProperties(argumentCaptor.capture());
        Map argumentCaptorValue = argumentCaptor.getValue();
        assertEquals(binPath, argumentCaptorValue.get(SCRIPT.toString()));

        verify(listener).onDirtyStateChanged();
    }
}
