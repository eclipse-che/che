/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.workspace.shared.event.WorkspaceRemovedEvent;
import org.eclipse.che.infrastructure.docker.client.DockerConnector;
import org.eclipse.che.infrastructure.docker.client.json.volume.Volume;
import org.eclipse.che.infrastructure.docker.client.json.volume.Volumes;
import org.eclipse.che.infrastructure.docker.client.params.volume.RemoveVolumeParams;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.volume.VolumeNames;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Alexander Garagatyi */
@Listeners(MockitoTestNGListener.class)
public class OnWorkspaceRemoveDataVolumeRemoverTest {
  static final String WS_ID = "testWSId";

  @Mock WorkspaceRemovedEvent event;
  @Mock Workspace workspace;
  @Mock Volumes volumes;
  @Mock DockerConnector docker;
  @InjectMocks OnWorkspaceRemoveDataVolumeRemover remover;

  @BeforeMethod
  public void setUp() throws Exception {
    when(event.getWorkspace()).thenReturn(workspace);
    when(workspace.getId()).thenReturn(WS_ID);
    when(docker.getVolumes()).thenReturn(volumes);
  }

  @Test
  public void shouldRemoveVolumes() throws Exception {
    // given
    String volume1Name = VolumeNames.generate(WS_ID, "volume1");
    String volume2Name = VolumeNames.generate(WS_ID, "volume2");
    String volume3Name = VolumeNames.generate(WS_ID, "volume3");
    Volume volume1 = new Volume().withName(volume1Name);
    Volume volume2 = new Volume().withName(volume2Name);
    Volume volume3 = new Volume().withName(volume3Name);
    when(volumes.getVolumes()).thenReturn(asList(volume1, volume2, volume3));

    // when
    remover.onEvent(event);

    // then
    verify(docker).removeVolume(eq(volume1Name));
    verify(docker).removeVolume(eq(volume2Name));
    verify(docker).removeVolume(eq(volume3Name));
  }

  @Test
  public void shouldKeepRemovingWhenOneRemovalFails() throws Exception {
    // given
    String volume1Name = VolumeNames.generate(WS_ID, "volume1");
    String volume2Name = VolumeNames.generate(WS_ID, "volume2");
    String volume3Name = VolumeNames.generate(WS_ID, "volume3");
    Volume volume1 = new Volume().withName(volume1Name);
    Volume volume2 = new Volume().withName(volume2Name);
    Volume volume3 = new Volume().withName(volume3Name);
    when(volumes.getVolumes()).thenReturn(asList(volume1, volume2, volume3));
    doThrow(new IOException("test exc")).when(docker).removeVolume(eq(volume2Name));

    // when
    remover.onEvent(event);

    // then
    InOrder inOrder = inOrder(docker);
    inOrder.verify(docker).removeVolume(eq(volume1Name));
    inOrder.verify(docker).removeVolume(eq(volume2Name));
    inOrder.verify(docker).removeVolume(eq(volume3Name));
  }

  @Test
  public void shouldNotRemoveVolumesThatDoesNotMatchWorkspace() throws Exception {
    // given
    String volume1Name = VolumeNames.generate(WS_ID, "volume1");
    String volume2Name = VolumeNames.generate(WS_ID + "2", "volume2");
    String volume3Name = VolumeNames.generate(WS_ID + WS_ID, "volume3");
    Volume volume1 = new Volume().withName(volume1Name);
    Volume volume2 = new Volume().withName(volume2Name);
    Volume volume3 = new Volume().withName(volume3Name);
    when(volumes.getVolumes()).thenReturn(asList(volume1, volume2, volume3));

    // when
    remover.onEvent(event);

    // then
    verify(docker).removeVolume(eq(volume1Name));
    verify(docker, never()).removeVolume(eq(volume2Name));
    verify(docker, never()).removeVolume(eq(volume3Name));
  }

  @Test
  public void shouldNotThrowExceptionIfVolumesListIsNull() throws Exception {
    // given
    when(volumes.getVolumes()).thenReturn(null);

    // when
    remover.onEvent(event);

    // then
    verify(docker, never()).removeVolume(anyString());
    verify(docker, never()).removeVolume(any(RemoveVolumeParams.class));
  }
}
