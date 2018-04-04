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
package org.eclipse.che.api.core.jsonrpc.commons;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Simple promise like binary consumer holder. First consumer's argument always represents endpoint
 * identifier, while the second can be of arbitrary type and depends on business logic.
 *
 * @param <R> type of second argument of binary consumer
 */
public class JsonRpcPromise<R> {
  private BiConsumer<String, R> successConsumer;
  private BiConsumer<String, JsonRpcError> failureConsumer;
  private Runnable timeoutRunnable;

  public Optional<BiConsumer<String, R>> getSuccessConsumer() {
    return Optional.ofNullable(successConsumer);
  }

  public Optional<BiConsumer<String, JsonRpcError>> getFailureConsumer() {
    return Optional.ofNullable(failureConsumer);
  }

  Optional<Runnable> getTimeoutRunnable() {
    return Optional.ofNullable(timeoutRunnable);
  }

  /**
   * Set timeout runnable to be called on this promise timeout.
   *
   * @param runnable timeout runnable
   * @return the instance of this very promise
   */
  public JsonRpcPromise<R> onTimeout(Runnable runnable) {
    checkNotNull(runnable, "JSON RPC timeout runnable argument must not be null");
    checkState(this.timeoutRunnable == null, "JSON RPC timeout runnable field must not be set");
    this.timeoutRunnable = runnable;
    return this;
  }

  /**
   * Set binary consumer to be called on this promise resolution. The first consumer argument is an
   * endpoint that the responses comes from while the second consumer argument is actually the value
   * of response result.
   *
   * @param biConsumer binary consumer
   * @return the instance of this very promise
   */
  public JsonRpcPromise<R> onSuccess(BiConsumer<String, R> biConsumer) {
    checkNotNull(biConsumer, "JSON RPC success consumer argument must not be null");
    checkState(this.successConsumer == null, "JSON RPC success field must not be set");
    this.successConsumer = biConsumer;
    return this;
  }

  /**
   * Set consumer to be called on this promise resolution. Ths variant of promise configuration must
   * be used only when you need not the value of endpoint ID. The only consumer argument is the
   * value of response result.
   *
   * @param consumer consumer
   * @return the instance of this very promise
   */
  public JsonRpcPromise<R> onSuccess(Consumer<R> consumer) {
    checkNotNull(consumer, "JSON RPC success consumer argument must not be null");
    checkState(this.successConsumer == null, "JSON RPC success consumer field must not be set");
    this.successConsumer = (s, r) -> consumer.accept(r);
    return this;
  }

  /**
   * Set runnable to be called on this promise resolution. Ths variant of promise configuration must
   * be used only when you need not both the value of endpoint ID and the value of response result.
   *
   * @param runnable runnable
   * @return the instance of this very promise
   */
  public JsonRpcPromise<R> onSuccess(Runnable runnable) {
    checkNotNull(runnable, "JSON RPC success runnable argument must not be null");
    checkState(this.successConsumer == null, "JSON RPC success field must not be set");
    this.successConsumer = (s, r) -> runnable.run();
    return this;
  }

  /**
   * Set binary consumer to be called on promise rejection. The first consumer argument is an
   * endpoint that the responses comes from while the second consumer argument is actually the value
   * of response error.
   *
   * @param biConsumer binary consumer
   * @return the instance of this very promise
   */
  public JsonRpcPromise<R> onFailure(BiConsumer<String, JsonRpcError> biConsumer) {
    checkNotNull(biConsumer, "JSON RPC failure consumer argument must not be null");
    checkState(this.failureConsumer == null, "JSON RPC failure consumer field must not be set");
    this.failureConsumer = biConsumer;
    return this;
  }

  /**
   * Set consumer to be called on this promise rejection. Ths variant of promise configuration must
   * be used only when you need not the value of endpoint ID. The only consumer argument is the
   * value of response result.
   *
   * @param consumer consumer
   * @return the instance of this very promise
   */
  public JsonRpcPromise<R> onFailure(Consumer<JsonRpcError> consumer) {
    checkNotNull(consumer, "JSON RPC failure consumer argument must not be null");
    checkState(this.failureConsumer == null, "JSON RPC failure consumer field must not be set");
    this.failureConsumer = (s, e) -> consumer.accept(e);
    return this;
  }

  /**
   * Set runnable to be called on this promise rejection. Ths variant of promise configuration must
   * be used only when you need not both the value of endpoint ID and the value of response error.
   *
   * @param runnable runnable
   * @return the instance of this very promise
   */
  public JsonRpcPromise<R> onFailure(Runnable runnable) {
    checkNotNull(runnable, "JSON RPC success runnable argument must not be null");
    checkState(this.successConsumer == null, "JSON RPC success field must not be set");
    this.successConsumer = (s, e) -> runnable.run();
    return this;
  }
}
