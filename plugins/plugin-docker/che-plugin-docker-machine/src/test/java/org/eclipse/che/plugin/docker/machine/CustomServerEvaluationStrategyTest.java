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
package org.eclipse.che.plugin.docker.machine;

import org.eclipse.che.api.machine.server.model.impl.ServerConfImpl;
import org.eclipse.che.api.machine.server.model.impl.ServerImpl;
import org.eclipse.che.plugin.docker.client.json.ContainerConfig;
import org.eclipse.che.plugin.docker.client.json.ContainerInfo;
import org.eclipse.che.plugin.docker.client.json.NetworkSettings;
import org.eclipse.che.plugin.docker.client.json.PortBinding;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.when;

/**
 * Test for {@Link CustomServerEvaluationStrategy}
 *
 * @author Florent Benoit
 */
@Listeners(MockitoTestNGListener.class)
public class CustomServerEvaluationStrategyTest {

    private static final String ALL_IP_ADDRESS = "0.0.0.0";

    private static final String WORKSPACE_ID_VALUE    = "work123";
    private static final String WORKSPACE_ID_PROPERTY = "CHE_WORKSPACE_ID=" + WORKSPACE_ID_VALUE;

    private static final String MACHINE_NAME_VALUE    = "myMachine";
    private static final String MACHINE_NAME_PROPERTY = "CHE_MACHINE_NAME=" + MACHINE_NAME_VALUE;

    @Mock
    private ContainerConfig containerConfig;

    @Mock
    private ContainerInfo containerInfo;

    @Mock
    private NetworkSettings networkSettings;


    private CustomServerEvaluationStrategy customServerEvaluationStrategy;

    private Map<String, Map<String, String>> containerExposedPorts;
    private Map<String, String>              containerLabels;
    private String[]                         envContainerConfig;
    private Map<String, List<PortBinding>>   networkExposedPorts;


    @BeforeMethod
    protected void setup() throws Exception {
        containerLabels = new HashMap<>();
        containerExposedPorts = new HashMap<>();
        networkExposedPorts = new HashMap<>();
        when(containerInfo.getConfig()).thenReturn(containerConfig);
        when(containerConfig.getLabels()).thenReturn(containerLabels);
        when(containerConfig.getExposedPorts()).thenReturn(containerExposedPorts);

        envContainerConfig = new String[]{WORKSPACE_ID_PROPERTY, MACHINE_NAME_PROPERTY};
        when(containerConfig.getEnv()).thenReturn(envContainerConfig);

        when(containerInfo.getNetworkSettings()).thenReturn(networkSettings);
        when(networkSettings.getGateway()).thenReturn("127.0.0.1");
        when(networkSettings.getPorts()).thenReturn(networkExposedPorts);

        containerLabels.put("foo1", "bar");
        containerLabels.put("foo1/dummy", "bar");
        containerLabels.put("che:server:4401/tcp:protocol", "http");
        containerLabels.put("che:server:4401/tcp:ref", "wsagent");
        containerLabels.put("che:server:22/tcp:protocol", "ssh");
        containerLabels.put("che:server:22/tcp:ref", "ssh");
        containerLabels.put("che:server:22/tcp:path", "/api");
        containerLabels.put("che:server:4411/tcp:ref", "terminal");
        containerLabels.put("che:server:4411/tcp:protocol", "http");
        containerLabels.put("che:server:8080:protocol", "http");
        containerLabels.put("che:server:8080:ref", "tomcat8");
        containerLabels.put("anotherfoo1", "bar2");
        containerLabels.put("anotherfoo1/dummy", "bar2");

        containerExposedPorts.put("22/tcp", Collections.emptyMap());
        networkExposedPorts.put("22/tcp", Collections.singletonList(new PortBinding().withHostIp(ALL_IP_ADDRESS)
                                                                                     .withHostPort("3222")));
        containerExposedPorts.put("4401/tcp", Collections.emptyMap());
        networkExposedPorts.put("4401/tcp", Collections.singletonList(new PortBinding().withHostIp(ALL_IP_ADDRESS)
                                                                                       .withHostPort("324401")));
        containerExposedPorts.put("4411/tcp", Collections.emptyMap());
        networkExposedPorts.put("4411/tcp", Collections.singletonList(new PortBinding().withHostIp(ALL_IP_ADDRESS)
                                                                                       .withHostPort("324411")));
        containerExposedPorts.put("8080/tcp", Collections.emptyMap());
        networkExposedPorts.put("8080/tcp", Collections.singletonList(new PortBinding().withHostIp(ALL_IP_ADDRESS)
                                                                                       .withHostPort("328080")));
    }


