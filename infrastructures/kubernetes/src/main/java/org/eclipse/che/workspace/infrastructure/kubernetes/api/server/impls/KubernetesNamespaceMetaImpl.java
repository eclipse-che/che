/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.api.server.impls;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.eclipse.che.workspace.infrastructure.kubernetes.api.shared.KubernetesNamespaceMeta;

/** @author Sergii Leshchenko */
public class KubernetesNamespaceMetaImpl implements KubernetesNamespaceMeta {

  private String name;
  private Map<String, String> attributes;

  public KubernetesNamespaceMetaImpl(String name) {
    this.name = name;
  }

  public KubernetesNamespaceMetaImpl(String name, Map<String, String> attributes) {
    this.name = name;
    if (attributes != null) {
      this.attributes = new HashMap<>(attributes);
    }
  }

  public KubernetesNamespaceMetaImpl(KubernetesNamespaceMeta namespaceMeta) {
    this(namespaceMeta.getName(), namespaceMeta.getAttributes());
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public Map<String, String> getAttributes() {
    if (attributes == null) {
      attributes = new HashMap<>();
    }
    return attributes;
  }

  public void setAttributes(Map<String, String> attributes) {
    this.attributes = attributes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof KubernetesNamespaceMetaImpl)) {
      return false;
    }
    KubernetesNamespaceMetaImpl that = (KubernetesNamespaceMetaImpl) o;
    return Objects.equals(getName(), that.getName())
        && Objects.equals(getAttributes(), that.getAttributes());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName(), getAttributes());
  }

  @Override
  public String toString() {
    return "KubernetesNamespaceMetaImpl{"
        + "name='"
        + name
        + '\''
        + ", attributes="
        + attributes
        + '}';
  }
}
