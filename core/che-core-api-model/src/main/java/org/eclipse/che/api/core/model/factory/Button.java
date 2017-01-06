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
package org.eclipse.che.api.core.model.factory;

/**
 * Defines factory button.
 *
 * @author Anton Korneta
 */
public interface Button {

    enum Type {
        LOGO {
            @Override
            public String toString() {
                return "logo";
            }
        },
        NOLOGO {
            @Override
            public String toString() {
                return "nologo";
            }
        };

        public static Type getIgnoreCase(String name) {
            for (Type type : values()) {
                if (name.equalsIgnoreCase(type.toString())) {
                    return type;
                }
            }
            throw new IllegalArgumentException();
        }
    }

    /**
     * Returns type of this button instance
     */
    Type getType();

    /**
     * Returns attributes of this button instance
     */
    ButtonAttributes getAttributes();
}
