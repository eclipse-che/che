/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.wsnext.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CheFeatureSpec {

  private String version = null;
  private List<ChePluginReference> services = new ArrayList<>();

  /** */
  public CheFeatureSpec version(String version) {
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
  public CheFeatureSpec services(List<ChePluginReference> services) {
    this.services = services;
    return this;
  }

  public List<ChePluginReference> getServices() {
    return services;
  }

  public void setServices(List<ChePluginReference> services) {
    this.services = services;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CheFeatureSpec cheFeatureSpec = (CheFeatureSpec) o;
    return Objects.equals(version, cheFeatureSpec.version)
        && Objects.equals(services, cheFeatureSpec.services);
  }

  @Override
  public int hashCode() {
    return Objects.hash(version, services);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CheFeatureSpec {\n");

    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    services: ").append(toIndentedString(services)).append("\n");
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