    /**
     * Check workspace Id template
     */
    @Test
    public void testWorkspaceIdRule() throws Throwable {
        this.customServerEvaluationStrategy =
                new CustomServerEvaluationStrategy("10.0.0.1", "192.168.1.1", "<workspaceId>", "http", "8080");

        Map<String, String> portMapping = this.customServerEvaluationStrategy.getExternalAddressesAndPorts(containerInfo, "localhost");

        Assert.assertTrue(portMapping.containsKey("4401/tcp"));
        Assert.assertEquals(portMapping.get("4401/tcp"), WORKSPACE_ID_VALUE);
    }


    /**
     * Check workspace Id template
     */
    @Test
    public void testMachineNameRule() throws Throwable {
        this.customServerEvaluationStrategy =
                new CustomServerEvaluationStrategy("10.0.0.1", "192.168.1.1", "<machineName>", "http", "8080");

        Map<String, String> portMapping = this.customServerEvaluationStrategy.getExternalAddressesAndPorts(containerInfo, "localhost");

        Assert.assertTrue(portMapping.containsKey("4401/tcp"));
        Assert.assertEquals(portMapping.get("4401/tcp"), MACHINE_NAME_VALUE);
    }

    /**
     * Check updates of protocols
     */
    @Test
    public void testProtocolUpdate() throws Throwable {
        HashMap<String, ServerConfImpl> serverConfs = new HashMap<>();
        this.customServerEvaluationStrategy =
                new CustomServerEvaluationStrategy("10.0.0.1", "192.168.1.1", "<workspaceId>", "https", "8080");
        Map<String, ServerImpl> portMapping = this.customServerEvaluationStrategy.getServers(containerInfo, "localhost", serverConfs);

        Assert.assertTrue(portMapping.containsKey("4401/tcp"));
        ServerImpl server = portMapping.get("4401/tcp");
        Assert.assertNotNull(server);
        Assert.assertEquals(server.getUrl(), "https://" + WORKSPACE_ID_VALUE);
        Assert.assertEquals(server.getProtocol(), "https");
        Assert.assertEquals(server.getAddress(), WORKSPACE_ID_VALUE);
        Assert.assertEquals(server.getRef(), "wsagent");
    }

    /**
     * Check defaults values
     */
    @Test
    public void testDefaults() throws Throwable {
        HashMap<String, ServerConfImpl> serverConfs = new HashMap<>();

        this.customServerEvaluationStrategy =
                new CustomServerEvaluationStrategy("127.0.0.1", null, "<externalAddress>-<workspaceId>", "https", "8080");

        Map<String, ServerImpl> portMapping = this.customServerEvaluationStrategy.getServers(containerInfo, "localhost", serverConfs);

        Assert.assertTrue(portMapping.containsKey("4401/tcp"));
        ServerImpl server = portMapping.get("4401/tcp");
        Assert.assertNotNull(server);
        Assert.assertEquals(server.getUrl(), "https://127.0.0.1-" + WORKSPACE_ID_VALUE);
        Assert.assertEquals(server.getProtocol(), "https");
        Assert.assertEquals(server.getAddress(), "127.0.0.1-" + WORKSPACE_ID_VALUE);
        Assert.assertEquals(server.getRef(), "wsagent");
        Assert.assertEquals(server.getRef(), "wsagent");
    }

