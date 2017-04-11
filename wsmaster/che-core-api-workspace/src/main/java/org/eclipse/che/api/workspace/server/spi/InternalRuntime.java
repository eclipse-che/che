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
import org.eclipse.che.api.machine.server.model.impl.ServerImpl;
import org.eclipse.che.api.workspace.server.URLRewriter;

import java.net.MalformedURLException;
import java.net.URL;
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

    private final RuntimeContext       context;
    private final URLRewriter          urlRewriter;
    private       Map<String, Machine> cachedExternalMachines;
    private final List<Warning> warnings = new ArrayList<>();

    public InternalRuntime(RuntimeContext context, URLRewriter urlRewriter) {
        this.context = context;
        this.urlRewriter = urlRewriter;
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
                Map<String, Server> newServers = rewriteExternalServers(machine.getServers());
                MachineImpl newMachine = new MachineImpl(machine.getProperties(), newServers);
                cachedExternalMachines.put(key, newMachine);
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

    /**
     * Convenient method to rewrite incoming external servers in a loop
     * @param incoming servers
     * @return rewriten Map of Servers (name -> Server)
     */
    private Map <String, Server> rewriteExternalServers(Map<String,? extends Server> incoming) {
        Map <String, Server> outgoing = new HashMap<>();
        for(Map.Entry<String, ? extends Server> entry : incoming.entrySet()) {
            String name = entry.getKey();
            String strUrl = entry.getValue().getUrl();
            try {
                URL url = new URL(strUrl);
                ServerImpl server = new ServerImpl(urlRewriter.rewriteURL(context.getIdentity(), name, url).toString());
                outgoing.put(name, server);
            } catch (MalformedURLException e) {
                warnings.add(new Warning() {
                    @Override
                    public int getCode() {
                        return 101;
                    }

                    @Override
                    public String getMessage() {
                        return "Malformed URL for " + name + " : " + e.getMessage();
                    }
                });
            }

        }

        return outgoing;
    }

}
