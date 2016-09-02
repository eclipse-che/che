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
package org.eclipse.che.ide.extension.machine.client.targets.categories.ssh;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.machine.shared.dto.LimitsDto;
import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.machine.shared.dto.MachineSourceDto;
import org.eclipse.che.api.machine.shared.dto.event.MachineStatusEvent;
import org.eclipse.che.api.machine.shared.dto.recipe.NewRecipe;
import org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor;
import org.eclipse.che.api.machine.shared.dto.recipe.RecipeUpdate;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.dialogs.CancelCallback;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.machine.MachineServiceClient;
import org.eclipse.che.ide.api.machine.RecipeServiceClient;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;
import org.eclipse.che.ide.api.workspace.event.MachineStatusChangedEvent;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.machine.MachineStateEvent;
import org.eclipse.che.ide.extension.machine.client.targets.CategoryPage;
import org.eclipse.che.ide.extension.machine.client.targets.Target;
import org.eclipse.che.ide.extension.machine.client.targets.TargetManager;
import org.eclipse.che.ide.extension.machine.client.targets.TargetsTreeManager;

import java.util.ArrayList;
import java.util.List;

import static org.eclipse.che.api.core.model.machine.MachineStatus.RUNNING;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;

/**
 * SSH type page presenter.
 *
 * @author Oleksii Orel
 * @author Vitaliy Guliy
 */
public class SshCategoryPresenter implements CategoryPage, TargetManager, SshView.ActionDelegate, MachineStatusChangedEvent.Handler {

    private final SshView                     sshView;
    private final RecipeServiceClient         recipeServiceClient;
    private final DtoFactory                  dtoFactory;
    private final DialogFactory               dialogFactory;
    private final NotificationManager         notificationManager;
    private final MachineLocalizationConstant machineLocale;
    private final AppContext                  appContext;
    private final MachineServiceClient        machineService;
    private final EventBus                    eventBus;
    private final WorkspaceServiceClient      workspaceServiceClient;

    private TargetsTreeManager targetsTreeManager;
    private SshMachineTarget   selectedTarget;

    /* Notification informing connecting to the target is in progress */
    private StatusNotification connectNotification;

    /* Name currently connecting target  */
    private String connectTargetName;

    @Inject
    public SshCategoryPresenter(SshView sshView,
                                RecipeServiceClient recipeServiceClient,
                                DtoFactory dtoFactory,
                                DialogFactory dialogFactory,
                                NotificationManager notificationManager,
                                MachineLocalizationConstant machineLocale,
                                WorkspaceServiceClient workspaceServiceClient,
                                AppContext appContext,
                                MachineServiceClient machineService,
                                EventBus eventBus) {
        this.sshView = sshView;
        this.recipeServiceClient = recipeServiceClient;
        this.dtoFactory = dtoFactory;
        this.dialogFactory = dialogFactory;
        this.notificationManager = notificationManager;
        this.workspaceServiceClient = workspaceServiceClient;
        this.machineLocale = machineLocale;
        this.appContext = appContext;
        this.machineService = machineService;
        this.eventBus = eventBus;

        sshView.setDelegate(this);

        eventBus.addHandler(MachineStatusChangedEvent.TYPE, this);
    }

    @Override
    public void setTargetsTreeManager(TargetsTreeManager targetsTreeManager) {
        this.targetsTreeManager = targetsTreeManager;
    }

    @Override
    public String getCategory() {
        return this.machineLocale.targetsViewCategorySsh();
    }

    @Override
    public TargetManager getTargetManager() {
        return this;
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(sshView);
    }

    private boolean isTargetNameExist(String targetName) {
        if(this.targetsTreeManager == null || targetName == null) {
            return false;
        }
        if(selectedTarget != null && targetName.equals(selectedTarget.getName())){
            RecipeDescriptor recipe = selectedTarget.getRecipe();
            if(recipe != null && recipe.getName().equals(targetName)) {
                return true;
            }
        }

        return this.targetsTreeManager.isTargetNameExist(targetName);
    }

    private MachineDto getMachineByName(String machineName) {
        if (this.targetsTreeManager == null) {
            return null;
        }
        return this.targetsTreeManager.getMachineByName(machineName);
    }

    private void updateTargets(String preselectTargetName) {
        if (this.targetsTreeManager == null) {
            return;
        }
        this.targetsTreeManager.updateTargets(preselectTargetName);
    }

    @Override
    public void onTargetNameChanged(String value) {
        if (selectedTarget == null || selectedTarget.getName().equals(value)) {
            return;
        }

        selectedTarget.setName(value);
        selectedTarget.setDirty(true);

        updateButtons(true);
    }

