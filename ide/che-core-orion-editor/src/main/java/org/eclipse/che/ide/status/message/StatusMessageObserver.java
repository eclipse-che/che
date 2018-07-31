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
package org.eclipse.che.ide.status.message;

/**
 * Observer to update object which is interested in {@link StatusMessage} report.
 *
 * @author Alexander Andrienko
 */
public interface StatusMessageObserver {
  /**
   * Update interested object by new editor status message.
   *
   * @param message message about editor status.
   */
  void update(StatusMessage message);
}
