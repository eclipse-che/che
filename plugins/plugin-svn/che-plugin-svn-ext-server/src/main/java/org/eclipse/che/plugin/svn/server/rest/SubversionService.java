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
package org.eclipse.che.plugin.svn.server.rest;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.project.server.ProjectRegistry;
import org.eclipse.che.api.project.server.RegisteredProject;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.plugin.svn.server.SubversionApi;
import org.eclipse.che.plugin.svn.server.SubversionException;
import org.eclipse.che.plugin.svn.shared.AddRequest;
import org.eclipse.che.plugin.svn.shared.CLIOutputResponse;
import org.eclipse.che.plugin.svn.shared.CLIOutputResponseList;
import org.eclipse.che.plugin.svn.shared.CLIOutputWithRevisionResponse;
import org.eclipse.che.plugin.svn.shared.CleanupRequest;
import org.eclipse.che.plugin.svn.shared.CommitRequest;
import org.eclipse.che.plugin.svn.shared.CopyRequest;
import org.eclipse.che.plugin.svn.shared.GetRevisionsRequest;
import org.eclipse.che.plugin.svn.shared.GetRevisionsResponse;
import org.eclipse.che.plugin.svn.shared.InfoRequest;
import org.eclipse.che.plugin.svn.shared.InfoResponse;
import org.eclipse.che.plugin.svn.shared.ListRequest;
import org.eclipse.che.plugin.svn.shared.ListResponse;
import org.eclipse.che.plugin.svn.shared.LockRequest;
import org.eclipse.che.plugin.svn.shared.MergeRequest;
import org.eclipse.che.plugin.svn.shared.MoveRequest;
import org.eclipse.che.plugin.svn.shared.PropertyDeleteRequest;
import org.eclipse.che.plugin.svn.shared.PropertyGetRequest;
import org.eclipse.che.plugin.svn.shared.PropertyListRequest;
import org.eclipse.che.plugin.svn.shared.PropertySetRequest;
import org.eclipse.che.plugin.svn.shared.RemoveRequest;
import org.eclipse.che.plugin.svn.shared.ResolveRequest;
import org.eclipse.che.plugin.svn.shared.RevertRequest;
import org.eclipse.che.plugin.svn.shared.ShowDiffRequest;
import org.eclipse.che.plugin.svn.shared.ShowLogRequest;
import org.eclipse.che.plugin.svn.shared.StatusRequest;
import org.eclipse.che.plugin.svn.shared.SwitchRequest;
import org.eclipse.che.plugin.svn.shared.UpdateRequest;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
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
import java.io.IOException;

/**
 * REST API endpoints for this extension.
 */
@Path("svn")
public class SubversionService extends Service {

    @Inject
    private ProjectRegistry projectRegistry;

    @Inject
    private SubversionApi subversionApi;


    /**
     * Add the selected paths to version control.
     *
     * @param request
     *         the add request
     * @return the add response
     * @throws IOException
     *         if there is a problem executing the command
     * @throws SubversionException
     *         if there is a Subversion issue
     */
    @Path("add")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public CLIOutputResponse add(final AddRequest request) throws ApiException, IOException {
        request.setProjectPath(getAbsoluteProjectPath(request.getProjectPath()));
        return this.subversionApi.add(request);
    }

    /**
     * Remove the selected paths to version control.
     *
     * @param request
     *         the remove request
     * @return the remove response
     * @throws IOException
     *         if there is a problem executing the command
     * @throws SubversionException
     *         if there is a Subversion issue
     */
    @Path("remove")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public CLIOutputResponse remove(final RemoveRequest request) throws ApiException, IOException {
        request.setProjectPath(getAbsoluteProjectPath(request.getProjectPath()));
        return this.subversionApi.remove(request);
    }

    /**
     * Revert the selected paths.
     *
     * @param request
     *         the revert request
     * @return the revert response
     * @throws IOException
     *         if there is a problem executing the command
     * @throws SubversionException
     *         if there is a Subversion issue
     */
    @Path("revert")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public CLIOutputResponse revert(final RevertRequest request) throws ApiException, IOException {
        request.setProjectPath(getAbsoluteProjectPath(request.getProjectPath()));
        return this.subversionApi.revert(request);
    }

    /**
     * Copy provided path.
     *
     * @param request
     *         the copy request
     * @return the copy response
     * @throws ServerException
     *         if there is a Subversion issue
     * @throws IOException
     *         if there is a problem executing the command
     */
    @Path("copy")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public CLIOutputResponse copy(final CopyRequest request) throws ApiException, IOException {
        request.setProjectPath(getAbsoluteProjectPath(request.getProjectPath()));
        return this.subversionApi.copy(request);
    }

