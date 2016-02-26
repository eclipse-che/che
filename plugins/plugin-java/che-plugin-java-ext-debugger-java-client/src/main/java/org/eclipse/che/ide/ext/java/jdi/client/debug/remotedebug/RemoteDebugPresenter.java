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
package org.eclipse.che.ide.ext.java.jdi.client.debug.remotedebug;

import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;

import org.eclipse.che.api.machine.gwt.client.MachineServiceClient;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.ext.java.jdi.client.debug.DebuggerPresenter;
import org.eclipse.che.ide.extension.machine.client.inject.factories.EntityFactory;
import org.eclipse.che.ide.extension.machine.client.machine.Machine;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.appliance.server.Server;
import org.eclipse.che.ide.util.Pair;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains methods which allows control of remote debugging.
 *
 * @author Dmitry Shnurenko
 */
public class RemoteDebugPresenter implements RemoteDebugView.ActionDelegate {

    private final RemoteDebugView      view;
    private final DebuggerPresenter    debuggerPresenter;
    private final AppContext           appContext;
    private final MachineServiceClient machineServiceClient;
    private final EntityFactory        entityFactory;

    @Inject
    public RemoteDebugPresenter(RemoteDebugView view,
                                DebuggerPresenter debuggerPresenter,
                                AppContext appContext,
                                MachineServiceClient machineServiceClient,
                                EntityFactory entityFactory) {
        this.view = view;
        this.appContext = appContext;
        this.machineServiceClient = machineServiceClient;
        this.entityFactory = entityFactory;
        this.view.setDelegate(this);

        this.debuggerPresenter = debuggerPresenter;
    }

    /** Calls special method on view which shows dialog window. */
    public void showDialog() {
        view.show();
        setPortsList();

        // TODO fix behaviour. Because of animation/render we cannot set focus without delay
        new Timer() {
            @Override
            public void run() {
                view.focus();
            }
        }.schedule(300);
    }

    private void setPortsList() {
        machineServiceClient.getMachine(appContext.getDevMachineId()).then(new Operation<MachineDto>() {
            @Override
            public void apply(MachineDto machineDto) throws OperationException {
                Machine machine = entityFactory.createMachine(machineDto);
                List<Pair<String, String>> ports = extractPortsList(machine);
                view.setPortsList(ports);
            }
        });
    }

    /**
     * Extracts list of ports available to connect by debugger.
     */
    private List<Pair<String, String>> extractPortsList(Machine machine) {
        List<Pair<String, String>> ports = new ArrayList<Pair<String, String>>();
        for (Server server : machine.getServersList()) {
            String description = server.getPort() + " (" + server.getRef() + ")";
            String value = server.getPort();
            Pair<String, String> pair = new Pair<>(description, value);

            ports.add(pair);
        }

        return ports;
    }

    /** {@inheritDoc} */
    @Override
    public void onConfirmClicked(@NotNull String host, @Min(1) int port) {
        debuggerPresenter.attachDebugger(host, port);
    }
}
