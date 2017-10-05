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

import com.google.common.annotations.VisibleForTesting;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import org.eclipse.che.inject.ConfigurationProperties;

/** Provider of {@link Session} */
@Singleton
public class MailSessionProvider implements Provider<Session> {

  private final Session session;
  /**
   * Configuration can be injected from container with help of {@lin ConfigurationProperties} class.
   * In this case all properties that starts with 'che.mail.' will be used to create {@link
   * Session}. First 4 letters 'che.' from property names will be removed.
   */
  @Inject
  public MailSessionProvider(ConfigurationProperties configurationProperties) {

    this(
        configurationProperties
            .getProperties("che.mail.*")
            .entrySet()
            .stream()
            .collect(Collectors.toMap(e -> e.getKey().substring(4), Map.Entry::getValue)));
  }

  @VisibleForTesting
  MailSessionProvider(Map<String, String> mailConfiguration) {
    if (mailConfiguration != null && !mailConfiguration.isEmpty()) {
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
    } else {
      this.session = null;
    }
  }

  @Override
  public Session get() {
    if (session == null) {
      throw new RuntimeException("SMTP is not configured");
    }
    return session;
  }
}
