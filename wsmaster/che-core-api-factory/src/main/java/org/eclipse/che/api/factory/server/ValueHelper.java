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
package org.eclipse.che.api.factory.server;

import java.util.Collection;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Sergii Kabashniuk
 */
public class ValueHelper {
    /**
     * Check that value wasn't set by json parser.
     *
     * @param value
     *         - value to check
     * @return - true if value is useless for factory (empty string, collection or map), false otherwise
     */
    public static boolean isEmpty(Object value) {
        return (null == value) ||
               (value.getClass().equals(String.class) && isNullOrEmpty((String)value) ||
               (Collection.class.isAssignableFrom(value.getClass()) && ((Collection)value).isEmpty()) ||
               (Map.class.isAssignableFrom(value.getClass()) && ((Map)value).isEmpty()));
    }

}
