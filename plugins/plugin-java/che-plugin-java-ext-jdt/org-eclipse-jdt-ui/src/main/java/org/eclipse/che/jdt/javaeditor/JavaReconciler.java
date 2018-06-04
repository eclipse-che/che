/**
 * ***************************************************************************** Copyright (c)
 * 2012-2015 Red Hat, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Red Hat, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.jdt.javaeditor;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.eclipse.che.jdt.javaeditor.JavaReconciler.Mode.ACTIVATED;
import static org.eclipse.che.jdt.javaeditor.JavaReconciler.Mode.DEACTIVATED;
import static org.eclipse.jdt.core.IJavaElement.COMPILATION_UNIT;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PreDestroy;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.editor.server.impl.EditorWorkingCopy;
import org.eclipse.che.api.editor.server.impl.EditorWorkingCopyManager;
import org.eclipse.che.api.editor.server.impl.EditorWorkingCopyUpdatedEvent;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.shared.dto.EditorChangesDto;
import org.eclipse.che.api.project.shared.dto.ServerError;
import org.eclipse.che.api.project.shared.dto.event.FileTrackingOperationDto;
import org.eclipse.che.api.project.shared.dto.event.FileTrackingOperationDto.Type;
import org.eclipse.che.api.watcher.server.detectors.FileTrackingOperationEvent;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.ide.ext.java.shared.dto.HighlightedPosition;
import org.eclipse.che.ide.ext.java.shared.dto.Problem;
import org.eclipse.che.ide.ext.java.shared.dto.ReconcileResult;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.core.ClassFileWorkingCopy;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.ui.javaeditor.DocumentAdapter;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Evgen Vidolob
 * @author Roman Nikitenko
 */
@Singleton
public class JavaReconciler {

  private static final Logger LOG = LoggerFactory.getLogger(JavaReconciler.class);
  private static final JavaModel JAVA_MODEL = JavaModelManager.getJavaModelManager().getJavaModel();
  private static final String RECONCILE_ERROR_METHOD = "event:java-reconcile-error";
  private static final String RECONCILE_STATE_CHANGED_METHOD = "event:java-reconcile-state-changed";

  private final List<EventSubscriber> subscribers = new ArrayList<>(2);

  private final EventService eventService;
  private final RequestTransmitter transmitter;
  private final ProjectManager projectManager;
  private final EditorWorkingCopyManager editorWorkingCopyManager;
  private final SemanticHighlightingReconciler semanticHighlighting;

  private Mode mode = ACTIVATED;

  @Inject
  public JavaReconciler(
      SemanticHighlightingReconciler semanticHighlighting,
      EventService eventService,
      RequestTransmitter transmitter,
      ProjectManager projectManager,
      EditorWorkingCopyManager editorWorkingCopyManager) {
    this.semanticHighlighting = semanticHighlighting;
    this.eventService = eventService;
    this.transmitter = transmitter;
    this.projectManager = projectManager;
    this.editorWorkingCopyManager = editorWorkingCopyManager;

    EventSubscriber<FileTrackingOperationEvent> fileOperationEventSubscriber =
        new EventSubscriber<FileTrackingOperationEvent>() {
          @Override
          public void onEvent(FileTrackingOperationEvent event) {
            onFileOperation(event.getEndpointId(), event.getFileTrackingOperation());
          }
        };
    eventService.subscribe(fileOperationEventSubscriber);
    subscribers.add(fileOperationEventSubscriber);

    EventSubscriber<EditorWorkingCopyUpdatedEvent> editorContentUpdateEventSubscriber =
        new EventSubscriber<EditorWorkingCopyUpdatedEvent>() {
          @Override
          public void onEvent(EditorWorkingCopyUpdatedEvent event) {
            onEditorContentUpdated(event);
          }
        };
    eventService.subscribe(editorContentUpdateEventSubscriber);
    subscribers.add(editorContentUpdateEventSubscriber);
  }

  @PreDestroy
  private void unsubscribe() {
    subscribers.forEach(eventService::unsubscribe);
  }

  public ReconcileResult reconcile(IJavaProject javaProject, String fqn) throws JavaModelException {
    IType type = getType(fqn, javaProject);
    ICompilationUnit compilationUnit = type.getCompilationUnit();

    return reconcile(compilationUnit, javaProject);
  }

