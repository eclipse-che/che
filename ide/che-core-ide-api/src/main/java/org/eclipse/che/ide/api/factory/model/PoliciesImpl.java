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
package org.eclipse.che.ide.api.factory.model;

import java.util.Objects;
import org.eclipse.che.api.core.model.factory.Policies;

/** Data object for {@link Policies}. */
public class PoliciesImpl implements Policies {

  private String referrer;
  private String create;
  private Long until;
  private Long since;

  public PoliciesImpl(String referrer, String create, Long until, Long since) {
    this.referrer = referrer;
    this.create = create;
    this.until = until;
    this.since = since;
  }

  public PoliciesImpl(Policies policies) {
    this(policies.getReferer(), policies.getCreate(), policies.getUntil(), policies.getSince());
  }

  @Override
  public String getReferer() {
    return referrer;
  }

  @Override
  public String getCreate() {
    return create;
  }

  @Override
  public Long getUntil() {
    return until;
  }

  @Override
  public Long getSince() {
    return since;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof PoliciesImpl)) return false;
    final PoliciesImpl other = (PoliciesImpl) obj;
    return Objects.equals(referrer, other.referrer)
        && Objects.equals(create, other.create)
        && Objects.equals(until, other.until)
        && Objects.equals(since, other.since);
  }

  @Override
  public int hashCode() {
    int result = 7;
    result = 31 * result + Objects.hashCode(referrer);
    result = 31 * result + Objects.hashCode(create);
    result = 31 * result + Objects.hashCode(until);
    result = 31 * result + Objects.hashCode(since);
    return result;
  }

  @Override
  public String toString() {
    return "PoliciesImpl{"
        + "referrer='"
        + referrer
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
