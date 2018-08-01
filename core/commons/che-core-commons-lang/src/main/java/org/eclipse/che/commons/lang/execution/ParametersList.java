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
package org.eclipse.che.commons.lang.execution;

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;

/** Represent and configure program parameters */
public class ParametersList {
  private final List<String> parameters = new ArrayList<>();

  public void add(String name, String value) {
    parameters.add(name);
    parameters.add(value);
  }

  /**
   * Adds a parameter without name.
   *
   * @param value value of the parameter
   */
  public void add(String value) {
    parameters.add(value);
  }

  public List<String> getParameters() {
    return unmodifiableList(parameters);
  }
}
