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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;

import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.eclipse.che.mail.EmailBean;
import org.eclipse.che.mail.template.TemplateProcessor;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Test for {@link OrganizationEmailNotifications}.
 *
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class OrganizationEmailNotificationsTest {

  private static final String MAIL_FROM = "mail@from.com";
  private static final String MEMBER_ADDED_SUBJECT = "Member Added";
  private static final String MEMBER_ADDED_TEMPLATE = "/member-added";
  private static final String MEMBER_REMOVED_SUBJECT = "Member Removed";
  private static final String MEMBER_REMOVED_TEMPLATE = "/member-removed";
  private static final String ORG_REMOVED_SUBJECT = "Org Removed";
  private static final String ORG_REMOVED_TEMPLATE = "/org-removed";
  private static final String ORG_RENAMED_SUBJECT = "Org Renamed";
  private static final String ORG_RENAMED_TEMPLATE = "/org-renamed";

  @Mock private TemplateProcessor templateProcessor;
  @Captor private ArgumentCaptor<Map<String, Object>> attributesCaptor;

  private OrganizationEmailNotifications emails;

  @BeforeMethod
  public void setUp() throws Exception {
    initMocks(this);

    emails =
        new OrganizationEmailNotifications(
            MAIL_FROM,
            MEMBER_ADDED_SUBJECT,
            MEMBER_ADDED_TEMPLATE,
            MEMBER_REMOVED_SUBJECT,
            MEMBER_REMOVED_TEMPLATE,
            ORG_REMOVED_SUBJECT,
            ORG_REMOVED_TEMPLATE,
            ORG_RENAMED_SUBJECT,
            ORG_RENAMED_TEMPLATE,
            templateProcessor);
  }

  @Test
  public void shouldReturnMemberAddedEmail() throws Exception {
    EmailBean email =
        emails.memberAdded("SuperOrg", "localhost:8080/dashboard", "/superOrg/org", "admin");

    assertEquals(email.getFrom(), MAIL_FROM);
    assertEquals(email.getReplyTo(), MAIL_FROM);
    assertEquals(email.getMimeType(), MediaType.TEXT_HTML);
    assertEquals(email.getSubject(), MEMBER_ADDED_SUBJECT);

    verify(templateProcessor).process(eq(MEMBER_ADDED_TEMPLATE), attributesCaptor.capture());
    Map<String, Object> attributes = attributesCaptor.getValue();
    assertEquals(attributes.get("organizationName"), "SuperOrg");
    assertEquals(attributes.get("dashboardEndpoint"), "localhost:8080/dashboard");
    assertEquals(attributes.get("orgQualifiedName"), "/superOrg/org");
    assertEquals(attributes.get("initiator"), "admin");
  }

  @Test
  public void shouldReturnMemberRemovedEmail() throws Exception {
    EmailBean email = emails.memberRemoved("SuperOrg", "admin");

    assertEquals(email.getFrom(), MAIL_FROM);
    assertEquals(email.getReplyTo(), MAIL_FROM);
    assertEquals(email.getMimeType(), MediaType.TEXT_HTML);
    assertEquals(email.getSubject(), MEMBER_REMOVED_SUBJECT);

    verify(templateProcessor).process(eq(MEMBER_REMOVED_TEMPLATE), attributesCaptor.capture());
    Map<String, Object> attributes = attributesCaptor.getValue();
    assertEquals(attributes.get("organizationName"), "SuperOrg");
    assertEquals(attributes.get("initiator"), "admin");
  }

  @Test
  public void shouldReturnOrgRenamedEmail() throws Exception {
    EmailBean email = emails.organizationRenamed("Org", "SuperOrg");

    assertEquals(email.getFrom(), MAIL_FROM);
    assertEquals(email.getReplyTo(), MAIL_FROM);
    assertEquals(email.getMimeType(), MediaType.TEXT_HTML);
    assertEquals(email.getSubject(), ORG_RENAMED_SUBJECT);

    verify(templateProcessor).process(eq(ORG_RENAMED_TEMPLATE), attributesCaptor.capture());
    Map<String, Object> attributes = attributesCaptor.getValue();
    assertEquals(attributes.get("orgOldName"), "Org");
    assertEquals(attributes.get("orgNewName"), "SuperOrg");
  }

  @Test
  public void shouldReturnOrgRemovedEmail() throws Exception {
    EmailBean email = emails.organizationRemoved("Org");

    assertEquals(email.getFrom(), MAIL_FROM);
    assertEquals(email.getReplyTo(), MAIL_FROM);
    assertEquals(email.getMimeType(), MediaType.TEXT_HTML);
    assertEquals(email.getSubject(), ORG_REMOVED_SUBJECT);

    verify(templateProcessor).process(eq(ORG_REMOVED_TEMPLATE), attributesCaptor.capture());
    Map<String, Object> attributes = attributesCaptor.getValue();
    assertEquals(attributes.get("organizationName"), "Org");
  }
}
