/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.jdb.server.expression;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.StackFrame;
import com.sun.jdi.Value;

/** @author andrew00x */
public class LocalValue implements ExpressionValue {
  private final StackFrame jdiStackFrame;
  private final LocalVariable variable;
  private Value value;

  public LocalValue(StackFrame jdiStackFrame, LocalVariable variable) {
    this.jdiStackFrame = jdiStackFrame;
    this.variable = variable;
  }

  @Override
  public Value getValue() {
    if (value == null) {
      try {
        value = jdiStackFrame.getValue(variable);
      } catch (IllegalArgumentException | InvalidStackFrameException e) {
        throw new ExpressionException(e.getMessage(), e);
      }
    }
    return value;
  }

  @Override
  public void setValue(Value value) {
    try {
      jdiStackFrame.setValue(variable, value);
    } catch (InvalidTypeException | ClassNotLoadedException e) {
      throw new ExpressionException(e.getMessage(), e);
    }
    this.value = value;
  }
}
