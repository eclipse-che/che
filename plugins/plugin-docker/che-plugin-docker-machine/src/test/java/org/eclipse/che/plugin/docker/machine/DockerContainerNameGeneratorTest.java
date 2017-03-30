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

import org.eclipse.che.commons.lang.Pair;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Optional;

import static java.lang.String.format;
import static org.eclipse.che.plugin.docker.machine.DockerContainerNameGenerator.ContainerNameInfo;
import static org.testng.Assert.assertFalse;
import static org.testng.AssertJUnit.assertEquals;

/**
 * Test for {@link DockerContainerNameGenerator}
 *
 * @author Alexander Andrienko
 * @author Alexander Garagatyi
 */
@Listeners(MockitoTestNGListener.class)
public class DockerContainerNameGeneratorTest {
    private static final String WORKSPACE_ID            = "workspacebbbx2ree3iykn8gc";
    private static final String MACHINE_NAME            = "ws-machine";
    private static final String MACHINE_ID              = "machineic131ppamujngv6y";
    private static final String USER_NAME               = "some_user";
    private static final String SERVER_ID               = "serversomehostcom";
    private static final String NON_NORMALIZED_HOSTNAME = "somehost.com";

    private DockerContainerNameGenerator nameGenerator;

    @BeforeMethod
    public void setUp() throws Exception {
        nameGenerator = new DockerContainerNameGenerator(NON_NORMALIZED_HOSTNAME);
    }

