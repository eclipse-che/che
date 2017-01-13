/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.debug;

import elemental.dom.Element;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import org.eclipse.che.ide.api.debug.BreakpointRenderer;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.gutter.Gutter;
import org.eclipse.che.ide.api.editor.gutter.Gutter.LineNumberingChangeCallback;
import org.eclipse.che.ide.api.editor.texteditor.EditorResources;
import org.eclipse.che.ide.api.editor.texteditor.LineStyler;
import org.eclipse.che.ide.util.dom.Elements;

import static org.eclipse.che.ide.api.editor.gutter.Gutters.BREAKPOINTS_GUTTER;

/**
 * Renderer for breakpoint marks in gutter (on the left margin of the text).
 */
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
    private Element inactiveBreakpointMark;

    @AssistedInject
    public BreakpointRendererImpl(final BreakpointResources breakpointResources,
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
    public void addBreakpointMark(final int lineNumber) {
        if (hasGutter != null) {
            this.hasGutter.addGutterItem(lineNumber, BREAKPOINTS_GUTTER, inactiveBreakpointMark);
        }
    }

    @Override
    public void addBreakpointMark(final int lineNumber, final LineChangeAction action) {
        if (hasGutter != null) {
            this.hasGutter.addGutterItem(lineNumber, BREAKPOINTS_GUTTER, inactiveBreakpointMark, new LineNumberingChangeCallback() {
                @Override
                public void onLineNumberingChange(final int fromLine, final int linesRemoved, final int linesAdded) {
                    action.onLineChange(document.getFile(), fromLine, linesAdded, linesRemoved);
                }
            });
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
    public void setBreakpointActive(final int lineNumber, final boolean active) {
        if (hasGutter != null) {
            final Element mark = this.hasGutter.getGutterItem(lineNumber, BREAKPOINTS_GUTTER);
            if (mark != null) {
                Element element = active ? activeBreakpointMark : inactiveBreakpointMark;
                this.hasGutter.setGutterItem(lineNumber, BREAKPOINTS_GUTTER, element);
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
        inactiveBreakpointMark = Elements.createDivElement(css.breakpoint(), css.inactive());
    }

    @Override
    public boolean isReady() {
        return this.hasGutter != null && this.lineStyler != null;
    }
}
