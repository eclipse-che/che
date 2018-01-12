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
package org.eclipse.che.ide.editor.orion.client;

import elemental.dom.Element;
import org.eclipse.che.ide.api.editor.gutter.Gutter;
import org.eclipse.che.ide.api.editor.gutter.Gutters;
import org.eclipse.che.ide.editor.orion.client.jso.OrionAnnotationModelOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionAnnotationOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionEditorOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionExtRulerOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionStyleOverlay;

/**
 * Orion implementation of the ruler for adding VCS change markers.
 *
 * @author Igor Vinokur
 */
public class OrionVcsChangeMarkersRuler implements Gutter {

  private static final String VCS_CHANGE_MARKER = "vcs.change.marker";

  private final OrionExtRulerOverlay orionExtRulerOverlay;
  private final OrionEditorOverlay editorOverlay;
  private final OrionAnnotationModelOverlay annotationModel;

  OrionVcsChangeMarkersRuler(OrionExtRulerOverlay rulerOverlay, OrionEditorOverlay editorOverlay) {
    this.orionExtRulerOverlay = rulerOverlay;
    this.editorOverlay = editorOverlay;
    this.orionExtRulerOverlay.addAnnotationType(VCS_CHANGE_MARKER, 1);
    this.annotationModel = orionExtRulerOverlay.getAnnotationModel();
  }

  @Override
  public void addGutterItem(int line, String gutterId, Element element) {}

  @Override
  public void addGutterItem(int lineStart, int lineEnd, String gutterId, Element element) {
    if (!Gutters.VCS_CHANGE_MARKERS_GUTTER.equals(gutterId)) {
      return;
    }

    OrionAnnotationOverlay annotation = toAnnotation(element, lineStart - 1, lineEnd - 1);
    annotationModel.addAnnotation(annotation);
  }

  @Override
  public void addGutterItem(
      int line, String gutterId, Element element, final LineNumberingChangeCallback lineCallback) {}

  @Override
  public void removeGutterItem(int lineStart, String gutterId) {}

  @Override
  public Element getGutterItem(int lineStart, String gutterId) {
    return null;
  }

  @Override
  public void clearGutter(String gutterId) {
    if (!Gutters.VCS_CHANGE_MARKERS_GUTTER.equals(gutterId)) {
      return;
    }

    OrionAnnotationOverlay[] annotations =
        orionExtRulerOverlay.getAnnotationsByType(annotationModel, 0, Integer.MAX_VALUE);
    removeAnnotations(annotations);
  }

  @Override
  public void setGutterItem(int lineStart, String gutterId, Element element) {}

  private void removeAnnotations(OrionAnnotationOverlay[] annotations) {
    for (OrionAnnotationOverlay annotation : annotations) {
      if (VCS_CHANGE_MARKER.equals(annotation.getType())) {
        annotationModel.removeAnnotation(annotation);
      }
    }
  }

  private OrionAnnotationOverlay toAnnotation(Element element, int lineStart, int lineEnd) {
    OrionAnnotationOverlay annotation = OrionAnnotationOverlay.create();

    OrionStyleOverlay styleOverlay = OrionStyleOverlay.create();
    styleOverlay.setStyleClass(element.getClassName());

    annotation.setStyle(styleOverlay);
    annotation.setType(VCS_CHANGE_MARKER);
    annotation.setStart(editorOverlay.getModel().getLineStart(lineStart));
    annotation.setEnd(editorOverlay.getModel().getLineEnd(lineEnd) + 1);

    return annotation;
  }
}
