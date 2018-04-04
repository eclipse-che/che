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
package org.eclipse.che.mail;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MailSenderTest {
  private MailSender mailSender;
  private SimpleSmtpServer server;

  public static void assertMail(
      SimpleSmtpServer server,
      String from,
      String to,
      String replyTo,
      String subject,
      String mimeType,
      String body,
      String attachmentContentID,
      String attachmentFileName) {
    assertEquals(server.getReceivedEmails().size(), 1);
    SmtpMessage email = server.getReceivedEmails().iterator().next();

    assertEquals(email.getHeaderValue("Subject"), subject);
    assertEquals(email.getHeaderValue("From"), from);
    assertEquals(email.getHeaderValue("Reply-To"), replyTo);
    assertEquals(email.getHeaderValue("To"), to);
    assertTrue(email.getBody().contains("Content-Type: " + mimeType));
    assertTrue(email.getBody().contains(body));
    if (attachmentFileName != null && attachmentContentID != null) {
      assertTrue(email.getBody().contains("filename=" + attachmentFileName));
      assertTrue(email.getBody().contains("Content-ID: <" + attachmentContentID + ">"));
    }
  }

  @BeforeMethod
  public void setup(ITestContext context) throws IOException {
    server = SimpleSmtpServer.start(SimpleSmtpServer.AUTO_SMTP_PORT);

    Map<String, String> mailConfiguration =
        ImmutableMap.of(
            "mail.smtp.host",
            "localhost",
            "mail.smtp.port",
            server.getPort() + "",
            "mail.transport.protocol",
            "smtp",
            " mail.smtp.auth",
            "false");

    mailSender = new MailSender(new MailSessionProvider(mailConfiguration));
  }

  @AfterMethod
  public void stop() {
    server.stop();
  }

  @Test
  public void shouldBeAbleToSendMessage() throws SendMailException {
    EmailBean emailBean =
        new EmailBean()
            .withFrom("noreply@cloud-ide.com")
            .withTo("dev-test@cloud-ide.com")
            .withReplyTo("dev-test@cloud-ide.com")
            .withSubject("Subject")
            .withMimeType("text/html")
            .withBody("hello user");
    mailSender.sendMail(emailBean);
    assertMail(
        server,
        "noreply@cloud-ide.com",
        "dev-test@cloud-ide.com",
        "dev-test@cloud-ide.com",
        "Subject",
        "text/html",
        "hello user",
        null,
        null);
  }

  @Test
  public void shouldBeAbleToSendMessageWithFormattedFields() throws SendMailException {
    EmailBean emailBean =
        new EmailBean()
            .withFrom("Exo IDE <noreply@cloud-ide.com>")
            .withTo("dev-test@cloud-ide.com")
            .withReplyTo("Developers to reply <dev-test@cloud-ide.com>")
            .withSubject("Subject")
            .withMimeType("text/html")
            .withBody("hello user");
    mailSender.sendMail(emailBean);
    assertMail(
        server,
        "Exo IDE <noreply@cloud-ide.com>",
        "dev-test@cloud-ide.com",
        "Developers to reply <dev-test@cloud-ide.com>",
        "Subject",
        "text/html",
        "hello user",
        null,
        null);
  }

  @Test
  public void shouldBeAbleToSendMessageToFewEmails() throws SendMailException {
    EmailBean emailBean =
        new EmailBean()
            .withFrom("noreply@cloud-ide.com")
            .withTo("dev-test@cloud-ide.com, dev-test1@cloud-ide.com, dev-test2@cloud-ide.com")
            .withReplyTo("dev-test@cloud-ide.com")
            .withSubject("Subject")
            .withMimeType("text/html")
            .withBody("hello user");
    mailSender.sendMail(emailBean);

    assertMail(
        server,
        "noreply@cloud-ide.com",
        "dev-test@cloud-ide.com, dev-test1@cloud-ide.com, dev-test2@cloud-ide.com",
        "dev-test@cloud-ide.com",
        "Subject",
        "text/html",
        "hello user",
        null,
        null);
  }

  @Test
  public void shouldBeAbleToSendMessageWithAttachment() throws SendMailException {
    EmailBean emailBean =
        new EmailBean()
            .withFrom("noreply@cloud-ide.com")
            .withTo("dev-test@cloud-ide.com")
            .withReplyTo("dev-test@cloud-ide.com")
            .withSubject("Subject")
            .withMimeType("text/html")
            .withBody("hello user");

    Attachment attachment =
        new Attachment()
            .withContentId("attachmentId")
            .withFileName("attachment.txt")
            .withContent(Base64.getEncoder().encodeToString("attachmentContent".getBytes(UTF_8)));

    emailBean.setAttachments(Collections.singletonList(attachment));

    mailSender.sendMail(emailBean);
    assertMail(
        server,
        "noreply@cloud-ide.com",
        "dev-test@cloud-ide.com",
        "dev-test@cloud-ide.com",
        "Subject",
        "text/html",
        "hello user",
        "attachmentId",
        "attachment.txt");
  }
}
