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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.eclipse.che.api.debug.shared.model.Field;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.StackFrameDump;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.api.debug.shared.model.impl.VariablePathImpl;
import org.eclipse.che.plugin.java.languageserver.JavaLanguageServerExtensionService;

/**
 * /** {@link org.eclipse.che.api.debug.shared.model.StackFrameDump} implementation for Java
 * Debugger.
 *
 * @author andrew00x
 * @author Anatolii Bazko
 */
public class JdbStackFrame implements StackFrameDump {
  private final com.sun.jdi.StackFrame jdiStackFrame;

  private final AtomicReference<List<Field>> fields;
  private final AtomicReference<List<Variable>> variables;
  private final Location location;

  public JdbStackFrame(
      JavaLanguageServerExtensionService languageServer, com.sun.jdi.StackFrame jdiStackFrame) {
    this.jdiStackFrame = jdiStackFrame;
    this.location = new JdbLocation(languageServer, jdiStackFrame);
    this.variables = new AtomicReference<>();
    this.fields = new AtomicReference<>();
  }

  public JdbStackFrame(
      com.sun.jdi.StackFrame jdiStackFrame,
      List<Field> fields,
      List<Variable> variables,
      Location location) {
    this.jdiStackFrame = jdiStackFrame;
    this.fields = new AtomicReference<>(fields);
    this.variables = new AtomicReference<>(variables);
    this.location = location;
  }

  @Override
  public List<Field> getFields() {
    if (fields.get() == null) {
      synchronized (fields) {
        if (fields.get() == null) {
          try {
            ObjectReference object = jdiStackFrame.thisObject();
            if (object == null) {
              ReferenceType type = jdiStackFrame.location().declaringType();
              fields.set(
                  jdiStackFrame
                      .location()
                      .declaringType()
                      .allFields()
                      .stream()
                      .map(
                          f -> new JdbField(f, type, new VariablePathImpl(Collections.emptyList())))
                      .sorted(new JdbFieldComparator())
                      .collect(Collectors.toList()));
            } else {
              fields.set(
                  object
                      .referenceType()
                      .allFields()
                      .stream()
                      .map(
                          f ->
                              new JdbField(
                                  f, object, new VariablePathImpl(Collections.emptyList())))
                      .sorted(new JdbFieldComparator())
                      .collect(Collectors.toList()));
            }

          } catch (Exception e) {
            fields.set(Collections.emptyList());
          }
        }
      }
    }

    return fields.get();
  }

  @Override
  public List<Variable> getVariables() {
    if (variables.get() == null) {
      synchronized (variables) {
        if (variables.get() == null) {
          try {
            variables.set(
                jdiStackFrame
                    .visibleVariables()
                    .stream()
                    .map(v -> new JdbVariable(jdiStackFrame, v))
                    .collect(Collectors.toList()));
          } catch (Exception e) {
            variables.set(Collections.emptyList());
          }
        }
      }
    }

    return variables.get();
  }

  @Override
  public Location getLocation() {
    return location;
  }
}
