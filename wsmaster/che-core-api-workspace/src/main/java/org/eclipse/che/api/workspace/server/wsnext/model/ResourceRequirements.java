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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** Compute Resources required by this container. Cannot be updated. More */
public class ResourceRequirements {

  private Map<String, String> requests = new HashMap<String, String>();

  /**
   * Requests describes the minimum amount of compute resources required. If Requests is omitted for
   * a container, it defaults to Limits if that is explicitly specified, otherwise to an
   * implementation-defined value.
   */
  public ResourceRequirements requests(Map<String, String> requests) {
    this.requests = requests;
    return this;
  }

  public Map<String, String> getRequests() {
    return requests;
  }

  public void setRequests(Map<String, String> requests) {
    this.requests = requests;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResourceRequirements resourceRequirements = (ResourceRequirements) o;
    return Objects.equals(requests, resourceRequirements.requests);
  }

  @Override
  public int hashCode() {
    return Objects.hash(requests);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ResourceRequirements {\n");

    sb.append("    requests: ").append(toIndentedString(requests)).append("\n");
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
