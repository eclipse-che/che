/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.user.server.event;

import org.eclipse.che.api.core.notification.EventOrigin;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.core.db.cascade.event.RemoveEvent;

/**
 * Published before {@link UserImpl user} removed.
 *
 * @author Yevhenii Voevodin
 */
@EventOrigin("user")
public class BeforeUserRemovedEvent extends RemoveEvent {

  private final UserImpl user;

  public BeforeUserRemovedEvent(UserImpl user) {
    this.user = user;
  }

  /** Returns user which is going to be removed. */
  public UserImpl getUser() {
    return user;
  }
}
