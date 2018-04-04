/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.java.client.editor;

import static org.eclipse.che.ide.ext.java.client.util.JavaUtil.resolveFQN;
import static org.eclipse.che.ide.project.ResolvingProjectStateHolder.ResolvingProjectState.IN_PROGRESS;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.editor.EditorWithErrors;
import org.eclipse.che.ide.api.editor.annotation.AnnotationModel;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.reconciler.DirtyRegion;
import org.eclipse.che.ide.api.editor.reconciler.ReconcilingStrategy;
import org.eclipse.che.ide.api.editor.text.Region;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.editor.ReconcileOperationEvent.ReconcileOperationHandler;
import org.eclipse.che.ide.ext.java.client.project.classpath.ClasspathChangedEvent;
import org.eclipse.che.ide.ext.java.client.project.classpath.ClasspathChangedEvent.ClasspathChangedHandler;
import org.eclipse.che.ide.ext.java.shared.dto.HighlightedPosition;
import org.eclipse.che.ide.ext.java.shared.dto.Problem;
import org.eclipse.che.ide.ext.java.shared.dto.ReconcileResult;
import org.eclipse.che.ide.project.ResolvingProjectStateHolder;
import org.eclipse.che.ide.project.ResolvingProjectStateHolder.ResolvingProjectState;
import org.eclipse.che.ide.project.ResolvingProjectStateHolder.ResolvingProjectStateListener;
import org.eclipse.che.ide.project.ResolvingProjectStateHolderRegistry;
import org.eclipse.che.ide.util.loging.Log;

