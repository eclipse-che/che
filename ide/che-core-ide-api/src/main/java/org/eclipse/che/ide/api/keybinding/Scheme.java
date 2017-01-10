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
package org.eclipse.che.ide.api.keybinding;

import org.eclipse.che.ide.util.input.CharCodeWithModifiers;

import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import java.util.List;

/**
 * Scheme is set of the key bindings.
 *
 * @author Evgen Vidolob
 * @author Artem Zatsarynnyi
 */
public interface Scheme {

    /**
     * Get id of the scheme.
     *
     * @return the scheme id
     */
    String getSchemeId();

    /**
     * Get scheme description.
     *
     * @return the scheme description
     */
    String getDescription();

    /**
     * Add key binding for action.
     *
     * @param key
     *         the hot key which bind
     * @param actionId
     *         the action id which keys bind
     */
    void addKey(@NotNull CharCodeWithModifiers key, @NotNull String actionId);

    /**
     * Remove key binding for action.
     *
     * @param key
     *         the hot key to remove
     * @param actionId
     *         the action's id for which key need to remove
     */
    void removeKey(@NotNull CharCodeWithModifiers key, @NotNull String actionId);

    /**
     * @return all actions that have the specified key. If there are no
     * such actions then the method returns an empty array
     */
    @NotNull
    List<String> getActionIds(int digest);

    /**
     * @return keyboard shortcut for the action with the specified <code>actionId</code>
     * or an null if the action doesn't have any keyboard shortcut
     */
    @Nullable
    CharCodeWithModifiers getKeyBinding(@NotNull String actionId);
}
