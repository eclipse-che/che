/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.filewatcher;

import static org.eclipse.che.ide.api.jsonrpc.Constants.WS_AGENT_JSON_RPC_ENDPOINT_ID;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcPromise;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.WindowActionEvent;
import org.eclipse.che.ide.api.WindowActionHandler;

/**
 * Tracks and allows to manage the file watcher exclude patterns for tracking creation, modification
 * and deletion events for corresponding entries.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class FileWatcherExcludesOperation implements WindowActionHandler {
  private static final String EXCLUDES_SUBSCRIBE = "fileWatcher/excludes/subscribe";
  private static final String EXCLUDES_UNSUBSCRIBE = "fileWatcher/excludes/unsubscribe";
  private static final String EXCLUDES_CHANGED = "fileWatcher/excludes/changed";
  private static final String ADD_TO_EXCLUDES = "fileWatcher/excludes/addToExcludes";
  private static final String REMOVE_FROM_EXCLUDES = "fileWatcher/excludes/removeFromExcludes";
  private static final String EXCLUDES_CLEAN_UP = "fileWatcher/excludes/cleanup";

  private PromiseProvider promises;
  private RequestTransmitter requestTransmitter;
  private Set<String> excludes = new HashSet<>();

  @Inject
  public FileWatcherExcludesOperation(
      EventBus eventBus, PromiseProvider promises, RequestTransmitter requestTransmitter) {
    this.promises = promises;
    this.requestTransmitter = requestTransmitter;
    eventBus.addHandler(WindowActionEvent.TYPE, this);
    subscribe();
  }

  @Inject
  private void configureHandlers(RequestHandlerConfigurator configurator) {
    configurator
        .newConfiguration()
        .methodName(EXCLUDES_CHANGED)
        .paramsAsListOfString()
        .noResult()
        .withConsumer(
            newExcludes -> {
              excludes.clear();
              excludes.addAll(newExcludes);
            });

    configurator
        .newConfiguration()
        .methodName(EXCLUDES_CLEAN_UP)
        .noParams()
        .noResult()
        .withConsumer(s -> excludes.clear());
  }

  /**
   * Checks if specified path is within excludes
   *
   * @param pathToTest path being examined
   * @return true if path is within excludes, false otherwise
   */
  public boolean isExcluded(String pathToTest) {
    return excludes.contains(pathToTest);
  }

  /**
   * Registers a set of paths to skip tracking of creation, modification and deletion events for
   * corresponding entries.
   *
   * @param pathsToExclude entries' paths to adding to excludes
   */
  public Promise<Boolean> addToFileWatcherExcludes(Set<String> pathsToExclude) {
    JsonRpcPromise<Boolean> jsonRpcPromise =
        requestTransmitter
            .newRequest()
            .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
            .methodName(ADD_TO_EXCLUDES)
            .paramsAsListOfString(new ArrayList<>(pathsToExclude))
            .sendAndReceiveResultAsBoolean();
    return toPromise(jsonRpcPromise);
  }

  /**
   * Removes a set of paths from excludes to resume tracking of corresponding entries creation,
   * modification and deletion events.
   *
   * @param paths entries' paths to remove from excludes
   */
  public Promise<Boolean> removeFromFileWatcherExcludes(Set<String> paths) {
    JsonRpcPromise<Boolean> jsonRpcPromise =
        requestTransmitter
            .newRequest()
            .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
            .methodName(REMOVE_FROM_EXCLUDES)
            .paramsAsListOfString(new ArrayList<>(paths))
            .sendAndReceiveResultAsBoolean();
    return toPromise(jsonRpcPromise);
  }

  private Promise<Boolean> toPromise(JsonRpcPromise<Boolean> jsonRpcPromise) {
    return promises.create(
        (AsyncCallback<Boolean> callback) -> {
          jsonRpcPromise.onSuccess(callback::onSuccess);

          jsonRpcPromise.onFailure(
              jsonRpcError -> callback.onFailure(new Throwable(jsonRpcError.getMessage())));
        });
  }

  @Override
  public void onWindowClosing(WindowActionEvent event) {}

  @Override
  public void onWindowClosed(WindowActionEvent event) {
    unSubscribe();
  }

  private void subscribe() {
    requestTransmitter
        .newRequest()
        .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
        .methodName(EXCLUDES_SUBSCRIBE)
        .noParams()
        .sendAndSkipResult();
  }

  private void unSubscribe() {
    requestTransmitter
        .newRequest()
        .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
        .methodName(EXCLUDES_UNSUBSCRIBE)
        .noParams()
        .sendAndSkipResult();
  }
}
