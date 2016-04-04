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
package org.eclipse.che.ide.client;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.api.machine.gwt.client.MachineManager;
import org.eclipse.che.api.machine.gwt.client.MachineServiceClient;
import org.eclipse.che.api.machine.shared.Constants;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.machine.shared.dto.ServerDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.context.AppContextImpl;
import org.eclipse.che.ide.util.loging.Log;

import javax.inject.Named;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.api.core.model.machine.MachineStatus.CREATING;
import static org.eclipse.che.api.core.model.machine.MachineStatus.RUNNING;

/**
 * @author Artem Zatsarynnyi
 * @author Dmitry Shnurenko
 */
@Singleton
public class DevMachineLauncher {

    private final MachineServiceClient machineServiceClient;
    private final AppContextImpl       appContext;
    private final MachineManager       machineManager;
    private final String               wsAgentPath;

    @Inject
    public DevMachineLauncher(@Named("ws.agent.path") String wsAgentPath,
                              AppContextImpl appContext,
                              MachineManager machineManager,
                              MachineServiceClient machineServiceClient) {
        this.wsAgentPath = wsAgentPath;
        this.machineServiceClient = machineServiceClient;
        this.appContext = appContext;
        this.machineManager = machineManager;
    }

    public void startDevMachine(final MachineStartedCallback startedCallback) {
        machineServiceClient.getMachines(appContext.getWorkspaceId()).then(new Operation<List<MachineDto>>() {
            @Override
            public void apply(List<MachineDto> machines) throws OperationException {
                if (machines.isEmpty()) {
                    return;
                }

                for (MachineDto machine : machines) {
                    boolean isDev = machine.getConfig().isDev();
                    MachineStatus status = machine.getStatus();

                    if (isDev && status == RUNNING) {
                        appContext.setWsAgentURL(getWsAgentUrl(machine));
                        appContext.setDevMachineId(machine.getId());

                        machineManager.onMachineRunning(machine.getId());

                        startedCallback.onStarted();
                        break;
                    }
                    if (isDev && status == CREATING) {
                        break;
                    }
                }
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                Log.error(getClass(), error.getMessage());
            }
        });
    }

    private String getWsAgentUrl(MachineDto devMachine) {
        Map<String, ServerDto> servers = devMachine.getRuntime().getServers();

        ServerDto serverDto = servers.get(Constants.WS_AGENT_PORT);

        String url = serverDto.getUrl();

        if (Strings.isNullOrEmpty(url)) {
            String errorMessage = "Ws agent url can not be null";
            Log.error(getClass(), errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        return url + wsAgentPath;
    }

    interface MachineStartedCallback {
        /** The method is called when dev machine started */
        void onStarted();
    }
}
