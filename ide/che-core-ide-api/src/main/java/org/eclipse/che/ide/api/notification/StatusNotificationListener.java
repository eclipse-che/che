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
 * Listener interface for being notified about changes of notification status.
 *
 * @author Roman Nikitenko
 */
public interface StatusNotificationListener {
    /** Performs some actions in response to a notification status has been changed */
    void onNotificationStatusChanged(StatusNotification notification);
}