  private ReconcileResult reconcile(ICompilationUnit compilationUnit, IJavaProject javaProject)
      throws JavaModelException {
    ICompilationUnit workingCopy = null;
    List<HighlightedPosition> positions;
    String filePath = compilationUnit.getPath().toString();

    final ProblemRequestor problemRequestor = new ProblemRequestor();
    final WorkingCopyOwner wcOwner = createWorkingCopyOwner(problemRequestor);

    try {
      workingCopy = compilationUnit.getWorkingCopy(wcOwner, null);
      synchronizeWorkingCopyContent(filePath, workingCopy);
      problemRequestor.reset();

      CompilationUnit unit = workingCopy.reconcile(AST.JLS8, true, wcOwner, null);
      positions = semanticHighlighting.reconcileSemanticHighlight(unit);

      if (workingCopy instanceof ClassFileWorkingCopy) {
        // we don't wont to show any errors from ".class" files
        problemRequestor.reset();
      }
    } catch (JavaModelException e) {
      LOG.error(
          format(
              "Can't reconcile class: %s in project: %s",
              filePath, javaProject.getPath().toOSString()),
          e);
      throw e;
    } finally {
      if (workingCopy != null && workingCopy.isWorkingCopy()) {
        try {
          workingCopy.getBuffer().close();
          workingCopy.discardWorkingCopy();
        } catch (JavaModelException e) {
          // ignore
        }
      }
    }

    DtoFactory dtoFactory = DtoFactory.getInstance();
    return dtoFactory
        .createDto(ReconcileResult.class)
        .withFileLocation(compilationUnit.getPath().toOSString())
        .withProblems(convertProblems(problemRequestor.problems))
        .withHighlightedPositions(positions);
  }

  private void synchronizeWorkingCopyContent(String filePath, ICompilationUnit workingCopy)
      throws JavaModelException {
    EditorWorkingCopy editorWorkingCopy = editorWorkingCopyManager.getWorkingCopy(filePath);
    if (editorWorkingCopy == null) {
      return;
    }

    String oldContent = workingCopy.getBuffer().getContents();
    String newContent = editorWorkingCopy.getContentAsString();

    TextEdit textEdit = new ReplaceEdit(0, oldContent.length(), newContent);
    workingCopy.applyTextEdit(textEdit, null);
  }

  private void onEditorContentUpdated(EditorWorkingCopyUpdatedEvent event) {
    if (mode == DEACTIVATED) {
      return;
    }

    String endpointId = event.getEndpointId();
    EditorChangesDto editorChanges = event.getChanges();
    String filePath = editorChanges.getFileLocation();
    String projectPath = editorChanges.getProjectPath();

    reconcileAndTransmit(filePath, projectPath, endpointId);
  }

  private void onFileOperation(String endpointId, FileTrackingOperationDto operation) {
    try {
      Type operationType = operation.getType();
      switch (operationType) {
        case START:
          {
            String filePath = operation.getPath();
            ProjectConfig project =
                projectManager
                    .getClosest(filePath)
                    .orElseThrow(
                        () -> new NotFoundException("The file is not found by path " + filePath));

            String projectPath = project.getPath();
            if (isNullOrEmpty(projectPath)) {
              throw new NotFoundException("The project is not recognized for " + filePath);
            }

            reconcileAndTransmit(filePath, projectPath, endpointId);
            break;
          }

        case SUSPEND:
          {
            mode = DEACTIVATED;
            break;
          }

        case RESUME:
          {
            mode = ACTIVATED;
            break;
          }

        default:
          {
            break;
          }
      }
    } catch (NotFoundException e) {
      String errorMessage = "Can not handle file operation: " + e.getMessage();

      LOG.error(errorMessage);

      transmitError(400, errorMessage, endpointId);
    }
  }

  private void reconcileAndTransmit(String filePath, String projectPath, String endpointId) {
    ICompilationUnit compilationUnit;
    try {
      compilationUnit = getCompilationUnit(filePath, projectPath);
      if (compilationUnit == null) {
        return;
      }
    } catch (JavaModelException e) {
      return; // ignore - we haven't compilation unit to reconcile
    }

    try {
      ReconcileResult reconcileResult = reconcile(compilationUnit, getJavaProject(projectPath));
      transmitter
          .newRequest()
          .endpointId(endpointId)
          .methodName(RECONCILE_STATE_CHANGED_METHOD)
          .paramsAsDto(reconcileResult)
          .sendAndSkipResult();
    } catch (JavaModelException e) {
      String errorMessage =
          format(
              "Can't reconcile class: %s in project: %s, the reason is %s",
              filePath, projectPath, e.getLocalizedMessage());

      LOG.error(errorMessage);

      transmitError(500, errorMessage, endpointId);
    }
  }

