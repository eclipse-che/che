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
package org.eclipse.che.api.devfile.server.convert;

import static org.eclipse.che.api.devfile.server.Constants.CURRENT_SPEC_VERSION;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.eclipse.che.api.devfile.model.Command;
import org.eclipse.che.api.devfile.model.Component;
import org.eclipse.che.api.devfile.model.Devfile;
import org.eclipse.che.api.devfile.model.Project;
import org.eclipse.che.api.devfile.server.FileContentProvider;
import org.eclipse.che.api.devfile.server.convert.component.ComponentProvisioner;
import org.eclipse.che.api.devfile.server.convert.component.ComponentToWorkspaceApplier;
import org.eclipse.che.api.devfile.server.exception.DevfileFormatException;
import org.eclipse.che.api.devfile.server.exception.WorkspaceExportException;
import org.eclipse.che.api.workspace.server.model.impl.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
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

  private DevfileConverter devfileConverter;

  @BeforeMethod
  public void setUp() {
    devfileConverter =
        new DevfileConverter(
            projectConverter,
            commandConverter,
            ImmutableSet.of(componentProvisioner),
            ImmutableMap.of(COMPONENT_TYPE, componentToWorkspaceApplier),
            defaultEditorToolApplier);
  }

  @Test
  public void shouldUseWorkspaceConfigNameAsDevfileNameDuringConvertingWorkspaceConfigToDevfile()
      throws Exception {
    // given
    WorkspaceConfigImpl wsConfig = new WorkspaceConfigImpl();
    wsConfig.setName("petclinic");

    // when
    Devfile devfile = devfileConverter.workspaceToDevFile(wsConfig);

    // then
    assertEquals(devfile.getName(), "petclinic");
  }

  @Test
  public void shouldUseCurrentSpecVersionDuringConvertingWorkspaceConfigToDevfile()
      throws Exception {
    // given
    WorkspaceConfigImpl wsConfig = new WorkspaceConfigImpl();

    // when
    Devfile devfile = devfileConverter.workspaceToDevFile(wsConfig);

    // then
    assertEquals(devfile.getSpecVersion(), CURRENT_SPEC_VERSION);
  }

  @Test
  public void shouldConvertCommandsDuringConvertingWorkspaceConfigToDevfile() throws Exception {
    // given
    WorkspaceConfigImpl wsConfig = new WorkspaceConfigImpl();
    CommandImpl workspaceCommand = mock(CommandImpl.class);
    wsConfig.getCommands().add(workspaceCommand);

    Command devfileCommand = mock(Command.class);
    when(commandConverter.toDevfileCommand(any())).thenReturn(devfileCommand);

    // when
    Devfile devfile = devfileConverter.workspaceToDevFile(wsConfig);

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

    Project devfileProject = mock(Project.class);
    when(projectConverter.toDevfileProject(any())).thenReturn(devfileProject);

    // when
    Devfile devfile = devfileConverter.workspaceToDevFile(wsConfig);

    // then
    assertEquals(devfile.getProjects().size(), 1);
    assertSame(devfile.getProjects().get(0), devfileProject);
  }

  @Test
  public void shouldConvertComponentsDuringConvertingWorkspaceConfigToDevfile() throws Exception {
    // given
    WorkspaceConfigImpl wsConfig = new WorkspaceConfigImpl();

    // when
    Devfile devfile = devfileConverter.workspaceToDevFile(wsConfig);

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
    Devfile devfile = newDevfile("petclinic");

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
    Devfile devfile = newDevfile("petclinic");

    // when
    devfileConverter.devFileToWorkspaceConfig(devfile, fileContentProvider);

    // then
    verify(defaultEditorToolApplier).apply(devfile);
  }

  @Test
  public void shouldConvertCommandsDuringConvertingDevfileToWorkspaceConfig() throws Exception {
    // given
    FileContentProvider fileContentProvider = mock(FileContentProvider.class);
    Devfile devfile = newDevfile("petclinic");
    Command devfileCommand = mock(Command.class);
    devfile.getCommands().add(devfileCommand);

    CommandImpl workspaceCommand = mock(CommandImpl.class);
    when(commandConverter.toWorkspaceCommand(any())).thenReturn(workspaceCommand);

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
    Devfile devfile = newDevfile("petclinic");
    Project devfileProject = mock(Project.class);
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
    Devfile devfile = newDevfile("petclinic");
    Component component = new Component();
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
      expectedExceptionsMessageRegExp = "Provided Devfile has no spec version specified")
  public void
      shouldThrowAnExceptionIfDevfileSpecVersionIsMissingDuringConvertingDevfileToWorkspaceConfig()
          throws Exception {
    // given
    FileContentProvider fileContentProvider = mock(FileContentProvider.class);
    Devfile devfile = new Devfile();
    devfile.setName("petclinic");

    // when
    devfileConverter.devFileToWorkspaceConfig(devfile, fileContentProvider);
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp =
          "Provided Devfile has unsupported version '1\\.0\\.0-non-supported'")
  public void
      shouldThrowAnExceptionIfDevfileSpecVersionIsNotSupportedDuringConvertingDevfileToWorkspaceConfig()
          throws Exception {
    // given
    FileContentProvider fileContentProvider = mock(FileContentProvider.class);
    Devfile devfile = new Devfile();
    devfile.setSpecVersion("1.0.0-non-supported");
    devfile.setName("petclinic");

    // when
    devfileConverter.devFileToWorkspaceConfig(devfile, fileContentProvider);
  }

  private Devfile newDevfile(String name) {
    Devfile devfile = new Devfile();
    devfile.setSpecVersion(CURRENT_SPEC_VERSION);
    devfile.setName(name);
    return devfile;
  }
}
