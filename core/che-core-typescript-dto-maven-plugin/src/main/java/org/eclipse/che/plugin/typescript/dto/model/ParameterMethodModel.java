/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.typescript.dto.model;

import static java.util.Objects.hash;

/**
 * Defines the model link to parameter of a method
 *
 * @author Florent Benoit
 */
public class ParameterMethodModel {

  /** Name of the parameter. */
  private String parameterName;

  /** Type of the parameter. (Type is in TypeScript format) */
  private String parameterType;

  /**
   * Create a new instance of parameter model with specified name and type
   *
   * @param parameterName the name of the parameter like foo
   * @param parameterType the type of the parameter (like foo.bar.MyDTO or primitive value like
   *     string)
   */
  public ParameterMethodModel(String parameterName, String parameterType) {
    this.parameterName = parameterName;
    this.parameterType = parameterType;
  }

  /**
   * Getter for the name
   *
   * @return the name of the parameter
   */
  public String getName() {
    return this.parameterName;
  }

  /**
   * Getter for the type
   *
   * @return the type of the parameter
   */
  public String getType() {
    return this.parameterType;
  }

  public int hashCode() {
    return hash(this.parameterName, this.parameterType);
  }

  public boolean equals(Object other) {
    if (other == null) {
      return false;
    }
    if (!(other instanceof ParameterMethodModel)) {
      return false;
    }
    ParameterMethodModel parameterMethodModelOther = (ParameterMethodModel) other;
    return this.parameterName.equals(parameterMethodModelOther.parameterName)
        && this.parameterType.equals(((ParameterMethodModel) other).parameterType);
  }

  public String toString() {
    return "ParameterMethodModel[" + this.parameterName + "/" + this.parameterType + "]";
  }
}
