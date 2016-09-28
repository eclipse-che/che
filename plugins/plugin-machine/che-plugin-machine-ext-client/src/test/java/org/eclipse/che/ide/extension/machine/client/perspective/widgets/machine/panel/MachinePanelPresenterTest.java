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

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.dialogs.InputCallback;
import org.eclipse.che.ide.api.event.ActivePartChangedEvent;
import org.eclipse.che.ide.api.machine.MachineServiceClient;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStartedEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.eclipse.che.ide.extension.machine.client.inject.factories.EntityFactory;
import org.eclipse.che.ide.extension.machine.client.inject.factories.WidgetsFactory;
import org.eclipse.che.ide.extension.machine.client.machine.Machine;
import org.eclipse.che.ide.extension.machine.client.machine.MachineStateEvent;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.appliance.MachineAppliancePresenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.vectomatic.dom.svg.ui.SVGResource;

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
import static org.mockito.Mockito.times;
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
    private Promise<List<MachineDto>> machinesPromise;
    @Mock
    private Promise<MachineDto>       machinePromise;
    @Mock
    private ProjectConfigDto          projectConfig;
    @Mock
    private MachineDto                machineDtoFromAPI1;
    @Mock
    private MachineDto                machineDtoFromAPI2;
    @Mock
    private Machine                   machine1;
    @Mock
    private Machine                   machine2;
    @Mock
    private MachineDto                selectedMachine1;
    @Mock
    private MachineDto                selectedMachine2;
    @Mock
    private AcceptsOneWidget          container;
    @Mock
    private MachineTreeNode           rootNode;
    @Mock
    private MachineTreeNode           machineNode1;
    @Mock
    private MachineTreeNode           machineNode2;
    @Mock
    private WorkspaceDto              workspaceDto;
    @Mock
    private MachineStateEvent         stateEvent;
    @Mock
    private AppContext                appContext;
    @Mock
    private Workspace                 usersWorkspace;

    @Captor
    private ArgumentCaptor<Operation<List<MachineDto>>> operationMachineStateCaptor;
    @Captor
    private ArgumentCaptor<Operation<MachineDto>>       operationMachineCaptor;
    @Captor
    private ArgumentCaptor<InputCallback>               inputCallbackCaptor;
    @Captor
    private ArgumentCaptor<Operation<PromiseError>>     errorPromiseCaptor;

    private MachinePanelPresenter presenter;

    @Before
    public void setUp() {
        when(entityFactory.createMachine(machineDtoFromAPI1)).thenReturn(machine1);
        when(entityFactory.createMachine(machineDtoFromAPI2)).thenReturn(machine2);

        MachineConfigDto machineConfig1 = mock(MachineConfigDto.class);
        MachineConfigDto machineConfig2 = mock(MachineConfigDto.class);
        when(selectedMachine1.getConfig()).thenReturn(machineConfig1);
        when(selectedMachine2.getConfig()).thenReturn(machineConfig2);

        when(entityFactory.createMachineNode(isNull(MachineTreeNode.class),
                                             anyString(),
                                             Matchers.<List<MachineTreeNode>>anyObject())).thenReturn(rootNode);

        //noinspection unchecked
        when(entityFactory.createMachineNode(eq(rootNode),
                                             eq(selectedMachine2),
                                             isNull(List.class))).thenReturn(machineNode2);
        //noinspection unchecked
        when(entityFactory.createMachineNode(eq(rootNode),
                                             eq(selectedMachine1),
                                             isNull(List.class))).thenReturn(machineNode1);

        presenter = new MachinePanelPresenter(view, service, entityFactory, locale, appliance, eventBus, resources, appContext);

        when(service.getMachines(anyString())).thenReturn(machinesPromise);
        when(machinesPromise.then(Matchers.<Operation<List<MachineDto>>>anyObject())).thenReturn(machinesPromise);

        when(service.getMachine(anyString(), anyString())).thenReturn(machinePromise);
        when(machinePromise.then(Matchers.<Operation<MachineDto>>anyObject())).thenReturn(machinePromise);

        when(appContext.getWorkspace()).thenReturn(usersWorkspace);
        when(usersWorkspace.getId()).thenReturn(TEXT);
    }

    @Test
    public void constructorShouldBeVerified() {
        verify(entityFactory).createMachineNode(eq(null), eq("root"), Matchers.<List<MachineTreeNode>>anyObject());

        verify(eventBus).addHandler(MachineStateEvent.TYPE, presenter);
        verify(eventBus).addHandler(WorkspaceStartedEvent.TYPE, presenter);
        verify(eventBus).addHandler(WorkspaceStoppedEvent.TYPE, presenter);
    }

    @Test
    public void treeShouldBeDisplayedWithMachines() throws Exception {
        presenter.showMachines();

        verify(service).getMachines(anyString());

        verify(machinesPromise).then(operationMachineStateCaptor.capture());
        operationMachineStateCaptor.getValue().apply(Collections.singletonList(selectedMachine1));

        verify(entityFactory).createMachineNode(isNull(MachineTreeNode.class), eq("root"), Matchers.<List<MachineTreeNode>>anyObject());
        verify(entityFactory).createMachineNode(eq(rootNode), eq(selectedMachine1), eq(null));

        verify(view).setData(Matchers.<MachineTreeNode>anyObject());
        verify(view).selectNode(machineNode1);
    }

    @Test
    public void stubShouldBeDisplayedWhenMachinesNotExist() throws OperationException {
        presenter.showMachines();

        verify(machinesPromise).then(operationMachineStateCaptor.capture());
        operationMachineStateCaptor.getValue().apply(Collections.<MachineDto>emptyList());

        verify(locale).unavailableMachineInfo();
        verify(appliance).showStub(anyString());

        verify(view, never()).setData(rootNode);
    }

    @Test
    public void requestShouldBeSendToGetMachine() throws Exception {
        when(machineDtoFromAPI1.getStatus()).thenReturn(MachineStatus.RUNNING);

        presenter.onMachineSelected(selectedMachine1);

        verify(selectedMachine1, times(2)).getId();

        verify(machinePromise).then(operationMachineCaptor.capture());
        operationMachineCaptor.getValue().apply(machineDtoFromAPI1);

        verify(entityFactory).createMachine(machineDtoFromAPI1);
        verify(appliance).showAppliance(machine1);

        assertThat(selectedMachine1, is(equalTo(presenter.getSelectedMachineState())));
        assertThat(presenter.isMachineRunning(), is(true));
    }

    @Test
    public void machineShouldBeGotFromCacheWhenWeSelectMachineTheSecondTime() throws Exception {
        when(machineDtoFromAPI1.getStatus()).thenReturn(MachineStatus.RUNNING);

        presenter.onMachineSelected(selectedMachine1);

        verify(machinePromise).then(operationMachineCaptor.capture());
        operationMachineCaptor.getValue().apply(machineDtoFromAPI1);
        reset(service, appliance);

        presenter.onMachineSelected(selectedMachine1);

        verify(appliance).showAppliance(machine1);

        verify(service, never()).getMachine(anyString(), anyString());

        assertThat(selectedMachine1, is(equalTo(presenter.getSelectedMachineState())));
        assertThat(presenter.isMachineRunning(), is(true));
    }

    @Test
    public void stubShouldBeDisplayedWhenWeTryGetMachineWhichIsNotCreatedYet() throws Exception {
        PromiseError error = mock(PromiseError.class);

        presenter.onMachineSelected(selectedMachine1);

        verify(machinePromise).catchError(errorPromiseCaptor.capture());
        errorPromiseCaptor.getValue().apply(error);

        verify(locale).machineNotFound(anyString());
        verify(appliance).showStub(anyString());

        assertThat(presenter.isMachineRunning(), is(false));
        assertThat(selectedMachine1, is(equalTo(presenter.getSelectedMachineState())));
    }

    @Test
    public void titleShouldBeReturned() {
        presenter.getTitle();

        verify(locale).machinePanelTitle();
    }

    @Test
    public void titleImageShouldBeReturned() {
        SVGResource resource = presenter.getTitleImage();

        assertThat(resource, nullValue(SVGResource.class));
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
        when(selectedMachine1.getId()).thenReturn("machine1");

        MachineStateEvent stateEvent = mock(MachineStateEvent.class);
        when(stateEvent.getMachineId()).thenReturn("machine1");
        when(stateEvent.getMachine()).thenReturn(selectedMachine1);

        presenter.onMachineCreating(stateEvent);

        verify(view).setData(rootNode);
        verify(view).selectNode(machineNode1);

        assertThat(presenter.isMachineRunning(), is(false));
    }

    @Test
    public void machineShouldBeSelectedWhenItIsRunning() {
        when(selectedMachine1.getId()).thenReturn("machine1");

        MachineStateEvent stateEvent = mock(MachineStateEvent.class);
        when(stateEvent.getMachineId()).thenReturn("machine1");
        when(stateEvent.getMachine()).thenReturn(selectedMachine1);

        presenter.onMachineCreating(stateEvent);
        reset(view);
        presenter.onMachineRunning(stateEvent);

        verify(view).selectNode(machineNode1);

        assertThat(presenter.isMachineRunning(), is(true));
    }

    @Test
    public void machineShouldBeRemovedFromTreeWhenItIsDestroyed() {
        when(selectedMachine1.getId()).thenReturn("machine1");

        MachineStateEvent stateEvent = mock(MachineStateEvent.class);
        when(stateEvent.getMachineId()).thenReturn("machine1");
        when(stateEvent.getMachine()).thenReturn(selectedMachine1);

        presenter.onMachineCreating(stateEvent);
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
        verify(service).getMachines(anyString());

        verify(machinesPromise).then(operationMachineStateCaptor.capture());
        operationMachineStateCaptor.getValue().apply(Collections.singletonList(selectedMachine1));

        verify(entityFactory).createMachineNode(isNull(MachineTreeNode.class), eq("root"), Matchers.<List<MachineTreeNode>>anyObject());
        verify(entityFactory).createMachineNode(eq(rootNode), eq(selectedMachine1), eq(null));

        verify(view).setData(Matchers.<MachineTreeNode>anyObject());
        verify(view).selectNode(machineNode1);
    }

}
