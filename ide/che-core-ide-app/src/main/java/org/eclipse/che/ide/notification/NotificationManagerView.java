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
package org.eclipse.che.ide.notification;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.notification.Notification;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;

/**
 * The view of {@link NotificationManagerImpl}.
 *
 * @author Andrey Plotnikov
 * @author Vlad Zhukovskyi
 * @see {@link Notification}
 */
public interface NotificationManagerView extends View<NotificationManagerView.ActionDelegate> {
  /** Required for delegating some functions in view. */
  interface ActionDelegate extends BaseActionDelegate, NotificationActionDelegate {}

  /** Delegate events between notifications widgets. */
  interface NotificationActionDelegate {
    /**
     * Handle notification <code>com.google.gwt.user.client.Event.ONCLICK</code> event.
     *
     * @param notification {@link Notification} on which onClick handled
     */
    void onClick(Notification notification);

    /**
     * Handle notification <code>com.google.gwt.user.client.Event.ONDBLCLICK</code> event.
     *
     * @param notification {@link Notification} on which onDoubleClick handled
     */
    void onDoubleClick(Notification notification);

    /**
     * Handle notification close event. This event fires when notification is closed automatically
     * or manually by user.
     *
     * @param notification {@link Notification} on which onClose handled
     */
    void onClose(Notification notification);
  }

  /**
   * Set widget container into notification manager presenter. This container need to display
   * notification row by row.
   *
   * @param container instance of {@link NotificationContainer}
   */
  void setContainer(NotificationContainer container);

  /**
   * Manage notification manager visibility.
   *
   * @param visible true - if notification part should be showed, false - otherwise
   */
  void setVisible(boolean visible);

  /**
   * Set title of event log part.
   *
   * @param title title that need to be set
   */
  void setTitle(String title);

  /** Scrolls the view to the bottom. */
  void scrollBottom();
}