    /**
     * Retrieve the status of the paths in the request or the working copy as a whole.
     *
     * @param request
     *         the status request
     * @return the status response
     * @throws IOException
     *         if there is a problem executing the command
     * @throws SubversionException
     *         if there is a Subversion issue
     */
    @Path("status")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public CLIOutputResponse update(final StatusRequest request) throws ApiException, IOException {
        request.setProjectPath(getAbsoluteProjectPath(request.getProjectPath()));
        return this.subversionApi.status(request);
    }

    /**
     * Retrieve information about subversion resource.
     *
     * @param request
     * @return
     * @throws ServerException
     * @throws IOException
     */
    @Path("info")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public InfoResponse info(final InfoRequest request) throws ApiException, IOException {
        request.withProjectPath(getAbsoluteProjectPath(request.getProjectPath()));
        return this.subversionApi.info(request);
    }

    /**
     * Merge specified URL with target.
     *
     * @param request request
     * @return merge response
     * @throws ServerException
     * @throws IOException
     */
    @Path("merge")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public CLIOutputResponse merge(final MergeRequest request) throws ApiException, IOException {
        request.withProjectPath(getAbsoluteProjectPath(request.getProjectPath()));
        return this.subversionApi.merge(request);
    }

    /**
     * Update the working copy.
     *
     * @param request
     *         the update request
     * @return the update response
     * @throws IOException
     *         if there is a problem executing the command
     * @throws SubversionException
     *         if there is a Subversion issue
     */
    @Path("update")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public CLIOutputWithRevisionResponse update(final UpdateRequest request) throws ApiException, IOException {
        request.setProjectPath(getAbsoluteProjectPath(request.getProjectPath()));
        return this.subversionApi.update(request);
    }

    /**
     * Update the working copy to a different URL within the same repository.
     *
     * @param request
     *         the switch request
     * @return the switch response
     * @throws ApiException
     *         if there is a Subversion issue
     */
    @Path("switch")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public CLIOutputWithRevisionResponse doSwitch(final SwitchRequest request) throws ApiException {
        request.setProjectPath(getAbsoluteProjectPath(request.getProjectPath()));
        return subversionApi.doSwitch(request);
    }


    /**
     * Show log.
     *
     * @param request
     *         the show log request
     * @return the show log response
     * @throws IOException
     *         if there is a problem executing the command
     * @throws SubversionException
     *         if there is a Subversion issue
     */
    @Path("showlog")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public CLIOutputResponse showLog(final ShowLogRequest request) throws ApiException, IOException {
        request.setProjectPath(getAbsoluteProjectPath(request.getProjectPath()));
        return this.subversionApi.showLog(request);
    }

    /**
     * Show diff.
     *
     * @param request
     *         the show diff request
     * @return the show diff response
     * @throws IOException
     *         if there is a problem executing the command
     * @throws SubversionException
     *         if there is a Subversion issue
     */
    @Path("showdiff")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public CLIOutputResponse showDiff(final ShowDiffRequest request) throws ApiException, IOException {
        request.setProjectPath(getAbsoluteProjectPath(request.getProjectPath()));
        return this.subversionApi.showDiff(request);
    }

    /**
     * Lists remote directory.
     *
     * @param request
     *      the list request
     * @return children of the requested target path
     * @throws ApiException
     *       if there is a Subversion issue
     */
    @Path("list")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public ListResponse list(final ListRequest request) throws ApiException {
        request.setProjectPath(getAbsoluteProjectPath(request.getProjectPath()));
        return subversionApi.list(request);
    }

    /**
     * Returns list of the branches of the project.
     *
     * @param request
     *      the list request
     * @throws ApiException
     *         if there is a Subversion issue
     */
    @Path("branches")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public ListResponse listBranches(final ListRequest request) throws ApiException {
        request.setProjectPath(getAbsoluteProjectPath(request.getProjectPath()));
        return this.subversionApi.listBranches(request);
    }

    /**
     * Returns list of the tags of the project.
     *
     * @param request
     *      the list request
     * @throws ApiException
     *         if there is a Subversion issue
     */
    @Path("tags")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public ListResponse listTags(final ListRequest request) throws ApiException {
        request.setProjectPath(getAbsoluteProjectPath(request.getProjectPath()));
        return subversionApi.listTags(request);
    }

    /**
     * Resolve conflicts.
     *
     * @param request
     *         the resolve conflicts request
     * @return the resolve conflicts response
     * @throws IOException
     *         if there is a problem executing the command
     * @throws SubversionException
     *         if there is a Subversion issue
     */
    @Path("resolve")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public CLIOutputResponseList resolve(final ResolveRequest request) throws ApiException, IOException {
        request.setProjectPath(getAbsoluteProjectPath(request.getProjectPath()));
        return subversionApi.resolve(request);
    }

    /**
     * Commits the specified changes.
     *
     * @param request
     *         the commit request
     * @return the commit response
     * @throws ServerException
     * @throws IOException
     */
    @Path("commit")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public CLIOutputWithRevisionResponse commit(final CommitRequest request) throws ApiException, IOException {
        request.setProjectPath(getAbsoluteProjectPath(request.getProjectPath()));
        return this.subversionApi.commit(request);
    }

