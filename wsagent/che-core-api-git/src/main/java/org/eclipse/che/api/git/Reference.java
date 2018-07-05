/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
