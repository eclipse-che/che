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
package org.eclipse.che.ide.extension.machine.client.targets;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.machine.shared.dto.recipe.RecipeDescriptor;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceRuntimeDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.MachineEntity;
import org.eclipse.che.ide.api.machine.RecipeServiceClient;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.inject.factories.EntityFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.eclipse.che.api.core.model.machine.MachineStatus.RUNNING;

/**
 * Targets manager presenter.
 *
 * @author Vitaliy Guliy
 * @author Oleksii Orel
 */
public class TargetsPresenter implements TargetsTreeManager, TargetsView.ActionDelegate {

    private final TargetsView                 view;
    private final EntityFactory               entityFactory;
    private final WorkspaceServiceClient      workspaceServiceClient;
    private final RecipeServiceClient         recipeServiceClient;
    private final MachineLocalizationConstant machineLocale;
    private final AppContext                  appContext;
    private final CategoryPageRegistry        categoryPageRegistry;

    private final Map<String, Target>        targets  = new HashMap<>();
    private final Map<String, MachineEntity> machines = new HashMap<>();

    private Target selectedTarget;

    @Inject
    public TargetsPresenter(TargetsView view,
                            EntityFactory entityFactory,
                            WorkspaceServiceClient workspaceServiceClient,
                            RecipeServiceClient recipeServiceClient,
                            MachineLocalizationConstant machineLocale,
                            AppContext appContext,
                            CategoryPageRegistry categoryPageRegistry) {
        this.view = view;
        this.entityFactory = entityFactory;
        this.workspaceServiceClient = workspaceServiceClient;
        this.recipeServiceClient = recipeServiceClient;
        this.machineLocale = machineLocale;
        this.appContext = appContext;
        this.categoryPageRegistry = categoryPageRegistry;

        view.setDelegate(this);
    }

    /**
     * Opens Targets popup.
     */
    public void edit() {
        view.show();
        view.clear();

        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                updateTargets(null);
            }
        });
    }

    /**
     * Fetches all recipes from the server, makes a list of targets and selects specified target.
     */
    @Override
    public void updateTargets(final String preselectTargetName) {
        targets.clear();
        machines.clear();

        getMachines(appContext.getWorkspaceId()).then(new Operation<List<MachineEntity>>() {
            @Override
            public void apply(List<MachineEntity> machineList) throws OperationException {
                //create Target objects from all machines
                for (MachineEntity machine : machineList) {
                    final MachineConfig machineConfig = machine.getConfig();
                    machines.put(machineConfig.getName(), machine);
                    final String targetCategory = machineConfig.isDev() ? machineLocale.devMachineCategory() : machineConfig.getType();
                    final Target target = createTarget(machineConfig.getName(), targetCategory);
                    target.setConnected(isMachineRunning(machine));
                    targets.put(target.getName(), target);
                }
                //create Target objects from recipe with ssh type
                recipeServiceClient.getAllRecipes().then(new Operation<List<RecipeDescriptor>>() {
                    @Override
                    public void apply(List<RecipeDescriptor> recipeList) throws OperationException {
                        for (RecipeDescriptor recipe : recipeList) {
                            //only for SSH recipes
                            if (!machineLocale.targetsViewCategorySsh().equalsIgnoreCase(recipe.getType())) {
                                continue;
                            }
                            Target target = targets.get(recipe.getName());
                            if (target == null) {
                                target = createTarget(recipe.getName(), recipe.getType());
                            }
                            target.setRecipe(recipe);
                            categoryPageRegistry.getCategoryPage(target.getCategory()).getTargetManager().restoreTarget(target);
                            targets.put(target.getName(), target);
                        }
                        view.showTargets(new ArrayList<>(targets.values()));
                        selectTarget(preselectTargetName == null ? selectedTarget : targets.get(preselectTargetName));
                    }
                });
            }
        });

    }

    public Promise<List<MachineEntity>> getMachines(String workspaceId) {
        return workspaceServiceClient.getWorkspace(workspaceId).then(new Function<WorkspaceDto, List<MachineEntity>>() {
            @Override
            public List<MachineEntity> apply(WorkspaceDto workspace) throws FunctionException {
                WorkspaceRuntimeDto workspaceRuntime = workspace.getRuntime();
                if (workspaceRuntime == null) {
                    return emptyList();
                }

                List<MachineDto> runtimeMachines = workspaceRuntime.getMachines();
                List<MachineEntity> machines = new ArrayList<>(runtimeMachines.size());
                for (MachineDto machineDto : runtimeMachines) {
                    MachineEntity machineEntity = entityFactory.createMachine(machineDto);
                    machines.add(machineEntity);
                }
                return machines;
            }
        });
    }

    private Target createTarget(String targetName, String targetCategory) {
        final CategoryPage page = categoryPageRegistry.getCategoryPage(targetCategory);

        final Target target;
        if (page != null) {
            page.setTargetsTreeManager(this);
            target = page.getTargetManager().createTarget(targetName);
            page.getTargetManager().restoreTarget(target);
        } else {//set default
            target = new BaseTarget();
            target.setName(targetName);
            target.setCategory(targetCategory);
        }

        return target;
    }

    /**
     * Determines whether machine is running or not.
     *
     * @return true for running machine
     */
    private boolean isMachineRunning(Machine machine) {
        return machine != null && machine.getStatus() == RUNNING;
    }

    private void selectTarget(Target target) {
        this.selectedTarget = target;
        if (target == null || !view.selectTarget(target)) {
            view.showHintPanel();
        }
    }

    @Override
    public void onCloseClicked() {
        view.hide();
    }

    @Override
    public void onTargetSelected(final Target target) {
        selectedTarget = target;
        if (target == null) {
            view.showHintPanel();
            return;
        }
        final CategoryPage page = categoryPageRegistry.getCategoryPage(target.getCategory());
        if (page == null) {
            view.showHintPanel();
            return;
        }
        page.go(new AcceptsOneWidget() {
            @Override
            public void setWidget(IsWidget widget) {
                view.setPropertiesPanel(widget.asWidget());
                page.setCurrentSelection(target);
            }
        });
    }

    @Override
    public void onAddTarget(String type) {
        final CategoryPage page = categoryPageRegistry.getCategoryPage(type);
        if (page == null) {
            return;
        }

        page.setTargetsTreeManager(this);
        Target defaultTarget = page.getTargetManager().createDefaultTarget();
        targets.put(defaultTarget.getName(), defaultTarget);
        view.showTargets(new ArrayList<>(targets.values()));
        selectTarget(defaultTarget);
    }

    @Override
    public void onDeleteTarget(Target target) {
        final String type = target.getCategory();
        final CategoryPage page = categoryPageRegistry.getCategoryPage(type);
        if (page == null) {
            return;
        }

        page.getTargetManager().onDeleteClicked(target);
    }

    @Override
    public boolean isTargetNameExist(String targetName) {
        for (Target target : targets.values()) {
            if (!target.equals(selectedTarget) && target.getName().equals(targetName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public MachineEntity getMachineByName(String machineName) {
        return machines.get(machineName);
    }
}
