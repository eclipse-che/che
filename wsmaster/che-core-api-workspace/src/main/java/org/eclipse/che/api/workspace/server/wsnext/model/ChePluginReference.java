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
package org.eclipse.che.api.workspace.server.wsnext.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChePluginReference {

  private String name = null;
  private String version = null;
  private List<ChePluginParameter> parameters = new ArrayList<ChePluginParameter>();

  /** */
  public ChePluginReference name(String name) {
    this.name = name;
    return this;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  /** */
  public ChePluginReference version(String version) {
    this.version = version;
    return this;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  /** */
  public ChePluginReference parameters(List<ChePluginParameter> parameters) {
    this.parameters = parameters;
    return this;
  }

  public List<ChePluginParameter> getParameters() {
    return parameters;
  }

  public void setParameters(List<ChePluginParameter> parameters) {
    this.parameters = parameters;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ChePluginReference chePluginReference = (ChePluginReference) o;
    return Objects.equals(name, chePluginReference.name)
        && Objects.equals(version, chePluginReference.version)
        && Objects.equals(parameters, chePluginReference.parameters);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, version, parameters);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ChePluginReference {\n");

    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    parameters: ").append(toIndentedString(parameters)).append("\n");
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
