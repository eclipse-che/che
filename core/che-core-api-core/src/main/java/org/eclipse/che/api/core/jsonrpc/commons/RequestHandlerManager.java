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

import static org.slf4j.LoggerFactory.getLogger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import org.eclipse.che.api.core.websocket.commons.WebSocketMessageTransmitter;
import org.slf4j.Logger;

/**
 * Manages request handlers. There are nine types of such handlers that differs by the type and
 * number of incoming parameters and outgoing results:
 *
 * <ul>
 *   <li>{@link NoneToNoneHandler} - to receive a notification w/o parameters
 *   <li>{@link NoneToOneHandler} - to receive a request w/o parameters and a single result
 *   <li>{@link NoneToManyHandler} - to receive a request w/o parameters and multiple results
 *   <li>{@link OneToNoneHandler} - to receive a notification with a single parameter
 *   <li>{@link OneToOneHandler} - to receive a request with a single parameter and a single result
 *   <li>{@link OneToManyHandler}- to receive a request with a single parameter and multiple results
 *   <li>{@link ManyToNoneHandler} - to receive a notification with multiple parameters
 *   <li>{@link ManyToOneHandler} - to receive request with multiple parameters and a single result
 *   <li>{@link ManyToManyHandler} - to receive request with multiple parameters and multiple
 *       results
 * </ul>
 */
@Singleton
public class RequestHandlerManager {
  private static final Logger LOGGER = getLogger(RequestHandlerManager.class);

  private final Map<String, Category> methodToCategory = new ConcurrentHashMap<>();
  private final Map<String, OneToOneHandler> oneToOneHandlers = new ConcurrentHashMap<>();
  private final Map<String, OneToPromiseOneHandler> oneToPromiseOneHandlers =
      new ConcurrentHashMap<>();
  private final Map<String, OneToManyHandler> oneToManyHandlers = new ConcurrentHashMap<>();
  private final Map<String, OneToNoneHandler> oneToNoneHandlers = new ConcurrentHashMap<>();
  private final Map<String, ManyToOneHandler> manyToOneHandlers = new ConcurrentHashMap<>();
  private final Map<String, ManyToManyHandler> manyToManyHandlers = new ConcurrentHashMap<>();
  private final Map<String, ManyToNoneHandler> manyToNoneHandlers = new ConcurrentHashMap<>();
  private final Map<String, NoneToOneHandler> noneToOneHandlers = new ConcurrentHashMap<>();
  private final Map<String, NoneToManyHandler> noneToManyHandlers = new ConcurrentHashMap<>();
  private final Map<String, NoneToNoneHandler> noneToNoneHandlers = new ConcurrentHashMap<>();

  private final WebSocketMessageTransmitter transmitter;
  private final JsonRpcComposer dtoComposer;
  private final JsonRpcMarshaller marshaller;

  @Inject
  public RequestHandlerManager(
      WebSocketMessageTransmitter transmitter,
      JsonRpcComposer dtoComposer,
      JsonRpcMarshaller marshaller) {
    this.transmitter = transmitter;
    this.dtoComposer = dtoComposer;
    this.marshaller = marshaller;
  }

  public synchronized <P, R> void registerOneToOne(
      String method, Class<P> pClass, Class<R> rClass, BiFunction<String, P, R> biFunction) {
    mustNotBeRegistered(method);

    methodToCategory.put(method, Category.ONE_TO_ONE);
    oneToOneHandlers.put(method, new OneToOneHandler<>(pClass, rClass, biFunction));
  }

  public synchronized <P, R> void registerOneToPromiseOne(
      String method,
      Class<P> pClass,
      Class<R> rClass,
      BiFunction<String, P, JsonRpcPromise<R>> function) {
    mustNotBeRegistered(method);
    methodToCategory.put(method, Category.ONE_TO_PROMISE_ONE);
    oneToPromiseOneHandlers.put(method, new OneToPromiseOneHandler<>(pClass, rClass, function));
  }

  public synchronized <P, R> void registerOneToMany(
      String method, Class<P> pClass, Class<R> rClass, BiFunction<String, P, List<R>> biFunction) {
    mustNotBeRegistered(method);

    methodToCategory.put(method, Category.ONE_TO_MANY);
    oneToManyHandlers.put(method, new OneToManyHandler<>(pClass, rClass, biFunction));
  }

