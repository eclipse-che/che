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
