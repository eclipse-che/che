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
package org.eclipse.che.ide.resources;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.project.shared.dto.event.ProjectTreeStateUpdateDto;
import org.eclipse.che.ide.api.resources.Container;

/**
 * Receives project tree status notifications from server side. There are three type of
 * notifications for files and directories in a project tree: creation, removal, modification. Each
 * notification is processed and passed further to an instance of workspace {@link Container}.
 *
 * @author Dmitry Kuleshov
 */
@Singleton
public class ProjectTreeStateNotificationOperation {
  private final ProjectTreeChangeHandler projectTreeChangeHandler;

  @Inject
  public ProjectTreeStateNotificationOperation(ProjectTreeChangeHandler projectTreeChangeHandler) {
    this.projectTreeChangeHandler = projectTreeChangeHandler;
  }

  @Inject
  public void configureHandler(RequestHandlerConfigurator configurator) {
    configurator
        .newConfiguration()
        .methodName("event/project-tree-state-changed")
        .paramsAsDto(ProjectTreeStateUpdateDto.class)
        .noResult()
        .withConsumer(projectTreeChangeHandler::handleFileChange);
  }
}
