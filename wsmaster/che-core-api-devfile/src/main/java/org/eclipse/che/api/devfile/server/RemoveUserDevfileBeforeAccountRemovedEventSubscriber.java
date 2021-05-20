/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.devfile.server;

import static org.eclipse.che.api.core.Pages.iterate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.account.event.BeforeAccountRemovedEvent;
import org.eclipse.che.api.core.model.workspace.devfile.UserDevfile;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.core.db.cascade.CascadeEventSubscriber;

/**
 * An event listener that is removing all {@link UserDevfile}s that belong to the account that is
 * going to be removed.
 */
@Singleton
public class RemoveUserDevfileBeforeAccountRemovedEventSubscriber
    extends CascadeEventSubscriber<BeforeAccountRemovedEvent> {

  private final EventService eventService;
  private final UserDevfileManager userDevfileManager;

  @Inject
  public RemoveUserDevfileBeforeAccountRemovedEventSubscriber(
      EventService eventService, UserDevfileManager userDevfileManager) {
    this.eventService = eventService;
    this.userDevfileManager = userDevfileManager;
  }

  @PostConstruct
  public void subscribe() {
    eventService.subscribe(this, BeforeAccountRemovedEvent.class);
  }

  @PreDestroy
  public void unsubscribe() {
    eventService.unsubscribe(this, BeforeAccountRemovedEvent.class);
  }

  @Override
  public void onCascadeEvent(BeforeAccountRemovedEvent event) throws Exception {
    for (UserDevfile userDevfile :
        iterate(
            (maxItems, skipCount) ->
                userDevfileManager.getByNamespace(
                    event.getAccount().getName(), maxItems, skipCount))) {
      userDevfileManager.removeUserDevfile(userDevfile.getId());
    }
  }
}
