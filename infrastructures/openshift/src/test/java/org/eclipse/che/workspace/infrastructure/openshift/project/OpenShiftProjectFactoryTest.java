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
package org.eclipse.che.workspace.infrastructure.openshift.project;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientFactory;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link OpenShiftProjectFactory}.
 *
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class OpenShiftProjectFactoryTest {
  @Mock private OpenShiftClientFactory clientFactory;
  private OpenShiftProjectFactory projectFactory;

  @Test
  public void shouldCreateAndPrepareProjectWithPredefinedValueIfItIsNotEmpty() throws Exception {
    // given
    projectFactory = spy(new OpenShiftProjectFactory("projectName", "", clientFactory));
    OpenShiftProject toReturnProject = mock(OpenShiftProject.class);
    doReturn(toReturnProject).when(projectFactory).doCreateProject(any(), any());

    // when
    OpenShiftProject project = projectFactory.create("workspace123");

    // then
    assertEquals(toReturnProject, project);
    verify(projectFactory).doCreateProject("workspace123", "projectName");
    verify(toReturnProject).prepare();
  }

  @Test
  public void shouldCreateAndPrepareProjectWithWorkspaceIdAsNameIfConfiguredValueIsEmtpy()
      throws Exception {
    // given
    projectFactory = spy(new OpenShiftProjectFactory("", "", clientFactory));
    OpenShiftProject toReturnProject = mock(OpenShiftProject.class);
    doReturn(toReturnProject).when(projectFactory).doCreateProject(any(), any());

    // when
    OpenShiftProject project = projectFactory.create("workspace123");

    // then
    assertEquals(toReturnProject, project);
    verify(projectFactory).doCreateProject("workspace123", "workspace123");
    verify(toReturnProject).prepare();
  }

  @Test
  public void shouldPrepareWorkspaceServiceAccountIfItIsConfiguredAndProjectIsNotPredefined()
      throws Exception {
    // given
    projectFactory = spy(new OpenShiftProjectFactory("", "serviceAccount", clientFactory));
    OpenShiftProject toReturnProject = mock(OpenShiftProject.class);
    doReturn(toReturnProject).when(projectFactory).doCreateProject(any(), any());

    OpenShiftWorkspaceServiceAccount serviceAccount = mock(OpenShiftWorkspaceServiceAccount.class);
    doReturn(serviceAccount).when(projectFactory).doCreateServiceAccount(any(), any());

    // when
    projectFactory.create("workspace123");

    // then
    verify(projectFactory).doCreateServiceAccount("workspace123", "workspace123");
    verify(serviceAccount).prepare();
  }

  @Test
  public void shouldNotPrepareWorkspaceServiceAccountIfItIsConfiguredAndProjectIsPredefined()
      throws Exception {
    // given
    projectFactory = spy(new OpenShiftProjectFactory("namespace", "serviceAccount", clientFactory));
    OpenShiftProject toReturnProject = mock(OpenShiftProject.class);
    doReturn(toReturnProject).when(projectFactory).doCreateProject(any(), any());

    // when
    projectFactory.create("workspace123");

    // then
    verify(projectFactory, never()).doCreateServiceAccount(any(), any());
  }

  @Test
  public void shouldNotPrepareWorkspaceServiceAccountIfItIsNotConfiguredAndProjectIsNotPredefined()
      throws Exception {
    // given
    projectFactory = spy(new OpenShiftProjectFactory("", "", clientFactory));
    OpenShiftProject toReturnProject = mock(OpenShiftProject.class);
    doReturn(toReturnProject).when(projectFactory).doCreateProject(any(), any());

    // when
    projectFactory.create("workspace123");

    // then
    verify(projectFactory, never()).doCreateServiceAccount(any(), any());
  }

  @Test
  public void
      shouldCreateProjectAndDoNotPrepareProjectOnCreatingProjectWithWorkspaceIdAndNameSpecified()
          throws Exception {
    // given
    projectFactory =
        spy(new OpenShiftProjectFactory("projectName", "serviceAccountName", clientFactory));
    OpenShiftProject toReturnProject = mock(OpenShiftProject.class);
    doReturn(toReturnProject).when(projectFactory).doCreateProject(any(), any());

    // when
    KubernetesNamespace namespace = projectFactory.create("workspace123", "name");

    // then
    assertEquals(toReturnProject, namespace);
    verify(projectFactory).doCreateProject("workspace123", "name");
    verify(toReturnProject, never()).prepare();
  }
}
