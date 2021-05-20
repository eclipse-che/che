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
package org.eclipse.che.api.devfile.server.model.impl;

import com.google.common.annotations.Beta;
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
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.model.workspace.devfile.Devfile;
import org.eclipse.che.api.core.model.workspace.devfile.UserDevfile;
import org.eclipse.che.api.workspace.server.model.impl.devfile.DevfileImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.MetadataImpl;

@Entity(name = "UserDevfile")
@Table(name = "userdevfile")
@NamedQueries({
  @NamedQuery(
      name = "UserDevfile.getByNamespace",
      query = "SELECT d FROM UserDevfile d WHERE d.account.name = :namespace"),
  @NamedQuery(
      name = "UserDevfile.getByNamespaceCount",
      query = "SELECT COUNT(d) FROM UserDevfile d WHERE d.account.name = :namespace "),
  @NamedQuery(name = "UserDevfile.getAll", query = "SELECT d FROM UserDevfile d ORDER BY d.id"),
  @NamedQuery(name = "UserDevfile.getTotalCount", query = "SELECT COUNT(d) FROM UserDevfile d"),
})
@Beta
public class UserDevfileImpl implements UserDevfile {

  /**
   * In {@MetadataImpl} name is mandatory and generateName is transient. That is not suitable for
   * UserDevfile because we need to handle situations when the name is not defined and generateName
   * is defined. To workaround that original name and generateName stored in individual fields
   * meta_name and meta_generated_name. But at the same time, we can't leave metadata filed null in
   * devfile because of database hard constrain. To replace that FAKE_META is used.
   */
  private static final MetadataImpl FAKE_META = new MetadataImpl("name");

  @Id
  @Column(name = "id", nullable = false)
  private String id;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "devfile_id")
  private DevfileImpl devfile;

  @Column(name = "meta_generated_name")
  private String metaGeneratedName;

  @Column(name = "meta_name")
  private String metaName;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "description")
  private String description;

  @ManyToOne
  @JoinColumn(name = "accountid", nullable = false)
  private AccountImpl account;

  public UserDevfileImpl() {}

  public UserDevfileImpl(String id, Account account, UserDevfile userDevfile) {
    this(
        id, account, userDevfile.getName(), userDevfile.getDescription(), userDevfile.getDevfile());
  }

  public UserDevfileImpl(UserDevfile userDevfile, Account account) {
    this(userDevfile.getId(), account, userDevfile);
  }

  public UserDevfileImpl(UserDevfileImpl userDevfile) {
    this(
        userDevfile.id,
        userDevfile.account,
        userDevfile.getName(),
        userDevfile.getDescription(),
        userDevfile.getDevfile());
  }

  public UserDevfileImpl(
      String id, Account account, String name, String description, Devfile devfile) {
    this.id = id;
    this.account = new AccountImpl(account);
    this.name = name;
    this.description = description;
    this.devfile = new DevfileImpl(devfile);
    syncMeta();
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

  @Override
  public String getNamespace() {
    return account.getName();
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public Devfile getDevfile() {
    return new DevfileImpl(
        devfile.getApiVersion(),
        devfile.getProjects(),
        devfile.getComponents(),
        devfile.getCommands(),
        devfile.getAttributes(),
        new MetadataImpl(metaName, metaGeneratedName));
  }

  public void setDevfile(DevfileImpl devfile) {
    this.devfile = devfile;
    syncMeta();
  }

  public AccountImpl getAccount() {
    return account;
  }

  public void setAccount(AccountImpl account) {
    this.account = account;
  }

  private void syncMeta() {
    MetadataImpl metadata = devfile.getMetadata();
    metaGeneratedName = metadata.getGenerateName();
    metaName = metadata.getName();
    devfile.setMetadata(FAKE_META);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UserDevfileImpl that = (UserDevfileImpl) o;
    return Objects.equals(id, that.id)
        && Objects.equals(devfile, that.devfile)
        && Objects.equals(metaGeneratedName, that.metaGeneratedName)
        && Objects.equals(metaName, that.metaName)
        && Objects.equals(name, that.name)
        && Objects.equals(description, that.description)
        && Objects.equals(account, that.account);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, devfile, metaGeneratedName, metaName, name, description, account);
  }

  @Override
  public String toString() {
    return "UserDevfileImpl{"
        + "id='"
        + id
        + '\''
        + ", devfile="
        + devfile
        + ", metaGeneratedName='"
        + metaGeneratedName
        + '\''
        + ", metaName='"
        + metaName
        + '\''
        + ", name='"
        + name
        + '\''
        + ", description='"
        + description
        + '\''
        + ", account="
        + account
        + '}';
  }
}
