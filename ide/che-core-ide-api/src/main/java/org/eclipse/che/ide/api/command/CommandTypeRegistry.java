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
package org.eclipse.che.ide.api.command;

import org.eclipse.che.commons.annotation.Nullable;

import java.util.List;

/**
 * Registry for command types.
 *
 * @author Artem Zatsarynnyi
 */
public interface CommandTypeRegistry {

    /**
     * Returns {@link CommandType} with the specified ID or {@code null} if none.
     *
     * @param id
     *         the ID of the command type
     * @return command type or {@code null}
     */
    @Nullable
    CommandType getCommandTypeById(String id);

    /** Returns all registered {@link CommandType}s. */
    List<CommandType> getCommandTypes();
}
