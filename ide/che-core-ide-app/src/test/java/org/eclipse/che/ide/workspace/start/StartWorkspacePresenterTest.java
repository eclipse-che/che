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
package org.eclipse.che.ide.workspace.start;

import com.google.gwt.core.client.Callback;
import com.google.inject.Provider;

import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.ide.workspace.DefaultWorkspaceComponent;
import org.eclipse.che.ide.workspace.WorkspaceComponent;
import org.eclipse.che.ide.api.component.Component;
import org.eclipse.che.ide.context.BrowserQueryFieldRenderer;
import org.eclipse.che.ide.workspace.WorkspaceWidgetFactory;
import org.eclipse.che.ide.workspace.create.CreateWorkspacePresenter;
import org.eclipse.che.ide.workspace.start.workspacewidget.WorkspaceWidget;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class StartWorkspacePresenterTest {

    //constructor mocks
    @Mock
    private StartWorkspaceView           view;
    @Mock
    private Provider<WorkspaceComponent> wsComponentProvider;
    @Mock
    private WorkspaceWidgetFactory       widgetFactory;
    @Mock
    private CreateWorkspacePresenter     createWorkspacePresenter;
    @Mock
    private BrowserQueryFieldRenderer    browserQueryFieldRenderer;

    //additional mocks
    @Mock
    private WorkspaceDto                   workspaceDto;
    @Mock
    private WorkspaceConfigDto             workspaceConfigDto;
    @Mock
    private WorkspaceWidget                widget;
    @Mock
    private DefaultWorkspaceComponent      workspaceComponent;
    @Mock
    private Callback<Component, Exception> callback;

    @InjectMocks
    private StartWorkspacePresenter presenter;

    @Before
    public void setUp() throws Exception {
        when(workspaceDto.getConfig()).thenReturn(workspaceConfigDto);
    }

    @Test
    public void delegateShouldBeSet() {
        verify(view).setDelegate(presenter);
    }

    @Test
    public void dialogStartWorkspaceShouldBeShown() {
        when(browserQueryFieldRenderer.getWorkspaceName()).thenReturn("test");
        when(widgetFactory.create(workspaceDto)).thenReturn(widget);

        presenter.show(Arrays.asList(workspaceDto), callback);

        verify(browserQueryFieldRenderer).getWorkspaceName();
        verify(widgetFactory).create(workspaceDto);
        verify(widget).setDelegate(presenter);
        verify(view).addWorkspace(widget);
        verify(view).setWsName(anyString());

        verify(view).show();
    }

    @Test
    public void workspaceWithExistingNameShouldBeSelected() {
        when(browserQueryFieldRenderer.getWorkspaceName()).thenReturn("test");
        when(wsComponentProvider.get()).thenReturn(workspaceComponent);
        when(widgetFactory.create(workspaceDto)).thenReturn(widget);

        when(workspaceConfigDto.getName()).thenReturn("test");

        presenter.show(Arrays.asList(workspaceDto), callback);

        presenter.onStartWorkspaceClicked();

        verify(wsComponentProvider).get();
        verify(workspaceComponent).startWorkspace(workspaceDto, callback);
        verify(view).hide();
    }

    @Test
    public void onCreateWorkspaceButtonShouldBeClicked() {
        when(browserQueryFieldRenderer.getWorkspaceName()).thenReturn("test");
        when(widgetFactory.create(workspaceDto)).thenReturn(widget);
        presenter.show(Arrays.asList(workspaceDto), callback);

        presenter.onCreateWorkspaceClicked();

        verify(view).hide();
        verify(createWorkspacePresenter).show(Matchers.<List<WorkspaceDto>>anyObject(), eq(callback));
    }

    @Test
    public void workspaceWidgetShouldBeSelected() {

        when(workspaceConfigDto.getDefaultEnv()).thenReturn("text");

        presenter.onWorkspaceSelected(workspaceDto);

        verify(workspaceConfigDto).getDefaultEnv();
        verify(view).setWsName("text");
        verify(view).setEnableStartButton(true);

        verify(view, never()).hide();
    }

    @Test
    public void workspaceShouldBeStartedWhenRunningWsWasSelected() {
        when(workspaceDto.getStatus()).thenReturn(WorkspaceStatus.RUNNING);

        when(workspaceConfigDto.getDefaultEnv()).thenReturn("test");
        when(wsComponentProvider.get()).thenReturn(workspaceComponent);

        presenter.onWorkspaceSelected(workspaceDto);

        verify(wsComponentProvider).get();
        verify(workspaceComponent).setCurrentWorkspace(eq(workspaceDto));
        verify(view).hide();
    }

    @Test
    public void selectedWorkspaceShouldBeStarted() {
        when(widgetFactory.create(workspaceDto)).thenReturn(widget);
        when(workspaceConfigDto.getDefaultEnv()).thenReturn("text");
        when(browserQueryFieldRenderer.getWorkspaceName()).thenReturn("test");
        when(wsComponentProvider.get()).thenReturn(workspaceComponent);

        presenter.show(Arrays.asList(workspaceDto), callback);
        presenter.onWorkspaceSelected(workspaceDto);
        reset(workspaceDto);

        presenter.onStartWorkspaceClicked();

        verify(wsComponentProvider).get();

        verify(workspaceComponent).startWorkspace(workspaceDto, callback);

        verify(view).hide();
    }

}
