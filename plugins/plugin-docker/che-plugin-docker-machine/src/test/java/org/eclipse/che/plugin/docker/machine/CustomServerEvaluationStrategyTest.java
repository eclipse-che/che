package org.eclipse.che.plugin.docker.machine;

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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;

/**
 * Test for {@Link CustomServerEvaluationStrategy}
 *
 * @author Florent Benoit
 */
@Listeners(MockitoTestNGListener.class)
public class CustomServerEvaluationStrategyTest {

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
        when(networkSettings.getGateway()).thenReturn("gateway");
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
        networkExposedPorts.put("22/tcp", Collections.emptyList());
        containerExposedPorts.put("4401/tcp", Collections.emptyMap());
        networkExposedPorts.put("4401/tcp", Collections.emptyList());
        containerExposedPorts.put("4411/tcp", Collections.emptyMap());
        networkExposedPorts.put("4411/tcp", Collections.emptyList());
        containerExposedPorts.put("8080/tcp", Collections.emptyMap());
        networkExposedPorts.put("8080/tcp", Collections.emptyList());
    }


    /**
     * Check workspace Id template
     */
    @Test
    public void testWorkspaceIdRule() throws Throwable {
        this.customServerEvaluationStrategy = new CustomServerEvaluationStrategy("10.0.0.1", "192.168.1.1", "<workspaceId>", "8080");

        Map<String, String> portMapping = this.customServerEvaluationStrategy.getExternalAddressesAndPorts(containerInfo, "localhost");

        Assert.assertTrue(portMapping.containsKey("4401/tcp"));
        Assert.assertEquals(portMapping.get("4401/tcp"), WORKSPACE_ID_VALUE);
    }


    /**
     * Check workspace Id template
     */
    @Test
    public void testMachineNameRule() throws Throwable {
        this.customServerEvaluationStrategy = new CustomServerEvaluationStrategy("10.0.0.1", "192.168.1.1", "<machineName>", "8080");

        Map<String, String> portMapping = this.customServerEvaluationStrategy.getExternalAddressesAndPorts(containerInfo, "localhost");

        Assert.assertTrue(portMapping.containsKey("4401/tcp"));
        Assert.assertEquals(portMapping.get("4401/tcp"), MACHINE_NAME_VALUE);


    }

}

