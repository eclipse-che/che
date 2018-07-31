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
package org.eclipse.che.ide.notification;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.notification.Notification;
import org.eclipse.che.ide.notification.NotificationManagerView.ActionDelegate;
import org.eclipse.che.ide.notification.NotificationManagerView.NotificationActionDelegate;

/**
 * Notification container. Performs rendering each notification.
 *
 * <p>Possible improvements:
 *
 * <ul>
 *   <li>Add ability to check if notification is visible on viewport to mark it as read
 * </ul>
 *
 * @author Andrey Plotnikov
 * @author Vlad Zhukovskyi
 */
@Singleton
public class NotificationContainer extends FlowPanel implements NotificationActionDelegate {

  private Grid nGrid;
  private Resources resources;
  private ActionDelegate delegate;

  private List<Notification> notifications = new ArrayList<>();

  /**
   * Create notification container.
   *
   * @param resources core resources
   */
  @Inject
  public NotificationContainer(Resources resources) {
    this.resources = resources;

    nGrid = new Grid(0, 1);
    nGrid.setStyleName(resources.notificationCss().notificationPanelContainer());
    add(nGrid);
  }

  /**
   * Show notification in container.
   *
   * @param notification notification that need to show
   */
  public void addNotification(@NotNull Notification notification) {
    notifications.add(notification);
    NotificationContainerItem item = new NotificationContainerItem(notification, resources);
    item.setDelegate(this);

    int index = nGrid.getRowCount();
    nGrid.resizeRows(index + 1);
    nGrid.setWidget(index, 0, item);
  }

  /**
   * Remove notification from the container.
   *
   * @param notification notification that need to removed
   */
  public void removeNotification(@NotNull Notification notification) {
    int index = notifications.indexOf(notification);
    if (index >= 0) {
      nGrid.removeRow(index);
      notifications.remove(index);
    }
  }

  /** {@inheritDoc} */
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
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
    removeNotification(notification);
    delegate.onClose(notification);
  }

  /** {@inheritDoc} */
  @Override
  public void clear() {
    notifications.clear();
    nGrid.clear();
    nGrid.resizeRows(0);
  }
}
