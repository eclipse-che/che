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
import static org.eclipse.che.plugin.typescript.dto.DTOHelper.convertType;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Defines the model of the method
 *
 * @author Florent Benoit
 */
public class MethodModel {

  /** Reflect object used internally. */
  private Method method;

  /** Typescript return value of the method */
  private String returnType;

  /** List of parameters for this method */
  private List<ParameterMethodModel> parameters;

  /** This method is a DTO getter method */
  private boolean isGetter;

  /** This method is a DTO setter method */
  private boolean isSetter;

  /** This method is a DTO with method */
  private boolean isWith;

  /**
   * Name of the field associated to this method (field to return for a getter, field to store for
   * setter/with)
   */
  private String fieldName;

  /** Type of the field associated to this method. */
  private String fieldType;

  /**
   * Build a new model around the DTO method.
   *
   * @param method
   */
  public MethodModel(Method method) {
    this.method = method;
    this.parameters = new ArrayList<>();
    analyze();
  }

  /** Loop on all parameters and initialize return value as well */
  protected void analyze() {
    IntStream.range(0, method.getGenericParameterTypes().length)
        .forEach(
            i ->
                parameters.add(
                    new ParameterMethodModel(
                        "arg" + i, convertType(method.getGenericParameterTypes()[i]))));

    // add return type
    this.returnType = convertType(method.getGenericReturnType());
  }

  public boolean isGetter() {
    return isGetter;
  }

  public void setGetter(boolean getter) {
    isGetter = getter;
  }

  public boolean isSetter() {
    return isSetter;
  }

  public void setSetter(boolean setter) {
    isSetter = setter;
  }

  public boolean isWith() {
    return isWith;
  }

  public void setWith(boolean with) {
    isWith = with;
  }

  public String getName() {
    return this.method.getName();
  }

  public List<ParameterMethodModel> getParameters() {
    return this.parameters;
  }

  public String getReturnType() {
    return this.returnType;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldType(String fieldType) {
    this.fieldType = fieldType;
  }

  public String getFieldType() {
    return fieldType;
  }

  public int hashCode() {
    return hash(this.parameters.toString(), this.returnType);
  }

  public boolean equals(Object other) {
    if (other == null) {
      return false;
    }
    if (!(other instanceof MethodModel)) {
      return false;
    }
    MethodModel methodModelOther = (MethodModel) other;
    return this.getName().equals(methodModelOther.getName())
        && this.returnType.equals(methodModelOther.returnType)
        && Arrays.equals(this.parameters.toArray(), methodModelOther.parameters.toArray());
  }
}
