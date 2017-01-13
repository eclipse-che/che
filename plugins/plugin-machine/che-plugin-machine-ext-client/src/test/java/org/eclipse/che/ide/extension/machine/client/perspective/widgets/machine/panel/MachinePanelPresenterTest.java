/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
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
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceRuntimeDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.event.ActivePartChangedEvent;
import org.eclipse.che.ide.api.machine.MachineEntity;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStartedEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.eclipse.che.ide.extension.machine.client.inject.factories.EntityFactory;
import org.eclipse.che.ide.extension.machine.client.inject.factories.WidgetsFactory;
import org.eclipse.che.ide.api.machine.events.MachineStateEvent;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.appliance.MachineAppliancePresenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
    private ProjectConfigDto    projectConfig;
    @Mock
    private MachineEntity       machine1;
    @Mock
    private MachineEntity       machine2;
    @Mock
    private MachineDto machineDtoFromAPI1;
    @Mock
    private MachineDto machineDtoFromAPI2;
    @Mock
    private MachineEntity       selectedMachine1;
    @Mock
    private MachineEntity       selectedMachine2;
    @Mock
    private AcceptsOneWidget    container;
    @Mock
    private MachineTreeNode     rootNode;
    @Mock
    private MachineTreeNode     machineNode1;
    @Mock
    private MachineTreeNode     machineNode2;
    @Mock
    private MachineStateEvent   stateEvent;
    @Mock
    private AppContext          appContext;
    @Mock
    private Workspace           usersWorkspace;
    @Mock
    private WorkspaceRuntimeDto workspaceRuntime;

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
                                             eq(machine2),
                                             isNull(List.class))).thenReturn(machineNode2);
        //noinspection unchecked
        when(entityFactory.createMachineNode(eq(rootNode),
                                             eq(machine1),
                                             isNull(List.class))).thenReturn(machineNode1);

        presenter = new MachinePanelPresenter(view, entityFactory, locale, appliance, eventBus, resources, appContext);

        when(appContext.getWorkspace()).thenReturn(usersWorkspace);
        when(usersWorkspace.getRuntime()).thenReturn(workspaceRuntime);
        when(usersWorkspace.getId()).thenReturn(TEXT);
    }

    @Test
    public void constructorShouldBeVerified() {
        verify(entityFactory).createMachineNode(eq(null), eq("root"), Matchers.<List<MachineTreeNode>>anyObject());

        verify(eventBus).addHandler(WorkspaceStartedEvent.TYPE, presenter);
        verify(eventBus).addHandler(WorkspaceStoppedEvent.TYPE, presenter);
    }

    @Test
    public void shouldSubscribeToMachineStateEvent() {
        WorkspaceStartedEvent event = mock(WorkspaceStartedEvent.class);
        when(event.getWorkspace()).thenReturn(usersWorkspace);

        presenter.onWorkspaceStarted(event);

        verify(eventBus).addHandler(MachineStateEvent.TYPE, presenter);
    }

    @Test
    public void treeShouldBeDisplayedWithMachines() throws Exception {
        when(workspaceRuntime.getMachines()).thenReturn(Collections.singletonList(machineDtoFromAPI1));

        presenter.showMachines();

        verify(appContext).getWorkspace();
        verify(usersWorkspace).getRuntime();

        verify(entityFactory).createMachineNode(isNull(MachineTreeNode.class), eq("root"), Matchers.<List<MachineTreeNode>>anyObject());
        verify(entityFactory).createMachineNode(eq(rootNode), eq(machine1), eq(null));

        verify(view).setData(Matchers.<MachineTreeNode>anyObject());
        verify(view).selectNode(machineNode1);
    }

    @Test
    public void stubShouldBeDisplayedWhenMachinesNotExist() throws OperationException {
        when(workspaceRuntime.getMachines()).thenReturn(Collections.<MachineDto>emptyList());

        presenter.showMachines();

        verify(locale).unavailableMachineInfo();
        verify(appliance).showStub(anyString());

        verify(view, never()).setData(rootNode);
    }

    @Test
    public void applianceShouldBeShownForSelectedMachine() throws Exception {
        when(selectedMachine1.getStatus()).thenReturn(MachineStatus.RUNNING);

        presenter.onMachineSelected(selectedMachine1);

        verify(selectedMachine1, times(2)).getId();

        verify(appliance).showAppliance(selectedMachine1);

        assertThat(selectedMachine1, is(equalTo(presenter.getSelectedMachineState())));
        assertThat(presenter.isMachineRunning(), is(true));
    }

    @Test
    public void machineShouldBeGotFromCacheWhenWeSelectMachineTheSecondTime() throws Exception {
        when(selectedMachine1.getStatus()).thenReturn(MachineStatus.RUNNING);
        presenter.onMachineSelected(selectedMachine1);
        reset(appliance, selectedMachine1);

        presenter.onMachineSelected(selectedMachine1);

        verify(appliance).showAppliance(selectedMachine1);
        verify(selectedMachine1, never()).getStatus();

        assertThat(selectedMachine1, is(equalTo(presenter.getSelectedMachineState())));
        assertThat(presenter.isMachineRunning(), is(true));
    }

    @Test
    public void stubShouldBeDisplayedWhenWeTryGetMachineWhichIsNotCreatedYet() throws Exception {
        when(selectedMachine1.getStatus()).thenReturn(MachineStatus.CREATING);

        presenter.onMachineSelected(selectedMachine1);

        verify(locale).unavailableMachineStarting(anyString());
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
        when(machine1.getId()).thenReturn("machine1");

        MachineStateEvent stateEvent = mock(MachineStateEvent.class);
        when(stateEvent.getMachine()).thenReturn(machine1);

        presenter.onMachineCreating(stateEvent);

        verify(view).setData(rootNode);
        verify(view).selectNode(machineNode1);

        assertThat(presenter.isMachineRunning(), is(false));
    }

    @Test
    public void machineShouldBeSelectedWhenItIsRunning() {
        when(machine1.getId()).thenReturn("machine1");

        MachineStateEvent stateEvent = mock(MachineStateEvent.class);
        when(stateEvent.getMachine()).thenReturn(machine1);

        presenter.onMachineCreating(stateEvent);
        reset(view);
        presenter.onMachineRunning(stateEvent);

        verify(view).selectNode(machineNode1);

        assertThat(presenter.isMachineRunning(), is(true));
    }

    @Test
    public void machineShouldBeRemovedFromTreeWhenItIsDestroyed() {
        when(machine1.getId()).thenReturn("machine1");

        MachineStateEvent stateEvent = mock(MachineStateEvent.class);
        when(stateEvent.getMachineId()).thenReturn("machine1");
        when(stateEvent.getMachine()).thenReturn(machine1);

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
        when(workspaceRuntime.getMachines()).thenReturn(Collections.singletonList(machineDtoFromAPI1));
        ActivePartChangedEvent event = mock(ActivePartChangedEvent.class);
        when(event.getActivePart()).thenReturn(presenter);

        presenter.onActivePartChanged(event);

        verify(event).getActivePart();
        verify(appContext).getWorkspace();
        verify(usersWorkspace).getRuntime();

        verify(entityFactory).createMachineNode(isNull(MachineTreeNode.class), eq("root"), Matchers.<List<MachineTreeNode>>anyObject());
        verify(entityFactory).createMachineNode(eq(rootNode), eq(machine1), eq(null));

        verify(view).setData(Matchers.<MachineTreeNode>anyObject());
        verify(view).selectNode(machineNode1);
    }
}
