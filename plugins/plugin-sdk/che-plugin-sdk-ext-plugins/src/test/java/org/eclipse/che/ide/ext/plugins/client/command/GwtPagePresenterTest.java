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
package org.eclipse.che.ide.ext.plugins.client.command;

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

    private static final String GWT_MODULE          = "org.eclipse.CHE";
    private static final String CODE_SERVER_ADDRESS = "127.0.0.1";
    private static final String CHE_CLASS_PATH = "class_path";

    @Mock
    private CommandPageView            gwtCommandPageView;
    @Mock
    private GwtCheCommandConfiguration configuration;

    @InjectMocks
    private CommandPagePresenter gwtCommandPagePresenter;

    @Before
    public void setUp() {
        when(configuration.getGwtModule()).thenReturn(GWT_MODULE);
        when(configuration.getCodeServerAddress()).thenReturn(CODE_SERVER_ADDRESS);
        when(configuration.getClassPath()).thenReturn(CHE_CLASS_PATH);

        gwtCommandPagePresenter.resetFrom(configuration);
    }

    @Test
    public void testResetting() throws Exception {
        verify(configuration).getGwtModule();
        verify(configuration).getCodeServerAddress();
        verify(configuration).getClassPath();
    }

    @Test
    public void testGo() throws Exception {
        AcceptsOneWidget container = Mockito.mock(AcceptsOneWidget.class);

        gwtCommandPagePresenter.go(container);

        verify(container).setWidget(eq(gwtCommandPageView));
        verify(configuration, times(2)).getGwtModule();
        verify(configuration, times(2)).getCodeServerAddress();
        verify(configuration, times(2)).getClassPath();
        verify(gwtCommandPageView).setGwtModule(eq(GWT_MODULE));
        verify(gwtCommandPageView).setCodeServerAddress(eq(CODE_SERVER_ADDRESS));
        verify(gwtCommandPageView).setClassPath(eq(CHE_CLASS_PATH));
    }

    @Test
    public void testOnGwtModuleChanged() throws Exception {
        when(gwtCommandPageView.getGwtModule()).thenReturn(GWT_MODULE);

        final CommandConfigurationPage.DirtyStateListener listener = mock(CommandConfigurationPage.DirtyStateListener.class);
        gwtCommandPagePresenter.setDirtyStateListener(listener);

        gwtCommandPagePresenter.onGwtModuleChanged();

        verify(gwtCommandPageView).getGwtModule();
        verify(configuration).setGwtModule(eq(GWT_MODULE));
        verify(listener).onDirtyStateChanged();
    }

    @Test
    public void testOnCodeServerAddressChanged() throws Exception {
        when(gwtCommandPageView.getCodeServerAddress()).thenReturn(CODE_SERVER_ADDRESS);

        final CommandConfigurationPage.DirtyStateListener listener = mock(CommandConfigurationPage.DirtyStateListener.class);
        gwtCommandPagePresenter.setDirtyStateListener(listener);

        gwtCommandPagePresenter.onCodeServerAddressChanged();

        verify(gwtCommandPageView).getCodeServerAddress();
        verify(configuration).setCodeServerAddress(eq(CODE_SERVER_ADDRESS));
        verify(listener).onDirtyStateChanged();
    }

    @Test
    public void testOnClassPathChanged() throws Exception {
        when(gwtCommandPageView.getClassPath()).thenReturn(CHE_CLASS_PATH);

        final CommandConfigurationPage.DirtyStateListener listener = mock(CommandConfigurationPage.DirtyStateListener.class);
        gwtCommandPagePresenter.setDirtyStateListener(listener);

        gwtCommandPagePresenter.onClassPathChanged();

        verify(gwtCommandPageView).getClassPath();
        verify(configuration).setClassPath(eq(CHE_CLASS_PATH));
        verify(listener).onDirtyStateChanged();
    }
}
