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
package org.eclipse.che.core.db.jpa;

import static org.eclipse.che.api.system.shared.SystemStatus.READY_TO_SHUTDOWN;

import com.google.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManagerFactory;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.system.shared.event.SystemStatusChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs termination of db components.
 *
 * @author Anton Korneta
 */
@Singleton
public class DBTerminator {

  private static final Logger LOG = LoggerFactory.getLogger(DBTerminator.class);

  private final EntityManagerFactory emFactory;

  @Inject
  public DBTerminator(EventService eventService, EntityManagerFactory emFactory) {
    this.emFactory = emFactory;
    eventService.subscribe(
        new EventSubscriber<SystemStatusChangedEvent>() {
          @Override
          public void onEvent(SystemStatusChangedEvent event) {
            if (READY_TO_SHUTDOWN.equals(event.getStatus())) {
              terminate();
            }
          }
        });
  }

  public void terminate() {
    try {
      LOG.info("Close entity manager factory..");
      emFactory.close();
    } catch (RuntimeException ex) {
      LOG.error(ex.getMessage());
    }
  }
}
