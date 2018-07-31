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
package org.eclipse.che.account.spi;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import org.eclipse.che.account.shared.model.Account;

/**
 * Data object for {@link Account}.
 *
 * @author Sergii Leschenko
 * @author Yevhenii Voevodin
 */
@Entity(name = "Account")
@NamedQueries({
  @NamedQuery(
    name = "Account.getByName",
    query = "SELECT a " + "FROM Account a " + "WHERE a.name = :name"
  )
})
@Table(name = "account")
public class AccountImpl implements Account {

  @Id
  @Column(name = "id")
  protected String id;

  @Column(nullable = false, name = "name")
  protected String name;

  @Column(name = "type")
  private String type;

  public AccountImpl() {}

  public AccountImpl(String id, String name, String type) {
    this.id = id;
    this.name = name;
    this.type = type;
  }

  public AccountImpl(Account account) {
    this(account.getId(), account.getName(), account.getType());
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
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof AccountImpl)) return false;
    AccountImpl account = (AccountImpl) o;
    return Objects.equals(id, account.id) && Objects.equals(name, account.name);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hash(id);
    hash = 31 * hash + Objects.hash(name);
    return hash;
  }

  @Override
  public String toString() {
    return "AccountImpl{" + "id='" + id + '\'' + ", name='" + name + '\'' + '}';
  }
}
