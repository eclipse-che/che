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
package org.eclipse.che.ide.ext.java.client.command;

import com.google.gwt.user.client.ui.AcceptsOneWidget;

import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.ext.java.client.command.mainclass.SelectNodePresenter;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfigurationPage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Valeriy Svydenko
 */
@RunWith(MockitoJUnitRunner.class)
public class JavaCommandPagePresenterTest {
    private static final String MAIN_CLASS_PATH          = "/project/src/com/company/Main.java";
    private static final String RELATIVE_MAIN_CLASS_PATH = "src/com/company/Main.java";
    private static final String PROJECT_PATH             = "/project";
    private static final String MAIN_CLASS_FQN           = "com.company.Main";
    private static final String COMMAND_LINE             = "command-line";

    @Mock
    private JavaCommandPageView view;
    @Mock
    private SelectNodePresenter selectNodePresenter;
    @Mock
    private AppContext          appContext;

    @Mock
    private JavaCommandConfiguration                          configuration;
    @Mock
    private CommandConfigurationPage.FieldStateActionDelegate fieldStateDelegate;
    @Mock
    private CurrentProject                                    currentProject;
    @Mock
    private ProjectConfigDto                                  projectConfigDto;

    @InjectMocks
    private JavaCommandPagePresenter presenter;

    @Before
    public void setUp() throws Exception {
        when(configuration.getCommandLine()).thenReturn(COMMAND_LINE);
        when(configuration.getMainClass()).thenReturn(MAIN_CLASS_PATH);
        when(configuration.getMainClassFqn()).thenReturn(MAIN_CLASS_FQN);
        when(appContext.getCurrentProject()).thenReturn(currentProject);
        when(currentProject.getProjectConfig()).thenReturn(projectConfigDto);
        when(projectConfigDto.getPath()).thenReturn(PROJECT_PATH);
    }

    @Test
    public void delegateShouldBeSet() throws Exception {
        verify(view).setDelegate(presenter);
    }

    @Test
    public void pageShouldBeInitialized() throws Exception {
        AcceptsOneWidget container = mock(AcceptsOneWidget.class);

        presenter.resetFrom(configuration);
        presenter.setFieldStateActionDelegate(fieldStateDelegate);
        presenter.go(container);

        verify(container).setWidget(view);
        verify(view).setMainClass(MAIN_CLASS_PATH);
        verify(view).setCommandLine(COMMAND_LINE);
        verify(fieldStateDelegate).updatePreviewURLState(false);
    }

    @Test
    public void selectedNodeWindowShouldBeShowed() throws Exception {
        presenter.onAddMainClassBtnClicked();

        verify(selectNodePresenter).show(presenter);
    }

    @Test
    public void configurationShouldBeReturned() throws Exception {
        presenter.resetFrom(configuration);
        assertEquals(configuration, presenter.getConfiguration());
    }

    @Test
    public void pageIsNotDirty() throws Exception {
        presenter.resetFrom(configuration);
        assertFalse(presenter.isDirty());
    }

    @Test
    public void pageIsDirty() throws Exception {
        presenter.resetFrom(configuration);

        when(configuration.getMainClass()).thenReturn(COMMAND_LINE);

        assertTrue(presenter.isDirty());
    }

    @Test
    public void mainClassShouldBeUpdated() throws Exception {
        CommandConfigurationPage.DirtyStateListener listener = mock(CommandConfigurationPage.DirtyStateListener.class);

        when(configuration.getMainClass()).thenReturn(COMMAND_LINE);

        presenter.setDirtyStateListener(listener);
        presenter.resetFrom(configuration);
        presenter.setMainClass(MAIN_CLASS_PATH, MAIN_CLASS_FQN);

        verify(view).setMainClass(RELATIVE_MAIN_CLASS_PATH);
        verify(configuration).setMainClass(RELATIVE_MAIN_CLASS_PATH);

        verify(listener).onDirtyStateChanged();
    }
}
