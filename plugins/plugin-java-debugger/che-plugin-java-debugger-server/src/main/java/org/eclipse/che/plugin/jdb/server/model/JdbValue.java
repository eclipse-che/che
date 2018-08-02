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

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.Value;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.eclipse.che.api.debug.shared.model.SimpleValue;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.debug.shared.model.VariablePath;

/**
 * {@link org.eclipse.che.api.debug.shared.model.SimpleValue} implementation for Java Debugger.
 *
 * @author Anatolii Bazko
 */
public class JdbValue implements SimpleValue {
  private final Value jdiValue;
  private final AtomicReference<List<Variable>> variables;
  private final VariablePath variablePath;

  public JdbValue(Value jdiValue, VariablePath variablePath) {
    this.jdiValue = jdiValue;
    this.variables = new AtomicReference<>();
    this.variablePath = variablePath;
  }

  @Override
  public String getString() {
    return jdiValue.toString();
  }

  @Override
  public List<Variable> getVariables() {
    if (variables.get() == null) {
      synchronized (variables) {
        if (variables.get() == null) {
          if (isPrimitive()) {
            variables.set(Collections.emptyList());
          } else if (isArray()) {
            variables.set(new LinkedList<>());

            ArrayReference array = (ArrayReference) jdiValue;
            for (int i = 0; i < array.length(); i++) {
              variables.get().add(new JdbArrayElement(array.getValue(i), i, variablePath));
            }
          } else {
            ObjectReference object = (ObjectReference) jdiValue;
            variables.set(
                object
                    .referenceType()
                    .allFields()
                    .stream()
                    .map(f -> new JdbField(f, object, variablePath))
                    .sorted(new JdbFieldComparator())
                    .collect(Collectors.toList()));
          }
        }
      }
    }

    return variables.get();
  }

  private boolean isArray() {
    return jdiValue instanceof ArrayReference;
  }

  private boolean isPrimitive() {
    return jdiValue instanceof PrimitiveValue;
  }
}
