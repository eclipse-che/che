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
package org.eclipse.che.api.installer.server;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import com.google.inject.Inject;

import org.eclipse.che.api.installer.server.exception.InstallerException;
import org.eclipse.che.api.installer.server.exception.InstallerNotFoundException;
import org.eclipse.che.api.installer.shared.dto.InstallerDto;
import org.eclipse.che.api.installer.shared.model.Installer;
import org.eclipse.che.api.installer.shared.model.InstallerKey;
import org.eclipse.che.api.installer.server.model.impl.InstallerKeyImpl;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.Service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.Collection;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.che.api.installer.server.DtoConverter.asDto;

/**
 * Defines Installer REST API.
 *
 * @see InstallerRegistry
 * @see Installer
 *
 * @author Anatoliy Bazko
 */
@Api(value = "/installer", description = "Installer REST API")
@Path("/installer")
public class InstallerRegistryService extends Service {

    private final InstallerRegistry installerRegistry;

    @Inject
    public InstallerRegistryService(InstallerRegistry installerRegistry) {
        this.installerRegistry = installerRegistry;
    }

    @GET
    @Path("/id/{id}")
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Gets the latest version of the installer", response = InstallerDto.class)
    @ApiResponses({@ApiResponse(code = 200, message = "The response contains requested installer entity"),
                   @ApiResponse(code = 404, message = "Installer not found in the registry"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public Installer getById(@ApiParam("The installer id") @PathParam("id") String id) throws ApiException {
        try {
            return asDto(installerRegistry.getInstaller(new InstallerKeyImpl(id)));
        } catch (InstallerNotFoundException e) {
            throw new NotFoundException(e.getMessage());
        } catch (InstallerException e) {
            throw new ServerException(e.getMessage(), e);
        }
    }

    @GET
    @Path("/id/{id}/version/{version}")
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Gets the specific version of the installer", response = InstallerDto.class)
    @ApiResponses({@ApiResponse(code = 200, message = "The response contains requested installer entity"),
                   @ApiResponse(code = 404, message = "Installer not found in the registry"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public Installer getByName(@ApiParam("The installer id") @PathParam("id") String id,
                           @ApiParam("The installer version") @PathParam("version") String version) throws ApiException {
        try {
            return asDto(installerRegistry.getInstaller(new InstallerKeyImpl(id, version)));
        } catch (InstallerNotFoundException e) {
            throw new NotFoundException(e.getMessage());
        } catch (InstallerException e) {
            throw new ServerException(e.getMessage(), e);
        }

    }

    @GET
    @Path("/versions/{id}")
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Get a list of available versions of the giving installer", response = List.class)
    @ApiResponses({@ApiResponse(code = 200, message = "The response contains available versions of the giving installer"),
                   @ApiResponse(code = 404, message = "Installer not found"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public List<String> getVersions(@ApiParam("The installer id") @PathParam("id") String id) throws ApiException {
        try {
            return installerRegistry.getVersions(id);
        } catch (InstallerNotFoundException e) {
            throw new NotFoundException(e.getMessage());
        } catch (InstallerException e) {
            throw new ServerException(e.getMessage(), e);
        }
    }

    @GET
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Get a collection of the available installers", response = Collection.class)
    @ApiResponses({@ApiResponse(code = 200, message = "The response contains collection of available installers"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public Collection<Installer> getInstallers() throws ApiException {
        try {
            return installerRegistry.getInstallers();
        } catch (InstallerNotFoundException e) {
            throw new NotFoundException(e.getMessage());
        } catch (InstallerException e) {
            throw new ServerException(e.getMessage(), e);
        }
    }

    // TODO
    public List <Installer> getOrderedInstallers(List <InstallerKey> keys) {
        return installerRegistry.getOrderedInstallers(keys);
    }
}
