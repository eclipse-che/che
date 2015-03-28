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

import org.eclipse.che.api.runner.RunnerException;
import org.eclipse.che.api.runner.internal.ApplicationProcess;

import java.io.File;
import java.util.zip.ZipFile;

/**
 * Application server to deploy a web app.
 *
 * @author Artem Zatsarynnyy
 */
public interface ApplicationServer {

    /** Application server name. */
    String getName();

    String getDescription();

    /**
     * Deploy WAR to application server.
     *
     * @param workDir
     *         root directory for this application server
     * @param warToDeploy
     *         WAR file to deploy
     * @param extensionJar
     *         JAR with extension
     * @param runnerConfiguration
     *         runner configuration
     * @param codeServerProcess
     *         may be <code>null</code> if no need to run GWT Code Server
     * @param callback
     *         Callback
     * @return {@code ApplicationProcess} that represents a deployed app
     * @throws org.eclipse.che.api.runner.RunnerException
     *         if an error occurs when try to deploy app to application server
     */
    ApplicationProcess deploy(java.io.File workDir,
                              ZipFile warToDeploy,
                              File extensionJar,
                              SDKRunnerConfiguration runnerConfiguration,
                              CodeServer.CodeServerProcess codeServerProcess,
                              ApplicationProcess.Callback callback) throws RunnerException;
}
