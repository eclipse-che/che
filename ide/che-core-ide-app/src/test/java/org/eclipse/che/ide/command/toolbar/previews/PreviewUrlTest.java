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
package org.eclipse.che.ide.command.toolbar.previews;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.machine.shared.dto.MachineRuntimeInfoDto;
import org.eclipse.che.api.machine.shared.dto.ServerDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.DevMachine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/** Tests for {@link PreviewUrl}. */
@RunWith(MockitoJUnitRunner.class)
public class PreviewUrlTest {

  private static final String PREVIEW_URL = "http://preview.com/param";
  private static final String MACHINE_NAME = "dev-machine";
  private static final String SERVER_PORT = "8080";

  @Mock private AppContext appContext;

  private PreviewUrl previewUrl;

  @Before
  public void setUp() {
    ServerDto server = mock(ServerDto.class);
    when(server.getUrl()).thenReturn("http://preview.com");

    Map<String, ServerDto> servers = new HashMap<>();
    servers.put(SERVER_PORT + "/tcp", server);

    MachineRuntimeInfoDto machineRuntimeInfo = mock(MachineRuntimeInfoDto.class);
    when(machineRuntimeInfo.getServers()).thenReturn(servers);

    DevMachine devMachine = mock(DevMachine.class);
    when(devMachine.getDisplayName()).thenReturn(MACHINE_NAME);
    when(devMachine.getRuntime()).thenReturn(machineRuntimeInfo);

    when(appContext.getDevMachine()).thenReturn(devMachine);

    previewUrl = new PreviewUrl(PREVIEW_URL, appContext);
  }

  @Test
  public void testGetUrl() throws Exception {
    assertEquals(PREVIEW_URL, previewUrl.getUrl());
  }

  @Test
  public void testGetDisplayName() throws Exception {
    assertEquals(MACHINE_NAME + ':' + SERVER_PORT + "/param", previewUrl.getDisplayName());
  }
}
