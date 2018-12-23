/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.factory.server.urlfactory;

import static org.eclipse.che.api.factory.shared.Constants.CURRENT_VERSION;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_TOOLING_EDITOR_ATTRIBUTE;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_TOOLING_PLUGINS_ATTRIBUTE;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.devfile.server.DevfileManager;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.dto.server.DtoFactory;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Testing {@link URLFactoryBuilder}
 *
 * @author Florent Benoit
 * @author Max Shaposhnyk
 */
@Listeners(MockitoTestNGListener.class)
public class URLFactoryBuilderTest {

  private final String defaultEditor = "org.eclipse.che.editor.theia:1.0.0";
  private final String defaultPlugin = "che-machine-exec-plugin:0.0.1";
  /** Grab content of URLs */
  @Mock private URLFetcher urlFetcher;

  @Mock private DevfileManager devfileManager;

  /** Tested instance. */
  private URLFactoryBuilder urlFactoryBuilder;

  @BeforeClass
  public void setUp() {
    this.urlFactoryBuilder =
        new URLFactoryBuilder(defaultEditor, defaultPlugin, urlFetcher, devfileManager);
  }

  @Test
  public void checkDefaultConfiguration() throws Exception {
    Map<String, String> attributes = new HashMap<>();
    attributes.put(WORKSPACE_TOOLING_EDITOR_ATTRIBUTE, defaultEditor);
    attributes.put(WORKSPACE_TOOLING_PLUGINS_ATTRIBUTE, defaultPlugin);
    // setup environment
    WorkspaceConfigDto expectedWsConfig =
        newDto(WorkspaceConfigDto.class).withAttributes(attributes).withName("foo");

    WorkspaceConfigDto actualWsConfigDto = urlFactoryBuilder.buildDefaultWorkspaceConfig("foo");

    assertEquals(actualWsConfigDto, expectedWsConfig);
  }

  /** Check that with a custom factory.json we've this factory being built */
  @Test
  public void checkWithCustomFactoryJsonFile() throws Exception {

    WorkspaceConfigDto workspaceConfigDto = newDto(WorkspaceConfigDto.class);
    FactoryDto templateFactory =
        newDto(FactoryDto.class)
            .withV(CURRENT_VERSION)
            .withName("florent")
            .withWorkspace(workspaceConfigDto);
    String jsonFactory = DtoFactory.getInstance().toJson(templateFactory);

    String myLocation = "http://foo-location";
    when(urlFetcher.fetch(myLocation)).thenReturn(jsonFactory);

    FactoryDto factory = urlFactoryBuilder.createFactoryFromJson(myLocation).get();

    assertEquals(templateFactory, factory);
  }
}
