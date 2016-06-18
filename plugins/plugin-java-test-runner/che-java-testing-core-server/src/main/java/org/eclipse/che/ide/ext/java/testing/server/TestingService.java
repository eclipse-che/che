/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * <p>
 * Contributors:
 * Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.java.testing.server;


import org.eclipse.che.ide.ext.java.testing.shared.TestResult;
import org.eclipse.core.resources.ResourcesPlugin;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("java/testing")
public class TestingService {

    @GET
    @Path("runClass")
    @Produces(MediaType.APPLICATION_JSON)
    public TestResult runClass(@QueryParam("projectpath") String projectPath, @QueryParam("fqn") String fqn) throws Exception {
        String absoluteProjectPath = ResourcesPlugin.getPathToWorkspace() + projectPath;
        TestRunnerFactory runnerFactory = new TestRunnerFactory();
        TestRunner testRunner = runnerFactory.getTestRunner(absoluteProjectPath);
        return testRunner.run(fqn);
    }

    @GET
    @Path("runAll")
    @Produces(MediaType.APPLICATION_JSON)
    public TestResult runAll(@QueryParam("projectpath") String projectPath) throws Exception {
        String absoluteProjectPath = ResourcesPlugin.getPathToWorkspace() + projectPath;
        TestRunnerFactory runnerFactory = new TestRunnerFactory();
        TestRunner testRunner = runnerFactory.getTestRunner(absoluteProjectPath);
        return testRunner.runAll();
    }
}
