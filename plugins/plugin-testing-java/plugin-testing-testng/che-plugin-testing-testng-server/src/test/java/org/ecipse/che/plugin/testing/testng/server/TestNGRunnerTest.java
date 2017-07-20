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
package org.ecipse.che.plugin.testing.testng.server;

import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.core.jsonrpc.commons.transmission.EndpointIdConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.transmission.MethodNameConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.transmission.ParamsConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.transmission.SendConfiguratorFromOne;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.ProjectCreatedEvent;
import org.eclipse.che.api.testing.server.dto.DtoServerImpls;
import org.eclipse.che.api.testing.shared.TestExecutionContext;
import org.eclipse.che.jdt.core.launching.JREContainerInitializer;
import org.eclipse.che.jdt.core.resources.ResourceChangedEvent;
import org.eclipse.che.plugin.java.testing.ClasspathUtil;
import org.eclipse.che.plugin.java.testing.ProjectClasspathProvider;
import org.eclipse.che.plugin.testing.testng.server.TestNGRunner;
import org.eclipse.che.plugin.testing.testng.server.TestNGSuiteUtil;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class TestNGRunnerTest extends BaseTest {
    private EndpointIdConfigurator  startEndpointIdConfigurator;
    private MethodNameConfigurator  startMethodNameConfigurator;
    private ParamsConfigurator      startParamsConfigurator;
    private SendConfiguratorFromOne startSendConfiguratorFromOne;
    private RequestTransmitter      transmitter;

    private TestNGRunner runner;

    @BeforeMethod
    public void setUp() throws Exception {
        startEndpointIdConfigurator = mock(EndpointIdConfigurator.class);
        startMethodNameConfigurator = mock(MethodNameConfigurator.class);
        startParamsConfigurator = mock(ParamsConfigurator.class);
        startSendConfiguratorFromOne = mock(SendConfiguratorFromOne.class);
        transmitter = mock(RequestTransmitter.class);

        runner = new TestNGRunner("", new ProjectClasspathProvider(""), new TestNGSuiteUtil());
    }

    @Test()
    public void testName() throws Exception {
        String name = "Test";
        FolderEntry folder = pm.getProjectsRoot().createFolder(name);
        FolderEntry testsFolder = folder.createFolder("src/tests");
        StringBuilder b = new StringBuilder("package tests;\n");
        b.append("public class TestNGTest {}");
        testsFolder.createFile("TestNGTest.java", b.toString().getBytes());
        projectRegistry.setProjectType(folder.getPath().toString(), "java", false);

        //inform DeltaProcessingStat about new project
        JavaModelManager.getJavaModelManager().deltaState.resourceChanged(
                new ResourceChangedEvent(root, new ProjectCreatedEvent("", "/Test")));

        IJavaProject javaProject = JavaModelManager.getJavaModelManager().getJavaModel().getJavaProject("/Test");
        IClasspathEntry testNg = JavaCore.newLibraryEntry(new Path(ClasspathUtil.getJarPathForClass(Test.class)),
                                                          null,
                                                          null);
        IClasspathEntry source = JavaCore.newSourceEntry(new Path("/Test/src"),
                                                         null,
                                                         new Path(ClasspathUtil.getJarPathForClass(TestNGRunnerTest.class)));
        IClasspathEntry jre = JavaCore.newContainerEntry(new Path(JREContainerInitializer.JRE_CONTAINER));

        javaProject.setRawClasspath(new IClasspathEntry[]{testNg, source, jre}, null);

        DtoServerImpls.TestExecutionContextImpl context = new DtoServerImpls.TestExecutionContextImpl();
        context.setDebugModeEnable(false);
        context.setTestType(TestExecutionContext.TestType.FILE);
        context.setProjectPath("/Test");
        context.setFilePath("/Test/src/tests/TestNGTest.java");
        assertEquals("testng", runner.getName());
    }

    private void prepareTransmitting(RequestTransmitter transmitter) {
        when(transmitter.newRequest()).thenReturn(startEndpointIdConfigurator);
        when(startEndpointIdConfigurator.endpointId(anyString())).thenReturn(startMethodNameConfigurator);
        when(startMethodNameConfigurator.methodName(anyString())).thenReturn(startParamsConfigurator);
        when(startParamsConfigurator.paramsAsString(anyString())).thenReturn(startSendConfiguratorFromOne);
    }
}
