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
package org.eclipse.che.ide.machine;

import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.ide.api.WsAgentURLModifier;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.workspace.event.WorkspaceRunningEvent;
import org.eclipse.che.ide.api.workspace.model.WorkspaceImpl;
import org.eclipse.che.ide.bootstrap.BasicIDEInitializedEvent;

/** Initializer for {@link WsAgentURLModifier}. */
@Singleton
public class WsAgentURLModifierInitializer {

  @Inject
  public WsAgentURLModifierInitializer(
      WsAgentURLModifier wsAgentURLModifier, AppContext appContext, EventBus eventBus) {

    eventBus.addHandler(
        BasicIDEInitializedEvent.TYPE,
        e -> {
          final WorkspaceImpl workspace = appContext.getWorkspace();

          if (workspace.getStatus() == RUNNING) {
            workspace.getDevMachine().ifPresent(wsAgentURLModifier::initialize);
          }
        });

    eventBus.addHandler(
        WorkspaceRunningEvent.TYPE,
        e -> appContext.getWorkspace().getDevMachine().ifPresent(wsAgentURLModifier::initialize));
  }
}
