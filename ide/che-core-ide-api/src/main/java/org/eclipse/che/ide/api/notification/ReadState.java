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
 * State of notification. The notification has 2 states: a notification is read and a notification is unread.
 *
 * @author Vlad Zhukovskiy
 */
public enum ReadState {
    READ,
    UNREAD
}
