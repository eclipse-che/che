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
package org.eclipse.che.ide.command;

import com.google.inject.Inject;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.api.command.CommandTypeRegistry;
import org.eclipse.che.ide.util.loging.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableList;

/**
 * Implementation of {@link CommandTypeRegistry}.
 *
 * @author Artem Zatsarynnyi
 */
public class CommandTypeRegistryImpl implements CommandTypeRegistry {

    private final Map<String, CommandType> commandTypes;

    public CommandTypeRegistryImpl() {
        this.commandTypes = new HashMap<>();
    }

    @Inject(optional = true)
    private void register(Set<CommandType> commandTypes) {
        for (CommandType type : commandTypes) {
            final String id = type.getId();
            if (this.commandTypes.containsKey(id)) {
                Log.warn(getClass(), "Command type with ID " + id + " is already registered.");
            } else {
                this.commandTypes.put(id, type);
            }
        }
    }

    @Nullable
    @Override
    public CommandType getCommandTypeById(String id) {
        return commandTypes.get(id);
    }

    @Override
    public List<CommandType> getCommandTypes() {
        return unmodifiableList(new ArrayList<>(commandTypes.values()));
    }
}
