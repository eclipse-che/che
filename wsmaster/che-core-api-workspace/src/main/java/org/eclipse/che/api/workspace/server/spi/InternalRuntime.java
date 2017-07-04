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
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.core.model.workspace.runtime.Server;
import org.eclipse.che.api.workspace.server.URLRewriter;
import org.eclipse.che.api.workspace.server.model.impl.MachineImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerImpl;
import org.slf4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.stream.Collectors.toMap;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Implementation of concrete Runtime
 *
 * @author gazarenkov
 */
public abstract class InternalRuntime <T extends RuntimeContext> implements Runtime {

    private static final Logger LOG = getLogger(InternalRuntime.class);
    private final T                    context;
    private final URLRewriter          urlRewriter;
    private final List<Warning>        warnings = new ArrayList<>();
    private       WorkspaceStatus      status;

    public InternalRuntime(T context, URLRewriter urlRewriter) {
        this.context = context;
        this.urlRewriter = urlRewriter != null ? urlRewriter : new URLRewriter.NoOpURLRewriter();
    }

    /**
     * @return Map name -> Machine. Implementation should not return null
     */
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
        return getInternalMachines()
                .entrySet()
                .stream()
                .collect(toMap(Map.Entry::getKey,
                               e -> new MachineImpl(e.getValue().getProperties(),
                                                    rewriteExternalServers(e.getValue().getServers()))));
    }

    /**
     * Starts Runtime.
     * In practice this method launching supposed to take unpredictable long time
     * so normally it should be launched in separated thread
     *
     * @param startOptions
     *         optional parameters
     * @throws StateException
     *         when the context is already used
     * @throws InternalInfrastructureException
     *         when error that indicates system internal problem occurs
     * @throws InfrastructureException
     *         when any other error occurs
     */
    public void start(Map<String, String> startOptions) throws InfrastructureException {
        if (this.status != null) {
            throw new StateException("Context already used");
        }
        status = WorkspaceStatus.STARTING;
        internalStart(startOptions);
        status = WorkspaceStatus.RUNNING;
    }

    /**
     * Starts underlying environment in implementation specific way.
     *
     * @param startOptions options of workspace that may used in environment start
     * @throws InternalInfrastructureException
     *         when error that indicates system internal problem occurs
     * @throws InfrastructureException
     *         when any other error occurs
     */
    protected abstract void internalStart(Map<String, String> startOptions) throws InfrastructureException;

    /**
     * Stops Runtime
     * Presumably can take some time so considered to launch in separate thread
     *
     * @param stopOptions  options of workspace that may used in environment stop
     * @throws StateException
     *         when the context can't be stopped because otherwise it would be in inconsistent status
     *         (e.g. stop(interrupt) might not be allowed during start)
     * @throws InfrastructureException
     *         when any other error occurs
     */
    public final void stop(Map<String, String> stopOptions) throws InfrastructureException {
        if (this.status != WorkspaceStatus.RUNNING) {
            throw new StateException("The environment must be running");
        }
        status = WorkspaceStatus.STOPPING;

        // TODO spi what to do in exception appears here?
        try {
            internalStop(stopOptions);
        } catch (InternalInfrastructureException e) {
            LOG.error(format("Error occurs on stop of workspace %s. Error: " + e.getLocalizedMessage(),
                             context.getIdentity().getWorkspaceId()), e);
        } catch (InfrastructureException e) {
            LOG.debug(e.getLocalizedMessage(), e);
        }
        status = WorkspaceStatus.STOPPED;
    }

    protected abstract void internalStop(Map<String, String> stopOptions) throws InfrastructureException;


    /**
     * @return some implementation specific properties if any
     */
    public abstract Map<String, String> getProperties();

    /**
     * @return the Context
     */
    public final RuntimeContext getContext() {
        return context;
    }

    /**
     * Convenient method to rewrite incoming external servers in a loop
     * @param incoming servers
     * @return rewritten Map of Servers (name -> Server)
     */
    private Map<String, Server> rewriteExternalServers(Map<String, ? extends Server> incoming) {
        Map<String, Server> outgoing = new HashMap<>();
        for (Map.Entry<String, ? extends Server> entry : incoming.entrySet()) {
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
