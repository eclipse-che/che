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
package org.eclipse.che.workspace.infrastructure.kubernetes.cache.jpa.entity;

import java.util.Map;
import java.util.Objects;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;
import org.eclipse.che.api.core.model.workspace.runtime.Server;
import org.eclipse.che.api.core.model.workspace.runtime.ServerStatus;

/** @author Sergii Leshchenko */
@Entity(name = "KubernetesServer")
@Table(name = "che_k8s_server")
public class KubernetesServerEntity {

  @EmbeddedId private KubernetesServerId serverId;

  @Column(name = "url")
  private String url;

  @Column(name = "status")
  private ServerStatus status;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
    name = "che_k8s_server_attributes",
    joinColumns = {
      @JoinColumn(name = "workspace_id", referencedColumnName = "workspace_id"),
      @JoinColumn(name = "machine_name", referencedColumnName = "machine_name"),
      @JoinColumn(name = "server_name", referencedColumnName = "server_name")
    }
  )
  @MapKeyColumn(name = "attribute_key")
  @Column(name = "attribute")
  private Map<String, String> attributes;

  public KubernetesServerEntity() {}

  public KubernetesServerEntity(
      String workspaceId, String machineName, String serverName, Server server) {
    this.serverId = new KubernetesServerId(workspaceId, machineName, serverName);
    this.url = server.getUrl();
    this.status = server.getStatus();
    this.attributes = server.getAttributes();
  }

  public KubernetesServerEntity(
      String workspaceId,
      String machineName,
      String serverName,
      String url,
      Map<String, String> attributes,
      ServerStatus status) {
    this.serverId = new KubernetesServerId(workspaceId, machineName, serverName);
    this.url = url;
    this.attributes = attributes;
    this.status = status;
  }

  public KubernetesServerId getServerId() {
    return serverId;
  }

  public KubernetesServerEntity setServerId(KubernetesServerId serverId) {
    this.serverId = serverId;
    return this;
  }

  public String getUrl() {
    return url;
  }

  public KubernetesServerEntity setUrl(String url) {
    this.url = url;
    return this;
  }

  public ServerStatus getStatus() {
    return status;
  }

  public KubernetesServerEntity setStatus(ServerStatus status) {
    this.status = status;
    return this;
  }

  public Map<String, String> getAttributes() {
    return attributes;
  }

  public KubernetesServerEntity setAttributes(Map<String, String> attributes) {
    this.attributes = attributes;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof KubernetesServerEntity)) {
      return false;
    }
    KubernetesServerEntity that = (KubernetesServerEntity) o;
    return Objects.equals(getServerId(), that.getServerId())
        && Objects.equals(getUrl(), that.getUrl())
        && getStatus() == that.getStatus()
        && Objects.equals(getAttributes(), that.getAttributes());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getServerId(), getUrl(), getStatus(), getAttributes());
  }

  @Override
  public String toString() {
    return "KubernetesServerEntity{"
        + "serverId="
        + serverId
        + ", url='"
        + url
        + '\''
        + ", status="
        + status
        + ", attributes="
        + attributes
        + '}';
  }

  public String getName() {
    return serverId.serverName;
  }

  @Embeddable
  public static class KubernetesServerId {
    @Column(name = "workspace_id")
    private String workspaceId;

    @Column(name = "machine_name")
    private String machineName;

    @Column(name = "server_name")
    private String serverName;

    public KubernetesServerId() {}

    public KubernetesServerId(String workspaceId, String machineName, String serverName) {
      this.workspaceId = workspaceId;
      this.machineName = machineName;
      this.serverName = serverName;
    }
  }
}
