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
package org.eclipse.che.ide.ui.smartTree.event;

/**
 * Provide mechanism of cancellation fired events.
 *
 * @author Vlad Zhukovskiy
 */
public interface CancellableEvent {

  /**
   * Returns true if the event has been cancelled.
   *
   * @return true for cancelled
   */
  public boolean isCancelled();

  /**
   * True to cancel the event.
   *
   * @param cancel true to cancel
   */
  public void setCancelled(boolean cancel);
}
