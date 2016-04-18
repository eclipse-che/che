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
package org.eclipse.che.ide.part.explorer.project.synchronize;

import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.machine.gwt.client.DevMachine;
import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.api.project.shared.Constants;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectProblemDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.event.ConfigureProjectEvent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.wizard.Wizard;
import org.eclipse.che.ide.project.node.ProjectNode;
import org.eclipse.che.ide.projectimport.wizard.ProjectImporter;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.ui.dialogs.CancelCallback;
import org.eclipse.che.ide.ui.dialogs.ConfirmCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.dialogs.confirm.ConfirmDialog;
import org.eclipse.che.ide.ui.dialogs.message.MessageDialog;
import org.eclipse.che.ide.ui.smartTree.event.BeforeExpandNodeEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class ProjectConfigSynchronizationListenerTest {

    private static final String WORKSPACE_ID = "wsId";
    private static final String PROJECT_NAME = "name";

    //constructor mocks
    @Mock
    private AppContext appContext;
    @Mock
    private DevMachine devMachine;

    @Mock
    private DialogFactory            dialogFactory;
    @Mock
    private ProjectImporter          projectImporter;
    @Mock
    private CoreLocalizationConstant locale;
    @Mock
    private ProjectServiceClient     projectService;
    @Mock
    private NotificationManager      notificationManager;
    @Mock
    private EventBus                 eventBus;
    @Mock
    private ChangeLocationWidget     changeLocationWidget;
    @Mock
    private CancelCallback           cancelCallback;
    @Mock
    private DtoUnmarshallerFactory   factory;

    //additional mocks
    @Mock
    private ProjectConfigDto      projectConfig;
    @Mock
    private BeforeExpandNodeEvent event;
    @Mock
    private ConfirmDialog         confirmDialog;
    @Mock
    private SourceStorageDto      sourceStorage;
    @Mock
    private ProjectNode           projectNode;

    @Captor
    private ArgumentCaptor<ConfirmCallback>            confirmCaptor;
    @Captor
    private ArgumentCaptor<CancelCallback>             cancelCaptor;
    @Captor
    private ArgumentCaptor<AsyncRequestCallback<Void>> deleteCaptor;

    private List<ProjectProblemDto> problems;

    private ProjectConfigSynchronizationListener listener;

    @Before
    public void setUp() {
        when(appContext.getWorkspaceId()).thenReturn(WORKSPACE_ID);
        when(appContext.getDevMachine()).thenReturn(devMachine);
        when(event.getNode()).thenReturn(projectNode);
        when(projectNode.getProjectConfig()).thenReturn(projectConfig);

        when(dialogFactory.createConfirmDialog(anyString(),
                                               anyString(),
                                               anyString(),
                                               anyString(),
                                               Matchers.<ConfirmCallback>anyObject(),
                                               Matchers.<CancelCallback>anyObject())).thenReturn(confirmDialog);
        when(dialogFactory.createConfirmDialog(anyString(),
                                               eq(changeLocationWidget),
                                               Matchers.<ConfirmCallback>anyObject(),
                                               Matchers.<CancelCallback>anyObject())).thenReturn(confirmDialog);

        problems = new ArrayList<>();
        when(projectConfig.getProblems()).thenReturn(problems);
        when(projectConfig.getName()).thenReturn(PROJECT_NAME);
        when(projectConfig.getSource()).thenReturn(sourceStorage);

        listener = new ProjectConfigSynchronizationListener(projectImporter,
                                                            eventBus,
                                                            dialogFactory,
                                                            locale,
                                                            projectService,
                                                            appContext,
                                                            notificationManager,
                                                            changeLocationWidget,
                                                            factory);
    }

    @Test
    public void constructorShouldBeInitialized() {
        verify(eventBus).addHandler(BeforeExpandNodeEvent.getType(), listener);
    }

    @Test
    public void nothingShouldHappenWhenProjectDoesNotHaveAnyProblem() {
        listener.onBeforeExpand(event);

        verify(dialogFactory, never()).createConfirmDialog(anyString(),
                                                           anyString(),
                                                           anyString(),
                                                           anyString(),
                                                           Matchers.<ConfirmCallback>anyObject(),
                                                           Matchers.<CancelCallback>anyObject());
    }

    @Test
    public void projectExistInWSButAbsentOnVFSDialogShouldBeShown() {
        ProjectProblemDto problem = newDto(ProjectProblemDto.class).withCode(10);

        problems.add(problem);

        listener.onBeforeExpand(event);

        verify(locale).synchronizeDialogTitle();
        verify(locale).existInWorkspaceDialogContent(PROJECT_NAME);
        verify(projectConfig).getName();
        verify(locale).buttonImport();
        verify(locale).buttonRemove();

        verify(dialogFactory).createConfirmDialog(anyString(),
                                                  anyString(),
                                                  anyString(),
                                                  anyString(),
                                                  Matchers.<ConfirmCallback>anyObject(),
                                                  Matchers.<CancelCallback>anyObject());
        verify(confirmDialog).show();
    }

    @Test
    public void changeLocationDialogShouldBeShownIfProjectHasNoLocationOrLocationIsIncorrectAndProjectShouldBeImported() {
        when(sourceStorage.getLocation()).thenReturn(null);
        ProjectProblemDto problem = newDto(ProjectProblemDto.class).withCode(10);

        problems.add(problem);

        listener.onBeforeExpand(event);

        verify(dialogFactory).createConfirmDialog(anyString(),
                                                  anyString(),
                                                  anyString(),
                                                  anyString(),
                                                  confirmCaptor.capture(),
                                                  Matchers.<CancelCallback>anyObject());
        confirmCaptor.getValue().accepted();

        verify(locale).locationDialogTitle();
        verify(dialogFactory).createConfirmDialog(anyString(),
                                                  eq(changeLocationWidget),
                                                  confirmCaptor.capture(),
                                                  Matchers.<CancelCallback>anyObject());

        confirmCaptor.getValue().accepted();

        verify(changeLocationWidget).getText();
        verify(sourceStorage).setLocation(anyString());

        verify(projectImporter).importProject(Matchers.<Wizard.CompleteCallback>anyObject(), eq(projectConfig));
    }

    @Test
    public void projectExistOnVFSButAbsentInWSDialogShouldBeShown() {
        ProjectProblemDto problem = newDto(ProjectProblemDto.class).withCode(9);

        problems.add(problem);

        listener.onBeforeExpand(event);

        verify(locale).synchronizeDialogTitle();
        verify(locale).existInFileSystemDialogContent(PROJECT_NAME);
        verify(locale).buttonConfigure();
        verify(locale).buttonRemove();

        verify(dialogFactory).createConfirmDialog(anyString(),
                                                  anyString(),
                                                  anyString(),
                                                  anyString(),
                                                  confirmCaptor.capture(),
                                                  Matchers.<CancelCallback>anyObject());
        confirmCaptor.getValue().accepted();

        verify(eventBus).fireEvent(Matchers.<ConfigureProjectEvent>anyObject());
    }

    @Test
    public void projectShouldBeDeletedFromWorkspaceWhenWeRemoveId() {
        ProjectProblemDto problem = newDto(ProjectProblemDto.class).withCode(9);

        problems.add(problem);

        listener.onBeforeExpand(event);

        verify(dialogFactory).createConfirmDialog(anyString(),
                                                  anyString(),
                                                  anyString(),
                                                  anyString(),
                                                  confirmCaptor.capture(),
                                                  cancelCaptor.capture());
        cancelCaptor.getValue().cancelled();

        //noinspection unchecked
        verify(projectService).delete(eq(devMachine), anyString(), Matchers.<AsyncRequestCallback>anyObject());
    }

    @Test
    public void projectConfigurationChangedDialogShouldBeShown() {
        ProjectProblemDto problem = newDto(ProjectProblemDto.class).withCode(8);

        problems.add(problem);

        listener.onBeforeExpand(event);

        verify(locale).synchronizeDialogTitle();
        verify(locale).projectConfigurationChanged();
        verify(locale).buttonConfigure();
        verify(locale).buttonKeepBlank();

        verify(dialogFactory).createConfirmDialog(anyString(),
                                                  anyString(),
                                                  anyString(),
                                                  anyString(),
                                                  confirmCaptor.capture(),
                                                  Matchers.<CancelCallback>anyObject());
        confirmCaptor.getValue().accepted();

        verify(eventBus).fireEvent(Matchers.<ConfigureProjectEvent>anyObject());
    }

    @Test
    public void projectShouldBeUpdatedAsBlankWhenProjectConfigurationChanged() {
        ProjectProblemDto problem = newDto(ProjectProblemDto.class).withCode(8);

        problems.add(problem);

        listener.onBeforeExpand(event);

        verify(dialogFactory).createConfirmDialog(anyString(),
                                                  anyString(),
                                                  anyString(),
                                                  anyString(),
                                                  confirmCaptor.capture(),
                                                  cancelCaptor.capture());
        cancelCaptor.getValue().cancelled();

        verify(projectConfig).setType(Constants.BLANK_ID);
        verify(appContext).getDevMachine();

        //noinspection unchecked

        verify(projectService).updateProject(eq(devMachine), anyString(), eq(projectConfig), Matchers.<AsyncRequestCallback>anyObject());
    }

    @Test
    public void messageDialogShouldBeShownWhenProjectIsInImportingState() {
        MessageDialog messageDialog = mock(MessageDialog.class);
        when(dialogFactory.createMessageDialog(anyString(),
                                               anyString(),
                                               Matchers.<ConfirmCallback>anyObject())).thenReturn(messageDialog);

        List<String> importingProjects = Arrays.asList("/test");
        when(projectConfig.getPath()).thenReturn("/test");
        when(appContext.getImportingProjects()).thenReturn(importingProjects);

        listener.onBeforeExpand(event);

        verify(event).setCancelled(true);
        verify(dialogFactory).createMessageDialog(anyString(), anyString(), Matchers.<ConfirmCallback>anyObject());
        verify(messageDialog).show();

        verify(projectConfig, never()).getProblems();
    }
}
