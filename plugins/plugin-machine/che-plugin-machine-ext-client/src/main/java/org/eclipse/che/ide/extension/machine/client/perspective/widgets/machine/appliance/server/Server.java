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

import org.eclipse.che.api.machine.shared.dto.ServerDto;

import javax.validation.constraints.NotNull;

/**
 * The class which describes entity which store information of current server.
 *
 * @author Dmitry Shnurenko
 */
public class Server {

    private final String    port;
    private final ServerDto descriptor;

    @Inject
    public Server(@Assisted String port, @Assisted ServerDto descriptor) {
        this.port = port;
        this.descriptor = descriptor;
    }

    @NotNull
    public String getPort() {
        return port;
    }

    @NotNull
    public String getAddress() {
        return descriptor.getAddress();
    }

    @NotNull
    public String getUrl() {
        return descriptor.getUrl();
    }

    @NotNull
    public String getRef() {
        return descriptor.getRef();
    }
}
