/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.debugger.ide.debug;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.StackFrameDump;
import org.eclipse.che.api.debug.shared.model.ThreadState;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.debug.shared.model.WatchExpression;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;

/**
 * Provides methods which allow change view representation of debugger panel. Also the interface
 * contains inner action delegate interface which provides methods which allows react on user's
 * actions.
 *
 * @author Andrey Plotnikov
 * @author Dmitry Shnurenko
 * @author Oleksandr Andriienko
 */
public interface DebuggerView extends View<DebuggerView.ActionDelegate> {

  /** Needs for delegate some function into Debugger view. */
  interface ActionDelegate extends BaseActionDelegate {
    /** Is invoked when a new thread is selected. */
    void onSelectedThread(long threadId);

    /**
     * Is invoked when a new frame is selected.
     *
     * @param frameIndex the frame index inside a thread
     */
    void onSelectedFrame(int frameIndex);

    /** Breakpoint context menu is invoked. */
    void onBreakpointContextMenu(int clientX, int clientY, Breakpoint breakpoint);

    void onBreakpointDoubleClick(Breakpoint breakpoint);

    /**
     * Performs any actions appropriate in response to the user having pressed the expand button in
     * variables tree.
     */
    void onExpandVariable(Variable variable);

    /** Is invoked when a add watch expression button clicked */
    void onAddExpressionBtnClicked(WatchExpression expression);

    /** Is invoked when remove watch expression button clicked. */
    void onRemoveExpressionBtnClicked(WatchExpression expression);

    /** Is invoked when edit watch expression button clicked. */
    void onEditExpressionBtnClicked(WatchExpression expression);
  }

  /**
   * Sets information about the execution point.
   *
   * @param location information about the execution point
   */
  void setExecutionPoint(@NotNull Location location);

  /** Remove all variables */
  void removeAllVariables();

  /**
   * Set variables.
   *
   * @param variables available variables
   */
  void setVariables(@NotNull List<? extends Variable> variables);

  /** Updates variable in the list */
  void updateVariable(Variable variable);

  /**
   * Expand variable in the debugger tree.
   *
   * @param variable to expand
   */
  void expandVariable(Variable variable);

  /**
   * Returns selected variable on the debugger panel or null if none selected variable.
   *
   * @return selected variable or null otherwise.
   */
  Variable getSelectedVariable();

  /**
   * Returns selected expression on the debugger panel or null if none selected expression.
   *
   * @return selected expression or null otherwise.
   */
  WatchExpression getSelectedExpression();

  /**
   * Add new watch expression.
   *
   * @param expression to add
   */
  void addExpression(WatchExpression expression);

  /**
   * Update new expression.
   *
   * @param expression to update
   */
  void updateExpression(WatchExpression expression);

  /**
   * Remove expression.
   *
   * @param expression to remove
   */
  void removeExpression(WatchExpression expression);

  /**
   * Sets breakpoints.
   *
   * @param breakpoints available breakpoints
   */
  void setBreakpoints(@NotNull List<ActiveBreakpointWrapper> breakpoints);

  /**
   * Sets thread dump and select the thread with {@link ThreadState#getId()} equal to {@code
   * activeThreadId}.
   */
  void setThreadDump(@NotNull List<? extends ThreadState> threadDump, long threadIdToSelect);

  /** Sets the list of frames for selected thread. */
  void setFrames(@NotNull List<? extends StackFrameDump> stackFrameDumps);

  /**
   * Sets java virtual machine name and version.
   *
   * @param name virtual machine name
   */
  void setVMName(@Nullable String name);

  /**
   * Sets title.
   *
   * @param title title of view
   */
  void setTitle(@NotNull String title);

  /** Returns selected thread id {@link ThreadState#getId()} or -1 if there is no selection. */
  long getSelectedThreadId();

  /** Returns selected frame index inside thread or -1 if there is no selection. */
  int getSelectedFrameIndex();

  void setThreadNotSuspendPlaceHolderVisible(boolean visible);

  /** Returns debugger toolbar panel widget. */
  AcceptsOneWidget getDebuggerToolbarPanel();

  /** Returns debugger watch toolbar panel widget. */
  AcceptsOneWidget getDebuggerWatchToolbarPanel();

  /*
   * Wraps breakpoint and its state.
   */
  class ActiveBreakpointWrapper {
    private Breakpoint breakpoint;
    private boolean active;

    public ActiveBreakpointWrapper(Breakpoint breakpoint, boolean active) {
      this.breakpoint = breakpoint;
      this.active = active;
    }

    public Breakpoint getBreakpoint() {
      return breakpoint;
    }

    public boolean isActive() {
      return active;
    }
  }
}
