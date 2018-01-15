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
package org.eclipse.che.multiuser.organization.api.notification;

import static java.util.Arrays.*;
import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.commons.test.mockito.answer.SelfReturningAnswer;
import org.eclipse.che.mail.EmailBean;
import org.eclipse.che.mail.MailSender;
import org.eclipse.che.multiuser.organization.api.OrganizationManager;
import org.eclipse.che.multiuser.organization.api.event.MemberAddedEvent;
import org.eclipse.che.multiuser.organization.api.event.MemberRemovedEvent;
import org.eclipse.che.multiuser.organization.api.event.OrganizationRemovedEvent;
import org.eclipse.che.multiuser.organization.api.event.OrganizationRenamedEvent;
import org.eclipse.che.multiuser.organization.shared.model.Member;
import org.eclipse.che.multiuser.organization.spi.impl.MemberImpl;
import org.eclipse.che.multiuser.organization.spi.impl.OrganizationImpl;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Test for {@link OrganizationNotificationEmailSender}.
 *
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class OrganizationNotificationEmailSenderTest {

  public static final String API_ENDPOINT = "http://localhost/api";
  public static final String DASHBOARD_ENDPOINT = API_ENDPOINT.replace("/api", "/dashboard");
  @Mock private MailSender mailSender;
  @Mock private OrganizationManager organizationManager;
  @Mock private UserManager userManager;
  @Mock private OrganizationEmailNotifications emails;
  @Mock private EventService eventService;

  OrganizationNotificationEmailSender emailSender;

  @BeforeMethod
  public void setUp() throws Exception {
    emailSender =
        new OrganizationNotificationEmailSender(
            API_ENDPOINT, mailSender, organizationManager, userManager, emails);
  }

  @Test
  public void shouldSelfSubscribe() {
    // when
    emailSender.subscribe(eventService);

    // then
    verify(eventService).subscribe(emailSender);
  }

  @Test
  public void shouldSendNotificationAboutMembershipAdding() throws Exception {
    // given
    EmailBean email = mock(EmailBean.class, new SelfReturningAnswer());

    when(emails.memberAdded(anyString(), anyString(), anyString(), anyString())).thenReturn(email);

    // when
    emailSender.onEvent(
        new MemberAddedEvent(
            "admin",
            new UserImpl("id", "email", null),
            new OrganizationImpl("id", "/parent/name", "parent")));

    // then
    verify(emails).memberAdded("name", DASHBOARD_ENDPOINT, "/parent/name", "admin");
    verify(email).withTo("email");
    verify(mailSender).sendAsync(email);
  }

  @Test
  public void shouldSendNotificationAboutMembershipRemoving() throws Exception {
    // given
    EmailBean email = mock(EmailBean.class, new SelfReturningAnswer());

    when(emails.memberRemoved(anyString(), anyString())).thenReturn(email);

    // when
    emailSender.onEvent(
        new MemberRemovedEvent(
            "admin",
            new UserImpl("id", "email", null),
            new OrganizationImpl("id", "/parent/name", "parent")));

    // then
    verify(emails).memberRemoved("name", "admin");
    verify(email).withTo("email");
    verify(mailSender).sendAsync(email);
  }

  @Test
  public void shouldSendNotificationAboutOrganizationRenaming() throws Exception {
    // given
    MemberImpl member1 = new MemberImpl("user1", "org123", ImmutableList.of());
    MemberImpl member2 = new MemberImpl("user2", "org123", ImmutableList.of());
    doReturn(new Page<Member>(asList(member1, member2), 0, 2, 2))
        .when(organizationManager)
        .getMembers(anyString(), anyInt(), anyLong());

    when(userManager.getById("user1"))
        .thenReturn(new UserImpl("user1", "email1", null, null, emptyList()));
    when(userManager.getById("user2"))
        .thenReturn(new UserImpl("user2", "email2", null, null, emptyList()));

    EmailBean email = new EmailBean().withBody("Org Remaned Notification");
    when(emails.organizationRenamed(anyString(), anyString())).thenReturn(email);

    // when
    emailSender.onEvent(
        new OrganizationRenamedEvent(
            "admin",
            "oldName",
            "newName",
            new OrganizationImpl("org123", "/parent/newName", "parent")));

    // then
    verify(emails).organizationRenamed("oldName", "newName");
    verify(mailSender).sendAsync(new EmailBean(email).withTo("email1"));
    verify(mailSender).sendAsync(new EmailBean(email).withTo("email2"));
  }

  @Test
  public void
      shouldDoNotBreakSendingOfNotificationAboutOrganizationRenamingWhenUnableToRetrieveAUser()
          throws Exception {
    // given
    MemberImpl member1 = new MemberImpl("user1", "org123", emptyList());
    MemberImpl member2 = new MemberImpl("user2", "org123", emptyList());
    doReturn(new Page<Member>(asList(member1, member2), 0, 2, 2))
        .when(organizationManager)
        .getMembers(anyString(), anyInt(), anyLong());

    when(userManager.getById("user1")).thenThrow(new NotFoundException(""));
    when(userManager.getById("user2"))
        .thenReturn(new UserImpl("user2", "email2", null, null, emptyList()));

    EmailBean email = new EmailBean().withBody("Org Renamed Notification");
    when(emails.organizationRenamed(anyString(), anyString())).thenReturn(email);

    // when
    emailSender.onEvent(
        new OrganizationRenamedEvent(
            "admin",
            "oldName",
            "newName",
            new OrganizationImpl("org123", "/parent/newName", "parent")));

    // then
    verify(emails).organizationRenamed("oldName", "newName");
    verify(mailSender).sendAsync(new EmailBean(email).withTo("email2"));
  }

  @Test
  public void shouldSendNotificationAboutOrganizationRemoving() throws Exception {
    // given
    MemberImpl member1 = new MemberImpl("user1", "org123", emptyList());
    MemberImpl member2 = new MemberImpl("user2", "org123", emptyList());

    when(userManager.getById("user1"))
        .thenReturn(new UserImpl("user1", "email1", null, null, emptyList()));
    when(userManager.getById("user2"))
        .thenReturn(new UserImpl("user2", "email2", null, null, emptyList()));

    EmailBean email = new EmailBean().withBody("Org Removed Notification");
    when(emails.organizationRemoved(anyString())).thenReturn(email);

    // when
    emailSender.onEvent(
        new OrganizationRemovedEvent(
            "admin", new OrganizationImpl("id", "/parent/q", "parent"), asList(member1, member2)));

    // then
    verify(emails).organizationRemoved("q");
    verify(mailSender).sendAsync(new EmailBean(email).withTo("email1"));
    verify(mailSender).sendAsync(new EmailBean(email).withTo("email2"));
  }

  @Test
  public void
      shouldDoNotBreakSendingOfNotificationAboutOrganizationRemovingWhenUnableToRetrieveAUser()
          throws Exception {
    // given
    MemberImpl member1 = new MemberImpl("user1", "org123", emptyList());
    MemberImpl member2 = new MemberImpl("user2", "org123", emptyList());

    when(userManager.getById("user1")).thenThrow(new NotFoundException(""));
    when(userManager.getById("user2"))
        .thenReturn(new UserImpl("user2", "email2", null, null, emptyList()));

    EmailBean email = new EmailBean().withBody("Org Removed Notification");
    when(emails.organizationRemoved(anyString())).thenReturn(email);

    // when
    emailSender.onEvent(
        new OrganizationRemovedEvent(
            "admin", new OrganizationImpl("id", "/parent/q", "parent"), asList(member1, member2)));

    // then
    verify(emails).organizationRemoved("q");
    verify(mailSender).sendAsync(new EmailBean(email).withTo("email2"));
  }
}
