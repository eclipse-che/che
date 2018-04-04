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
package org.eclipse.che.ide.notification;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.LinkedList;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.notification.Notification;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.notification.NotificationManagerView.ActionDelegate;
import org.eclipse.che.ide.notification.NotificationManagerView.NotificationActionDelegate;

/**
 * Stack for storing notifications that should be showed to user in future. Three notifications may
 * be showed at once. If new notification is pushed into stack it will wait when at least one
 * notification will be release from the panel. To add new notification into stack to show it in the
 * future simply call {@link
 * NotificationPopupStack#push(org.eclipse.che.ide.api.notification.StatusNotification)}.
 *
 * @author Andrey Plotnikov
 * @author Vlad Zhukovskyi
 * @see {@link StatusNotification}
 */
@Singleton
public class NotificationPopupStack implements NotificationActionDelegate {

  public static final int POPUP_COUNT = 3;

  private ActionDelegate delegate;
  private LinkedList<StatusNotification> stack = new LinkedList<>();
  private FlowPanel notificationContainer = new FlowPanel();

  public static final String NOTIFICATION_CONTAINER_DBG_ID = "popup-container";

  /**
   * Create message stack.
   *
   * @param resources core resources
   */
  @Inject
  public NotificationPopupStack(final Resources resources) {
    notificationContainer.ensureDebugId(NOTIFICATION_CONTAINER_DBG_ID);
    notificationContainer.setStyleName(resources.notificationCss().notificationPopupPlaceholder());

    RootPanel.get().add(notificationContainer);

    Scheduler.get()
        .scheduleFixedPeriod(
            new Scheduler.RepeatingCommand() {

              /** {@inheritDoc} */
              @Override
              public boolean execute() {
                while (notificationContainer.getWidgetCount() < POPUP_COUNT && !stack.isEmpty()) {
                  notificationContainer.add(
                      new NotificationPopup(stack.pop(), resources, NotificationPopupStack.this));
                }

                return true;
              }
            },
            1000);
  }

  /** Sets the delegate for receiving events from this view. */
  public void setDelegate(@NotNull ActionDelegate delegate) {
    this.delegate = delegate;
  }

  /**
   * Push notification to message stack.
   *
   * @param notification notification that need to add
   */
  public void push(@NotNull StatusNotification notification) {
    stack.push(notification);
  }

  /**
   * Remove notification from message stack.
   *
   * @param notification notification that need to remove
   */
  public void remove(@NotNull StatusNotification notification) {
    stack.remove(notification);
    onClose(notification);
  }

  /** {@inheritDoc} */
  @Override
  public void onClick(Notification notification) {
    if (delegate != null) {
      delegate.onClick(notification);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void onDoubleClick(Notification notification) {
    if (delegate != null) {
      delegate.onDoubleClick(notification);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void onClose(Notification notification) {
    for (int i = 0; i < notificationContainer.getWidgetCount(); i++) {
      Widget child = notificationContainer.getWidget(i);
      if (child instanceof NotificationPopup
          && notification.equals(((NotificationPopup) child).getNotification())) {
        child.removeFromParent();
      }
    }
  }

  /** Remove all notifications from the stack and from notifications container. */
  public void clear() {
    stack.clear();

    for (int i = 0; i < notificationContainer.getWidgetCount(); i++) {
      Widget child = notificationContainer.getWidget(i);
      if (child instanceof NotificationPopup) {
        child.removeFromParent();
      }
    }
  }
}
