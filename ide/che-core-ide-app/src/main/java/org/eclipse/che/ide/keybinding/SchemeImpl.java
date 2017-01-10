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
package org.eclipse.che.ide.keybinding;

import org.eclipse.che.ide.api.keybinding.Scheme;
import org.eclipse.che.ide.util.input.CharCodeWithModifiers;

import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Evgen Vidolob
 * @author Artem Zatsarynnyi
 */
public class SchemeImpl implements Scheme {

    private String id;

    private String description;

    private Map<Integer, List<String>> handlers;

    private Map<String, CharCodeWithModifiers> actionId2CharCode;

    public SchemeImpl(String id, String description) {
        this.id = id;
        this.description = description;
        handlers = new HashMap<>();
        actionId2CharCode = new HashMap<>();
    }

    /** {@inheritDoc} */
    @Override
    public String getSchemeId() {
        return id;
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return description;
    }

    /** {@inheritDoc} */
    @Override
    public void addKey(@NotNull CharCodeWithModifiers key, @NotNull String actionId) {
        final int digest = key.getKeyDigest();
        if (!handlers.containsKey(digest)) {
            handlers.put(digest, new ArrayList<String>());
        }
        handlers.get(digest).add(actionId);
        actionId2CharCode.put(actionId, key);
    }

    /** {@inheritDoc} */
    @Override
    public void removeKey(@NotNull CharCodeWithModifiers key, @NotNull String actionId) {
        final int digest = key.getKeyDigest();

        List<String> array = handlers.get(digest);
        if (array != null) {
            array.remove(actionId);
            if (array.isEmpty()) {
                handlers.remove(digest);
            }
        }

        actionId2CharCode.remove(actionId);
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    public List<String> getActionIds(int digest) {
        if (handlers.containsKey(digest)) {
            return handlers.get(digest);
        }
        return new ArrayList<>();
    }

    /** {@inheritDoc} */
    @Nullable
    @Override
    public CharCodeWithModifiers getKeyBinding(@NotNull String actionId) {
        return actionId2CharCode.get(actionId);
    }
}
