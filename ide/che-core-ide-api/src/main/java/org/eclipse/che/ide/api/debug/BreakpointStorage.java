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

import java.util.List;
import java.util.Optional;
import org.eclipse.che.api.debug.shared.model.Breakpoint;

/**
 * Preserves and loads breakpoints for the active workspace.
 *
 * @author Anatolii Bazko
 */
public interface BreakpointStorage {

  /**
   * Preserves breakpoints into a storage.
   *
   * @param breakpoints the list of the breakpoints
   */
  void addAll(List<Breakpoint> breakpoints);

  /**
   * Preserve a single breakpoints into a storage.
   *
   * @param breakpoint the breakpoint
   */
  void add(Breakpoint breakpoint);

  /**
   * Removes breakpoints out of the storage.
   *
   * @param breakpoint the breakpoint
   */
  void delete(Breakpoint breakpoint);

  /**
   * Removes breakpoints out of the storage.
   *
   * @param breakpoints the list of the breakpoints
   */
  void deleteAll(List<Breakpoint> breakpoints);

  /**
   * Updates breakpoint.
   *
   * @param breakpoint
   */
  void update(Breakpoint breakpoint);

  /** Clears storage. */
  void clear();

  /** Gets all breakpoints that are set for given file. */
  List<Breakpoint> getByPath(String filePath);

  /** Returns breakpoints that is set for given file and at a given line number. */
  Optional<Breakpoint> get(String filePath, int lineNumber);

  /**
   * Loads all breakpoints out of the storage.
   *
   * @return the list of the breakpoints
   */
  List<Breakpoint> getAll();
}
