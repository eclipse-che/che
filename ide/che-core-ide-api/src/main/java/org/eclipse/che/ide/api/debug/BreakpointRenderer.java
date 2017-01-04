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
package org.eclipse.che.ide.api.debug;

import org.eclipse.che.ide.api.resources.VirtualFile;

/** Component that handles breakpoints display. */
public interface BreakpointRenderer {

    /**
     * Add a breakpoint mark in the gutter on the given line.
     * 
     * @param lineNumber the line where the breakpoint is set
     */
    void addBreakpointMark(int lineNumber);

    /**
     * Add a breakpoint mark in the gutter on the given line.
     * 
     * @param lineNumber the line where the breakpoint is set
     * @param action to line numbering changes
     */
    void addBreakpointMark(int lineNumber, LineChangeAction action);

    /**
     * Removes the breakpoint mark in the gutter on the given line.<br>
     * Does nothing if there is no breakpoint on this line.
     * 
     * @param lineNumber the line where the breakpoint is set
     */
    void removeBreakpointMark(int lineNumber);

    /**
     * Removes all breakpoint marks.
     */
    void clearBreakpointMarks();

    /**
     * Changes appearance of the breakpoint on the line to active/inactive.<br>
     * Does nothing if there is no breakpoint of this line.
     * 
     * @param lineNumber the line where the breakpoint is set
     */
    void setBreakpointActive(int lineNumber, boolean active);

    /**
     * Changes appearance of the line to active/inactive.
     * 
     * @param lineNumber the line
     */
    void setLineActive(int lineNumber, boolean active);

    /**
     * Tells if the renderer is ready for use.
     * @return true iff the renderer is ready
     */
    boolean isReady();

    /** Reaction on line numbering changes. */
    interface LineChangeAction {
        /** Action taken on change. */
        void onLineChange(VirtualFile file, int firstLine, int linesAdded, int linesRemoved);
    }
}
