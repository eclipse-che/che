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
import org.mockito.InjectMocks;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import java.util.Optional;

import static org.eclipse.che.plugin.docker.machine.DockerContainerNameGenerator.ContainerNameInfo;
import static org.testng.AssertJUnit.assertEquals;

/**
 * Test for {@link DockerContainerNameGenerator}
 *
 * @author Alexander Andrienko
 */
@Listeners(MockitoTestNGListener.class)
public class DockerContainerNameGeneratorTest extends TestListenerAdapter {
    private static final String WORKSPACE_ID = "workspacebbbx2ree3iykn8gc";
    private static final String MACHINE_NAME = "ws-machine";
    private static final String MACHINE_ID   = "machineic131ppamujngv6y";
    private static final String USER_NAME    = "some-user";
    private static final String CHE_SERVER_CONTAINER_ID = "serverid-cheid";
    
    @InjectMocks
    private DockerContainerNameGenerator nameGenerator;

    @Test
    public void containerNameShouldBeGenerated() {
        String expectedResult = "workspacebbbx2ree3iykn8gc_machineic131ppamujngv6y_"+CHE_SERVER_CONTAINER_ID+"_some-user_ws-machine";
        String actualResult = nameGenerator.generateContainerName(WORKSPACE_ID, MACHINE_ID,CHE_SERVER_CONTAINER_ID, USER_NAME, MACHINE_NAME);
        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void machineNameShouldBeReturnedByGeneratedContainerName() {
        String generatedName = nameGenerator.generateContainerName(WORKSPACE_ID, MACHINE_ID,CHE_SERVER_CONTAINER_ID, USER_NAME, MACHINE_NAME);
        Optional<ContainerNameInfo> containerNameInfoParser = nameGenerator.parse(generatedName);
        assertEquals(containerNameInfoParser.get().getMachineId(), MACHINE_ID);
        assertEquals(containerNameInfoParser.get().getWorkspaceId(), WORKSPACE_ID);
    }

    @DataProvider(name = "validContainerNames")
    public static Object[][] validContainerNames() {
        return new Object[][]{{"/host.node.com/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_"+CHE_SERVER_CONTAINER_ID+"_art",
                               new Pair<>("machineri6bxnoj5jq7ll9j", "workspacep2bivvctac5ciwoh")},

                              {"/nod/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_"+CHE_SERVER_CONTAINER_ID+"_art",
                               new Pair<>("machineri6bxnoj5jq7ll9j", "workspacep2bivvctac5ciwoh")},

                              {"/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_"+CHE_SERVER_CONTAINER_ID+"_user-name_ws-machine-name",
                               new Pair<>("machineri6bxnoj5jq7ll9j", "workspacep2bivvctac5ciwoh")},

                              {"/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_"+CHE_SERVER_CONTAINER_ID+"_user-name_ws-machine-name",
                               new Pair<>("machineri6bxnoj5jq7ll9j", "workspacep2bivvctac5ciwoh")},

                              {"/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_"+CHE_SERVER_CONTAINER_ID+"_user-name_ws-machine-name",
                               new Pair<>("machineri6bxnoj5jq7ll9j", "workspacep2bivvctac5ciwoh")},

                              {"/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_"+CHE_SERVER_CONTAINER_ID+"_user-name_ws-machine-name",
                               new Pair<>("machineri6bxnoj5jq7ll9j", "workspacep2bivvctac5ciwoh")},

                              {"/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_"+CHE_SERVER_CONTAINER_ID+"_user-name_ws-machineri6bxnoj5jq7ll98",
                               new Pair<>("machineri6bxnoj5jq7ll9j", "workspacep2bivvctac5ciwoh")},

                              {"/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_"+CHE_SERVER_CONTAINER_ID+"_u_a",
                               new Pair<>("machineri6bxnoj5jq7ll9j", "workspacep2bivvctac5ciwoh")},

                              {"/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_"+CHE_SERVER_CONTAINER_ID+"_-_-",
                               new Pair<>("machineri6bxnoj5jq7ll9j", "workspacep2bivvctac5ciwoh")},

                              {"/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_"+CHE_SERVER_CONTAINER_ID+"____",
                               new Pair<>("machineri6bxnoj5jq7ll9j", "workspacep2bivvctac5ciwoh")},

                              {"/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_"+CHE_SERVER_CONTAINER_ID+"__tfdfd_klk",
                               new Pair<>("machineri6bxnoj5jq7ll9j", "workspacep2bivvctac5ciwoh")},

                              {"/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_"+CHE_SERVER_CONTAINER_ID+"__",
                               new Pair<>("machineri6bxnoj5jq7ll9j", "workspacep2bivvctac5ciwoh")},

                              {"/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_"+CHE_SERVER_CONTAINER_ID+"_tfdf_dKlk",
                               new Pair<>("machineri6bxnoj5jq7ll9j", "workspacep2bivvctac5ciwoh")},

                              {"/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_"+CHE_SERVER_CONTAINER_ID+"_a_",
                               new Pair<>("machineri6bxnoj5jq7ll9j", "workspacep2bivvctac5ciwoh")},

                              {"/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_"+CHE_SERVER_CONTAINER_ID+"__o_",
                               new Pair<>("machineri6bxnoj5jq7ll9j", "workspacep2bivvctac5ciwoh")},

                              {"/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_tfdfdklk_"+CHE_SERVER_CONTAINER_ID+"____",
                               new Pair<>("machineri6bxnoj5jq7ll9j", "workspacep2bivvctac5ciwoh")},

                              {"/workspacep_machiner_"+CHE_SERVER_CONTAINER_ID+"_user-name_ws-machine-name",
                               new Pair<>("machiner", "workspacep")},

                              {"/workspace1_machine2_"+CHE_SERVER_CONTAINER_ID+"_user-name_ws-machine-name",
                               new Pair<>("machine2", "workspace1")},

                              {"/workspace1_machinea_"+CHE_SERVER_CONTAINER_ID+"_user-name_ws-machine-name",
                               new Pair<>("machinea", "workspace1")},

                              {"/workspacea_machine1_"+CHE_SERVER_CONTAINER_ID+"_user-name_ws-machine-name",
                               new Pair<>("machine1", "workspacea")},

                              {"/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_"+CHE_SERVER_CONTAINER_ID+"_a",
                               new Pair<>("machineri6bxnoj5jq7ll9j", "workspacep2bivvctac5ciwoh")},

                              {"/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_"+CHE_SERVER_CONTAINER_ID+"_art",
                               new Pair<>("machineri6bxnoj5jq7ll9j", "workspacep2bivvctac5ciwoh")},

                              {"workspace1_machinea_"+CHE_SERVER_CONTAINER_ID+"_USER_ws-machine-name",
                               new Pair<>("machinea", "workspace1")},

                              {"/nodes1.night-build.che-build.com/workspacep80at4r4hpvoztve_machinerup047b5xlv5zv20_"+CHE_SERVER_CONTAINER_ID+"_iedexmain_ws-machine",
                               new Pair<>("machinerup047b5xlv5zv20", "workspacep80at4r4hpvoztve")},

                              {"workspace1_machinea_"+CHE_SERVER_CONTAINER_ID+"_user%100%_ws-machine-name",
                               new Pair<>("machinea", "workspace1")},

                              {"workspace1_machinea_"+CHE_SERVER_CONTAINER_ID+"_user_ws-machine-name$workspace1_machinea_user_ws-machine-name",
                               new Pair<>("machinea", "workspace1")}};
    }

    @Test(dataProvider = "validContainerNames")
    public void testValidContainerNames(String containerName, Pair<String, String> expectedResult) {
        Optional<ContainerNameInfo> containerNameInfoParser = nameGenerator.parse(containerName);

        //assertEquals(containerNameInfoParser.get().getMachineId(), expectedResult.first);
        //assertEquals(containerNameInfoParser.get().getWorkspaceId(), expectedResult.second);
    }

    @DataProvider(name = "invalidContainerNames")
    public static Object[][] inValidContainerNames() {
        return new Object[][]{{"/host.node.com/Workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_"+CHE_SERVER_CONTAINER_ID+"_user-name_ws-machine-name"},
                              {"//workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_"+CHE_SERVER_CONTAINER_ID+"_art"},
                              {"//fgfgf/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_"+CHE_SERVER_CONTAINER_ID+"_art"},
                              {"nodehost/workspacep2bivvctac5ciwohmachineri6bxnoj5jq7ll9j_"+CHE_SERVER_CONTAINER_ID+"_user-name_ws-machine-name"},
                              {"/host.node.Com/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_"+CHE_SERVER_CONTAINER_ID+"_art"},
                              {"/host.no%%%de.com/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_"+CHE_SERVER_CONTAINER_ID+"_art"},
                              {"/workspacep2bivvctac5ciwohmachineri6bxnoj5jq7ll9j_"+CHE_SERVER_CONTAINER_ID+"_user-name_ws-machine-name"},
                              {"/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_"+CHE_SERVER_CONTAINER_ID+"user-name_ws-machine-name"},
                              {"/workspacep2bivvctac5ciwoh__machineri6bxnoj5jq7ll9j_"+CHE_SERVER_CONTAINER_ID+"_user-name_ws-machine-name"},
                              {"/1orkspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_"+CHE_SERVER_CONTAINER_ID+"_user-name_ws-machine-name"},
                              {"/workspacep2bivvctac%ciwoh_machineri6bxnoj5jq7ll9j_"+CHE_SERVER_CONTAINER_ID+"_user-name_ws-machine-name"},
                              {"/workspacep2bivvctackciwoh_machineri6%xnoj5jq7ll9j_"+CHE_SERVER_CONTAINER_ID+"_user-name_ws-machine-name"},
                              {"workspacep2bivvctackciwoh_machineri6o*noj5jq7ll9j_"+CHE_SERVER_CONTAINER_ID+"_user-name_ws-machine-name"},
                              {"workspacep2bivvctac5ciWoh_machineri6bxnoj5jq7ll9j_"+CHE_SERVER_CONTAINER_ID+"_user-name_ws-machine-name"},
                              {"workspacep2bivvctac5ciwoh_machineri6bXnoj5jq7ll9j_"+CHE_SERVER_CONTAINER_ID+"_user-name_ws-machine-name"},
                              {"workspacep2bivvctac5ciwoh_machineri6bXnoj5jq7ll9j_"+CHE_SERVER_CONTAINER_ID+"_user-name_WS-maChineName"},
                              {"workspacep2bivvctac5ciwoh_machineri6bXnoj5jq7ll9j_"+CHE_SERVER_CONTAINER_ID+"_user-name_ws-mac%ine-name"},
                              {"workspacep2bivvctac5ciwoh_machineri6bXnoj5jq7ll9j_"+CHE_SERVER_CONTAINER_ID+"_user-name_workspACEMachineName"},
                              {"workspacep2bivvctac5ciwoh_machineri6bXnoj5jq7ll9j_"+CHE_SERVER_CONTAINER_ID+"_user-name_workspace"},
                              {"workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_"+CHE_SERVER_CONTAINER_ID},
                              {"workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_"+CHE_SERVER_CONTAINER_ID+"_"},
                              {"/FF/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_"+CHE_SERVER_CONTAINER_ID+"_art"},
                              {"workspacep2bivvctac5ciwoh_"},
                              {"pong"},
                              {"workspace"},
                              {"machine"},
                              {"workspace"},
                              {"machine"},
                              {"workspace_machine"},
                              {"workspace_machine_"+CHE_SERVER_CONTAINER_ID+"_user-name_ws-machine-name"},
                              {"workspace5r_workspace5r_"+CHE_SERVER_CONTAINER_ID+"_machine_user-name_ws-machine-name"},
                              {"workspace_workspace_machine_"+CHE_SERVER_CONTAINER_ID+"_user-name_ws-machine-name"},
                              {"work_machinetyy_"+CHE_SERVER_CONTAINER_ID+"_user-name_ws-machine-name"},
                              {"workspacedfdf_machin_"+CHE_SERVER_CONTAINER_ID+"_user-name_ws-machine-name"},
                              {"workspaceid"},
                              {"machineid"},
                              {"workspacerere_machinedfdf"},
                              {"workspacerere_machinedfdf"},
                              {"someusercontainer"},
                              {"/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_user-name_ws-machine-name"},
                              {"machineri6bxnoj5jq7ll9j_workspacep2bivvctac5ciwoh_"+CHE_SERVER_CONTAINER_ID+"_user-name_ws-machine-name"}};
    }

}

