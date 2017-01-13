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
package org.eclipse.che.api.workspace.server;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.machine.server.spi.Instance;
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
 * @author Mihail Kuznyetsov
 * @author Alexander Garagatyi
 */
@Path("/recipe/script")
public class RecipeScriptDownloadService extends Service {
    private final WorkspaceManager workspaceManager;
    private final RecipeRetriever  recipeRetriever;

    @Inject
    public RecipeScriptDownloadService(WorkspaceManager workspaceManager, RecipeRetriever recipeRetriever) {
        this.workspaceManager = workspaceManager;
        this.recipeRetriever = recipeRetriever;
    }

    @GET
    @Path("/{workspaceId}/{machineId}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getRecipeScript(@PathParam("workspaceId") String workspaceId,
                                  @PathParam("machineId") String machineId) throws ServerException,
                                                                                   NotFoundException {
        Instance machineInstance = workspaceManager.getMachineInstance(workspaceId, machineId);
        return recipeRetriever.getRecipe(machineInstance.getConfig()).getScript();
    }
}
