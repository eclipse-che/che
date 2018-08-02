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
package org.eclipse.che.api.git;

/** @author Igor Vinokur */
public class Reference {
  private final String refName;
  private final ReferenceType type;

  /**
   * Represents Git reference e.g. branch, tag or commit id.
   *
   * @param refName reference name
   * @param type reference type
   */
  protected Reference(String refName, ReferenceType type) {
    this.refName = refName;
    this.type = type;
  }

  /** Returns reference name. */
  public String getName() {
    return refName;
  }

  /** Returns reference type */
  public ReferenceType getType() {
    return type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Reference)) return false;

    Reference reference = (Reference) o;

    if (refName != null ? !refName.equals(reference.refName) : reference.refName != null)
      return false;
    return getType() == reference.getType();
  }

  @Override
  public int hashCode() {
    int result = refName != null ? refName.hashCode() : 0;
    result = 31 * result + (getType() != null ? getType().hashCode() : 0);
    return result;
  }
}
