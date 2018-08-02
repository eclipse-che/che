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
package org.eclipse.che.api.factory.server.model.impl;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import org.eclipse.che.api.core.model.factory.Policies;

/**
 * Data object for {@link Policies}.
 *
 * @author Anton Korneta
 */
@Embeddable
public class PoliciesImpl implements Policies {

  @Column(name = "referrer")
  private String referer;

  @Column(name = "creation_strategy")
  private String create;

  @Column(name = "until")
  private Long until;

  @Column(name = "since")
  private Long since;

  public PoliciesImpl() {}

  public PoliciesImpl(String referer, String create, Long until, Long since) {
    this.referer = referer;
    this.create = create;
    this.until = until;
    this.since = since;
  }

  public PoliciesImpl(Policies policies) {
    this(policies.getReferer(), policies.getCreate(), policies.getUntil(), policies.getSince());
  }

  @Override
  public String getReferer() {
    return referer;
  }

  public void setReferer(String referer) {
    this.referer = referer;
  }

  @Override
  public String getCreate() {
    return create;
  }

  public void setCreate(String create) {
    this.create = create;
  }

  @Override
  public Long getUntil() {
    return until;
  }

  public void setUntil(Long until) {
    this.until = until;
  }

  @Override
  public Long getSince() {
    return since;
  }

  public void setSince(Long since) {
    this.since = since;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof PoliciesImpl)) return false;
    final PoliciesImpl other = (PoliciesImpl) obj;
    return Objects.equals(referer, other.referer)
        && Objects.equals(create, other.create)
        && Objects.equals(until, other.until)
        && Objects.equals(since, other.since);
  }

  @Override
  public int hashCode() {
    int result = 7;
    result = 31 * result + Objects.hashCode(referer);
    result = 31 * result + Objects.hashCode(create);
    result = 31 * result + Objects.hashCode(until);
    result = 31 * result + Objects.hashCode(since);
    return result;
  }

  @Override
  public String toString() {
    return "PoliciesImpl{"
        + "referer='"
        + referer
        + '\''
        + ", create='"
        + create
        + '\''
        + ", until="
        + until
        + ", since="
        + since
        + '}';
  }
}
