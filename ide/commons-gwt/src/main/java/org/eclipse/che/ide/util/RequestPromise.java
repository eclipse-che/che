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

import com.google.common.annotations.Beta;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Provider of the promise for the abstract operation. May hold success and failure consumer.
 *
 * @author Vlad Zhukovskyi
 * @since 5.19.0
 */
@Beta
public class RequestPromise<R> {

  private Consumer<R> successConsumer;
  private Consumer<Throwable> failureConsumer;

  public Optional<Consumer<R>> getSuccessConsumer() {
    return Optional.ofNullable(successConsumer);
  }

  public Optional<Consumer<Throwable>> getFailureConsumer() {
    return Optional.ofNullable(failureConsumer);
  }

  public RequestPromise<R> onSuccess(Consumer<R> consumer) {
    successConsumer = consumer;
    return this;
  }

  public RequestPromise<R> onFailure(Consumer<Throwable> consumer) {
    failureConsumer = consumer;
    return this;
  }
}
