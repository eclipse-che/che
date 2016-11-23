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
package org.eclipse.che.api.ssh.server.model.impl;

import org.eclipse.che.api.ssh.server.jpa.SshPairPrimaryKey;
import org.eclipse.che.api.ssh.shared.model.SshPair;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.commons.annotation.Nullable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.util.Objects;

/**
 * @author Sergii Leschenko
 */
@Entity(name = "SshKeyPair")
@NamedQueries(
        {
                @NamedQuery(name = "SshKeyPair.getByOwnerAndService",
                            query = "SELECT pair " +
                                    "FROM SshKeyPair pair " +
                                    "WHERE pair.owner = :owner " +
                                    "  AND pair.service = :service"),
                @NamedQuery(name = "SshKeyPair.getByOwner",
                            query = "SELECT pair " +
                                    "FROM SshKeyPair pair " +
                                    "WHERE pair.owner = :owner")
        }
)
@IdClass(SshPairPrimaryKey.class)
@Table(name = "sshkeypair")
public class SshPairImpl implements SshPair {
    @Id
    @Column(name = "owner")
    private String owner;

    @Id
    @Column(name = "service")
    private String service;

    @Id
    @Column(name = "name")
    private String name;

    @Column(name = "publickey", columnDefinition = "TEXT")
    private String publicKey;

    @Column(name = "privatekey", columnDefinition = "TEXT")
    private String privateKey;

    @ManyToOne
    @JoinColumn(name = "owner", insertable = false, updatable = false)
    private UserImpl user;

    public SshPairImpl() {
    }

    public SshPairImpl(String owner, String service, String name, String publicKey, String privateKey) {
        this.owner = owner;
        this.service = service;
        this.name = name;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public SshPairImpl(String owner, SshPair sshPair) {
        this.owner = owner;
        this.service = sshPair.getService();
        this.name = sshPair.getName();
        this.publicKey = sshPair.getPublicKey();
        this.privateKey = sshPair.getPrivateKey();
    }

    public SshPairImpl(SshPairImpl sshPair) {
        this(sshPair.owner, sshPair.service, sshPair.name, sshPair.publicKey, sshPair.privateKey);
    }

    public String getOwner() {
        return owner;
    }

    @Override
    public String getService() {
        return service;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    @Nullable
    public String getPublicKey() {
        return publicKey;
    }

    @Override
    @Nullable
    public String getPrivateKey() {
        return privateKey;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SshPairImpl)) return false;
        final SshPairImpl other = (SshPairImpl)obj;
        return Objects.equals(owner, other.owner) &&
               Objects.equals(service, other.service) &&
               Objects.equals(name, other.name) &&
               Objects.equals(publicKey, other.publicKey) &&
               Objects.equals(privateKey, other.privateKey);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(owner);
        hash = 31 * hash + Objects.hashCode(service);
        hash = 31 * hash + Objects.hashCode(name);
        hash = 31 * hash + Objects.hashCode(publicKey);
        hash = 31 * hash + Objects.hashCode(privateKey);
        return hash;
    }

    @Override
    public String toString() {
        return "SshPairImpl{" +
               "owner='" + owner + '\'' +
               ", service='" + service + '\'' +
               ", name='" + name + '\'' +
               ", publicKey='" + publicKey + '\'' +
               ", privateKey='" + privateKey + '\'' +
               '}';
    }
}
