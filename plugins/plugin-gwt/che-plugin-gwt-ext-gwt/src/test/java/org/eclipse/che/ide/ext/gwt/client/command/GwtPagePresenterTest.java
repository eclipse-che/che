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
package org.eclipse.che.ide.ext.gwt.client.command;

import com.google.gwt.user.client.ui.AcceptsOneWidget;

import org.eclipse.che.ide.extension.machine.client.command.CommandConfigurationPage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** @author Artem Zatsarynnyi */
@RunWith(MockitoJUnitRunner.class)
public class GwtPagePresenterTest {

    private static final String WORK_DIR            = "project";
    private static final String GWT_MODULE          = "org.eclipse.CHE";
    private static final String CODE_SERVER_ADDRESS = "127.0.0.1";

    @Mock
    private GwtCommandPageView      gwtCommandPageView;
    @Mock
    private GwtCommandConfiguration configuration;

    @InjectMocks
    private GwtCommandPagePresenter gwtCommandPagePresenter;

    @Before
    public void setUp() {
        when(configuration.getWorkingDirectory()).thenReturn(WORK_DIR);
        when(configuration.getGwtModule()).thenReturn(GWT_MODULE);
        when(configuration.getCodeServerAddress()).thenReturn(CODE_SERVER_ADDRESS);

        gwtCommandPagePresenter.resetFrom(configuration);
    }

    @Test
    public void testResetting() throws Exception {
        verify(configuration).getWorkingDirectory();
        verify(configuration).getGwtModule();
        verify(configuration).getCodeServerAddress();
    }

    @Test
    public void testGo() throws Exception {
        AcceptsOneWidget container = Mockito.mock(AcceptsOneWidget.class);

        gwtCommandPagePresenter.go(container);

        verify(container).setWidget(eq(gwtCommandPageView));
        verify(configuration, times(2)).getWorkingDirectory();
        verify(configuration, times(2)).getGwtModule();
        verify(configuration, times(2)).getCodeServerAddress();
        verify(gwtCommandPageView).setWorkingDirectory(eq(WORK_DIR));
        verify(gwtCommandPageView).setGwtModule(eq(GWT_MODULE));
        verify(gwtCommandPageView).setCodeServerAddress(eq(CODE_SERVER_ADDRESS));
    }

    @Test
    public void testOnWorkingDirectoryChanged() throws Exception {
        String workDir = "project";
        when(gwtCommandPageView.getWorkingDirectory()).thenReturn(workDir);

        final CommandConfigurationPage.DirtyStateListener listener = mock(CommandConfigurationPage.DirtyStateListener.class);
        gwtCommandPagePresenter.setDirtyStateListener(listener);

        gwtCommandPagePresenter.onWorkingDirectoryChanged();

        verify(gwtCommandPageView).getWorkingDirectory();
        verify(configuration).setWorkingDirectory(eq(workDir));
        verify(listener).onDirtyStateChanged();
    }

    @Test
    public void testOnGwtModuleChanged() throws Exception {
        String gwtModule = "module";
        when(gwtCommandPageView.getGwtModule()).thenReturn(gwtModule);

        final CommandConfigurationPage.DirtyStateListener listener = mock(CommandConfigurationPage.DirtyStateListener.class);
        gwtCommandPagePresenter.setDirtyStateListener(listener);

        gwtCommandPagePresenter.onGwtModuleChanged();

        verify(gwtCommandPageView).getGwtModule();
        verify(configuration).setGwtModule(eq(gwtModule));
        verify(listener).onDirtyStateChanged();
    }

    @Test
    public void testOnCodeServerAddressChanged() throws Exception {
        String codeServer = "localhost";
        when(gwtCommandPageView.getCodeServerAddress()).thenReturn(codeServer);

        final CommandConfigurationPage.DirtyStateListener listener = mock(CommandConfigurationPage.DirtyStateListener.class);
        gwtCommandPagePresenter.setDirtyStateListener(listener);

        gwtCommandPagePresenter.onCodeServerAddressChanged();

        verify(gwtCommandPageView).getCodeServerAddress();
        verify(configuration).setCodeServerAddress(eq(codeServer));
        verify(listener).onDirtyStateChanged();
    }
}
