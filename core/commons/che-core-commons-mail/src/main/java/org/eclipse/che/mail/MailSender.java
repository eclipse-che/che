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
package org.eclipse.che.mail;

import static java.util.concurrent.Executors.newFixedThreadPool;

import com.google.common.io.Files;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.apache.commons.io.FileUtils;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.commons.lang.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides email sending capability
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class MailSender {
  private static final Logger LOG = LoggerFactory.getLogger(MailSender.class);

  private final ExecutorService executor;
  private Session mailSession;

  @Inject
  public MailSender(Session mailSession) {
    this.mailSession = mailSession;
    this.executor =
        newFixedThreadPool(
            2 * Runtime.getRuntime().availableProcessors(),
            new ThreadFactoryBuilder()
                .setNameFormat("MailNotificationsPool-%d")
                .setDaemon(false)
                .build());
  }

  public void sendMail(
      String from, String to, String replyTo, String subject, String mimeType, String template)
      throws ServerException {
    sendMail(from, to, replyTo, subject, mimeType, template, null);
  }

  public void sendMail(
      String from,
      String to,
      String replyTo,
      String subject,
      String mimeType,
      String template,
      Map<String, String> templateProperties)
      throws ServerException {
    EmailBean emailBean =
        new EmailBean()
            .withBody(
                templateProperties == null
                    ? template
                    : Deserializer.resolveVariables(template, templateProperties))
            .withFrom(from)
            .withTo(to)
            .withReplyTo(replyTo)
            .withSubject(subject)
            .withMimeType(mimeType);

    sendMail(emailBean);
  }

  public void sendAsync(EmailBean emailBean) {
    executor.submit(
        () -> {
          try {
            sendMail(emailBean);
          } catch (Exception ex) {
            LOG.warn(
                "Failed to send email notification for {} with subject {}. Cause: '{}'",
                emailBean.getTo(),
                emailBean.getSubject(),
                ex.getLocalizedMessage());
          }
        });
  }

  public void sendMail(EmailBean emailBean) throws ServerException {
    File tempDir = null;
    try {
      MimeMessage message = new MimeMessage(mailSession);
      Multipart contentPart = new MimeMultipart();

      MimeBodyPart bodyPart = new MimeBodyPart();
      bodyPart.setText(emailBean.getBody(), "UTF-8", getSubType(emailBean.getMimeType()));
      contentPart.addBodyPart(bodyPart);

      if (emailBean.getAttachments() != null) {
        tempDir = Files.createTempDir();
        for (Attachment attachment : emailBean.getAttachments()) {
          // Create attachment file in temporary directory
          byte[] attachmentContent = Base64.getDecoder().decode(attachment.getContent());
          File attachmentFile = new File(tempDir, attachment.getFileName());
          Files.write(attachmentContent, attachmentFile);

          // Attach the attachment file to email
          MimeBodyPart attachmentPart = new MimeBodyPart();
          attachmentPart.attachFile(attachmentFile);
          attachmentPart.setContentID("<" + attachment.getContentId() + ">");
          contentPart.addBodyPart(attachmentPart);
        }
      }

      message.setContent(contentPart);
      message.setSubject(emailBean.getSubject(), "UTF-8");
      message.setFrom(new InternetAddress(emailBean.getFrom(), true));
      message.setSentDate(new Date());
      message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(emailBean.getTo()));

      if (emailBean.getReplyTo() != null) {
        message.setReplyTo(InternetAddress.parse(emailBean.getReplyTo()));
      }
      LOG.info(
          "Sending from {} to {} with subject {}",
          emailBean.getFrom(),
          emailBean.getTo(),
          emailBean.getSubject());

      Transport.send(message);
      LOG.debug("Mail send");
    } catch (Exception e) {
      LOG.error(e.getLocalizedMessage());
    } finally {
      if (tempDir != null) {
        try {
          FileUtils.deleteDirectory(tempDir);
        } catch (IOException exception) {
          LOG.error(exception.getMessage());
        }
      }
    }
  }

  /**
   * Get the specified MIME subtype from given primary MIME type.
   *
   * <p>It is needed for setText method in MimeBodyPar because it works only with text MimeTypes.
   * setText method in MimeBodyPar already adds predefined "text/" to given subtype.
   *
   * @param mimeType primary MIME type
   * @return MIME subtype
   */
  private String getSubType(String mimeType) {
    return mimeType.substring(mimeType.lastIndexOf("/") + 1);
  }

  @PreDestroy
  public void shutdown() throws InterruptedException {
    // Tell threads to finish off.
    executor.shutdown(); // Disable new tasks from being submitted
    try {
      // Wait a while for existing tasks to terminate
      if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
        executor.shutdownNow(); // Cancel currently executing tasks
        // Wait a while for tasks to respond to being cancelled
        if (!executor.awaitTermination(60, TimeUnit.SECONDS)) LOG.warn("Pool did not terminate");
      }
    } catch (InterruptedException ie) {
      // (Re-)Cancel if current thread also interrupted
      executor.shutdownNow();
      // Preserve interrupt status
      Thread.currentThread().interrupt();
    }
  }
}
