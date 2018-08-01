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

import com.sun.jdi.AbsentInformationException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.eclipse.che.api.debug.shared.model.Method;
import org.eclipse.che.api.debug.shared.model.SimpleValue;
import org.eclipse.che.api.debug.shared.model.Variable;

/**
 * {@link org.eclipse.che.api.debug.shared.model.Method} implementation for Java Debugger.
 *
 * @author Anatolii Bazko
 */
public class JdbMethod implements Method {
  private final com.sun.jdi.Method jdiMethod;

  private final AtomicReference<List<Variable>> arguments;

  public JdbMethod(com.sun.jdi.StackFrame jdiStackFrame) {
    this.jdiMethod = jdiStackFrame.location().method();
    this.arguments = new AtomicReference<>();
  }

  @Override
  public String getName() {
    return jdiMethod.name();
  }

  @Override
  public List<Variable> getArguments() {
    if (arguments.get() == null) {
      synchronized (arguments) {
        if (arguments.get() == null) {
          try {
            // to reduce unnecessary requests. Value can be retrieved on demand throw
            // Debugger.getValue() method
            arguments.set(
                jdiMethod
                    .arguments()
                    .stream()
                    .map(v -> new JdbVariable((SimpleValue) null, v))
                    .collect(Collectors.toList()));
          } catch (AbsentInformationException e) {
            arguments.set(Collections.emptyList());
          }
        }
      }
    }

    return arguments.get();
  }
}
