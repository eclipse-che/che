/**
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.che.api.languageserver.shared.lsapi;

import io.typefox.lsapi.MessageType;
import io.typefox.lsapi.ShowMessageRequestParams;

import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * @author Sven Efftinge
 */
@DTO
public interface ShowMessageRequestParamsDTO extends ShowMessageRequestParams {
    /**
     * The message action items to present. Overridden to return the DTO type.
     */
    List<MessageActionItemDTO> getActions();

    /**
     * The message action items to present.
     */
    void setActions(final List<MessageActionItemDTO> actions);

    /**
     * The message type.
     */
    void setType(final MessageType type);

    /**
     * The actual message.
     */
    void setMessage(final String message);
}
