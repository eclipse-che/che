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
package org.eclipse.che.ide.api.filewatcher;

import org.eclipse.che.api.promises.client.Promise;

/**
 * Send file tracking operation calls on server side. There are several types of such calls:
 *
 * <ul>
 *   <li>START/STOP - tells to start/stop tracking specific file
 *   <li>SUSPEND/RESUME - tells to start/stop tracking all files registered for specific endpoint
 *   <li>MOVE - tells that file that is being tracked should be moved (renamed)
 * </ul>
 */
public interface ClientServerEventService {

  /**
   * Sends event on server side which tells to start tracking specific file
   *
   * @param path the path to the specific file
   */
  Promise<Boolean> sendFileTrackingStartEvent(String path);

  /**
   * Sends event on server side which tells to stop tracking specific file
   *
   * @param path the path to the specific file
   */
  Promise<Boolean> sendFileTrackingStopEvent(String path);

  /**
   * Sends event on server side which tells to suspend tracking all files registered for specific
   * endpoint
   */
  Promise<Boolean> sendFileTrackingSuspendEvent();

  /**
   * Sends event on server side which tells to resume tracking all files registered for specific
   * endpoint
   */
  Promise<Boolean> sendFileTrackingResumeEvent();

  /**
   * Sends event on server side which tells file that is being tracked should be moved (renamed)
   *
   * @param oldPath the old path to the specific file
   * @param newPath the new path to the specific file
   */
  Promise<Boolean> sendFileTrackingMoveEvent(String oldPath, String newPath);
}