    /**
     * Cleans up the working copy.
     *
     * @param request
     *         the cleanup request
     * @return the response
     * @throws ServerException
     * @throws IOException
     */
    @Path("cleanup")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public CLIOutputResponse cleanup(final CleanupRequest request) throws ApiException, IOException {
        request.setProjectPath(getAbsoluteProjectPath(request.getProjectPath()));
        return this.subversionApi.cleanup(request);
    }

    @Path("lock")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public CLIOutputResponse lock(final LockRequest request) throws ApiException, IOException {
        request.setProjectPath(getAbsoluteProjectPath(request.getProjectPath()));
        return this.subversionApi.lockUnlock(request, true);
    }

    @Path("unlock")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public CLIOutputResponse unlock(final LockRequest request) throws ApiException, IOException {
        request.setProjectPath(getAbsoluteProjectPath(request.getProjectPath()));
        return this.subversionApi.lockUnlock(request, false);
    }

    @Path("export/{projectPath:.*}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response exportPath(final @PathParam("projectPath") String projectPath,
                               final @DefaultValue(".") @QueryParam("path") String path,
                               final @QueryParam("revision") String revision) throws ApiException, IOException {
        return this.subversionApi.exportPath(getAbsoluteProjectPath(projectPath), path, revision);
    }

    @Path("revisions")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public GetRevisionsResponse getRevisions(final GetRevisionsRequest request) throws ApiException, IOException {
        request.setProjectPath(getAbsoluteProjectPath(request.getProjectPath()));
        return this.subversionApi.getRevisions(request);
    }

    /**
     * Move provided path.
     *
     * @param request
     *         the copy request
     * @return the copy response
     * @throws ServerException
     *         if there is a Subversion issue
     * @throws IOException
     *         if there is a problem executing the command
     */
    @Path("move")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public CLIOutputResponse move(final MoveRequest request) throws ApiException, IOException {
        request.setProjectPath(getAbsoluteProjectPath(request.getProjectPath()));
        return this.subversionApi.move(request);
    }

    /**
     * Set property to specified path or target.
     *
     * @param request
     *         the property setting request
     * @return the property setting response
     * @throws ServerException
     *         if there is a Subversion issue
     * @throws IOException
     *         if there is a problem executing the command
     */
    @Path("propset")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public CLIOutputResponse propset(final PropertySetRequest request) throws ApiException, IOException {
        request.setProjectPath(getAbsoluteProjectPath(request.getProjectPath()));
        return this.subversionApi.propset(request);
    }

    /**
     * Delete property from specified path or target.
     *
     * @param request
     *         the property delete request
     * @return the property delete response
     * @throws ServerException
     *         if there is a Subversion issue
     * @throws IOException
     *         if there is a problem executing the command
     */
    @Path("propdel")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public CLIOutputResponse propdel(final PropertyDeleteRequest request) throws ApiException, IOException {
        request.setProjectPath(getAbsoluteProjectPath(request.getProjectPath()));
        return this.subversionApi.propdel(request);
    }

    /**
     * Get property for specified path or target.
     *
     * @param request
     *         the property setting request
     * @return the property setting response
     * @throws ServerException
     *         if there is a Subversion issue
     * @throws IOException
     *         if there is a problem executing the command
     */
    @Path("propget")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public CLIOutputResponse propget(final PropertyGetRequest request) throws ApiException, IOException {
        request.setProjectPath(getAbsoluteProjectPath(request.getProjectPath()));
        return this.subversionApi.propget(request);
    }

    /**
     * Get property for specified path or target.
     *
     * @param request
     *         the property setting request
     * @return the property setting response
     * @throws ServerException
     *         if there is a Subversion issue
     * @throws IOException
     *         if there is a problem executing the command
     */
    @Path("proplist")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public CLIOutputResponse proplist(final PropertyListRequest request) throws ApiException, IOException {
        request.setProjectPath(getAbsoluteProjectPath(request.getProjectPath()));
        return this.subversionApi.proplist(request);
    }

    @Path("import-source-descriptor")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public SourceStorageDto importDescriptor(@Context UriInfo uriInfo, @QueryParam("projectPath") String projectPath)
            throws ApiException, IOException {
        final RegisteredProject project = projectRegistry.getProject(projectPath);
        if (project.getBaseFolder().getChildFolder(".svn") != null) {
            return DtoFactory.getInstance().createDto(SourceStorageDto.class)
                    .withType("subversion")
                    .withLocation(subversionApi.getRepositoryUrl(getAbsoluteProjectPath(projectPath)));
        } else {
            throw new ServerException("Not subversion repository");
        }
    }

    private String getAbsoluteProjectPath(String wsRelatedProjectPath) {
        final RegisteredProject project = projectRegistry.getProject(wsRelatedProjectPath);
        return project.getBaseFolder().getVirtualFile().toIoFile().getAbsolutePath();
    }
}
