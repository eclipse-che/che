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
package org.eclipse.che.api.workspace.server.model.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.model.workspace.Runtime;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.model.workspace.devfile.Devfile;
import org.eclipse.che.api.workspace.server.model.impl.devfile.DevfileImpl;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.persistence.descriptors.DescriptorEvent;
import org.eclipse.persistence.descriptors.DescriptorEventAdapter;

/**
 * Data object for {@link Workspace}.
 *
 * @author Yevhenii Voevodin
 */
@Entity(name = "Workspace")
@Table(name = "workspace")
@NamedQueries({
  @NamedQuery(
      name = "Workspace.getByNamespace",
      query = "SELECT w FROM Workspace w WHERE w.account.name = :namespace"),
  @NamedQuery(
      name = "Workspace.getByName",
      query = "SELECT w FROM Workspace w WHERE w.account.name = :namespace AND w.name = :name"),
  @NamedQuery(name = "Workspace.getAll", query = "SELECT w FROM Workspace w"),
  @NamedQuery(
      name = "Workspace.getByTemporary",
      query = "SELECT w " + "FROM Workspace w " + "WHERE w.isTemporary = :temporary "),
  @NamedQuery(name = "Workspace.getAllCount", query = "SELECT COUNT(w) FROM Workspace w"),
  @NamedQuery(
      name = "Workspace.getByNamespaceCount",
      query = "SELECT COUNT(w) " + "FROM Workspace w " + "WHERE w.account.name = :namespace "),
  @NamedQuery(
      name = "Workspace.getWorkspacesTotalCount",
      query = "SELECT COUNT(w) FROM Workspace w"),
  @NamedQuery(
      name = "Workspace.getByTemporaryCount",
      query = "SELECT COUNT(w) " + "FROM Workspace w " + "WHERE w.isTemporary = :temporary ")
})
@EntityListeners(WorkspaceImpl.SyncNameOnUpdateAndPersistEventListener.class)
public class WorkspaceImpl implements Workspace {

  public static WorkspaceImplBuilder builder() {
    return new WorkspaceImplBuilder();
  }

  @Id
  @Column(name = "id")
  private String id;

