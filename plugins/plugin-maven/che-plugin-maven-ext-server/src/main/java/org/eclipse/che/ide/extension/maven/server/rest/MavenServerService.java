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
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.ProjectRegistry;
import org.eclipse.che.api.project.server.RegisteredProject;
import org.eclipse.che.api.project.server.VirtualFileEntry;
import org.eclipse.che.commons.xml.XMLTreeException;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.ide.ext.java.shared.dto.Problem;
import org.eclipse.che.ide.extension.maven.server.MavenServerManager;
import org.eclipse.che.ide.extension.maven.server.MavenServerWrapper;
import org.eclipse.che.ide.extension.maven.server.core.MavenProgressNotifier;
import org.eclipse.che.ide.extension.maven.server.core.MavenProjectManager;
import org.eclipse.che.ide.extension.maven.server.core.classpath.ClasspathManager;
import org.eclipse.che.ide.extension.maven.server.core.project.MavenProject;
import org.eclipse.che.ide.maven.tools.Model;
import org.eclipse.che.maven.data.MavenProjectProblem;
import org.eclipse.che.maven.server.MavenTerminal;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXParseException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.TEXT_XML;

/**
 * Service for the maven operations.
 *
 * @author Valeriy Svydenko
 */
@Path("/maven/{wsId}/server")
public class MavenServerService {
    private static final Logger LOG = LoggerFactory.getLogger(MavenServerService.class);

    private final MavenServerManager  mavenServerManager;
    private final ProjectRegistry     projectRegistry;
    private final MavenProjectManager mavenProjectManager;
    private final ProjectManager      cheProjectManager;

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
    public MavenServerService(MavenServerManager mavenServerManager,
                              ProjectRegistry projectRegistry,
                              ProjectManager projectManager,
                              MavenProjectManager mavenProjectManager) {

        cheProjectManager = projectManager;
        this.mavenServerManager = mavenServerManager;
        this.projectRegistry = projectRegistry;
        this.mavenProjectManager = mavenProjectManager;
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
            mavenServer.dispose();
        }
    }

    @GET
    @Path("download/sources")
    @Produces("text/plain")
    public String downloadSource(@QueryParam("projectpath") String projectPath, @QueryParam("fqn") String fqn) {
        return Boolean.toString(classpathManager.downloadSources(projectPath, fqn));
    }

    @GET
    @Path("pom/reconsile")
    @Produces("application/json")
    public List<Problem> reconsilePom(@QueryParam("pompath") String pomPath) {
        VirtualFileEntry entry = null;
        List<Problem> result = new ArrayList<>();
        try {
            entry = cheProjectManager.getProjectsRoot().getChild(pomPath);
            if (entry == null) {
                return result;
            }
            Model.readFrom(entry.getVirtualFile());
            org.eclipse.che.api.vfs.Path path = entry.getPath();
            String pomContent = entry.getVirtualFile().getContentAsString();
            MavenProject mavenProject =
                    mavenProjectManager.findMavenProject(ResourcesPlugin.getWorkspace().getRoot().getProject(path.getParent().toString()));
            if (mavenProject != null) {
                List<MavenProjectProblem> problems = mavenProject.getProblems();
                int start = pomContent.indexOf("<project ") + 1;
                int end = start + "<project ".length();
                List<Problem> problemList = problems.stream().map(mavenProjectProblem -> {
                    Problem problem = DtoFactory.newDto(Problem.class);
                    problem.setError(true);
                    problem.setSourceStart(start);
                    problem.setSourceEnd(end);
                    problem.setMessage(mavenProjectProblem.getDescription());
                    return problem;
                }).collect(Collectors.toList());

                List<Problem> missedArtifacts =
                        mavenProject.getDependencies().stream()
                                    .filter(mavenArtifact -> !mavenArtifact.isResolved())
                                    .map(artifact -> {
                                        Problem problem = DtoFactory.newDto(Problem.class);
                                        problem.setError(true);
                                        problem.setSourceStart(start);
                                        problem.setSourceEnd(end);
                                        problem.setMessage("Dependency " + artifact.getDisplayString() + " not found.");
                                        return problem;
                                    }).collect(Collectors.toList());

                result.addAll(missedArtifacts);
                result.addAll(problemList);
            }
        } catch (ServerException | ForbiddenException | IOException e) {
            LOG.error(e.getMessage(), e);
        } catch (XMLTreeException exception) {
            Throwable cause = exception.getCause();
            if (cause != null && cause instanceof SAXParseException) {
                result.add(createProblem(entry, (SAXParseException)cause));
            }
        }
        return result;

    }

    private Problem createProblem(VirtualFileEntry entry, SAXParseException spe) {
        Problem problem = DtoFactory.newDto(Problem.class);
        problem.setError(true);
        problem.setMessage(spe.getMessage());
        if (entry != null) {
            int lineNumber = spe.getLineNumber();
            int columnNumber = spe.getColumnNumber();
            try {
                String content = entry.getVirtualFile().getContentAsString();
                Document document = new Document(content);
                int lineOffset = document.getLineOffset(lineNumber - 1);
                problem.setSourceStart(lineOffset + columnNumber - 1);
                problem.setSourceEnd(lineOffset + columnNumber);
            } catch (ForbiddenException | ServerException | BadLocationException e) {
                LOG.error(e.getMessage(), e);
            }

        }
        return problem;
    }

}
