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
package org.eclipse.che.plugin.jdb.server.model;

import com.sun.jdi.AbsentInformationException;

import org.eclipse.che.api.debug.shared.model.Method;
import org.eclipse.che.api.debug.shared.model.Variable;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * {@link org.eclipse.che.api.debug.shared.model.Method} implementation for Java Debugger.
 *
 * @author Anatolii Bazko
 */
public class JdbMethod implements Method {
    private final com.sun.jdi.Method     method;
    private final com.sun.jdi.StackFrame stackFrame;

    private final AtomicReference<List<Variable>> arguments;

    public JdbMethod(com.sun.jdi.StackFrame stackFrame) {
        this.stackFrame = stackFrame;
        this.method = stackFrame.location().method();
        this.arguments = new AtomicReference<>();
    }

    public JdbMethod(com.sun.jdi.StackFrame stackFrame, List<Variable> arguments) {
        this.stackFrame = stackFrame;
        this.method = stackFrame.location().method();
        this.arguments = new AtomicReference<>(arguments);
    }

    @Override
    public String getName() {
        return method.name();
    }

    @Override
    public List<Variable> getArguments() {
        if (arguments.get() == null) {
            synchronized (arguments) {
                if (arguments.get() == null) {
                    try {
                        arguments.set(method.arguments()
                                            .stream()
                                            .map(v -> new JdbVariable(stackFrame, v))
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
