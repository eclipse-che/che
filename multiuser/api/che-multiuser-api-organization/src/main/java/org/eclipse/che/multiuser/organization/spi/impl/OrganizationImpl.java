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
package org.eclipse.che.multiuser.organization.spi.impl;

import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.multiuser.organization.shared.model.Organization;

/**
 * Data object for {@link Organization}.
 *
 * @author Sergii Leschenko
 */
@Entity(name = "Organization")
@NamedQueries({
  @NamedQuery(
      name = "Organization.getByName",
      query = "SELECT o " + "FROM Organization o " + "WHERE o.account.name = :name"),
  @NamedQuery(
      name = "Organization.getByParent",
      query = "SELECT o " + "FROM Organization o " + "WHERE o.parent = :parent "),
  @NamedQuery(
      name = "Organization.getByParentCount",
      query = "SELECT COUNT(o) " + "FROM Organization o " + "WHERE o.parent = :parent "),
  @NamedQuery(
      name = "Organization.getSuborganizations",
      query = "SELECT o " + "FROM Organization o " + "WHERE o.account.name LIKE :qualifiedName "),
  @NamedQuery(
      name = "Organization.getSuborganizationsCount",
      query =
          "SELECT COUNT(o) " + "FROM Organization o " + "WHERE o.account.name LIKE :qualifiedName ")
})
@Table(name = "che_organization")
public class OrganizationImpl implements Organization {
  public static final String ORGANIZATIONAL_ACCOUNT = "organizational";

  @Id
  @Column(name = "id")
  private String id;

  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "account_id", nullable = false)
  private AccountImpl account;

  @Column(name = "parent")
  private String parent;

  // Mapping exists for explicit constraints which allows
  // jpa backend to perform operations in correct order
  @ManyToOne
  @JoinColumn(name = "parent", insertable = false, updatable = false)
  private OrganizationImpl parentObj;

  public OrganizationImpl() {}

  public OrganizationImpl(Organization organization) {
    this(organization.getId(), organization.getQualifiedName(), organization.getParent());
  }

  public OrganizationImpl(String id, String qualifiedName, String parent) {
    this.id = id;
    this.account = new AccountImpl(id, qualifiedName, ORGANIZATIONAL_ACCOUNT);
    this.parent = parent;
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
    String qualifiedName = getQualifiedName();
    if (qualifiedName == null) {
      return null;
    }

    int lastSlashIndex = qualifiedName.lastIndexOf("/");

    if (lastSlashIndex == -1) {
      return qualifiedName;
    }

    return qualifiedName.substring(lastSlashIndex + 1);
  }

  @Override
  public String getQualifiedName() {
    if (account != null) {
      return account.getName();
    }
    return null;
  }

  public void setQualifiedName(String qualifiedName) {
    if (account != null) {
      account.setName(qualifiedName);
    }
  }

  @Override
  public String getParent() {
    return parent;
  }

  public void setParent(String parent) {
    this.parent = parent;
  }

  public AccountImpl getAccount() {
    return account;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof OrganizationImpl)) {
      return false;
    }
    OrganizationImpl that = (OrganizationImpl) o;
    return Objects.equals(id, that.id)
        && Objects.equals(getName(), that.getName())
        && Objects.equals(parent, that.parent);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(id);
    hash = 31 * hash + Objects.hashCode(getName());
    hash = 31 * hash + Objects.hashCode(getQualifiedName());
    hash = 31 * hash + Objects.hashCode(parent);
    return hash;
  }

  @Override
  public String toString() {
    return "OrganizationImpl{"
        + "id='"
        + id
        + '\''
        + ", name='"
        + getName()
        + '\''
        + ", qualifiedName='"
        + getQualifiedName()
        + '\''
        + ", parent='"
        + parent
        + '\''
        + '}';
  }
}
