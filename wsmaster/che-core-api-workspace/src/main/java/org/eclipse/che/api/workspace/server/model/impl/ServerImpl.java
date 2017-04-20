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
package org.eclipse.che.api.workspace.server.model.impl;

import org.eclipse.che.api.core.model.workspace.runtime.Server;

/**
 * @author gazarenkov
 */
public class ServerImpl implements Server {

    private String url;

    public ServerImpl(String url) {
        this.url = url;
    }

    @Override
    public String getUrl() {
        return url;
    }
}
