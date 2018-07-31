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
package org.eclipse.che.ide.editor.orion.client;

import elemental.dom.Element;
import org.eclipse.che.ide.api.editor.gutter.Gutter;
import org.eclipse.che.ide.api.editor.gutter.Gutters;
import org.eclipse.che.ide.editor.orion.client.jso.ModelChangedEventOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionAnnotationModelOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionAnnotationOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionEditorOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionExtRulerOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionStyleOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionTextModelOverlay;
import org.eclipse.che.ide.util.dom.Elements;

/**
 * Orion implementation of the ruler for adding breakpoints marks.
 *
 * @author Anatoliy Bazko
 */
public class OrionBreakpointRuler implements Gutter {

  private static final String CHE_BREAKPOINT = "che.breakpoint";

  private final OrionExtRulerOverlay orionExtRulerOverlay;
  private final OrionEditorOverlay editorOverlay;
  private final OrionAnnotationModelOverlay annotationModel;

  private OrionTextModelOverlay.EventHandler<ModelChangedEventOverlay> afterChangedEventHandler;
  private OrionTextModelOverlay.EventHandler<ModelChangedEventOverlay> beforeChangedEventHandler;

  public OrionBreakpointRuler(OrionExtRulerOverlay rulerOverlay, OrionEditorOverlay editorOverlay) {
    this.orionExtRulerOverlay = rulerOverlay;
    this.editorOverlay = editorOverlay;
    this.orionExtRulerOverlay.addAnnotationType(CHE_BREAKPOINT, 1);
    this.annotationModel = orionExtRulerOverlay.getAnnotationModel();
  }

  /** {@inheritDoc} */
  @Override
  public void addGutterItem(int line, String gutterId, Element element) {
    if (!Gutters.BREAKPOINTS_GUTTER.equals(gutterId)) {
      return;
    }

    OrionAnnotationOverlay annotation = toAnnotation(element, line);
    annotationModel.addAnnotation(annotation);
  }

  /** {@inheritDoc} */
  @Override
  public void addGutterItem(
      int line, String gutterId, Element element, final LineNumberingChangeCallback lineCallback) {
    if (!Gutters.BREAKPOINTS_GUTTER.equals(gutterId)) {
      return;
    }

    addGutterItem(line, gutterId, element);
    if (beforeChangedEventHandler == null) {
      beforeChangedEventHandler =
          parameter -> {
            int linesAdded = parameter.addedLineCount();
            int linesRemoved = parameter.removedLineCount();
            int fromLine = editorOverlay.getModel().getLineAtOffset(parameter.start());

            if (linesAdded > 0 || linesRemoved > 0) {
              for (int i = fromLine; i < fromLine + Math.abs(linesAdded - linesRemoved); i++) {
                removeAnnotations(getAnnotations(i));
              }
            }
          };

      this.editorOverlay.getModel().addEventListener("Changing", beforeChangedEventHandler, false);
    }

    if (afterChangedEventHandler == null) {
      afterChangedEventHandler =
          parameter -> {
            int linesAdded = parameter.addedLineCount();
            int linesRemoved = parameter.removedLineCount();
            int fromLine = editorOverlay.getModel().getLineAtOffset(parameter.start());

            if (linesAdded > 0
                || linesRemoved > 0
                || editorOverlay.getModel().getLine(fromLine).isEmpty()) {
              lineCallback.onLineNumberingChange(fromLine, linesRemoved, linesAdded);
            }
          };

      this.editorOverlay.getModel().addEventListener("Changed", afterChangedEventHandler, false);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void removeGutterItem(int line, String gutterId) {
    if (!Gutters.BREAKPOINTS_GUTTER.equals(gutterId)) {
      return;
    }

    OrionAnnotationOverlay[] annotations = getAnnotations(line);
    removeAnnotations(annotations);
  }

  /** {@inheritDoc} */
  @Override
  public Element getGutterItem(int line, String gutterId) {
    if (!Gutters.BREAKPOINTS_GUTTER.equals(gutterId)) {
      return null;
    }

    OrionAnnotationOverlay[] annotations = getAnnotations(line);
    for (OrionAnnotationOverlay annotation : annotations) {
      if (isBreakpointAnnotation(annotation)) {
        return Elements.createDivElement(annotation.getStyle().getStyleClass());
      }
    }

    return null;
  }

  /** {@inheritDoc} */
  @Override
  public void clearGutter(String gutterId) {
    if (!Gutters.BREAKPOINTS_GUTTER.equals(gutterId)) {
      return;
    }

    OrionAnnotationOverlay[] annotations = getAllAnnotations();
    removeAnnotations(annotations);
  }

  /** {@inheritDoc} */
  @Override
  public void setGutterItem(int line, String gutterId, Element element) {
    if (!Gutters.BREAKPOINTS_GUTTER.equals(gutterId)) {
      return;
    }

    OrionAnnotationOverlay[] oldAnnotations = getAnnotations(line);
    removeAnnotations(oldAnnotations);

    addGutterItem(line, gutterId, element);
  }

  private void removeAnnotations(OrionAnnotationOverlay[] annotations) {
    for (OrionAnnotationOverlay annotation : annotations) {
      if (isBreakpointAnnotation(annotation)) {
        annotationModel.removeAnnotation(annotation);
      }
    }
  }

  private OrionAnnotationOverlay toAnnotation(Element element, int line) {
    int lineStart = editorOverlay.getModel().getLineStart(line);
    OrionAnnotationOverlay annotation = OrionAnnotationOverlay.create();

    OrionStyleOverlay styleOverlay = OrionStyleOverlay.create();
    styleOverlay.setStyleClass(element.getClassName());

    annotation.setStyle(styleOverlay);
    annotation.setType(CHE_BREAKPOINT);
    annotation.setStart(lineStart);
    annotation.setEnd(lineStart);
    annotation.setTitle(" "); // empty title is required to display html
    String hint = element.getTitle().replace("\n", "<br>").replace(" ", "&nbsp;");
    annotation.setHtml("<div>" + hint + "</div>");
    return annotation;
  }

  private OrionAnnotationOverlay[] getAnnotations(int line) {
    int lineStart = editorOverlay.getModel().getLineStart(line);
    return doGetAnnotations(lineStart, lineStart);
  }

  private OrionAnnotationOverlay[] getAllAnnotations() {
    return doGetAnnotations(0, Integer.MAX_VALUE);
  }

  private OrionAnnotationOverlay[] doGetAnnotations(int lineStart, int lineEnd) {
    return orionExtRulerOverlay.getAnnotationsByType(annotationModel, lineStart, lineEnd);
  }

  private boolean isBreakpointAnnotation(OrionAnnotationOverlay annotation) {
    return CHE_BREAKPOINT.equals(annotation.getType());
  }
}
