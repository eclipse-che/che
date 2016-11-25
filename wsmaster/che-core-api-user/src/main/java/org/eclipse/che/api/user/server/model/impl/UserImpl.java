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
package org.eclipse.che.api.user.server.model.impl;

import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.user.server.jpa.UserEntityListener;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Data object for the {@link User}.
 *
 * @author Yevhenii Voevodin
 */
@Entity(name = "Usr")
@NamedQueries(
        {
                @NamedQuery(name = "User.getByAliasAndPassword",
                            query = "SELECT u " +
                                    "FROM Usr u " +
                                    "WHERE :alias = u.account.name OR" +
                                    "      :alias = u.email"),
                @NamedQuery(name = "User.getByAlias",
                            query = "SELECT u FROM Usr u WHERE :alias MEMBER OF u.aliases"),
                @NamedQuery(name = "User.getByName",
                            query = "SELECT u FROM Usr u WHERE u.account.name = :name"),
                @NamedQuery(name = "User.getByEmail",
                            query = "SELECT u FROM Usr u WHERE u.email = :email"),
                @NamedQuery(name = "User.getAll",
                            query = "SELECT u FROM Usr u"),
                @NamedQuery(name = "User.getTotalCount",
                            query = "SELECT COUNT(u) FROM Usr u")

        }
)
@EntityListeners(UserEntityListener.class)
@Table(name = "usr")
public class UserImpl implements User {
    public static final String PERSONAL_ACCOUNT = "personal";

    @Id
    @Column(name = "id")
    private String id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(nullable = false, name = "account_id")
    private AccountImpl account;

    @Column(nullable = false, name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @ElementCollection
    @Column(name = "alias", nullable = false, unique = true)
    @CollectionTable(name = "user_aliases",
                     indexes = @Index(columnList = "alias"),
                     joinColumns = @JoinColumn(name = "user_id"))
    private List<String> aliases;

    public UserImpl() {
        this.account = new AccountImpl();
        account.setType(PERSONAL_ACCOUNT);
    }

    public UserImpl(String id, String email, String name) {
        this.account = new AccountImpl(id, name, PERSONAL_ACCOUNT);
        this.id = id;
        this.email = email;
    }

    public UserImpl(String id,
                    String email,
                    String name,
                    String password,
                    Collection<String> aliases) {
        this(id, email, name);
        this.password = password;
        if (aliases != null) {
            this.aliases = new ArrayList<>(aliases);
        }
    }

    public UserImpl(User user) {
        this(user.getId(),
             user.getEmail(),
             user.getName(),
             user.getPassword(),
             user.getAliases());
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        if (account != null) {
            account.setId(id);
        }
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
        if (account != null) {
            return account.getName();
        }
        return null;
    }

    public void setName(String name) {
        if (account != null) {
            account.setName(name);
        }
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

    public AccountImpl getAccount() {
        return account;
    }

    public void setAccount(AccountImpl account) {
        this.account = account;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof UserImpl)) {
            return false;
        }
        final UserImpl that = (UserImpl)obj;
        return Objects.equals(id, that.id)
               && Objects.equals(email, that.email)
               && Objects.equals(getName(), that.getName())
               && Objects.equals(password, that.password)
               && getAliases().equals(that.getAliases());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(id);
        hash = 31 * hash + Objects.hashCode(email);
        hash = 31 * hash + Objects.hashCode(getName());
        hash = 31 * hash + Objects.hashCode(password);
        hash = 31 * hash + getAliases().hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return "UserImpl{" +
               "id='" + id + '\'' +
               ", email='" + email + '\'' +
               ", name='" + getName() + '\'' +
               ", password='" + password + '\'' +
               ", aliases=" + aliases +
               '}';
    }
}
