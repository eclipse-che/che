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
package org.eclipse.che.ide.ext.machine.server.ssh;

import java.util.concurrent.CompletableFuture;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.agent.exec.client.ExecAgentClientFactory;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.ssh.server.SshManager;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
import org.eclipse.che.commons.lang.concurrent.ThreadLocalPropagateContext;

/**
 * Handles workspace startup events and starts injection of SSH keys.
 *
 * @author Max Shaposhnyk (mshaposh@redhat.com)
 */
@Singleton
public class WorkspaceStatusSubscriber {

  private final EventService eventService;
  private final KeysInjector keysInjector;

  @Inject
  public WorkspaceStatusSubscriber(
      EventService eventService,
      SshManager sshManager,
      WorkspaceManager workspaceManager,
      ExecAgentClientFactory execAgentClientFactory) {
    this.eventService = eventService;
    this.keysInjector = new KeysInjector(sshManager, workspaceManager, execAgentClientFactory);
  }

  @PostConstruct
  public void start() {
    eventService.subscribe(
        new EventSubscriber<WorkspaceStatusEvent>() {
          @Override
          public void onEvent(WorkspaceStatusEvent event) {
            if (event.getStatus() != WorkspaceStatus.RUNNING) {
              return;
            }
            CompletableFuture.runAsync(
                ThreadLocalPropagateContext.wrap(
                    () -> keysInjector.injectPublicKeys(event.getWorkspaceId())));
          }
        });
  }
}
