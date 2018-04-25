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
package org.eclipse.che.core.db;

import static org.eclipse.che.api.system.shared.SystemStatus.READY_TO_SHUTDOWN;

import com.google.inject.Inject;
import com.google.inject.persist.PersistService;
import javax.inject.Singleton;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.system.shared.dto.SystemStatusChangedEventDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stops {@link PersistService} when a system is ready to shutdown.
 *
 * @author Anton Korneta
 */
@Singleton
public class DBTerminator {

  private static final Logger LOG = LoggerFactory.getLogger(DBTerminator.class);

  @Inject
  public DBTerminator(EventService eventService, PersistService persistService) {
    eventService.subscribe(
        new EventSubscriber<SystemStatusChangedEventDto>() {
          @Override
          public void onEvent(SystemStatusChangedEventDto event) {
            if (READY_TO_SHUTDOWN.equals(event.getStatus())) {
              try {
                LOG.info("Stopping persistence service.");
                persistService.stop();
              } catch (RuntimeException ex) {
                LOG.error("Failed to stop persistent service. Cause: " + ex.getMessage());
              }
            }
          }
        },
        SystemStatusChangedEventDto.class);
  }
}
