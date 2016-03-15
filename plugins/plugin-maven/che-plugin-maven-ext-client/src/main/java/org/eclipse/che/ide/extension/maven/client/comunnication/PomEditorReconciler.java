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
package org.eclipse.che.ide.extension.maven.client.comunnication;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorOpenedEvent;
import org.eclipse.che.ide.api.editor.EditorOpenedEventHandler;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.editor.ProblemRequester;
import org.eclipse.che.ide.ext.java.shared.dto.Problem;
import org.eclipse.che.ide.extension.maven.shared.dto.MavenProblem;
import org.eclipse.che.ide.jseditor.client.annotation.AnnotationModel;
import org.eclipse.che.ide.jseditor.client.document.Document;
import org.eclipse.che.ide.jseditor.client.texteditor.ConfigurableTextEditor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class PomEditorReconciler {

    private final EditorAgent                     editorAgent;
    private final DtoFactory dtoFactory;
    private       Map<String, List<MavenProblem>> pomToProblem;

    @Inject
    public PomEditorReconciler(EditorAgent editorAgent, DtoFactory dtoFactory, EventBus eventBus) {

        this.editorAgent = editorAgent;
        this.dtoFactory = dtoFactory;
        eventBus.addHandler(EditorOpenedEvent.TYPE, new EditorOpenedEventHandler(){

            @Override
            public void onEditorOpened(EditorOpenedEvent event) {
                addProblems();
            }
        });
    }

    public void updateProblems(List<MavenProblem> problems) {
        pomToProblem = new HashMap<>();
        for (MavenProblem problem : problems) {
            if (!pomToProblem.containsKey(problem.getPomPath())) {
                pomToProblem.put(problem.getPomPath(), new ArrayList<MavenProblem>());
            }

            pomToProblem.get(problem.getPomPath()).add(problem);
        }

        addProblems();
    }

    private void addProblems() {Map<String, EditorPartPresenter> openedEditors = editorAgent.getOpenedEditors();
        for (String pomPath : pomToProblem.keySet()) {
            if (openedEditors.containsKey(pomPath)) {
                EditorPartPresenter editorPartPresenter = openedEditors.get(pomPath);
                if (editorPartPresenter instanceof ConfigurableTextEditor) {
                    ConfigurableTextEditor textEditor = (ConfigurableTextEditor)editorPartPresenter;
                    AnnotationModel annotationModel = textEditor.getConfiguration().getAnnotationModel();
                    if (annotationModel instanceof ProblemRequester) {
                        ProblemRequester problemRequester = (ProblemRequester)annotationModel;
                        try {
                            problemRequester.beginReporting();
                            List<MavenProblem> mavenProblems = pomToProblem.get(pomPath);
                            if (mavenProblems != null) {
                                Document document = textEditor.getDocument();
                                for (MavenProblem mavenProblem : mavenProblems) {
                                    problemRequester.acceptProblem(convertToProblem(mavenProblem, document));
                                }
                            }
                        } finally {
                            problemRequester.endReporting();
                        }
                    }

                }
            }
        }
    }

    private Problem convertToProblem(MavenProblem mavenProblem, Document document) {
        Problem dto = dtoFactory.createDto(Problem.class);
        dto.setMessage(mavenProblem.getDescription());
        dto.setError(true);
        //TODO dirty dirty hacks
        int start = document.getContents().indexOf("<project ") + 1;
        dto.setSourceStart(start);
        dto.setSourceEnd(start + "project".length());
        return dto;
    }
}
