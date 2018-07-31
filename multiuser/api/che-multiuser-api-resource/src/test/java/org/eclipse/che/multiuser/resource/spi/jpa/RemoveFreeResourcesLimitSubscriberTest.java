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
package org.eclipse.che.multiuser.resource.spi.jpa;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.eclipse.che.account.event.BeforeAccountRemovedEvent;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.multiuser.resource.spi.FreeResourcesLimitDao;
import org.eclipse.che.multiuser.resource.spi.jpa.JpaFreeResourcesLimitDao.RemoveFreeResourcesLimitSubscriber;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link RemoveFreeResourcesLimitSubscriber}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class RemoveFreeResourcesLimitSubscriberTest {
  @Mock private EventService eventService;
  @Mock private FreeResourcesLimitDao limitDao;

  @InjectMocks RemoveFreeResourcesLimitSubscriber subscriber;

  @Test
  public void shouldSubscribeItself() {
    subscriber.subscribe();

    verify(eventService).subscribe(eq(subscriber));
  }

  @Test
  public void shouldUnsubscribeItself() {
    subscriber.unsubscribe();

    verify(eventService).unsubscribe(eq(subscriber));
  }

  @Test
  public void shouldRemoveMembersOnBeforeOrganizationRemovedEvent() throws Exception {
    final AccountImpl account = new AccountImpl("id", "name", "test");

    subscriber.onEvent(new BeforeAccountRemovedEvent(account));

    verify(limitDao).remove("id");
  }
}
