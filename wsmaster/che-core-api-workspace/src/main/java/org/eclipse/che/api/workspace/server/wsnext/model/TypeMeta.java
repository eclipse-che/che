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

public class TypeMeta {

  private String kind = null;
  private String apiVersion = null;

  /** Kind is a string value representing the REST resource this object represents. */
  public TypeMeta kind(String kind) {
    this.kind = kind;
    return this;
  }

  public String getKind() {
    return kind;
  }

  public void setKind(String kind) {
    this.kind = kind;
  }

  /** APIVersion defines the versioned schema of this representation of an object */
  public TypeMeta apiVersion(String apiVersion) {
    this.apiVersion = apiVersion;
    return this;
  }

  public String getApiVersion() {
    return apiVersion;
  }

  public void setApiVersion(String apiVersion) {
    this.apiVersion = apiVersion;
  }

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TypeMeta typeMeta = (TypeMeta) o;
    return Objects.equals(kind, typeMeta.kind) && Objects.equals(apiVersion, typeMeta.apiVersion);
  }

  @Override
  public int hashCode() {
    return Objects.hash(kind, apiVersion);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TypeMeta {\n");

    sb.append("    kind: ").append(toIndentedString(kind)).append("\n");
    sb.append("    apiVersion: ").append(toIndentedString(apiVersion)).append("\n");
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
