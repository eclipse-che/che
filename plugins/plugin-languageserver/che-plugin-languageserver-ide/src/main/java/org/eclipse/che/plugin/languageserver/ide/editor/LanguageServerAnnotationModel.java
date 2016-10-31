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
package org.eclipse.che.plugin.languageserver.ide.editor;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import org.eclipse.che.api.languageserver.shared.lsapi.DiagnosticDTO;
import org.eclipse.che.api.languageserver.shared.lsapi.RangeDTO;
import org.eclipse.che.ide.api.editor.annotation.AnnotationModelImpl;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.document.DocumentHandle;
import org.eclipse.che.ide.api.editor.partition.DocumentPositionMap;
import org.eclipse.che.ide.api.editor.text.Position;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.texteditor.EditorResources;
import org.eclipse.che.ide.editor.orion.client.OrionAnnotationSeverityProvider;
import org.eclipse.che.plugin.languageserver.ide.LanguageServerResources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Evgen Vidolob
 */
public class LanguageServerAnnotationModel extends AnnotationModelImpl implements DiagnosticCollector, OrionAnnotationSeverityProvider {
    private final LanguageServerResources.LSCss lsCss;
    private final EditorResources.EditorCss     editorCss;
    private       List<DiagnosticDTO>           diagnostics;
    private List<DiagnosticAnnotation> generatedAnnotations = new ArrayList<>();

    @AssistedInject
    public LanguageServerAnnotationModel(@Assisted final DocumentPositionMap docPositionMap,
                                         final LanguageServerResources resources,
                                         final EditorResources editorResources) {
        super(docPositionMap);
        this.lsCss = resources.css();
        this.editorCss = editorResources.editorCss();
    }

    protected Position createPositionFromDiagnostic(final DiagnosticDTO diagnostic) {
        DocumentHandle documentHandle = getDocumentHandle();
        Document document = documentHandle.getDocument();
        RangeDTO range = diagnostic.getRange();
        int start = document.getIndexFromPosition(new TextPosition(range.getStart().getLine(), range.getStart().getCharacter()));
        int end = document.getIndexFromPosition(new TextPosition(range.getEnd().getLine(), range.getEnd().getCharacter()));

        if (start == -1 && end == -1) {
            return new Position(0);
        }

        if (start == -1) {
            return new Position(end);
        }

        if (end == -1) {
            return new Position(start);
        }

        int length = end - start;
        if (length < 0) {
            return null;
        }
        return new Position(start, length);
    }

    @Override
    public void acceptDiagnostic(final DiagnosticDTO problem) {
        diagnostics.add(problem);
    }

    @Override
    public void beginReporting() {
        diagnostics = new ArrayList<>();
    }

    @Override
    public void endReporting() {
        reportDiagnostic();
    }

    private void reportDiagnostic() {
        boolean temporaryProblemsChanged = false;

        if (!generatedAnnotations.isEmpty()) {
            temporaryProblemsChanged = true;
            super.clear();
            generatedAnnotations.clear();
        }

        if (diagnostics != null && !diagnostics.isEmpty()) {

            for (final DiagnosticDTO diagnostic : diagnostics) {
                final Position position = createPositionFromDiagnostic(diagnostic);

                if (position != null) {
                    final DiagnosticAnnotation annotation = new DiagnosticAnnotation(diagnostic);
                    addAnnotation(annotation, position, false);
                    generatedAnnotations.add(annotation);

                    temporaryProblemsChanged = true;
                }
            }
        }

        if (temporaryProblemsChanged) {
            fireModelChanged();
        }
    }

    @Override
    public Map<String, String> getAnnotationDecorations() {
        final Map<String, String> decorations = new HashMap<>();
        // TODO configure this
        decorations.put(DiagnosticAnnotation.ERROR_ANNOTATION_TYPE, this.editorCss.lineError());
        decorations.put(DiagnosticAnnotation.WARNING_ANNOTATION_TYPE, this.editorCss.lineWarning());

        return decorations;
    }

    @Override
    public Map<String, String> getAnnotationStyle() {
        final Map<String, String> decorations = new HashMap<>();
        // //TODO configure this
        decorations.put(DiagnosticAnnotation.ERROR_ANNOTATION_TYPE, lsCss.overviewMarkError());
        decorations.put(DiagnosticAnnotation.WARNING_ANNOTATION_TYPE, lsCss.overviewMarkWarning());
        //TODO add differed styles for info and hint type
        decorations.put(DiagnosticAnnotation.INFO_ANNOTATION_TYPE, lsCss.overviewMarkTask());
        decorations.put(DiagnosticAnnotation.HINT_ANNOTATION_TYPE, lsCss.overviewMarkTask());
        return decorations;
    }

    @Override
    public String getSeverity(String annotationType) {

        if (annotationType == null) {
            return "error";
        }
        //TODO we need better integration with Orion annotation system
        switch (annotationType) {
            case DiagnosticAnnotation.WARNING_ANNOTATION_TYPE:
                return "warning";

            case DiagnosticAnnotation.HINT_ANNOTATION_TYPE:
            case DiagnosticAnnotation.INFO_ANNOTATION_TYPE:
                return "task";

            case DiagnosticAnnotation.ERROR_ANNOTATION_TYPE:
            default:
                return "error";

        }
    }
}
