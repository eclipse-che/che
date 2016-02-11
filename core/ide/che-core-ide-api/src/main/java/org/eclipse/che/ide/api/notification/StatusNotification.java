/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.api.notification;


import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;

/**
 * Status notification. May have one of three status: PROGRESS, SUCCESS and FAIL.
 *
 * @author Vlad Zhukovskiy
 */
public class StatusNotification extends Notification {

    public enum Status {
        PROGRESS,
        SUCCESS,
        FAIL
    }

    private Status  status;
    private boolean balloon;

    /**
     * Creates status notification object with specified title, status and balloon flag.
     *
     * @param title
     *         notification title (required)
     * @param status
     *         notification status (required)
     * @param balloon
     *         true if need to show this notification as balloon
     */
    public StatusNotification(String title, Status status, boolean balloon) {
        super(title);
        this.status = status;
        this.balloon = balloon;
    }

    /**
     * Creates status notification.
     *
     * @param title
     *         notification title (required)
     * @param content
     *         notification content (optional)
     * @param status
     *         notification status (required)
     * @param balloon
     *         true if need to show this notification as balloon
     * @param project
     *         project which name will be displayed in the notification (optional)
     * @param listener
     *         event listener that handle mouse events (optional)
     */
    public StatusNotification(String title,
                              String content,
                              Status status,
                              boolean balloon,
                              ProjectConfigDto project,
                              NotificationListener listener) {
        super(title, content, project, listener);
        this.status = status;
        this.balloon = balloon;
    }

    /**
     * Return the notification status.
     *
     * @return notification status
     * @see org.eclipse.che.ide.api.notification.StatusNotification.Status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Set new status notification.
     *
     * @param status
     *         new notification status
     * @throws IllegalArgumentException
     *         if status is null
     * @see org.eclipse.che.ide.api.notification.StatusNotification.Status
     */
    public void setStatus(Status status) {
        if (status == null) {
            throw new IllegalArgumentException("Status shouldn't be a null");
        }
        this.status = status;
        setUnreadState();
    }

    /**
     * Return the state if notification should be showed as balloon notification.
     *
     * @return true if notification should be balloon also
     */
    public boolean isBalloon() {
        return balloon;
    }

    /**
     * Set balloon state
     *
     * @param balloon
     *         true if notification should be balloon
     */
    public void setBalloon(boolean balloon) {
        this.balloon = balloon;
        setUnreadState();
    }
}
