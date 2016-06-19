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
package org.eclipse.che.ide.ext.java.testing.core.server;


import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.ide.ext.java.testing.core.server.classpath.TestClasspathRegistry;
import org.eclipse.che.ide.ext.java.testing.core.server.framework.TestFrameworkRegistry;
import org.eclipse.che.ide.ext.java.testing.shared.TestResult;
import org.eclipse.core.resources.ResourcesPlugin;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("java/testing")
public class TestingService {

    private final ProjectManager projectManager;
    private final TestFrameworkRegistry frameworkRegistry;
    private final TestClasspathRegistry classpathRegistry;

    @Inject
    public TestingService(ProjectManager projectManager, TestFrameworkRegistry frameworkRegistry,
                          TestClasspathRegistry classpathRegistry) {
        this.projectManager = projectManager;
        this.frameworkRegistry = frameworkRegistry;
        this.classpathRegistry = classpathRegistry;
        System.out.println("inititilaized TestingService");
    }

//    @GET
//    @Path("runClass")
//    @Produces(MediaType.APPLICATION_JSON)
//    public TestResult runClass(@QueryParam("projectpath") String projectPath, @QueryParam("fqn") String fqn) throws Exception {
//        String absoluteProjectPath = ResourcesPlugin.getPathToWorkspace() + projectPath;
//        TestRunnerFactory runnerFactory = new TestRunnerFactory();
//        TestRunner testRunner = runnerFactory.getTestRunner(absoluteProjectPath);
//        return testRunner.run(fqn);
//    }
//
//    @GET
//    @Path("runAll")
//    @Produces(MediaType.APPLICATION_JSON)
//    public TestResult runAll(@QueryParam("projectpath") String projectPath) throws Exception {
//        String absoluteProjectPath = ResourcesPlugin.getPathToWorkspace() + projectPath;
//        TestRunnerFactory runnerFactory = new TestRunnerFactory();
//        TestRunner testRunner = runnerFactory.getTestRunner(absoluteProjectPath);
//        return testRunner.runAll();
//    }

    @GET
    @Path("run")
    @Produces(MediaType.APPLICATION_JSON)
    public TestResult run(@Context UriInfo uriInfo) throws Exception {
        Map<String, String> queryParameters = getMap(uriInfo.getQueryParameters());
        String projectPath = queryParameters.get("projectPath");
        String absoluteProjectPath = ResourcesPlugin.getPathToWorkspace() + projectPath;
        queryParameters.put("absoluteProjectPath", absoluteProjectPath);
        System.out.println(projectManager);
        System.out.println(absoluteProjectPath);
        System.out.println(projectPath);

        String projectType = "";
        if (projectManager != null) {
            projectType = projectManager.getProject(projectPath).getType();
            System.out.println(projectType);
        }
        String testFramework = queryParameters.get("testFramework");
        System.out.println(queryParameters.toString());
        System.out.println(frameworkRegistry.getTestRunner(testFramework));
        TestResult result = frameworkRegistry.getTestRunner(testFramework).execute(queryParameters,
                classpathRegistry.getTestClasspathProvider(projectType));
        System.out.println(result);
        return result;
    }

    private Map<String, String> getMap(MultivaluedMap<String, String> queryParameters) {
        Map<String, String> map = new HashMap<>();
        if (queryParameters == null) {
            return map;
        }
        for (Map.Entry<String, List<String>> entry : queryParameters.entrySet()) {
            StringBuilder sb = new StringBuilder();
            for (String s : entry.getValue()) {
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append(s);
            }
            map.put(entry.getKey(), sb.toString());
        }
        return map;
    }
}
