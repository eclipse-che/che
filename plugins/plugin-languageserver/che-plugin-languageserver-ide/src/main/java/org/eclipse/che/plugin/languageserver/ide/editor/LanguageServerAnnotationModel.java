/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.languageserver.ide.editor;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.ide.api.editor.annotation.AnnotationModelImpl;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.document.DocumentHandle;
import org.eclipse.che.ide.api.editor.partition.DocumentPositionMap;
import org.eclipse.che.ide.api.editor.text.Position;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.texteditor.EditorResources;
import org.eclipse.che.ide.editor.orion.client.OrionAnnotationSeverityProvider;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.plugin.languageserver.ide.LanguageServerResources;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;

/** @author Evgen Vidolob */
public class LanguageServerAnnotationModel extends AnnotationModelImpl
    implements DiagnosticCollector, OrionAnnotationSeverityProvider {
  private final LanguageServerResources.LSCss lsCss;
  private final EditorResources.EditorCss editorCss;
  private final Map<String, List<Diagnostic>> diagnostics = new HashMap<>();
  private final Map<String, List<Diagnostic>> collectedDiagnostics = new HashMap<>();
  private final Map<Diagnostic, DiagnosticAnnotation> generatedAnnotations = new HashMap<>();

  @AssistedInject
  public LanguageServerAnnotationModel(
      @Assisted final DocumentPositionMap docPositionMap,
      final LanguageServerResources resources,
      final EditorResources editorResources) {
    super(docPositionMap);
    this.lsCss = resources.css();
    this.editorCss = editorResources.editorCss();
  }

  protected Position createPositionFromDiagnostic(final Diagnostic diagnostic) {
    DocumentHandle documentHandle = getDocumentHandle();
    Document document = documentHandle.getDocument();
    Range range = diagnostic.getRange();
    int start =
        document.getIndexFromPosition(
            new TextPosition(range.getStart().getLine(), range.getStart().getCharacter()));
    int end =
        document.getIndexFromPosition(
            new TextPosition(range.getEnd().getLine(), range.getEnd().getCharacter()));

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
  public void acceptDiagnostic(String diagnosticCollection, final Diagnostic problem) {
    collectedDiagnostics.get(diagnosticCollection).add(problem);
  }

  @Override
  public void beginReporting(String diagnosticCollection) {
    collectedDiagnostics.put(diagnosticCollection, new ArrayList<>());
  }

  @Override
  public void endReporting(String diagnosticCollection) {
    reportDiagnostic(diagnosticCollection, collectedDiagnostics.remove(diagnosticCollection));
  }

  private void reportDiagnostic(String diagnosticCollection, List<Diagnostic> newDiagnostics) {
    boolean temporaryProblemsChanged = false;

    List<Diagnostic> currentDiagnostics =
        diagnostics.getOrDefault(diagnosticCollection, Collections.emptyList());
    // new list becomes previous list, but make a copy; the collection is modified below.
    diagnostics.put(diagnosticCollection, new ArrayList<>(newDiagnostics));

    for (Diagnostic diagnostic : currentDiagnostics) {
      // go through the current set and remove those that are not present
      // in the new set.
      if (!newDiagnostics.contains(diagnostic)) {
        removeAnnotationFor(diagnostic);
        temporaryProblemsChanged = true;
      } else {
        newDiagnostics.remove(diagnostic);
      }
    }
    for (Diagnostic diagnostic : newDiagnostics) {
      // now go through the ones left in the new set: they are new
      addAnnotationFor(diagnostic);
      temporaryProblemsChanged = true;
    }

    if (temporaryProblemsChanged) {
      fireModelChanged();
    }
  }

  private void addAnnotationFor(Diagnostic diagnostic) {
    Log.debug(getClass(), "adding annotation for " + diagnostic);
    DiagnosticAnnotation annotation = new DiagnosticAnnotation(diagnostic);
    generatedAnnotations.put(diagnostic, annotation);
    Position position = createPositionFromDiagnostic(diagnostic);
    if (position != null) {
      addAnnotation(annotation, position, false);
    } else {
      Log.error(getClass(), "Position is null for " + diagnostic);
    }
  }

  private void removeAnnotationFor(Diagnostic diagnostic) {
    Log.debug(getClass(), "removing annotation for " + diagnostic);
    DiagnosticAnnotation annotation = generatedAnnotations.remove(diagnostic);
    removeAnnotation(annotation, false);
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
    // TODO add differed styles for info and hint type
    decorations.put(DiagnosticAnnotation.INFO_ANNOTATION_TYPE, lsCss.overviewMarkTask());
    decorations.put(DiagnosticAnnotation.HINT_ANNOTATION_TYPE, lsCss.overviewMarkTask());
    return decorations;
  }

  @Override
  public String getSeverity(String annotationType) {

    if (annotationType == null) {
      return "orion.annotation.error";
    }
    // TODO we need better integration with Orion annotation system
    switch (annotationType) {
      case DiagnosticAnnotation.WARNING_ANNOTATION_TYPE:
        return "orion.annotation.warning";

      case DiagnosticAnnotation.HINT_ANNOTATION_TYPE:
      case DiagnosticAnnotation.INFO_ANNOTATION_TYPE:
        return "orion.annotation.info";

      case DiagnosticAnnotation.ERROR_ANNOTATION_TYPE:
      default:
        return "orion.annotation.error";
    }
  }
}
