/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.factory.server;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.util.Collection;
import java.util.Map;

/** @author Sergii Kabashniuk */
public class ValueHelper {
  /**
   * Check that value wasn't set by json parser.
   *
   * @param value - value to check
   * @return - true if value is useless for factory (empty string, collection or map), false
   *     otherwise
   */
  public static boolean isEmpty(Object value) {
    return (null == value)
        || (value.getClass().equals(String.class) && isNullOrEmpty((String) value)
            || (Collection.class.isAssignableFrom(value.getClass())
                && ((Collection) value).isEmpty())
            || (Map.class.isAssignableFrom(value.getClass()) && ((Map) value).isEmpty()));
  }
}
