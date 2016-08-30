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
package org.eclipse.che.ide.extension.machine.client.targets.categories.docker;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.dialogs.CancelCallback;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.machine.MachineServiceClient;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.machine.MachineStateEvent;
import org.eclipse.che.ide.extension.machine.client.targets.CategoryPage;
import org.eclipse.che.ide.extension.machine.client.targets.Target;
import org.eclipse.che.ide.extension.machine.client.targets.TargetManager;
import org.eclipse.che.ide.extension.machine.client.targets.TargetsTreeManager;

import static org.eclipse.che.api.core.model.machine.MachineStatus.RUNNING;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

/**
 * Docker type page presenter.
 *
 * @author Oleksii Orel
 */
public class DockerCategoryPresenter implements CategoryPage, TargetManager, DockerView.ActionDelegate {
    private final DockerView                  dockerView;
    private final DialogFactory               dialogFactory;
    private final NotificationManager         notificationManager;
    private final MachineLocalizationConstant machineLocale;
    private final MachineServiceClient        machineService;
    private final EventBus                    eventBus;

    private DockerMachineTarget selectedTarget;
    private TargetsTreeManager  targetsTreeManager;

    @Inject
    public DockerCategoryPresenter(DockerView dockerView,
                                   DialogFactory dialogFactory,
                                   NotificationManager notificationManager,
                                   MachineLocalizationConstant machineLocale,
                                   MachineServiceClient machineService,
                                   EventBus eventBus) {
        this.dockerView = dockerView;
        this.dialogFactory = dialogFactory;
        this.notificationManager = notificationManager;
        this.machineLocale = machineLocale;
        this.machineService = machineService;
        this.eventBus = eventBus;

        dockerView.setDelegate(this);
    }

    @Override
    public void setTargetsTreeManager(TargetsTreeManager targetsTreeManage) {
        this.targetsTreeManager = targetsTreeManage;
    }

    @Override
    public String getCategory() {
        return this.machineLocale.targetsViewCategoryDocker();
    }

    @Override
    public TargetManager getTargetManager() {
        return this;
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(dockerView);
    }

    private MachineDto getMachineByName(String machineName) {
        return this.targetsTreeManager != null ? this.targetsTreeManager.getMachineByName(machineName) : null;
    }

    private void updateTargets(String preselectTargetName) {
        if (this.targetsTreeManager == null) {
            return;
        }
        this.targetsTreeManager.updateTargets(preselectTargetName);
    }

    @Override
    public boolean onRestoreTargetFields(DockerMachineTarget target) {
        if (target == null) {
            return false;
        }

        final MachineDto machine = this.getMachineByName(target.getName());
        if (machine == null) {
            return false;
        }

        target.setOwner(machine.getOwner());
        target.setType(machine.getConfig().getType());
        target.setSourceType(machine.getConfig().getSource().getType());
        target.setSourceContent(machine.getConfig().getSource().getContent());
        target.setSource(machine.getConfig().getSource().getLocation());

        return true;
    }

    @Override
    public void onDeleteClicked(final Target target) {
        dialogFactory.createConfirmDialog(machineLocale.targetsViewDeleteConfirmTitle(),
                                          machineLocale.targetsViewDeleteConfirm(target.getName()),
                                          new ConfirmCallback() {
                                              @Override
                                              public void accepted() {
                                                  destroyTargetMachine(target);
                                              }
                                          }, new CancelCallback() {
                    @Override
                    public void cancelled() {
                        updateTargets(null);
                    }
                }).show();
    }

    private void destroyTargetMachine(final Target target) {
        final MachineDto machine = this.getMachineByName(target.getName());

        if (machine == null || machine.getStatus() != RUNNING) {
            return;
        }

        machineService.destroyMachine(machine.getWorkspaceId(),
                                      machine.getId()).then(new Operation<Void>() {
            @Override
            public void apply(Void arg) throws OperationException {
                eventBus.fireEvent(new MachineStateEvent(machine, MachineStateEvent.MachineAction.DESTROYED));
                notificationManager.notify(machineLocale.targetsViewDisconnectSuccess(target.getName()), SUCCESS, FLOAT_MODE);
                updateTargets(null);
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                notificationManager.notify(machineLocale.targetsViewDisconnectError(target.getName()), FAIL, FLOAT_MODE);
                updateTargets(target.getName());
            }
        });
    }

    @Override
    public void setCurrentSelection(Target selectedTarget) {
        this.selectedTarget = (DockerMachineTarget)selectedTarget;
        dockerView.updateTargetFields(this.selectedTarget);
    }

    @Override
    public DockerMachineTarget createTarget(String name) {
        final DockerMachineTarget target = new DockerMachineTarget();

        target.setName(name);
        target.setCategory(getCategory());
        target.setDirty(false);

        return target;
    }

    @Override
    public DockerMachineTarget createDefaultTarget() {
        return createTarget("new_target");
    }

    @Override
    public void restoreTarget(Target target) {
        this.dockerView.restoreTargetFields((DockerMachineTarget)target);
    }
}
