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
package org.eclipse.che.ide.extension.maven.client.command;

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
public class MavenPagePresenterTest {

    private static final String WORK_DIR     = "project";
    private static final String COMMAND_LINE = "mvn clean install";

    @Mock
    private MavenCommandPageView      mavenCommandPageView;
    @Mock
    private MavenCommandConfiguration configuration;

    @InjectMocks
    private MavenCommandPagePresenter mavenCommandPagePresenter;

    @Before
    public void setUp() {
        when(configuration.getWorkingDirectory()).thenReturn(WORK_DIR);
        when(configuration.getCommandLine()).thenReturn(COMMAND_LINE);

        mavenCommandPagePresenter.resetFrom(configuration);
    }

    @Test
    public void testResetting() throws Exception {
        verify(configuration).getCommandLine();
    }

    @Test
    public void testGo() throws Exception {
        AcceptsOneWidget container = Mockito.mock(AcceptsOneWidget.class);

        mavenCommandPagePresenter.go(container);

        verify(container).setWidget(eq(mavenCommandPageView));
        verify(configuration, times(2)).getWorkingDirectory();
        verify(configuration, times(2)).getCommandLine();
        verify(mavenCommandPageView).setWorkingDirectory(eq(WORK_DIR));
        verify(mavenCommandPageView).setCommandLine(eq(COMMAND_LINE));
    }

    @Test
    public void testOnWorkingDirectoryChanged() throws Exception {
        String workDir = "project";
        when(mavenCommandPageView.getWorkingDirectory()).thenReturn(workDir);

        final CommandConfigurationPage.DirtyStateListener listener = mock(CommandConfigurationPage.DirtyStateListener.class);
        mavenCommandPagePresenter.setDirtyStateListener(listener);

        mavenCommandPagePresenter.onWorkingDirectoryChanged();

        verify(mavenCommandPageView).getWorkingDirectory();
        verify(configuration).setWorkingDirectory(eq(workDir));
        verify(listener).onDirtyStateChanged();
    }

    @Test
    public void testOnCommandLineChanged() throws Exception {
        String commandLine = "commandLine";
        when(mavenCommandPageView.getCommandLine()).thenReturn(commandLine);

        final CommandConfigurationPage.DirtyStateListener listener = mock(CommandConfigurationPage.DirtyStateListener.class);
        mavenCommandPagePresenter.setDirtyStateListener(listener);

        mavenCommandPagePresenter.onCommandLineChanged();

        verify(mavenCommandPageView).getCommandLine();
        verify(configuration).setCommandLine(eq(commandLine));
        verify(listener).onDirtyStateChanged();
    }
}
