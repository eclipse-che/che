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
package org.eclipse.che.ide.project;

import org.eclipse.che.ide.api.editor.reconciler.ReconcilingStrategy;

/**
 * Keeper for the state of Resolving Project process. Resolving Project process has the meaning for
 * {@link ReconcilingStrategy}. For example, 'Resolving Project process' for a Maven project means
 * reimporting maven model.
 *
 * <p>
 *
 * <ul>
 *   Makes it possible to:
 *   <li>keep the state of Resolving Project process
 *   <li>get the state of Resolving Project process when you need
 *   <li>notify the corresponding listener when the state of Resolving Project process has been
 *       changed
 * </ul>
 *
 * @author Roman Nikitenko
 */
public interface ResolvingProjectStateHolder {
  /** Describes state of Resolving Project process. */
  public enum ResolvingProjectState {
    NOT_RESOLVED,
    IN_PROGRESS,
    RESOLVED
  }

  /** Returns the current state for Resolving Project process. See {@link ResolvingProjectState} */
  ResolvingProjectState getState();

  /** Returns the project type for which it holds the state of Resolving project process. */
  String getProjectType();

  /**
   * Adds {@link ResolvingProjectStateListener} that should be called every time when the state of
   * Resolving Project process has been changed.
   */
  void addResolvingProjectStateListener(ResolvingProjectStateListener listener);

  /**
   * Removes {@link ResolvingProjectStateListener} to don't get notification about changes of the
   * state of Resolving Project process.
   */
  void removeResolvingProjectStateListener(ResolvingProjectStateListener listener);

  /** Listener that will be called when resolving project state has been changed. */
  public interface ResolvingProjectStateListener {
    /**
     * Will be called when the resolving project state has been changed
     *
     * @param state the current state of Resolving Project process, see {@link
     *     ResolvingProjectState}
     */
    void onResolvingProjectStateChanged(ResolvingProjectState state);
  }
}
