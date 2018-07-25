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
package org.eclipse.che.ide.api.debug;

import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.ide.api.resources.VirtualFile;

/** Component that handles breakpoints display. */
public interface BreakpointRenderer {

  /**
   * Adds inactive breakpoint mark in the given line.
   *
   * @param lineNumber the line where the breakpoint is set
   */
  void addBreakpointMark(int lineNumber);

  /**
   * Adds inactive breakpoint mark in the given line.
   *
   * @param lineNumber the line where the breakpoint is set
   * @param action to line numbering changes
   */
  void addBreakpointMark(int lineNumber, LineChangeAction action);

  /**
   * Adds a breakpoint mark of the given breakpoint. If the mark exists then it will be changed
   * depending on {@code active} state and {@link Breakpoint#getBreakpointConfiguration()}.
   *
   * @param breakpoint the given breakpoint
   * @param active indicates if breakpoint is active or isn't
   * @param action to line numbering changes
   */
  default void setBreakpointMark(Breakpoint breakpoint, boolean active, LineChangeAction action) {}

  /**
   * Removes the breakpoint mark in the gutter on the given line. Does nothing if there is no
   * breakpoint on this line.
   *
   * @param lineNumber the line where the breakpoint is set
   */
  void removeBreakpointMark(int lineNumber);

  /** Removes all breakpoint marks. */
  void clearBreakpointMarks();

  /**
   * Adds {@code active} breakpoint mark in the given line.
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
   *
   * @return true iff the renderer is ready
   */
  boolean isReady();

  /** Reaction on line numbering changes. */
  interface LineChangeAction {
    /** Action taken on change. */
    void onLineChange(VirtualFile file, int firstLine, int linesAdded, int linesRemoved);
  }
}
