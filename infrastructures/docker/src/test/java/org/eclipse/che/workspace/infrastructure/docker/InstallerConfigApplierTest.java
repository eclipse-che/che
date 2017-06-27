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
package org.eclipse.che.workspace.infrastructure.docker;

import org.eclipse.che.api.installer.server.model.impl.InstallerImpl;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Anatolii Bazko
 */
@Listeners(value = {MockitoTestNGListener.class})
public class InstallerConfigApplierTest {

//    @Mock
//    private Instance             machine;
    @Mock
    private InstallerImpl   agent1;
    @Mock
    private InstallerImpl   agent2;
    @Mock
    private InstallerImpl   agent3;
    /*
    TODO Uncomment and fix
    @Mock
    private AgentLauncherFactory agentLauncher;
    @Mock
    private InstallerRegistry    installerRegistry;

    private InstallerConfigApplier agentConfigApplier;

    @BeforeMethod
    public void setUp() throws Exception {
        agentConfigApplier = new InstallerConfigApplier(sorter, installerRegistry);
        when(installerRegistry.getInstaller(InstallerKeyImpl.parse("agent1"))).thenReturn(agent1);
        when(installerRegistry.getInstaller(InstallerKeyImpl.parse("agent2"))).thenReturn(agent2);
        when(installerRegistry.getInstaller(InstallerKeyImpl.parse("agent3"))).thenReturn(agent3);

        when(agent1.getScript()).thenReturn("script1");
        when(agent1.getDependencies()).thenReturn(singletonList("fqn3"));

        when(agent2.getScript()).thenReturn("script2");
        when(agent2.getDependencies()).thenReturn(singletonList("fqn3"));

        when(agent3.getScript()).thenReturn("script3");

    }

    @Test
    public void shouldAddLabels() throws Exception {
        final ServerConfig serverConf1 = mock(ServerConfig.class);
        when(serverConf1.getPort()).thenReturn("1111/udp");
        when(serverConf1.getProtocol()).thenReturn("http");
        when(serverConf1.getPath()).thenReturn("b");

        when(sorter.sort(any())).thenReturn(singletonList(InstallerKeyImpl.parse("agent1")));
        when(agent1.getServers()).thenAnswer(invocation -> singletonMap("a", serverConf1));
        DockerContainerConfig service = new DockerContainerConfig();

        agentConfigApplier.apply(new MachineConfigImpl(singletonList("agent1"),
                                                       emptyMap(),
                                                       emptyMap()),
                                 service);

        Map<String, String> labels = service.getLabels();
        assertEquals(labels.size(), 3);
        assertEquals(labels.get("che:server:1111/udp:ref"), "a");
        assertEquals(labels.get("che:server:1111/udp:protocol"), "http");
        assertEquals(labels.get("che:server:1111/udp:path"), "b");
    }

    @Test
    public void shouldAddExposedPorts() throws Exception {
        final ServerConfig serverConf1 = mock(ServerConfig.class);
        final ServerConfig serverConfig = mock(ServerConfig.class);
        when(serverConf1.getPort()).thenReturn("1111/udp");
        when(serverConfig.getPort()).thenReturn("2222/tcp");

        when(sorter.sort(any())).thenReturn(asList(InstallerKeyImpl.parse("agent1"),
                                                   InstallerKeyImpl.parse("agent2"),
                                                   InstallerKeyImpl.parse("agent3")));
        when(agent1.getServers()).thenAnswer(invocation -> singletonMap("a", serverConf1));
        when(agent2.getServers()).thenAnswer(invocation -> singletonMap("b", serverConfig));
        when(agent3.getServers()).thenReturn(emptyMap());
        DockerContainerConfig service = new DockerContainerConfig();

        agentConfigApplier.apply(new MachineConfigImpl(asList("agent1", "agent2", "agent3"),
                                                       emptyMap(),
                                                       emptyMap()),
                                 service);

        List<String> exposedPorts = service.getExpose();
        assertTrue(exposedPorts.contains("1111/udp"));
        assertTrue(exposedPorts.contains("2222/tcp"));
    }

    @Test
    public void shouldAddEnvVariables() throws Exception {
        when(sorter.sort(any())).thenReturn(asList(InstallerKeyImpl.parse("agent1"), InstallerKeyImpl.parse("agent2")));
        when(agent1.getProperties()).thenReturn(singletonMap("environment", "p1=v1,p2=v2"));
        when(agent2.getProperties()).thenReturn(singletonMap("environment", "p3=v3"));
        DockerContainerConfig service = new DockerContainerConfig();

        agentConfigApplier.apply(new MachineConfigImpl(asList("agent1", "agent2"),
                                                       emptyMap(),
                                                       emptyMap()),
                                 service);

        Map<String, String> env = service.getEnvironment();
        assertEquals(env.size(), 3);
        assertEquals(env.get("p1"), "v1");
        assertEquals(env.get("p2"), "v2");
        assertEquals(env.get("p3"), "v3");
    }

    @Test
    public void shouldIgnoreEnvironmentIfIllegalFormat() throws Exception {
        when(sorter.sort(any())).thenReturn(singletonList(InstallerKeyImpl.parse("agent1")));
        when(agent1.getProperties()).thenReturn(singletonMap("environment", "p1"));
        DockerContainerConfig service = new DockerContainerConfig();

        agentConfigApplier.apply(new MachineConfigImpl(singletonList("agent1"),
                                                       emptyMap(),
                                                       emptyMap()),
                                 service);

        Map<String, String> env = service.getEnvironment();
        assertEquals(env.size(), 0);
    }
    */
}
