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
package org.eclipse.che.multiuser.permission.machine.recipe;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.recipe.RecipePersistedEvent;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.core.db.cascade.CascadeEventSubscriber;
import org.eclipse.che.multiuser.api.permission.server.PermissionsManager;

/**
 * Adds recipe permissions for current subject, if there is no subject present in {@link
 * EnvironmentContext#getCurrent() current} context then no permissions will be added.
 *
 * @author Anton Korneta
 */
@Singleton
public class RecipeCreatorPermissionsProvider extends CascadeEventSubscriber<RecipePersistedEvent> {

  @Inject private PermissionsManager permissionsManager;

  @Inject private EventService eventService;

  @Override
  public void onCascadeEvent(RecipePersistedEvent event) throws Exception {
    final Subject subject = EnvironmentContext.getCurrent().getSubject();
    if (!subject.isAnonymous()) {
      permissionsManager.storePermission(
          new RecipePermissionsImpl(
              subject.getUserId(), event.getRecipe().getId(), RecipeDomain.getActions()));
    }
  }

  @PostConstruct
  public void subscribe() {
    eventService.subscribe(this, RecipePersistedEvent.class);
  }

  @PreDestroy
  public void unsubscribe() {
    eventService.unsubscribe(this, RecipePersistedEvent.class);
  }
}
