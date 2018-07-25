/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.core.jsonrpc.commons;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;

/** Dispatches JSON RPC responses */
@Singleton
public class ResponseDispatcher {
  private static final Logger LOGGER = getLogger(ResponseDispatcher.class);

  private final JsonRpcComposer composer;
  private final TimeoutActionRunner timeoutActionRunner;

  private final Map<String, SingleTypedPromise<?>> singleTypedPromises = new ConcurrentHashMap<>();
  private final Map<String, ListTypedPromise<?>> listTypedPromises = new ConcurrentHashMap<>();

  @Inject
  public ResponseDispatcher(JsonRpcComposer composer, TimeoutActionRunner timeoutActionRunner) {
    this.composer = composer;
    this.timeoutActionRunner = timeoutActionRunner;
  }

  private static void checkArguments(String endpointId, String requestId, Class<?> rClass) {
    checkNotNull(endpointId, "Endpoint ID must not be null");
    checkArgument(!endpointId.isEmpty(), "Endpoint ID must not be empty");

    checkNotNull(requestId, "Request ID must not be null");
    checkArgument(!requestId.isEmpty(), "Request ID must not be empty");

    checkNotNull(rClass, "Result class must not be null");
  }

  private static String generateKey(String endpointId, String requestId) {
    return endpointId + '@' + requestId;
  }

  public void dispatch(String endpointId, JsonRpcResponse response) {
    checkNotNull(endpointId, "Endpoint ID name must not be null");
    checkArgument(!endpointId.isEmpty(), "Endpoint ID name must not be empty");
    checkNotNull(response, "Response name must not be null");

    String responseId = response.getId();
    if (responseId == null) {
      return;
    }

    String key = generateKey(endpointId, responseId);

    if (response.hasResult()) {
      dispatchResult(endpointId, response, key);
    } else if (response.hasError()) {
      dispatchError(endpointId, response, key);
    } else {
      LOGGER.error("Received incorrect response: no error, no result");
    }
  }

  public synchronized <R> JsonRpcPromise<R> registerPromiseForSingleObject(
      String endpointId, String requestId, Class<R> rClass, int timeoutInMillis) {
    checkArguments(endpointId, requestId, rClass);

    SingleTypedPromise<R> promise = new SingleTypedPromise<>(rClass);
    String key = generateKey(endpointId, requestId);
    singleTypedPromises.put(key, promise);
    if (timeoutInMillis > 0) {
      timeoutActionRunner.schedule(
          timeoutInMillis, () -> runTimeoutConsumer(singleTypedPromises.remove(key)));
    }
    return promise;
  }

  public synchronized <R> JsonRpcPromise<List<R>> registerPromiseForListOfObjects(
      String endpointId, String requestId, Class<R> rClass, int timeoutInMillis) {
    checkArguments(endpointId, requestId, rClass);

    ListTypedPromise<R> promise = new ListTypedPromise<>(rClass);
    String key = generateKey(endpointId, requestId);
    listTypedPromises.put(key, promise);
    if (timeoutInMillis > 0) {
      timeoutActionRunner.schedule(
          timeoutInMillis, () -> runTimeoutConsumer(listTypedPromises.remove(key)));
    }
    return promise;
  }

  private void runTimeoutConsumer(JsonRpcPromise<?> promise) {
    Optional.ofNullable(promise)
        .flatMap(JsonRpcPromise::getTimeoutRunnable)
        .ifPresent(Runnable::run);
  }

  private void dispatchResult(String endpointId, JsonRpcResponse response, String key) {
    Optional.ofNullable(listTypedPromises.remove(key))
        .ifPresent(
            promise ->
                promise
                    .getSuccessConsumer()
                    .ifPresent(
                        consumer ->
                            promise
                                .getType()
                                .ifPresent(
                                    type ->
                                        consumer.accept(
                                            endpointId,
                                            composer.composeMany(response.getResult(), type)))));

    Optional.ofNullable(singleTypedPromises.remove(key))
        .ifPresent(
            promise ->
                promise
                    .getSuccessConsumer()
                    .ifPresent(
                        consumer ->
                            promise
                                .getType()
                                .ifPresent(
                                    type ->
                                        consumer.accept(
                                            endpointId,
                                            composer.composeOne(response.getResult(), type)))));
  }

  private void dispatchError(String endpointId, JsonRpcResponse response, String key) {
    SingleTypedPromise<?> singlePromise = singleTypedPromises.remove(key);
    ListTypedPromise<?> listPromise = listTypedPromises.remove(key);
    JsonRpcPromise<?> promise = singlePromise != null ? singlePromise : listPromise;
    promise.getFailureConsumer().ifPresent(it -> it.accept(endpointId, response.getError()));
  }

  private class ListTypedPromise<R> extends JsonRpcPromise<List<R>> {
    private final Class<R> type;

    private ListTypedPromise(Class<R> type) {
      this.type = type;
    }

    private Optional<Class<R>> getType() {
      return Optional.ofNullable(type);
    }
  }

  private class SingleTypedPromise<R> extends JsonRpcPromise<R> {
    private final Class<R> type;

    private SingleTypedPromise(Class<R> type) {
      this.type = type;
    }

    public Optional<Class<R>> getType() {
      return Optional.ofNullable(type);
    }
  }
}