  public synchronized <P> void registerOneToNone(
      String method, Class<P> pClass, BiConsumer<String, P> biConsumer) {
    mustNotBeRegistered(method);

    methodToCategory.put(method, Category.ONE_TO_NONE);
    oneToNoneHandlers.put(method, new OneToNoneHandler<>(pClass, biConsumer));
  }

  public synchronized <P, R> void registerManyToOne(
      String method, Class<P> pClass, Class<R> rClass, BiFunction<String, List<P>, R> biFunction) {
    mustNotBeRegistered(method);

    methodToCategory.put(method, Category.MANY_TO_ONE);
    manyToOneHandlers.put(method, new ManyToOneHandler<>(pClass, rClass, biFunction));
  }

  public synchronized <P, R> void registerManyToMany(
      String method,
      Class<P> pClass,
      Class<R> rClass,
      BiFunction<String, List<P>, List<R>> function) {
    mustNotBeRegistered(method);

    methodToCategory.put(method, Category.MANY_TO_MANY);
    manyToManyHandlers.put(method, new ManyToManyHandler<>(pClass, rClass, function));
  }

  public synchronized <P> void registerManyToNone(
      String method, Class<P> pClass, BiConsumer<String, List<P>> biConsumer) {
    mustNotBeRegistered(method);

    methodToCategory.put(method, Category.MANY_TO_NONE);
    manyToNoneHandlers.put(method, new ManyToNoneHandler<>(pClass, biConsumer));
  }

  public synchronized <R> void registerNoneToOne(
      String method, Class<R> rClass, Function<String, R> function) {
    mustNotBeRegistered(method);

    methodToCategory.put(method, Category.NONE_TO_ONE);
    noneToOneHandlers.put(method, new NoneToOneHandler<>(rClass, function));
  }

  public synchronized <R> void registerNoneToMany(
      String method, Class<R> rClass, Function<String, List<R>> function) {
    mustNotBeRegistered(method);

    methodToCategory.put(method, Category.NONE_TO_MANY);
    noneToManyHandlers.put(method, new NoneToManyHandler<>(rClass, function));
  }

  public synchronized void registerNoneToNone(String method, Consumer<String> consumer) {
    mustNotBeRegistered(method);

    methodToCategory.put(method, Category.NONE_TO_NONE);
    noneToNoneHandlers.put(method, new NoneToNoneHandler(consumer));
  }

  public boolean isRegistered(String method) {
    return methodToCategory.containsKey(method);
  }

  public synchronized boolean deregister(String method) {
    Category category = methodToCategory.remove(method);

    if (category == null) {
      return false;
    }

    switch (category) {
      case ONE_TO_ONE:
        oneToOneHandlers.remove(method);
        break;
      case ONE_TO_MANY:
        oneToManyHandlers.remove(method);
        break;
      case ONE_TO_NONE:
        oneToNoneHandlers.remove(method);
        break;
      case MANY_TO_ONE:
        manyToOneHandlers.remove(method);
        break;
      case MANY_TO_MANY:
        manyToManyHandlers.remove(method);
        break;
      case MANY_TO_NONE:
        manyToNoneHandlers.remove(method);
        break;
      case NONE_TO_ONE:
        noneToOneHandlers.remove(method);
        break;
      case NONE_TO_MANY:
        noneToManyHandlers.remove(method);
        break;
      case NONE_TO_NONE:
        noneToNoneHandlers.remove(method);
        break;
      case ONE_TO_PROMISE_ONE:
        oneToPromiseOneHandlers.remove(method);
        break;
    }

    return true;
  }

