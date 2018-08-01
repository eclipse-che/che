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

import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.che.api.debug.shared.model.Field;
import org.eclipse.che.api.debug.shared.model.SimpleValue;
import org.eclipse.che.api.debug.shared.model.VariablePath;
import org.eclipse.che.api.debug.shared.model.impl.VariablePathImpl;

/**
 * Java debugger implementation of {@link Field}
 *
 * @author andrew00x
 * @author Anatolii Bazko
 */
public class JdbField implements Field {
  private final com.sun.jdi.Field jdiField;

  private final ReferenceType type;
  private final ObjectReference object;
  private final VariablePath parentPath;

  public JdbField(com.sun.jdi.Field jdiField, ObjectReference object, VariablePath parentPath) {
    this.jdiField = jdiField;
    this.object = object;
    this.type = null;
    this.parentPath = parentPath;
  }

  public JdbField(com.sun.jdi.Field jdiField, ReferenceType type, VariablePath parentPath) {
    this.jdiField = jdiField;
    this.type = type;
    this.object = null;
    this.parentPath = parentPath;
  }

  @Override
  public String getName() {
    return jdiField.name();
  }

  @Override
  public boolean isIsStatic() {
    return jdiField.isStatic();
  }

  @Override
  public boolean isIsTransient() {
    return jdiField.isTransient();
  }

  @Override
  public boolean isIsVolatile() {
    return jdiField.isVolatile();
  }

  @Override
  public boolean isIsFinal() {
    return jdiField.isFinal();
  }

  @Override
  public boolean isPrimitive() {
    return JdbType.isPrimitive(jdiField.signature());
  }

  @Override
  public SimpleValue getValue() {
    Value value = object == null ? type.getValue(jdiField) : object.getValue(jdiField);
    if (value == null) {
      return new JdbNullValue();
    }
    return new JdbValue(value, getVariablePath());
  }

  @Override
  public String getType() {
    return jdiField.typeName();
  }

  @Override
  public VariablePath getVariablePath() {
    List<String> pathEntries = new LinkedList<>();

    if (parentPath.getPath().isEmpty()) {
      pathEntries.add(isIsStatic() ? "static" : "this");
    } else {
      pathEntries.addAll(parentPath.getPath());
    }
    pathEntries.add(getName());

    return new VariablePathImpl(pathEntries);
  }
}
