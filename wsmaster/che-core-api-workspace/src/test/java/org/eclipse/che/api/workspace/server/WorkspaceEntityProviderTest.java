/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server;

import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import org.eclipse.che.api.workspace.server.devfile.DevfileParser;
import org.eclipse.che.api.workspace.server.model.impl.devfile.DevfileImpl;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.api.workspace.shared.dto.devfile.DevfileDto;
import org.eclipse.che.dto.server.DtoFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class WorkspaceEntityProviderTest {

  @Mock private DevfileParser devfileParser;

  @InjectMocks private WorkspaceEntityProvider workspaceEntityProvider;

  @Test
  public void shouldBuildDtoFromValidJson() throws Exception {

    when(devfileParser.parseJson(anyString())).thenReturn(new DevfileImpl());

    WorkspaceDto actual = newDto(WorkspaceDto.class).withDevfile(newDto(DevfileDto.class));

    workspaceEntityProvider.readFrom(
        WorkspaceDto.class,
        WorkspaceDto.class,
        null,
        MediaType.APPLICATION_JSON_TYPE,
        new MultivaluedHashMap<>(),
        new ByteArrayInputStream(
            DtoFactory.getInstance().toJson(actual).getBytes(StandardCharsets.UTF_8)));

    verify(devfileParser).parseJson(anyString());
  }
}
