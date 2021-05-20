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
package org.eclipse.che.api.factory.server.model.impl;

import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.eclipse.che.api.core.model.factory.Author;
import org.eclipse.che.api.core.model.factory.Factory;
import org.eclipse.che.api.core.model.factory.Ide;
import org.eclipse.che.api.core.model.factory.Policies;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.commons.lang.NameGenerator;

/**
 * Data object for {@link Factory}.
 *
 * @author Anton Korneta
 */
@Entity(name = "Factory")
@Table(name = "che_factory")
// TODO fix after issue: https://github.com/eclipse/che/issues/2110
// (uniqueConstraints = {@UniqueConstraint(columnNames = {"name", "userId"})})
public class FactoryImpl implements Factory {

  public static FactoryImplBuilder builder() {
    return new FactoryImplBuilder();
  }

  @Id
  @Column(name = "id")
  private String id;

  @Column(name = "name")
  private String name;

  @Column(name = "version", nullable = false)
  private String version;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
  @JoinColumn(name = "workspace_id")
  private WorkspaceConfigImpl workspace;

  @Embedded private AuthorImpl creator;

  // Mapping exists for explicit constraints which allows
  // jpa backend to perform operations in correct order
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(insertable = false, updatable = false, name = "user_id")
  private UserImpl userEntity;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "ide_id")
  private IdeImpl ide;

  @Embedded private PoliciesImpl policies;

  public FactoryImpl() {}

  public FactoryImpl(
      String id,
      String name,
      String version,
      WorkspaceConfig workspace,
      Author creator,
      Policies policies,
      Ide ide) {
    this.id = id;
    this.name = name;
    this.version = version;
    if (workspace != null) {
      this.workspace = new WorkspaceConfigImpl(workspace);
    }
    if (creator != null) {
      this.creator = new AuthorImpl(creator);
    }
    if (policies != null) {
      this.policies = new PoliciesImpl(policies);
    }
    if (ide != null) {
      this.ide = new IdeImpl(ide);
    }
  }

  public FactoryImpl(Factory factory) {
    this(
        factory.getId(),
        factory.getName(),
        factory.getV(),
        factory.getWorkspace(),
        factory.getCreator(),
        factory.getPolicies(),
        factory.getIde());
  }

  @Override
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getV() {
    return version;
  }

  public void setV(String version) {
    this.version = version;
  }

  @Override
  public WorkspaceConfigImpl getWorkspace() {
    return workspace;
  }

  public void setWorkspace(WorkspaceConfigImpl workspace) {
    this.workspace = workspace;
  }

  @Override
  public AuthorImpl getCreator() {
    return creator;
  }

  public void setCreator(AuthorImpl creator) {
    this.creator = creator;
  }

  @Override
  public PoliciesImpl getPolicies() {
    return policies;
  }

  public void setPolicies(PoliciesImpl policies) {
    this.policies = policies;
  }

  @Override
  public IdeImpl getIde() {
    return ide;
  }

  public void setIde(IdeImpl ide) {
    this.ide = ide;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof FactoryImpl)) return false;
    final FactoryImpl other = (FactoryImpl) obj;
    return Objects.equals(id, other.id)
        && Objects.equals(name, other.name)
        && Objects.equals(version, other.version)
        && Objects.equals(workspace, other.workspace)
        && Objects.equals(creator, other.creator)
        && Objects.equals(policies, other.policies)
        && Objects.equals(ide, other.ide);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(id);
    hash = 31 * hash + Objects.hashCode(name);
    hash = 31 * hash + Objects.hashCode(version);
    hash = 31 * hash + Objects.hashCode(workspace);
    hash = 31 * hash + Objects.hashCode(creator);
    hash = 31 * hash + Objects.hashCode(policies);
    hash = 31 * hash + Objects.hashCode(ide);
    return hash;
  }

  @Override
  public String toString() {
    return "FactoryImpl{"
        + "id='"
        + id
        + '\''
        + ", name='"
        + name
        + '\''
        + ", version='"
        + version
        + '\''
        + ", workspace="
        + workspace
        + ", creator="
        + creator
        + ", policies="
        + policies
        + ", ide="
        + ide
        + '}';
  }

  /** Helps to create the instance of {@link FactoryImpl}. */
  public static class FactoryImplBuilder {

    private String id;
    private String name;
    private String version;
    private WorkspaceConfig workspace;
    private Author creator;
    private Policies policies;
    private Ide ide;

    private FactoryImplBuilder() {}

    public FactoryImpl build() {
      return new FactoryImpl(id, name, version, workspace, creator, policies, ide);
    }

    public FactoryImplBuilder from(FactoryImpl factory) {
      this.id = factory.getId();
      this.name = factory.getName();
      this.version = factory.getV();
      this.workspace = factory.getWorkspace();
      this.creator = factory.getCreator();
      this.policies = factory.getPolicies();
      this.ide = factory.getIde();
      return this;
    }

    public FactoryImplBuilder generateId() {
      id = NameGenerator.generate("", 16);
      return this;
    }

    public FactoryImplBuilder setId(String id) {
      this.id = id;
      return this;
    }

    public FactoryImplBuilder setName(String name) {
      this.name = name;
      return this;
    }

    public FactoryImplBuilder setVersion(String version) {
      this.version = version;
      return this;
    }

    public FactoryImplBuilder setWorkspace(WorkspaceConfig workspace) {
      this.workspace = workspace;
      return this;
    }

    public FactoryImplBuilder setCreator(Author creator) {
      this.creator = creator;
      return this;
    }

    public FactoryImplBuilder setPolicies(Policies policies) {
      this.policies = policies;
      return this;
    }

    public FactoryImplBuilder setIde(Ide ide) {
      this.ide = ide;
      return this;
    }
  }
}
