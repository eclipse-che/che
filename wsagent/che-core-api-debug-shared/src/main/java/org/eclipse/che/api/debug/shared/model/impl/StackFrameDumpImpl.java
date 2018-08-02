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
package org.eclipse.che.api.debug.shared.model.impl;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.eclipse.che.api.debug.shared.dto.StackFrameDumpDto;
import org.eclipse.che.api.debug.shared.model.Field;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.StackFrameDump;
import org.eclipse.che.api.debug.shared.model.Variable;

/** @author Anatoliy Bazko */
public class StackFrameDumpImpl implements StackFrameDump {
  private final List<? extends Field> fields;
  private final List<? extends Variable> variables;
  private final Location location;

  public StackFrameDumpImpl(
      List<? extends Field> fields, List<? extends Variable> variables, Location location) {
    this.fields = fields;
    this.variables = variables;
    this.location = location;
  }

  public StackFrameDumpImpl(List<? extends Field> fields, List<? extends Variable> variables) {
    this(fields, variables, null);
  }

  public StackFrameDumpImpl(StackFrameDumpDto dto) {
    this(dto.getFields(), dto.getVariables(), dto.getLocation());
  }

  @Override
  public List<Field> getFields() {
    return Collections.unmodifiableList(fields);
  }

  @Override
  public List<Variable> getVariables() {
    return Collections.unmodifiableList(variables);
  }

  @Override
  public Location getLocation() {
    return location;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof StackFrameDumpImpl)) return false;

    StackFrameDumpImpl that = (StackFrameDumpImpl) o;

    return Objects.equals(fields, that.fields)
        && Objects.equals(variables, that.variables)
        && Objects.equals(location, that.location);
  }

  @Override
  public int hashCode() {
    return Objects.hash(fields, variables, location);
  }

  @Override
  public String toString() {
    return "StackFrameDumpImpl{"
        + " fields="
        + fields
        + ",variables = "
        + variables
        + ", location="
        + location
        + '}';
  }
}
