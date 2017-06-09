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
package org.eclipse.che.api.testing.server;


import static com.jayway.restassured.RestAssured.given;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.eclipse.che.api.project.server.ProjectRegistry;
import org.eclipse.che.api.project.server.RegisteredProject;
import org.eclipse.che.api.testing.server.framework.TestFrameworkRegistry;
import org.eclipse.che.api.testing.server.framework.TestRunner;
import org.eclipse.che.api.testing.shared.TestResult;
import org.eclipse.che.api.vfs.impl.file.LocalVirtualFileSystemProvider;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.assured.EverrestJetty;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.jayway.restassured.response.Response;

/**
 * testing service tests.
 * 
 * @author Mirage Abeysekara
 */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class TestingServiceTest {

    private final String SERVICE_PATH = "/che/testing";
    private final String SERVICE_PATH_RUN_ACTION = SERVICE_PATH + "/run";
    private final String TEST_FRAMEWORK_A = "test-framework-a";
    private final String TEST_FRAMEWORK_B = "test-framework-b";
    private final String PROJECT_TYPE_MAVEN = "maven";

    @Mock
    private TestFrameworkRegistry frameworkRegistry;
    @Mock
    protected LocalVirtualFileSystemProvider vfsProvider;
    @Mock
    protected ProjectRegistry projectRegistry;
    @Mock
    private RegisteredProject registeredProject;
    @Mock
    private TestRunner testRunnerA;
    @Mock
    private TestRunner testRunnerB;

    private DtoFactory dto;
    @SuppressWarnings("unused") // not really...
    private TestingService testingService;

    @BeforeMethod
    public void setUp() throws Exception {
        dto = DtoFactory.getInstance();
        when(registeredProject.getType()).thenReturn(PROJECT_TYPE_MAVEN);
        when(projectRegistry.getProject(anyString())).thenReturn(registeredProject);
        when(frameworkRegistry.getTestRunner(TEST_FRAMEWORK_A)).thenReturn(testRunnerA);
        when(frameworkRegistry.getTestRunner(TEST_FRAMEWORK_B)).thenReturn(testRunnerB);
        testingService = new TestingService(frameworkRegistry);
    }

    // Asking for run a test from known framework.
    @Test
    public void testSuccessfulTestExecution() throws Exception {
        //given
        String query = "projectPath=/sample-project&testFramework=" + TEST_FRAMEWORK_A;
        TestResult result = dto.createDto(TestResult.class);
        result.setSuccess(true);
        TestResult expected = dto.clone(result);
        //when
        when(testRunnerA.execute(anyMapOf(String.class,String.class))).thenReturn(result);
        //then
        Response response = given().when().get(SERVICE_PATH_RUN_ACTION + "/?" + query);
        assertEquals(response.getStatusCode(), 200);
        TestResult responseResult = dto.createDtoFromJson(response.getBody().asInputStream(), TestResult.class);
        assertEquals(responseResult, expected);
    }

    // Asking for run a test from un-known framework.
    @Test
    public void testUnsuccessfulTestExecution() throws Exception {
        //given
        String query = "projectPath=/sample-project&testFramework=unknown";
        //then
        Response response = given().when().get(SERVICE_PATH_RUN_ACTION + "/?" + query);
        assertNotEquals(response.getStatusCode(), 200);
        assertEquals(response.getBody().print(), "No test frameworks found: unknown");
    }
}
