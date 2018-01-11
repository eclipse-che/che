/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
