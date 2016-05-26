/**
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.che.plugin.languageserver.shared.lsapi;

import java.util.List;

import org.eclipse.che.dto.shared.DTO;

import io.typefox.lsapi.ShowMessageRequestParams;

@DTO
public interface ShowMessageRequestParamsDTO extends ShowMessageRequestParams {
    /**
     * The message action items to present. Overridden to return the DTO type.
     * 
     */
    public abstract List<MessageActionItemDTO> getActions();

    /**
     * The message action items to present.
     * 
     */
    public abstract void setActions(final List<MessageActionItemDTO> actions);

    /**
     * The message type.
     * 
     */
    public abstract void setType(final int type);

    /**
     * The actual message.
     * 
     */
    public abstract void setMessage(final String message);
}
