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
package org.eclipse.che.api.workspace.server.devfile.convert;

import static org.eclipse.che.api.workspace.server.devfile.Constants.CURRENT_API_VERSION;
import static org.eclipse.che.api.workspace.shared.Constants.PERSIST_VOLUMES_ATTRIBUTE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.workspace.server.devfile.FileContentProvider;
import org.eclipse.che.api.workspace.server.devfile.URLFetcher;
import org.eclipse.che.api.workspace.server.devfile.convert.component.ComponentProvisioner;
import org.eclipse.che.api.workspace.server.devfile.convert.component.ComponentToWorkspaceApplier;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileException;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileFormatException;
import org.eclipse.che.api.workspace.server.devfile.exception.WorkspaceExportException;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.DevfileImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ProjectImpl;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class DevfileConverterTest {

  public static final String COMPONENT_TYPE = "componentType";
  @Mock private ProjectConverter projectConverter;
  @Mock private CommandConverter commandConverter;
  @Mock private ComponentProvisioner componentProvisioner;
  @Mock private ComponentToWorkspaceApplier componentToWorkspaceApplier;
  @Mock private DefaultEditorProvisioner defaultEditorToolApplier;
  @Mock private URLFetcher urlFetcher;

  private DevfileConverter devfileConverter;

  @BeforeMethod
  public void setUp() {
    devfileConverter =
        new DevfileConverter(
            projectConverter,
            commandConverter,
            ImmutableSet.of(componentProvisioner),
            ImmutableMap.of(COMPONENT_TYPE, componentToWorkspaceApplier),
            defaultEditorToolApplier,
            urlFetcher);
  }

  @Test
  public void shouldUseWorkspaceConfigNameAsDevfileNameDuringConvertingWorkspaceConfigToDevfile()
      throws Exception {
    // given
    WorkspaceConfigImpl wsConfig = new WorkspaceConfigImpl();
    wsConfig.setName("petclinic");

    // when
    DevfileImpl devfile = devfileConverter.workspaceToDevFile(wsConfig);

    // then
    assertEquals(devfile.getName(), "petclinic");
  }

  @Test
  public void shouldUseCurrentSpecVersionDuringConvertingWorkspaceConfigToDevfile()
      throws Exception {
    // given
    WorkspaceConfigImpl wsConfig = new WorkspaceConfigImpl();

    // when
    DevfileImpl devfile = devfileConverter.workspaceToDevFile(wsConfig);

    // then
    assertEquals(devfile.getApiVersion(), CURRENT_API_VERSION);
  }

  @Test
  public void shouldConvertCommandsDuringConvertingWorkspaceConfigToDevfile() throws Exception {
    // given
    WorkspaceConfigImpl wsConfig = new WorkspaceConfigImpl();
    org.eclipse.che.api.workspace.server.model.impl.CommandImpl workspaceCommand =
        mock(org.eclipse.che.api.workspace.server.model.impl.CommandImpl.class);
    wsConfig.getCommands().add(workspaceCommand);

    CommandImpl devfileCommand = mock(CommandImpl.class);
    when(commandConverter.toDevfileCommand(any())).thenReturn(devfileCommand);

    // when
    DevfileImpl devfile = devfileConverter.workspaceToDevFile(wsConfig);

    // then
    assertEquals(devfile.getCommands().size(), 1);
    assertSame(devfile.getCommands().get(0), devfileCommand);
  }

  @Test
  public void shouldConvertProjectsDuringConvertingWorkspaceConfigToDevfile() throws Exception {
    // given
    WorkspaceConfigImpl wsConfig = new WorkspaceConfigImpl();
    ProjectConfigImpl workspaceProject = mock(ProjectConfigImpl.class);
    wsConfig.getProjects().add(workspaceProject);

    ProjectImpl devfileProject = mock(ProjectImpl.class);
    when(projectConverter.toDevfileProject(any())).thenReturn(devfileProject);

    // when
    DevfileImpl devfile = devfileConverter.workspaceToDevFile(wsConfig);

    // then
    assertEquals(devfile.getProjects().size(), 1);
    assertSame(devfile.getProjects().get(0), devfileProject);
  }

  @Test
  public void shouldConvertComponentsDuringConvertingWorkspaceConfigToDevfile() throws Exception {
    // given
    WorkspaceConfigImpl wsConfig = new WorkspaceConfigImpl();

    // when
    DevfileImpl devfile = devfileConverter.workspaceToDevFile(wsConfig);

    // then
    verify(componentProvisioner).provision(devfile, wsConfig);
  }

  @Test(
      expectedExceptions = WorkspaceExportException.class,
      expectedExceptionsMessageRegExp =
          "Workspace 'ws123' cannot be converted to devfile because it contains multiple environments")
  public void shouldThrowExceptionIfWorkspaceHasMultipleEnvironmentDuringConvertingToDevfile()
      throws Exception {
    // given
    WorkspaceConfigImpl wsConfig = new WorkspaceConfigImpl();
    wsConfig.setName("ws123");
    wsConfig.getEnvironments().put("env1", new EnvironmentImpl());
    wsConfig.getEnvironments().put("env2", new EnvironmentImpl());

    // when
    devfileConverter.workspaceToDevFile(wsConfig);
  }

  @Test
  public void shouldUseDevfileNameForWorkspaceNameDuringConvertingDevfileToWorkspaceConfig()
      throws Exception {
    // given
    FileContentProvider fileContentProvider = mock(FileContentProvider.class);
    DevfileImpl devfile = newDevfile("petclinic");

    // when
    WorkspaceConfigImpl workspaceConfig =
        devfileConverter.devFileToWorkspaceConfig(devfile, fileContentProvider);

    // then
    assertEquals(workspaceConfig.getName(), "petclinic");
  }

  @Test
  public void shouldInvokeDefaultEditorProvisionerDuringConvertingDevfileToWorkrspaceConfig()
      throws Exception {
    // given
    FileContentProvider fileContentProvider = mock(FileContentProvider.class);
    DevfileImpl devfile = newDevfile("petclinic");

    // when
    devfileConverter.devFileToWorkspaceConfig(devfile, fileContentProvider);

    // then
    verify(defaultEditorToolApplier).apply(devfile, fileContentProvider);
  }

  @Test
  public void
      shouldProvisionDevfileAttributesAsConfigAttributesDuringConvertingDevfileToWorkspaceConfig()
          throws Exception {
    // given
    FileContentProvider fileContentProvider = mock(FileContentProvider.class);
    Map<String, String> devfileAttributes = new HashMap<>();
    devfileAttributes.put(PERSIST_VOLUMES_ATTRIBUTE, "false");
    devfileAttributes.put("anotherAttribute", "value");

    DevfileImpl devfile = newDevfile("petclinic");
    devfile.getAttributes().putAll(devfileAttributes);

    // when
    WorkspaceConfigImpl config =
        devfileConverter.devFileToWorkspaceConfig(devfile, fileContentProvider);

    // then
    assertEquals(config.getAttributes(), devfileAttributes);
  }

  @Test
  public void shouldConvertCommandsDuringConvertingDevfileToWorkspaceConfig() throws Exception {
    // given
    FileContentProvider fileContentProvider = mock(FileContentProvider.class);
    DevfileImpl devfile = newDevfile("petclinic");
    CommandImpl devfileCommand = mock(CommandImpl.class);
    devfile.getCommands().add(devfileCommand);

    org.eclipse.che.api.workspace.server.model.impl.CommandImpl workspaceCommand =
        mock(org.eclipse.che.api.workspace.server.model.impl.CommandImpl.class);
    when(commandConverter.toWorkspaceCommand(any(), any())).thenReturn(workspaceCommand);

    // when
    WorkspaceConfigImpl workspaceConfig =
        devfileConverter.devFileToWorkspaceConfig(devfile, fileContentProvider);

    // then
    assertEquals(workspaceConfig.getCommands().size(), 1);
    assertSame(workspaceConfig.getCommands().get(0), workspaceCommand);
  }

  @Test
  public void shouldConvertProjectsDuringConvertingDevfileToWorkspaceConfig() throws Exception {
    // given
    FileContentProvider fileContentProvider = mock(FileContentProvider.class);
    DevfileImpl devfile = newDevfile("petclinic");
    ProjectImpl devfileProject = mock(ProjectImpl.class);
    devfile.getProjects().add(devfileProject);

    ProjectConfigImpl workspaceProject = mock(ProjectConfigImpl.class);
    when(projectConverter.toWorkspaceProject(any())).thenReturn(workspaceProject);

    // when
    WorkspaceConfigImpl workspaceConfig =
        devfileConverter.devFileToWorkspaceConfig(devfile, fileContentProvider);

    // then
    assertEquals(workspaceConfig.getProjects().size(), 1);
    assertSame(workspaceConfig.getProjects().get(0), workspaceProject);
  }

  @Test
  public void shouldConvertComponentsDuringConvertingDevfileToWorkspaceConfig() throws Exception {
    // given
    DevfileImpl devfile = newDevfile("petclinic");
    ComponentImpl component = new ComponentImpl();
    component.setType(COMPONENT_TYPE);
    devfile.getComponents().add(component);

    FileContentProvider fileContentProvider = mock(FileContentProvider.class);

    // when
    WorkspaceConfigImpl workspaceConfig =
        devfileConverter.devFileToWorkspaceConfig(devfile, fileContentProvider);

    // then
    verify(componentToWorkspaceApplier).apply(workspaceConfig, component, fileContentProvider);
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp = "Provided Devfile has no API version specified")
  public void
      shouldThrowAnExceptionIfDevfileApiVersionIsMissingDuringConvertingDevfileToWorkspaceConfig()
          throws Exception {
    // given
    FileContentProvider fileContentProvider = mock(FileContentProvider.class);
    DevfileImpl devfile = new DevfileImpl();
    devfile.setName("petclinic");

    // when
    devfileConverter.devFileToWorkspaceConfig(devfile, fileContentProvider);
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp =
          "Provided Devfile has unsupported version '1\\.0\\.0-non-supported'. The following versions are supported: .*")
  public void
      shouldThrowAnExceptionIfDevfileApiVersionIsNotSupportedDuringConvertingDevfileToWorkspaceConfig()
          throws Exception {
    // given
    FileContentProvider fileContentProvider = mock(FileContentProvider.class);
    DevfileImpl devfile = new DevfileImpl();
    devfile.setApiVersion("1.0.0-non-supported");
    devfile.setName("petclinic");

    // when
    devfileConverter.devFileToWorkspaceConfig(devfile, fileContentProvider);
  }

  @Test
  public void shouldConvertDevfileToWorkspaceConfig() throws Exception {
    devfileConverter = spy(devfileConverter);
    WorkspaceConfigImpl wsConfig = new WorkspaceConfigImpl();
    wsConfig.setName("converted");
    wsConfig.getAttributes().put("att", "value");
    doReturn(wsConfig).when(devfileConverter).devFileToWorkspaceConfig(any(), any());

    WorkspaceConfig converted = devfileConverter.convert(new DevfileImpl());

    assertEquals(converted, wsConfig);
  }

  @Test(expectedExceptions = ServerException.class, expectedExceptionsMessageRegExp = "error")
  public void
      shouldThrowServerExceptionIfAnyDevfileExceptionOccursOnConvertingDevfileToWorkspaceConfig()
          throws Exception {
    devfileConverter = spy(devfileConverter);
    doThrow(new DevfileException("error"))
        .when(devfileConverter)
        .devFileToWorkspaceConfig(any(), any());

    devfileConverter.convert(new DevfileImpl());
  }

  private DevfileImpl newDevfile(String name) {
    DevfileImpl devfile = new DevfileImpl();
    devfile.setApiVersion(CURRENT_API_VERSION);
    devfile.setName(name);
    return devfile;
  }
}
