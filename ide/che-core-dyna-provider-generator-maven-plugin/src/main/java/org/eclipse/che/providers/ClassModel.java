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
package org.eclipse.che.providers;

/**
 * Data class for generator. Holds class name and variable name for class.
 *
 * @author Evgen Vidolob
 */
public class ClassModel {

  private String name;

  private String varName;

  public ClassModel(Class<?> clazz) {
    name = clazz.getName();
    varName = clazz.getName().replaceAll("\\.", "_");
  }

  public String getName() {
    return name;
  }

  public String getVarName() {
    return varName;
  }
}
