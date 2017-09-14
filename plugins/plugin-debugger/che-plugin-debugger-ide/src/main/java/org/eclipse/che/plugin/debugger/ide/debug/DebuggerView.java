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
package org.eclipse.che.plugin.debugger.ide.debug;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.debug.shared.model.Breakpoint;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.MutableVariable;
import org.eclipse.che.api.debug.shared.model.SimpleValue;
import org.eclipse.che.api.debug.shared.model.StackFrameDump;
import org.eclipse.che.api.debug.shared.model.ThreadState;
import org.eclipse.che.api.debug.shared.model.Variable;
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
 */
public interface DebuggerView extends View<DebuggerView.ActionDelegate> {
  /** Needs for delegate some function into Debugger view. */
  interface ActionDelegate extends BaseActionDelegate {
    /**
     * Performs any actions appropriate in response to the user having pressed the expand button in
     * variables tree.
     */
    void onExpandVariablesTree(MutableVariable variable);

    /** Is invoked when a new thread is selected. */
    void onSelectedThread(long threadId);

    /**
     * Is invoked when a new frame is selected.
     *
     * @param frameIndex the frame index inside a thread
     */
    void onSelectedFrame(int frameIndex);
  }

  /**
   * Sets information about the execution point.
   *
   * @param location information about the execution point
   */
  void setExecutionPoint(@NotNull Location location);

  /**
   * Sets variables.
   *
   * @param variables available variables
   */
  void setVariables(@NotNull List<? extends Variable> variables);

  /** Updates variable in the list */
  void setVariableValue(@NotNull Variable variable, @NotNull SimpleValue value);

  /**
   * Sets breakpoints.
   *
   * @param breakPoints available breakpoints
   */
  void setBreakpoints(@NotNull List<Breakpoint> breakPoints);

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

  /**
   * Returns selected variable in the variables list on debugger panel or null if no selection.
   *
   * @return selected variable or null if no selection.
   */
  MutableVariable getSelectedDebuggerVariable();

  AcceptsOneWidget getDebuggerToolbarPanel();
}
