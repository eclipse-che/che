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
package org.eclipse.che.workspace.infrastructure.docker.container;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.util.Optional;
import org.eclipse.che.commons.lang.Pair;
import org.mockito.InjectMocks;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Test for {@link ContainerNameGenerator}
 *
 * @author Alexander Andrienko
 */
@Listeners(MockitoTestNGListener.class)
public class DockerContainerNameGeneratorTest {
  private static final String WORKSPACE_ID = "workspacebbbx2ree3iykn8gc";
  private static final String MACHINE_NAME = "ws-machine";
  private static final String MACHINE_ID = "machineic131ppamujngv6y";
  private static final String USER_NAME = "some_user";

  @InjectMocks private ContainerNameGenerator nameGenerator;

  @Test
  public void containerNameShouldBeGenerated() {
    String expectedResult =
        "workspacebbbx2ree3iykn8gc_machineic131ppamujngv6y_some_user_ws-machine";
    String actualResult =
        nameGenerator.generateContainerName(WORKSPACE_ID, MACHINE_ID, USER_NAME, MACHINE_NAME);
    assertEquals(expectedResult, actualResult);
  }

  @Test
  public void machineNameShouldBeReturnedByGeneratedContainerName() {
    String generatedName =
        nameGenerator.generateContainerName(WORKSPACE_ID, MACHINE_ID, USER_NAME, MACHINE_NAME);

    Optional<ContainerNameGenerator.ContainerNameInfo> containerNameInfoParser =
        nameGenerator.parse(generatedName);

    assertEquals(containerNameInfoParser.get().getMachineId(), MACHINE_ID);
    assertEquals(containerNameInfoParser.get().getWorkspaceId(), WORKSPACE_ID);
  }

  @DataProvider(name = "validContainerNames")
  public static Object[][] validContainerNames() {
    return new Object[][] {
      {
        "/host.node.com/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_art",
        new Pair<>("machineri6bxnoj5jq7ll9j", "workspacep2bivvctac5ciwoh")
      },
      {
        "/nod/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_art",
        new Pair<>("machineri6bxnoj5jq7ll9j", "workspacep2bivvctac5ciwoh")
      },
      {
        "/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_user-name_ws-machine-name",
        new Pair<>("machineri6bxnoj5jq7ll9j", "workspacep2bivvctac5ciwoh")
      },
      {
        "/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_user-name_ws-machine-name",
        new Pair<>("machineri6bxnoj5jq7ll9j", "workspacep2bivvctac5ciwoh")
      },
      {
        "/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_user-name_ws-machine-name",
        new Pair<>("machineri6bxnoj5jq7ll9j", "workspacep2bivvctac5ciwoh")
      },
      {
        "/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_user-name_ws-machine-name",
        new Pair<>("machineri6bxnoj5jq7ll9j", "workspacep2bivvctac5ciwoh")
      },
      {
        "/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_user-name_ws-machineri6bxnoj5jq7ll98",
        new Pair<>("machineri6bxnoj5jq7ll9j", "workspacep2bivvctac5ciwoh")
      },
      {
        "/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_u_a",
        new Pair<>("machineri6bxnoj5jq7ll9j", "workspacep2bivvctac5ciwoh")
      },
      {
        "/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_-_-",
        new Pair<>("machineri6bxnoj5jq7ll9j", "workspacep2bivvctac5ciwoh")
      },
      {
        "/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j____",
        new Pair<>("machineri6bxnoj5jq7ll9j", "workspacep2bivvctac5ciwoh")
      },
      {
        "/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j__tfdfd_klk",
        new Pair<>("machineri6bxnoj5jq7ll9j", "workspacep2bivvctac5ciwoh")
      },
      {
        "/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j__",
        new Pair<>("machineri6bxnoj5jq7ll9j", "workspacep2bivvctac5ciwoh")
      },
      {
        "/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_tfdf_dKlk",
        new Pair<>("machineri6bxnoj5jq7ll9j", "workspacep2bivvctac5ciwoh")
      },
      {
        "/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_a_",
        new Pair<>("machineri6bxnoj5jq7ll9j", "workspacep2bivvctac5ciwoh")
      },
      {
        "/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j__o_",
        new Pair<>("machineri6bxnoj5jq7ll9j", "workspacep2bivvctac5ciwoh")
      },
      {
        "/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_tfdfdklk____",
        new Pair<>("machineri6bxnoj5jq7ll9j", "workspacep2bivvctac5ciwoh")
      },
      {"/workspacep_machiner_user-name_ws-machine-name", new Pair<>("machiner", "workspacep")},
      {"/workspace1_machine2_user-name_ws-machine-name", new Pair<>("machine2", "workspace1")},
      {"/workspace1_machinea_user-name_ws-machine-name", new Pair<>("machinea", "workspace1")},
      {"/workspacea_machine1_user-name_ws-machine-name", new Pair<>("machine1", "workspacea")},
      {
        "/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_a",
        new Pair<>("machineri6bxnoj5jq7ll9j", "workspacep2bivvctac5ciwoh")
      },
      {
        "/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_art",
        new Pair<>("machineri6bxnoj5jq7ll9j", "workspacep2bivvctac5ciwoh")
      },
      {"workspace1_machinea_USER_ws-machine-name", new Pair<>("machinea", "workspace1")},
      {
        "/nodes1.night-build.che-build.com/workspacep80at4r4hpvoztve_machinerup047b5xlv5zv20_iedexmain_ws-machine",
        new Pair<>("machinerup047b5xlv5zv20", "workspacep80at4r4hpvoztve")
      },
      {"workspace1_machinea_user%100%_ws-machine-name", new Pair<>("machinea", "workspace1")},
      {
        "workspace1_machinea_user_ws-machine-name$workspace1_machinea_user_ws-machine-name",
        new Pair<>("machinea", "workspace1")
      }
    };
  }

