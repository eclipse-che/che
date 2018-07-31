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
package org.eclipse.che.commons.proxy;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 * Set default java.net.Authenticator for requesting through the http(s) proxy. Proxy credentials
 * are read from the system properties: http.proxyUser http.proxyPassword https.proxyUser
 * https.proxyPassword
 *
 * <p>Usage: ProxyAuthenticator.initAuthenticator(url) ... making http(s) request by url ...
 * ProxyAuthenticator.resetAuthenticator()
 *
 * @author Dmytro Nochevnov
 */
public class ProxyAuthenticator extends Authenticator {
  private static ThreadLocal<Protocol> currentProtocolHolder = new ThreadLocal<>();

  static {
    Authenticator.setDefault(new ProxyAuthenticator());
  }

  public static void initAuthenticator(String remoteUrl) {
    if (remoteUrl != null && remoteUrl.toUpperCase().startsWith(Protocol.HTTPS.toString())) {
      currentProtocolHolder.set(Protocol.HTTPS);
    } else {
      currentProtocolHolder.set(Protocol.HTTP);
    }
  }

  public static void resetAuthenticator() {
    currentProtocolHolder.remove();
  }

  private enum Protocol {
    HTTP,
    HTTPS;

    private PasswordAuthentication passwordAuthentication = createPasswordAuthentication();

    private PasswordAuthentication createPasswordAuthentication() {
      if (!(isNullOrEmpty(getProxyUserSystemProperty())
          || isNullOrEmpty(getProxyPasswordSystemProperty()))) {
        return new PasswordAuthentication(
            getProxyUserSystemProperty(), getProxyPasswordSystemProperty().toCharArray());
      } else {
        return null;
      }
    }

    private String getProxyUserSystemProperty() {
      String propertyName = format("%s.proxyUser", this.toString().toLowerCase());
      return System.getProperty(propertyName);
    }

    private String getProxyPasswordSystemProperty() {
      String propertyName = format("%s.proxyPassword", this.toString().toLowerCase());
      return System.getProperty(propertyName);
    }

    private PasswordAuthentication getPasswordAuthentication() {
      return passwordAuthentication;
    }
  }

  protected PasswordAuthentication getPasswordAuthentication() {
    Protocol protocol = currentProtocolHolder.get();
    if (protocol != null) {
      return protocol.getPasswordAuthentication();
    } else {
      return null;
    }
  }
}
