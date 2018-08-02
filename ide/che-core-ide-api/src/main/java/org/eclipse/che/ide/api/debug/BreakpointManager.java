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
package org.eclipse.che.ide.api.debug;

import java.util.List;
import org.eclipse.che.api.debug.shared.model.Breakpoint;

/**
 * Breakpoint manager.
 *
 * @author Anatoliy Bazko
 */
public interface BreakpointManager extends BreakpointManagerObservable {

  /** Toggle / untoggle breakpoint. */
  void changeBreakpointState(int lineNumber);

  /** Indicates if breakpoint is active. */
  boolean isActive(Breakpoint breakpoint);

  /** @return all breakpoints */
  List<Breakpoint> getAll();

  /** Removes all breakpoints. */
  void deleteAll();

  /** Updates the given breakpoint. */
  void update(Breakpoint breakpoint);

  /** Deletes the given breakpoint. */
  void delete(Breakpoint breakpoint);
}
