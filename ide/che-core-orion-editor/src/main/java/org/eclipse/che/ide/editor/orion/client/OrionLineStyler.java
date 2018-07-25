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

import org.eclipse.che.ide.api.editor.texteditor.LineStyler;
import org.eclipse.che.ide.editor.orion.client.jso.OrionAnnotationIteratorOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionAnnotationOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionAttributesOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionEditorOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionStyleOverlay;

/**
 * Implementation of {@link LineStyler} for orion.
 *
 * @author Anatoliy Bazko
 */
public class OrionLineStyler implements LineStyler {

  private static final String CHE_CUSTOM_LINE = "che-custom-line";

  /** The editor object. */
  private final OrionEditorOverlay editorOverlay;

  public OrionLineStyler(OrionEditorOverlay editorOverlay) {
    this.editorOverlay = editorOverlay;
    this.editorOverlay.getAnnotationStyler().addAnnotationType(CHE_CUSTOM_LINE, 50);
  }

  @Override
  public void addLineStyles(final int lineNumber, final String... styles) {
    for (final String classname : styles) {
      OrionAttributesOverlay attributesOverlay = OrionAttributesOverlay.create();
      attributesOverlay.setAttribute("debugId", "debug-line");

      OrionStyleOverlay style = OrionStyleOverlay.create();
      style.setStyleClass(classname);
      style.setAttributes(attributesOverlay);

      int lineStart = editorOverlay.getModel().getLineStart(lineNumber);
      int lineEnd = editorOverlay.getModel().getLineEnd(lineNumber);

      OrionAnnotationOverlay annotation = OrionAnnotationOverlay.create();
      annotation.setStart(lineStart);
      annotation.setEnd(lineEnd);
      annotation.setRangeStyle(style);
      annotation.setType(CHE_CUSTOM_LINE);

      editorOverlay.getAnnotationModel().addAnnotation(annotation);
    }
  }

  @Override
  public void removeLineStyles(final int lineNumber, final String... styles) {
    for (final String classname : styles) {
      int lineStart = editorOverlay.getModel().getLineStart(lineNumber);
      int lineEnd = editorOverlay.getModel().getLineEnd(lineNumber);

      OrionAnnotationIteratorOverlay iter =
          editorOverlay.getAnnotationModel().getAnnotations(lineStart, lineEnd);
      if (iter != null) {
        while (iter.hasNext()) {
          OrionAnnotationOverlay annotation = iter.next();
          if (CHE_CUSTOM_LINE.equals(annotation.getType())
              && annotation.getRangeStyle().getStyleClass().equals(classname)) {
            editorOverlay.getAnnotationModel().removeAnnotation(annotation);
          }
        }
      }
    }
  }

  @Override
  public void clearLineStyles(final int lineNumber) {
    int lineStart = editorOverlay.getModel().getLineStart(lineNumber);
    int lineEnd = editorOverlay.getModel().getLineEnd(lineNumber);

    OrionAnnotationIteratorOverlay iter =
        editorOverlay.getAnnotationModel().getAnnotations(lineStart, lineEnd);
    if (iter != null) {
      while (iter.hasNext()) {
        OrionAnnotationOverlay annotation = iter.next();
        if (CHE_CUSTOM_LINE.equals(annotation.getType())) {
          editorOverlay.getAnnotationModel().removeAnnotation(annotation);
        }
      }
    }
  }
}
