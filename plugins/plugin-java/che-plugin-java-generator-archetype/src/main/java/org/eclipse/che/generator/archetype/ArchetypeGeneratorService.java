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
package org.eclipse.che.generator.archetype;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.util.ContentTypeGuesser;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.generator.archetype.dto.GenerationTaskDescriptor;
import org.eclipse.che.generator.archetype.dto.MavenArchetype;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Provides access to the {@link ArchetypeGenerator} through HTTP.
 *
 * @author Artem Zatsarynnyi
 */
@Path("/generator-archetype")
public class ArchetypeGeneratorService {
    private static final Logger LOG = LoggerFactory.getLogger(ArchetypeGeneratorService.class);
    @Inject
    private ArchetypeGenerator archetypeGenerator;

    @Path("/generate")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public GenerationTaskDescriptor generate(@Context UriInfo uriInfo,
                                             @QueryParam("groupId") String groupId,
                                             @QueryParam("artifactId") String artifactId,
                                             @QueryParam("version") String version,
                                             MavenArchetype archetype) throws ServerException {
        ArchetypeGenerator.GenerationTask task = archetypeGenerator.generateFromArchetype(archetype, groupId, artifactId, version);
        final String statusUrl = uriInfo.getBaseUriBuilder().path(getClass()).path(getClass(), "getStatus").build(task.getId()).toString();
        return DtoFactory.getInstance().createDto(GenerationTaskDescriptor.class).withStatusUrl(statusUrl);
    }

    @GET
    @Path("/status/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public GenerationTaskDescriptor getStatus(@Context UriInfo uriInfo, @PathParam("id") String id) throws ServerException {
        ArchetypeGenerator.GenerationTask task = archetypeGenerator.getTaskById(Long.valueOf(id));
        final GenerationTaskDescriptor status = DtoFactory.getInstance().createDto(GenerationTaskDescriptor.class);
        if (!task.isDone()) {
            status.setStatus(GenerationTaskDescriptor.Status.IN_PROGRESS);
        } else if (task.getResult().isSuccessful()) {
            status.setStatus(GenerationTaskDescriptor.Status.SUCCESSFUL);
            final String downloadURL = uriInfo.getBaseUriBuilder()
                                              .path(getClass()).path(getClass(), "downloadGeneratedProject")
                                              .build(task.getId()).toString();
            status.setDownloadUrl(downloadURL);
        } else {
            status.setStatus(GenerationTaskDescriptor.Status.FAILED);
            try {
                status.setReport(new String(Files.readAllBytes(task.getResult().getGenerationReport().toPath())));
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        return status;
    }

    @GET
    @Path("/download/{id}")
    public Response downloadGeneratedProject(@PathParam("id") String id) throws ServerException {
        ArchetypeGenerator.GenerationTask task = archetypeGenerator.getTaskById(Long.valueOf(id));
        final File projectZip = task.getResult().getGeneratedProject();
        return Response.status(200)
                       .header("Content-Disposition", String.format("attachment; filename=\"%s\"", projectZip.getName()))
                       .type(ContentTypeGuesser.guessContentType(projectZip))
                       .entity(projectZip)
                       .build();
    }
}
