/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.traefik;

import org.aopalliance.intercept.MethodInvocation;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.infrastructure.docker.client.json.ContainerConfig;
import org.eclipse.che.infrastructure.docker.client.json.ExposedPort;
import org.eclipse.che.infrastructure.docker.client.json.ImageConfig;
import org.eclipse.che.infrastructure.docker.client.json.ImageInfo;
import org.eclipse.che.infrastructure.docker.client.params.CreateContainerParams;
import org.eclipse.che.infrastructure.docker.client.params.InspectImageParams;
import org.eclipse.che.plugin.docker.machine.CustomServerEvaluationStrategy;
import org.eclipse.che.plugin.docker.machine.DefaultServerEvaluationStrategy;
import org.eclipse.che.plugin.docker.machine.ServerEvaluationStrategyProvider;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test for {@Link CreateContainerInterceptor}
 *
 * @author Florent Benoit
 */
// FIXME: spi
@Listeners(MockitoTestNGListener.class)
public class TraefikCreateContainerInterceptorTest {

    private static String TEMPLATE = "<serverName>.<machineName>.<workspaceId>.<wildcardNipDomain>:<chePort>";

    @Mock
    private MethodInvocation methodInvocation;

    @Mock
    private DockerConnector dockerConnector;

    @Mock
    private CreateContainerParams createContainerParams;

    @Mock
    private ContainerConfig containerConfig;

    @Mock
    private ImageConfig imageInfoConfig;

    @Mock
    private ImageInfo imageInfo;

    private String[] envContainerConfig;

    private String[] envImageConfig;


    @InjectMocks
    private TraefikCreateContainerInterceptor traefikCreateContainerInterceptor;

    @Mock
    private ServerEvaluationStrategyProvider serverEvaluationStrategyProvider;

    private CustomServerEvaluationStrategy customServerEvaluationStrategy;


    private Map<String, Map<String, String>> containerExposedPorts;
    private Map<String, ExposedPort>         imageExposedPorts;
    private Map<String, String>              containerLabels;
    private Map<String, String>              imageLabels;


//    @BeforeMethod
//    protected void setup() throws Exception {
//
//        this.customServerEvaluationStrategy = new CustomServerEvaluationStrategy("10.0.0.1", "127.0.0.1", TEMPLATE, "http", "8080");
//        when(serverEvaluationStrategyProvider.get()).thenReturn(customServerEvaluationStrategy);
//        traefikCreateContainerInterceptor.setServerEvaluationStrategyProvider(serverEvaluationStrategyProvider);
//        traefikCreateContainerInterceptor.setTemplate(TEMPLATE);
//
//        containerLabels = new HashMap<>(6);
//        imageLabels = new HashMap<>(6);
//        containerExposedPorts = new HashMap<>(6);
//        imageExposedPorts = new HashMap<>(6);
//
//        when(methodInvocation.getThis()).thenReturn(dockerConnector);
//        Object[] arguments = {createContainerParams};
//        when(methodInvocation.getArguments()).thenReturn(arguments);
//        when(createContainerParams.getContainerConfig()).thenReturn(containerConfig);
//        when(containerConfig.getImage()).thenReturn("IMAGE");
//
//        when(dockerConnector.inspectImage(any(InspectImageParams.class))).thenReturn(imageInfo);
//
//        when(containerConfig.getLabels()).thenReturn(containerLabels);
//        when(imageInfo.getConfig()).thenReturn(imageInfoConfig);
//        when(imageInfoConfig.getLabels()).thenReturn(imageLabels);
//
//
//        when(containerConfig.getExposedPorts()).thenReturn(containerExposedPorts);
//        when(imageInfoConfig.getExposedPorts()).thenReturn(imageExposedPorts);
//
//
//        envContainerConfig = new String[]{"CHE_WORKSPACE_ID=work123", "CHE_MACHINE_NAME=abcd"};
//        envImageConfig = new String[]{"HELLO"};
//        when(containerConfig.getEnv()).thenReturn(envContainerConfig);
//        when(imageInfoConfig.getEnv()).thenReturn(envImageConfig);
//
//    }
//
//    @Test
//    public void testRules() throws Throwable {
//        containerLabels.put("foo1", "bar");
//        containerLabels.put("foo1/dummy", "bar");
//        containerLabels.put("che:server:4401/tcp:protocol", "http");
//        containerLabels.put("che:server:4401/tcp:ref", "wsagent");
//        containerLabels.put("che:server:22/tcp:protocol", "ssh");
//        containerLabels.put("che:server:22/tcp:ref", "ssh");
//        containerLabels.put("che:server:22/tcp:path", "/api");
//        containerLabels.put("che:server:4411/tcp:ref", "terminal");
//        containerLabels.put("che:server:4411/tcp:protocol", "http");
//
//        imageLabels.put("che:server:8080:protocol", "http");
//        imageLabels.put("che:server:8080:ref", "tomcat8");
//        imageLabels.put("che:server:8000:protocol", "http");
//        imageLabels.put("che:server:8000:ref", "tomcat8-debug");
//        imageLabels.put("anotherfoo1", "bar2");
//        imageLabels.put("anotherfoo1/dummy", "bar2");
//
//        containerExposedPorts.put("22/tcp", Collections.emptyMap());
//        containerExposedPorts.put("4401/tcp", Collections.emptyMap());
//        containerExposedPorts.put("4411/tcp", Collections.emptyMap());
//
//        imageExposedPorts.put("7000/tcp", new ExposedPort());
//        imageExposedPorts.put("8080/tcp", new ExposedPort());
//        imageExposedPorts.put("8000/tcp", new ExposedPort());
//
//        traefikCreateContainerInterceptor.invoke(methodInvocation);
//
//
//        Assert.assertTrue(containerLabels.containsKey("traefik.service-wsagent.port"));
//        Assert.assertEquals(containerLabels.get("traefik.service-wsagent.port"), "4401");
//
//        Assert.assertTrue(containerLabels.containsKey("traefik.service-wsagent.frontend.entryPoints"));
//        Assert.assertEquals(containerLabels.get("traefik.service-wsagent.frontend.entryPoints"), "http");
//
//        Assert.assertTrue(containerLabels.containsKey("traefik.service-wsagent.frontend.rule"));
//        Assert.assertEquals(containerLabels.get("traefik.service-wsagent.frontend.rule"), "Host:wsagent.abcd.work123.127.0.0.1.nip.io");
//
//        Assert.assertTrue(containerLabels.containsKey("traefik.service-tomcat8.frontend.rule"));
//        Assert.assertEquals(containerLabels.get("traefik.service-tomcat8.frontend.rule"), "Host:tomcat8.abcd.work123.127.0.0.1.nip.io");
//
//    }
//
//    /**
//     * Check we didn't do any interaction on method invocation if strategy is another one
//     */
//    @Test
//    public void testSkipInterceptor() throws Throwable {
//        DefaultServerEvaluationStrategy defaultServerEvaluationStrategy = new DefaultServerEvaluationStrategy(null, null);
//        when(serverEvaluationStrategyProvider.get()).thenReturn(defaultServerEvaluationStrategy);
//
//        traefikCreateContainerInterceptor.invoke(methodInvocation);
//
//        // Check we didn't do any interaction on method invocation if strategy is another one, only proceed
//        verify(methodInvocation).proceed();
//        verify(methodInvocation, never()).getThis();
//
//    }
}


