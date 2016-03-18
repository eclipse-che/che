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
package org.eclipse.che.api.vfs.shared;

/**
 * Object types.
 *
 * @author andrew00x
 */
public enum ItemType {
    FILE("file"), FOLDER("folder");

    private final String value;

    private ItemType(String value) {
        this.value = value;
    }

    /** @return value of Type */
    public String value() {
        return value;
    }

    /**
     * Get Type instance from string value.
     *
     * @param value
     *         string value
     * @return Type
     * @throws IllegalArgumentException
     *         if there is no corresponded Type for specified <code>value</code>
     */
    public static ItemType fromValue(String value) {
        String v = value.toLowerCase();
        for (ItemType e : ItemType.values()) {
            if (e.value.equals(v)) {
                return e;
            }
        }
        throw new IllegalArgumentException(value);
    }

    /** @see java.lang.Enum#toString() */
    @Override
    public String toString() {
        return value;
    }
}