    @Override
    public void onHostChanged(String value) {
        if (selectedTarget == null || selectedTarget.getHost().equals(value)) {
            return;
        }

        selectedTarget.setHost(value);
        selectedTarget.setDirty(true);

        updateButtons(false);
    }

    @Override
    public void onPortChanged(String value) {
        if (selectedTarget == null || selectedTarget.getPort().equals(value)) {
            return;
        }

        selectedTarget.setPort(value);
        selectedTarget.setDirty(true);

        updateButtons(false);
    }

    @Override
    public void onUserNameChanged(String value) {
        if (selectedTarget == null || selectedTarget.getUserName().equals(value)) {
            return;
        }

        selectedTarget.setUserName(value);
        selectedTarget.setDirty(true);

        updateButtons(false);
    }

    @Override
    public void onPasswordChanged(String value) {
        if (selectedTarget == null || selectedTarget.getPassword().equals(value)) {
            return;
        }

        selectedTarget.setPassword(value);
        selectedTarget.setDirty(true);

        updateButtons(false);
    }

    /**
     * Updates buttons state.
     */
    public void updateButtons(boolean verifyTargetName) {
        if (selectedTarget == null) {
            return;
        }

        // Update text of Connect / Disconnect button
        if (selectedTarget.isConnected()) {
            sshView.setConnectButtonText("Disconnect");
        } else {
            sshView.setConnectButtonText("Connect");
        }

        // Disable Save and Cancel buttons and enable Connect for non dirty target.
        if (!selectedTarget.isDirty()) {
            sshView.enableConnectButton(true);
            sshView.enableCancelButton(false);
            sshView.enableSaveButton(false);

            sshView.unmarkTargetName();
            sshView.unmarkHost();
            sshView.unmarkPort();
            return;
        }

        sshView.enableConnectButton(false);
        sshView.enableCancelButton(true);

        // target name must be not empty
        if (sshView.getTargetName().isEmpty()) {
            sshView.markTargetNameInvalid();
            sshView.enableSaveButton(false);
            return;
        }

        boolean enableSave = true;

        // check target name to being not empty
        if (sshView.getTargetName().isEmpty()) {
            enableSave = false;
            sshView.markTargetNameInvalid();
        } else {
            if (verifyTargetName) {
                if (isTargetNameExist(sshView.getTargetName())) {
                    enableSave = false;
                    sshView.markTargetNameInvalid();
                } else {
                    sshView.unmarkTargetName();
                }
            }
        }

        // check host to being not empty
        if (sshView.getHost().isEmpty()) {
            enableSave = false;
            sshView.markHostInvalid();
        } else {
            sshView.unmarkHost();
        }

        // check port to being not empty
        if (sshView.getPort().isEmpty()) {
            enableSave = false;
            sshView.markPortInvalid();
        } else {
            sshView.unmarkPort();
        }

        sshView.enableSaveButton(enableSave);
    }

    @Override
    public void onSaveClicked() {
        if (selectedTarget.getRecipe() == null) {
            createTargetRecipe();
        } else {
            updateTargetRecipe();
        }
    }

