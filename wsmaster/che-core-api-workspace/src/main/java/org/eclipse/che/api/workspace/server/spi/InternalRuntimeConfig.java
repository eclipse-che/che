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

import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.eclipse.che.api.core.model.workspace.config.Recipe;
import org.eclipse.che.api.core.rest.HttpRequestHelper;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;

import javax.ws.rs.HttpMethod;
import java.net.URL;
import java.util.Map;

/**
 * Environment configuration transformed to view useful for Infrastructure to create Runtime
 * @author gazarenkov
 */
public class InternalRuntimeConfig {

    protected Map<String, InternalMachineConfig> internalMachines;
//    protected InternalRecipeConfig recipe;
    protected EnvironmentImpl config;

    public InternalRuntimeConfig(Environment environment, URL registryEndpoint) throws InfrastructureException {

//        this.recipe = new InternalRecipeConfig(environment.getRecipe());

        Map<String, ? extends MachineConfig> effectiveMachines = environment.getMachines();

//        if(effectiveMachines.isEmpty())
//            effectiveMachines = analyzeRecipe(recipe.getScript());

        for(Map.Entry<String, ? extends MachineConfig> entry : effectiveMachines.entrySet()) {
            internalMachines.put(entry.getKey(), new InternalMachineConfig(entry.getValue(), registryEndpoint));
        }
    }



    //protected abstract Map<String, ? extends MachineConfig> analyzeRecipe(String recipe);

    private class InternalRecipeConfig {
        private String script;

        public InternalRecipeConfig(Recipe recipe) throws InfrastructureException {
            //this.recipe = new RecipeImpl(recipe.getType(), recipe.getContentType(), recipe.getContent(), recipe.getLocation());
            if(recipe.getContent() != null && !recipe.getContent().isEmpty()) {
                script = recipe.getContent();
            } else if(recipe.getLocation() != null && !recipe.getLocation().isEmpty()) {
                try {
                    script = HttpRequestHelper.requestString(recipe.getLocation(), HttpMethod.GET, null, null);
                } catch (Exception x) {
                    throw new InfrastructureException(x);
                }
            } else {
                script = "";
            }
        }

        public String getScript() {
            return script;
        }
    }
}
