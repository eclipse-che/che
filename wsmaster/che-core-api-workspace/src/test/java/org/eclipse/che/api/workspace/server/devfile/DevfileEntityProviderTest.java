/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.devfile;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ws.rs.NotSupportedException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import org.eclipse.che.api.workspace.server.model.impl.devfile.DevfileImpl;
import org.eclipse.che.api.workspace.shared.dto.devfile.DevfileDto;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class DevfileEntityProviderTest {

  @Mock private DevfileParser devfileParser;

  @InjectMocks private DevfileEntityProvider devfileEntityProvider;

  @Test
  public void shouldBuildDtoFromValidYaml() throws Exception {

    when(devfileParser.parseYaml(anyString())).thenReturn(new DevfileImpl());

    devfileEntityProvider.readFrom(
        DevfileDto.class,
        DevfileDto.class,
        null,
        MediaType.valueOf("text/x-yaml"),
        new MultivaluedHashMap<>(),
        getClass().getClassLoader().getResourceAsStream("devfile/devfile.yaml"));

    verify(devfileParser).parseYaml(anyString());
  }

  @Test
  public void shouldBuildDtoFromValidJson() throws Exception {

    when(devfileParser.parseJson(anyString())).thenReturn(new DevfileImpl());

    devfileEntityProvider.readFrom(
        DevfileDto.class,
        DevfileDto.class,
        null,
        MediaType.APPLICATION_JSON_TYPE,
        new MultivaluedHashMap<>(),
        getClass().getClassLoader().getResourceAsStream("devfile/devfile.json"));

    verify(devfileParser).parseJson(anyString());
  }

  @Test(
      expectedExceptions = NotSupportedException.class,
      expectedExceptionsMessageRegExp = "Unknown media type text/plain")
  public void shouldThrowErrorOnInvalidMediaType() throws Exception {

    devfileEntityProvider.readFrom(
        DevfileDto.class,
        DevfileDto.class,
        null,
        MediaType.TEXT_PLAIN_TYPE,
        new MultivaluedHashMap<>(),
        getClass().getClassLoader().getResourceAsStream("devfile/devfile.json"));
  }
}
