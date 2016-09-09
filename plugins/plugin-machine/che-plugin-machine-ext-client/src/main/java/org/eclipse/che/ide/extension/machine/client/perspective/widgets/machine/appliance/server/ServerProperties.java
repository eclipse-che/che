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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.appliance.server;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.machine.shared.dto.ServerPropertiesDto;

import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * ServeProperties class stores non mandatory properties of the current server.
 *
 * @author Mario Loriedo
 */
public class ServerProperties implements org.eclipse.che.api.core.model.machine.ServerProperties {

    private final ServerPropertiesDto descriptor;

    @Inject
    public ServerProperties(@Assisted ServerPropertiesDto descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public String getPath() {
        return descriptor.getPath();
    }

    @Override
    public String getInternalAddress() { return descriptor.getInternalAddress(); }

    @Override
    public String getInternalUrl() {
        return descriptor.getInternalUrl();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerProperties)) return false;
        final ServerProperties other = (ServerProperties) o;
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
