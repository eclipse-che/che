/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.jdb.server.expression;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;

/** @author andrew00x */
public class LocalValue implements ExpressionValue {
    private final ThreadReference thread;
    private final LocalVariable   variable;
    private       Value           value;

    public LocalValue(ThreadReference thread, LocalVariable variable) {
        this.thread = thread;
        this.variable = variable;
    }

    @Override
    public Value getValue() {
        if (value == null) {
            try {
                value = thread.frame(0).getValue(variable);
            } catch (IncompatibleThreadStateException | IllegalArgumentException | InvalidStackFrameException e) {
                throw new ExpressionException(e.getMessage(), e);
            }
        }
        return value;
    }

    @Override
    public void setValue(Value value) {
        try {
            thread.frame(0).setValue(variable, value);
        } catch (IncompatibleThreadStateException | InvalidTypeException | ClassNotLoadedException e) {
            throw new ExpressionException(e.getMessage(), e);
        }
        this.value = value;
    }
}
