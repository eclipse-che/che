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
package org.eclipse.che.plugin.jdb.server.model;

import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.Value;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.che.api.debug.shared.model.SimpleValue;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.debug.shared.model.VariablePath;
import org.eclipse.che.api.debug.shared.model.impl.VariablePathImpl;

/**
 * @author andrew00x
 * @author Anatolii Bazko
 */
public class JdbArrayElement implements Variable {
  private final Value jdiValue;
  private final SimpleValue value;
  private final String name;
  private final String type;
  private final VariablePath parentPath;

  public JdbArrayElement(Value jdiValue, int index, VariablePath parentPath) {
    this.jdiValue = jdiValue;
    this.name = "[" + index + "]";
    this.parentPath = parentPath;
    this.value = jdiValue == null ? new JdbNullValue() : new JdbValue(jdiValue, getVariablePath());
    this.type = jdiValue == null ? "null" : jdiValue.type().name();
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean isPrimitive() {
    return jdiValue instanceof PrimitiveValue;
  }

  @Override
  public SimpleValue getValue() {
    return value;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public VariablePath getVariablePath() {
    List<String> pathEntries = new LinkedList<>(parentPath.getPath());
    pathEntries.add(getName());
    return new VariablePathImpl(pathEntries);
  }
}
