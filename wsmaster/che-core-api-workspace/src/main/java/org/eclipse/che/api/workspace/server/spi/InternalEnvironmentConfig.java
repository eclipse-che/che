package org.eclipse.che.api.workspace.server.spi;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.config.Recipe;
import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.eclipse.che.api.core.rest.DefaultHttpJsonRequest;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

/**
 * Environment configuration transformed to view useful for Infrastructure to create Runtime
 * @author gazarenkov
 */
public class InternalEnvironmentConfig {

    protected Map<String, InternalMachineConfig> internalMachines;
    protected InternalRecipeConfig recipe;
    protected EnvironmentImpl config;

    public InternalEnvironmentConfig(Environment environment, URL registryEndpoint) throws ApiException, IOException {

        this.recipe = new InternalRecipeConfig(environment.getRecipe());

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

        public InternalRecipeConfig(Recipe recipe) throws ApiException, IOException {
            //this.recipe = new RecipeImpl(recipe.getType(), recipe.getContentType(), recipe.getContent(), recipe.getLocation());
            if(recipe.getContent() != null && !recipe.getContent().isEmpty()) {
                script = recipe.getContent();
            } else if(recipe.getLocation() != null && !recipe.getLocation().isEmpty()) {
                script = DefaultHttpJsonRequest.create(recipe.getLocation()).request().asString();
            } else {
                script = "";
            }
        }

        public String getScript() {
            return script;
        }
    }
}