  public void handle(String endpointId, String requestId, String method, JsonRpcParams params) {
    mustBeRegistered(method);

    switch (methodToCategory.get(method)) {
      case ONE_TO_ONE:
        OneToOneHandler oneToOneHandler = oneToOneHandlers.get(method);
        transmitOne(endpointId, requestId, oneToOneHandler.handle(endpointId, params));
        break;
      case ONE_TO_MANY:
        OneToManyHandler oneToManyHandler = oneToManyHandlers.get(method);
        transmitMany(endpointId, requestId, oneToManyHandler.handle(endpointId, params));
        break;
      case MANY_TO_ONE:
        ManyToOneHandler manyToOneHandler = manyToOneHandlers.get(method);
        transmitOne(endpointId, requestId, manyToOneHandler.handle(endpointId, params));
        break;
      case MANY_TO_MANY:
        ManyToManyHandler manyToManyHandler = manyToManyHandlers.get(method);
        transmitMany(endpointId, requestId, manyToManyHandler.handle(endpointId, params));
        break;
      case NONE_TO_ONE:
        NoneToOneHandler noneToOneHandler = noneToOneHandlers.get(method);
        transmitOne(endpointId, requestId, noneToOneHandler.handle(endpointId));
        break;
      case NONE_TO_MANY:
        NoneToManyHandler noneToManyHandler = noneToManyHandlers.get(method);
        transmitMany(endpointId, requestId, noneToManyHandler.handle(endpointId));
        break;
      case ONE_TO_PROMISE_ONE:
        OneToPromiseOneHandler promiseOneHandler = oneToPromiseOneHandlers.get(method);
        transmitPromiseOne(endpointId, requestId, promiseOneHandler.handle(endpointId, params));
        break;
      default:
        LOGGER.error("Something went wrong trying to find out handler category");
    }
  }

  public void handle(String endpointId, String method, JsonRpcParams params) {
    mustBeRegistered(method);

    switch (methodToCategory.get(method)) {
      case ONE_TO_NONE:
        oneToNoneHandlers.get(method).handle(endpointId, params);
        break;
      case MANY_TO_NONE:
        manyToNoneHandlers.get(method).handle(endpointId, params);
        break;
      case NONE_TO_NONE:
        noneToNoneHandlers.get(method).handle(endpointId);
        break;
      default:
        LOGGER.error("Something went wrong trying to find out handler category");
    }
  }

  private void mustBeRegistered(String method) {
    if (!isRegistered(method)) {
      String message = "Method '" + method + "' is not registered";
      LOGGER.error(message);
      throw new IllegalStateException(message);
    }
  }

  private void mustNotBeRegistered(String method) {
    if (isRegistered(method)) {
      String message = "Method '" + method + "' is already registered";
      LOGGER.error(message);
      throw new IllegalStateException(message);
    }
  }

  private void transmitOne(String endpointId, String id, Object result) {
    JsonRpcResult jsonRpcResult = new JsonRpcResult(result);
    JsonRpcResponse jsonRpcResponse = new JsonRpcResponse(id, jsonRpcResult, null);
    String message = marshaller.marshall(jsonRpcResponse);
    transmitter.transmit(endpointId, message);
  }

  private void transmitMany(String endpointId, String id, List<?> result) {
    JsonRpcResult jsonRpcResult = new JsonRpcResult(result);
    JsonRpcResponse jsonRpcResponse = new JsonRpcResponse(id, jsonRpcResult, null);
    String message = marshaller.marshall(jsonRpcResponse);
    transmitter.transmit(endpointId, message);
  }

  private void transmitPromiseOne(
      String endpointId, String requestId, JsonRpcPromise<Object> promise) {
    promise.onSuccess(result -> transmitOne(endpointId, requestId, result));
    promise.onFailure(
        jsonRpcError -> {
          JsonRpcResponse jsonRpcResponse = new JsonRpcResponse(requestId, null, jsonRpcError);
          String message = marshaller.marshall(jsonRpcResponse);
          transmitter.transmit(endpointId, message);
        });
  }

  public enum Category {
    ONE_TO_ONE,
    ONE_TO_MANY,
    ONE_TO_NONE,
    MANY_TO_ONE,
    MANY_TO_MANY,
    MANY_TO_NONE,
    NONE_TO_ONE,
    NONE_TO_MANY,
    NONE_TO_NONE,
    ONE_TO_PROMISE_ONE
  }

  private class OneToOneHandler<P, R> {
    private final Class<P> pClass;
    private final Class<R> rClass;
    private final BiFunction<String, P, R> biFunction;

    private OneToOneHandler(Class<P> pClass, Class<R> rClass, BiFunction<String, P, R> biFunction) {
      this.pClass = pClass;
      this.rClass = rClass;
      this.biFunction = biFunction;
    }

    private R handle(String endpointId, JsonRpcParams params) {
      P dto = dtoComposer.composeOne(params, pClass);
      return biFunction.apply(endpointId, dto);
    }
  }

  private class OneToPromiseOneHandler<P, R> {
    private final Class<P> pClass;
    private final Class<R> rClass;
    private BiFunction<String, P, JsonRpcPromise<R>> function;

