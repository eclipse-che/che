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
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.MutableVariable;
import org.eclipse.che.api.debug.shared.model.StackFrameDump;
import org.eclipse.che.api.debug.shared.model.ThreadDump;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.debug.Breakpoint;
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
    void onExpandVariablesTree();

    /**
     * Performs any actions appropriate in response to the user having selected variable in*
     * variables tree.
     *
     * @param variable variable that is selected
     */
    void onSelectedVariableElement(@NotNull MutableVariable variable);

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

  /**
   * Sets breakpoints.
   *
   * @param breakPoints available breakpoints
   */
  void setBreakpoints(@NotNull List<Breakpoint> breakPoints);

  /**
   * Sets the list of the threads and select the one with {@link ThreadDump#getId()} equal to {@code
   * activeThreadId}.
   */
  void setThreads(@NotNull List<? extends ThreadDump> threadDumps, long threadIdToSelect);

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

  /** Update contents for selected variable. */
  void updateSelectedVariable();

  /**
   * Add elements into selected variable.
   *
   * @param variables variable what need to add into
   */
  void setVariablesIntoSelectedVariable(@NotNull List<? extends Variable> variables);

  /**
   * Sets whether this object is visible.
   *
   * @param visible <code>true</code> to show the tab, <code>false</code> to hide it
   */
  void setVisible(boolean visible);

  /**
   * Returns selected variable in the variables list on debugger panel or null if no selection.
   *
   * @return selected variable or null if no selection.
   */
  MutableVariable getSelectedDebuggerVariable();

  AcceptsOneWidget getDebuggerToolbarPanel();
}
