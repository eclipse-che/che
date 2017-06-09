/*******************************************************************************
 * Copyright (c) 2017 RedHat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   RedHat, Inc. - initial commit
 *******************************************************************************/package org.eclipse.che.plugin.testing.ide;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Mockito Answer that applies a given function to the first argument of 
 * the answer InvocationOnMock argument.
 *
 * @author David Festal
 */
public class FunctionAnswer<ArgumentType, Return> implements Answer<Return> {
    private java.util.function.Function<ArgumentType, Return> apply;
    public FunctionAnswer(java.util.function.Function<ArgumentType, Return> apply) {
        this.apply = apply;
    }
    
    @Override
    public Return answer(InvocationOnMock invocation) throws Throwable {
        @SuppressWarnings("unchecked")
        ArgumentType arg = (ArgumentType)invocation.getArguments()[0];
        return apply.apply(arg);
    }
}