  /**
   * The original workspace name is workspace.config.name this attribute is stored for unique
   * constraint with account id. See {@link #syncName()}.
   */
  @Column(name = "name")
  private String name;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "config_id")
  private WorkspaceConfigImpl config;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "devfile_id")
  private DevfileImpl devfile;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "workspace_attributes", joinColumns = @JoinColumn(name = "workspace_id"))
  @MapKeyColumn(name = "attributes_key")
  @Column(name = "attributes")
  private Map<String, String> attributes;

  @Column(name = "istemporary")
  private boolean isTemporary;

  @ManyToOne
  @JoinColumn(name = "accountid", nullable = false)
  private AccountImpl account;

  @Transient private WorkspaceStatus status;

  @Transient private Runtime runtime;

  public WorkspaceImpl() {}

  public WorkspaceImpl(String id, Account account, WorkspaceConfig config) {
    this(id, account, config, null, null, false, null);
  }

  public WorkspaceImpl(String id, Account account, Devfile devfile) {
    this(id, account, devfile, null, null, false, null);
  }

  public WorkspaceImpl(
      String id,
      Account account,
      WorkspaceConfig config,
      Runtime runtime,
      Map<String, String> attributes,
      boolean isTemporary,
      WorkspaceStatus status) {
    this(id, account, config, null, runtime, attributes, isTemporary, status);
  }

  public WorkspaceImpl(
      String id,
      Account account,
      Devfile devfile,
      Runtime runtime,
      Map<String, String> attributes,
      boolean isTemporary,
      WorkspaceStatus status) {
    this(id, account, null, devfile, runtime, attributes, isTemporary, status);
  }

  public WorkspaceImpl(
      String id,
      Account account,
      WorkspaceConfig config,
      Devfile devfile,
      Runtime runtime,
      Map<String, String> attributes,
      boolean isTemporary,
      WorkspaceStatus status) {
    this.id = id;
    if (account != null) {
      this.account = new AccountImpl(account);
    }
    if (config != null && devfile != null) {
      throw new IllegalArgumentException("Only config or devfile must be specified.");
    }
    if (config != null) {
      this.config = new WorkspaceConfigImpl(config);
    }
    if (devfile != null) {
      this.devfile = new DevfileImpl(devfile);
    }
    if (runtime != null) {
      this.runtime =
          new RuntimeImpl(
              runtime.getActiveEnv(),
              runtime.getMachines(),
              runtime.getOwner(),
              runtime.getCommands(),
              runtime.getWarnings());
    }
    if (attributes != null) {
      this.attributes = new HashMap<>(attributes);
    }
    this.isTemporary = isTemporary;
    this.status = status;
  }

  public WorkspaceImpl(Workspace workspace, Account account) {
    this(
        workspace.getId(),
        account,
        workspace.getConfig(),
        workspace.getDevfile(),
        workspace.getRuntime(),
        workspace.getAttributes(),
        workspace.isTemporary(),
        workspace.getStatus());
  }

  public WorkspaceImpl(WorkspaceImpl workspace) {
    this(workspace, workspace.account);
  }

  @Override
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public String getNamespace() {
    if (account != null) {
      return account.getName();
    }
    return null;
  }

  /** Returns the name of workspace. It can be stored by workspace config or devfile. */
  public String getName() {
    if (devfile != null) {
      return devfile.getMetadata().getName();
    } else if (config != null) {
      return config.getName();
    } else {
      return null;
    }
  }

  public void setAccount(AccountImpl account) {
    this.account = account;
  }

  public AccountImpl getAccount() {
    return account;
  }

  @Nullable
  @Override
  public WorkspaceConfigImpl getConfig() {
    return config;
  }

  public void setConfig(WorkspaceConfigImpl config) {
    this.config = config;
  }

  @Nullable
  @Override
  public DevfileImpl getDevfile() {
    return devfile;
  }

  public WorkspaceImpl setDevfile(DevfileImpl devfile) {
    this.devfile = devfile;
    return this;
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
  public boolean isTemporary() {
    return isTemporary;
  }

  public void setTemporary(boolean temporary) {
    isTemporary = temporary;
  }

  @Override
  public WorkspaceStatus getStatus() {
    return status;
  }

  public void setStatus(WorkspaceStatus status) {
    this.status = status;
  }

  @Override
  public Runtime getRuntime() {
    return runtime;
  }

  public void setRuntime(Runtime runtime) {
    this.runtime = runtime;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof WorkspaceImpl)) return false;
    final WorkspaceImpl other = (WorkspaceImpl) obj;
    return Objects.equals(id, other.id)
        && Objects.equals(getNamespace(), other.getNamespace())
        && Objects.equals(status, other.status)
        && isTemporary == other.isTemporary
        && getAttributes().equals(other.getAttributes())
        && Objects.equals(config, other.config)
        && Objects.equals(devfile, other.devfile)
        && Objects.equals(runtime, other.runtime);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(id);
    hash = 31 * hash + Objects.hashCode(getNamespace());
    hash = 31 * hash + Objects.hashCode(status);
    hash = 31 * hash + Objects.hashCode(config);
    hash = 31 * hash + Objects.hashCode(devfile);
    hash = 31 * hash + getAttributes().hashCode();
    hash = 31 * hash + Boolean.hashCode(isTemporary);
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
        + getNamespace()
        + '\''
        + ", name='"
        + name
        + '\''
        + ", config="
        + config
        + ", devfile="
        + devfile
        + ", isTemporary="
        + isTemporary
        + ", status="
        + status
        + ", attributes="
        + attributes
        + ", runtime="
        + runtime
        + '}';
  }

  /** Syncs {@link #name} with config name or devfile name. */
  private void syncName() {
    name = getName();
  }

  /**
   * {@link PreUpdate} and {@link PrePersist} methods are not called when the configuration is
   * updated and workspace object is not, while listener methods are always called, even if
   * workspace instance is not changed.
   */
  public static class SyncNameOnUpdateAndPersistEventListener extends DescriptorEventAdapter {
    @Override
    public void preUpdate(DescriptorEvent event) {
      ((WorkspaceImpl) event.getObject()).syncName();
    }

    @Override
    public void prePersist(DescriptorEvent event) {
      ((WorkspaceImpl) event.getObject()).syncName();
    }

    @Override
    public void preUpdateWithChanges(DescriptorEvent event) {
      ((WorkspaceImpl) event.getObject()).syncName();
    }
  }

  /**
   * Helps to build complex {@link WorkspaceImpl workspace} instance.
   *
   * @see WorkspaceImpl#builder()
   */
  public static class WorkspaceImplBuilder {

    private String id;
    private Account account;
    private boolean isTemporary;
    private WorkspaceStatus status;
    private WorkspaceConfig config;
    private Devfile devfile;
    private Runtime runtime;
    private Map<String, String> attributes;

    public WorkspaceImpl build() {
      return new WorkspaceImpl(
          id, account, config, devfile, runtime, attributes, isTemporary, status);
    }

    public WorkspaceImplBuilder generateId() {
      id = NameGenerator.generate("workspace", 16);
      return this;
    }

    public WorkspaceImplBuilder setConfig(WorkspaceConfig workspaceConfig) {
      this.config = workspaceConfig;
      return this;
    }

    public WorkspaceImplBuilder setDevfile(Devfile devfile) {
      this.devfile = devfile;
      return this;
    }

    public WorkspaceImplBuilder setId(String id) {
      this.id = id;
      return this;
    }

    public WorkspaceImplBuilder setAccount(Account account) {
      this.account = account;
      return this;
    }

    public WorkspaceImplBuilder setTemporary(boolean isTemporary) {
      this.isTemporary = isTemporary;
      return this;
    }

    public WorkspaceImplBuilder setStatus(WorkspaceStatus status) {
      this.status = status;
      return this;
    }

    public WorkspaceImplBuilder setAttributes(Map<String, String> attributes) {
      this.attributes = attributes;
      return this;
    }

    public WorkspaceImplBuilder setRuntime(Runtime runtime) {
      this.runtime = runtime;
      return this;
    }
  }
}
