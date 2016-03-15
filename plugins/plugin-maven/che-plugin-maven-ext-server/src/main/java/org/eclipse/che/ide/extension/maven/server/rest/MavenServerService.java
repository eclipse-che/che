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
package org.eclipse.che.ide.extension.maven.server.rest;

import com.google.inject.Inject;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.ProjectRegistry;
import org.eclipse.che.api.project.server.RegisteredProject;
import org.eclipse.che.api.project.server.VirtualFileEntry;
import org.eclipse.che.ide.extension.maven.server.MavenServerManager;
import org.eclipse.che.ide.extension.maven.server.MavenServerWrapper;
import org.eclipse.che.ide.extension.maven.server.core.MavenProgressNotifier;
import org.eclipse.che.ide.extension.maven.server.core.MavenProjectManager;
import org.eclipse.che.ide.extension.maven.server.core.classpath.ClasspathManager;
import org.eclipse.che.maven.server.MavenTerminal;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.Collections;

import static javax.ws.rs.core.MediaType.TEXT_XML;

/**
 * Service for the maven operations.
 *
 * @author Valeriy Svydenko
 */
@Path("/maven/{wsId}/server")
public class MavenServerService {
    private final MavenServerManager mavenServerManager;
    private final ProjectRegistry    projectRegistry;

    @PathParam("wsId")
    private String workspaceId;

    @Inject
    private MavenProgressNotifier notifier;

    @Inject
    private MavenTerminal terminal;

    @Inject
    private MavenProjectManager projectManager;

    @Inject
    private ClasspathManager classpathManager;

    @Inject
    public MavenServerService(MavenServerManager mavenServerManager, ProjectRegistry projectRegistry) {
        this.mavenServerManager = mavenServerManager;
        this.projectRegistry = projectRegistry;
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


        MavenServerWrapper mavenServer = mavenServerManager.createMavenServer();

        try {
            mavenServer.customize(projectManager.copyWorkspaceCache(), terminal, notifier, false, false);
            VirtualFileEntry pomFile = project.getBaseFolder().getChild("pom.xml");
            if (pomFile == null) {
                throw new NotFoundException("pom.xml doesn't exist");
            }
            return mavenServer.getEffectivePom(pomFile.getVirtualFile().toIoFile(), Collections.emptyList(), Collections.emptyList());
        } finally {
            mavenServer.reset();
        }
    }

    @GET
    @Path("download/sources")
    public boolean downloadSource(@QueryParam("projectpath") String projectPath, @QueryParam("fqn") String fqn) {
        return classpathManager.downloadSources(projectPath, fqn);
    }


}
