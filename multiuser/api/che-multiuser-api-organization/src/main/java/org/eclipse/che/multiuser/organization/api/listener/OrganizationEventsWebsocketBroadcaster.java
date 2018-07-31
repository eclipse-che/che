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
package org.eclipse.che.multiuser.organization.api.listener;

import static org.eclipse.che.multiuser.organization.shared.event.EventType.MEMBER_ADDED;
import static org.eclipse.che.multiuser.organization.shared.event.EventType.MEMBER_REMOVED;

import java.util.Map;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.notification.RemoteSubscriptionManager;
import org.eclipse.che.multiuser.organization.shared.dto.MemberAddedEventDto;
import org.eclipse.che.multiuser.organization.shared.dto.MemberRemovedEventDto;
import org.eclipse.che.multiuser.organization.shared.event.OrganizationEvent;

/**
 * Broadcasts organization events through websocket connection.
 *
 * @author Anton Korneta
 */
@Singleton
public class OrganizationEventsWebsocketBroadcaster {

  private final RemoteSubscriptionManager remoteSubscriptionManager;

  private static final String ORGANIZATION_MEMBERSHIP_METHOD_NAME =
      "organization/membershipChanged";
  private static final String ORGANIZATION_CHANGED_METHOD_NAME = "organization/statusChanged";

  @Inject
  public OrganizationEventsWebsocketBroadcaster(
      RemoteSubscriptionManager remoteSubscriptionManager) {
    this.remoteSubscriptionManager = remoteSubscriptionManager;
  }

  @PostConstruct
  private void subscribe() {
    remoteSubscriptionManager.register(
        ORGANIZATION_MEMBERSHIP_METHOD_NAME, OrganizationEvent.class, this::predicate);
    remoteSubscriptionManager.register(
        ORGANIZATION_CHANGED_METHOD_NAME, OrganizationEvent.class, this::predicate);
  }

  private boolean predicate(OrganizationEvent event, Map<String, String> scope) {
    if (MEMBER_ADDED == event.getType()) {
      return ((MemberAddedEventDto) event).getMember().getId().equals(scope.get("userId"));
    } else if (MEMBER_REMOVED == event.getType()) {
      return ((MemberRemovedEventDto) event).getMember().getId().equals(scope.get("userId"));
    } else {
      return event.getOrganization().getId().equals(scope.get("organizationId"));
    }
  }
}