    /**
     * Check defaults values
     */
    @Test
    public void testDefaultsNoGateway() throws Throwable {
        when(networkSettings.getGateway()).thenReturn(null);
        HashMap<String, ServerConfImpl> serverConfs = new HashMap<>();
        this.customServerEvaluationStrategy =
                new CustomServerEvaluationStrategy("127.0.0.1", null, "<externalAddress>-<workspaceId>", "https", "8080");
        Map<String, ServerImpl> portMapping = this.customServerEvaluationStrategy.getServers(containerInfo, "127.0.0.1", serverConfs);
        Assert.assertTrue(portMapping.containsKey("4401/tcp"));
        ServerImpl server = portMapping.get("4401/tcp");
        Assert.assertNotNull(server);
        Assert.assertEquals(server.getUrl(), "https://127.0.0.1-" + WORKSPACE_ID_VALUE);
        Assert.assertEquals(server.getProtocol(), "https");
        Assert.assertEquals(server.getAddress(), "127.0.0.1-" + WORKSPACE_ID_VALUE);
        Assert.assertEquals(server.getRef(), "wsagent");
        Assert.assertEquals(server.getRef(), "wsagent");
    }


    /**
     * Check offline mode
     */
    @Test
    public void testOffline() throws Throwable {
        Set<String> exposedPorts = new HashSet<>();
        exposedPorts.add("22/tcp");
        exposedPorts.add("4401/tcp");
        exposedPorts.add("4411/tcp");
        exposedPorts.add("8080/tcp");
        List<String> env = Arrays.asList(WORKSPACE_ID_PROPERTY, MACHINE_NAME_PROPERTY);
        this.customServerEvaluationStrategy =
                new CustomServerEvaluationStrategy("127.0.0.1", null, "<externalAddress>-<workspaceId>", "https", "8080");
        CustomServerEvaluationStrategy.RenderingEvaluation renderingEvaluation = this.customServerEvaluationStrategy
                .getOfflineRenderingEvaluation(containerLabels, exposedPorts, env.stream().toArray(String[]::new));
        String eval = renderingEvaluation.render("<workspaceId>", "4401/tcp");
        Assert.assertEquals(eval, WORKSPACE_ID_VALUE);
    }


    /**
     * Check offline mode with external ip
     */
    @Test
    public void testOfflineExternal() throws Throwable {
        Set<String> exposedPorts = new HashSet<>();
        exposedPorts.add("22/tcp");
        exposedPorts.add("4401/tcp");
        exposedPorts.add("4411/tcp");
        exposedPorts.add("8080/tcp");
        List<String> env = Arrays.asList(WORKSPACE_ID_PROPERTY, MACHINE_NAME_PROPERTY);
        this.customServerEvaluationStrategy =
                new CustomServerEvaluationStrategy("127.0.0.1", "127.0.0.1", "<externalAddress>-<workspaceId>", "https", "8080");
        CustomServerEvaluationStrategy.RenderingEvaluation renderingEvaluation = this.customServerEvaluationStrategy
                .getOfflineRenderingEvaluation(containerLabels, exposedPorts, env.stream().toArray(String[]::new));
        String eval = renderingEvaluation.render("<workspaceId>", "4401/tcp");
        Assert.assertEquals(eval, WORKSPACE_ID_VALUE);
    }


    /**
     * Check offline mode with external ip
     */
    //@Test(expectedExceptions = UnsupportedOperationException.class, expectedExceptionsMessageRegExp = "Unable to find the IP for the address .*")
    public void testOfflineInvalidExternal() throws Throwable {
        Set<String> exposedPorts = new HashSet<>();
        exposedPorts.add("22/tcp");
        exposedPorts.add("4401/tcp");
        exposedPorts.add("4411/tcp");
        exposedPorts.add("8080/tcp");
        List<String> env = Arrays.asList(WORKSPACE_ID_PROPERTY, MACHINE_NAME_PROPERTY);
        this.customServerEvaluationStrategy =
                new CustomServerEvaluationStrategy("127.0.0.1", "300.300.300.300", "<externalAddress>-<workspaceId>", "https", "8080");
        CustomServerEvaluationStrategy.RenderingEvaluation renderingEvaluation = this.customServerEvaluationStrategy
                .getOfflineRenderingEvaluation(containerLabels, exposedPorts, env.stream().toArray(String[]::new));
        String eval = renderingEvaluation.render("<workspaceId>", "4401/tcp");
        Assert.assertEquals(eval, WORKSPACE_ID_VALUE);
    }


}

