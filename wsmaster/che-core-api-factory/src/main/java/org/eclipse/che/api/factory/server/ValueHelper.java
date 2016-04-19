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
package org.eclipse.che.api.factory.server;

import java.util.Collection;
import java.util.Map;

/**
 * @author Sergii Kabashniuk
 */
public class ValueHelper {
    /**
     * Check that value wasn't set by json parser.
     *
     * @param value
     *         - value to check
     * @return - true if value is useless for factory (0 for primitives or empty collection or map), false otherwise
     */
    public static boolean isEmpty(Object value) {
        return (null == value) ||
               (value.getClass().equals(Boolean.class) && !((Boolean)value)) ||
               (value.getClass().equals(Integer.class) && (Integer)value == 0) ||
               (value.getClass().equals(Long.class) && (Long)value == 0) ||
               (Collection.class.isAssignableFrom(value.getClass()) && ((Collection)value).isEmpty()) ||
               (Map.class.isAssignableFrom(value.getClass()) && ((Map)value).isEmpty()) ||
               (value.getClass().equals(Byte.class) && (Byte)value == 0) ||
               (value.getClass().equals(Short.class) && (Short)value == 0) ||
               (value.getClass().equals(Double.class) && (Double)value == 0) ||
               (value.getClass().equals(Float.class) && (Float)value == 0);
    }

}
