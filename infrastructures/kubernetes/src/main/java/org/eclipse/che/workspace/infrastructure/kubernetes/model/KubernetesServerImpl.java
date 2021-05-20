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
package org.eclipse.che.workspace.infrastructure.kubernetes.model;

import java.util.Map;
import java.util.Objects;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;
import org.eclipse.che.api.core.model.workspace.runtime.Server;
import org.eclipse.che.api.core.model.workspace.runtime.ServerStatus;

/** @author Sergii Leshchenko */
@Entity(name = "KubernetesServer")
@Table(name = "che_k8s_server")
public class KubernetesServerImpl implements Server {

  @EmbeddedId private ServerId serverId;

  @Column(name = "url")
  private String url;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private ServerStatus status;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
      name = "che_k8s_server_attributes",
      joinColumns = {
        @JoinColumn(name = "workspace_id", referencedColumnName = "workspace_id"),
        @JoinColumn(name = "machine_name", referencedColumnName = "machine_name"),
        @JoinColumn(name = "server_name", referencedColumnName = "server_name")
      })
  @MapKeyColumn(name = "attribute_key")
  @Column(name = "attribute")
  private Map<String, String> attributes;

  public KubernetesServerImpl() {}

  public KubernetesServerImpl(
      String workspaceId, String machineName, String serverName, Server server) {
    this.serverId = new ServerId(workspaceId, machineName, serverName);
    this.url = server.getUrl();
    this.status = server.getStatus();
    this.attributes = server.getAttributes();
  }

  @Override
  public String getUrl() {
    return url;
  }

  @Override
  public ServerStatus getStatus() {
    return status;
  }

  public void setStatus(ServerStatus status) {
    this.status = status;
  }

  @Override
  public Map<String, String> getAttributes() {
    return attributes;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof KubernetesServerImpl)) {
      return false;
    }
    final KubernetesServerImpl that = (KubernetesServerImpl) obj;
    return Objects.equals(serverId, that.serverId)
        && Objects.equals(url, that.url)
        && Objects.equals(status, that.status)
        && getAttributes().equals(that.getAttributes());
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(serverId);
    hash = 31 * hash + Objects.hashCode(url);
    hash = 31 * hash + Objects.hashCode(status);
    hash = 31 * hash + getAttributes().hashCode();
    return hash;
  }

  @Override
  public String toString() {
    return "KubernetesServerImpl{"
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

  @Embeddable
  public static class ServerId {
    @Column(name = "workspace_id")
    private String workspaceId;

    @Column(name = "machine_name")
    private String machineName;

    @Column(name = "server_name")
    private String serverName;

    public ServerId() {}

    public ServerId(String workspaceId, String machineName, String serverName) {
      this.workspaceId = workspaceId;
      this.machineName = machineName;
      this.serverName = serverName;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (!(obj instanceof ServerId)) {
        return false;
      }
      final ServerId that = (ServerId) obj;
      return Objects.equals(workspaceId, that.workspaceId)
          && Objects.equals(machineName, that.machineName)
          && Objects.equals(serverName, that.serverName);
    }

    @Override
    public int hashCode() {
      int hash = 7;
      hash = 31 * hash + Objects.hashCode(workspaceId);
      hash = 31 * hash + Objects.hashCode(machineName);
      hash = 31 * hash + Objects.hashCode(serverName);
      return hash;
    }

    @Override
    public String toString() {
      return "ServerId{"
          + "workspaceId='"
          + workspaceId
          + '\''
          + ", machineName='"
          + machineName
          + '\''
          + ", serverName='"
          + serverName
          + '\''
          + '}';
    }
  }
}
