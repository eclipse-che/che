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
package org.eclipse.che.api.core.rest.permission;

import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Describes named operation.
 *
 * <p>Operation is related to {@link PermissionManager} and used as a unit for permission check.
 *
 * @author Eugene Voevodin
 */
public class Operation {

    private final String name;

    /**
     * Creates operation from name.
     *
     * @param name
     *         operation name
     * @throws NullPointerException
     *         when name is null
     */
    public Operation(@NotNull String name) {
        this.name = Objects.requireNonNull(name, "Operation must not be null");
    }

    /**
     * Returns name of this operation.
     */
    @NotNull
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Operation)) {
            return false;
        }
        final Operation other = (Operation)obj;
        return name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "Operation{" +
               "name='" + name + '\'' +
               '}';
    }
}
