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

import io.typefox.lsapi.Command;

@DTO
public interface CommandDTO extends Command {
    /**
     * Title of the command, like `save`.
     * 
     */
    public abstract void setTitle(final String title);

    /**
     * The identifier of the actual command handler.
     * 
     */
    public abstract void setCommand(final String command);

    /**
     * Arguments that the command handler should be invoked with.
     * 
     */
    public abstract void setArguments(final List<Object> arguments);
}
