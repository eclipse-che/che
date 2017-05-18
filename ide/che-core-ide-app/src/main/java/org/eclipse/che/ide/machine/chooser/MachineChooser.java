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
package org.eclipse.che.ide.machine.chooser;

import com.google.inject.Inject;

import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.api.promises.client.js.Executor;
import org.eclipse.che.api.promises.client.js.Executor.ExecutorBody;
import org.eclipse.che.api.promises.client.js.JsPromiseError;
import org.eclipse.che.api.promises.client.js.RejectFunction;
import org.eclipse.che.api.promises.client.js.ResolveFunction;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.ActiveRuntime;
import org.eclipse.che.ide.api.machine.MachineEntity;

import java.util.List;

/**
 * Provides a simple mechanism for the user to choose a {@link Machine}.
 *
 * @author Artem Zatsarynnyi
 */
public class MachineChooser implements MachineChooserView.ActionDelegate {

    private final MachineChooserView view;
    private final AppContext         appContext;
    private final PromiseProvider    promiseProvider;

    private ResolveFunction<MachineEntity> resolveFunction;
    private RejectFunction                 rejectFunction;

    @Inject
    public MachineChooser(MachineChooserView view,
                          AppContext appContext,
                          PromiseProvider promiseProvider) {
        this.view = view;
        this.appContext = appContext;
        this.promiseProvider = promiseProvider;

        view.setDelegate(this);
    }

    /**
     * Pops up a dialog for choosing a machine.
     * <p><b>Note:</b> if there is only one machine running in the workspace
     * then returned promise will be resolved with that machine without asking user.
     *
     * @return promise that will be resolved with a chosen {@link MachineEntity}
     * or rejected in case machine selection has been cancelled.
     */
    public Promise<MachineEntity> show() {
        final ActiveRuntime runtime = appContext.getActiveRuntime();

        if (runtime != null) {
            final List<MachineEntity> machines = runtime.getMachines();

            if (machines.size() == 1) {
                return promiseProvider.resolve(machines.get(0));
            }

            view.setMachines(machines);
        }

        view.show();

        return promiseProvider.create(Executor.create((ExecutorBody<MachineEntity>)(resolve, reject) -> {
            resolveFunction = resolve;
            rejectFunction = reject;
        }));
    }

    @Override
    public void onMachineSelected(MachineEntity machine) {
        view.close();

        resolveFunction.apply(machine);
    }

    @Override
    public void onCanceled() {
        rejectFunction.apply(JsPromiseError.create("Machine selection has been canceled"));
    }
}
