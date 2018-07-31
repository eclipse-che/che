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
package org.eclipse.che.multiuser.machine.authentication.server.signature.model.impl;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.eclipse.che.multiuser.machine.authentication.server.signature.SignatureKeyPair;

/** @author Anton Korneta */
@Entity(name = "SignKeyPair")
@Table(name = "che_sign_key_pair")
@NamedQueries({
  @NamedQuery(name = "SignKeyPair.getAll", query = "SELECT kp FROM SignKeyPair kp"),
  @NamedQuery(name = "SignKeyPair.getAllCount", query = "SELECT COUNT(kp) FROM SignKeyPair kp")
})
public class SignatureKeyPairImpl implements SignatureKeyPair {

  @Id
  @Column(name = "id")
  private String id;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "public_key")
  private SignatureKeyImpl publicKey;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "private_key")
  private SignatureKeyImpl privateKey;

  public SignatureKeyPairImpl() {}

  public SignatureKeyPairImpl(SignatureKeyPairImpl keyPair) {
    this(keyPair.getId(), keyPair.getPublicKey(), keyPair.getPrivateKey());
  }

  public SignatureKeyPairImpl(String id, PublicKey publicKey, PrivateKey privateKey) {
    this(id, new SignatureKeyImpl(publicKey), new SignatureKeyImpl(privateKey));
  }

  public SignatureKeyPairImpl(String id, SignatureKeyImpl publicKey, SignatureKeyImpl privateKey) {
    this.id = id;
    this.publicKey = publicKey;
    this.privateKey = privateKey;
  }

  @Override
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public SignatureKeyImpl getPublicKey() {
    return publicKey;
  }

  public void setPublicKey(SignatureKeyImpl publicKey) {
    this.publicKey = publicKey;
  }

  @Override
  public SignatureKeyImpl getPrivateKey() {
    return privateKey;
  }

  public void setPrivateKey(SignatureKeyImpl privateKey) {
    this.privateKey = privateKey;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof SignatureKeyPairImpl)) {
      return false;
    }
    final SignatureKeyPairImpl that = (SignatureKeyPairImpl) obj;
    return Objects.equals(id, that.id)
        && Objects.equals(publicKey, that.publicKey)
        && Objects.equals(privateKey, that.privateKey);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(id);
    hash = 31 * hash + Objects.hashCode(publicKey);
    hash = 31 * hash + Objects.hashCode(privateKey);
    return hash;
  }

  @Override
  public String toString() {
    return "SignatureKeyPairImpl{"
        + "id='"
        + id
        + '\''
        + ", publicKey="
        + publicKey
        + ", privateKey="
        + privateKey
        + '}';
  }
}
