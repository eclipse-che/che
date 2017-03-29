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
package org.eclipse.che.ide.api.command;

import org.eclipse.che.commons.annotation.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Registry of command goals.
 *
 * @author Artem Zatsarynnyi
 */
public interface CommandGoalRegistry {

    /** Returns all registered predefined {@link CommandGoal}s. */
    List<CommandGoal> getAllPredefinedGoals();

    /** Returns the default command goal which is used for grouping commands which doesn't belong to any goal. */
    CommandGoal getDefaultGoal();

    /**
     * Returns an optional {@link CommandGoal} by the given ID
     * or {@code Optional.absent()} if none was registered.
     *
     * @param id
     *         the ID of the predefined command goal to get
     * @return an optional {@link CommandGoal} or {@code Optional.absent()} if none was registered
     */
    Optional<CommandGoal> getPredefinedGoalById(String id);

    /**
     * Returns a predefined {@link CommandGoal} by the given ID
     * or the custom (non-predefined) goal if none was registered
     * or the default goal if the given {@code id} is {@code null} or empty string.
     *
     * @param id
     *         the ID of the command goal
     * @return a predefined {@link CommandGoal} with the given ID
     * or the custom (non-predefined) goal if none was registered
     * or the default goal if the given {@code id} is {@code null} or empty string. Never null.
     */
    CommandGoal getGoalForId(@Nullable String id);
}
