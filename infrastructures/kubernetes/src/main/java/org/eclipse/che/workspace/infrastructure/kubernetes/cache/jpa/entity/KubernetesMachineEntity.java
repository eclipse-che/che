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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.eclipse.che.api.core.model.workspace.runtime.MachineStatus;

/** @author Sergii Leshchenko */
@Entity(name = "KubernetesMachine")
@Table(name = "che_k8s_machine")
public class KubernetesMachineEntity {
  @EmbeddedId private KubernetesMachineId machineId;

  @Column(name = "pod_name")
  private String podName;

  @Column(name = "container_name")
  private String containerName;

  @Column(name = "status")
  private MachineStatus status;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
    name = "che_k8s_machine_attributes",
    joinColumns = {
      @JoinColumn(name = "workspace_id", referencedColumnName = "workspace_id"),
      @JoinColumn(name = "machine_name", referencedColumnName = "machine_name")
    }
  )
  @MapKeyColumn(name = "attribute_key")
  @Column(name = "attribute")
  private Map<String, String> attributes;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  @JoinColumns({
    @JoinColumn(name = "workspace_id", referencedColumnName = "workspace_id"),
    @JoinColumn(name = "machine_name", referencedColumnName = "machine_name")
  })
  private List<KubernetesServerEntity> servers;

  public KubernetesMachineEntity() {}

  public KubernetesMachineEntity(
      String workspaceId,
      String machineName,
      String podName,
      String containerName,
      MachineStatus status,
      Map<String, String> attributes,
      List<KubernetesServerEntity> servers) {
    this.machineId = new KubernetesMachineId(workspaceId, machineName);
    this.podName = podName;
    this.containerName = containerName;
    this.status = status;
    this.attributes = attributes;
    this.servers = servers;
  }

  public MachineStatus getStatus() {
    return status;
  }

  public KubernetesMachineId getMachineId() {
    return machineId;
  }

  public String getPodName() {
    return podName;
  }

  public String getContainerName() {
    return containerName;
  }

  public Map<String, String> getAttributes() {
    return attributes;
  }

  public List<KubernetesServerEntity> getServers() {
    return servers;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof KubernetesMachineEntity)) {
      return false;
    }
    KubernetesMachineEntity that = (KubernetesMachineEntity) o;
    return Objects.equals(getMachineId(), that.getMachineId())
        && Objects.equals(getPodName(), that.getPodName())
        && Objects.equals(getContainerName(), that.getContainerName())
        && getStatus() == that.getStatus()
        && Objects.equals(getAttributes(), that.getAttributes())
        && Objects.equals(getServers(), that.getServers());
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        getMachineId(),
        getPodName(),
        getContainerName(),
        getStatus(),
        getAttributes(),
        getServers());
  }

  @Override
  public String toString() {
    return "KubernetesMachineEntity{"
        + "machineId="
        + machineId
        + ", podName='"
        + podName
        + '\''
        + ", containerName='"
        + containerName
        + '\''
        + ", status="
        + status
        + ", attributes="
        + attributes
        + ", servers="
        + servers
        + '}';
  }

  public String getMachineName() {
    return machineId.machineName;
  }

  public void setStatus(MachineStatus status) {
    this.status = status;
  }

  public String getWorkspaceId() {
    return machineId.workspaceId;
  }

  @Embeddable
  public static class KubernetesMachineId {
    @Column(name = "workspace_id")
    private String workspaceId;

    @Column(name = "machine_name")
    private String machineName;

    public KubernetesMachineId() {}

    public KubernetesMachineId(String workspaceId, String machineName) {
      this.workspaceId = workspaceId;
      this.machineName = machineName;
    }
  }
}
