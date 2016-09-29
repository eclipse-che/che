/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.api.machine;

import org.eclipse.che.api.core.model.machine.Server;
import org.eclipse.che.api.core.model.machine.ServerProperties;
import org.eclipse.che.api.machine.shared.dto.ServerDto;

import java.util.Objects;

/**
 * Describe development machine server instance.
 *
 * @link Server
 * @author Vitalii Parfonov
 */
public class DevMachineServer implements Server {


    private final String address;
    private final String protocol;
    private final String ref;
    private final String url;
    private final ServerProperties properties;

        public DevMachineServer(Server dto) {
            address = dto.getAddress();
            protocol = dto.getProtocol();
            ref = dto.getRef();
            url = dto.getUrl();
            properties = dto.getProperties();
        }


        @Override
        public String getRef() {
            return ref;
        }

        @Override
        public String getAddress() {
            return address;
        }

        @Override
        public String getProtocol() {
            return protocol;
        }

        @Override
        public String getUrl() {
            return url;
        }

        @Override
        public ServerProperties getProperties() { return properties; };

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DevMachineServer)) return false;
            final DevMachineServer other = (DevMachineServer) o;
            return Objects.equals(ref, other.ref) &&
                           Objects.equals(protocol, other.protocol) &&
                           Objects.equals(address, other.address) &&
                           Objects.equals(url, other.url) &&
                           Objects.equals(properties, other.properties);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = hash * 31 + Objects.hashCode(ref);
            hash = hash * 31 + Objects.hashCode(protocol);
            hash = hash * 31 + Objects.hashCode(address);
            hash = hash * 31 + Objects.hashCode(url);
            hash = hash * 31 + Objects.hashCode(properties);
            return hash;
        }

        @Override
        public String toString() {
            return "DevMachineServer{" +
                           "ref='" + ref + '\'' +
                           ", protocol='" + protocol + '\'' +
                           ", address='" + address + '\'' +
                           ", url='" + url + '\'' +
                           ", properties='" + properties + '\'' +
                           '}';
        }

    }
