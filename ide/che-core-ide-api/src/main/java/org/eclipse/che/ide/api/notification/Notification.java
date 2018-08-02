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

import static org.eclipse.che.ide.api.notification.ReadState.READ;
import static org.eclipse.che.ide.api.notification.ReadState.UNREAD;

import com.google.common.base.Strings;
import com.google.gwt.dom.client.Document;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.DelayedTask;

/**
 * Presents an entity that reflects the state of a notification.
 *
 * <p>In order to show a notification you need to create an instance of this class and give it to
 * {@link NotificationManager}. The manager knows how to show and handle it. In case you want to
 * change the notification you will change your own instance and these changes will be take place in
 * view.
 *
 * <p>The notification makes it possible to delegate some actions in response to opening and closing
 * of a notification. Also the notification has an popup state. The notification with this state
 * will be closed only when user clicks 'Close' button. Other notifications (non-popup) will be
 * closed after 5 a second time-out.
 *
 * <p>Title is a mandatory parameter for the notification.
 *
 * @author Andrey Plotnikov
 * @author Vlad Zhukovskyi
 */
public class Notification {

  protected NotificationListener listener;
  protected String id;
  protected long time;
  protected String title;
  protected String content;
  protected ReadState state;
  protected ProjectConfigDto project;

  private List<NotificationObserver> observers;

  protected DelayedTask setUnreadStateTask =
      new DelayedTask() {
        @Override
        public void onExecute() {
          time = System.currentTimeMillis();
          setState(UNREAD);
        }
      };

  /**
   * Creates notification object with specified title.
   *
   * @param title notification title (required)
   */
  public Notification(String title) {
    this(title, null, null, null);
  }

  /**
   * Creates notification object.
   *
   * @param title notification title (required)
   * @param content notification content (optional)
   * @param project project which name will be displayed in the notification (optional)
   * @param listener event listener that handle mouse events (optional)
   */
  public Notification(
      String title, String content, ProjectConfigDto project, NotificationListener listener) {
    id = Document.get().createUniqueId();
    observers = new ArrayList<>();

    this.project = project;
    this.title = title;
    this.content = content;
    this.listener = listener;

    setUnreadState();
  }

  /**
   * Get notification event listener.
   *
   * @return notification event listener
   */
  public NotificationListener getListener() {
    return listener;
  }

  /**
   * Set notification event listener.
   *
   * @param listener notification event listener (required)
   */
  public void setListener(NotificationListener listener) {
    if (listener != null) {
      this.listener = listener;
      setUnreadState();
    }
  }

  protected void setUnreadState() {
    // lets collect all calls instead of continuously calling update observers
    setUnreadStateTask.delay(200);
  }

  /**
   * Get notification title.
   *
   * @return notification title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Set notification title.
   *
   * @param title notification title (required)
   * @throws IllegalArgumentException if title is null or empty
   */
  public void setTitle(String title) {
    if (Strings.isNullOrEmpty(title)) {
      throw new IllegalArgumentException("Title shouldn't be a null");
    }

    this.title = title;
    setUnreadState();
  }

  /**
   * Get notification content.
   *
   * @return notification content
   */
  public String getContent() {
    return content;
  }

  /**
   * Set notification's content
   *
   * @param content notification content (required)
   */
  public void setContent(String content) {
    this.content = content;
    setUnreadState();
  }

  /**
   * Returns whether this notification is read.
   *
   * @return true if the notification is read, and false if it's not
   */
  public boolean isRead() {
    return state == READ;
  }

  /**
   * Set notification's state
   *
   * @param state notification state
   * @throws IllegalArgumentException if status is null
   */
  public void setState(ReadState state) {
    if (state == null) {
      throw new IllegalArgumentException("State shouldn't be a null");
    }
    this.state = state;
    notifyObservers();
  }

  /**
   * Get timestamp when this notification was created or updated.
   *
   * @return timestamp when this notification was created or updated
   */
  public long getTime() {
    return time;
  }

  /**
   * Get project to which notification belongs.
   *
   * @return project
   */
  public ProjectConfigDto getProject() {
    return project;
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Notification)) return false;

    Notification that = (Notification) o;

    return id.equals(that.id);
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    return id.hashCode();
  }

  /**
   * Add a notification's observer.
   *
   * @param observer observer that need to add
   */
  public void addObserver(NotificationObserver observer) {
    if (!observers.contains(observer)) {
      observers.add(observer);
    }
  }

  /**
   * Remove a notification's observer.
   *
   * @param observer observer that need to remove
   */
  public void removeObserver(NotificationObserver observer) {
    observers.remove(observer);
  }

  /** Notify observes. */
  public void notifyObservers() {
    for (NotificationObserver observer : observers) {
      observer.onValueChanged();
    }
  }

  /**
   * Return internal ID for the notification
   *
   * @return notification ID
   */
  public String getId() {
    return id;
  }
}
