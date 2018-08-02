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
package org.eclipse.che.ide.processes;

import javax.validation.constraints.NotNull;

/**
 * Handler for the processing of process stopping
 *
 * @author Roman Nikitenko
 */
public interface StopProcessHandler {

  /**
   * Will be called when user clicks 'Stop' button
   *
   * @param node node of process to stop without closing output
   */
  void onStopProcessClick(@NotNull ProcessTreeNode node);

  /**
   * Will be called when user clicks 'Close' button
   *
   * @param node node of process to stop with closing output
   */
  void onCloseProcessOutputClick(@NotNull ProcessTreeNode node);
}
