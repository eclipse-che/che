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
package org.eclipse.che.ide.extension.machine.client.targets.categories.docker;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

import org.eclipse.che.ide.api.machine.MachineEntity;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.targets.CategoryPage;
import org.eclipse.che.ide.extension.machine.client.targets.Target;
import org.eclipse.che.ide.extension.machine.client.targets.TargetManager;
import org.eclipse.che.ide.extension.machine.client.targets.TargetsTreeManager;

/**
 * Docker type page presenter.
 *
 * @author Oleksii Orel
 */
public class DockerCategoryPresenter implements CategoryPage, TargetManager, DockerView.ActionDelegate {
    private final DockerView                  dockerView;
    private final MachineLocalizationConstant machineLocale;

    private DockerMachineTarget selectedTarget;
    private TargetsTreeManager  targetsTreeManager;

    @Inject
    public DockerCategoryPresenter(DockerView dockerView,
                                   MachineLocalizationConstant machineLocale) {
        this.dockerView = dockerView;
        this.machineLocale = machineLocale;
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

    private MachineEntity getMachineByName(String machineName) {
        return this.targetsTreeManager != null ? this.targetsTreeManager.getMachineByName(machineName) : null;
    }

    @Override
    public boolean onRestoreTargetFields(DockerMachineTarget target) {
        if (target == null) {
            return false;
        }

        final MachineEntity machine = this.getMachineByName(target.getName());
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
        //unsupported operation
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
