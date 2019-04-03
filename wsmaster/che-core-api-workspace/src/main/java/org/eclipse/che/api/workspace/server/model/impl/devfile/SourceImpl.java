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
package org.eclipse.che.api.workspace.server.model.impl.devfile;

import java.util.Objects;
import org.eclipse.che.api.core.model.workspace.devfile.Source;

/** @author Sergii Leshchenko */
public class SourceImpl implements Source {

  private String type;
  private String location;
  private String refspec;

  public SourceImpl() {}

  public SourceImpl(String type, String location, String refspec) {
    this.type = type;
    this.location = location;
    this.refspec = refspec;
  }

  public SourceImpl(Source source) {
    this(source.getType(), source.getLocation(), source.getRefspec());
  }

  @Override
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  @Override
  public String getRefspec() {
    return refspec;
  }

  public void setRefspec(String refspec) {
    this.refspec = refspec;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SourceImpl)) {
      return false;
    }
    SourceImpl source = (SourceImpl) o;
    return Objects.equals(getType(), source.getType())
        && Objects.equals(getLocation(), source.getLocation())
        && Objects.equals(getRefspec(), source.getRefspec());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getType(), getLocation(), getRefspec());
  }

  @Override
  public String toString() {
    return "SourceImpl{"
        + "type='"
        + type
        + '\''
        + ", location='"
        + location
        + '\''
        + ", refspec='"
        + refspec
        + '\''
        + '}';
  }
}
