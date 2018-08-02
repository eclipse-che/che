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
package org.eclipse.che.ide.terminal;

import javax.validation.constraints.NotNull;

/**
 * Handler for the processing of click on 'Add new terminal' button
 *
 * @author Anna Shumilova
 */
public interface AddTerminalClickHandler {

  /**
   * Will be called when user clicks 'Add new terminal' button
   *
   * @param machineId id of machine in which the terminal will be added
   */
  void onAddTerminalClick(@NotNull String machineId);
}
