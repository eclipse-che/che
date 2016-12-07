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
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceRuntime;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.event.ActivePartChangedEvent;
import org.eclipse.che.ide.api.event.ActivePartChangedHandler;
import org.eclipse.che.ide.api.machine.MachineEntity;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStartedEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.eclipse.che.ide.extension.machine.client.inject.factories.EntityFactory;
import org.eclipse.che.ide.api.machine.events.MachineStateEvent;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.appliance.MachineAppliancePresenter;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.eclipse.che.api.core.model.machine.MachineStatus.RUNNING;

/**
 * The class contains business logic to control displaying of machines on special view.
 *
 * @author Dmitry Shnurenko
 * @author Artem Zatsarynnyi
 */
@Singleton
public class MachinePanelPresenter extends BasePresenter implements MachinePanelView.ActionDelegate,
                                                                    MachineStateEvent.Handler,
                                                                    WorkspaceStartedEvent.Handler,
                                                                    WorkspaceStoppedEvent.Handler,
                                                                    ActivePartChangedHandler {
    private final MachinePanelView             view;
    private final EventBus                     eventBus;
    private final EntityFactory                entityFactory;
    private final MachineLocalizationConstant  locale;
    private final MachineAppliancePresenter    appliance;
    private final MachineResources             resources;
    private final Map<String, MachineTreeNode> existingMachineNodes;
    private final Map<String, MachineEntity>   cachedMachines;
    private final MachineTreeNode              rootNode;
    private final List<MachineTreeNode>        machineNodes;
    private final AppContext                   appContext;

    private MachineEntity selectedMachine;
    private boolean       isMachineRunning;

    @Inject
    public MachinePanelPresenter(MachinePanelView view,
                                 EntityFactory entityFactory,
                                 MachineLocalizationConstant locale,
                                 MachineAppliancePresenter appliance,
                                 EventBus eventBus,
                                 MachineResources resources,
                                 AppContext appContext) {
        this.view = view;
        this.eventBus = eventBus;
        this.view.setDelegate(this);

        this.entityFactory = entityFactory;
        this.locale = locale;
        this.appliance = appliance;
        this.resources = resources;
        this.appContext = appContext;

        this.machineNodes = new ArrayList<>();
        this.rootNode = entityFactory.createMachineNode(null, "root", machineNodes);

        this.existingMachineNodes = new HashMap<>();
        this.cachedMachines = new HashMap<>();

        eventBus.addHandler(WorkspaceStartedEvent.TYPE, this);
        eventBus.addHandler(WorkspaceStoppedEvent.TYPE, this);
        eventBus.addHandler(ActivePartChangedEvent.TYPE, this);
    }

    /** Gets all machines and adds them to special place on view. */
    public void showMachines() {
        showMachines(appContext.getWorkspace());
    }

    private void showMachines(Workspace workspace) {
        List<MachineEntity> machines = getMachines(workspace);
        machineNodes.clear();
        if (machines.isEmpty()) {
            appliance.showStub(locale.unavailableMachineInfo());
            return;
        }

        for (MachineEntity machine : machines) {
            addNodeToTree(machine);
        }

        view.setData(rootNode);
        selectFirstNode();
    }

    private List<MachineEntity> getMachines(Workspace workspace) {
        WorkspaceRuntime workspaceRuntime = workspace.getRuntime();
        if (workspaceRuntime == null) {
            return emptyList();
        }

        List<? extends Machine> runtimeMachines = workspaceRuntime.getMachines();
        List<MachineEntity> machines = new ArrayList<>(runtimeMachines.size());
        for (Machine machine : runtimeMachines) {
            if (machine instanceof MachineDto) {
                MachineEntity machineEntity = entityFactory.createMachine((MachineDto)machine);
                machines.add(machineEntity);
            }

        }
        return machines;
    }

    private MachineTreeNode addNodeToTree(Machine machine) {
        MachineTreeNode machineNode = entityFactory.createMachineNode(rootNode, machine, null);

        existingMachineNodes.put(machine.getId(), machineNode);

        if (!machineNodes.contains(machineNode)) {
            machineNodes.add(machineNode);
        }

        return machineNode;
    }

    private void selectFirstNode() {
        if (!machineNodes.isEmpty()) {
            view.selectNode(machineNodes.get(0));
        }
    }

    /**
     * Returns selected machine state.
     */
    public MachineEntity getSelectedMachineState() {
        return selectedMachine;
    }

    /** {@inheritDoc} */
    @Override
    public void onMachineSelected(final MachineEntity selectedMachine) {
        this.selectedMachine = selectedMachine;

        if (cachedMachines.containsKey(selectedMachine.getId())) {
            appliance.showAppliance(cachedMachines.get(selectedMachine.getId()));
            return;
        }

        if (RUNNING == selectedMachine.getStatus()) {
            isMachineRunning = true;

            cachedMachines.put(selectedMachine.getId(), selectedMachine);

            appliance.showAppliance(selectedMachine);
        } else {
            isMachineRunning = false;

            final MachineConfig machineConfig = selectedMachine.getConfig();
            final boolean isDevMachine = machineConfig.isDev();
            final String machineName = machineConfig.getName();

            // we show the loader for dev machine so this message isn't necessary for dev machine
            if (!isDevMachine) {
                appliance.showStub(locale.unavailableMachineStarting(machineName));
            }
        }
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    public String getTitle() {
        return locale.machinePanelTitle();
    }

    /** {@inheritDoc} */
    @Override
    public IsWidget getView() {
        return view;
    }

    @Nullable
    @Override
    public SVGResource getTitleImage() {
        return resources.machinesPartIcon();
    }

    /** {@inheritDoc} */
    @Nullable
    @Override
    public String getTitleToolTip() {
        return locale.machinePanelTooltip();
    }

    /** {@inheritDoc} */
    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }

    /** {@inheritDoc} */
    @Override
    public void onWorkspaceStarted(WorkspaceStartedEvent event) {
        eventBus.addHandler(MachineStateEvent.TYPE, this);
        showMachines(event.getWorkspace());
    }

    /** {@inheritDoc} */
    @Override
    public void onWorkspaceStopped(WorkspaceStoppedEvent event) {
        showMachines(event.getWorkspace());
    }

    @Override
    public void onMachineCreating(MachineStateEvent event) {
        isMachineRunning = false;

        selectedMachine = event.getMachine();

        addNodeToTree(selectedMachine);

        view.setData(rootNode);

        view.selectNode(existingMachineNodes.get(selectedMachine.getId()));
    }

    /** {@inheritDoc} */
    @Override
    public void onMachineRunning(final MachineStateEvent event) {
        isMachineRunning = true;

        selectedMachine = event.getMachine();
        final MachineTreeNode machineTreeNode = existingMachineNodes.get(selectedMachine.getId());
        if (machineTreeNode == null) {
            return;
        }

        machineTreeNode.setData(selectedMachine);
        view.selectNode(machineTreeNode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMachineDestroyed(MachineStateEvent event) {
        Machine machine = event.getMachine();

        MachineTreeNode deletedNode = existingMachineNodes.get(machine.getId());

        machineNodes.remove(deletedNode);
        existingMachineNodes.remove(machine.getId());

        view.setData(rootNode);

        selectFirstNode();
    }

    /**
     * Returns <code>true</code> if selected machine running, and <code>false</code> if selected machine isn't running
     */
    public boolean isMachineRunning() {
        return isMachineRunning;
    }

    @Override
    public void onActivePartChanged(ActivePartChangedEvent event) {
        if ((event.getActivePart() instanceof MachinePanelPresenter)) {
            showMachines();
        }
    }
}
