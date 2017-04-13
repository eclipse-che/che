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

import com.google.inject.ImplementedBy;

import org.eclipse.che.api.workspace.server.spi.RuntimeIdentity;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * System specific strategy for rewriting URLs to use in rewriting Servers, Hyperlincs, etc
 * For example in a case when machines supposed to be accessible via reverse Proxy
 *
 * @author gazarenkov
 */
@ImplementedBy(NoopUrlRewriter.class)
public interface URLRewriter {

    /**
     * Rewrites URL according to Strategy rules. May depend on RuntimeIdentity(workspace, owner,..) and name (some id)
     * of this particular URL
     * @param identity RuntimeIdentity (may be null)
     * @param name symbolic name of the server (may be null)
     * @param url URL to rewrite
     * @return Result
     * @throws MalformedURLException if String -> URL conversion failed
     */
    URL rewriteURL(RuntimeIdentity identity, String name, URL url) throws MalformedURLException;

}
