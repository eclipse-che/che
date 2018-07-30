/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.maven.server.core.reconcile;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.nio.charset.Charset.defaultCharset;
import static org.eclipse.che.maven.data.MavenConstants.POM_FILE_NAME;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.editor.server.impl.EditorWorkingCopy;
import org.eclipse.che.api.editor.server.impl.EditorWorkingCopyManager;
import org.eclipse.che.api.editor.server.impl.EditorWorkingCopyUpdatedEvent;
import org.eclipse.che.api.languageserver.CheLanguageClientFactory;
import org.eclipse.che.api.languageserver.LanguageServiceUtils;
import org.eclipse.che.api.project.shared.dto.EditorChangesDto;
import org.eclipse.che.commons.xml.XMLTreeException;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.ide.ext.java.shared.dto.Problem;
import org.eclipse.che.ide.maven.tools.Model;
import org.eclipse.che.maven.data.MavenProjectProblem;
import org.eclipse.che.plugin.maven.server.core.MavenProjectManager;
import org.eclipse.che.plugin.maven.server.core.project.MavenProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.services.LanguageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXParseException;

/**
 * Handles reconcile operations for pom.xml file.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class PomReconciler {
  private static final Logger LOG = LoggerFactory.getLogger(PomReconciler.class);

  private MavenProjectManager mavenProjectManager;
  private EditorWorkingCopyManager editorWorkingCopyManager;
  private EventService eventService;
  private EventSubscriber<EditorWorkingCopyUpdatedEvent> editorContentUpdateEventSubscriber;
  private LanguageClient client;

  @Inject
  public PomReconciler(
      MavenProjectManager mavenProjectManager,
      EditorWorkingCopyManager editorWorkingCopyManager,
      EventService eventService,
      CheLanguageClientFactory cheLanguageClientFactory) {
    this.mavenProjectManager = mavenProjectManager;
    this.editorWorkingCopyManager = editorWorkingCopyManager;
    this.eventService = eventService;
    this.client = cheLanguageClientFactory.create("maven-language-server");

    editorContentUpdateEventSubscriber =
        new EventSubscriber<EditorWorkingCopyUpdatedEvent>() {
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

  List<Problem> reconcile(String pomPath, String projectPath, String pomContent)
      throws ServerException, NotFoundException {
    List<Problem> result = new ArrayList<>();

    if (isNullOrEmpty(pomContent)) {
      throw new ServerException(
          format("Couldn't reconcile pom file '%s' because its content is empty", pomPath));
    }

    try {
      Model.readFrom(new ByteArrayInputStream(pomContent.getBytes(defaultCharset())));

      if (isNullOrEmpty(projectPath)) {
        return result;
      }

      IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectPath);
      MavenProject mavenProject = mavenProjectManager.findMavenProject(project);
      if (mavenProject == null) {
        return result;
      }

      List<MavenProjectProblem> problems = mavenProject.getProblems();

      int start = pomContent.indexOf("<project ") + 1;
      int end = start + "<project ".length();

      List<Problem> problemList =
          problems
              .stream()
              .map(
                  mavenProjectProblem ->
                      DtoFactory.newDto(Problem.class)
                          .withError(true)
                          .withSourceStart(start)
                          .withSourceEnd(end)
                          .withMessage(mavenProjectProblem.getDescription()))
              .collect(Collectors.toList());
      result.addAll(problemList);
    } catch (XMLTreeException exception) {
      Throwable cause = exception.getCause();
      if (cause instanceof SAXParseException) {
        result.add(createProblem(pomContent, (SAXParseException) cause));

      } else {
        String error =
            format(
                "Couldn't reconcile pom file '%s', the reason is '%s'",
                pomPath, exception.getLocalizedMessage());
        LOG.error(error, exception);
        throw new ServerException(error);
      }
    } catch (IOException e) {
      String error =
          format(
              "Couldn't reconcile pom file '%s', the reason is '%s'",
              pomPath, e.getLocalizedMessage());
      LOG.error(error, e);
      throw new ServerException(error);
    }
    return result;
  }

  private void onEditorContentUpdated(EditorWorkingCopyUpdatedEvent event) {
    EditorChangesDto editorChanges = event.getChanges();
    String fileLocation = editorChanges.getFileLocation();
    String projectPath = editorChanges.getProjectPath();
    reconcilePath(fileLocation, projectPath);
  }

  public void reconcilePath(String fileLocation, String projectPath) {
    String fileName = new Path(fileLocation).lastSegment();
    if (!POM_FILE_NAME.equals(fileName)) {
      return;
    }

    EditorWorkingCopy workingCopy = editorWorkingCopyManager.getWorkingCopy(fileLocation);
    if (workingCopy == null) {
      return;
    }

    String newPomContent = workingCopy.getContentAsString();
    if (isNullOrEmpty(newPomContent)) {
      return;
    }

    List<Problem> problems;
    try {
      problems = reconcile(fileLocation, projectPath, newPomContent);
      List<Diagnostic> diagnostics = convertProblems(newPomContent, problems);
      client.publishDiagnostics(
          new PublishDiagnosticsParams(LanguageServiceUtils.prefixURI(fileLocation), diagnostics));
    } catch (ServerException | NotFoundException e) {
      LOG.error(e.getMessage(), e);
      client.showMessage(new MessageParams(MessageType.Error, "Error reconciling " + fileLocation));
    }
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

  public void reconcileUri(String uri, String text) {
    try {
      String pomPath = LanguageServiceUtils.removePrefixUri(uri);
      List<Problem> problems = reconcile(pomPath, new File(pomPath).getParent(), text);
      List<Diagnostic> diagnostics = convertProblems(text, problems);
      client.publishDiagnostics(new PublishDiagnosticsParams(uri, diagnostics));
    } catch (ServerException | NotFoundException e) {
      LOG.error("Error reconciling content: " + uri, e);
      client.showMessage(new MessageParams(MessageType.Error, "Error reconciling " + uri));
    }
  }

  private static List<Diagnostic> convertProblems(String text, List<Problem> problems) {
    Map<Integer, Position> positions = mapPositions(text, problems);
    return problems
        .stream()
        .map(p -> convertProblem(positions, p))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  private static Map<Integer, Position> mapPositions(String text, List<Problem> problems) {
    SortedSet<Integer> offsets = new TreeSet<>();
    for (Problem problem : problems) {
      offsets.add(problem.getSourceStart());
      offsets.add(problem.getSourceEnd());
    }
    Map<Integer, Position> result = new HashMap<>();
    int line = 0;
    int character = 0;
    int pos = 0;
    for (int offset : offsets) {
      while (pos < offset && pos < text.length()) {
        char ch = text.charAt(pos++);
        if (ch == '\r') {
          if (text.charAt(pos) == '\n') {
            pos++;
          }
          line++;
          character = 0;
        } else if (ch == '\n') {
          line++;
          character = 0;
        } else {
          character++;
        }
      }
      result.put(offset, new Position(line, character));
    }
    ;
    return result;
  }

  private static Diagnostic convertProblem(Map<Integer, Position> positionMap, Problem problem) {
    Diagnostic result = new Diagnostic();
    Position start = positionMap.get(problem.getSourceStart());
    Position end = positionMap.get(problem.getSourceEnd());
    if (start == null || end == null) {
      LOG.error("Could not map problem range: " + problem);
      return null;
    }
    result.setRange(new Range(start, end));
    result.setMessage(problem.getMessage());
    result.setSeverity(problem.isError() ? DiagnosticSeverity.Error : DiagnosticSeverity.Warning);
    return result;
  }
}
