/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide;

import static com.google.gwt.user.client.Window.Location.getParameter;
import static com.google.gwt.user.client.Window.Location.getParameterMap;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Singleton;

/**
 * Provides access to query parameters
 *
 * @author Dmitry Shnurenko
 * @author Sergii Leschenko
 */
@Singleton
public class QueryParameters {

  /**
   * Returns the query parameter by the specified name or empty string if parameter was not found.
   * Note that if multiple parameters have been specified with the same name, the last one will be
   * returned.
   *
   * @param name name of value parameter
   * @return query parameter value
   */
  public String getByName(String name) {
    String value = getParameter(name);

    return value == null ? "" : value;
  }

  /**
   * Returns map containing key and value of query parameters or empty map if there are no
   * parameters.
   *
   * <p>Note that if multiple parameters have been specified with the same name, the result will
   * contains only first one.
   *
   * @return map with query parameters
   */
  public Map<String, String> getAll() {
    Map<String, String> parameters = new HashMap<>();
    getParameterMap().forEach((key, value) -> parameters.put(key, value.get(0)));

    return parameters;
  }
}
