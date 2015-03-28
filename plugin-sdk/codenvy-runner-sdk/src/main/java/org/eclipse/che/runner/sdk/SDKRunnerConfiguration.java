/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.runner.sdk;

import org.eclipse.che.api.runner.dto.RunRequest;
import org.eclipse.che.api.runner.internal.RunnerConfiguration;

/**
 * Configuration of Codenvy extensions runner.
 *
 * @author Artem Zatsarynnyy
 */
public class SDKRunnerConfiguration extends RunnerConfiguration {
    private final String server;
    /** Specifies the domain name or IP address of the code server. */
    private final String codeServerBindAddress;
    /** Specifies the HTTP port for the code server. */
    private final int    codeServerPort;
    private final int    httpPort;

    private boolean debugSuspend;

    public SDKRunnerConfiguration(String server,
                                  int memory,
                                  int httpPort,
                                  String codeServerBindAddress,
                                  int codeServerPort,
                                  RunRequest runRequest) {
        super(memory, runRequest);
        this.server = server;
        this.httpPort = httpPort;
        this.codeServerBindAddress = codeServerBindAddress;
        this.codeServerPort = codeServerPort;
    }

    public String getServer() {
        return server;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public String getCodeServerBindAddress() {
        return codeServerBindAddress;
    }

    public int getCodeServerPort() {
        return codeServerPort;
    }

    public boolean isDebugSuspend() {
        return debugSuspend;
    }

    public void setDebugSuspend(boolean debugSuspend) {
        this.debugSuspend = debugSuspend;
    }

    @Override
    public String toString() {
        return "SDKRunnerConfiguration{" +
               "memory=" + getMemory() +
               ", codeServerPort=" + codeServerPort +
               ", codeServerBindAddress='" + codeServerBindAddress + '\'' +
               ", links=" + getLinks() +
               ", request=" + getRequest() +
               ", debugHost='" + getDebugHost() + '\'' +
               ", debugPort=" + getDebugPort() +
               ", debugSuspend=" + debugSuspend +
               ", server='" + server + '\'' +
               '}';
    }
}
