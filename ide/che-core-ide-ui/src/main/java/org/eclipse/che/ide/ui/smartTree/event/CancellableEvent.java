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
