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
package org.eclipse.che.api.workspace.server.spi;

import org.eclipse.che.api.core.model.workspace.Runtime;
import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.core.model.workspace.runtime.Server;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.api.workspace.server.ServerRewritingStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of concrete Runtime
 * Important to notice - no states in here, it is always RUNNING
 * @author gazarenkov
 */
public abstract class InternalRuntime implements Runtime {

    private final RuntimeContext          context;
    private final ServerRewritingStrategy serverRewritingStrategy;
    private Map<String, Machine>    cachedExternalMachines;
    private final List<Warning> warnings = new ArrayList<>();

    public InternalRuntime(RuntimeContext context, ServerRewritingStrategy serverRewritingStrategy) {
        this.context = context;
        this.serverRewritingStrategy = serverRewritingStrategy;
    }

    public abstract Map<String, ? extends Machine> getInternalMachines();


    @Override
    public String getActiveEnv() {
        return context.getIdentity().getEnvName();
    }

    @Override
    public String getOwner() {
        return context.getIdentity().getOwner();
    }

    @Override
    public List<? extends Warning> getWarnings() {
        return warnings;
    }

    @Override
    public Map<String, ? extends Machine> getMachines() {

        if(cachedExternalMachines == null) {
            cachedExternalMachines = new HashMap<>();
            for(Map.Entry<String, ? extends Machine> entry : getInternalMachines().entrySet()) {
                String key = entry.getKey();
                Machine machine = entry.getValue();
                ServerRewritingStrategy.Result result = serverRewritingStrategy.rewrite(context.getIdentity(), entry.getValue().getServers());
                Map<String, Server> newServers = result.getServers();
                MachineImpl newMachine = new MachineImpl(machine.getProperties(), newServers);
                cachedExternalMachines.put(key, newMachine);
                if(!result.getWarnings().isEmpty()) {
                    warnings.addAll(result.getWarnings());
                }
            }

        }
        return cachedExternalMachines;

    }


    /**
     * @return some implementation specific properties if any
     */
    public abstract Map <String, String> getProperties();

    /**
     * @return the Context
     */
    public final RuntimeContext getContext() {
        return context;
    }

}
