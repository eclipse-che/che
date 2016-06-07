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
package org.eclipse.che.ide.ext.java.client.editor;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

import org.eclipse.che.ide.api.editor.EditorWithErrors;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.api.editor.text.Region;
import org.eclipse.che.ide.ext.java.client.event.DependencyUpdatedEvent;
import org.eclipse.che.ide.ext.java.client.event.DependencyUpdatedEventHandler;
import org.eclipse.che.ide.ext.java.client.projecttree.JavaSourceFolderUtil;
import org.eclipse.che.ide.ext.java.shared.dto.Problem;
import org.eclipse.che.ide.ext.java.shared.dto.ReconcileResult;
import org.eclipse.che.ide.api.editor.annotation.AnnotationModel;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.reconciler.DirtyRegion;
import org.eclipse.che.ide.api.editor.reconciler.ReconcilingStrategy;
import org.eclipse.che.ide.api.editor.texteditor.TextEditorPresenter;
import org.eclipse.che.ide.util.loging.Log;

import javax.validation.constraints.NotNull;
import java.util.List;

public class JavaReconcilerStrategy implements ReconcilingStrategy {


    private final TextEditorPresenter<?>    editor;
    private final JavaCodeAssistProcessor   codeAssistProcessor;
    private final AnnotationModel           annotationModel;
    private final HandlerRegistration       handlerRegistration;
    private       SemanticHighlightRenderer highlighter;
    private       JavaReconcileClient       client;
    private       VirtualFile               file;
    private boolean first = true;

    @AssistedInject
    public JavaReconcilerStrategy(@Assisted @NotNull final TextEditorPresenter<?> editor,
                                  @Assisted final JavaCodeAssistProcessor codeAssistProcessor,
                                  @Assisted final AnnotationModel annotationModel,
                                  final JavaReconcileClient client,
                                  final SemanticHighlightRenderer highlighter,
                                  EventBus eventBus) {
        this.editor = editor;
        this.client = client;
        this.codeAssistProcessor = codeAssistProcessor;
        this.annotationModel = annotationModel;
        this.highlighter = highlighter;

        handlerRegistration = eventBus.addHandler(DependencyUpdatedEvent.TYPE, new DependencyUpdatedEventHandler() {
            @Override
            public void onDependencyUpdated() {
                parse();
            }
        });
    }

    @Override
    public void setDocument(final Document document) {
        file = editor.getEditorInput().getFile();
        highlighter.init(editor.getHasTextMarkers(), document);
    }

    @Override
    public void reconcile(final DirtyRegion dirtyRegion, final Region subRegion) {
        parse();
    }

    public void parse() {
        if (first) {
            codeAssistProcessor.disableCodeAssistant();
            first = false;
        }


        String fqn = JavaSourceFolderUtil.getFQNForFile(file);
        client.reconcile(file.getProject().getProjectConfig().getPath(), fqn, new JavaReconcileClient.ReconcileCallback() {
            @Override
            public void onReconcile(ReconcileResult result) {
                if (result == null) {
                    return;
                }
                doReconcile(result.getProblems());
                highlighter.reconcile(result.getHighlightedPositions());
            }
        });
    }


    @Override
    public void reconcile(final Region partition) {
        parse();
    }

    public VirtualFile getFile() {
        return file;
    }

    private void doReconcile(final List<Problem> problems) {
        if (!first) {
            codeAssistProcessor.enableCodeAssistant();
        }

        if (this.annotationModel == null) {
            return;
        }
        ProblemRequester problemRequester;
        if (this.annotationModel instanceof ProblemRequester) {
            problemRequester = (ProblemRequester)this.annotationModel;
            problemRequester.beginReporting();
        } else {
            editor.setErrorState(EditorWithErrors.EditorState.NONE);
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
            if (error) {
                editor.setErrorState(EditorWithErrors.EditorState.ERROR);
            } else if (warning) {
                editor.setErrorState(EditorWithErrors.EditorState.WARNING);
            } else {
                editor.setErrorState(EditorWithErrors.EditorState.NONE);
            }
        } catch (final Exception e) {
            Log.error(getClass(), e);
        } finally {
            problemRequester.endReporting();
        }
    }

    @Override
    public void closeReconciler() {
        if (handlerRegistration != null) {
            handlerRegistration.removeHandler();
        }
    }
}
