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
package org.eclipse.che.ide.api.machine.execagent;

import org.eclipse.che.api.machine.shared.dto.execagent.event.DtoWithPid;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.jsonrpc.JsonRpcRequestBiOperation;
import org.eclipse.che.ide.util.loging.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public abstract class AbstractExecAgentEventHandler<P extends DtoWithPid> implements JsonRpcRequestBiOperation<P> {

    private final Map<String, Set<Operation<P>>> operationRegistry = new HashMap<>();

    protected void handle(String endpointId, P params) {
        int pid = params.getPid();
        String key = endpointId + '@' + pid;

        if (!operationRegistry.containsKey(key)) {
            return;
        }

        for (Operation<P> operation : operationRegistry.get(key)) {
            try {
                operation.apply(params);
            } catch (OperationException e) {
                Log.error(getClass(), "Cannot perform operation for DTO: " + params + ", because of " + e.getLocalizedMessage());
            }
        }
    }

    public void registerOperation(String endpointId, int pid, Operation<P> operation) {
        String key = endpointId + '@' + pid;
        if (!operationRegistry.containsKey(key)) {
            operationRegistry.put(key, new HashSet<Operation<P>>());
        }

        operationRegistry.get(key).add(operation);
    }

    public void unregisterOperation(String endpointId, int pid, Operation<P> operation) {
        String key = endpointId + '@' + pid;
        if (operationRegistry.containsKey(key)) {
            operationRegistry.get(key).remove(operation);
        }
    }

    public void unregisterOperations(String endpointId, int pid) {
        String key = endpointId + '@' + pid;
        operationRegistry.remove(key);
    }
}
