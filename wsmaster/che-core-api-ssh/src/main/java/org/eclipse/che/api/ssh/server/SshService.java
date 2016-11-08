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
package org.eclipse.che.api.ssh.server;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.apache.commons.fileupload.FileItem;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.core.rest.annotations.GenerateLink;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.core.util.LinksHelper;
import org.eclipse.che.api.ssh.server.model.impl.SshPairImpl;
import org.eclipse.che.api.ssh.shared.Constants;
import org.eclipse.che.api.ssh.shared.dto.GenerateSshPairRequest;
import org.eclipse.che.api.ssh.shared.dto.SshPairDto;
import org.eclipse.che.api.ssh.shared.model.SshPair;
import org.eclipse.che.commons.env.EnvironmentContext;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.che.api.ssh.shared.Constants.LINK_REL_GET_PAIR;
import static org.eclipse.che.api.ssh.shared.Constants.LINK_REL_REMOVE_PAIR;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Defines Ssh Rest API.
 *
 * @author Sergii Leschenko
 */
@Api(value = "/ssh", description = "Ssh REST API")
@Path("/ssh")
public class SshService extends Service {
    private final SshManager sshManager;

    @Inject
    public SshService(SshManager sshManager) {
        this.sshManager = sshManager;
    }

    @POST
    @Path("generate")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @GenerateLink(rel = Constants.LINK_REL_GENERATE_PAIR)
    @ApiOperation(value = "Generate and stores ssh pair based on the request",
                  notes = "This operation can be performed only by authorized user," +
                          "this user will be the owner of the created ssh pair",
                  response = SshPairDto.class)
    @ApiResponses({@ApiResponse(code = 201, message = "The ssh pair successfully generated"),
                   @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
                   @ApiResponse(code = 409, message = "Conflict error occurred during the ssh pair generation" +
                                                      "(e.g. The Ssh pair with such name and service already exists)"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public Response generatePair(@ApiParam(value = "The configuration to generate the new ssh pair", required = true)
                                   GenerateSshPairRequest request) throws BadRequestException, ServerException, ConflictException {
        requiredNotNull(request, "Generate ssh pair request required");
        requiredNotNull(request.getService(), "Service name required");
        requiredNotNull(request.getName(), "Name required");
        final SshPairImpl generatedPair = sshManager.generatePair(getCurrentUserId(), request.getService(), request.getName());

        return Response.status(Response.Status.CREATED)
                       .entity(asDto(injectLinks(asDto(generatedPair))))
                       .build();
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_HTML)
    @GenerateLink(rel = Constants.LINK_REL_CREATE_PAIR)
    public Response createPair(Iterator<FileItem> formData) throws BadRequestException, ServerException, ConflictException {
        String service = null;
        String name = null;
        String privateKey = null;
        String publicKey = null;

        while (formData.hasNext()) {
            FileItem item = formData.next();
            String fieldName = item.getFieldName();
            switch (fieldName) {
                case "service":
                    service = item.getString();
                    break;
                case "name":
                    name = item.getString();
                    break;
                case "privateKey":
                    privateKey = item.getString();
                    break;
                case "publicKey":
                    publicKey = item.getString();
                    break;
                default:
                    //do nothing
            }
        }

        requiredNotNull(service, "Service name required");
        requiredNotNull(name, "Name required");
        if (privateKey == null && publicKey == null) {
            throw new BadRequestException("Key content was not provided.");
        }

        sshManager.createPair(new SshPairImpl(getCurrentUserId(), service, name, publicKey, privateKey));

        // We should send 200 response code and body with empty line
        // through specific of html form that doesn't invoke complete submit handler
        return Response.ok("", MediaType.TEXT_HTML).build();
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @GenerateLink(rel = Constants.LINK_REL_CREATE_PAIR)
    @ApiOperation(value = "Create a new ssh pair",
                  notes = "This operation can be performed only by authorized user," +
                          "this user will be the owner of the created ssh pair")
    @ApiResponses({@ApiResponse(code = 204, message = "The ssh pair successfully created"),
                   @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
                   @ApiResponse(code = 409, message = "Conflict error occurred during the ssh pair creation" +
                                                      "(e.g. The Ssh pair with such name and service already exists)"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public void createPair(@ApiParam(value = "The ssh pair to create", required = true)
                           SshPairDto sshPair) throws BadRequestException, ServerException, ConflictException {
        requiredNotNull(sshPair, "Ssh pair required");
        requiredNotNull(sshPair.getService(), "Service name required");
        requiredNotNull(sshPair.getName(), "Name required");
        if (sshPair.getPublicKey() == null && sshPair.getPrivateKey() == null) {
            throw new BadRequestException("Key content was not provided.");
        }

        sshManager.createPair(new SshPairImpl(getCurrentUserId(), sshPair));
    }

    @GET
    @Path("{service}/find")
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Get the ssh pair by the name of pair and name of service owned by the current user",
                  notes = "This operation can be performed only by authorized user.")
    @ApiResponses({@ApiResponse(code = 200, message = "The ssh pair successfully fetched"),
                   @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
                   @ApiResponse(code = 404, message = "The ssh pair with specified name and service does not exist for current user"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public SshPairDto getPair(@ApiParam("Name of service")
                              @PathParam("service")
                              String service,
                              @ApiParam(value = "Name of ssh pair", required = true)
                              @QueryParam("name")
                              String name) throws NotFoundException, ServerException, BadRequestException {
        requiredNotNull(name, "Name of ssh pair");
        return injectLinks(asDto(sshManager.getPair(getCurrentUserId(), service, name)));
    }

    @DELETE
    @Path("{service}")
    @ApiOperation(value = "Remove the ssh pair by the name of pair and name of service owned by the current user")
    @ApiResponses({@ApiResponse(code = 204, message = "The ssh pair successfully removed"),
                   @ApiResponse(code = 400, message = "Missed required parameters, parameters are not valid"),
                   @ApiResponse(code = 404, message = "The ssh pair doesn't exist"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public void removePair(@ApiParam("Name of service")
                           @PathParam("service")
                           String service,
                           @ApiParam(value = "Name of ssh pair", required = true)
                           @QueryParam("name")
                           String name) throws ServerException, NotFoundException, BadRequestException {
        requiredNotNull(name, "Name of ssh pair");
        sshManager.removePair(getCurrentUserId(), service, name);
    }

    @GET
    @Path("{service}")
    @Produces(APPLICATION_JSON)
    @ApiOperation(value = "Get the ssh pairs by name of service owned by the current user",
                  notes = "This operation can be performed only by authorized user.",
                  response = SshPairDto.class,
                  responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "The ssh pairs successfully fetched"),
                   @ApiResponse(code = 500, message = "Internal server error occurred")})
    public List<SshPairDto> getPairs(@ApiParam("Name of service")
                                     @PathParam("service")
                                     String service) throws ServerException {
        return sshManager.getPairs(getCurrentUserId(), service)
                         .stream()
                         .map(sshPair -> injectLinks(asDto(sshPair)))
                         .collect(Collectors.toList());
    }

    private static String getCurrentUserId() {
        return EnvironmentContext.getCurrent().getSubject().getUserId();
    }

    private static SshPairDto asDto(SshPair pair) {
        return newDto(SshPairDto.class).withService(pair.getService())
                                       .withName(pair.getName())
                                       .withPublicKey(pair.getPublicKey())
                                       .withPrivateKey(pair.getPrivateKey());
    }

    private SshPairDto injectLinks(SshPairDto sshPairDto) {
        final UriBuilder uriBuilder = getServiceContext().getServiceUriBuilder();
        final Link getPairsLink = LinksHelper.createLink("GET",
                                                         uriBuilder.clone()
                                                                   .path(getClass(), "getPairs")
                                                                   .build(sshPairDto.getService())
                                                                   .toString(),
                                                         APPLICATION_JSON,
                                                         LINK_REL_GET_PAIR);

        final Link removePairLink = LinksHelper.createLink("DELETE",
                                                           uriBuilder.clone()
                                                                     .path(getClass(), "removePair")
                                                                     .build(sshPairDto.getService(), sshPairDto.getName())
                                                                     .toString(),
                                                           APPLICATION_JSON,
                                                           LINK_REL_REMOVE_PAIR);

        final Link getPairLink = LinksHelper.createLink("GET",
                                                        uriBuilder.clone()
                                                                  .path(getClass(), "getPair")
                                                                  .build(sshPairDto.getService(), sshPairDto.getName())
                                                                  .toString(),
                                                        APPLICATION_JSON,
                                                        LINK_REL_GET_PAIR);

        return sshPairDto.withLinks(Arrays.asList(getPairsLink, removePairLink, getPairLink));
    }

    /**
     * Checks object reference is not {@code null}
     *
     * @param object
     *         object reference to check
     * @param subject
     *         used as subject of exception message "{subject} required"
     * @throws BadRequestException
     *         when object reference is {@code null}
     */
    private void requiredNotNull(Object object, String subject) throws BadRequestException {
        if (object == null) {
            throw new BadRequestException(subject + " required");
        }
    }
}
