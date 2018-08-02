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
package org.eclipse.che.ide.api.notification;

/**
 * Handle events for the specific notification.
 *
 * @author Vlad Zhukovskiy
 */
public interface NotificationListener {
  /** Perform operation when user clicks on the notification. */
  void onClick(Notification notification);

  /** Perform operation when user double clicks on the notification. */
  void onDoubleClick(Notification notification);

  /** Perform operation when user closes the notification. */
  void onClose(Notification notification);
}
