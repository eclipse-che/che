/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.account.spi;

import org.eclipse.che.account.shared.model.Account;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.util.Objects;

/**
 * Data object for {@link Account}.
 *
 * @author Sergii Leschenko
 */
@Entity(name = "Account")
@Inheritance(strategy = InheritanceType.JOINED)
@NamedQueries(
        {
                @NamedQuery(name = "Account.getByName",
                            query = "SELECT a " +
                                    "FROM Account a " +
                                    "WHERE a.name = :name")
        }
)
@Table(indexes = @Index(columnList = "name", unique = true))
public abstract class AccountImpl implements Account {
    @Id
    protected String id;

    @Column(nullable = false)
    protected String name;

    public AccountImpl() {}

    public AccountImpl(String id, String name) {
        this.id = id;
        this.name = name;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AccountImpl)) return false;
        AccountImpl account = (AccountImpl)o;
        return Objects.equals(id, account.id) &&
               Objects.equals(name, account.name);
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
        return "AccountImpl{" +
               "id='" + id + '\'' +
               ", name='" + name + '\'' +
               '}';
    }
}
