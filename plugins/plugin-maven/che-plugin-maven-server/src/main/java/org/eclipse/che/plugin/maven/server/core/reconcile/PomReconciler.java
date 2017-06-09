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
package org.eclipse.che.plugin.maven.server.core.reconcile;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.project.server.EditorWorkingCopy;
import org.eclipse.che.api.project.server.EditorWorkingCopyManager;
import org.eclipse.che.api.project.server.EditorWorkingCopyUpdatedEvent;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.VirtualFileEntry;
import org.eclipse.che.api.project.shared.dto.EditorChangesDto;
import org.eclipse.che.api.project.shared.dto.ServerError;
import org.eclipse.che.commons.xml.XMLTreeException;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.ide.ext.java.shared.dto.Problem;
import org.eclipse.che.ide.ext.java.shared.dto.ReconcileResult;
import org.eclipse.che.ide.maven.tools.Model;
import org.eclipse.che.maven.data.MavenProjectProblem;
import org.eclipse.che.plugin.maven.server.core.MavenProjectManager;
import org.eclipse.che.plugin.maven.server.core.project.MavenProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXParseException;

import javax.annotation.PreDestroy;
import javax.inject.Provider;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.nio.charset.Charset.defaultCharset;
import static org.eclipse.che.maven.data.MavenConstants.POM_FILE_NAME;

