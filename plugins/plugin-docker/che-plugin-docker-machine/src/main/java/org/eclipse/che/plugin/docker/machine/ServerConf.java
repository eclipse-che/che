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
package org.eclipse.che.plugin.docker.machine;

import com.google.common.base.Objects;

/**
 * Describes configuration of the Che server in machine
 *
 * @author Alexander Garagatyi
 */
public class ServerConf {
    private String ref;
    private String port;
    private String protocol;

    public ServerConf() {
    }

    public ServerConf(String ref, String port, String protocol) {
        this.ref = ref;
        this.port = port;
        this.protocol = protocol;
    }

    public String getRef() {
        return ref;
    }

    public ServerConf setRef(String ref) {
        this.ref = ref;
        return this;
    }

    public String getPort() {
        return port;
    }

    public ServerConf setPort(String port) {
        this.port = port;
        return this;
    }

    public String getProtocol() {
        return protocol;
    }

    public ServerConf setProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerConf)) return false;
        ServerConf that = (ServerConf)o;
        return Objects.equal(port, that.port) &&
               Objects.equal(ref, that.ref) &&
               Objects.equal(protocol, that.protocol);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(ref, port, protocol);
    }
}
