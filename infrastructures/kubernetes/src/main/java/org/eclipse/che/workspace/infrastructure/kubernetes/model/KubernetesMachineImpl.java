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
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
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
import javax.persistence.JoinColumns;
import javax.persistence.MapKeyColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.core.model.workspace.runtime.MachineStatus;
import org.eclipse.che.api.workspace.server.model.impl.ServerImpl;

/** @author Sergii Leshchenko */
@Entity(name = "KubernetesMachine")
@Table(name = "che_k8s_machine")
@NamedQueries({
  @NamedQuery(
      name = "KubernetesMachine.getByWorkspaceId",
      query = "SELECT m FROM KubernetesMachine m WHERE m.machineId.workspaceId = :workspaceId")
})
public class KubernetesMachineImpl implements Machine {

  @EmbeddedId private MachineId machineId;

  @Column(name = "pod_name")
  private String podName;

  @Column(name = "container_name")
  private String containerName;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private MachineStatus status;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
      name = "che_k8s_machine_attributes",
      joinColumns = {
        @JoinColumn(name = "workspace_id", referencedColumnName = "workspace_id"),
        @JoinColumn(name = "machine_name", referencedColumnName = "machine_name")
      })
  @MapKeyColumn(name = "attribute_key")
  @Column(name = "attribute")
  private Map<String, String> attributes;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  @JoinColumns({
    @JoinColumn(name = "workspace_id", referencedColumnName = "workspace_id"),
    @JoinColumn(name = "machine_name", referencedColumnName = "machine_name")
  })
  @MapKeyColumn(name = "server_name", insertable = false, updatable = false)
  private Map<String, KubernetesServerImpl> servers;

  public KubernetesMachineImpl() {}

  public KubernetesMachineImpl(
      String workspaceId,
      String machineName,
      String podName,
      String containerName,
      MachineStatus status,
      Map<String, String> attributes,
      Map<String, ServerImpl> servers) {
    this.machineId = new MachineId(workspaceId, machineName);
    this.podName = podName;
    this.containerName = containerName;
    this.status = status;
    this.attributes = attributes;
    this.servers =
        servers
            .entrySet()
            .stream()
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    e ->
                        new KubernetesServerImpl(
                            workspaceId, machineName, e.getKey(), e.getValue())));
  }

  public MachineStatus getStatus() {
    return status;
  }

  public void setStatus(MachineStatus status) {
    this.status = status;
  }

  public MachineId getMachineId() {
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

  public Map<String, KubernetesServerImpl> getServers() {
    return servers;
  }

  public String getName() {
    return machineId.machineName;
  }

  public String getWorkspaceId() {
    return machineId.workspaceId;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof KubernetesMachineImpl)) {
      return false;
    }
    final KubernetesMachineImpl that = (KubernetesMachineImpl) obj;
    return Objects.equals(machineId, that.machineId)
        && Objects.equals(podName, that.podName)
        && Objects.equals(containerName, that.containerName)
        && Objects.equals(status, that.status)
        && getAttributes().equals(that.getAttributes())
        && getServers().equals(that.getServers());
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(machineId);
    hash = 31 * hash + Objects.hashCode(podName);
    hash = 31 * hash + Objects.hashCode(containerName);
    hash = 31 * hash + Objects.hashCode(status);
    hash = 31 * hash + getAttributes().hashCode();
    hash = 31 * hash + getServers().hashCode();
    return hash;
  }

  @Override
  public String toString() {
    return "KubernetesMachineImpl{"
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

  @Embeddable
  public static class MachineId {

    @Column(name = "workspace_id")
    private String workspaceId;

    @Column(name = "machine_name")
    private String machineName;

    public MachineId() {}

    public MachineId(String workspaceId, String machineName) {
      this.workspaceId = workspaceId;
      this.machineName = machineName;
    }

    public String getWorkspaceId() {
      return workspaceId;
    }

    public String getMachineName() {
      return machineName;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (!(obj instanceof MachineId)) {
        return false;
      }
      final MachineId that = (MachineId) obj;
      return Objects.equals(workspaceId, that.workspaceId)
          && Objects.equals(machineName, that.machineName);
    }

    @Override
    public int hashCode() {
      int hash = 7;
      hash = 31 * hash + Objects.hashCode(workspaceId);
      hash = 31 * hash + Objects.hashCode(machineName);
      return hash;
    }

    @Override
    public String toString() {
      return "MachineId{"
          + "workspaceId='"
          + workspaceId
          + '\''
          + ", machineName='"
          + machineName
          + '\''
          + '}';
    }
  }
}
