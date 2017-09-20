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

import java.util.Map;
import java.util.Properties;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import org.eclipse.che.inject.ConfigurationProperties;

public class MailSessionProvider implements Provider<Session> {

  private final Session session;

  @Inject
  public MailSessionProvider(ConfigurationProperties configurationProperties) {

    Map<String, String> mailConfiguration = configurationProperties.getProperties("mail.*");

    Properties props = new Properties();
    mailConfiguration.forEach(props::setProperty);

    if (Boolean.parseBoolean(props.getProperty("mail.smtp.auth"))) {
      final String username = props.getProperty("mail.smtp.auth.username");
      final String password = props.getProperty("mail.smtp.auth.password");

      // remove useless properties
      props.remove("mail.smtp.auth.username");
      props.remove("mail.smtp.auth.password");

      this.session =
          Session.getInstance(
              props,
              new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                  return new PasswordAuthentication(username, password);
                }
              });
    } else {
      this.session = Session.getInstance(props);
    }
  }

  @Override
  public Session get() {
    return session;
  }
}
