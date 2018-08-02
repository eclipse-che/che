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
package org.eclipse.che.multiuser.machine.authentication.server.signature.model.impl;

import java.security.Key;
import java.util.Arrays;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import org.eclipse.che.multiuser.machine.authentication.server.signature.SignatureKey;

/** @author Anton Korneta */
@Entity(name = "SignKey")
@Table(name = "che_sign_key")
public class SignatureKeyImpl implements SignatureKey {

  @Id
  @GeneratedValue
  @Column(name = "id")
  private Long id;

  @Column(name = "algorithm")
  private String algorithm;

  @Column(name = "encoding_format")
  private String format;

  @Column(name = "encoded_value")
  private byte[] encoded;

  public SignatureKeyImpl() {}

  public SignatureKeyImpl(Key publicKey) {
    this(publicKey.getEncoded(), publicKey.getAlgorithm(), publicKey.getFormat());
  }

  public SignatureKeyImpl(byte[] encoded, String algorithm, String format) {
    this.encoded = encoded;
    this.algorithm = algorithm;
    this.format = format;
  }

  @Override
  public String getAlgorithm() {
    return algorithm;
  }

  @Override
  public String getFormat() {
    return format;
  }

  @Override
  public byte[] getEncoded() {
    return encoded;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof SignatureKeyImpl)) {
      return false;
    }
    final SignatureKeyImpl that = (SignatureKeyImpl) obj;
    return Objects.equals(id, that.id)
        && Objects.equals(algorithm, that.algorithm)
        && Objects.equals(format, that.format)
        && Arrays.equals(encoded, that.encoded);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(id);
    hash = 31 * hash + Objects.hashCode(algorithm);
    hash = 31 * hash + Objects.hashCode(format);
    hash = 31 * hash + Arrays.hashCode(encoded);
    return hash;
  }

  @Override
  public String toString() {
    return "SignatureKeyImpl{"
        + "id="
        + id
        + ", algorithm='"
        + algorithm
        + '\''
        + ", format='"
        + format
        + '\''
        + ", encoded="
        + Arrays.toString(encoded)
        + '}';
  }
}
