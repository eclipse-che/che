/**
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.che.plugin.languageserver.shared.lsapi;

import org.eclipse.che.dto.shared.DTO;

import io.typefox.lsapi.NotificationMessage;

@DTO
public interface NotificationMessageDTO extends NotificationMessage {
    /**
     * The method to be invoked.
     * 
     */
    public abstract void setMethod(final String method);

    /**
     * The notification's params.
     * 
     */
    public abstract void setParams(final Object params);

    public abstract void setJsonrpc(final String jsonrpc);
}
