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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.util.function.BiFunction;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.js.Executor;
import org.eclipse.che.api.promises.client.js.RejectFunction;
import org.eclipse.che.api.promises.client.js.ResolveFunction;

/**
 * Utility class that allows mocking a Che Promise from an Executor.ExecutorBody.
 *
 * @author David Festal
 */
public class ExecutorPromiseMocker<T> extends PromiseMocker<T> {
  private final ResolveFunction<T> resolveFunction;
  private final RejectFunction rejectFunction;

  @SuppressWarnings("unchecked")
  public ExecutorPromiseMocker(
      final Executor.ExecutorBody<T> executorBody,
      final BiFunction<T, PromiseMocker<T>, Void> onResolved,
      final BiFunction<PromiseError, PromiseMocker<T>, Void> onRejected) {
    super();
    resolveFunction = (ResolveFunction<T>) mock(ResolveFunction.class);
    rejectFunction = mock(RejectFunction.class);

    doAnswer(
            new FunctionAnswer<T, Void>(
                resolvedValue -> {
                  onResolved.apply(resolvedValue, this);
                  return null;
                }))
        .when(resolveFunction)
        .apply(org.mockito.ArgumentMatchers.<T>any());

    doAnswer(
            new FunctionAnswer<PromiseError, Void>(
                promiseError -> {
                  onRejected.apply(promiseError, this);
                  return null;
                }))
        .when(rejectFunction)
        .apply(any(PromiseError.class));
  }

  public ResolveFunction<T> getResolveFunction() {
    return resolveFunction;
  }

  public RejectFunction getRejectFunction() {
    return rejectFunction;
  }
}
