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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.appliance.server;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.core.model.machine.ServerProperties;

import java.util.Objects;

/**
 * ServeProperties class stores non mandatory properties of the current server.
 *
 * @author Mario Loriedo
 */
public class ServerPropertiesImpl implements ServerProperties {

    private final ServerProperties descriptor;

    @Inject
    public ServerPropertiesImpl(@Assisted ServerProperties descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public String getPath() {
        return descriptor.getPath();
    }

    @Override
    public String getInternalAddress() {
        return descriptor.getInternalAddress();
    }

    @Override
    public String getInternalUrl() {
        return descriptor.getInternalUrl();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerPropertiesImpl)) return false;
        final ServerPropertiesImpl other = (ServerPropertiesImpl)o;
        return Objects.equals(descriptor, other.descriptor);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(descriptor.getPath());
    }

    @Override
    public String toString() {
        return "ServerProperties{" +
               "descriptor=" + descriptor +
               '}';
    }
}
