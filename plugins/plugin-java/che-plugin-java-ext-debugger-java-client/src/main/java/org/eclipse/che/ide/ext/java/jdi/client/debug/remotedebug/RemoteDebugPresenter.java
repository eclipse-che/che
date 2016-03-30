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
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.debug.Debugger;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.jdi.client.JavaRuntimeLocalizationConstant;
import org.eclipse.che.ide.ext.java.jdi.client.debug.JavaDebugger;
import org.eclipse.che.ide.extension.machine.client.inject.factories.EntityFactory;
import org.eclipse.che.ide.extension.machine.client.machine.Machine;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.appliance.server.Server;
import org.eclipse.che.ide.ui.dialogs.ConfirmCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.util.Pair;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.ide.ext.java.jdi.client.debug.JavaDebugger.JavaConnectionProperties.HOST;
import static org.eclipse.che.ide.ext.java.jdi.client.debug.JavaDebugger.JavaConnectionProperties.PORT;

/**
 * Contains methods which allows control of remote debugging.
 *
 * @author Dmitry Shnurenko
 */
public class RemoteDebugPresenter implements RemoteDebugView.ActionDelegate {

    private final RemoteDebugView                 view;
    private final DebuggerManager                 debuggerManager;
    private final AppContext                      appContext;
    private final MachineServiceClient            machineServiceClient;
    private final EntityFactory                   entityFactory;
    private final DtoFactory                      dtoFactory;
    private final DialogFactory                   dialogFactory;
    private final JavaRuntimeLocalizationConstant localizationConstant;

    @Inject
    public RemoteDebugPresenter(RemoteDebugView view,
                                DebuggerManager debuggerManager, AppContext appContext,
                                MachineServiceClient machineServiceClient,
                                EntityFactory entityFactory,
                                DtoFactory dtoFactory,
                                DialogFactory dialogFactory,
                                JavaRuntimeLocalizationConstant localizationConstant) {
        this.view = view;
        this.debuggerManager = debuggerManager;
        this.appContext = appContext;
        this.machineServiceClient = machineServiceClient;
        this.entityFactory = entityFactory;
        this.dtoFactory = dtoFactory;
        this.dialogFactory = dialogFactory;
        this.localizationConstant = localizationConstant;
        this.view.setDelegate(this);
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
            if (server.getPort().endsWith("/tcp")) {
                String portWithoutTcp = server.getPort().substring(0, server.getPort().length() - 4);
                String description = portWithoutTcp + " (" + server.getRef() + ")";
                Pair<String, String> pair = new Pair<>(description, portWithoutTcp);

                ports.add(pair);
            }
        }

        return ports;
    }

    @Override
    public void onConfirmClicked(@NotNull String host, @Min(1) int port) {
        if (debuggerManager.getActiveDebugger() != null) {
            dialogFactory.createMessageDialog(localizationConstant.connectToRemote(),
                                              localizationConstant.debuggerAlreadyConnected(),
                                              new ConfirmCallback() {
                                                  @Override
                                                  public void accepted() {
                                                  }
                                              }).show();
            return;
        }

        final Debugger javaDebugger = debuggerManager.getDebugger(JavaDebugger.LANGUAGE);
        if (javaDebugger != null) {
            debuggerManager.setActiveDebugger(javaDebugger);

            Map<String, String> connectionProperties = new HashMap<>(2);
            connectionProperties.put(HOST.toString(), host);
            connectionProperties.put(PORT.toString(), String.valueOf(port));

            Promise<Void> promise = javaDebugger.attachDebugger(connectionProperties);
            promise.catchError(new Operation<PromiseError>() {
                @Override
                public void apply(PromiseError arg) throws OperationException {
                    debuggerManager.setActiveDebugger(null);
                }
            });
        }
    }
}
