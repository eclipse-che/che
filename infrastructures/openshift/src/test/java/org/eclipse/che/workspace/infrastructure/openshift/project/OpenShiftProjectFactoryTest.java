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

import static java.util.Collections.singletonList;
import static org.eclipse.che.workspace.infrastructure.kubernetes.api.shared.KubernetesNamespaceMeta.DEFAULT_ATTRIBUTE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.api.shared.KubernetesNamespaceMeta.PHASE_ATTRIBUTE;
import static org.eclipse.che.workspace.infrastructure.openshift.Constants.PROJECT_DESCRIPTION_ANNOTATION;
import static org.eclipse.che.workspace.infrastructure.openshift.Constants.PROJECT_DESCRIPTION_ATTRIBUTE;
import static org.eclipse.che.workspace.infrastructure.openshift.Constants.PROJECT_DISPLAY_NAME_ANNOTATION;
import static org.eclipse.che.workspace.infrastructure.openshift.Constants.PROJECT_DISPLAY_NAME_ATTRIBUTE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.openshift.api.model.DoneableProject;
import io.fabric8.openshift.api.model.Project;
import io.fabric8.openshift.api.model.ProjectBuilder;
import io.fabric8.openshift.api.model.ProjectList;
import io.fabric8.openshift.client.OpenShiftClient;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.inject.ConfigurationException;
import org.eclipse.che.workspace.infrastructure.kubernetes.api.shared.KubernetesNamespaceMeta;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientConfigFactory;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientFactory;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link OpenShiftProjectFactory}.
 *
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class OpenShiftProjectFactoryTest {

  @Mock private OpenShiftClientConfigFactory configFactory;
  @Mock private OpenShiftClientFactory clientFactory;

  @Mock
  private NonNamespaceOperation<
          Project, ProjectList, DoneableProject, Resource<Project, DoneableProject>>
      projectOperation;

  @Mock private OpenShiftClient osClient;

  private OpenShiftProjectFactory projectFactory;

  @BeforeMethod
  public void setUp() throws Exception {
    lenient().when(clientFactory.createOC()).thenReturn(osClient);
    lenient().when(osClient.projects()).thenReturn(projectOperation);
  }

  @Test(
      expectedExceptions = ConfigurationException.class,
      expectedExceptionsMessageRegExp =
          "che.infra.kubernetes.namespace.default or "
              + "che.infra.kubernetes.namespace.allow_user_defined must be configured")
  public void
      shouldThrowExceptionIfNoDefaultNamespaceIsConfiguredAndUserDefinedNamespacesAreNotAllowed()
          throws Exception {
    projectFactory =
        new OpenShiftProjectFactory(
            "projectName", "", "", null, false, clientFactory, configFactory);
  }

  @Test
  public void shouldReturnDefaultProjectWhenItExistsAndUserDefinedIsNotAllowed() throws Exception {
    prepareNamespaceToBeFoundByName(
        "che-default",
        new ProjectBuilder()
            .withNewMetadata()
            .withName("che-default")
            .withAnnotations(
                ImmutableMap.of(
                    PROJECT_DISPLAY_NAME_ANNOTATION,
                    "Default Che Project",
                    PROJECT_DESCRIPTION_ANNOTATION,
                    "some description"))
            .endMetadata()
            .withNewStatus("Active")
            .build());

    projectFactory =
        new OpenShiftProjectFactory(
            "predefined", "", "", "che-default", false, clientFactory, configFactory);

    List<KubernetesNamespaceMeta> availableNamespaces = projectFactory.list();
    assertEquals(availableNamespaces.size(), 1);
    KubernetesNamespaceMeta defaultNamespace = availableNamespaces.get(0);
    assertEquals(defaultNamespace.getName(), "che-default");
    assertEquals(defaultNamespace.getAttributes().get(DEFAULT_ATTRIBUTE), "true");
    assertEquals(
        defaultNamespace.getAttributes().get(PROJECT_DISPLAY_NAME_ATTRIBUTE),
        "Default Che Project");
    assertEquals(
        defaultNamespace.getAttributes().get(PROJECT_DESCRIPTION_ATTRIBUTE), "some description");
    assertEquals(defaultNamespace.getAttributes().get(PHASE_ATTRIBUTE), "Active");
  }

  @Test
  public void shouldReturnDefaultProjectWhenItDoesNotExistAndUserDefinedIsNotAllowed()
      throws Exception {
    throwOnTryToGetProjectByName(
        "che-default", new KubernetesClientException("forbidden", 403, null));

    projectFactory =
        new OpenShiftProjectFactory(
            "predefined", "", "", "che-default", false, clientFactory, configFactory);

    List<KubernetesNamespaceMeta> availableNamespaces = projectFactory.list();
    assertEquals(availableNamespaces.size(), 1);
    KubernetesNamespaceMeta defaultNamespace = availableNamespaces.get(0);
    assertEquals(defaultNamespace.getName(), "che-default");
    assertEquals(defaultNamespace.getAttributes().get(DEFAULT_ATTRIBUTE), "true");
    assertNull(
        defaultNamespace
            .getAttributes()
            .get(PHASE_ATTRIBUTE)); // no phase - means such project does not exist
  }

  @Test(
      expectedExceptions = InfrastructureException.class,
      expectedExceptionsMessageRegExp =
          "Error occurred when tried to fetch default project. Cause: connection refused")
  public void shouldThrownExceptionWhenFailedToGetInfoAboutDefaultNamespace() throws Exception {
    throwOnTryToGetProjectByName(
        "che-default", new KubernetesClientException("connection refused"));

    projectFactory =
        new OpenShiftProjectFactory(
            "predefined", "", "", "che-default", false, clientFactory, configFactory);

    projectFactory.list();
  }

  @Test
  public void shouldReturnListOfExistingProjectsIfUserDefinedIsAllowed() throws Exception {
    prepareListedProjects(
        Arrays.asList(
            createProject("my-for-ws", "Project for Workspaces", "some description", "Active"),
            createProject("experimental", null, null, "Terminating")));

    projectFactory =
        new OpenShiftProjectFactory("predefined", "", "", null, true, clientFactory, configFactory);

    List<KubernetesNamespaceMeta> availableNamespaces = projectFactory.list();
    assertEquals(availableNamespaces.size(), 2);
    KubernetesNamespaceMeta forWS = availableNamespaces.get(0);
    assertEquals(forWS.getName(), "my-for-ws");
    assertEquals(
        forWS.getAttributes().get(PROJECT_DISPLAY_NAME_ATTRIBUTE), "Project for Workspaces");
    assertEquals(forWS.getAttributes().get(PROJECT_DESCRIPTION_ATTRIBUTE), "some description");
    assertEquals(forWS.getAttributes().get(PHASE_ATTRIBUTE), "Active");

    KubernetesNamespaceMeta experimental = availableNamespaces.get(1);
    assertEquals(experimental.getName(), "experimental");
    assertEquals(experimental.getAttributes().get(PHASE_ATTRIBUTE), "Terminating");
  }

  @Test
  public void shouldReturnListOfExistingProjectsAlongWithDefaultIfUserDefinedIsAllowed()
      throws Exception {
    prepareListedProjects(
        Arrays.asList(
            createProject("my-for-ws", "Project for Workspaces", "some description", "Active"),
            createProject("default", "Default Che Project", "some description", "Active")));

    projectFactory =
        new OpenShiftProjectFactory(
            "predefined", "", "", "default", true, clientFactory, configFactory);

    List<KubernetesNamespaceMeta> availableNamespaces = projectFactory.list();

    assertEquals(availableNamespaces.size(), 2);
    KubernetesNamespaceMeta forWS = availableNamespaces.get(0);
    assertEquals(forWS.getName(), "my-for-ws");
    assertEquals(
        forWS.getAttributes().get(PROJECT_DISPLAY_NAME_ATTRIBUTE), "Project for Workspaces");
    assertEquals(forWS.getAttributes().get(PROJECT_DESCRIPTION_ATTRIBUTE), "some description");
    assertEquals(forWS.getAttributes().get(PHASE_ATTRIBUTE), "Active");
    assertNull(forWS.getAttributes().get(DEFAULT_ATTRIBUTE));

    KubernetesNamespaceMeta defaultNamespace = availableNamespaces.get(1);
    assertEquals(defaultNamespace.getName(), "default");
    assertEquals(
        defaultNamespace.getAttributes().get(PROJECT_DISPLAY_NAME_ATTRIBUTE),
        "Default Che Project");
    assertEquals(
        defaultNamespace.getAttributes().get(PROJECT_DESCRIPTION_ATTRIBUTE), "some description");
    assertEquals(defaultNamespace.getAttributes().get(PHASE_ATTRIBUTE), "Active");
    assertEquals(defaultNamespace.getAttributes().get(DEFAULT_ATTRIBUTE), "true");
  }

  @Test
  public void shouldReturnListOfExistingProjectsAlongWithNonExistingDefaultIfUserDefinedIsAllowed()
      throws Exception {
    prepareListedProjects(singletonList(createProject("my-for-ws", "", "", "Active")));

    projectFactory =
        new OpenShiftProjectFactory(
            "predefined", "", "", "default", true, clientFactory, configFactory);

    List<KubernetesNamespaceMeta> availableNamespaces = projectFactory.list();
    assertEquals(availableNamespaces.size(), 2);
    KubernetesNamespaceMeta forWS = availableNamespaces.get(0);
    assertEquals(forWS.getName(), "my-for-ws");
    assertEquals(forWS.getAttributes().get(PHASE_ATTRIBUTE), "Active");
    assertNull(forWS.getAttributes().get(DEFAULT_ATTRIBUTE));

    KubernetesNamespaceMeta defaultNamespace = availableNamespaces.get(1);
    assertEquals(defaultNamespace.getName(), "default");
    assertEquals(defaultNamespace.getAttributes().get(DEFAULT_ATTRIBUTE), "true");
    assertNull(
        defaultNamespace
            .getAttributes()
            .get(PHASE_ATTRIBUTE)); // no phase - means such namespace does not exist
  }

  @Test(
      expectedExceptions = InfrastructureException.class,
      expectedExceptionsMessageRegExp =
          "Error occurred when tried to list all available projects. Cause: connection refused")
  public void shouldThrownExceptionWhenFailedToGetNamespaces() throws Exception {
    throwOnTryToGetProjectsList(new KubernetesClientException("connection refused"));
    projectFactory =
        new OpenShiftProjectFactory("predefined", "", "", "", true, clientFactory, configFactory);

    projectFactory.list();
  }

  @Test
  public void shouldCreateAndPrepareProjectWithPredefinedValueIfItIsNotEmpty() throws Exception {
    // given
    projectFactory =
        spy(
            new OpenShiftProjectFactory(
                "projectName", "", "", "che", false, clientFactory, configFactory));
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
    projectFactory =
        spy(new OpenShiftProjectFactory("", "", "", "che", false, clientFactory, configFactory));
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
    projectFactory =
        spy(
            new OpenShiftProjectFactory(
                "", "serviceAccount", "", "che", false, clientFactory, configFactory));
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
    projectFactory =
        spy(
            new OpenShiftProjectFactory(
                "namespace",
                "serviceAccount",
                "clusterRole",
                "che",
                false,
                clientFactory,
                configFactory));
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
    projectFactory =
        spy(new OpenShiftProjectFactory("", "", "", "che", false, clientFactory, configFactory));
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
        spy(
            new OpenShiftProjectFactory(
                "projectName",
                "serviceAccountName",
                "clusterRole",
                "che",
                false,
                clientFactory,
                configFactory));
    OpenShiftProject toReturnProject = mock(OpenShiftProject.class);
    doReturn(toReturnProject).when(projectFactory).doCreateProject(any(), any());

    // when
    KubernetesNamespace namespace = projectFactory.create("workspace123", "name");

    // then
    assertEquals(toReturnProject, namespace);
    verify(projectFactory).doCreateProject("workspace123", "name");
    verify(toReturnProject, never()).prepare();
  }

  private void prepareNamespaceToBeFoundByName(String name, Project project) throws Exception {
    @SuppressWarnings("unchecked")
    Resource<Project, DoneableProject> getProjectByNameOperation = mock(Resource.class);
    when(projectOperation.withName(name)).thenReturn(getProjectByNameOperation);

    when(getProjectByNameOperation.get()).thenReturn(project);
  }

  private void throwOnTryToGetProjectByName(String name, KubernetesClientException e)
      throws Exception {
    @SuppressWarnings("unchecked")
    Resource<Project, DoneableProject> getProjectByNameOperation = mock(Resource.class);
    when(projectOperation.withName(name)).thenReturn(getProjectByNameOperation);

    when(getProjectByNameOperation.get()).thenThrow(e);
  }

  private void prepareListedProjects(List<Project> projects) throws Exception {
    @SuppressWarnings("unchecked")
    ProjectList projectList = mock(ProjectList.class);
    when(projectOperation.list()).thenReturn(projectList);

    when(projectList.getItems()).thenReturn(projects);
  }

  private void throwOnTryToGetProjectsList(Throwable e) throws Exception {
    when(projectOperation.list()).thenThrow(e);
  }

  private Project createProject(String name, String displayName, String description, String phase) {
    Map<String, String> annotations = new HashMap<>();
    if (displayName != null) {
      annotations.put(PROJECT_DISPLAY_NAME_ANNOTATION, displayName);
    }
    if (description != null) {
      annotations.put(PROJECT_DESCRIPTION_ANNOTATION, description);
    }

    return new ProjectBuilder()
        .withNewMetadata()
        .withName(name)
        .withAnnotations(annotations)
        .endMetadata()
        .withNewStatus(phase)
        .build();
  }
}
