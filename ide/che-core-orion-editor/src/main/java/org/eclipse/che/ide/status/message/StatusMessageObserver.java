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
