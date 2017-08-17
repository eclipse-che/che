/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.workspace.server.spi;

import com.google.common.collect.ImmutableMap;

import org.eclipse.che.api.installer.server.InstallerRegistry;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.eclipse.che.api.core.model.workspace.config.Recipe;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.rest.HttpRequestHelper;

import javax.ws.rs.HttpMethod;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * A Context for running Workspace's Runtime
 *
 * @author gazarenkov
 */
public abstract class RuntimeContext {

    protected final Environment           environment;
    protected final RuntimeIdentity       identity;
    protected final RuntimeInfrastructure infrastructure;
    protected final InternalRecipe        recipe;
    protected final Map<String, InternalMachineConfig> internalMachines = new HashMap<>();

    public RuntimeContext(Environment environment,
                          RuntimeIdentity identity,
                          RuntimeInfrastructure infrastructure,
                          InstallerRegistry installerRegistry)
            throws ValidationException, InfrastructureException {
        this.environment = environment;
        this.identity = identity;
        this.infrastructure = infrastructure;
        this.recipe = resolveRecipe(environment.getRecipe());

        Map<String, ? extends MachineConfig> effectiveMachines = environment.getMachines();
        for (Map.Entry<String, ? extends MachineConfig> entry : effectiveMachines.entrySet()) {
            internalMachines.put(entry.getKey(), new InternalMachineConfig(entry.getValue(), installerRegistry));
        }
    }

    /**
     * Context must return the Runtime object whatever its status is (STOPPED status including)
     *
     * @return Runtime object
     * @throws InfrastructureException
     *         when any error during runtime retrieving/creation
     */
    public abstract InternalRuntime getRuntime() throws InfrastructureException;

    /**
     * Infrastructure should assign channel (usual WebSocket) to push long lived processes messages
     * Examples of such messages include:
     * - Start/Stop logs output
     * - Installers output
     * etc
     * It is expected that ones returning this URI implementation guarantees supporting and not changing
     * it during the whole life time of Runtime. Repeating calls of this method should return the same URI
     * If infrastructure implementation provides a channel it guarantees:
     * - this endpoint is open and ready to use
     * - this endpoint emits only messages of specified formats (TODO specify the formats)
     * - high loaded infrastructure provides scaling of "messaging server" to avoid overloading
     *
     * @return URI of the channels endpoint
     * @throws UnsupportedOperationException
     *         if implementation does not provide channel
     * @throws InfrastructureException
     *         when any other error occurs
     */
    public abstract URI getOutputChannel() throws InfrastructureException,
                                                  UnsupportedOperationException;

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


    /**
     * Return internal machines map.
     *
     * @return immutable copy of internal machines map.
     */
    public Map<String, InternalMachineConfig> getMachineConfigs() {
        return ImmutableMap.copyOf(internalMachines);
    }

    /**
     * @return RuntimeInfrastructure the Context created from
     */
    public RuntimeInfrastructure getInfrastructure() {
        return infrastructure;
    }


    private InternalRecipe resolveRecipe(Recipe recipe) throws InfrastructureException {
        if (recipe.getContent() != null && !recipe.getContent().isEmpty()) {
            return new InternalRecipe(recipe, recipe.getContent());
        } else if (recipe.getLocation() != null && !recipe.getLocation().isEmpty()) {

            try {
                URL recipeUrl = new URL(recipe.getLocation());
                if (recipeUrl.getProtocol().startsWith("http")) {
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

    protected class InternalRecipe {

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
