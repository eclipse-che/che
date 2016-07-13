/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *   SAP           - implementation
 *******************************************************************************/
package org.eclipse.che.git.impl.jgit;

import com.google.common.base.Strings;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

import static java.lang.String.format;

/**
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

    public enum Protocol {
        HTTP, HTTPS;

        public PasswordAuthentication passwordAuthentication = createPasswordAuthentication();

        private PasswordAuthentication createPasswordAuthentication() {
            if (! (Strings.isNullOrEmpty(getProxyUserSystemProperty())
                   || Strings.isNullOrEmpty(getProxyPasswordSystemProperty()))) {
                return new PasswordAuthentication(getProxyUserSystemProperty(),
                                                  getProxyPasswordSystemProperty().toCharArray());
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

        public PasswordAuthentication getPasswordAuthentication() {
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
