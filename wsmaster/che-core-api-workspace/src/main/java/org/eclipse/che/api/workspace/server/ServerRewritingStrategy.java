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

import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.api.core.model.workspace.runtime.Server;
import org.eclipse.che.api.machine.server.model.impl.ServerImpl;
import org.eclipse.che.api.workspace.server.spi.RuntimeIdentity;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A strategy for rewriting Server URLs
 * @author gazarenkov
 */
public abstract class ServerRewritingStrategy {

    public final Result rewrite(RuntimeIdentity identity, Map<String, ? extends Server> incoming) {

        Map <String, Server> outgoing = new HashMap<>();
        List<Warning> warnings = new ArrayList<>();

        for(Map.Entry<String, ? extends Server> entry : incoming.entrySet()) {
            String name = entry.getKey();
            String strUrl = entry.getValue().getUrl();
            try {
                URL url = new URL(strUrl);
                ServerImpl server = new ServerImpl(rewriteURL(identity, name, url).toString());
                outgoing.put(name, server);
            } catch (MalformedURLException e) {
                warnings.add(new Warning() {
                    @Override
                    public int getCode() {
                        return 101;
                    }

                    @Override
                    public String getMessage() {
                        return "Malformed URL for " + name + " : " + e.getMessage();
                    }
                });
            }

        }

        return new Result(outgoing, warnings);

    }

    protected abstract URL rewriteURL(RuntimeIdentity identity, String name, URL url) throws MalformedURLException;

    public static class Result {

        private final Map <String, Server> servers;
        private final List<Warning> warnings;

        protected Result(Map<String, Server> servers, List<Warning> warnings) {
            this.servers = servers;
            this.warnings = warnings;
        }

        public Map<String, Server> getServers() {
            return servers;
        }

        public List<Warning> getWarnings() {
            return warnings;
        }
    }


}
