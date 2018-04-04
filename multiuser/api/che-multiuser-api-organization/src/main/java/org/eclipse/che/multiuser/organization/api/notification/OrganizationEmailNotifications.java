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

import static javax.ws.rs.core.MediaType.TEXT_HTML;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.mail.EmailBean;
import org.eclipse.che.mail.template.TemplateProcessor;
import org.eclipse.che.mail.template.exception.TemplateException;

/**
 * Builds emails notification about organization changes.
 *
 * @author Sergii Leshchenko
 */
public class OrganizationEmailNotifications {

  private final String mailFrom;
  private final String memberAddedSubject;
  private final String memberAddedTemplate;
  private final String memberRemovedSubject;
  private final String memberRemovedTemplate;
  private final String orgRemovedSubject;
  private final String orgRemovedTemplate;
  private final String orgRenamedSubject;
  private final String orgRenamedTemplate;
  private final TemplateProcessor templateProcessor;

  @Inject
  public OrganizationEmailNotifications(
      @Named("che.mail.from_email_address") String mailFrom,
      @Named("che.organization.email.member_added_subject") String memberAddedSubject,
      @Named("che.organization.email.member_added_template") String memberAddedTemplate,
      @Named("che.organization.email.member_removed_subject") String memberRemovedSubject,
      @Named("che.organization.email.member_removed_template") String memberRemovedTemplate,
      @Named("che.organization.email.org_removed_subject") String orgRemovedSubject,
      @Named("che.organization.email.org_removed_template") String orgRemovedTemplate,
      @Named("che.organization.email.org_renamed_subject") String orgRenamedSubject,
      @Named("che.organization.email.org_renamed_template") String orgRenamedTemplate,
      TemplateProcessor templateProcessor) {
    this.mailFrom = mailFrom;
    this.memberAddedSubject = memberAddedSubject;
    this.memberAddedTemplate = memberAddedTemplate;
    this.memberRemovedSubject = memberRemovedSubject;
    this.memberRemovedTemplate = memberRemovedTemplate;
    this.orgRemovedSubject = orgRemovedSubject;
    this.orgRemovedTemplate = orgRemovedTemplate;
    this.orgRenamedSubject = orgRenamedSubject;
    this.orgRenamedTemplate = orgRenamedTemplate;
    this.templateProcessor = templateProcessor;
  }

  public EmailBean memberAdded(
      String organizationName, String dashboardEndpoint, String orgQualifiedName, String initiator)
      throws ServerException {
    Map<String, Object> attributes = new HashMap<>();
    attributes.put("organizationName", organizationName);
    attributes.put("dashboardEndpoint", dashboardEndpoint);
    attributes.put("orgQualifiedName", orgQualifiedName);
    attributes.put("initiator", initiator);

    return doBuildEmail(memberAddedSubject, memberAddedTemplate, attributes);
  }

  public EmailBean memberRemoved(String organizationName, String initiator) throws ServerException {
    Map<String, Object> attributes = new HashMap<>();
    attributes.put("organizationName", organizationName);
    attributes.put("initiator", initiator);

    return doBuildEmail(memberRemovedSubject, memberRemovedTemplate, attributes);
  }

  public EmailBean organizationRenamed(String oldName, String newName) throws ServerException {
    Map<String, Object> attributes = new HashMap<>();
    attributes.put("orgOldName", oldName);
    attributes.put("orgNewName", newName);
    return doBuildEmail(orgRenamedSubject, orgRenamedTemplate, attributes);
  }

  public EmailBean organizationRemoved(String organizationName) throws ServerException {
    Map<String, Object> attributes = new HashMap<>();
    attributes.put("organizationName", organizationName);
    return doBuildEmail(orgRemovedSubject, orgRemovedTemplate, attributes);
  }

  protected EmailBean doBuildEmail(
      String subject, String templatePath, Map<String, Object> attributes) throws ServerException {
    try {
      return new EmailBean()
          .withSubject(subject)
          .withBody(templateProcessor.process(templatePath, attributes))
          .withFrom(mailFrom)
          .withReplyTo(mailFrom)
          .withMimeType(TEXT_HTML);
    } catch (TemplateException e) {
      throw new ServerException(e.getMessage());
    }
  }
}
