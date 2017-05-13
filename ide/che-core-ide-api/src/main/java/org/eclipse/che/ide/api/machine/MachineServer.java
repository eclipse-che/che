/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.api.machine;

import org.eclipse.che.api.core.model.workspace.runtime.Server;
import org.eclipse.che.api.core.model.workspace.runtime.ServerStatus;

import java.util.Objects;

/**
 * Describe development machine server instance.
 *
 * @author Vitalii Parfonov
 * @link Server
 */
public class MachineServer implements Server {

    private final String ref;
    private final String url;

    public MachineServer(String name, Server dto) {
        ref = name;
        url = dto.getUrl();
    }

    public String getRef() {
        return ref;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public ServerStatus getStatus() {
        return ServerStatus.UNKNOWN;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MachineServer)) return false;
        final MachineServer other = (MachineServer)o;
        return Objects.equals(ref, other.ref) &&
               Objects.equals(url, other.url);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = hash * 31 + Objects.hashCode(ref);
        hash = hash * 31 + Objects.hashCode(url);
        return hash;
    }

    @Override
    public String toString() {
        return "MachineServer{" +
               "ref='" + ref + '\'' +
               ", url='" + url + '\'' +
               '}';
    }
}
