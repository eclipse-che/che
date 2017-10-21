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
package org.eclipse.che.ide.resources;

import static org.eclipse.che.ide.api.resources.ResourceDelta.ADDED;
import static org.eclipse.che.ide.api.resources.ResourceDelta.REMOVED;
import static org.eclipse.che.ide.api.resources.ResourceDelta.UPDATED;

import java.util.function.BiConsumer;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcException;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType;
import org.eclipse.che.api.project.shared.dto.event.ProjectTreeStateUpdateDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.ExternalResourceDelta;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.util.loging.Log;

/**
 * Receives project tree status notifications from server side. There are three type of
 * notifications for files and directories in a project tree: creation, removal, modification. Each
 * notification is processed and passed further to an instance of workspace {@link Container}.
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class ProjectTreeStateNotificationOperation
    implements BiConsumer<String, ProjectTreeStateUpdateDto> {
  private final AppContext appContext;

  @Inject
  public ProjectTreeStateNotificationOperation(AppContext appContext) {
    this.appContext = appContext;
  }

  @Inject
  public void configureHandler(RequestHandlerConfigurator configurator) {
    configurator
        .newConfiguration()
        .methodName("event/project-tree-state-changed")
        .paramsAsDto(ProjectTreeStateUpdateDto.class)
        .noResult()
        .withBiConsumer(this);
  }

  @Override
  public void accept(String endpointId, ProjectTreeStateUpdateDto params) throws JsonRpcException {
    final String path = params.getPath();
    final FileWatcherEventType type = params.getType();

    final int status;

    switch (type) {
      case CREATED:
        {
          status = ADDED;

          break;
        }
      case DELETED:
        {
          status = REMOVED;

          break;
        }
      case MODIFIED:
        {
          status = UPDATED;

          break;
        }
      default:
        {
          status = UPDATED;

          break;
        }
    }

    Log.debug(
        getClass(), "Received request\npath: " + path + "\ntype:" + type + "\nstatus:" + status);

    if (path == null || path.isEmpty()) {
      appContext.getWorkspaceRoot().synchronize();
    } else {
      appContext
          .getWorkspaceRoot()
          .synchronize(new ExternalResourceDelta(Path.valueOf(path), Path.valueOf(path), status));
    }
  }
}
