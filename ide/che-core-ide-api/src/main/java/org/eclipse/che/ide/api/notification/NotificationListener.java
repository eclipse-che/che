/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.api.notification;

/**
 * Handle events for the specific notification.
 *
 * @author Vlad Zhukovskiy
 */
public interface NotificationListener {
    /**
     * Perform operation when user clicks on the notification.
     */
    void onClick(Notification notification);

    /**
     * Perform operation when user double clicks on the notification.
     */
    void onDoubleClick(Notification notification);

    /**
     * Perform operation when user closes the notification.
     */
    void onClose(Notification notification);
}
