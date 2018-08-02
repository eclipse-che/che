/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.organization.api.notification;

import static org.eclipse.che.api.core.Pages.iterate;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.mail.EmailBean;
import org.eclipse.che.mail.MailSender;
import org.eclipse.che.multiuser.organization.api.OrganizationManager;
import org.eclipse.che.multiuser.organization.shared.dto.MemberAddedEventDto;
import org.eclipse.che.multiuser.organization.shared.dto.MemberRemovedEventDto;
import org.eclipse.che.multiuser.organization.shared.dto.OrganizationRemovedEventDto;
import org.eclipse.che.multiuser.organization.shared.dto.OrganizationRenamedEventDto;
import org.eclipse.che.multiuser.organization.shared.event.OrganizationEvent;
import org.eclipse.che.multiuser.organization.shared.model.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Notify users about organization changes.
 *
 * @author Anton Korneta
 * @author Sergii Leshchenko
 */
@Singleton
public class OrganizationNotificationEmailSender implements EventSubscriber<OrganizationEvent> {

  private static final Logger LOG =
      LoggerFactory.getLogger(OrganizationNotificationEmailSender.class);

  private final String apiEndpoint;
  private final MailSender mailSender;
  private final OrganizationManager organizationManager;
  private final UserManager userManager;
  private final OrganizationEmailNotifications emails;

  @Inject
  public OrganizationNotificationEmailSender(
      @Named("che.api") String apiEndpoint,
      MailSender mailSender,
      OrganizationManager organizationManager,
      UserManager userManager,
      OrganizationEmailNotifications emails) {
    this.apiEndpoint = apiEndpoint;
    this.mailSender = mailSender;
    this.organizationManager = organizationManager;
    this.userManager = userManager;
    this.emails = emails;
  }

  @Inject
  public void subscribe(EventService eventService) {
    eventService.subscribe(this);
  }

  @Override
  public void onEvent(OrganizationEvent event) {
    try {
      if (event.getInitiator() != null) {
        if (event.getOrganization().getParent() == null) {
          try {
            userManager.getByName(event.getOrganization().getName());
            return;
          } catch (NotFoundException ex) {
            // it is not personal organization
          }
        }
        switch (event.getType()) {
          case MEMBER_ADDED:
            send((MemberAddedEventDto) event);
            break;
          case MEMBER_REMOVED:
            send((MemberRemovedEventDto) event);
            break;
          case ORGANIZATION_REMOVED:
            send((OrganizationRemovedEventDto) event);
            break;
          case ORGANIZATION_RENAMED:
            send((OrganizationRenamedEventDto) event);
        }
      }
    } catch (Exception ex) {
      LOG.error("Failed to send email notification '{}' cause : '{}'", ex.getMessage());
    }
  }

  private void send(MemberAddedEventDto event) throws ServerException {
    final String orgName = event.getOrganization().getName();
    final String emailTo = event.getMember().getEmail();
    final String initiator = event.getInitiator();
    final String dashboardEndpoint = apiEndpoint.replace("api", "dashboard");
    final String orgQualifiedName = event.getOrganization().getQualifiedName();
    EmailBean memberAddedEmail =
        emails.memberAdded(orgName, dashboardEndpoint, orgQualifiedName, initiator);
    mailSender.sendAsync(memberAddedEmail.withTo(emailTo));
  }

  private void send(MemberRemovedEventDto event) throws ServerException {
    final String organizationName = event.getOrganization().getName();
    final String initiator = event.getInitiator();
    final String emailTo = event.getMember().getEmail();

    EmailBean memberRemovedEmail = emails.memberRemoved(organizationName, initiator);
    mailSender.sendAsync(memberRemovedEmail.withTo(emailTo));
  }

  private void send(OrganizationRemovedEventDto event) throws ServerException, NotFoundException {
    String organizationName = event.getOrganization().getName();
    EmailBean orgRemovedEmail = emails.organizationRemoved(organizationName);
    for (String member : event.getMembers()) {
      try {
        final String emailTo = userManager.getById(member).getEmail();
        mailSender.sendAsync(new EmailBean(orgRemovedEmail).withTo(emailTo));
      } catch (Exception ignore) {
      }
    }
  }

  private void send(OrganizationRenamedEventDto event) throws ServerException, NotFoundException {
    EmailBean orgRenamedEmail = emails.organizationRenamed(event.getOldName(), event.getNewName());
    for (Member member :
        iterate(
            (max, skip) ->
                organizationManager.getMembers(event.getOrganization().getId(), max, skip))) {
      try {
        final String emailTo = userManager.getById(member.getUserId()).getEmail();
        mailSender.sendAsync(new EmailBean(orgRenamedEmail).withTo(emailTo));
      } catch (Exception ignore) {
      }
    }
  }
}
