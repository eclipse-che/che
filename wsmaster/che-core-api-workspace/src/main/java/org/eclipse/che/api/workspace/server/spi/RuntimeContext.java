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

import org.eclipse.che.api.agent.server.AgentRegistry;
import org.eclipse.che.api.agent.server.impl.AgentSorter;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.eclipse.che.api.core.model.workspace.config.Recipe;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.rest.HttpRequestHelper;
import org.slf4j.Logger;

import javax.ws.rs.HttpMethod;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * A Context for running Workspace's Runtime
 *
 * @author gazarenkov
 */
public abstract class RuntimeContext {

    private static final Logger LOG = getLogger(RuntimeInfrastructure.class);

    protected final Environment           environment;
    protected final RuntimeIdentity   identity;
    protected final RuntimeInfrastructure infrastructure;
    // TODO other than WorkspaceStatus impl
    private         WorkspaceStatus       state;
    protected final InternalRecipe        recipe;
    protected final Map<String, InternalMachineConfig> internalMachines = new HashMap<>();
    //protected final URL statusChannel;

    public RuntimeContext(Environment environment,
                          RuntimeIdentity identity,
                          RuntimeInfrastructure infrastructure,
                          AgentSorter agentSorter,
                          AgentRegistry agentRegistry)
            throws ValidationException, InfrastructureException {
        this.environment = environment;
        this.identity = identity;
        this.infrastructure = infrastructure;
        this.recipe = resolveRecipe(environment.getRecipe());

        Map<String, ? extends MachineConfig> effectiveMachines = environment.getMachines();
        for(Map.Entry<String, ? extends MachineConfig> entry : effectiveMachines.entrySet()) {
            internalMachines.put(entry.getKey(), new InternalMachineConfig(entry.getValue(), agentRegistry, agentSorter));
        }

    }

    /**
     * Creates and starts Runtime.
     * In practice this method launching supposed to take unpredictable long time
     * so normally it should be launched in separated thread
     *
     * @param startOptions
     *         optional parameters
     * @return running runtime
     * @throws StateException
     *         when the context is already used
     * @throws InternalInfrastructureException
     *         when error that indicates system internal problem occurs
     * @throws InfrastructureException
     *         when any other error occurs
     */
    public void start(Map<String, String> startOptions) throws InfrastructureException {
        if (this.state != null) {
            throw new StateException("Context already used");
        }
        state = WorkspaceStatus.STARTING;
        internalStart(startOptions);
        state = WorkspaceStatus.RUNNING;
        //eturn runtime;
    }

    /**
     * Starts underlying environment in implementation specific way.
     *
     * @param startOptions options of workspace that may used in environment start
     * @return server internal representation of workspace runtime
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
     * @param stopOptions
     * @throws StateException
     *         when the context can't be stopped because otherwise it would be in inconsistent state
     *         (e.g. stop(interrupt) might not be allowed during start)
     * @throws InfrastructureException
     *         when any other error occurs
     */
    public final void stop(Map<String, String> stopOptions) throws InfrastructureException {
        if (this.state != WorkspaceStatus.RUNNING) {
            throw new StateException("The environment must be running");
        }
        state = WorkspaceStatus.STOPPING;
        // TODO spi what to do in exception appears here?
        try {
            internalStop(stopOptions);
        } catch (InternalInfrastructureException e) {
            LOG.error(format("Error occurs on stop of workspace %s. Error: " + e.getLocalizedMessage(),
                             identity.getWorkspaceId()), e);
        } catch (InfrastructureException e) {
            LOG.debug(e.getLocalizedMessage(), e);
        }
        state = WorkspaceStatus.STOPPED;
    }

    protected abstract void internalStop(Map<String, String> stopOptions) throws InfrastructureException;

    /**
     * Infrastructure should assign channel (usual WebSocket) to push long lived processes messages
     * Examples of such messages include:
     * - Start/Stop logs output
     * - Agent installer output
     * etc
     * It is expected that ones returning this URL implementation guarantees supporting and not changing
     * it during the whole life time of Runtime. Repeating calls of this method should return the same URL
     * If infrastructure implementation provides a channel it guarantees:
     * - this endpoint is open and ready to use
     * - this endpoint emits only messages of specified formats (TODO specify the formats)
     * - high loaded infrastructure provides scaling of "messaging server" to avoid overloading
     *
     * @return URL of the channels endpoint
     * @throws UnsupportedOperationException
     *         if implementation does not provide channel
     * @throws InfrastructureException
     */
    public abstract URL getOutputChannel() throws InfrastructureException,
                                                  UnsupportedOperationException;



//    /**
//     * Status Channel URL should be passed by Workspace API level. It is used for events about any kind of status changes, such as:
//     * - Agent installing statuses
//     * - Servers statuses
//     * - Infrastructure specific events
//     * Infrastructure MUST NOT use this channel for long-lived output (process stdout, logs etc)
//     * @return URL of status channel
//     */
//    public URL getStatusChannel() {
//        return statusChannel;
//    }


    /**
     * Runtime Identity contains information allowing uniquely identify a Runtime
     * It is not necessary that all of this information is used for identifying
     * Runtime outside of SPI framework (in practice workspace ID looks like enough)
     *
     * @return the RuntimeIdentityImpl
     */
    public RuntimeIdentity getIdentity() {
        return identity;
    }

//    /**
//     * @return incoming Workspace Environment
//     */
//    public Environment getEnvironment() {
//        return environment;
//    }


    public abstract InternalRuntime getRuntime();

    /**
     * @return RuntimeInfrastructure the Context created from
     */
    public RuntimeInfrastructure getInfrastructure() {
        return infrastructure;
    }

    // Returns environment with "suggested" RuntimeMachine list if no machines was declared
    // TODO need that?
//    public Environment getSuggestedEnv(Environment env) {
//        return effectiveEnv;
//    }


    private InternalRecipe resolveRecipe(Recipe recipe) throws InfrastructureException {
        if(recipe.getContent() != null && !recipe.getContent().isEmpty()) {
            return new InternalRecipe(recipe, recipe.getContent());
        } else if(recipe.getLocation() != null && !recipe.getLocation().isEmpty()) {

            try {
                URL recipeUrl = new URL(recipe.getLocation());
                if(recipeUrl.getProtocol().startsWith("http")) {
                    String script = HttpRequestHelper.requestString(recipe.getLocation(), HttpMethod.GET, null, null);
                    return new InternalRecipe(recipe, script);
                } else {
                    return new InternalRecipe(recipe, recipe.getLocation());
                }
            } catch (MalformedURLException e) {
                return new InternalRecipe(recipe, recipe.getLocation());
            } catch (Exception x) {
                throw new InfrastructureException(x);
            }

        } else {
            return new InternalRecipe(recipe, "");
        }
    }

    private class InternalRecipe {

        private final String content;
        private final String type;
        private final String contentType;

        public InternalRecipe(Recipe recipe, String content) {
            this.content = content;
            this.type = recipe.getType();
            this.contentType = recipe.getContentType();
        }

        public String getType() {
            return type;
        }

        public String getContentType() {
            return contentType;
        }

        public String getContent() {
            return content;
        }

    }


}
