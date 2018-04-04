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
package org.eclipse.che.ide.api.workspace.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.eclipse.che.api.core.model.workspace.config.SourceStorage;

/** Data object for {@link SourceStorage}. */
public class SourceStorageImpl implements SourceStorage {

  private String type;
  private String location;
  private Map<String, String> parameters;

  public SourceStorageImpl(String type, String location, Map<String, String> parameters) {
    this.type = type;
    this.location = location;
    if (parameters != null) {
      this.parameters = new HashMap<>(parameters);
    }
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public String getLocation() {
    return location;
  }

  @Override
  public Map<String, String> getParameters() {
    if (parameters == null) {
      parameters = new HashMap<>();
    }
    return parameters;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof SourceStorageImpl)) {
      return false;
    }
    final SourceStorageImpl that = (SourceStorageImpl) obj;
    return Objects.equals(type, that.type)
        && Objects.equals(location, that.location)
        && getParameters().equals(that.getParameters());
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + Objects.hashCode(type);
    hash = 31 * hash + Objects.hashCode(location);
    hash = 31 * hash + getParameters().hashCode();
    return hash;
  }

  @Override
  public String toString() {
    return "SourceStorageImpl{"
        + "type='"
        + type
        + '\''
        + ", location='"
        + location
        + '\''
        + ", parameters="
        + parameters
        + '}';
  }
}
