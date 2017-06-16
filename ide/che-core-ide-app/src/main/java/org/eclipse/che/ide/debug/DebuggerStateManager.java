/*******************************************************************************
 * Copyright (c) 2017 Rogue Wave Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.debug;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.che.api.debug.shared.dto.DebugSessionStateDto;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.util.storage.LocalStorage;
import org.eclipse.che.ide.util.storage.LocalStorageProvider;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Debug session state manager.
 * 
 * @author Bartlomiej Laczkowski
 */
@Singleton
public class DebuggerStateManager {

    public static final String LOCAL_STORAGE_DEBUGGER_STATES_KEY_PREFIX = "che-debugger-session-states-";

    private final EventBus     eventBus;
    private final LocalStorage localStorage;
    private final DtoFactory   dtoFactory;
    private final String       storageUniqueKey;
    private final String       workspaceId;

    @Inject
    public DebuggerStateManager(AppContext appContext,
                                WorkspaceServiceClient workspaceServiceClient,
                                EventBus eventBus,
                                LocalStorageProvider localStorageProvider,
                                DtoFactory dtoFactory) {
        this.eventBus = eventBus;
        this.localStorage = localStorageProvider.get();
        this.dtoFactory = dtoFactory;
        this.workspaceId = appContext.getWorkspaceId();
        this.storageUniqueKey = LOCAL_STORAGE_DEBUGGER_STATES_KEY_PREFIX + workspaceId;
        cleanup(workspaceServiceClient);
        addHandlers();
    }

    /**
     * Returns debugger state for given debugger type in scope of current workspace.
     * 
     * @param debuggerType
     * @return debugger state
     */
    public DebugSessionStateDto getDebuggerState(String debuggerType) {
        Map<String, DebugSessionStateDto> debugSessionStates = readStateData();
        return debugSessionStates.get(debuggerType);
    }

    /**
     * Sets debugger state for given debugger type in scope of current workspace.
     * 
     * @param debugSessionState
     */
    public void setDebuggerState(DebugSessionStateDto debugSessionState) {
        Map<String, DebugSessionStateDto> debugSessionStates = readStateData();
        debugSessionStates.put(debugSessionState.getDebuggerType(), debugSessionState);
        writeStateData(debugSessionStates);
    }

    /**
     * Removes debugger state for given debugger type in scope of current workspace.
     * 
     * @param debuggerType
     */
    public void removeDebuggerState(String debuggerType) {
        Map<String, DebugSessionStateDto> debugSessionStates = readStateData();
        debugSessionStates.remove(debuggerType);
        writeStateData(debugSessionStates);
    }

    private void addHandlers() {
        eventBus.addHandler(WorkspaceStoppedEvent.TYPE, new WorkspaceStoppedEvent.Handler() {
            @Override
            public void onWorkspaceStopped(WorkspaceStoppedEvent event) {
                if (workspaceId.equals(event.getWorkspace().getId())) {
                    // Wipe out storage data key on workspace stop
                    writeStateData(null);
                }
            }
        });
    }

    private Map<String, DebugSessionStateDto> readStateData() {
        if (localStorage == null) {
            return new HashMap<>();
        }
        String data = localStorage.getItem(storageUniqueKey);
        if (data != null && !data.isEmpty()) {
            List<DebugSessionStateDto> dssList = dtoFactory.createListDtoFromJson(data, DebugSessionStateDto.class);
            return dssList.stream().collect(Collectors.toMap(DebugSessionStateDto::getDebuggerType, dss -> dss));
        }
        return new HashMap<>();
    };

    private void writeStateData(Map<String, DebugSessionStateDto> debugSessionStates) {
        if (localStorage == null) {
            return;
        }
        if (debugSessionStates == null || debugSessionStates.isEmpty()) {
            localStorage.removeItem(storageUniqueKey);
        } else {
            localStorage.setItem(storageUniqueKey,
                                 dtoFactory.toJson(debugSessionStates.values().stream().collect(Collectors.toList())));
        }
    }

    private void cleanup(WorkspaceServiceClient workspaceServiceClient) {
        if (localStorage == null) {
            return;
        }
        for (int i = 0; i < localStorage.getLength(); i++) {
            String key = localStorage.key(i);
            if (key != null && key.startsWith(LOCAL_STORAGE_DEBUGGER_STATES_KEY_PREFIX)) {
                String workspaceId = key.substring(LOCAL_STORAGE_DEBUGGER_STATES_KEY_PREFIX.length());
                Promise<WorkspaceDto> workspace = workspaceServiceClient.getWorkspace(workspaceId);
                workspace.catchError(error -> {
                    localStorage.removeItem(key);
                });
            }
        }
    }

}
