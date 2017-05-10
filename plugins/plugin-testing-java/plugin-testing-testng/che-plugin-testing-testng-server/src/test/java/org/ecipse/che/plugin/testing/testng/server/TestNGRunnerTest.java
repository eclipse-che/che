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

import org.eclipse.che.api.core.jsonrpc.RequestTransmitter;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.ProjectCreatedEvent;
import org.eclipse.che.api.testing.server.dto.DtoServerImpls;
import org.eclipse.che.api.testing.server.framework.TestMessagesOutputTransmitter;
import org.eclipse.che.api.testing.shared.Constants;
import org.eclipse.che.api.testing.shared.TestExecutionContext;
import org.eclipse.che.commons.lang.execution.ProcessHandler;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;

/**
 *
 */
public class TestNGRunnerTest extends BaseTest{


    private TestNGRunner runner;

    @BeforeMethod
    public void setUp() throws Exception {
        runner = new TestNGRunner(new ProjectClasspathProvider(""), new TestNGSuiteUtil());

    }

    @Test
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
        IClasspathEntry testNg = JavaCore.newLibraryEntry(new Path(ClasspathUtil.getJarPathForClass(Test.class)), null, null);
        IClasspathEntry source = JavaCore.newSourceEntry(new Path("/Test/src"), null, new Path(ClasspathUtil.getJarPathForClass(TestNGRunnerTest.class)));
        IClasspathEntry jre = JavaCore.newContainerEntry(new Path(JREContainerInitializer.JRE_CONTAINER));

        javaProject.setRawClasspath(new IClasspathEntry[]{testNg, source, jre}, null);

        DtoServerImpls.TestExecutionContextImpl context = new DtoServerImpls.TestExecutionContextImpl();
        context.setTestType(TestExecutionContext.TestType.FILE);
        context.setProjectPath("/Test");
        context.setFilePath("/Test/src/tests/TestNGTest.java");
        ProcessHandler processHandler = runner.execute(context);

        RequestTransmitter transmitter = Mockito.mock(RequestTransmitter.class);
        TestMessagesOutputTransmitter outputTransmitter = new TestMessagesOutputTransmitter(processHandler, transmitter, "test");
        Thread.sleep(2000);
        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(transmitter,  times(25)).transmitStringToNone(eq("test"), eq(Constants.TESTING_RPC_METHOD_NAME), jsonCaptor.capture());

        jsonCaptor.getAllValues().forEach(System.out::println);

    }
}
