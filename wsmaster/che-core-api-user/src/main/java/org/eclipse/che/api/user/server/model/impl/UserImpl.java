/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.user.server.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import org.eclipse.che.api.core.model.user.User;

/**
 * Data object for the {@link User}.
 *
 * @author Yevhenii Voevodin
 */
@Entity(name = "Usr")
@NamedQueries({
  @NamedQuery(
    name = "User.getByAliasAndPassword",
    query = "SELECT u " + "FROM Usr u " + "WHERE :alias = u.name OR" + "      :alias = u.email"
  ),
  @NamedQuery(
    name = "User.getByAlias",
    query = "SELECT u FROM Usr u WHERE :alias MEMBER OF u.aliases"
  ),
  @NamedQuery(name = "User.getByName", query = "SELECT u FROM Usr u WHERE u.name = :name"),
  @NamedQuery(name = "User.getByEmail", query = "SELECT u FROM Usr u WHERE u.email = :email"),
  @NamedQuery(name = "User.getAll", query = "SELECT u FROM Usr u"),
  @NamedQuery(name = "User.getTotalCount", query = "SELECT COUNT(u) FROM Usr u"),
  @NamedQuery(
    name = "User.getByEmailPart",
    query = "SELECT u FROM Usr u WHERE LOWER(u.email) LIKE CONCAT('%', :email, '%')"
  ),
  @NamedQuery(
    name = "User.getByEmailPartCount",
    query = "SELECT COUNT(u) FROM Usr u WHERE LOWER(u.email) LIKE CONCAT('%', :email, '%')"
  ),
  @NamedQuery(
    name = "User.getByNamePart",
    query = "SELECT u FROM Usr u WHERE LOWER(u.name) LIKE CONCAT('%', :name, '%')"
  ),
  @NamedQuery(
    name = "User.getByNamePartCount",
    query = "SELECT COUNT(u) FROM Usr u WHERE LOWER(u.name) LIKE CONCAT('%', :name, '%')"
  )
})
@Table(name = "usr")
public class UserImpl implements User {
  @Id
  @Column(name = "id")
  private String id;

  @Column(nullable = false, name = "email")
  private String email;

  @Column(nullable = false, name = "name")
  private String name;

  @Column(name = "password")
  private String password;

  @ElementCollection
  @Column(name = "alias", nullable = false, unique = true)
  @CollectionTable(
    name = "user_aliases",
    indexes = @Index(columnList = "alias"),
    joinColumns = @JoinColumn(name = "user_id")
  )
  private List<String> aliases;

  public UserImpl() {}

  public UserImpl(String id, String email, String name) {
    this.id = id;
    this.name = name;
    this.email = email;
  }

  public UserImpl(
      String id, String email, String name, String password, Collection<String> aliases) {
    this(id, email, name);
    this.password = password;
    if (aliases != null) {
      this.aliases = new ArrayList<>(aliases);
    }
  }

  public UserImpl(User user) {
    this(user.getId(), user.getEmail(), user.getName(), user.getPassword(), user.getAliases());
  }

  @Override
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  @Override
  public List<String> getAliases() {
    if (aliases == null) {
      aliases = new ArrayList<>();
    }
    return aliases;
  }

  public void setAliases(List<String> aliases) {
    this.aliases = aliases;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof UserImpl)) {
      return false;
    }
    final UserImpl that = (UserImpl) obj;
    return Objects.equals(id, that.id)
        && Objects.equals(email, that.email)
        && Objects.equals(name, that.name)
        && Objects.equals(password, that.password)
        && getAliases().equals(that.getAliases());
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(id);
    hash = 31 * hash + Objects.hashCode(email);
    hash = 31 * hash + Objects.hashCode(name);
    hash = 31 * hash + Objects.hashCode(password);
    hash = 31 * hash + getAliases().hashCode();
    return hash;
  }

  @Override
  public String toString() {
    return "UserImpl{"
        + "id='"
        + id
        + '\''
        + ", email='"
        + email
        + '\''
        + ", name='"
        + name
        + '\''
        + ", password='"
        + password
        + '\''
        + ", aliases="
        + aliases
        + '}';
  }
}
