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
package org.eclipse.che.multiuser.permission.workspace.server.stack;

import com.google.inject.Inject;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.event.StackPersistedEvent;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.core.db.cascade.CascadeEventSubscriber;
import org.eclipse.che.multiuser.api.permission.server.PermissionsManager;

/**
 * Grants access to stack which is created by user who is {@link EnvironmentContext#getSubject()
 * subject}, if there is no subject present in {@link EnvironmentContext#getCurrent() current}
 * context then no permissions will be added.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class StackCreatorPermissionsProvider extends CascadeEventSubscriber<StackPersistedEvent> {

  @Inject private PermissionsManager permissionsManager;

  @Inject private EventService eventService;

  @Override
  public void onCascadeEvent(StackPersistedEvent event) throws Exception {
    final Subject subject = EnvironmentContext.getCurrent().getSubject();
    if (!subject.isAnonymous()) {
      permissionsManager.storePermission(
          new StackPermissionsImpl(
              subject.getUserId(), event.getStack().getId(), StackDomain.getActions()));
    }
  }

  @PostConstruct
  public void subscribe() {
    eventService.subscribe(this, StackPersistedEvent.class);
  }

  @PreDestroy
  public void unsubscribe() {
    eventService.unsubscribe(this, StackPersistedEvent.class);
  }
}
