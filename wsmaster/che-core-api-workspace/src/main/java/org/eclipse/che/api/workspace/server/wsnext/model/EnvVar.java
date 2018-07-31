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
package org.eclipse.che.api.workspace.server.wsnext.model;

import java.util.Objects;

/** EnvVar represents an environment variable present in a Container. */
public class EnvVar {

  private String name = null;
  private String value = null;

  /** Name of the environment variable. Must be a C_IDENTIFIER. */
  public EnvVar name(String name) {
    this.name = name;
    return this;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  /**
   * Variable references $(VAR_NAME) are expanded using the previous defined environment variables
   * in the container and any service environment variables. If a variable cannot be resolved, the
   * reference in the input string will be unchanged. The $(VAR_NAME) syntax can be escaped with a
   * double $$, ie: $$(VAR_NAME). Escaped references will never be expanded, regardless of whether
   * the variable exists or not. Defaults to \&quot;\&quot;.
   */
  public EnvVar value(String value) {
    this.value = value;
    return this;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EnvVar envVar = (EnvVar) o;
    return Objects.equals(name, envVar.name) && Objects.equals(value, envVar.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, value);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class EnvVar {\n");

    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    value: ").append(toIndentedString(value)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
