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

import static org.eclipse.che.jdt.ls.extension.api.Commands.CLIENT_UPDATE_PROJECT;
import static org.eclipse.che.jdt.ls.extension.api.Commands.CLIENT_UPDATE_PROJECTS_CLASSPATH;

import com.google.gwt.json.client.JSONString;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.project.node.ProjectClasspathChangedEvent;
import org.eclipse.lsp4j.ExecuteCommandParams;

/**
 * A processor for incoming <code>workspace/ClasspathChanged</code> notifications sent by a language
 * server.
 *
 * @author V. Rubezhny
 */
@Singleton
public class ExecuteClientCommandProcessor {
  private EventBus eventBus;
  private AppContext appContext;

  @Inject
  public ExecuteClientCommandProcessor(EventBus eventBus, AppContext appContext) {
    this.eventBus = eventBus;
    this.appContext = appContext;
  }

  public void execute(ExecuteCommandParams params) {
    switch (params.getCommand()) {
      case CLIENT_UPDATE_PROJECTS_CLASSPATH:
        for (Object project : params.getArguments()) {
          eventBus.fireEvent(new ProjectClasspathChangedEvent(stringValue(project)));
        }
        break;
      case CLIENT_UPDATE_PROJECT:
        updateProject(stringValue(params.getArguments()));
        break;
      default:
        break;
    }
  }

  private void updateProject(String project) {
    appContext
        .getWorkspaceRoot()
        .getContainer(project)
        .then(
            container -> {
              if (container.isPresent()) {
                container.get().synchronize();
              }
            });
  }

  private String stringValue(Object value) {
    return value instanceof JSONString ? ((JSONString) value).stringValue() : String.valueOf(value);
  }
}
