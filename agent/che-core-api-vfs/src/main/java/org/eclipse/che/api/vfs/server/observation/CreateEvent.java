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
package org.eclipse.che.api.vfs.server.observation;

import org.eclipse.che.api.core.notification.EventOrigin;

/**
 * @author andrew00x
 */
@EventOrigin("vfs")
public class CreateEvent extends VirtualFileEvent {
    public CreateEvent(String workspaceId, String path, boolean folder) {
        super(workspaceId, path, ChangeType.CREATED, folder);
    }

    public CreateEvent() {
    }
}
