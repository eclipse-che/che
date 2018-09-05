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
package org.eclipse.che.plugin.languageserver.ide.service;

import static org.eclipse.che.ide.api.jsonrpc.Constants.WS_AGENT_JSON_RPC_ENDPOINT_ID;

import com.google.gwt.json.client.JSONString;
import com.google.web.bindery.event.shared.EventBus;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.project.ProjectServiceClient;
import org.eclipse.che.ide.project.node.ProjectClasspathChangedEvent;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.jdt.ls.extension.api.Notifications;
import org.eclipse.lsp4j.ExecuteCommandParams;

/** Subscribes and receives JSON-RPC messages related custom notifications. */
@Singleton
public class CustomNotificationReceiver {
  public static final String NOTIFY = "workspace/notify";
  public static final String NOTIFY_SUBSCRIBE = "workspace/notify/subscribe";

  private final RequestTransmitter transmitter;
  private final EventBus eventBus;
  private final AppContext appContext;
  private final ProjectServiceClient projectService;
  private final PromiseProvider promises;

  @Inject
  public CustomNotificationReceiver(
      RequestTransmitter transmitter,
      EventBus eventBus,
      AppContext appContext,
      ProjectServiceClient projectService,
      PromiseProvider promises) {
    this.eventBus = eventBus;
    this.appContext = appContext;
    this.projectService = projectService;
    this.promises = promises;
    this.transmitter = transmitter;
  }

  public void subscribe() {
    subscribe(transmitter);
  }

  @Inject
  private void configureReceiver(RequestHandlerConfigurator configurator) {
    configurator
        .newConfiguration()
        .methodName(NOTIFY)
        .paramsAsDto(ExecuteCommandParams.class)
        .noResult()
        .withConsumer(this::handleNotification);
  }

  private void handleNotification(ExecuteCommandParams params) {
    switch (params.getCommand()) {
      case Notifications.UPDATE_PROJECTS_CLASSPATH:
        for (Object project : params.getArguments()) {
          eventBus.fireEvent(new ProjectClasspathChangedEvent(stringValue(project)));
        }
        break;
      case Notifications.UPDATE_PROJECT:
        for (Object project : params.getArguments()) {
          updateProject(stringValue(project));
        }
        break;
      case Notifications.UPDATE_PROJECT_CONFIG:
        updateProjectConfig(stringValue(params.getArguments()));
        break;
      case Notifications.UPDATE_ON_PROJECT_CLASSPATH_CHANGED:
        for (Object project : params.getArguments()) {
          updateProject(stringValue(project))
              .then(
                  container -> {
                    eventBus.fireEvent(
                        new ProjectClasspathChangedEvent(
                            stringValue(container.getLocation().toString())));
                  });
        }
        break;
      default:
        break;
    }
  }

  private void subscribe(RequestTransmitter transmitter) {
    transmitter
        .newRequest()
        .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
        .methodName(NOTIFY_SUBSCRIBE)
        .noParams()
        .sendAndSkipResult();
  }

  private Promise<Container> updateProject(String project) {
    return appContext
        .getWorkspaceRoot()
        .getContainer(project)
        .thenPromise(
            optContainer -> {
              if (optContainer.isPresent()) {
                Container container = optContainer.get();
                container.synchronize();
                return promises.resolve(container);
              }
              return promises.resolve(null);
            });
  }

  private void updateProjectConfig(String project) {
    appContext
        .getWorkspaceRoot()
        .getContainer(project)
        .then(
            container -> {
              projectService
                  .getProject(Path.valueOf(project))
                  .then(
                      projectConfigDto -> {
                        projectService
                            .updateProject(projectConfigDto)
                            .then(
                                arg -> {
                                  if (container.isPresent()) {
                                    container.get().synchronize();
                                  }
                                });
                      });
            });
  }

  private String stringValue(Object value) {
    return value instanceof JSONString
        ? ((JSONString) value).stringValue()
        : (value instanceof List ? stringValue(((List<?>) value).get(0)) : String.valueOf(value));
  }
}
