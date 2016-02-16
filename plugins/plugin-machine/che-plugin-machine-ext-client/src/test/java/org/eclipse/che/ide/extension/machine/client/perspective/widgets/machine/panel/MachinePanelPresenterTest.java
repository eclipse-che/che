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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.panel;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.machine.gwt.client.MachineServiceClient;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.machine.shared.dto.MachineStateDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.UsersWorkspaceDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.event.ActivePartChangedEvent;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.eclipse.che.ide.extension.machine.client.inject.factories.EntityFactory;
import org.eclipse.che.ide.extension.machine.client.inject.factories.WidgetsFactory;
import org.eclipse.che.ide.extension.machine.client.machine.Machine;
import org.eclipse.che.api.machine.gwt.client.events.MachineStartingEvent;
import org.eclipse.che.ide.extension.machine.client.machine.events.MachineStateEvent;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.appliance.MachineAppliancePresenter;
import org.eclipse.che.ide.ui.dialogs.InputCallback;
import org.eclipse.che.api.workspace.gwt.client.event.WorkspaceStartedEvent;
import org.eclipse.che.ide.workspace.start.StopWorkspaceEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class MachinePanelPresenterTest {

    private static final String TEXT = "There is nothing either good or bad but thinking makes it so.";
    //constructor mocks
    @Mock
    private MachinePanelView            view;
    @Mock
    private MachineServiceClient        service;
    @Mock
    private EntityFactory               entityFactory;
    @Mock
    private WidgetsFactory              widgetsFactory;
    @Mock
    private MachineLocalizationConstant locale;
    @Mock
    private MachineAppliancePresenter   appliance;
    @Mock
    private EventBus                    eventBus;
    @Mock
    private MachineResources            resources;

    //additional mocks
    @Mock
    private Promise<List<MachineStateDto>> machineStatePromise;
    @Mock
    private Promise<MachineDto>            machinePromise;
    @Mock
    private CurrentProject                 currentProject;
    @Mock
    private ProjectConfigDto               projectConfig;
    @Mock
    private MachineDto                     machineDescriptor1;
    @Mock
    private MachineDto                     machineDescriptor2;
    @Mock
    private Machine                        machine1;
    @Mock
    private Machine                        machine2;
    @Mock
    private MachineStateDto                machineState1;
    @Mock
    private MachineStateDto                machineState2;
    @Mock
    private AcceptsOneWidget               container;
    @Mock
    private MachineTreeNode                rootNode;
    @Mock
    private MachineTreeNode                machineNode1;
    @Mock
    private MachineTreeNode                machineNode2;
    @Mock
    private UsersWorkspaceDto              workspaceDto;
    @Mock
    private MachineStateEvent              stateEvent;
    @Mock
    private AppContext                     appContext;
    @Mock
    private UsersWorkspaceDto              usersWorkspaceDto;

    @Captor
    private ArgumentCaptor<Operation<List<MachineStateDto>>> operationMachineStateCaptor;
    @Captor
    private ArgumentCaptor<Operation<MachineDto>>            operationMachineCaptor;
    @Captor
    private ArgumentCaptor<InputCallback>                    inputCallbackCaptor;
    @Captor
    private ArgumentCaptor<Operation<PromiseError>>          errorPromiseCaptor;

    private MachinePanelPresenter presenter;

    @Before
    public void setUp() {
        when(entityFactory.createMachine(machineDescriptor1)).thenReturn(machine1);
        when(entityFactory.createMachine(machineDescriptor2)).thenReturn(machine2);

        when(entityFactory.createMachineNode(isNull(MachineTreeNode.class),
                                             anyString(),
                                             Matchers.<List<MachineTreeNode>>anyObject())).thenReturn(rootNode);

        //noinspection unchecked
        when(entityFactory.createMachineNode(eq(rootNode),
                                             eq(machineState2),
                                             isNull(List.class))).thenReturn(machineNode2);
        //noinspection unchecked
        when(entityFactory.createMachineNode(eq(rootNode),
                                             eq(machineState1),
                                             isNull(List.class))).thenReturn(machineNode1);

        presenter = new MachinePanelPresenter(view, service, entityFactory, locale, appliance, eventBus, resources, appContext);

        when(service.getMachinesStates(anyString())).thenReturn(machineStatePromise);
        when(machineStatePromise.then(Matchers.<Operation<List<MachineStateDto>>>anyObject())).thenReturn(machineStatePromise);

        when(service.getMachine(anyString())).thenReturn(machinePromise);
        when(machinePromise.then(Matchers.<Operation<MachineDto>>anyObject())).thenReturn(machinePromise);

        when(appContext.getWorkspace()).thenReturn(usersWorkspaceDto);
        when(usersWorkspaceDto.getId()).thenReturn(TEXT);
    }

    @Test
    public void constructorShouldBeVerified() {
        verify(entityFactory).createMachineNode(eq(null), eq("root"), Matchers.<List<MachineTreeNode>>anyObject());

        verify(eventBus).addHandler(MachineStateEvent.TYPE, presenter);
        verify(eventBus).addHandler(WorkspaceStartedEvent.TYPE, presenter);
        verify(eventBus).addHandler(StopWorkspaceEvent.TYPE, presenter);
        verify(eventBus).addHandler(MachineStartingEvent.TYPE, presenter);
    }

    @Test
    public void treeShouldBeDisplayedWithMachines() throws Exception {
        presenter.showMachines();

        verify(service).getMachinesStates(anyString());

        verify(machineStatePromise).then(operationMachineStateCaptor.capture());
        operationMachineStateCaptor.getValue().apply(Collections.singletonList(machineState1));

        verify(entityFactory).createMachineNode(isNull(MachineTreeNode.class), eq("root"), Matchers.<List<MachineTreeNode>>anyObject());
        verify(entityFactory).createMachineNode(eq(rootNode), eq(machineState1), eq(null));

        verify(view).setData(Matchers.<MachineTreeNode>anyObject());
        verify(view).selectNode(machineNode1);
    }

    @Test
    public void stubShouldBeDisplayedWhenMachinesNotExist() throws OperationException {
        presenter.showMachines();

        verify(machineStatePromise).then(operationMachineStateCaptor.capture());
        operationMachineStateCaptor.getValue().apply(Collections.<MachineStateDto>emptyList());

        verify(locale).unavailableMachineInfo();
        verify(appliance).showStub(anyString());

        verify(view, never()).setData(rootNode);
    }

    @Test
    public void requestShouldBeSendToGetMachine() throws Exception {
        presenter.onMachineSelected(machineState1);

        verify(machineState1).getId();

        verify(machinePromise).then(operationMachineCaptor.capture());
        operationMachineCaptor.getValue().apply(machineDescriptor1);

        verify(entityFactory).createMachine(machineDescriptor1);
        verify(appliance).showAppliance(machine1);

        assertThat(machineState1, is(equalTo(presenter.getSelectedMachineState())));
        assertThat(presenter.isMachineRunning(), is(true));
    }

    @Test
    public void machineShouldBeGotFromCacheWhenWeSelectMachineTheSecondTime() throws Exception {
        presenter.onMachineSelected(machineState1);

        verify(machinePromise).then(operationMachineCaptor.capture());
        operationMachineCaptor.getValue().apply(machineDescriptor1);
        reset(service, appliance);

        presenter.onMachineSelected(machineState1);

        verify(appliance).showAppliance(machine1);

        verify(service, never()).getMachine(anyString());

        assertThat(machineState1, is(equalTo(presenter.getSelectedMachineState())));
        assertThat(presenter.isMachineRunning(), is(true));
    }

    @Test
    public void stubShouldBeDisplayedWhenWeTryGetMachineWhichIsNotCreatedYet() throws Exception {
        PromiseError error = mock(PromiseError.class);

        presenter.onMachineSelected(machineState1);

        verify(machinePromise).catchError(errorPromiseCaptor.capture());
        errorPromiseCaptor.getValue().apply(error);

        verify(machineState1).getName();
        verify(locale).unavailableMachineStarting(anyString());
        verify(appliance).showStub(anyString());

        assertThat(presenter.isMachineRunning(), is(false));
        assertThat(machineState1, is(equalTo(presenter.getSelectedMachineState())));
    }

    @Test
    public void titleShouldBeReturned() {
        presenter.getTitle();

        verify(locale).machinePanelTitle();
    }

    @Test
    public void titleImageShouldBeReturned() {
        ImageResource resource = presenter.getTitleImage();

        assertThat(resource, nullValue(ImageResource.class));
    }

    @Test
    public void titleTooltipShouldBeReturned() {
        presenter.getTitleToolTip();

        verify(locale).machinePanelTooltip();
    }

    @Test
    public void viewShouldBeSetToContainer() {
        presenter.go(container);

        verify(container).setWidget(view);
    }

    @Test
    public void machineShouldBeAddedToTreeWhenItIsJustCreated() {
        MachineStartingEvent startingEvent = mock(MachineStartingEvent.class);

        when(startingEvent.getMachineState()).thenReturn(machineState1);

        presenter.onMachineStarting(startingEvent);

        verify(view).setData(rootNode);
        verify(view).selectNode(machineNode1);

        assertThat(presenter.isMachineRunning(), is(false));
    }

    @Test
    public void machineShouldBeSelectedWhenItIsRunning() {
        MachineStartingEvent startingEvent = mock(MachineStartingEvent.class);

        when(startingEvent.getMachineState()).thenReturn(machineState1);
        when(stateEvent.getMachineState()).thenReturn(machineState1);

        presenter.onMachineStarting(startingEvent);
        reset(view);

        presenter.onMachineRunning(stateEvent);

        verify(view).selectNode(machineNode1);

        assertThat(presenter.isMachineRunning(), is(true));
    }

    @Test
    public void machineShouldBeRemovedFromTreeWhenItIsDestroyed() {
        MachineStartingEvent startingEvent = mock(MachineStartingEvent.class);

        when(startingEvent.getMachineState()).thenReturn(machineState1);
        when(stateEvent.getMachineState()).thenReturn(machineState1);

        presenter.onMachineStarting(startingEvent);
        reset(view);
        presenter.onMachineRunning(stateEvent);

        verify(view).selectNode(machineNode1);

        reset(view);

        presenter.onMachineDestroyed(stateEvent);

        verify(view).setData(rootNode);
        verify(view, never()).selectNode(machineNode1);
    }

    @Test
    public void shouldShowMachinesWhenMachinesPartIsActive() throws Exception {
        ActivePartChangedEvent event = mock(ActivePartChangedEvent.class);
        when(event.getActivePart()).thenReturn(presenter);

        presenter.onActivePartChanged(event);

        verify(event).getActivePart();
        verify(appContext).getWorkspace();
        verify(service).getMachinesStates(anyString());

        verify(machineStatePromise).then(operationMachineStateCaptor.capture());
        operationMachineStateCaptor.getValue().apply(Collections.singletonList(machineState1));

        verify(entityFactory).createMachineNode(isNull(MachineTreeNode.class), eq("root"), Matchers.<List<MachineTreeNode>>anyObject());
        verify(entityFactory).createMachineNode(eq(rootNode), eq(machineState1), eq(null));

        verify(view).setData(Matchers.<MachineTreeNode>anyObject());
        verify(view).selectNode(machineNode1);
    }
}
