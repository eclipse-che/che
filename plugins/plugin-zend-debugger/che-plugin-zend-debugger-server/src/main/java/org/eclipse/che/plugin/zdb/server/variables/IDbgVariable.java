/*
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.zdb.server.variables;

import org.eclipse.che.api.debug.shared.model.Variable;

/**
 * Zend debugger specific variable.
 *
 * @author Bartlomiej Laczkowski
 */
public interface IDbgVariable extends Variable {

  /** Requests child variables computation. */
  void makeComplete();

  /**
   * Assigns new value to this variable.
   *
   * @param newValue
   */
  void setValue(String newValue);
}
