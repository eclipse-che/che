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
package org.eclipse.che.ide.api.editor.annotation;

import java.util.IdentityHashMap;
import java.util.Map;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.text.Position;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.text.TextRange;
import org.eclipse.che.ide.api.editor.text.annotation.Annotation;
import org.eclipse.che.ide.api.editor.texteditor.HasTextMarkers;
import org.eclipse.che.ide.api.editor.texteditor.HasTextMarkers.MarkerRegistration;
import org.eclipse.che.ide.util.loging.Log;

/** Render the inline marks on the text. */
public class InlineAnnotationRenderer
    implements AnnotationModelHandler, ClearAnnotationModelHandler {

  /** The currently show markers. */
  private final IdentityHashMap<Annotation, MarkerRegistration> markers = new IdentityHashMap<>();

  /** The component that handles markers. */
  private HasTextMarkers hasTextMarkers;

  /** The document. */
  private Document document;

  /**
   * Sets the component that handles markers.
   *
   * @param hasTextMarkers the new component
   */
  public void setHasTextMarkers(final HasTextMarkers hasTextMarkers) {
    this.hasTextMarkers = hasTextMarkers;
  }

  @Override
  public void onAnnotationModel(final AnnotationModelEvent event) {
    // remove removed and changed annotations
    for (final Annotation annotation : event.getRemovedAnnotations()) {
      removeAnnotationItem(annotation);
    }
    for (final Annotation annotation : event.getChangedAnnotations()) {
      removeAnnotationItem(annotation);
    }

    // add new and changed (new version) annotation

    final Map<String, String> decorations = event.getAnnotationModel().getAnnotationDecorations();

    for (final Annotation annotation : event.getAddedAnnotations()) {
      addAnnotationItem(event.getAnnotationModel(), annotation, decorations);
    }
    for (final Annotation annotation : event.getChangedAnnotations()) {
      addAnnotationItem(event.getAnnotationModel(), annotation, decorations);
    }
  }

  /**
   * Add an inline annotation.
   *
   * @param annotationModel the annotation model
   * @param annotation the annotation to add
   * @param decorations the available decorations
   */
  private void addAnnotationItem(
      AnnotationModel annotationModel, Annotation annotation, Map<String, String> decorations) {
    if (this.hasTextMarkers != null) {
      final String className = decorations.get(annotation.getType());
      if (className == null) {
        return;
      }

      final Position position = annotationModel.getPosition(annotation);
      if (position == null) {
        Log.warn(InlineAnnotationRenderer.class, "Can't add annotation with no position");
        return;
      }

      final TextPosition from = this.document.getPositionFromIndex(position.getOffset());
      final TextPosition to =
          this.document.getPositionFromIndex(position.getOffset() + position.getLength());

      final MarkerRegistration registration =
          this.hasTextMarkers.addMarker(new TextRange(from, to), className);
      if (registration != null) {
        this.markers.put(annotation, registration);
      }
    }
  }

  /**
   * Remove an annotation.
   *
   * @param annotation the annotation to remove
   */
  private void removeAnnotationItem(final Annotation annotation) {
    final MarkerRegistration marker = this.markers.get(annotation);
    if (marker != null) {
      marker.clearMark();
    } else {
      Log.warn(InlineAnnotationRenderer.class, "Inline marker with no handle: " + annotation);
    }
  }

  /**
   * Sets the document.
   *
   * @param document the new value
   */
  public void setDocument(final Document document) {
    this.document = document;
  }

  @Override
  public void onClearModel(final ClearAnnotationModelEvent event) {
    for (final MarkerRegistration registration : this.markers.values()) {
      registration.clearMark();
    }
  }
}
