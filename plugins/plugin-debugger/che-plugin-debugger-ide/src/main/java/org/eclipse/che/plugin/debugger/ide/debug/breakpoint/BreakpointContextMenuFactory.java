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
package org.eclipse.che.plugin.debugger.ide.debug.breakpoint;

import org.eclipse.che.api.debug.shared.model.Breakpoint;

/**
 * Breakpoint context menu factory.
 *
 * @author Anatolii Bazko
 */
public interface BreakpointContextMenuFactory {

  /**
   * Creates new context menu for a given breakpoint.
   *
   * @param breakpoint the breakpoint
   * @return new context menu
   */
  BreakpointContextMenu newContextMenu(Breakpoint breakpoint);
}
