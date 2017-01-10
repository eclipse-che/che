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
package org.eclipse.che.api.core.factory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provide factory parameter compatibility options.
 *
 * @author Alexander Garagatyi
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FactoryParameter {
    enum Obligation {
        MANDATORY, OPTIONAL
    }

    enum Version {
        // NEVER must be the last defined constant
        V4_0, NEVER;

        public static Version fromString(String v) {
            if (null != v) {
                switch (v) {
                    case "4.0":
                        return V4_0;
                }
            }

            throw new IllegalArgumentException("Unknown version " + v + ".");
        }

        @Override
        public String toString() {
            return super.name().substring(1).replace('_', '.');
        }
    }

    Obligation obligation();

    boolean setByServer() default false;

    boolean trackedOnly() default false;

    Version deprecatedSince() default Version.NEVER;

    Version ignoredSince() default Version.NEVER;
}
