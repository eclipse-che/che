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
package org.eclipse.che.plugin.nodejsdbg.server;

import org.eclipse.che.plugin.nodejsdbg.server.exception.NodeJsDebuggerException;

/** @author Anatolii Bazko */
public interface NodeJsProcessObserver {

  /**
   * Is occurred when a nodejs generates a new output.
   *
   * <p>Returns {@code true} if no processing requires after.
   */
  boolean onOutputProduced(NodeJsOutput nodeJsOutput) throws NodeJsDebuggerException;
}
