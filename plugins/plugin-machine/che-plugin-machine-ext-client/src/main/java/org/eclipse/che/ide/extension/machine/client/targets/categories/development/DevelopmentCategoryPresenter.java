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
package org.eclipse.che.ide.extension.machine.client.targets.categories.development;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.targets.CategoryPage;
import org.eclipse.che.ide.extension.machine.client.targets.Target;
import org.eclipse.che.ide.extension.machine.client.targets.TargetManager;
import org.eclipse.che.ide.extension.machine.client.targets.TargetsTreeManager;

/**
 * Development type page presenter.
 *
 * @author Oleksii Orel
 */
public class DevelopmentCategoryPresenter implements CategoryPage, TargetManager, DevelopmentView.ActionDelegate {
    private final DevelopmentView             developmentView;
    private final MachineLocalizationConstant machineLocale;

    private DevelopmentMachineTarget selectedTarget;
    private TargetsTreeManager       targetsTreeManager;

    @Inject
    public DevelopmentCategoryPresenter(DevelopmentView developmentView,
                                        MachineLocalizationConstant machineLocale) {
        this.developmentView = developmentView;
        this.machineLocale = machineLocale;

        developmentView.setDelegate(this);
    }

    @Override
    public void setTargetsTreeManager(TargetsTreeManager targetsTreeManager) {
        this.targetsTreeManager = targetsTreeManager;
    }

    @Override
    public String getCategory() {
        return this.machineLocale.targetsViewCategoryDevelopment();
    }

    @Override
    public TargetManager getTargetManager() {
        return this;
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(developmentView);
    }

    private Machine getMachineByName(String machineName) {
        if (targetsTreeManager == null) {
            return null;
        }

        return this.targetsTreeManager.getMachineByName(machineName);
    }

    @Override
    public boolean onRestoreTargetFields(DevelopmentMachineTarget target) {
        if (target == null) {
            return false;
        }

        final Machine machine = this.getMachineByName(target.getName());
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
    public void setCurrentSelection(Target selectedTarget) {
        this.selectedTarget = (DevelopmentMachineTarget)selectedTarget;
        developmentView.updateTargetFields(this.selectedTarget);
    }

    @Override
    public DevelopmentMachineTarget createTarget(String name) {
        final DevelopmentMachineTarget target = new DevelopmentMachineTarget();

        target.setName(name);
        target.setCategory(getCategory());
        target.setDirty(false);

        return target;
    }

    @Override
    public DevelopmentMachineTarget createDefaultTarget() {
        return createTarget("new_target");
    }

    @Override
    public void onDeleteClicked(final Target target) {
        //not implemented
    }

    @Override
    public void restoreTarget(Target target) {
        this.developmentView.restoreTargetFields((DevelopmentMachineTarget)target);
    }
}
