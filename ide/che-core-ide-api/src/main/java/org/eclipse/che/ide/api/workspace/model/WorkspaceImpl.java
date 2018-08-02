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
package org.eclipse.che.ide.api.workspace.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.eclipse.che.api.core.model.workspace.Runtime;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.commons.annotation.Nullable;

/** Data object for {@link Workspace}. */
public class WorkspaceImpl implements Workspace {

  private String id;
  private String namespace;
  private WorkspaceConfigImpl config;
  private Map<String, String> attributes;
  private boolean isTemporary;
  private WorkspaceStatus status;
  private RuntimeImpl runtime;
  private Map<String, String> links;

  public WorkspaceImpl(WorkspaceImpl workspace) {
    this(
        workspace.getId(),
        workspace.getNamespace(),
        workspace.getConfig(),
        workspace.getRuntime(),
        workspace.getAttributes(),
        workspace.isTemporary(),
        workspace.getStatus(),
        workspace.getLinks(),
        workspace.getRuntime() != null ? workspace.getRuntime().getMachineToken() : null);
  }

  public WorkspaceImpl(WorkspaceDto workspace) {
    this(
        workspace.getId(),
        workspace.getNamespace(),
        workspace.getConfig(),
        workspace.getRuntime(),
        workspace.getAttributes(),
        workspace.isTemporary(),
        workspace.getStatus(),
        workspace.getLinks(),
        workspace.getRuntime() != null ? workspace.getRuntime().getMachineToken() : null);
  }

  private WorkspaceImpl(
      String id,
      String namespace,
      WorkspaceConfig config,
      Runtime runtime,
      Map<String, String> attributes,
      boolean isTemporary,
      WorkspaceStatus status,
      Map<String, String> links,
      String machineToken) {

    this.id = id;
    this.namespace = namespace;
    if (config != null) {
      this.config = new WorkspaceConfigImpl(config);
    }
    if (runtime != null) {
      this.runtime =
          new RuntimeImpl(
              runtime.getActiveEnv(),
              runtime.getMachines(),
              runtime.getOwner(),
              machineToken,
              runtime.getWarnings());
    }
    if (attributes != null) {
      this.attributes = new HashMap<>(attributes);
    }
    this.isTemporary = isTemporary;
    this.status = status;
    if (links != null) {
      this.links = new HashMap<>(links);
    }
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getNamespace() {
    return namespace;
  }

  @Override
  public WorkspaceConfigImpl getConfig() {
    return config;
  }

  @Override
  public Map<String, String> getAttributes() {
    if (attributes == null) {
      attributes = new HashMap<>();
    }
    return attributes;
  }

  @Override
  public boolean isTemporary() {
    return isTemporary;
  }

  @Override
  public WorkspaceStatus getStatus() {
    return status;
  }

  public Map<String, String> getLinks() {
    if (links == null) {
      links = new HashMap<>();
    }
    return links;
  }

  @Nullable
  @Override
  public RuntimeImpl getRuntime() {
    return runtime;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof WorkspaceImpl)) return false;
    final WorkspaceImpl other = (WorkspaceImpl) obj;
    return Objects.equals(id, other.id)
        && Objects.equals(getNamespace(), other.getNamespace())
        && Objects.equals(config, other.config)
        && getAttributes().equals(other.getAttributes())
        && isTemporary == other.isTemporary
        && Objects.equals(status, other.status)
        && Objects.equals(runtime, other.runtime);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(id);
    hash = 31 * hash + Objects.hashCode(getNamespace());
    hash = 31 * hash + Objects.hashCode(config);
    hash = 31 * hash + getAttributes().hashCode();
    hash = 31 * hash + Boolean.hashCode(isTemporary);
    hash = 31 * hash + Objects.hashCode(status);
    hash = 31 * hash + Objects.hashCode(runtime);
    return hash;
  }

  @Override
  public String toString() {
    return "WorkspaceImpl{"
        + "id='"
        + id
        + '\''
        + ", namespace='"
        + namespace
        + '\''
        + ", config="
        + config
        + ", attributes="
        + attributes
        + ", isTemporary="
        + isTemporary
        + ", status="
        + status
        + ", runtime="
        + runtime
        + '}';
  }
}