    private OneToPromiseOneHandler(
        Class<P> pClass, Class<R> rClass, BiFunction<String, P, JsonRpcPromise<R>> function) {
      this.pClass = pClass;
      this.rClass = rClass;
      this.function = function;
    }

    private JsonRpcPromise<R> handle(String endpointId, JsonRpcParams params) {
      P dto = dtoComposer.composeOne(params, pClass);
      return function.apply(endpointId, dto);
    }
  }

  private class OneToManyHandler<P, R> {
    private final Class<P> pClass;
    private final Class<R> rClass;
    private final BiFunction<String, P, List<R>> biFunction;

    private OneToManyHandler(
        Class<P> pClass, Class<R> rClass, BiFunction<String, P, List<R>> biFunction) {
      this.pClass = pClass;
      this.rClass = rClass;
      this.biFunction = biFunction;
    }

    private List<R> handle(String endpointId, JsonRpcParams params) {
      P dto = dtoComposer.composeOne(params, pClass);
      return biFunction.apply(endpointId, dto);
    }
  }

  private class OneToNoneHandler<P> {
    private final Class<P> pClass;
    private final BiConsumer<String, P> biConsumer;

    private OneToNoneHandler(Class<P> pClass, BiConsumer<String, P> biConsumer) {
      this.pClass = pClass;
      this.biConsumer = biConsumer;
    }

    private void handle(String endpointId, JsonRpcParams params) {
      P dto = dtoComposer.composeOne(params, pClass);
      biConsumer.accept(endpointId, dto);
    }
  }

  private class ManyToOneHandler<P, R> {
    private final Class<P> pClass;
    private final Class<R> rClass;
    private final BiFunction<String, List<P>, R> biFunction;

    private ManyToOneHandler(
        Class<P> pClass, Class<R> rClass, BiFunction<String, List<P>, R> biFunction) {
      this.pClass = pClass;
      this.rClass = rClass;
      this.biFunction = biFunction;
    }

    private R handle(String endpointId, JsonRpcParams params) {
      List<P> dto = dtoComposer.composeMany(params, pClass);
      return biFunction.apply(endpointId, dto);
    }
  }

  private class ManyToManyHandler<P, R> {
    private final Class<P> pClass;
    private final Class<R> rClass;
    private final BiFunction<String, List<P>, List<R>> biFunction;

    private ManyToManyHandler(
        Class<P> pClass, Class<R> rClass, BiFunction<String, List<P>, List<R>> biFunction) {
      this.pClass = pClass;
      this.rClass = rClass;
      this.biFunction = biFunction;
    }

    private List<R> handle(String endpointId, JsonRpcParams params) {
      List<P> dto = dtoComposer.composeMany(params, pClass);
      return biFunction.apply(endpointId, dto);
    }
  }

  private class ManyToNoneHandler<P> {
    private final Class<P> pClass;
    private final BiConsumer<String, List<P>> biConsumer;

    private ManyToNoneHandler(Class<P> pClass, BiConsumer<String, List<P>> biConsumer) {
      this.pClass = pClass;
      this.biConsumer = biConsumer;
    }

    private void handle(String endpointId, JsonRpcParams params) {
      List<P> dto = dtoComposer.composeMany(params, pClass);
      biConsumer.accept(endpointId, dto);
    }
  }

  private class NoneToOneHandler<R> {
    private final Class<R> rClass;
    private final Function<String, R> function;

    private NoneToOneHandler(Class<R> rClass, Function<String, R> function) {
      this.rClass = rClass;
      this.function = function;
    }

    private R handle(String endpointId) {
      return function.apply(endpointId);
    }
  }

  private class NoneToManyHandler<R> {
    private final Class<R> rClass;
    private final Function<String, List<R>> function;

    private NoneToManyHandler(Class<R> rClass, Function<String, List<R>> function) {
      this.rClass = rClass;
      this.function = function;
    }

    private List<R> handle(String endpointId) {
      return function.apply(endpointId);
    }
  }

  private class NoneToNoneHandler {
    private final Consumer<String> consumer;

    private NoneToNoneHandler(Consumer<String> consumer) {
      this.consumer = consumer;
    }

    private void handle(String endpointId) {
      consumer.accept(endpointId);
    }
  }
}