  private void transmitError(int code, String errorMessage, String endpointId) {
    DtoFactory dtoFactory = DtoFactory.getInstance();
    ServerError reconcileError =
        dtoFactory.createDto(ServerError.class).withCode(code).withMessage(errorMessage);
    transmitter
        .newRequest()
        .endpointId(endpointId)
        .methodName(RECONCILE_ERROR_METHOD)
        .paramsAsDto(reconcileError)
        .sendAndSkipResult();
  }

  @Nullable
  private ICompilationUnit getCompilationUnit(String filePath, String projectPath)
      throws JavaModelException {
    IJavaProject javaProject = getJavaProject(projectPath);
    if (javaProject == null) {
      return null;
    }

    List<IClasspathEntry> classpathEntries = asList(javaProject.getRawClasspath());
    for (IClasspathEntry classpathEntry : classpathEntries) {
      String entryPath = classpathEntry.getPath().toString();
      if (!filePath.contains(entryPath)) {
        continue;
      }

      String fileRelativePath = filePath.substring(entryPath.length() + 1);
      IJavaElement javaElement = javaProject.findElement(new Path(fileRelativePath));
      if (javaElement == null) {
        continue;
      }

      int elementType = javaElement.getElementType();
      if (COMPILATION_UNIT == elementType) {
        return (ICompilationUnit) javaElement;
      }
    }
    return null;
  }

  @Nullable
  private IJavaProject getJavaProject(String projectPath) throws JavaModelException {
    IJavaProject project = JAVA_MODEL.getJavaProject(projectPath);
    List<IJavaProject> javaProjects = asList(JAVA_MODEL.getJavaProjects());

    return javaProjects.contains(project) ? project : null;
  }

  private WorkingCopyOwner createWorkingCopyOwner(ProblemRequestor problemRequestor) {
    return new WorkingCopyOwner() {
      public IProblemRequestor getProblemRequestor(ICompilationUnit unit) {
        return problemRequestor;
      }

      @Override
      public IBuffer createBuffer(ICompilationUnit workingCopy) {
        return new DocumentAdapter(workingCopy, (IFile) workingCopy.getResource());
      }
    };
  }

  private List<Problem> convertProblems(List<IProblem> problems) {
    List<Problem> result = new ArrayList<>(problems.size());
    for (IProblem problem : problems) {
      result.add(convertProblem(problem));
    }
    return result;
  }

  private Problem convertProblem(IProblem problem) {
    Problem result = DtoFactory.getInstance().createDto(Problem.class);

    result.setArguments(asList(problem.getArguments()));
    result.setID(problem.getID());
    result.setMessage(problem.getMessage());
    result.setOriginatingFileName(new String(problem.getOriginatingFileName()));
    result.setError(problem.isError());
    result.setWarning(problem.isWarning());
    result.setSourceEnd(problem.getSourceEnd());
    result.setSourceStart(problem.getSourceStart());
    result.setSourceLineNumber(problem.getSourceLineNumber());

    return result;
  }

  private IType getType(String fqn, IJavaProject javaProject) throws JavaModelException {
    checkState(!isNullOrEmpty(fqn), "Incorrect fully qualified name is specified");

    final IType type = javaProject.findType(fqn);
    if (type == null) {
      throw new JavaModelException(new Throwable("Can not find type for " + fqn), 500);
    }

    if (type.isBinary()) {
      throw new JavaModelException(new Throwable("Can't reconcile binary type: " + fqn), 500);
    }
    return type;
  }

  enum Mode {
    /** The state when the reconciler is turned on. */
    ACTIVATED,
    /**
     * The state when the reconciler is turned off for processing reconcile operation (while java
     * refactoring in progress, for example)
     */
    DEACTIVATED
  }

  private class ProblemRequestor implements IProblemRequestor {

    private List<IProblem> problems = new ArrayList<>();

    @Override
    public void acceptProblem(IProblem problem) {
      problems.add(problem);
    }

    @Override
    public void beginReporting() {}

    @Override
    public void endReporting() {}

    @Override
    public boolean isActive() {
      return true;
    }

    public void reset() {
      problems.clear();
    }
  }
}
