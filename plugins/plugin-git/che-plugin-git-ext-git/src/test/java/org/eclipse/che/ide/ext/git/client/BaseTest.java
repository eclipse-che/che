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
package org.eclipse.che.ide.ext.git.client;

import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.git.GitServiceClient;
import org.eclipse.che.ide.api.machine.DevMachine;
import org.eclipse.che.ide.api.project.ProjectServiceClient;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.selection.SelectionAgent;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsole;
import org.eclipse.che.ide.ext.git.client.outputconsole.GitOutputConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.processes.ConsolesPanelPresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Base test for git extension.
 *
 * @author Andrey Plotnikov
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class BaseTest {
    public static final String  PROJECT_PATH    = "/test";
    public static final boolean SELECTED_ITEM   = true;
    public static final boolean UNSELECTED_ITEM = false;
    public static final boolean ENABLE_BUTTON   = true;
    public static final boolean DISABLE_BUTTON  = false;
    public static final boolean ENABLE_FIELD    = true;
    public static final boolean DISABLE_FIELD   = false;
    public static final boolean ACTIVE_BRANCH   = true;
    public static final String  EMPTY_TEXT      = "";
    public static final String  PROJECT_NAME    = "test";
    public static final String  REMOTE_NAME     = "codenvy";
    public static final String  LOCALE_URI      = "http://codenvy.com/git/workspace/test";
    public static final String  REMOTE_URI      = "git@github.com:codenvy/test.git";
    public static final String  REPOSITORY_NAME = "origin";
    public static final String  LOCAL_BRANCH    = "localBranch";
    public static final String  REMOTE_BRANCH   = "remoteBranch";
    @Mock
    protected CurrentProject           currentProject;
    @Mock
    protected ProjectConfigDto         projectConfig;
    @Mock
    protected ProjectConfigDto         rootProjectConfig;
    @Mock
    protected AppContext               appContext;
    @Mock
    protected DevMachine               devMachine;
    @Mock
    protected GitServiceClient         service;
    @Mock
    protected GitLocalizationConstant  constant;
    @Mock
    protected GitOutputConsole         console;
    @Mock
    protected GitOutputConsoleFactory  gitOutputConsoleFactory;
    @Mock
    protected ConsolesPanelPresenter   consolesPanelPresenter;
    @Mock
    protected GitResources             resources;
    @Mock
    protected EventBus                 eventBus;
    @Mock
    protected SelectionAgent           selectionAgent;
    @Mock
    protected NotificationManager      notificationManager;
    @Mock
    protected DtoFactory               dtoFactory;
    @Mock
    protected DtoUnmarshallerFactory   dtoUnmarshallerFactory;
    @Mock
    protected DialogFactory            dialogFactory;
    @Mock
    protected ProjectServiceClient     projectServiceClient;
    @Mock
    protected ProjectExplorerPresenter projectExplorer;

    @Before
    public void disarm() {
        when(appContext.getWorkspaceId()).thenReturn("id");
        when(appContext.getCurrentProject()).thenReturn(currentProject);

        when(currentProject.getProjectConfig()).thenReturn(projectConfig);
        when(currentProject.getRootProject()).thenReturn(rootProjectConfig);

        when(projectConfig.getName()).thenReturn(PROJECT_NAME);
        when(projectConfig.getPath()).thenReturn(PROJECT_PATH);

        when(rootProjectConfig.getName()).thenReturn(PROJECT_NAME);
        when(rootProjectConfig.getPath()).thenReturn(PROJECT_PATH);

        when(gitOutputConsoleFactory.create(anyString())).thenReturn(console);
    }
}
