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
package org.eclipse.che.api.workspace.server;

import org.eclipse.che.api.workspace.server.spi.RuntimeIdentity;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Alexander Garagatyi
 */
public class NoopUrlRewriter implements URLRewriter {
    @Override
    public URL rewriteURL(RuntimeIdentity identity, String name, URL url) throws MalformedURLException {
        return url;
    }
}
