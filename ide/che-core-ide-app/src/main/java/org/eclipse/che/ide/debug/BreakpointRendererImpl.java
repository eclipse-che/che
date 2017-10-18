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

import com.google.common.base.Strings;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import elemental.dom.Element;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
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

  private Element activeBreakpointMark;
  private Element activeConditionBreakpointMark;
  private Element inactiveBreakpointMark;
  private Element inactiveConditionBreakpointMark;

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

    breakpointResources.getCss().ensureInjected();

    initBreakpointMarks();
  }

  @Override
  public void addBreakpointMark(int lineNumber) {
    addBreakpointMark(lineNumber, (file, firstLine, linesAdded, linesRemoved) -> {});
  }

  @Override
  public void addBreakpointMark(int lineNumber, LineChangeAction action) {
    if (hasGutter != null) {
      Element newElement = inactiveBreakpointMark;

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
      boolean hasCondition = !Strings.isNullOrEmpty(breakpoint.getCondition());

      Element newElement =
          active
              ? (hasCondition ? activeConditionBreakpointMark : activeBreakpointMark)
              : (hasCondition ? inactiveConditionBreakpointMark : inactiveBreakpointMark);
      if (hasCondition) {
        newElement.setTitle("Condition: " + breakpoint.getCondition());
      }

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
      Element existedElement = hasGutter.getGutterItem(lineNumber, BREAKPOINTS_GUTTER);
      if (existedElement != null) {
        hasGutter.setGutterItem(lineNumber, BREAKPOINTS_GUTTER, activeBreakpointMark);
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

  private void initBreakpointMarks() {
    BreakpointResources.Css css = breakpointResources.getCss();
    activeBreakpointMark = Elements.createDivElement(css.breakpoint(), css.active());
    activeConditionBreakpointMark =
        Elements.createDivElement(css.breakpoint(), css.active(), css.condition());
    inactiveBreakpointMark = Elements.createDivElement(css.breakpoint(), css.inactive());
    inactiveConditionBreakpointMark =
        Elements.createDivElement(css.breakpoint(), css.inactive(), css.condition());
  }

  @Override
  public boolean isReady() {
    return this.hasGutter != null && this.lineStyler != null;
  }
}
