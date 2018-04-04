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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.minimap.Minimap;
import org.eclipse.che.ide.api.editor.text.LinearRange;
import org.eclipse.che.ide.api.editor.text.Position;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.text.annotation.Annotation;
import org.eclipse.che.ide.util.loging.Log;

/** Renderer for annotation marks in the minimap (on the right margin of the text). */
public class MinimapAnnotationRenderer
    implements AnnotationModelHandler, ClearAnnotationModelHandler {

  /** The logger. */
  private static Logger LOG = Logger.getLogger(MinimapAnnotationRenderer.class.getName());

  /** The component responsible for minimap handling. */
  private Minimap minimap;

  /** The document. */
  private Document document;

  /**
   * Sets the component responsible for minimap handling.
   *
   * @param minimap the component
   */
  public void setMinimap(final Minimap minimap) {
    this.minimap = minimap;
  }

  @Override
  public void onAnnotationModel(final AnnotationModelEvent event) {
    // annotation is mutable, not easy to use a set
    final Map<Integer, List<Annotation>> toRestore = new HashMap<>();
    // remove removed and changed annotations
    for (final Annotation annotation : event.getRemovedAnnotations()) {
      LOG.fine("Remove annotation: " + annotation);
      removeAnnotationItem(event, annotation, toRestore);
    }
    for (final Annotation annotation : event.getChangedAnnotations()) {
      LOG.fine("Remove changed annotation: " + annotation);
      removeAnnotationItem(event, annotation, toRestore);
    }

    final Map<String, String> decorations = event.getAnnotationModel().getAnnotationStyle();
    // restore annotations that were deleted but shouldn't have
    for (final List<Annotation> annotations : toRestore.values()) {
      for (final Annotation annotation : annotations) {
        addAnnotationItem(event.getAnnotationModel(), annotation, decorations);
      }
    }
    // add new and changed (new version) annotation
    for (final Annotation annotation : event.getAddedAnnotations()) {
      LOG.fine("Add annotation: " + annotation);
      addAnnotationItem(event.getAnnotationModel(), annotation, decorations);
    }
    for (final Annotation annotation : event.getChangedAnnotations()) {
      LOG.fine("Add back changed annotation: " + annotation);
      addAnnotationItem(event.getAnnotationModel(), annotation, decorations);
    }
  }

  private void removeAnnotationItem(
      final AnnotationModelEvent event,
      final Annotation annotation,
      final Map<Integer, List<Annotation>> toRestore) {
    final Position position = event.getPositionOfRemovedAnnotation(annotation);
    final TextPosition textPosition = this.document.getPositionFromIndex(position.getOffset());
    final int line = textPosition.getLine();
    // remove all marks on the line
    this.minimap.removeMarks(line, line);
    // restore marks that are not removed
    final LinearRange rangeForLine = this.document.getLinearRangeForLine(line);

    final AnnotationModel model = event.getAnnotationModel();
    final Iterator<Annotation> it =
        model.getAnnotationIterator(
            rangeForLine.getStartOffset(), rangeForLine.getLength(), false, true);
    while (it.hasNext()) {
      final Annotation current = it.next();
      List<Annotation> lineAnnotations = toRestore.get(line);
      if (!current.equals(annotation)) {
        if (lineAnnotations == null) {
          lineAnnotations = new ArrayList<>();
          toRestore.put(line, lineAnnotations);
        }
        lineAnnotations.add(current);
      } else {
        if (lineAnnotations != null) {
          lineAnnotations.removeAll(Collections.singletonList(current));
          if (lineAnnotations.isEmpty()) {
            toRestore.remove(line);
          }
        }
      }
    }
  }

  private void addAnnotationItem(
      final AnnotationModel model,
      final Annotation annotation,
      final Map<String, String> decorations) {
    final Position position = model.getPosition(annotation);
    if (position == null) {
      Log.warn(MinimapAnnotationRenderer.class, "No position for annotation " + annotation);
      return;
    }
    //        final TextPosition textPosition =
    // this.document.getPositionFromIndex(position.getOffset());
    //        final int line = textPosition.getLine();

    final String style = decorations.get(annotation.getType());
    this.minimap.addMark(position.getOffset(), style, annotation.getLayer(), annotation.getText());
  }

  @Override
  public void onClearModel(final ClearAnnotationModelEvent event) {
    this.minimap.clearMarks();
  }

  public void setDocument(final Document document) {
    this.document = document;
  }
}
