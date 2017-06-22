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
package org.eclipse.che.ide.api.workspace.model;

import org.eclipse.che.api.core.model.workspace.runtime.Server;
import org.eclipse.che.api.core.model.workspace.runtime.ServerStatus;

import static com.google.common.base.Strings.nullToEmpty;

public class ServerImpl implements Server {

    private String       name;
    private String       url;
    private ServerStatus status;

    public ServerImpl(String name, String url) {
        this(name, url, ServerStatus.UNKNOWN);
    }

    public ServerImpl(String name, String url, ServerStatus status) {
        this.name = name;
        this.url = nullToEmpty(url); // some servers doesn't have URL
        this.status = status;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public ServerStatus getStatus() {
        return this.status;
    }
}