/**
 * Handles reconcile operations for pom.xml file.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class PomReconciler {
    private static final Logger LOG                            = LoggerFactory.getLogger(PomReconciler.class);
    private static final String RECONCILE_ERROR_METHOD         = "event:pom-reconcile-error";
    private static final String RECONCILE_STATE_CHANGED_METHOD = "event:pom-reconcile-state-changed";

    private Provider<ProjectManager>                       projectManagerProvider;
    private MavenProjectManager                            mavenProjectManager;
    private EditorWorkingCopyManager                       editorWorkingCopyManager;
    private EventService                                   eventService;
    private RequestTransmitter                             transmitter;
    private EventSubscriber<EditorWorkingCopyUpdatedEvent> editorContentUpdateEventSubscriber;

    @Inject
    public PomReconciler(Provider<ProjectManager> projectManagerProvider,
                         MavenProjectManager mavenProjectManager,
                         EditorWorkingCopyManager editorWorkingCopyManager,
                         EventService eventService,
                         RequestTransmitter transmitter) {
        this.projectManagerProvider = projectManagerProvider;
        this.mavenProjectManager = mavenProjectManager;
        this.editorWorkingCopyManager = editorWorkingCopyManager;
        this.eventService = eventService;
        this.transmitter = transmitter;

        editorContentUpdateEventSubscriber = new EventSubscriber<EditorWorkingCopyUpdatedEvent>() {
            @Override
            public void onEvent(EditorWorkingCopyUpdatedEvent event) {
                onEditorContentUpdated(event);
            }
        };
        eventService.subscribe(editorContentUpdateEventSubscriber);
    }

    @PreDestroy
    private void unsubscribe() {
        eventService.unsubscribe(editorContentUpdateEventSubscriber);
    }

    /**
     * Handles reconcile operations for pom.xml file by given path.
     *
     * @param pomPath
     *         path to the pom file to reconcile
     * @return result of reconcile operation as a list of {@link Problem}s
     * @throws NotFoundException
     *         if file is not found by given {@code pomPath}
     * @throws ForbiddenException
     *         if item is not a file
     * @throws ServerException
     *         if other error occurs
     */
    public List<Problem> reconcile(String pomPath) throws ServerException, ForbiddenException, NotFoundException {
        VirtualFileEntry entry = projectManagerProvider.get().getProjectsRoot().getChild(pomPath);
        if (entry == null) {
            throw new NotFoundException(format("File '%s' doesn't exist", pomPath));
        }

        EditorWorkingCopy workingCopy = editorWorkingCopyManager.getWorkingCopy(pomPath);
        String pomContent = workingCopy != null ? workingCopy.getContentAsString() : entry.getVirtualFile().getContentAsString();
        String projectPath = entry.getPath().getParent().toString();

        return reconcile(pomPath, projectPath, pomContent);
    }

    private List<Problem> reconcile(String pomPath, String projectPath, String pomContent) throws ServerException, NotFoundException {
        List<Problem> result = new ArrayList<>();

        if (isNullOrEmpty(pomContent)) {
            throw new ServerException(format("Couldn't reconcile pom file '%s' because its content is empty", pomPath));
        }

        try {
            Model.readFrom(new ByteArrayInputStream(pomContent.getBytes(defaultCharset())));

            IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectPath);
            MavenProject mavenProject = mavenProjectManager.findMavenProject(project);
            if (mavenProject == null) {
                return result;
            }

            List<MavenProjectProblem> problems = mavenProject.getProblems();

            int start = pomContent.indexOf("<project ") + 1;
            int end = start + "<project ".length();

            List<Problem> problemList = problems.stream().map(mavenProjectProblem -> DtoFactory.newDto(Problem.class)
                                                                                               .withError(true)
                                                                                               .withSourceStart(start)
                                                                                               .withSourceEnd(end)
                                                                                               .withMessage(mavenProjectProblem
                                                                                                                    .getDescription()))
                                                .collect(Collectors.toList());
            result.addAll(problemList);
        } catch (XMLTreeException exception) {
            Throwable cause = exception.getCause();
            if (cause != null && cause instanceof SAXParseException) {
                result.add(createProblem(pomContent, (SAXParseException)cause));

            } else {
                String error = format("Couldn't reconcile pom file '%s', the reason is '%s'", pomPath, exception.getLocalizedMessage());
                LOG.error(error, exception);
                throw new ServerException(error);
            }
        } catch (IOException e) {
            String error = format("Couldn't reconcile pom file '%s', the reason is '%s'", pomPath, e.getLocalizedMessage());
            LOG.error(error, e);
            throw new ServerException(error);
        }
        return result;
    }

    private void onEditorContentUpdated(EditorWorkingCopyUpdatedEvent event) {
        EditorChangesDto editorChanges = event.getChanges();
        String endpointId = event.getEndpointId();
        String fileLocation = editorChanges.getFileLocation();
        String fileName = new Path(fileLocation).lastSegment();
        if (!POM_FILE_NAME.equals(fileName)) {
            return;
        }

        try {
            EditorWorkingCopy workingCopy = editorWorkingCopyManager.getWorkingCopy(fileLocation);
            if (workingCopy == null) {
                return;
            }

            String newPomContent = workingCopy.getContentAsString();
            if (isNullOrEmpty(newPomContent)) {
                return;
            }

            String projectPath = editorChanges.getProjectPath();
            List<Problem> problemList = reconcile(fileLocation, projectPath, newPomContent);
            DtoFactory dtoFactory = DtoFactory.getInstance();
            ReconcileResult reconcileResult = dtoFactory.createDto(ReconcileResult.class)
                                                        .withFileLocation(fileLocation)
                                                        .withProblems(problemList);
            transmitter.newRequest()
                       .endpointId(endpointId)
                       .methodName(RECONCILE_STATE_CHANGED_METHOD)
                       .paramsAsDto(reconcileResult)
                       .sendAndSkipResult();
        } catch (Exception e) {
            String error = e.getLocalizedMessage();
            LOG.error(error, e);
            transmitError(500, error, endpointId);
        }
    }

    private void transmitError(int code, String errorMessage, String endpointId) {
        DtoFactory dtoFactory = DtoFactory.getInstance();
        ServerError reconcileError = dtoFactory.createDto(ServerError.class)
                                               .withCode(code)
                                               .withMessage(errorMessage);
        transmitter.newRequest()
                   .endpointId(endpointId)
                   .methodName(RECONCILE_ERROR_METHOD)
                   .paramsAsDto(reconcileError)
                   .sendAndSkipResult();
    }

    private Problem createProblem(String pomContent, SAXParseException spe) {
        Problem problem = DtoFactory.newDto(Problem.class);
        problem.setError(true);
        problem.setMessage(spe.getMessage());
        if (pomContent != null) {
            int lineNumber = spe.getLineNumber();
            int columnNumber = spe.getColumnNumber();
            try {
                Document document = new Document(pomContent);
                int lineOffset = document.getLineOffset(lineNumber - 1);
                problem.setSourceStart(lineOffset + columnNumber - 1);
                problem.setSourceEnd(lineOffset + columnNumber);
            } catch (BadLocationException e) {
                LOG.error(e.getMessage(), e);
            }

        }
        return problem;
    }
}
