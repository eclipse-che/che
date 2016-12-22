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

import com.google.common.base.Optional;

import org.eclipse.che.commons.annotation.Nullable;

import java.util.List;

/**
 * Registry of predefined command goals.
 *
 * @author Artem Zatsarynnyi
 */
public interface PredefinedCommandGoalRegistry {

    /**
     * Returns an {@code Optional} instance containing the {@link CommandGoal} with the given ID
     * or {@code Optional.absent()} otherwise.
     *
     * @param id
     *         the ID of the command goal to get
     * @return an {@code Optional} instance containing the {@link CommandGoal} or {@code Optional.absent()} otherwise
     */
    Optional<CommandGoal> getGoalById(String id);

    /**
     * Returns a {@link CommandGoal} with the given ID or the default goal if none was registered.
     *
     * @param id
     *         the ID of the command goal to get. May be {@code null}
     * @return a {@link CommandGoal} with the given ID or the default goal if none was registered
     */
    CommandGoal getGoalByIdOrDefault(@Nullable String id);

    /** Returns the default command goal which is used for grouping commands which doesn't belong to any goal. */
    CommandGoal getDefaultGoal();

    /** Returns all registered predefined {@link CommandGoal}s. */
    List<CommandGoal> getAllGoals();
}
