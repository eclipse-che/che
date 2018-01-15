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

import static org.eclipse.che.ide.api.editor.gutter.Gutters.ANNOTATION_GUTTER;

import elemental.dom.Element;
import elemental.events.Event;
import elemental.events.EventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.gutter.Gutter;
import org.eclipse.che.ide.api.editor.text.Position;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.text.annotation.Annotation;
import org.eclipse.che.ide.ui.Tooltip;
import org.eclipse.che.ide.ui.menu.PositionController;
import org.eclipse.che.ide.util.loging.Log;

/** Renderer for annotation marks in gutter (on the left margin of the text). */
public class GutterAnnotationRenderer
    implements AnnotationModelHandler, ClearAnnotationModelHandler {

  /** The logger. */
  private static Logger LOG = Logger.getLogger(GutterAnnotationRenderer.class.getName());

  /** The component responsible for gutter handling. */
  private Gutter hasGutter;

  /** The document. */
  private Document document;

  /**
   * Sets the component responsible for gutter handling.
   *
   * @param hasGutter the component
   */
  public void setHasGutter(final Gutter hasGutter) {
    this.hasGutter = hasGutter;
  }

  @Override
  public void onAnnotationModel(final AnnotationModelEvent event) {
    // remove removed and changed annotations
    for (final Annotation annotation : event.getRemovedAnnotations()) {
      LOG.fine("Remove annotation: " + annotation);
      removeAnnotationItem(event, annotation);
    }
    for (final Annotation annotation : event.getChangedAnnotations()) {
      LOG.fine("Remove changed annotation: " + annotation);
      removeAnnotationItem(event, annotation);
    }
    // add new and changed (new version) annotation
    for (final Annotation annotation : event.getAddedAnnotations()) {
      LOG.fine("Add annotation: " + annotation);
      addAnnotationItem(event.getAnnotationModel(), annotation);
    }
    for (final Annotation annotation : event.getChangedAnnotations()) {
      LOG.fine("Add back changed annotation: " + annotation);
      addAnnotationItem(event.getAnnotationModel(), annotation);
    }
  }

  private void removeAnnotationItem(final AnnotationModelEvent event, final Annotation annotation) {
    final Position position = event.getPositionOfRemovedAnnotation(annotation);
    final TextPosition textPosition = this.document.getPositionFromIndex(position.getOffset());
    final Element annotationItem =
        this.hasGutter.getGutterItem(textPosition.getLine(), ANNOTATION_GUTTER);
    if (AnnotationGroupImpl.isAnnotation(annotationItem)) {
      final AnnotationGroup group = AnnotationGroupImpl.create(annotationItem);
      group.removeAnnotation(annotation, position.getOffset());
      if (group.getAnnotationCount() != 0) {
        return;
      }
    }
    // else
    this.hasGutter.removeGutterItem(textPosition.getLine(), ANNOTATION_GUTTER);
  }

  private void addAnnotationItem(final AnnotationModel model, final Annotation annotation) {
    final Position position = model.getPosition(annotation);
    if (position == null) {
      Log.warn(GutterAnnotationRenderer.class, "No position for annotation " + annotation);
      return;
    }
    final TextPosition textPosition = this.document.getPositionFromIndex(position.getOffset());

    final Element annotationItem =
        this.hasGutter.getGutterItem(textPosition.getLine(), ANNOTATION_GUTTER);

    AnnotationGroup annotationGroup;
    if (!AnnotationGroupImpl.isAnnotation(annotationItem)) {
      LOG.fine("Create new annotation group for line " + textPosition.getLine());
      final AnnotationGroup newGroup = AnnotationGroupImpl.create();
      newGroup
          .getElement()
          .addEventListener(
              Event.MOUSEOVER,
              new EventListener() {
                @Override
                public void handleEvent(final Event evt) {
                  showToolTip(newGroup, textPosition.getLine());
                }
              },
              false);

      this.hasGutter.addGutterItem(
          textPosition.getLine(), ANNOTATION_GUTTER, newGroup.getElement());
      annotationGroup = newGroup;
    } else {
      LOG.fine("Reuse annotation group for line " + textPosition.getLine());
      annotationGroup = AnnotationGroupImpl.create(annotationItem);
    }
    annotationGroup.addAnnotation(annotation, position.getOffset());
  }

  private void showToolTip(AnnotationGroup item, int line) {
    final List<String> messages = new ArrayList<>();
    for (final String message : item.getMessages()) {
      messages.add(message);
    }

    Tooltip tooltip = null;
    if (messages.size() == 1) {
      tooltip =
          Tooltip.create(
              item.getElement(),
              PositionController.VerticalAlign.MIDDLE,
              PositionController.HorizontalAlign.RIGHT,
              messages.get(0));

    } else if (messages.size() > 1) {
      String[] messagesArray = new String[messages.size()];
      messagesArray = messages.toArray(messagesArray);
      tooltip =
          ListTooltipFactory.create(
              item.getElement(),
              "Multiple markers on this line:",
              PositionController.VerticalAlign.MIDDLE,
              PositionController.HorizontalAlign.RIGHT,
              messagesArray);
    }
    if (tooltip != null) {
      tooltip.show();
    }
  }

  public void setDocument(final Document document) {
    this.document = document;
  }

  @Override
  public void onClearModel(final ClearAnnotationModelEvent event) {
    this.hasGutter.clearGutter(ANNOTATION_GUTTER);
  }
}