public class JavaReconcilerStrategy
    implements ReconcilingStrategy,
        ResolvingProjectStateListener,
        ReconcileOperationHandler,
        ClasspathChangedHandler {
  private final TextEditor editor;
  private final JavaCodeAssistProcessor codeAssistProcessor;
  private final AnnotationModel annotationModel;
  private final SemanticHighlightRenderer highlighter;
  private final ResolvingProjectStateHolderRegistry resolvingProjectStateHolderRegistry;
  private final JavaLocalizationConstant localizationConstant;
  private final EventBus eventBus;
  private final JavaReconcileClient client;

  private EditorWithErrors editorWithErrors;
  private ResolvingProjectStateHolder resolvingProjectStateHolder;
  private HashSet<HandlerRegistration> handlerRegistrations = new HashSet<>(2);

  @AssistedInject
  public JavaReconcilerStrategy(
      @Assisted @NotNull final TextEditor editor,
      @Assisted final JavaCodeAssistProcessor codeAssistProcessor,
      @Assisted final AnnotationModel annotationModel,
      final JavaReconcileClient client,
      final SemanticHighlightRenderer highlighter,
      final ResolvingProjectStateHolderRegistry resolvingProjectStateHolderRegistry,
      final JavaLocalizationConstant localizationConstant,
      final EventBus eventBus) {
    this.editor = editor;
    this.client = client;
    this.codeAssistProcessor = codeAssistProcessor;
    this.annotationModel = annotationModel;
    this.highlighter = highlighter;
    this.resolvingProjectStateHolderRegistry = resolvingProjectStateHolderRegistry;
    this.localizationConstant = localizationConstant;
    this.eventBus = eventBus;
    if (editor instanceof EditorWithErrors) {
      this.editorWithErrors = ((EditorWithErrors) editor);
    }

    HandlerRegistration reconcileOperationHandlerRegistration =
        eventBus.addHandler(ReconcileOperationEvent.TYPE, this);
    handlerRegistrations.add(reconcileOperationHandlerRegistration);

    HandlerRegistration classpathChangedHandlerRegistration =
        eventBus.addHandler(ClasspathChangedEvent.TYPE, this);
    handlerRegistrations.add(classpathChangedHandlerRegistration);
  }

  @Override
  public void setDocument(final Document document) {
    highlighter.init(editor.getEditorWidget(), document);

    VirtualFile file = getFile();
    Project project = getProject(file);
    if (project == null) {
      return;
    }

    String projectType = project.getType();
    resolvingProjectStateHolder =
        resolvingProjectStateHolderRegistry.getResolvingProjectStateHolder(projectType);
    if (resolvingProjectStateHolder == null) {
      return;
    }
    resolvingProjectStateHolder.addResolvingProjectStateListener(this);

    if (isProjectResolving()) {
      disableReconciler(localizationConstant.codeAssistErrorMessageResolvingProject());
    }
  }

  @Override
  public void reconcile(final DirtyRegion dirtyRegion, final Region subRegion) {
    parse();
  }

  void parse() {
    VirtualFile file = getFile();
    Project project = getProject(file);
    if (project == null) {
      return;
    }

    String fqn = resolveFQN(file);
    String projectPath = project.getPath();

    client
        .reconcile(fqn, projectPath)
        .onSuccess(
            reconcileResult -> {
              if (isProjectResolving()) {
                disableReconciler(localizationConstant.codeAssistErrorMessageResolvingProject());
                return;
              } else {
                codeAssistProcessor.enableCodeAssistant();
              }

              if (reconcileResult == null) {
                return;
              }

              doReconcile(reconcileResult.getProblems());
              highlighter.reconcile(reconcileResult.getHighlightedPositions());
              eventBus.fireEvent(new JavaReconsilerEvent(editor));
            })
        .onFailure(jsonRpcError -> Log.info(getClass(), jsonRpcError.getMessage()));
  }

  @Override
  public void reconcile(final Region partition) {
    parse();
  }

  private VirtualFile getFile() {
    return editor.getEditorInput().getFile();
  }

  private Project getProject(VirtualFile file) {
    if (file == null || !(file instanceof Resource)) {
      return null;
    }

    Project project = ((Resource) file).getProject();
    return (project != null && project.exists()) ? project : null;
  }

  private void doReconcile(final List<Problem> problems) {
    if (this.annotationModel == null) {
      return;
    }
    ProblemRequester problemRequester;
    if (this.annotationModel instanceof ProblemRequester) {
      problemRequester = (ProblemRequester) this.annotationModel;
      problemRequester.beginReporting();
    } else {
      if (editorWithErrors != null) {
        editorWithErrors.setErrorState(EditorWithErrors.EditorState.NONE);
      }
      return;
    }
    try {
      boolean error = false;
      boolean warning = false;
      for (Problem problem : problems) {

        if (!error) {
          error = problem.isError();
        }
        if (!warning) {
          warning = problem.isWarning();
        }
        problemRequester.acceptProblem(problem);
      }
      if (editorWithErrors != null) {
        if (error) {
          editorWithErrors.setErrorState(EditorWithErrors.EditorState.ERROR);
        } else if (warning) {
          editorWithErrors.setErrorState(EditorWithErrors.EditorState.WARNING);
        } else {
          editorWithErrors.setErrorState(EditorWithErrors.EditorState.NONE);
        }
      }
    } catch (final Exception e) {
      Log.error(getClass(), e);
    } finally {
      problemRequester.endReporting();
    }
  }

  private void disableReconciler(String errorMessage) {
    codeAssistProcessor.disableCodeAssistant(errorMessage);
    doReconcile(Collections.<Problem>emptyList());
    highlighter.reconcile(Collections.<HighlightedPosition>emptyList());
  }

  @Override
  public void closeReconciler() {
    if (resolvingProjectStateHolder != null) {
      resolvingProjectStateHolder.removeResolvingProjectStateListener(this);
    }
    handlerRegistrations.forEach(HandlerRegistration::removeHandler);
  }

  @Override
  public void onResolvingProjectStateChanged(ResolvingProjectState state) {
    switch (state) {
      case IN_PROGRESS:
        disableReconciler(localizationConstant.codeAssistErrorMessageResolvingProject());
        break;
      case RESOLVED:
        parse();
        break;
      default:
        break;
    }
  }

  private boolean isProjectResolving() {
    return resolvingProjectStateHolder != null
        && resolvingProjectStateHolder.getState() == IN_PROGRESS;
  }

  @Override
  public void onReconcileOperation(ReconcileResult reconcileResult) {
    String currentEditorPath = editor.getEditorInput().getFile().getLocation().toString();
    if (!currentEditorPath.equals(reconcileResult.getFileLocation())) {
      return;
    }

    if (isProjectResolving()) {
      disableReconciler(localizationConstant.codeAssistErrorMessageResolvingProject());
      return;
    } else {
      codeAssistProcessor.enableCodeAssistant();
    }

    doReconcile(reconcileResult.getProblems());
    highlighter.reconcile(reconcileResult.getHighlightedPositions());
  }

  @Override
  public void onClasspathChanged(ClasspathChangedEvent event) {
    VirtualFile file = getFile();
    Project project = getProject(file);
    if (project == null) {
      return;
    }

    String projectPath = project.getPath();
    if (projectPath.equals(event.getPath())) {
      parse();
    }
  }
}
