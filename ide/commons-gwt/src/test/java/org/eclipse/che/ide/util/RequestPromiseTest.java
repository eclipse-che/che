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
package org.eclipse.che.ide.util;

import static org.junit.Assert.*;

import java.util.function.Consumer;
import org.junit.Test;

/**
 * Unit tests for the {@link RequestPromise}.
 *
 * @author Vlad Zhukovskyi
 */
public class RequestPromiseTest {

  @Test
  public void testShouldReturnNonEmptyConsumers() throws Exception {
    RequestPromise<Object> promise = new RequestPromise<>();

    Consumer<Object> successConsumer = __ -> {};
    Consumer<Throwable> failureConsumer = __ -> {};

    promise.onSuccess(successConsumer).onFailure(failureConsumer);

    assertTrue(promise.getSuccessConsumer().isPresent());
    assertTrue(promise.getFailureConsumer().isPresent());
    assertSame(promise.getSuccessConsumer().get(), successConsumer);
    assertSame(promise.getFailureConsumer().get(), failureConsumer);
  }

  @Test
  public void testShouldReturnEmptyConsumers() throws Exception {
    RequestPromise<Object> promise = new RequestPromise<>();

    assertFalse(promise.getSuccessConsumer().isPresent());
    assertFalse(promise.getFailureConsumer().isPresent());
  }
}