  @Test(dataProvider = "validContainerNames")
  public void testValidContainerNames(String containerName, Pair<String, String> expectedResult) {
    Optional<ContainerNameGenerator.ContainerNameInfo> containerNameInfoParser =
        nameGenerator.parse(containerName);

    assertEquals(containerNameInfoParser.get().getMachineId(), expectedResult.first);
    assertEquals(containerNameInfoParser.get().getWorkspaceId(), expectedResult.second);
  }

  @DataProvider(name = "invalidContainerNames")
  public static Object[][] inValidContainerNames() {
    return new Object[][] {
      {
        "/host.node.com/Workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_user-name_ws-machine-name"
      },
      {"//workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_art"},
      {"//fgfgf/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_art"},
      {"nodehost/workspacep2bivvctac5ciwohmachineri6bxnoj5jq7ll9j_user-name_ws-machine-name"},
      {"/host.node.Com/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_art"},
      {"/host.no%%%de.com/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_art"},
      {"/workspacep2bivvctac5ciwohmachineri6bxnoj5jq7ll9j_user-name_ws-machine-name"},
      {"/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9juser-name_ws-machine-name"},
      {"/workspacep2bivvctac5ciwoh__machineri6bxnoj5jq7ll9j_user-name_ws-machine-name"},
      {"/1orkspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_user-name_ws-machine-name"},
      {"/workspacep2bivvctac%ciwoh_machineri6bxnoj5jq7ll9j_user-name_ws-machine-name"},
      {"/workspacep2bivvctackciwoh_machineri6%xnoj5jq7ll9j_user-name_ws-machine-name"},
      {"workspacep2bivvctackciwoh_machineri6o*noj5jq7ll9j_user-name_ws-machine-name"},
      {"workspacep2bivvctac5ciWoh_machineri6bxnoj5jq7ll9j_user-name_ws-machine-name"},
      {"workspacep2bivvctac5ciwoh_machineri6bXnoj5jq7ll9j_user-name_ws-machine-name"},
      {"workspacep2bivvctac5ciwoh_machineri6bXnoj5jq7ll9j_user-name_WS-maChineName"},
      {"workspacep2bivvctac5ciwoh_machineri6bXnoj5jq7ll9j_user-name_ws-mac%ine-name"},
      {"workspacep2bivvctac5ciwoh_machineri6bXnoj5jq7ll9j_user-name_workspACEMachineName"},
      {"workspacep2bivvctac5ciwoh_machineri6bXnoj5jq7ll9j_user-name_workspace"},
      {"workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j"},
      {"workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_"},
      {"/FF/workspacep2bivvctac5ciwoh_machineri6bxnoj5jq7ll9j_art"},
      {"workspacep2bivvctac5ciwoh_"},
      {"pong"},
      {"workspace"},
      {"machine"},
      {"workspace"},
      {"machine"},
      {"workspace_machine"},
      {"workspace_machine_user-name_ws-machine-name"},
      {"workspace5r_workspace5r_machine_user-name_ws-machine-name"},
      {"workspace_workspace_machine_user-name_ws-machine-name"},
      {"work_machinetyy_user-name_ws-machine-name"},
      {"workspacedfdf_machin_user-name_ws-machine-name"},
      {"workspaceid"},
      {"machineid"},
      {"workspacerere_machinedfdf"},
      {"workspacerere_machinedfdf"},
      {"someusercontainer"},
      {"machineri6bxnoj5jq7ll9j_workspacep2bivvctac5ciwoh_user-name_ws-machine-name"}
    };
  }

  @Test(dataProvider = "invalidContainerNames")
  public void testInvalidContainerNames(String containerName) {
    Optional<ContainerNameGenerator.ContainerNameInfo> containerNameInfoParser =
        nameGenerator.parse(containerName);

    assertFalse(containerNameInfoParser.isPresent());
  }
}
