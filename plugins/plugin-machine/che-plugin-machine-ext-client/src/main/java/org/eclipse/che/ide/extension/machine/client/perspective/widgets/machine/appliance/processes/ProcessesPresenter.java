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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.appliance.processes;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.machine.MachineServiceClient;
import org.eclipse.che.api.machine.shared.dto.MachineProcessDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.content.TabPresenter;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * The class defines methods which contains business logic to control machine's processes.
 *
 * @author Dmitry Shnurenko
 */
@Singleton
public class ProcessesPresenter implements TabPresenter, ProcessesView.ActionDelegate {

    private final ProcessesView        view;
    private final MachineServiceClient service;

    @Inject
    public ProcessesPresenter(ProcessesView view, MachineServiceClient service) {
        this.view = view;
        this.view.setDelegate(this);

        this.service = service;
    }

    /**
     * Gets all process for current machine and adds them to special table on view.
     *
     * @param machineId
     *         machine identifier for which need get processes
     */
    public void showProcesses(@NotNull String machineId) {
        Promise<List<MachineProcessDto>> processesPromise = service.getProcesses(machineId);

        processesPromise.then(new Operation<List<MachineProcessDto>>() {
            @Override
            public void apply(List<MachineProcessDto> descriptors) throws OperationException {
                view.setProcesses(descriptors);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }

    /** {@inheritDoc} */
    @Override
    public void onProcessClicked(@NotNull MachineProcessDto descriptor) {
        //TODO need add implementation
    }

    /** {@inheritDoc} */
    @Override
    public IsWidget getView() {
        return view;
    }

    /** {@inheritDoc} */
    @Override
    public void setVisible(boolean visible) {
        view.setVisible(visible);
    }
}
