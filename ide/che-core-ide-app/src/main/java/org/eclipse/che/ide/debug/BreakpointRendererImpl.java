/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.debug;

import static org.eclipse.che.ide.api.editor.gutter.Gutters.BREAKPOINTS_GUTTER;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import elemental.dom.Element;
import elemental.html.DivElement;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.debug.BreakpointRenderer;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.gutter.Gutter;
import org.eclipse.che.ide.api.editor.texteditor.EditorResources;
import org.eclipse.che.ide.api.editor.texteditor.LineStyler;
import org.eclipse.che.ide.util.dom.Elements;

/** Renderer for breakpoint marks in gutter (on the left margin of the text). */
public class BreakpointRendererImpl implements BreakpointRenderer {

  /** The resources for breakpoint display. */
  private final BreakpointResources breakpointResources;

  /** The resources for editor display. */
  private final EditorResources editorResources;

  /** The component responsible for gutter handling. */
  private final Gutter hasGutter;

  /** The component responsible for line style handling. */
  private final LineStyler lineStyler;

  /** The document. */
  private Document document;

  @AssistedInject
  public BreakpointRendererImpl(
      final BreakpointResources breakpointResources,
      final EditorResources editorResources,
      @Assisted final Gutter hasGutter,
      @Assisted final LineStyler lineStyler,
      @Assisted final Document document) {
    this.breakpointResources = breakpointResources;
    this.editorResources = editorResources;
    this.hasGutter = hasGutter;
    this.lineStyler = lineStyler;
    this.document = document;
  }

  @Override
  public void addBreakpointMark(int lineNumber) {
    addBreakpointMark(lineNumber, (file, firstLine, linesAdded, linesRemoved) -> {});
  }

  @Override
  public void addBreakpointMark(int lineNumber, LineChangeAction action) {
    if (hasGutter != null) {

      Element newElement = createBreakpointMarks(true, null, false);
      Element existedElement = hasGutter.getGutterItem(lineNumber, BREAKPOINTS_GUTTER);
      if (existedElement != null) {
        hasGutter.setGutterItem(lineNumber, BREAKPOINTS_GUTTER, newElement);
      } else {
        hasGutter.addGutterItem(
            lineNumber,
            BREAKPOINTS_GUTTER,
            newElement,
            (fromLine, linesRemoved, linesAdded) ->
                action.onLineChange(document.getFile(), fromLine, linesAdded, linesRemoved));
      }
    }
  }

  @Override
  public void setBreakpointMark(
      final Breakpoint breakpoint, final boolean active, final LineChangeAction action) {

    if (hasGutter != null) {
      int lineNumber = breakpoint.getLocation().getLineNumber() - 1;

      Element newElement =
          createBreakpointMarks(breakpoint.isEnabled(), breakpoint.getCondition(), active);
      Element existedElement = hasGutter.getGutterItem(lineNumber, BREAKPOINTS_GUTTER);

      if (existedElement != null) {
        hasGutter.setGutterItem(lineNumber, BREAKPOINTS_GUTTER, newElement);
      } else {
        hasGutter.addGutterItem(
            lineNumber,
            BREAKPOINTS_GUTTER,
            newElement,
            (fromLine, linesRemoved, linesAdded) ->
                action.onLineChange(document.getFile(), fromLine, linesAdded, linesRemoved));
      }
    }
  }

  @Override
  public void removeBreakpointMark(final int lineNumber) {
    if (hasGutter != null) {
      this.hasGutter.removeGutterItem(lineNumber, BREAKPOINTS_GUTTER);
    }
  }

  @Override
  public void clearBreakpointMarks() {
    if (hasGutter != null) {
      this.hasGutter.clearGutter(BREAKPOINTS_GUTTER);
    }
  }

  @Override
  public void setBreakpointActive(int lineNumber, boolean active) {
    if (hasGutter != null) {
      Element newElement = createBreakpointMarks(true, null, true);
      Element existedElement = hasGutter.getGutterItem(lineNumber, BREAKPOINTS_GUTTER);
      if (existedElement != null) {
        hasGutter.setGutterItem(lineNumber, BREAKPOINTS_GUTTER, newElement);
      }
    }
  }

  @Override
  public void setLineActive(final int lineNumber, final boolean active) {
    if (active && this.lineStyler != null) {
      this.lineStyler.addLineStyles(lineNumber, this.editorResources.editorCss().debugLine());
    } else {
      this.lineStyler.removeLineStyles(lineNumber, this.editorResources.editorCss().debugLine());
    }
  }

  private Element createBreakpointMarks(
      boolean isEnabled, @Nullable String condition, boolean active) {
    BreakpointResources.Css css = breakpointResources.getCss();

    List<String> styles = new ArrayList<>();
    styles.add(css.breakpoint());
    if (!isEnabled) {
      styles.add(css.disabled());
    } else if (active) {
      styles.add(css.active());
    } else {
      styles.add(css.inactive());
    }

    if (condition != null) {
      styles.add(css.condition());
    }

    DivElement element = Elements.createDivElement(styles.stream().toArray(String[]::new));
    if (condition != null) {
      element.setTitle("Condition: " + condition);
    }

    return element;
  }

  @Override
  public boolean isReady() {
    return this.hasGutter != null && this.lineStyler != null;
  }
}