    /**
     * Create a new target recipe and save it.
     */
    private void createTargetRecipe() {
        List<String> tags = new ArrayList<>();
        tags.add(this.getCategory());

        NewRecipe newRecipe = dtoFactory.createDto(NewRecipe.class)
                                        .withName(selectedTarget.getName())
                                        .withType(getCategory())
                                        .withScript("{" +
                                                    "\"host\": \"" + selectedTarget.getHost() + "\", " +
                                                    "\"port\": \"" + selectedTarget.getPort() + "\", " +
                                                    "\"username\": \"" + selectedTarget.getUserName() + "\", " +
                                                    "\"password\": \"" + selectedTarget.getPassword() + "\"" +
                                                    "}")
                                        .withTags(tags);

        Promise<RecipeDescriptor> createRecipe = recipeServiceClient.createRecipe(newRecipe);
        createRecipe.then(new Operation<RecipeDescriptor>() {
            @Override
            public void apply(RecipeDescriptor recipe) throws OperationException {
                onTargetSaved(recipe);
            }
        });

        createRecipe.catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                dialogFactory.createMessageDialog("Error", machineLocale.targetsViewSaveError(), null).show();
            }
        });
    }

    /**
     * Updates as existent target recipe and save it.
     */
    private void updateTargetRecipe() {
        RecipeUpdate recipeUpdate = dtoFactory.createDto(RecipeUpdate.class)
                                              .withId(selectedTarget.getRecipe().getId())
                                              .withName(sshView.getTargetName())
                                              .withType(selectedTarget.getRecipe().getType())
                                              .withTags(selectedTarget.getRecipe().getTags())
                                              .withDescription(selectedTarget.getRecipe().getDescription())
                                              .withScript("{" +
                                                      "\"host\": \"" + selectedTarget.getHost() + "\", " +
                                                      "\"port\": \"" + selectedTarget.getPort() + "\", " +
                                                      "\"username\": \"" + selectedTarget.getUserName() + "\", " +
                                                      "\"password\": \"" + selectedTarget.getPassword() + "\"" +
                                                      "}");

        Promise<RecipeDescriptor> updateRecipe = recipeServiceClient.updateRecipe(recipeUpdate);
        updateRecipe.then(new Operation<RecipeDescriptor>() {
            @Override
            public void apply(RecipeDescriptor recipe) throws OperationException {
                onTargetSaved(recipe);
            }
        });

        updateRecipe.catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                dialogFactory.createMessageDialog("Error", machineLocale.targetsViewSaveError(), null).show();
            }
        });
    }

    /**
     * Performs actions when target is saved.
     */
    private void onTargetSaved(RecipeDescriptor recipe) {
        selectedTarget.setRecipe(recipe);
        selectedTarget.setDirty(false);

        this.updateTargets(recipe.getName());

        notificationManager.notify(machineLocale.targetsViewSaveSuccess(), SUCCESS, FLOAT_MODE);
    }

    @Override
    public void onCancelClicked() {
        if (selectedTarget == null) {
            return;
        }

        selectedTarget.setDirty(false);
        restoreTarget(selectedTarget);
        sshView.updateTargetFields(selectedTarget);
        updateButtons(false);
    }

    @Override
    public void onConnectClicked() {
        if (selectedTarget == null) {
            return;
        }

        if (selectedTarget.isConnected()) {
            final MachineDto machine = this.getMachineByName(selectedTarget.getName());
            disconnect(machine);
            return;
        }

        if (selectedTarget.getRecipe() == null) {
            this.sshView.enableConnectButton(false);
            return;
        }

        connect();
    }

    /**
     * Opens a connection to the selected target.
     * Starts a machine based on the selected recipe.
     */
    private void connect() {
        sshView.setConnectButtonText(null);

        connectTargetName = selectedTarget.getName();
        connectNotification =
                notificationManager.notify(machineLocale.targetsViewConnectProgress(selectedTarget.getName()), PROGRESS, FLOAT_MODE);

        String recipeURL = selectedTarget.getRecipe().getLink("get recipe script").getHref();

        LimitsDto limitsDto = dtoFactory.createDto(LimitsDto.class).withRam(1024);
        MachineSourceDto sourceDto = dtoFactory.createDto(MachineSourceDto.class).withType("ssh-config").withLocation(recipeURL);

        MachineConfigDto configDto = dtoFactory.createDto(MachineConfigDto.class)
                                               .withDev(false)
                                               .withName(selectedTarget.getName())
                                               .withSource(sourceDto)
                                               .withLimits(limitsDto)
                                               .withType(getCategory());

        Promise<Void> machinePromise = workspaceServiceClient.createMachine(appContext.getWorkspaceId(), configDto);

        machinePromise.then(new Operation<Void>() {
            @Override
            public void apply(Void arg) throws OperationException {
            }
        });

        machinePromise.catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError promiseError) throws OperationException {
                onConnectingFailed(null);
            }
        });
    }

    /**
     * Destroys the machine.
     *
     * @param machine
     *         machine to destroy
     */
    private void disconnect(final MachineDto machine) {
        if (machine == null || machine.getStatus() != RUNNING) {
            updateTargets(null);
            return;
        }
        sshView.setConnectButtonText(null);

        machineService.destroyMachine(machine.getWorkspaceId(),
                                      machine.getId()).then(new Operation<Void>() {
            @Override
            public void apply(Void arg) throws OperationException {
                eventBus.fireEvent(new MachineStateEvent(machine, MachineStateEvent.MachineAction.DESTROYED));

                notificationManager.notify(machineLocale.targetsViewDisconnectSuccess(selectedTarget.getName()), SUCCESS, FLOAT_MODE);
                updateTargets(null);
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                notificationManager.notify(machineLocale.targetsViewDisconnectError(selectedTarget.getName()), FAIL, FLOAT_MODE);
                updateTargets(null);
            }
        });
    }

    @Override
    public void onDeleteClicked(final Target target) {
        dialogFactory.createConfirmDialog(machineLocale.targetsViewDeleteConfirmTitle(),
                machineLocale.targetsViewDeleteConfirm(target.getName()),
                new ConfirmCallback() {
                    @Override
                    public void accepted() {
                        deleteTarget(target);
                    }
                }, new CancelCallback() {
                    @Override
                    public void cancelled() {
                        updateTargets(null);
                    }
                }).show();
    }

    private void deleteTarget(final Target target) {
        final MachineDto machine = this.getMachineByName(target.getName());

        if (machine == null || machine.getStatus() != RUNNING) {
            deleteTargetRecipe(target);
            return;
        }

        if (target.getRecipe() == null) {
            disconnect(machine);
            return;
        }

        machineService.destroyMachine(machine.getWorkspaceId(),
                                      machine.getId()).then(new Operation<Void>() {
            @Override
            public void apply(Void arg) throws OperationException {
                eventBus.fireEvent(new MachineStateEvent(machine, MachineStateEvent.MachineAction.DESTROYED));
                notificationManager.notify(machineLocale.targetsViewDisconnectSuccess(target.getName()), SUCCESS, FLOAT_MODE);
                deleteTargetRecipe(target);
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                notificationManager.notify(machineLocale.targetsViewDisconnectError(target.getName()), FAIL, FLOAT_MODE);
                updateTargets(target.getName());
            }
        });
    }

    /**
     * Deletes specified  target.
     *
     * @param target
     *         target to delete
     */
    private void deleteTargetRecipe(final Target target) {
        Promise<Void> voidPromise = recipeServiceClient.removeRecipe(target.getRecipe().getId());
        voidPromise.then(new Operation<Void>() {
            @Override
            public void apply(Void arg) throws OperationException {
                notificationManager.notify(machineLocale.targetsRecipeDeleteSuccess(target.getName()), SUCCESS, FLOAT_MODE);
                if (target.isConnected()) {
                    updateTargets(null);
                    return;
                }
                final MachineDto machine = getMachineByName(target.getName());
                disconnect(machine);
            }
        });

        voidPromise.catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                dialogFactory.createMessageDialog("Error", machineLocale.targetsViewDeleteError(target.getName()), null).show();
            }
        });
    }

    /**
     * Ensures machine is started.
     */
    private void onConnected(final String workspaceId, final String machineId) {
        // There is a little bug in machine service on the server side.
        // The machine info is updated with a little delay after running a machine.
        // Using timer must fix the problem.
        new Timer() {
            @Override
            public void run() {
                machineService.getMachine(workspaceId, machineId).then(new Operation<MachineDto>() {
                    @Override
                    public void apply(MachineDto machineDto) throws OperationException {
                        if (machineDto.getStatus() == RUNNING) {
                            connectNotification.setTitle(machineLocale.targetsViewConnectSuccess(machineDto.getConfig().getName()));
                            connectNotification.setStatus(StatusNotification.Status.SUCCESS);
                            updateTargets(machineDto.getConfig().getName());
                        } else {
                            onConnectingFailed(null);
                        }
                    }
                }).catchError(new Operation<PromiseError>() {
                    @Override
                    public void apply(PromiseError arg) throws OperationException {
                        onConnectingFailed(null);
                    }
                });
            }
        }.schedule(500);
    }

    /**
     * Handles connecting error and displays an error message.
     *
     * @param reason
     *         a reason to be attached to the error message
     */
    private void onConnectingFailed(String reason) {
        connectNotification.setTitle(machineLocale.targetsViewConnectError(selectedTarget.getName()));
        if (reason != null) {
            connectNotification.setContent(reason);
        }

        connectNotification.setStatus(StatusNotification.Status.FAIL);

        updateButtons(false);
    }

    @Override
    public void setCurrentSelection(Target selectedTarget) {
        this.selectedTarget = (SshMachineTarget)selectedTarget;
        sshView.updateTargetFields(this.selectedTarget);
        updateButtons(false);
    }

    @Override
    public SshMachineTarget createTarget(String name) {
        final SshMachineTarget target = new SshMachineTarget();

        target.setName(name);
        target.setCategory(this.getCategory());
        target.setDirty(false);

        return target;
    }

    @Override
    public SshMachineTarget createDefaultTarget() {
        final SshMachineTarget target = createTarget("new_target");

        target.setHost("");
        target.setPort("22");
        target.setDirty(true);
        target.setUserName("root");
        target.setPassword("");
        target.setConnected(false);

        return target;
    }

    @Override
    public void restoreTarget(Target target) {
        this.sshView.restoreTargetFields((SshMachineTarget) target);
    }

    @Override
    public void onMachineStatusChanged(MachineStatusChangedEvent event) {
        if (MachineStatusEvent.EventType.RUNNING == event.getEventType()
                && connectNotification != null && connectTargetName != null
                && connectTargetName.equals(event.getMachineName())) {
            onConnected(event.getWorkspaceId(), event.getMachineId());
            return;
        }

        if (MachineStatusEvent.EventType.ERROR == event.getEventType()
                && connectNotification != null && connectTargetName != null
                && connectTargetName.equals(event.getMachineName())) {
            onConnectingFailed(event.getErrorMessage());
            return;
        }
    }

}
