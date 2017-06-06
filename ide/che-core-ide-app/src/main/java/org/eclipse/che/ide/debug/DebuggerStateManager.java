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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.che.api.debug.shared.dto.DebugSessionStateDto;
import org.eclipse.che.api.debug.shared.model.DebugSessionState;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.util.storage.LocalStorage;
import org.eclipse.che.ide.util.storage.LocalStorageProvider;

import com.google.gwt.storage.client.Storage;
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

    public static final String                      LOCAL_STORAGE_DEBUGGER_STATES_KEY_PREFIX =
                                                                                             "che-debugger-session-states-";
    private static final List<DebugSessionStateDto> EMPTY_LIST                               = new ArrayList<>();

    private final EventBus                          eventBus;
    private final LocalStorage                      localStorage;
    private final DtoFactory                        dtoFactory;
    private final String                            storageUniqueKey;
    private final String                            workspaceId;

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
        List<DebugSessionStateDto> debugSessionStates = readStateData();
        return findStoredState(debugSessionStates, debuggerType);
    }

    /**
     * Sets debugger state for given debugger type in scope of current workspace.
     * 
     * @param debugSessionState
     */
    public void setDebuggerState(DebugSessionStateDto debugSessionState) {
        List<DebugSessionStateDto> debugSessionStates = readStateData();
        DebugSessionState matchingSessionState =
                                               findStoredState(debugSessionStates, debugSessionState.getDebuggerType());
        debugSessionStates.remove(matchingSessionState);
        debugSessionStates.add(debugSessionState);
        writeStateData(debugSessionStates);
    }

    /**
     * Removes debugger state for given debugger type in scope of current workspace.
     * 
     * @param debuggerType
     */
    public void removeDebuggerState(String debuggerType) {
        List<DebugSessionStateDto> debugSessionStates = readStateData();
        DebugSessionState matchingSessionState = findStoredState(debugSessionStates, debuggerType);
        debugSessionStates.remove(matchingSessionState);
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

    private List<DebugSessionStateDto> readStateData() {
        if (localStorage == null) {
            return EMPTY_LIST;
        }
        String data = localStorage.getItem(storageUniqueKey);
        if (data != null && !data.isEmpty()) {
            return dtoFactory.createListDtoFromJson(data, DebugSessionStateDto.class);
        }
        return EMPTY_LIST;
    };

    private void writeStateData(List<DebugSessionStateDto> debugSessionStates) {
        if (localStorage == null) {
            return;
        }
        if (debugSessionStates == null || debugSessionStates.isEmpty()) {
            localStorage.removeItem(storageUniqueKey);
        } else {
            localStorage.setItem(storageUniqueKey, dtoFactory.toJson(debugSessionStates));
        }
    }

    private DebugSessionStateDto findStoredState(List<DebugSessionStateDto> debugSessionStates, String debuggerType) {
        for (DebugSessionStateDto storedSessionState : debugSessionStates) {
            if (debuggerType.equals(storedSessionState.getDebuggerType())) {
                return storedSessionState;
            }
        }
        return null;
    }

    private void cleanup(WorkspaceServiceClient workspaceServiceClient) {
        final Storage localStorage = Storage.getLocalStorageIfSupported();
        if (localStorage == null) {
            return;
        }
        final List<String> workspaceIds = new ArrayList<>();
        int storageKeysSize = localStorage.getLength();
        for (int i = 0; i < storageKeysSize; i++) {
            String storageKey = localStorage.key(i);
            if (storageKey.startsWith(LOCAL_STORAGE_DEBUGGER_STATES_KEY_PREFIX)) {
                workspaceIds.add(storageKey.substring(LOCAL_STORAGE_DEBUGGER_STATES_KEY_PREFIX.length()));
            }
        }
        workspaceServiceClient.getWorkspaces(0, 100).then(new Operation<List<WorkspaceDto>>() {
            @Override
            public void apply(List<WorkspaceDto> workspaceDtos) throws OperationException {
                for (String workspaceId : workspaceIds) {
                    boolean workspaceExists = false;
                    for (WorkspaceDto workspaceDto : workspaceDtos) {
                        if (workspaceId.equals(workspaceDto.getId())) {
                            workspaceExists = true;
                            break;
                        }
                    }
                    if (!workspaceExists) {
                        localStorage.removeItem(LOCAL_STORAGE_DEBUGGER_STATES_KEY_PREFIX + workspaceId);
                    }
                }
            }
        });
    }

}
