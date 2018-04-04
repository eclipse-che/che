/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.testing.ide;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Mockito Answer that applies a given function to the first argument of the answer InvocationOnMock
 * argument.
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
    ArgumentType arg = (ArgumentType) invocation.getArguments()[0];
    return apply.apply(arg);
  }
}
