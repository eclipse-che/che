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
package org.eclipse.che.ide.command.goal;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.api.command.PredefinedCommandGoalRegistry;
import org.eclipse.che.ide.util.loging.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableList;

/**
 * Implementation of {@link PredefinedCommandGoalRegistry}.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class PredefinedCommandGoalRegistryImpl implements PredefinedCommandGoalRegistry {

    private final CommandGoal              defaultGoal;
    private final GoalMessages             messages;
    private final Map<String, CommandGoal> commandGoals;

    @Inject
    public PredefinedCommandGoalRegistryImpl(@Named("default") CommandGoal defaultCommandGoal,
                                             GoalMessages messages) {
        defaultGoal = defaultCommandGoal;
        this.messages = messages;

        commandGoals = new HashMap<>();
    }

    @Inject(optional = true)
    private void register(Set<CommandGoal> commandGoals) {
        for (CommandGoal type : commandGoals) {
            final String id = type.getId();

            if (this.commandGoals.containsKey(id)) {
                Log.warn(getClass(), messages.messageGoalAlreadyRegistered(id));
            } else {
                this.commandGoals.put(id, type);
            }
        }
    }

    @Override
    public Optional<CommandGoal> getGoalById(String id) {
        return Optional.fromNullable(commandGoals.get(id));
    }

    @Override
    public CommandGoal getGoalByIdOrDefault(@Nullable String id) {
        if (id == null) {
            return getDefaultGoal();
        }

        return getGoalById(id).or(getDefaultGoal());
    }

    @Override
    public CommandGoal getDefaultGoal() {
        return defaultGoal;
    }

    @Override
    public List<CommandGoal> getAllGoals() {
        return unmodifiableList(new ArrayList<>(commandGoals.values()));
    }
}
