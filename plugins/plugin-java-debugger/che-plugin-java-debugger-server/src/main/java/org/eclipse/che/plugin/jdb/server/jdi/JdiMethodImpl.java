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
package org.eclipse.che.plugin.jdb.server.jdi;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Method;
import com.sun.jdi.StackFrame;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Anatolii Bazko
 */
public class JdiMethodImpl implements JdiMethod {
    private final String            name;
    private final List<JdiVariable> arguments;

    public JdiMethodImpl(StackFrame stackFrame, Method method) {
        this.name = method.name();

        List<JdiVariable> args;
        try {
            args = method.arguments()
                         .stream()
                         .map(v -> new JdiVariableImpl(stackFrame, v))
                         .collect(Collectors.toList());
        } catch (AbsentInformationException e) {
            args = Collections.emptyList();
        }
        this.arguments = args;
    }

    public JdiMethodImpl(Method method) {
        this.name = method.name();

        List<JdiVariable> args;
        try {
            args = method.arguments()
                         .stream()
                         .map(JdiVariableImpl::new)
                         .collect(Collectors.toList());
        } catch (AbsentInformationException e) {
            args = Collections.emptyList();
        }
        this.arguments = args;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<JdiVariable> getArguments() {
        return arguments;
    }
}
