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
package org.eclipse.che.plugin.maven.server.rest;

import com.google.inject.Inject;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.languageserver.registry.InitializedLanguageServer;
import org.eclipse.che.api.languageserver.registry.LanguageServerRegistry;
import org.eclipse.che.api.languageserver.service.LanguageServiceUtils;
import org.eclipse.che.api.project.server.ProjectRegistry;
import org.eclipse.che.api.project.server.RegisteredProject;
import org.eclipse.che.api.project.server.VirtualFileEntry;
import org.eclipse.che.maven.server.MavenTerminal;
import org.eclipse.che.plugin.maven.lsp.MavenLanguageServer;
import org.eclipse.che.plugin.maven.server.MavenServerWrapper;
import org.eclipse.che.plugin.maven.server.MavenWrapperManager;
import org.eclipse.che.plugin.maven.server.core.EclipseWorkspaceProvider;
import org.eclipse.che.plugin.maven.server.core.MavenProgressNotifier;
import org.eclipse.che.plugin.maven.server.core.MavenProjectManager;
import org.eclipse.che.plugin.maven.server.core.MavenWorkspace;
import org.eclipse.che.plugin.maven.server.core.classpath.ClasspathManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.TEXT_XML;

/**
 * Service for the maven operations.
 *
 * @author Valeriy Svydenko
 */
@Path("/maven/server")
public class MavenServerService {
    private final MavenWrapperManager      wrapperManager;
    private final ProjectRegistry          projectRegistry;
    private final MavenWorkspace           mavenWorkspace;
    private final EclipseWorkspaceProvider eclipseWorkspaceProvider;

    @Inject
    private MavenProgressNotifier notifier;

    @Inject
    private MavenTerminal terminal;

    @Inject
    private MavenProjectManager projectManager;

    @Inject
    private ClasspathManager classpathManager;

    @Inject
    private LanguageServerRegistry lsRegistry;

    @Inject
    public MavenServerService(MavenWrapperManager wrapperManager,
                              ProjectRegistry projectRegistry,
                              MavenWorkspace mavenWorkspace,
                              EclipseWorkspaceProvider eclipseWorkspaceProvider) {

        this.wrapperManager = wrapperManager;
        this.projectRegistry = projectRegistry;
        this.mavenWorkspace = mavenWorkspace;
        this.eclipseWorkspaceProvider = eclipseWorkspaceProvider;
    }

    /**
     * Returns maven effective pom file.
     *
     * @param projectPath
     *         path to the opened pom file
     * @return content of the effective pom
     * @throws ServerException
     *         when getting mount point has a problem
     * @throws NotFoundException
     *         when current pom file isn't exist
     * @throws ForbiddenException
     *         when response code is 403
     */
    @GET
    @Path("effective/pom")
    @Produces(TEXT_XML)
    public String getEffectivePom(@QueryParam("projectpath") String projectPath) throws ServerException,
                                                                                        NotFoundException,
                                                                                        ForbiddenException {
        RegisteredProject project = projectRegistry.getProject(projectPath);
        if (project == null) {
            throw new NotFoundException("Project " + projectPath + " doesn't exist");
        }


        MavenServerWrapper mavenServer = wrapperManager.getMavenServer(MavenWrapperManager.ServerType.DOWNLOAD);

        try {
            mavenServer.customize(projectManager.copyWorkspaceCache(), terminal, notifier, false, false);
            VirtualFileEntry pomFile = project.getBaseFolder().getChild("pom.xml");
            if (pomFile == null) {
                throw new NotFoundException("pom.xml doesn't exist");
            }
            return mavenServer.getEffectivePom(pomFile.getVirtualFile().toIoFile(), Collections.emptyList(), Collections.emptyList());
        } finally {
            wrapperManager.release(mavenServer);
        }
    }

    @GET
    @Path("download/sources")
    @Produces("text/plain")
    public String downloadSource(@QueryParam("projectpath") String projectPath, @QueryParam("fqn") String fqn) {
        return Boolean.toString(classpathManager.downloadSources(projectPath, fqn));
    }

    @POST
    @Path("reimport")
    @ApiOperation(value = "Re-import maven model")
    @ApiResponses({@ApiResponse(code = 200, message = "OK"),
                   @ApiResponse(code = 500, message = "Internal Server Error")})
    public Response reimportDependencies(@ApiParam(value = "The paths to projects which need to be reimported")
                                         @QueryParam("projectPath") List<String> paths) throws ServerException {
        IWorkspace workspace = eclipseWorkspaceProvider.get();
        List<IProject> projectsList =
                paths.stream().map(projectPath -> workspace.getRoot().getProject(projectPath)).collect(Collectors.toList());
        mavenWorkspace.update(projectsList);
        return Response.ok().build();
    }

    @GET
    @Path("pom/reconcile")
    @ApiOperation(value = "Reconcile pom.xml file")
    @ApiResponses({@ApiResponse(code = 200, message = "OK")})
    public void reconcile(@ApiParam(value = "The paths to pom.xml file which need to be reconciled")
                                      @QueryParam("pompath") String pomPath)
            throws ForbiddenException, ConflictException, NotFoundException, ServerException {
        String projectPath = new File(pomPath).getParent();
        List<Collection<InitializedLanguageServer>> languageServers = lsRegistry.getApplicableLanguageServers(LanguageServiceUtils.prefixURI(pomPath));
        languageServers.stream().flatMap(Collection::stream).map(InitializedLanguageServer::getServer).filter(ls -> ls instanceof MavenLanguageServer).findAny().ifPresent(ls -> {
            ((MavenLanguageServer) ls).reconcile(pomPath, projectPath);
        });
        
    }
}
