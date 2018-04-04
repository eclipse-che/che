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
package org.eclipse.che.ide.api.notification;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode;

import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.notification.StatusNotification.Status;
import org.eclipse.che.ide.api.parts.PartPresenter;

/**
 * The manager for notifications. Used to show notifications and change their states.
 *
 * @author Andrey Plotnikov
 * @author Vlad Zhukovskii
 */
public interface NotificationManager extends PartPresenter {

  /**
   * Show notification.
   *
   * @param notification notification
   * @param <T> return type of the notification
   * @return notification object
   */
  <T extends Notification> T notify(T notification);

  /**
   * Show notification.
   *
   * @param title title
   * @return notification object
   */
  Notification notify(String title);

  /**
   * Show notification.
   *
   * @param title title
   * @param content content
   * @return notification object
   */
  Notification notify(String title, String content);

  /**
   * Show notification.
   *
   * @param title title
   * @param listener notification event listener
   * @return notification object
   */
  Notification notify(String title, NotificationListener listener);

  /**
   * Show notification.
   *
   * @param title title
   * @param status notification status
   * @param displayMode mode of displaying of the notification
   * @return notification object
   */
  StatusNotification notify(String title, Status status, DisplayMode displayMode);

  /**
   * Show notification.
   *
   * @param title title
   * @param content content
   * @param listener notification event listener
   * @return notification object
   */
  Notification notify(String title, String content, NotificationListener listener);

  /**
   * Show status notification.
   *
   * @param title title
   * @param content content
   * @param status notification status
   * @param displayMode mode of displaying of the notification
   * @return notification object
   */
  StatusNotification notify(String title, String content, Status status, DisplayMode displayMode);

  /**
   * Show status notification.
   *
   * @param title title
   * @param content content
   * @param status notification status
   * @param displayMode mode of displaying of the notification
   * @param listener notification event listener
   * @return notification object
   */
  StatusNotification notify(
      String title,
      String content,
      Status status,
      DisplayMode displayMode,
      NotificationListener listener);

  /**
   * Show status notification.
   *
   * @param title title
   * @param status notification status
   * @param displayMode mode of displaying of the notification
   * @param listener notification event listener
   * @return notification object
   */
  StatusNotification notify(
      String title, Status status, DisplayMode displayMode, NotificationListener listener);

  /**
   * Show notification.
   *
   * @param title title
   * @param content content
   * @param project provide project information to which notification belongs
   * @return notification object
   */
  Notification notify(String title, String content, ProjectConfigDto project);

  /**
   * Show notification.
   *
   * @param title title
   * @param project provide project information to which notification belongs
   * @return notification object
   */
  Notification notify(String title, ProjectConfigDto project);

  /**
   * Show notification.
   *
   * @param title title
   * @param content content
   * @param listener notification event listener
   * @param project provide project information to which notification belongs
   * @return notification object
   */
  Notification notify(
      String title, String content, NotificationListener listener, ProjectConfigDto project);

  /**
   * Show notification.
   *
   * @param title title
   * @param listener notification event listener
   * @param project provide project information to which notification belongs
   * @return notification object
   */
  Notification notify(String title, NotificationListener listener, ProjectConfigDto project);

  /**
   * Show status notification.
   *
   * @param title title
   * @param content content
   * @param status notification status
   * @param displayMode mode of displaying of the notification
   * @param project provide project information to which notification belongs
   * @return notification object
   */
  StatusNotification notify(
      String title,
      String content,
      Status status,
      DisplayMode displayMode,
      ProjectConfigDto project);

  /**
   * Show status notification.
   *
   * @param title title
   * @param status notification status
   * @param displayMode mode of displaying of the notification
   * @param project provide project information to which notification belongs
   * @return notification object
   */
  StatusNotification notify(
      String title, Status status, DisplayMode displayMode, ProjectConfigDto project);

  /**
   * Show status notification.
   *
   * @param title title
   * @param content content
   * @param status notification status
   * @param displayMode mode of displaying of the notification
   * @param listener notification event listener
   * @param project provide project information to which notification belongs
   * @return notification object
   */
  StatusNotification notify(
      String title,
      String content,
      Status status,
      DisplayMode displayMode,
      NotificationListener listener,
      ProjectConfigDto project);

  /**
   * Show status notification.
   *
   * @param title title
   * @param status notification status
   * @param displayMode mode of displaying of the notification
   * @param listener notification event listener
   * @param project provide project information to which notification belongs
   * @return notification object
   */
  StatusNotification notify(
      String title,
      Status status,
      DisplayMode displayMode,
      NotificationListener listener,
      ProjectConfigDto project);
}