    @Test
    public void containerNameShouldBeGenerated() {
        String expectedResult = WORKSPACE_ID + "_" +
                                MACHINE_ID + "_" +
                                SERVER_ID + "_" +
                                USER_NAME + "_" +
                                MACHINE_NAME;
        String actualResult = nameGenerator.generateContainerName(WORKSPACE_ID, MACHINE_ID, USER_NAME, MACHINE_NAME);
        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void machineNameShouldBeReturnedByGeneratedContainerName() {
        String generatedName = nameGenerator.generateContainerName(WORKSPACE_ID, MACHINE_ID, USER_NAME, MACHINE_NAME);

        Optional<ContainerNameInfo> containerNameInfoParser = nameGenerator.parse(generatedName);

        @SuppressWarnings("OptionalGetWithoutIsPresent")
        ContainerNameInfo containerNameInfo = containerNameInfoParser.get();
        assertEquals(containerNameInfo.getMachineId(), MACHINE_ID);
        assertEquals(containerNameInfo.getWorkspaceId(), WORKSPACE_ID);
    }

    @DataProvider(name = "validContainerNames")
    public static Object[][] validContainerNames() {
        return new Object[][] {
                {format("/host.node.com/%s_%s_%s_art", WORKSPACE_ID, MACHINE_ID, SERVER_ID),
                 new Pair<>(WORKSPACE_ID, MACHINE_ID)},

                {format("/nod/%s_%s_%s_art", WORKSPACE_ID, MACHINE_ID, SERVER_ID),
                 new Pair<>(WORKSPACE_ID, MACHINE_ID)},

                {format("/%s_%s_%s_user-name_ws-machine-name", WORKSPACE_ID, MACHINE_ID, SERVER_ID),
                 new Pair<>(WORKSPACE_ID, MACHINE_ID)},

                {format("/%s_%s_%s_user-name_ws-machine-name", WORKSPACE_ID, MACHINE_ID, SERVER_ID),
                 new Pair<>(WORKSPACE_ID, MACHINE_ID)},

                {format("/%s_%s_%s_user-name_ws-machine-name", WORKSPACE_ID, MACHINE_ID, SERVER_ID),
                 new Pair<>(WORKSPACE_ID, MACHINE_ID)},

                {format("/%s_%s_%s_user-name_ws-machine-name", WORKSPACE_ID, MACHINE_ID, SERVER_ID),
                 new Pair<>(WORKSPACE_ID, MACHINE_ID)},

                {format("/%s_%s_%s_user-name_ws-machineri6bxnoj5jq7ll98", WORKSPACE_ID, MACHINE_ID, SERVER_ID),
                 new Pair<>(WORKSPACE_ID, MACHINE_ID)},

                {format("/%s_%s_%s_u_a", WORKSPACE_ID, MACHINE_ID, SERVER_ID),
                 new Pair<>(WORKSPACE_ID, MACHINE_ID)},

                {format("/%s_%s_%s_-_-", WORKSPACE_ID, MACHINE_ID, SERVER_ID),
                 new Pair<>(WORKSPACE_ID, MACHINE_ID)},

                {format("/%s_%s_%s____", WORKSPACE_ID, MACHINE_ID, SERVER_ID),
                 new Pair<>(WORKSPACE_ID, MACHINE_ID)},

                {format("/%s_%s_%s__tfdfd_klk", WORKSPACE_ID, MACHINE_ID, SERVER_ID),
                 new Pair<>(WORKSPACE_ID, MACHINE_ID)},

                {format("/%s_%s_%s__", WORKSPACE_ID, MACHINE_ID, SERVER_ID),
                 new Pair<>(WORKSPACE_ID, MACHINE_ID)},

                {format("/%s_%s_%s_tfdf_dKlk", WORKSPACE_ID, MACHINE_ID, SERVER_ID),
                 new Pair<>(WORKSPACE_ID, MACHINE_ID)},

                {format("/%s_%s_%s_a_", WORKSPACE_ID, MACHINE_ID, SERVER_ID),
                 new Pair<>(WORKSPACE_ID, MACHINE_ID)},

                {format("/%s_%s_%s__o_", WORKSPACE_ID, MACHINE_ID, SERVER_ID),
                 new Pair<>(WORKSPACE_ID, MACHINE_ID)},

                {format("/%s_%s_%s_tfdfdklk____", WORKSPACE_ID, MACHINE_ID, SERVER_ID),
                 new Pair<>(WORKSPACE_ID, MACHINE_ID)},

                {format("/workspacep_machiner_%s_user-name_ws-machine-name", SERVER_ID),
                 new Pair<>("workspacep", "machiner")},

                {format("/workspace1_machine2_%s_user-name_ws-machine-name", SERVER_ID),
                 new Pair<>("workspace1", "machine2")},

                {format("/workspace1_machinea_%s_user-name_ws-machine-name", SERVER_ID),
                 new Pair<>("workspace1", "machinea")},

                {format("/workspacea_machine1_%s_user-name_ws-machine-name", SERVER_ID),
                 new Pair<>("workspacea", "machine1")},

                {format("/%s_%s_%s_a", WORKSPACE_ID, MACHINE_ID, SERVER_ID),
                 new Pair<>(WORKSPACE_ID, MACHINE_ID)},

                {format("/%s_%s_%s_art", WORKSPACE_ID, MACHINE_ID, SERVER_ID),
                 new Pair<>(WORKSPACE_ID, MACHINE_ID)},

                {format("workspace1_machinea_%s_USER_ws-machine-name", SERVER_ID),
                 new Pair<>("workspace1", "machinea")},

                {format("/nodes1.night-build.che-build.com/%s_%s_%s_iedexmain_ws-machine", WORKSPACE_ID,
                        MACHINE_ID, SERVER_ID),
                 new Pair<>(WORKSPACE_ID, MACHINE_ID)},

                {format("workspace1_machinea_%s_user%100%_ws-machine-name", SERVER_ID),
                 new Pair<>("workspace1", "machinea")},

                {format("workspace1_machinea_%s_user_ws-machine-name$workspace1_machinea_user_ws-machine-name", SERVER_ID),
                 new Pair<>("workspace1", "machinea")}};
    }

    @Test(dataProvider = "validContainerNames")
    public void testValidContainerNames(String containerName, Pair<String, String> expectedResult) {
        Optional<ContainerNameInfo> containerNameInfoParser = nameGenerator.parse(containerName);

        @SuppressWarnings("OptionalGetWithoutIsPresent")
        ContainerNameInfo containerNameInfo = containerNameInfoParser.get();
        assertEquals(containerNameInfo.getWorkspaceId(), expectedResult.first);
        assertEquals(containerNameInfo.getMachineId(), expectedResult.second);
    }

    @DataProvider(name = "invalidContainerNames")
    public static Object[][] inValidContainerNames() {
        return new Object[][] {
                {"/host.node.com/Workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_serversomehostcom_user-name_ws-machine-name"},
                {format("//%s_%s_art", WORKSPACE_ID, MACHINE_ID)},
                {format("//fgfgf/%s_%s_art", WORKSPACE_ID, MACHINE_ID)},
                {format("nodehost/%s%s_user-name_ws-machine-name", WORKSPACE_ID, MACHINE_ID)},
                {format("/host.node.Com/%s_%s_art", WORKSPACE_ID, MACHINE_ID)},
                {"/host.no%%%de.com/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_serversomehostcom_art"},
                {format("/%s%s_user-name_ws-machine-name", WORKSPACE_ID, MACHINE_ID)},
                {format("/%s_%suser-name_ws-machine-name", WORKSPACE_ID, MACHINE_ID)},
                {format("/%s__%s_user-name_ws-machine-name", WORKSPACE_ID, MACHINE_ID)},
                {"/1orkspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_serversomehostcom_user-name_ws-machine-name"},
                {"/workspacep2bivvctac%ciwoh_machineri6bxnoj5jq7ll9j_serversomehostcom_user-name_ws-machine-name"},
                {"/workspacep2bivvctackciwoh_machineri6%xnoj5jq7ll9j_serversomehostcom_user-name_ws-machine-name"},
                {"workspacep2bivvctackciwoh_machineri6o*noj5jq7ll9j_serversomehostcom_user-name_ws-machine-name"},
                {"workspacep2bivvctac5ciWoh_machineri6bxnoj5jq7ll9j_serversomehostcom_user-name_ws-machine-name"},
                {format("%s_machineri6bXnoj5jq7ll9j_user-name_ws-machine-name", WORKSPACE_ID)},
                {format("%s_machineri6bXnoj5jq7ll9j_user-name_WS-maChineName", WORKSPACE_ID)},
                {"workspacep2bivvctac5ciwoh_machineri6bXnoj5jq7ll9j_serversomehostcom_user-name_ws-mac%ine-name"},
                {format("%s_machineri6bXnoj5jq7ll9j_user-name_workspACEMachineName", WORKSPACE_ID)},
                {format("%s_machineri6bXnoj5jq7ll9j_user-name_workspace", WORKSPACE_ID)},
                {format("%s_%s", WORKSPACE_ID, MACHINE_ID)},
                {format("%s_%s_", WORKSPACE_ID, MACHINE_ID)},
                {format("%s_%s_%s_", WORKSPACE_ID, MACHINE_ID, SERVER_ID)},
                {format("/FF/%s_%s_art", WORKSPACE_ID, MACHINE_ID)},
                {format("/FF/%s_%s_%s_art", WORKSPACE_ID, MACHINE_ID, SERVER_ID)},
                {format("%s_", WORKSPACE_ID)},
                {"pong"},
                {"workspace"},
                {"machine"},
                {"workspace"},
                {"machine"},
                {"workspace_machine"},
                {"workspace_machine_serversomehostcom"},
                {"workspace_machine_serversomehostcom_user-name_ws-machine-name"},
                {"workspace5r_workspace5r_machine_serversomehostcom_user-name_ws-machine-name"},
                {"workspace_workspace_machine_serversomehostcom_user-name_ws-machine-name"},
                {"work_machinetyy_serversomehostcom_user-name_ws-machine-name"},
                {"workspacedfdf_machin_serversomehostcom_user-name_ws-machine-name"},
                {"workspaceid"},
                {"machineid"},
                {"workspacerere_machinedfdf"},
                {"workspacerere_machinedfdf_serversomehostcom"},
                {"someusercontainer"},
                // no server id
                {format("%s_%s_user-name_ws-machine-name", WORKSPACE_ID, MACHINE_ID)},
                // no server id
                {format("/%s_%s_user-name_ws-machine-name", WORKSPACE_ID, MACHINE_ID)},
                // no server id
                {format("/host.node.com/%s_%s_art", WORKSPACE_ID, MACHINE_ID)},
                // server id differs
                {format("%s_%s_%s_user-name_ws-machine-name", WORKSPACE_ID, MACHINE_ID, SERVER_ID + "other")},
                // server id differs
                {format("/%s_%s_%s_user-name_ws-machine-name", WORKSPACE_ID, MACHINE_ID, SERVER_ID + "other")},
                // server id differs
                {format("/host.node.com/%s_%s_%s_art", WORKSPACE_ID, MACHINE_ID, SERVER_ID + "other")}
        };
    }

    @Test(dataProvider = "invalidContainerNames")
    public void testInvalidContainerNames(String containerName) {
        Optional<ContainerNameInfo> containerNameInfoParser = nameGenerator.parse(containerName);

        assertFalse(containerNameInfoParser.isPresent());
    }
}

