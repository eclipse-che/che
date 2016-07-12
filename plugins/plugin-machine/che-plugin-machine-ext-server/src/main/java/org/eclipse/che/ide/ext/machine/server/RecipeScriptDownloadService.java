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
package org.eclipse.che.ide.ext.machine.server;


import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.machine.server.MachineManager;
import org.eclipse.che.api.machine.server.util.RecipeRetriever;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Service for downloading recipe script for machine.
 *
 * @author Mihail Kuznyetsov.
 */
@Path("/recipe/script")
public class RecipeScriptDownloadService extends Service {

    private final MachineManager  machineManager;
    private final RecipeRetriever recipeRetriever;

    @Inject
    public RecipeScriptDownloadService(MachineManager machineManager, RecipeRetriever recipeRetriever) {
        this.machineManager = machineManager;
        this.recipeRetriever = recipeRetriever;
    }

    @GET
    @Path("/{machineId}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getRecipeScript(@PathParam("machineId") String machineId) throws ServerException, NotFoundException {
        return recipeRetriever.getRecipe(machineManager.getMachine(machineId).getConfig()).getScript();
    }
}
