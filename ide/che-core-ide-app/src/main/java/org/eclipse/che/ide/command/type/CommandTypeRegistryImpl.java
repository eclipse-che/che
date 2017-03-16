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

package org.eclipse.che.ide.command.type;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.api.command.CommandTypeRegistry;
import org.eclipse.che.ide.util.loging.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of {@link CommandTypeRegistry}.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class CommandTypeRegistryImpl implements CommandTypeRegistry {

    private final CommandTypeMessages messages;

    private final Map<String, CommandType> commandTypes;

    @Inject
    public CommandTypeRegistryImpl(CommandTypeMessages messages) {
        this.messages = messages;
        this.commandTypes = new HashMap<>();
    }

    @Inject(optional = true)
    private void register(Set<CommandType> commandTypes) {
        for (CommandType type : commandTypes) {
            final String id = type.getId();

            if (this.commandTypes.containsKey(id)) {
                Log.warn(getClass(), messages.typeRegistryMessageAlreadyRegistered(id));
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
        return new ArrayList<>(commandTypes.values());
    }
}
