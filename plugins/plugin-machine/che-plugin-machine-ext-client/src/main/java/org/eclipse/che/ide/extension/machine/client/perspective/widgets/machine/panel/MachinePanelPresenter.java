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

import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.event.ActivePartChangedEvent;
import org.eclipse.che.ide.api.event.ActivePartChangedHandler;
import org.eclipse.che.ide.api.machine.MachineServiceClient;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStartedEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.eclipse.che.ide.extension.machine.client.inject.factories.EntityFactory;
import org.eclipse.che.ide.extension.machine.client.machine.Machine;
import org.eclipse.che.ide.extension.machine.client.machine.MachineStateEvent;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.appliance.MachineAppliancePresenter;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final MachineServiceClient         service;
    private final EntityFactory                entityFactory;
    private final MachineLocalizationConstant  locale;
    private final MachineAppliancePresenter    appliance;
    private final MachineResources             resources;
    private final Map<String, MachineTreeNode> existingMachineNodes;
    private final Map<String, Machine>         cachedMachines;
    private final MachineTreeNode              rootNode;
    private final List<MachineTreeNode>        machineNodes;
    private final AppContext                   appContext;

    private org.eclipse.che.api.core.model.machine.Machine selectedMachine;
    private boolean                                        isMachineRunning;

    @Inject
    public MachinePanelPresenter(MachinePanelView view,
                                 MachineServiceClient service,
                                 EntityFactory entityFactory,
                                 MachineLocalizationConstant locale,
                                 MachineAppliancePresenter appliance,
                                 EventBus eventBus,
                                 MachineResources resources,
                                 AppContext appContext) {
        this.view = view;
        this.view.setDelegate(this);

        this.service = service;
        this.entityFactory = entityFactory;
        this.locale = locale;
        this.appliance = appliance;
        this.resources = resources;
        this.appContext = appContext;

        this.machineNodes = new ArrayList<>();
        this.rootNode = entityFactory.createMachineNode(null, "root", machineNodes);

        this.existingMachineNodes = new HashMap<>();
        this.cachedMachines = new HashMap<>();

        eventBus.addHandler(MachineStateEvent.TYPE, this);
        eventBus.addHandler(WorkspaceStartedEvent.TYPE, this);
        eventBus.addHandler(WorkspaceStoppedEvent.TYPE, this);
        eventBus.addHandler(ActivePartChangedEvent.TYPE, this);
    }

    /** Gets all machines and adds them to special place on view. */
    public Promise<List<MachineDto>> showMachines() {
        return showMachines(appContext.getWorkspace().getId());
    }

    private Promise<List<MachineDto>> showMachines(String workspaceId) {
        Promise<List<MachineDto>> machinesPromise = service.getMachines(workspaceId);

        return machinesPromise.then(new Operation<List<MachineDto>>() {
            @Override
            public void apply(List<MachineDto> machines) throws OperationException {
                machineNodes.clear();
                if (machines.isEmpty()) {
                    appliance.showStub(locale.unavailableMachineInfo());
                    return;
                }

                for (MachineDto machine : machines) {
                    addNodeToTree(machine);
                }

                view.setData(rootNode);
                selectFirstNode();
            }
        });
    }

    private void addNodeToTree(org.eclipse.che.api.core.model.machine.Machine machine) {
        MachineTreeNode machineNode = entityFactory.createMachineNode(rootNode, machine, null);

        existingMachineNodes.put(machine.getId(), machineNode);

        if (!machineNodes.contains(machineNode)) {
            machineNodes.add(machineNode);
        }
    }

    private void selectFirstNode() {
        if (!machineNodes.isEmpty()) {
            view.selectNode(machineNodes.get(0));
        }
    }

    /**
     * Returns selected machine state.
     */
    public org.eclipse.che.api.core.model.machine.Machine getSelectedMachineState() {
        return selectedMachine;
    }

    /** {@inheritDoc} */
    @Override
    public void onMachineSelected(final MachineDto selectedMachine) {
        this.selectedMachine = selectedMachine;

        if (cachedMachines.containsKey(selectedMachine.getId())) {
            appliance.showAppliance(cachedMachines.get(selectedMachine.getId()));

            return;
        }

        service.getMachine(selectedMachine.getWorkspaceId(),
                           selectedMachine.getId()).then(new Operation<MachineDto>() {
            @Override
            public void apply(MachineDto machineDto) throws OperationException {
                if (machineDto.getStatus() == MachineStatus.RUNNING) {
                    isMachineRunning = true;

                    Machine machine = entityFactory.createMachine(machineDto);

                    cachedMachines.put(selectedMachine.getId(), machine);

                    appliance.showAppliance(machine);
                } else {
                    isMachineRunning = false;
                    // we show the loader for dev machine so this message isn't necessary for dev machine
                    if (!selectedMachine.getConfig().isDev()) {
                        appliance.showStub(locale.unavailableMachineStarting(selectedMachine.getConfig().getName()));
                    }
                }
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                isMachineRunning = false;

                appliance.showStub(locale.machineNotFound(selectedMachine.getId()));
            }
        });
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    public String getTitle() {
        return locale.machinePanelTitle();
    }

    /** {@inheritDoc} */
    @Override
    public void setVisible(boolean visible) {
        view.setVisible(visible);
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
        showMachines(event.getWorkspace().getId());
    }

    /** {@inheritDoc} */
    @Override
    public void onWorkspaceStopped(WorkspaceStoppedEvent event) {
        showMachines(event.getWorkspace().getId());
    }

    @Override
    public void onMachineCreating(MachineStateEvent event) {
        isMachineRunning = false;

        selectedMachine = event.getMachine();

        addNodeToTree(selectedMachine);

        view.setData(rootNode);

        view.selectNode(existingMachineNodes.get(event.getMachine().getId()));
    }

    /** {@inheritDoc} */
    @Override
    public void onMachineRunning(final MachineStateEvent event) {
        isMachineRunning = true;

        selectedMachine = event.getMachine();

        view.selectNode(existingMachineNodes.get(selectedMachine.getId()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMachineDestroyed(MachineStateEvent event) {
        org.eclipse.che.api.core.model.machine.Machine machine = event.getMachine();

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
