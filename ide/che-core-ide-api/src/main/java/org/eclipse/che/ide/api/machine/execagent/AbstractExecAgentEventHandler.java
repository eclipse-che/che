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
package org.eclipse.che.ide.api.machine.execagent;

import org.eclipse.che.api.machine.shared.dto.execagent.event.DtoWithPidDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.jsonrpc.RequestHandler;
import org.eclipse.che.ide.util.loging.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Dmitry Kuleshov
 */
public abstract class AbstractExecAgentEventHandler<P extends DtoWithPidDto, R> extends RequestHandler<P, R>{

    private final Map<Integer, Set<Operation<P>>> operationRegistry = new HashMap<>();

    protected AbstractExecAgentEventHandler(Class<P> paramsClass, Class<R> resultClass) {
        super(paramsClass, resultClass);
    }

    public void handle(P params) {
        final int pid = params.getPid();
        final Set<Operation<P>> operations = operationRegistry.get(pid);

        if (operations != null) {
            for (Operation<P> operation : operations) {
                try {
                    operation.apply(params);
                } catch (OperationException e) {
                    Log.error(getClass(), "Cannot perform operation for DTO: " + params + ", because of " + e.getLocalizedMessage());
                }
            }
        }
    }

    public void registerOperation(int pid, Operation<P> operation){
        if (!operationRegistry.containsKey(pid)){
            operationRegistry.put(pid, new HashSet<Operation<P>>());
        }

        operationRegistry.get(pid).add(operation);
    }

    public void unregisterOperation(int pid, Operation<P> operation){
        if (operationRegistry.containsKey(pid)){
            operationRegistry.get(pid).remove(operation);
        }
    }

    public void unregisterOperations(int pid){
        operationRegistry.remove(pid);
    }
}
