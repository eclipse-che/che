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

import java.util.Objects;

/**
 * Base implementation of the {@link CommandGoal}.
 *
 * @author Artem Zatsarynnyi
 */
public class BaseCommandGoal implements CommandGoal {

    private final String id;
    private final String displayName;

    public BaseCommandGoal(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof CommandGoal)) {
            return false;
        }

        CommandGoal other = (CommandGoal)o;

        return Objects.equals(getId(), other.getId())
               && Objects.equals(getDisplayName(), other.getDisplayName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, displayName);
    }
}
