/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.ecipse.che.plugin.testing.testng.server;

public class TestNGRunnerTest extends BaseTest {
  //  private EndpointIdConfigurator startEndpointIdConfigurator;
  //  private MethodNameConfigurator startMethodNameConfigurator;
  //  private ParamsConfigurator startParamsConfigurator;
  //  private SendConfiguratorFromOne startSendConfiguratorFromOne;
  //  private JavaTestFinder testNGTestFinder;
  //  private RequestTransmitter transmitter;
  //
  //  private TestNGRunner runner;
  //
  //  @BeforeMethod
  //  public void setUp() throws Exception {
  //    startEndpointIdConfigurator = mock(EndpointIdConfigurator.class);
  //    testNGTestFinder = mock(JavaTestFinder.class);
  //    startMethodNameConfigurator = mock(MethodNameConfigurator.class);
  //    startParamsConfigurator = mock(ParamsConfigurator.class);
  //    startSendConfiguratorFromOne = mock(SendConfiguratorFromOne.class);
  //    transmitter = mock(RequestTransmitter.class);
  //
  //    runner =
  //        new TestNGRunner(
  //            "", testNGTestFinder, new ProjectClasspathProvider(""), new TestNGSuiteUtil());
  //  }
  //
  //  @Test()
  //  public void testName() throws Exception {
  //    String name = "Test";
  //    FolderEntry folder = pm.getProjectsRoot().createFolder(name);
  //    FolderEntry testsFolder = folder.createFolder("src/tests");
  //    StringBuilder b = new StringBuilder("package tests;\n");
  //    b.append("public class TestNGTest {}");
  //    testsFolder.createFile("TestNGTest.java", b.toString().getBytes());
  //    projectRegistry.setProjectType(folder.getPath().toString(), "java", false);
  //
  //    //inform DeltaProcessingStat about new project
  //    JavaModelManager.getJavaModelManager()
  //        .deltaState
  //        .resourceChanged(new ResourceChangedEvent(root, new ProjectCreatedEvent("", "/Test")));
  //
  //    IJavaProject javaProject =
  //        JavaModelManager.getJavaModelManager().getJavaModel().getJavaProject("/Test");
  //    IClasspathEntry testNg =
  //        JavaCore.newLibraryEntry(
  //            new Path(ClasspathUtil.getJarPathForClass(Test.class)), null, null);
  //    IClasspathEntry source =
  //        JavaCore.newSourceEntry(
  //            new Path("/Test/src"),
  //            null,
  //            new Path(ClasspathUtil.getJarPathForClass(TestNGRunnerTest.class)));
  //    IClasspathEntry jre =
  //        JavaCore.newContainerEntry(new Path(JREContainerInitializer.JRE_CONTAINER));
  //
  //    javaProject.setRawClasspath(new IClasspathEntry[] {testNg, source, jre}, null);
  //
  //    DtoServerImpls.TestExecutionContextImpl context = new DtoServerImpls.TestExecutionContextImpl();
  //    context.setDebugModeEnable(false);
  //    context.setContextType(TestExecutionContext.ContextType.FILE);
  //    context.setProjectPath("/Test");
  //    context.setFilePath("/Test/src/tests/TestNGTest.java");
  //    assertEquals("testng", runner.getName());
  //  }
  //
  //  private void prepareTransmitting(RequestTransmitter transmitter) {
  //    when(transmitter.newRequest()).thenReturn(startEndpointIdConfigurator);
  //    when(startEndpointIdConfigurator.endpointId(anyString()))
  //        .thenReturn(startMethodNameConfigurator);
  //    when(startMethodNameConfigurator.methodName(anyString())).thenReturn(startParamsConfigurator);
  //    when(startParamsConfigurator.paramsAsString(anyString()))
  //        .thenReturn(startSendConfiguratorFromOne);
  //  }
}
